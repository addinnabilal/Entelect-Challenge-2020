package main.bot.optimalization;

import main.bot.command.*;
import main.bot.entities.*;

import java.util.*;


public class PointsOfAllLaneChecker {
    public final static Command ACCELERATE = new AccelerateCommand();
    public final static Command LIZARD = new LizardCommand();
    public final static Command OIL = new OilCommand();
    public final static Command BOOST = new BoostCommand();
    public final static Command EMP = new EmpCommand();

    public final static Command TURN_RIGHT = new ChangeLaneCommand(1);
    public final static Command TURN_LEFT = new ChangeLaneCommand(-1);

    public static List<Integer> getPointsOfAllLane(List<Integer> power_ups_points, GameState gameState){
        Car myCar=gameState.player;
        int currLane=myCar.position.lane;
        int currBlock=myCar.position.block;
        int speedIf;
        int TURNING_POINT_REDUCTION = 0;
        int ACCELERATE_POINT_BONUS = 3;
        int BOOSTING_POINT_BONUS = 20;
        int bonus_point;
        boolean isBoosting = (myCar.speed == 15);

        List<Integer> pointsPerLane;

        //Number of Powerups
        List<Integer> numOfPowerup = NumOfPowerupChecker.getNumofPowerUps(gameState);

        // List to store points per lane
        List<Integer> lanePoints = Arrays.asList(-999, -999, -999, -999);

        // Calculate points if car turns left
        if (currLane-1>0){
            bonus_point = 0;
            int leftLane = currLane - 1;
            speedIf = ConditionChecker.current_speed_if(myCar, TURN_LEFT);
            pointsPerLane = BlockChecker.getNumOfBlockInFront(leftLane, currBlock-1, speedIf, gameState);
            bonus_point += TURNING_POINT_REDUCTION;
            if (isBoosting && PointsFromList.getPointsFromList(pointsPerLane, numOfPowerup).get(1) == 0)
            {
                bonus_point += BOOSTING_POINT_BONUS; // boosting success when doesn't hit by obstacle
            }
            lanePoints.set(1, PointsFromList.getPointsFromList(pointsPerLane, numOfPowerup).get(0) + bonus_point);
        }
        // Calculate points if car turns right
        if (currLane+1 <= gameState.lanes.size()){
            bonus_point = 0;
            int rightLane = currLane+1;
            speedIf = ConditionChecker.current_speed_if(myCar, TURN_RIGHT);
            pointsPerLane = BlockChecker.getNumOfBlockInFront(rightLane, currBlock-1, speedIf, gameState);
            bonus_point += TURNING_POINT_REDUCTION;
            if (isBoosting && PointsFromList.getPointsFromList(pointsPerLane, numOfPowerup).get(1) == 0)
            {
                bonus_point += BOOSTING_POINT_BONUS; //boosting success when doesn't hit by obstacle
            }
            lanePoints.set(2, PointsFromList.getPointsFromList(pointsPerLane, numOfPowerup).get(0) + bonus_point);
        }

        // Calculate if stays in currrent lane
        int choosedLane;
        // Using PowerUp
        if (power_ups_points.get(1)!=null){
            speedIf=ConditionChecker.current_speed_if(myCar, ConditionChecker.use_powerups(power_ups_points.get(1)));
            choosedLane = 3;
            pointsPerLane=BlockChecker.getNumOfBlockInFront(currLane, currBlock, speedIf, gameState);
            if (power_ups_points.get(0)==1 && pointsPerLane.get(1) < 0)
            {
                pointsPerLane.set(0, 0);
            }
            lanePoints.set(choosedLane, PointsFromList.getPointsFromList(pointsPerLane, numOfPowerup).get(0) + power_ups_points.get(0));
        }
        // Accelerating
        speedIf = ConditionChecker.current_speed_if(myCar, ACCELERATE);
        choosedLane = 0;
        bonus_point = 0;
        pointsPerLane=BlockChecker.getNumOfBlockInFront(currLane, currBlock, speedIf, gameState);
        if (PointsFromList.getPointsFromList(pointsPerLane, numOfPowerup).get(1) == 0)
        {
            bonus_point += ACCELERATE_POINT_BONUS; //accelerate success when doesn't hit by obstacle
        }
        if (isBoosting && PointsFromList.getPointsFromList(pointsPerLane, numOfPowerup).get(1) == 0)
        {
            bonus_point += BOOSTING_POINT_BONUS; //boosting success when doesn't hit by obstacle
        }
        lanePoints.set(choosedLane, PointsFromList.getPointsFromList(pointsPerLane, numOfPowerup).get(0) + bonus_point);

        return lanePoints;
    }
}
