package racinggame;
import java.io.Serializable;

public class Obstacle implements Serializable {
    private static final long serialVersionUID = 1L;
    public int id;
    public double x, y;
    public int width, height;
    public boolean active;

    public Obstacle(int id, double x, double y, int width, int height) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.active = true;
    }
}