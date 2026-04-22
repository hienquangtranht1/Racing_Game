package racinggame;
import java.io.Serializable;

public class CarState implements Serializable {
    private static final long serialVersionUID = 1L;
    public int carId;
    public double x, y, speed;
    public boolean isFinished, isEliminated, isReady;
    
    // THÊM: Thời gian kết thúc trạng thái bị làm chậm (Unix timestamp)
    public long slowUntilTime = 0; 
    
    public CarState(int carId, double x, double y) {
        this.carId = carId;
        this.x = x;
        this.y = y;
        this.speed = 0.0;
        this.isFinished = false;
        this.isEliminated = false;
        this.isReady = false;
        this.slowUntilTime = 0;
    }
}