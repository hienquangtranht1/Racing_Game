package racinggame;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class GameServer {
    // --- CẤU HÌNH ---
    private static final String SERVER_IP = "10.241.75.71"; 
    private static final int TCP_PORT = 12345;
    private static final int MAX_CARS = 3; 
    
    private static final double ROAD_LEFT = 50.0;
    private static final double ROAD_RIGHT = 550.0;
    private static final double FINISH_LINE_Y = 4000.0; 
    
    // ĐỊNH NGHĨA LANE (Làn đường)
    // Lane 1 tâm 150 (75-225), Lane 2 tâm 300 (225-375), Lane 3 tâm 450 (375-525)
    private static final int LANE_WIDTH = 150;
    
    private final Map<Integer, CarState> carStates = new ConcurrentHashMap<>();
    private final Map<Integer, InetSocketAddress> clientAddresses = new ConcurrentHashMap<>();
    private final List<Obstacle> obstacles = Collections.synchronizedList(new ArrayList<>());

    private volatile int nextCarId = 1;
    private volatile boolean gameStarted = false;
    private volatile int winnerId = -1;

    public static void main(String[] args) {
        new GameServer().startServer();
    }

    public void startServer() {
        System.out.println(">>> SERVER SURVIVAL (3 PLAYERS): " + SERVER_IP);
        System.out.println(">>> Waiting for players...");

        generateObstacles();
        new UdpBroadcastThread().start();

        try {
            InetAddress bindAddr = InetAddress.getByName(SERVER_IP);
            try (ServerSocket serverSocket = new ServerSocket(TCP_PORT, 50, bindAddr)) {
                while (true) {
                    if (!gameStarted && carStates.size() < MAX_CARS) {
                        Socket clientSocket = serverSocket.accept();
                        int assignedId = nextCarId++;
                        
                        // Vị trí xuất phát chuẩn của từng xe (Tâm Lane)
                        double laneX = 150 + (assignedId - 1) * LANE_WIDTH; 
                        CarState cs = new CarState(assignedId, laneX, 0.0);
                        carStates.put(assignedId, cs);
                        
                        System.out.println("Player P" + assignedId + " connected.");
                        new ClientHandler(clientSocket, assignedId).start();
                    } else {
                        Thread.sleep(100); 
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private synchronized void resetGame() {
        System.out.println(">>> RESET GAME REQUESTED");
        gameStarted = false;
        winnerId = -1;
        generateObstacles();
        
        for (CarState cs : carStates.values()) {
            cs.y = 0;
            cs.speed = 0;
            cs.isEliminated = false;
            cs.isReady = false; 
            cs.slowUntilTime = 0; // Reset trạng thái chậm
            cs.x = 150 + (cs.carId - 1) * LANE_WIDTH; // Về đúng làn
        }
    }

    private void generateObstacles() {
        obstacles.clear();
        Random rnd = new Random();
        int obsId = 1;
        for (double y = 800; y < FINISH_LINE_Y - 500; y += 200) { 
            double x = 100 + rnd.nextInt(400); 
            obstacles.add(new Obstacle(obsId++, x, y, 40, 40));
        }
    }

    private void checkStartGame() {
        if (carStates.size() == MAX_CARS) {
            boolean allReady = true;
            for (CarState cs : carStates.values()) {
                if (!cs.isReady) {
                    allReady = false;
                    break;
                }
            }
            if (allReady && !gameStarted) {
                gameStarted = true;
                winnerId = -1;
                new GameLoopThread().start();
                System.out.println("=== GAME STARTED ===");
            }
        }
    }

    class GameLoopThread extends Thread {
        @Override
        public void run() {
            long last = System.currentTimeMillis();
            
            while (gameStarted && winnerId == -1) {
                long now = System.currentTimeMillis();
                double dt = (now - last) / 1000.0;
                last = now;

                List<CarState> aliveCars = new ArrayList<>();
                List<CarState> carsToCheck = new ArrayList<>(carStates.values());

                for (CarState car : carsToCheck) {
                    synchronized (car) {
                        if (car.isEliminated) continue;

                        // --- LOGIC 1: Xử lý bị làm chậm (3s) ---
                        if (now < car.slowUntilTime) {
                            // Nếu đang bị phạt, tốc độ bị giới hạn cứng ở mức thấp (ví dụ 50)
                            car.speed = 50.0;
                        }

                        car.y += car.speed * dt;

                        // Check Win
                        if (car.y >= FINISH_LINE_Y) {
                            winnerId = car.carId;
                            break; 
                        }

                        // Va chạm TƯỜNG
                        if (car.x - 20 < ROAD_LEFT || car.x + 20 > ROAD_RIGHT) {
                            eliminateCar(car, "Đâm tường");
                        }

                        // Va chạm VẬT CẢN
                        for (Obstacle o : obstacles) {
                            if (!o.active) continue;
                            if (Math.abs(car.x - o.x) < (20 + o.width/2.0) && 
                                Math.abs(car.y - o.y) < (20 + o.height/2.0)) {
                                eliminateCar(car, "Đâm vật cản");
                            }
                        }
                    }
                }
                
                // --- LOGIC 2: Xử lý va chạm giữa 2 xe (Xe lấn làn thua) ---
                // Duyệt qua từng cặp xe
                for (int i = 0; i < carsToCheck.size(); i++) {
                    for (int j = i + 1; j < carsToCheck.size(); j++) {
                        CarState c1 = carsToCheck.get(i);
                        CarState c2 = carsToCheck.get(j);

                        if (c1.isEliminated || c2.isEliminated) continue;

                        double dist = Math.sqrt(Math.pow(c1.x - c2.x, 2) + Math.pow(c1.y - c2.y, 2));
                        
                        // Nếu va chạm (khoảng cách < 40)
                        if (dist < 40.0) {
                            // Xác định xe nào đang ở làn của mình (Chủ nhà)
                            boolean c1InHome = isCarInOwnLane(c1);
                            boolean c2InHome = isCarInOwnLane(c2);

                            if (c1InHome && !c2InHome) {
                                // C2 lấn làn C1 -> C2 thua, C1 bị chậm 3s
                                eliminateCar(c2, "Lấn làn đâm P" + c1.carId);
                                applySlowPenalty(c1);
                            } else if (!c1InHome && c2InHome) {
                                // C1 lấn làn C2 -> C1 thua, C2 bị chậm 3s
                                eliminateCar(c1, "Lấn làn đâm P" + c2.carId);
                                applySlowPenalty(c2);
                            } else {
                                // Cả 2 đều không ở làn mình (va chạm ở giữa đường biên) hoặc cả 2 đều sai
                                // Xử lý: Cả 2 cùng chết (như cũ) hoặc 1 random. 
                                // Ở đây giữ logic cũ cho trường hợp nhập nhằng: Cả 2 loại
                                eliminateCar(c1, "Tai nạn hỗn hợp");
                                eliminateCar(c2, "Tai nạn hỗn hợp");
                            }
                        }
                    }
                }

                // Check Alive
                for (CarState c : carStates.values()) if (!c.isEliminated) aliveCars.add(c);
                
                if (winnerId != -1) break;

                if (aliveCars.size() == 1 && carStates.size() > 1) {
                    winnerId = aliveCars.get(0).carId;
                    System.out.println(">>> WINNER (SURVIVOR): P" + winnerId);
                } else if (aliveCars.isEmpty() && carStates.size() > 0) {
                    winnerId = 0; 
                    System.out.println(">>> DRAW (ALL DEAD)");
                }

                try { Thread.sleep(30); } catch (Exception e) {}
            }
        }

        // Hàm kiểm tra xe có đang ở trong làn của mình không
        private boolean isCarInOwnLane(CarState c) {
            // Lane 1: 75-225, Lane 2: 225-375, Lane 3: 375-525
            double center = 150 + (c.carId - 1) * 150;
            // Cho phép sai số +/- 60 đơn vị quanh tâm làn (Làn rộng 150)
            return Math.abs(c.x - center) <= 60; 
        }

        private void applySlowPenalty(CarState car) {
            System.out.println("P" + car.carId + " bị làm chậm 3 giây do va chạm!");
            car.slowUntilTime = System.currentTimeMillis() + 3000; // 3 giây từ bây giờ
            car.speed = 50.0; // Giảm tốc ngay lập tức
        }

        private void eliminateCar(CarState car, String reason) {
            if (!car.isEliminated) {
                car.isEliminated = true;
                car.speed = 0;
                System.out.println("P" + car.carId + " BỊ LOẠI: " + reason);
            }
        }
    }

    class UdpBroadcastThread extends Thread {
        @Override
        public void run() {
            try {
                InetAddress bindAddr = InetAddress.getByName(SERVER_IP);
                try (DatagramSocket ds = new DatagramSocket(0, bindAddr)) {
                    while (true) {
                        if (!clientAddresses.isEmpty()) {
                            GameState snapshot = new GameState(carStates, obstacles, gameStarted, FINISH_LINE_Y, winnerId);
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            ObjectOutputStream oos = new ObjectOutputStream(baos);
                            oos.writeObject(snapshot);
                            oos.flush();
                            byte[] data = baos.toByteArray();

                            for (InetSocketAddress addr : clientAddresses.values()) {
                                try {
                                    ds.send(new DatagramPacket(data, data.length, addr.getAddress(), addr.getPort()));
                                } catch (Exception e) {}
                            }
                        }
                        Thread.sleep(50);
                    }
                }
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    class ClientHandler extends Thread {
        private final Socket socket;
        private final int carId;

        public ClientHandler(Socket socket, int carId) {
            this.socket = socket;
            this.carId = carId;
        }

        @Override
        public void run() {
            try (ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                 ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())) {

                oos.writeObject(carId);
                oos.flush();

                while (true) {
                    Object obj = ois.readObject();
                    if (obj instanceof InetSocketAddress) {
                        clientAddresses.put(carId, (InetSocketAddress) obj);
                    } else if (obj instanceof String) {
                        String cmd = (String) obj;
                        CarState car = carStates.get(carId);
                        if (car != null) {
                            synchronized (car) {
                                double MAX_SPEED = 150.0; 
                                double ACCEL = 8.0;       
                                
                                switch (cmd) {
                                    case "READY":
                                        car.isReady = true;
                                        checkStartGame();
                                        break;
                                    case "RESET":
                                        if (winnerId != -1) resetGame();
                                        break;
                                    case "ACCELERATE":
                                        if (gameStarted && !car.isEliminated) {
                                            // Chỉ cho phép tăng tốc nếu KHÔNG bị phạt
                                            if (System.currentTimeMillis() > car.slowUntilTime) {
                                                car.speed = Math.min(car.speed + ACCEL, MAX_SPEED);
                                            }
                                        }
                                        break;
                                    case "BRAKE":
                                        if (gameStarted) car.speed = Math.max(car.speed - 20.0, 0.0);
                                        break;
                                    case "TURN_LEFT":
                                        if (gameStarted && !car.isEliminated) car.x -= 8;
                                        break;
                                    case "TURN_RIGHT":
                                        if (gameStarted && !car.isEliminated) car.x += 8;
                                        break;
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                carStates.remove(carId);
                clientAddresses.remove(carId);
            }
        }
    }
}