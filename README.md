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

## 📂 Cấu trúc dự án

```text
📦 src/racinggame
 ┣ 📜 GameServer.java      # Máy chủ: Xử lý logic, va chạm, quản lý Client
 ┣ 📜 GameClientGUI.java   # Máy khách: Giao diện game, bắt sự kiện bàn phím
 ┣ 📜 GameState.java       # Lưu trữ "ảnh chụp" toàn bộ trạng thái màn chơi
 ┣ 📜 CarState.java        # Quản lý tọa độ, tốc độ, trạng thái sống/chết của xe
 ┗ 📜 Obstacle.java        # Định nghĩa các chướng ngại vật sinh ra ngẫu nhiên
