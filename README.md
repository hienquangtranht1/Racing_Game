Racing Survival (3 Players)
Dự án này là một trò chơi đua xe nhiều người chơi (tối đa 3 người) được xây dựng bằng ngôn ngữ lập trình Java. Trò chơi áp dụng kiến trúc Server-Client, sử dụng giao thức TCP để gửi các lệnh điều khiển và giao thức UDP để truyền tải trạng thái trò chơi theo thời gian thực nhằm đảm bảo độ trễ thấp.
🎮 Tính năng chính
Đua xe Multiplayer: Hỗ trợ tối đa 3 người chơi cùng lúc trong một phòng đua.

Cơ chế vật lý & va chạm:

Va chạm với chướng ngại vật (vật cản màu đỏ) hoặc đâm tường sẽ bị loại khỏi cuộc đua.

Va chạm giữa các xe: Xe lấn làn sẽ bị loại, xe đi đúng làn bị va chạm sẽ bị phạt làm chậm tốc độ trong 3 giây.

Hệ thống đồng bộ: Sử dụng UDP để phát sóng (broadcast) trạng thái của tất cả các xe và chướng ngại vật đến mọi Client cứ sau mỗi 50ms.

Giao diện đồ họa (GUI): Xây dựng bằng thư viện Swing, hiển thị tốc độ, trạng thái "Choáng" (Stun), và bảng kết quả khi kết thúc.

Quản lý phòng chờ: Người chơi cần nhấn nút "READY" để bắt đầu ván đấu khi đủ số lượng thành viên.

🛠 Công nghệ sử dụng
Ngôn ngữ: Java (JDK 17).

Giao tiếp mạng: Java Networking (Socket, ServerSocket, DatagramSocket).

Đồ họa: Java Swing & AWT (Graphics2D).

Quản lý tiến trình: Multi-threading (mỗi Client một luồng xử lý riêng trên Server).

📂 Cấu trúc mã nguồn
GameServer.java: Xử lý logic trò chơi, tính toán va chạm, quản lý danh sách người chơi và phát sóng trạng thái.

GameClientGUI.java: Giao diện người dùng, tiếp nhận input từ bàn phím và hiển thị hình ảnh trò chơi.

CarState.java: Lưu trữ thông tin chi tiết của từng xe (tọa độ x, y, tốc độ, trạng thái sống/chết).

GameState.java: Chứa toàn bộ "ảnh chụp" của trò chơi tại một thời điểm để đồng bộ giữa Server và Client.

Obstacle.java: Định nghĩa các chướng ngại vật trên đường đua.

🚀 Hướng dẫn chạy trò chơi
Cấu hình IP:

Mở file GameServer.java và GameClientGUI.java.

Tìm biến SERVER_IP (Server) và SERVER_HOST (Client), thay đổi giá trị 10.241.75.71 thành địa chỉ IP máy tính của bạn nếu chạy trong mạng LAN hoặc localhost nếu chạy trên cùng một máy.

Chạy Server: Khởi chạy class GameServer.java trước để mở cổng lắng nghe (Port: 12345).

Chạy Client: Khởi chạy class GameClientGUI.java (mở tối đa 3 cửa sổ tương ứng với 3 người chơi).

🕹 Cách điều khiển
Phím Lên (↑): Tăng tốc.

Phím Xuống (↓): Phanh/Giảm tốc.

Phím Trái/Phải (←/→): Di chuyển sang trái hoặc phải để tránh vật cản hoặc vượt xe khác.

Mục tiêu: Vượt qua vạch đích tại tọa độ 4000.0 hoặc trở thành người duy nhất còn sống sót trên đường đua.
