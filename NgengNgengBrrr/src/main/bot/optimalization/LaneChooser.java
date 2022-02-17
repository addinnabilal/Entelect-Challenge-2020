import main.bot.command.*;
import main.bot.entities.*;
import main.bot.enums.State;
import main.bot.enums.Terrain;
import main.bot.enums.PowerUps;
import main.bot.optimalization.*;

import java.security.SecureRandom;
import java.util.*;

import static java.lang.Math.max;
import static java.lang.Math.abs;

public class LaneChooser {
    public static Command choosingLane(List<Integer> lane_points, List<Integer> power_ups_points, GameState gameState){
        //Check max score
        int maxPoint = Collections.max(lane_points);
        int lane = lane_points.indexOf(maxPoint);

        if (lane == 1){
            return  TURN_LEFT;
        }
        else if (lane == 2) {
            return TURN_RIGHT;
        }
        else if(lane == 0){
            return ACCELERATE;
        }
        else if(gameState.player.speed < 15){
            return ConditionChecker.use_powerups(power_ups_points.get(1));
        }

        return ACCELERATE;
    }
}
