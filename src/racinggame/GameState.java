package racinggame;

import java.io.Serializable;
import java.util.*;

public class GameState implements Serializable {
    private static final long serialVersionUID = 1L;

    public Map<Integer, CarState> carStates;
    public List<Obstacle> obstacles;
    public boolean gameRunning; 
    public double raceDistance;
    public int winnerId = -1; // -1: Đang chơi, 0: Hòa, >0: ID người thắng

    public GameState(Map<Integer, CarState> carStates, List<Obstacle> obstacles, boolean gameRunning, double raceDistance, int winnerId) {
        this.carStates = new HashMap<>(carStates);
        this.obstacles = new ArrayList<>(obstacles);
        this.gameRunning = gameRunning;
        this.raceDistance = raceDistance;
        this.winnerId = winnerId;
    }
}