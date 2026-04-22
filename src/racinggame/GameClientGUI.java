package racinggame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.HashSet;
import java.util.Set;

public class GameClientGUI extends JFrame {
    private static final String SERVER_HOST = "10.241.75.71";
    private static final int SERVER_PORT = 12345;

    private Socket tcpSocket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private DatagramSocket udpSocket;
    
    private int myCarId = -1;
    private volatile GameState latestState;
    private final Set<Integer> pressedKeys = new HashSet<>();

    private DrawPanel drawPanel;
    private JLabel statusLabel;
    private JButton btnReady;
    private JButton btnRestart; 
    private JPanel bottomPanel;

    public GameClientGUI() {
        super("Racing Survival (3 Players)");
        setSize(700, 850);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initUI();
        
        new Thread(this::connectToServer).start();
        new javax.swing.Timer(30, e -> processInput()).start();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        drawPanel = new DrawPanel();
        add(drawPanel, BorderLayout.CENTER);

        bottomPanel = new JPanel();
        bottomPanel.setBackground(Color.WHITE);
        
        btnReady = new JButton("SẴN SÀNG (READY)");
        styleButton(btnReady, new Color(50, 50, 50));
        btnReady.addActionListener(e -> {
            sendCmd("READY");
            btnReady.setEnabled(false);
            btnReady.setText("ĐANG CHỜ...");
            drawPanel.requestFocus(); 
        });

        btnRestart = new JButton("ĐUA LẠI");
        styleButton(btnRestart, new Color(231, 76, 60));
        btnRestart.setVisible(false);
        btnRestart.addActionListener(e -> {
            sendCmd("RESET"); 
            btnRestart.setVisible(false);
            btnReady.setVisible(true);
            btnReady.setEnabled(true);
            btnReady.setText("SẴN SÀNG (READY)");
            drawPanel.requestFocus();
        });

        bottomPanel.add(btnReady);
        bottomPanel.add(btnRestart);
        add(bottomPanel, BorderLayout.SOUTH);

        statusLabel = new JLabel("Đang kết nối...", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(statusLabel, BorderLayout.NORTH);

        drawPanel.setFocusable(true);
        drawPanel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) { pressedKeys.add(e.getKeyCode()); }
            @Override
            public void keyReleased(KeyEvent e) { pressedKeys.remove(e.getKeyCode()); }
        });
    }

    private void styleButton(JButton btn, Color bg) {
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
    }

    private void processInput() {
        if (latestState != null && latestState.gameRunning) {
            if (pressedKeys.contains(KeyEvent.VK_UP)) sendCmd("ACCELERATE");
            if (pressedKeys.contains(KeyEvent.VK_DOWN)) sendCmd("BRAKE");
            if (pressedKeys.contains(KeyEvent.VK_LEFT)) sendCmd("TURN_LEFT");
            if (pressedKeys.contains(KeyEvent.VK_RIGHT)) sendCmd("TURN_RIGHT");
        }
    }

    private void connectToServer() {
        try {
            tcpSocket = new Socket(SERVER_HOST, SERVER_PORT);
            oos = new ObjectOutputStream(tcpSocket.getOutputStream());
            ois = new ObjectInputStream(tcpSocket.getInputStream());

            myCarId = (Integer) ois.readObject();
            SwingUtilities.invokeLater(() -> statusLabel.setText("Kết nối thành công! Bạn là P" + myCarId));

            udpSocket = new DatagramSocket();
            InetSocketAddress localAddr = new InetSocketAddress(InetAddress.getLocalHost(), udpSocket.getLocalPort());
            oos.writeObject(localAddr);
            oos.flush();

            new Thread(() -> {
                try {
                    byte[] buf = new byte[8192];
                    DatagramPacket p = new DatagramPacket(buf, buf.length);
                    while (true) {
                        udpSocket.receive(p);
                        ObjectInputStream oisUDP = new ObjectInputStream(new ByteArrayInputStream(p.getData()));
                        Object stateObj = oisUDP.readObject();
                        if (stateObj instanceof GameState) {
                            latestState = (GameState) stateObj;
                            drawPanel.repaint();
                            updateGameLogic();
                        }
                    }
                } catch (Exception e) { e.printStackTrace(); }
            }).start();

        } catch (Exception e) {
            SwingUtilities.invokeLater(() -> statusLabel.setText("Lỗi kết nối: " + e.getMessage()));
        }
    }

    private void updateGameLogic() {
        if (latestState == null) return;
        
        SwingUtilities.invokeLater(() -> {
            if (latestState.winnerId != -1) {
                if (!btnRestart.isVisible()) {
                    btnRestart.setVisible(true);
                    btnReady.setVisible(false);
                }
                if (latestState.winnerId == myCarId) statusLabel.setText("BẠN THẮNG!");
                else if (latestState.winnerId == 0) statusLabel.setText("HÒA (TẤT CẢ BỊ LOẠI)");
                else statusLabel.setText("NGƯỜI THẮNG: P" + latestState.winnerId);
                statusLabel.setForeground(Color.RED);
            } else if (!latestState.gameRunning && latestState.winnerId == -1) {
                 if (btnRestart.isVisible()) {
                    btnRestart.setVisible(false);
                    btnReady.setVisible(true);
                    btnReady.setEnabled(true);
                    btnReady.setText("SẴN SÀNG");
                 }
                 statusLabel.setText("Đang chờ người chơi...");
                 statusLabel.setForeground(Color.BLACK);
            } else {
                 CarState me = latestState.carStates.get(myCarId);
                 if (me != null) {
                     String txt = "Player " + myCarId;
                     if (me.isEliminated) txt += " [ĐÃ CHẾT]";
                     else {
                         // Hiển thị trạng thái bị choáng/chậm
                         if (System.currentTimeMillis() < me.slowUntilTime) {
                             txt += " | ĐANG BỊ CHOÁNG! (CHẬM)";
                             statusLabel.setForeground(Color.ORANGE);
                         } else {
                             txt += String.format(" | Tốc độ: %.0f km/h", me.speed);
                             statusLabel.setForeground(Color.BLACK);
                         }
                     }
                     statusLabel.setText(txt);
                 }
            }
        });
    }

    private void sendCmd(String cmd) {
        if (oos != null) {
            try { oos.writeObject(cmd); oos.flush(); } catch (IOException e) {}
        }
    }

    class DrawPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(new Color(240, 242, 245));
            g2.fillRect(0, 0, getWidth(), getHeight());

            if (latestState == null) return;

            int roadLeft = 50;
            int roadWidth = 500;
            
            g2.setColor(Color.WHITE);
            g2.fillRect(roadLeft, 0, roadWidth, getHeight());

            g2.setColor(new Color(50, 50, 50));
            g2.setStroke(new BasicStroke(4));
            g2.drawLine(roadLeft, 0, roadLeft, getHeight());
            g2.drawLine(roadLeft + roadWidth, 0, roadLeft + roadWidth, getHeight());

            CarState me = latestState.carStates.get(myCarId);
            double cameraY = (me != null) ? me.y : 0;
            float dashPhase = (float) (cameraY % 100); 
            
            // Vẽ 2 vạch phân làn để thấy rõ 3 làn
            g2.setColor(new Color(200, 200, 200));
            g2.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{30, 30}, dashPhase));
            g2.drawLine(roadLeft + 150, 0, roadLeft + 150, getHeight()); // Vạch 1
            g2.drawLine(roadLeft + 300, 0, roadLeft + 300, getHeight()); // Vạch 2

            // Vẽ Vạch Đích
            if (latestState.raceDistance > 0) {
                int finishScreenY = getHeight() - 150 - (int)(latestState.raceDistance - cameraY);
                if (finishScreenY > -50 && finishScreenY < getHeight() + 50) {
                    g2.setColor(Color.BLACK);
                    g2.fillRect(roadLeft, finishScreenY, roadWidth, 20);
                    g2.setColor(Color.WHITE);
                    for (int k = roadLeft; k < roadLeft + roadWidth; k+=40) {
                        g2.fillRect(k, finishScreenY, 20, 20);
                    }
                    g2.setColor(Color.RED);
                    g2.setFont(new Font("Arial", Font.BOLD, 20));
                    g2.drawString("FINISH", roadLeft + roadWidth/2 - 30, finishScreenY - 10);
                }
            }

            // Vẽ vật cản
            g2.setStroke(new BasicStroke(1));
            for (Obstacle o : latestState.obstacles) {
                int screenX = (int) o.x; 
                int screenY = getHeight() - 150 - (int)(o.y - cameraY); 
                
                if (screenY > -50 && screenY < getHeight() + 50) {
                     g2.setColor(new Color(231, 76, 60));
                     g2.fillRoundRect(screenX - o.width/2, screenY - o.height/2, o.width, o.height, 10, 10);
                }
            }

            // Vẽ Xe
            for (CarState cs : latestState.carStates.values()) {
                int screenX = (int) cs.x;
                int screenY = getHeight() - 150 - (int)(cs.y - cameraY);

                if (cs.isEliminated) {
                    g2.setColor(Color.GRAY);
                    g2.setStroke(new BasicStroke(3));
                    g2.drawLine(screenX - 20, screenY - 20, screenX + 20, screenY + 20);
                    g2.drawLine(screenX + 20, screenY - 20, screenX - 20, screenY + 20);
                } else {
                    if (cs.carId == myCarId) g2.setColor(new Color(52, 152, 219)); 
                    else g2.setColor(new Color(46, 204, 113)); 

                    // Nếu xe đang bị choáng/chậm thì vẽ màu Vàng hoặc nhấp nháy
                    if (System.currentTimeMillis() < cs.slowUntilTime) {
                        g2.setColor(Color.ORANGE);
                    }

                    g2.fillRoundRect(screenX - 18, screenY - 30, 36, 60, 15, 15);
                    
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("Arial", Font.BOLD, 12));
                    g2.drawString("P" + cs.carId, screenX - 8, screenY + 5);
                    
                    // Vẽ hiệu ứng choáng trên đầu xe
                    if (System.currentTimeMillis() < cs.slowUntilTime) {
                        g2.setColor(Color.RED);
                        g2.setFont(new Font("Arial", Font.BOLD, 10));
                        g2.drawString("CHOÁNG!", screenX - 20, screenY - 35);
                    }
                }
            }

            // Overlay Kết quả
            if (latestState.winnerId != -1) {
                g2.setColor(new Color(0, 0, 0, 180));
                g2.fillRect(0, getHeight()/2 - 80, getWidth(), 160);
                
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Arial", Font.BOLD, 40));
                
                String msg;
                if (latestState.winnerId == myCarId) msg = "YOU WIN!";
                else if (latestState.winnerId == 0) msg = "GAME OVER";
                else msg = "WINNER: P" + latestState.winnerId;
                
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(msg)) / 2;
                g2.drawString(msg, x, getHeight()/2 - 10);
                
                g2.setFont(new Font("Arial", Font.PLAIN, 18));
                String sub = "Bấm 'ĐUA LẠI' để chơi ván mới";
                int subX = (getWidth() - g2.getFontMetrics().stringWidth(sub)) / 2;
                g2.drawString(sub, subX, getHeight()/2 + 30);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GameClientGUI().setVisible(true));
    }
}