package main.bot.optimalization;

import main.bot.command.*;
import main.bot.entities.*;

import java.util.*;


public class LaneChooser {
    public final static Command ACCELERATE = new AccelerateCommand();
    public final static Command LIZARD = new LizardCommand();
    public final static Command OIL = new OilCommand();
    public final static Command BOOST = new BoostCommand();
    public final static Command EMP = new EmpCommand();
    public final static Command FIX = new FixCommand();
    public static Command TWEET_COMMAND;

    public final static Command TURN_RIGHT = new ChangeLaneCommand(1);
    public final static Command TURN_LEFT = new ChangeLaneCommand(-1);

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
