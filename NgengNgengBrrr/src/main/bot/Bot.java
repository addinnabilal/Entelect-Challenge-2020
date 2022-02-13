package main.bot;

import main.bot.command.*;
import main.bot.entities.*;
import main.bot.enums.State;
import main.bot.enums.Terrain;
import main.bot.enums.PowerUps;

import java.security.SecureRandom;
import java.util.*;

import static java.lang.Math.max;
import static java.lang.Math.abs;

public class Bot {

    private static final int maxSpeed = 9;
    private List<Command> directionList = new ArrayList<>();

    private static final int MINIMUM_SPEED = 0;
    private static final int SPEED_STATE_1 = 3;
    private static final int INITIAL_SPEED = 5;
    private static final int SPEED_STATE_2 = 6;
    private static final int SPEED_STATE_3 = 8;
    private static final int MAXIMUM_SPEED = 9;
    private static final int BOOST_SPEED = 15;
    List<Integer> SPEEDS = Arrays.asList(MINIMUM_SPEED, SPEED_STATE_1, INITIAL_SPEED, SPEED_STATE_2, SPEED_STATE_3, MAXIMUM_SPEED);

    private final Random random;

    private final static Command ACCELERATE = new AccelerateCommand();
    private final static Command LIZARD = new LizardCommand();
    private final static Command OIL = new OilCommand();
    private final static Command BOOST = new BoostCommand();
    private final static Command EMP = new EmpCommand();
    private final static Command FIX = new FixCommand();
    private static Command TWEET_COMMAND;

    private final static Command TURN_RIGHT = new ChangeLaneCommand(1);
    private final static Command TURN_LEFT = new ChangeLaneCommand(-1);

    public Bot() {
        this.random = new SecureRandom();
        directionList.add(TURN_LEFT);
        directionList.add(TURN_RIGHT);
    }

    public Command run(GameState gameState) {
        Car myCar = gameState.player;
        Car opponent = gameState.opponent;

        //Basic fix logic
        List<Integer> power_ups_points = get_total_points_using_powerups(gameState);
        List<Integer> lane_points = getPointsOfAllLane(power_ups_points, gameState);
        Command COMMAND = choosingLane(lane_points, power_ups_points, gameState);

        //Check damage
        if (damage_check(myCar) && myCar.state != State.USED_BOOST)
        {
            return FIX;
        }

        //Check power-ups
        //Cek juga ga lagi ngeboost/power up lain
        if (COMMAND == TWEET_COMMAND){
            return new TweetCommand(opponent.position.lane, opponent.position.block + opponent.speed) ; //nilai x dan y
        }
        else
        {
            return COMMAND;
        }
    }

    // BASIC DAMAGE CHECK
    private boolean damage_check(Car myCar){
        int maxSpeed = max_speed_check(myCar);

        return myCar.speed == maxSpeed && (maxSpeed < 8 || (hasPowerUp(PowerUps.BOOST, myCar.powerups)));
    }

    //USING POWER UP AND CHECKING POWERUPS POINT
    private int total_point_using_powerups(PowerUps powerUpToCheck, GameState gameState) {
        //Player & Enemy Car Condition
        Car myCar = gameState.player;
        Car opponent = gameState.opponent;
        int speedIf;
        List<Object> blocks;

        //List all obstacles
        List<Object> obstacles = new ArrayList<>();
        obstacles.add(Terrain.MUD);
        obstacles.add(Terrain.OIL_SPILL);
        obstacles.add(Terrain.WALL); //belum nambah truck elon sama block lizard

        //Default Point
        int point = 0;

        //Block positions difference
        int diff;

        //Count point per power_ups
        switch (powerUpToCheck) {
            case EMP:
                diff = abs(opponent.position.lane - myCar.position.lane);
                if ( diff <= 1 ) {
                    point = 8;
                }
                break;
            case BOOST:
                speedIf = current_speed_if(myCar, BOOST);
                blocks = getBlocksInFront(myCar.position.lane, myCar.position.block, gameState, speedIf);
                if (!(blocks.containsAll(obstacles)) && myCar.damage == 0)
                {
                    point = 10;
                }
                break;
            case OIL:
                diff = abs(opponent.position.block - myCar.position.block);
                if (myCar.position.lane == opponent.position.lane && diff <= opponent.speed)
                {
                    point = 5;
                }
                break;
            case TWEET:
                point = 7;
                break;
            case LIZARD:
                speedIf = current_speed_if(myCar, LIZARD);
                blocks = getBlocksInFront(myCar.position.lane, myCar.position.block, gameState, speedIf);

                int distanceLeft = gameState.lanes.get(0)[gameState.lanes.get(0).length-1].position.block - myCar.position.block;
                List<Object> landingBlocks = blocks.subList(0, Math.max(0, Math.min(distanceLeft, myCar.speed)-1));
                int currBlock=myCar.position.block;

                if (!(landingBlocks.containsAll(obstacles)))
                {
                    point = 3;

                    //List all object
                    List<Integer> allObstacles = getNumOfBlockInFront(3, currBlock, 0, gameState);

                    for (int i = 0; i < allObstacles.size(); i++)
                    {
                        //Mengandung MUD/OILSPILL/WALL
                        if (Arrays.asList(0, 1, 5).contains(i)) //ekuivalen sama (if i in [0, 1, 5])
                        {
                            point += allObstacles.get(i);
                        }
                        //Mengandung POWER UP
                        if (Arrays.asList(2, 4, 6, 7, 8).contains(i))
                        {
                            point -= allObstacles.get(i);
                        }
                    }
                }
                break;
        }

        //Check if player have the powerups
        if (hasPowerUp(powerUpToCheck, myCar.powerups))
        {
            return point;
        }
        else
        {
            return 0;
        }
    }

    private List<Integer> get_total_points_using_powerups(GameState gameState){
        //Looking for the best powerups to use, return in 2d array <points,command>
        List<Integer> points_using_powerups = new ArrayList<Integer>();

        //Points from EMP
        points_using_powerups.add(total_point_using_powerups(PowerUps.EMP, gameState));
        //Points from BOOST
        points_using_powerups.add(total_point_using_powerups(PowerUps.BOOST, gameState));
        //Points from OIL
        points_using_powerups.add(total_point_using_powerups(PowerUps.OIL, gameState));
        //Points from LIZARD
        points_using_powerups.add(total_point_using_powerups(PowerUps.LIZARD, gameState));
        //Point from TWEET
        points_using_powerups.add(total_point_using_powerups(PowerUps.TWEET, gameState));

        //Looking for best powerups
        int max_points = 0, command = 0;
        for (int i = 0; i < points_using_powerups.size(); i++)
        {
            if (max_points < points_using_powerups.get(i))
            {
                max_points = points_using_powerups.get(i);
                command = i + 1;
            }
        }

        //Return best powerups
        List<Integer> powerups_to_use = new ArrayList<Integer>();
        if (max_points==0 && command==points_using_powerups.size()){
            powerups_to_use.add(null);
            powerups_to_use.add(null);
        }
        else{
            powerups_to_use.add(max_points);
            powerups_to_use.add(command);
        }

        return  powerups_to_use;
    }

    private Boolean hasPowerUp(PowerUps powerUpToCheck, PowerUps[] available) {
        for (PowerUps powerUp: available) {
            if (powerUp.equals(powerUpToCheck)) {
                return true;
            }
        }
        return false;
    }

    private Command use_powerups(int commandNum){
        switch (commandNum) {
            case 1:
                return EMP;
            case 2:
                return BOOST;
            case 3:
                return OIL;
            case 4:
                return LIZARD;
            case 5:
                return TWEET_COMMAND;
            default:
                return ACCELERATE;
        }
    }

    //SPEED CHECKER BASED ON DAMAGE & POWERUPS
    private int max_speed_check(Car myCar){
        int maxSpeed;
        switch(myCar.damage) {
            case 5:
                maxSpeed = 0;
                break;
            case 4:
                maxSpeed = 3;
                break;
            case 3:
                maxSpeed = 6;
                break;
            case 2:
                maxSpeed = 8;
                break;
            case 1:
                maxSpeed = 9;
                break;
            case 0:
                maxSpeed = 15;
                break;
            default:
                maxSpeed = 5;
                break;
        }
        return  maxSpeed;
    }

    private int current_speed_if(Car myCar, Command command){
        int currSpeed, maxSpeed = max_speed_check(myCar);
        if (command==ACCELERATE){
            if (myCar.speed < maxSpeed && myCar.speed != 9) {
                currSpeed = SPEEDS.get(SPEEDS.indexOf(myCar.speed)+1);
            }
            else {
                currSpeed=myCar.speed;
            }
        }
        else if (command==TURN_LEFT || command==TURN_RIGHT){
            currSpeed=myCar.speed-1;
        }
        else if(command==BOOST){
            currSpeed=15;
        }
        else{
            currSpeed=myCar.speed;
        }
        return currSpeed;
    }

    //LANES POINT CHECKER
    private int getPointsFromList(List<Integer> pointsPerLane){
        int points = 0;
        points += pointsPerLane.get(0)*(-3); //MUD
        points += pointsPerLane.get(1)*(-3); //OIL SPILL
        points += pointsPerLane.get(2)*(3);  // OIL POWER
        points += pointsPerLane.get(4)*(5);  //BOOST
        points += pointsPerLane.get(5)*(-5); //WALL
        points += pointsPerLane.get(6)*(3);  //LIZARD
        points += pointsPerLane.get(7)*(3);  // TWEET
        points += pointsPerLane.get(8)*(5);  //EMP
        return points;
    }

    private List<Integer> getPointsOfAllLane(List<Integer> power_ups_points, GameState gameState){
        Car myCar=gameState.player;
        int currLane=myCar.position.lane;
        int currBlock=myCar.position.block;
        int speedIf;
        int TURNING_POINT_REDUCTION = -1;

        List<Integer> pointsPerLane;
        // List to store points per lane
        List<Integer> lanePoints = Arrays.asList(-999, -999, -999, -999);

        // Calculate points if car turns left
        if (currLane-1>0){
            int leftLane = currLane - 1;
            speedIf = current_speed_if(myCar, TURN_LEFT);
            pointsPerLane = getNumOfBlockInFront(leftLane, currBlock-1, speedIf, gameState);
            lanePoints.set(0, getPointsFromList(pointsPerLane) + TURNING_POINT_REDUCTION);
        }
        // Calculate points if car turns right
        if (currLane+1 <= gameState.lanes.size()){
            int rightLane = currLane+1;
            speedIf = current_speed_if(myCar, TURN_RIGHT);
            pointsPerLane = getNumOfBlockInFront(rightLane, currBlock-1, speedIf, gameState);
            lanePoints.set(2, getPointsFromList(pointsPerLane) + TURNING_POINT_REDUCTION);
        }

        // Calculate if stays in currrent lane
        int choosedLane;
        // Using PowerUp
        if (power_ups_points.get(1)!=null){
            speedIf=current_speed_if(myCar, use_powerups(power_ups_points.get(1)));
            choosedLane = 3;
            pointsPerLane=getNumOfBlockInFront(currLane, currBlock, speedIf, gameState);
            lanePoints.set(choosedLane, getPointsFromList(pointsPerLane));
        }
        // Accelerating
        speedIf = current_speed_if(myCar, ACCELERATE);
        choosedLane = 1;
        pointsPerLane=getNumOfBlockInFront(currLane, currBlock, speedIf, gameState);
        lanePoints.set(choosedLane, getPointsFromList(pointsPerLane));

        return lanePoints;
    }

    private Command choosingLane(List<Integer> lane_points, List<Integer> power_ups_points, GameState gameState){
        //Check max score
        int maxPoint = Collections.max(lane_points);
        int lane = lane_points.indexOf(maxPoint);

        if (lane == 0){
            return  TURN_LEFT;
        }
        else if (lane == 2) {
            return TURN_RIGHT;
        }
        else if(lane == 1){
            return ACCELERATE;
        }
        else if(gameState.player.state != State.USED_BOOST){
            return use_powerups(power_ups_points.get(1));
        }

        return ACCELERATE;
    }

    //BLOCK CHECKER
    private List<Integer> getNumOfBlockInFront(int pos_lane, int pos_block, int currSpeed, GameState gameState){
        //Game and player state
        Car myCar = gameState.player;
        List<Object> blocks = getBlocksInFront(pos_lane, pos_block, gameState, currSpeed);

        //Number of each block
        List<Integer> NumOfBlockInFront = Arrays.asList(0, 0, 0, 0, 0, 0, 0, 0, 0);

        // Itterate through each blocl
        // cant use swithc case, bcs we comparing objects
        for (Object block : blocks) {
            if (block == Terrain.MUD) {
                NumOfBlockInFront.set(0, NumOfBlockInFront.get(0) + 1);
            }
            else if (block == Terrain.OIL_SPILL) {
                NumOfBlockInFront.set(1, NumOfBlockInFront.get(1) + 1);
            }
            else if (block == Terrain.OIL_POWER) {
                NumOfBlockInFront.set(2, NumOfBlockInFront.get(2) + 1);
            }
            else if (block == Terrain.FINISH) {
                NumOfBlockInFront.set(3, NumOfBlockInFront.get(3) + 1);
            }
            else if (block == Terrain.BOOST) {
                NumOfBlockInFront.set(4, NumOfBlockInFront.get(4) + 1);
            }
            else if (block == Terrain.WALL) {
                NumOfBlockInFront.set(5, NumOfBlockInFront.get(5) + 1);
            }
            else if (block == Terrain.LIZARD) {
                NumOfBlockInFront.set(6, NumOfBlockInFront.get(6) + 1);
            }
            else if (block == Terrain.TWEET) {
                NumOfBlockInFront.set(7, NumOfBlockInFront.get(7) + 1);
            }
            else if (block == Terrain.EMP) {
                NumOfBlockInFront.set(8, NumOfBlockInFront.get(8) + 1);
            }
        }

        return  NumOfBlockInFront;
    }

    private List<Object> getBlocksInFront(int lane, int block, GameState gameState, int currSpeed) {
        //Current map condition
        List<Lane[]> map = gameState.lanes;
        List<Object> blocks = new ArrayList<>();
        int startBlock = map.get(0)[0].position.block;

        Lane[] laneList = map.get(lane-1);
        for (int i = max(block - startBlock, 0); i <= block - startBlock + currSpeed; i++) {
            if (laneList[i] == null || laneList[i].terrain == Terrain.FINISH) {
                break;
            }

            blocks.add(laneList[i].terrain);

        }
        return blocks;
    }
}
