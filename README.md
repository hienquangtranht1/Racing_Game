<h1 align="center">🏎️ Racing Survival (3 Players) 🏁</h1>

<div align="center">
  <img src="https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white" alt="Java Badge"/>
  <img src="https://img.shields.io/badge/Swing-GUI-blue?style=for-the-badge" alt="Swing Badge"/>
  <img src="https://img.shields.io/badge/Socket-TCP%20%7C%20UDP-success?style=for-the-badge" alt="Socket Badge"/>
</div>

<br/>

> **Racing Survival** là một tựa game đua xe sinh tồn nhiều người chơi (tối đa 3 người) được phát triển hoàn toàn bằng **Java**. Trò chơi kết hợp kiến trúc Server-Client mạnh mẽ, xử lý va chạm thời gian thực và đồng bộ hóa mạng mượt mà để mang lại trải nghiệm đua xe đầy kịch tính!

---

## ✨ Tính năng nổi bật

* 🎮 **Multiplayer Real-time**: Hỗ trợ tối đa 3 tay đua cùng tranh tài trong một phòng chơi.
* ⚡ **Giao thức mạng tối ưu**: 
  * Sử dụng **TCP** để truyền lệnh điều khiển và quản lý phòng chờ (đảm bảo độ tin cậy).
  * Sử dụng **UDP** để phát sóng (broadcast) trạng thái game ở tốc độ cao (50ms/lần), giảm thiểu độ trễ.
* 💥 **Cơ chế vật lý & Sinh tồn**:
  * Đâm vào chướng ngại vật (màu đỏ) hoặc vách đường: **Loại trực tiếp!** 💀
  * Cơ chế tạt đầu: Xe lấn làn sẽ bị loại, xe đi đúng làn nếu bị đâm sẽ rơi vào trạng thái **Choáng (Stun)** và giảm tốc độ trong 3 giây.
* 🏁 **Phòng chờ (Lobby)**: Hệ thống sẵn sàng (Ready) thông minh, game chỉ bắt đầu khi tất cả tay đua đã vào vị trí.

---

## 🛠️ Công nghệ & Kiến trúc

| Thành phần | Công nghệ sử dụng | Chức năng chính |
| :--- | :--- | :--- |
| **Ngôn ngữ** | `Java (JDK 17+)` | Core logic, xử lý luồng (Multithreading) |
| **Đồ họa** | `Java Swing & AWT` | Render giao diện, xe, hiệu ứng bằng `Graphics2D` |
| **Mạng** | `Socket`, `DatagramSocket` | Giao tiếp mạng Server - Client |
| **Đồng bộ** | `UDP Broadcast` | Đồng bộ hóa `GameState` tới toàn bộ Client |

---

## 🚀 Hướng dẫn cài đặt & Chạy game

### 1. Chuẩn bị (Prerequisites)
* Đảm bảo máy tính của bạn đã cài đặt **Java Development Kit (JDK) 8** trở lên.

### 2. Cấu hình Mạng (Tùy chọn)
Mặc định game được cấu hình chạy trên cùng một máy (`localhost`). Nếu muốn chơi qua mạng LAN, bạn cần sửa đổi IP:
* Mở `GameServer.java` và `GameClientGUI.java`.
* Tìm biến `SERVER_IP` (Server) và `SERVER_HOST` (Client), thay đổi thành địa chỉ IP IPv4 của máy chủ (VD: `192.168.1.x`).

### 3. Khởi chạy
Mở Terminal/Command Prompt hoặc chạy trực tiếp từ IDE (NetBeans, IntelliJ, Eclipse):

**Bước 1: Chạy Server**
```bash
# Khởi động máy chủ trước để mở cổng lắng nghe (Port: 12345)
java racinggame.GameServer
```

**Bước 2: Chạy Client (Người chơi)**
```bash
# Khởi chạy cửa sổ game (Mở tối đa 3 lần cho 3 người chơi)
java racinggame.GameClientGUI
```

---

## 🕹️ Cách thức điều khiển

Sử dụng các phím mũi tên trên bàn phím để điều khiển siêu xe của bạn:

| Phím | Hành động | Mô tả |
| :---: | :--- | :--- |
| **`↑`** | **Tăng tốc** | Nhấn giữ để tăng vận tốc tiến về đích. |
| **`↓`** | **Giảm tốc/Phanh** | Hãm phanh để né tránh chướng ngại vật an toàn. |
| **`←` / `→`** | **Chuyển làn** | Đánh lái sang trái hoặc phải (cẩn thận va chạm!). |

🏆 **Mục tiêu chiến thắng:** Hãy là người đầu tiên cán đích ở mốc **4000.0** hoặc trở thành **kẻ sống sót cuối cùng** trên đường đua tử thần này!

## 📂 Cấu trúc dự án

```text
📦 src/racinggame
 ┣ 📜 GameServer.java      # Máy chủ: Xử lý logic, va chạm, quản lý Client
 ┣ 📜 GameClientGUI.java   # Máy khách: Giao diện game, bắt sự kiện bàn phím
 ┣ 📜 GameState.java       # Lưu trữ "ảnh chụp" toàn bộ trạng thái màn chơi
 ┣ 📜 CarState.java        # Quản lý tọa độ, tốc độ, trạng thái sống/chết của xe
 ┗ 📜 Obstacle.java        # Định nghĩa các chướng ngại vật sinh ra ngẫu nhiên


