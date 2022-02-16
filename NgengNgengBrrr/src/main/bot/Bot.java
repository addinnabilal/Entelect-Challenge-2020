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
        if (myCar.damage>0 && (damage_check(gameState) && myCar.speed < 15))
        {
            return FIX;
        }

        //Check speed
        if (myCar.speed == 0)
        {
            return  ACCELERATE;
        }

        //Check power-ups
        //Cek juga ga lagi ngeboost/power up lain
        if (COMMAND == TWEET_COMMAND){
            int randomLane = getRandomNumber(-1,1);
            return new TweetCommand(randomLane,opponent.position.block + opponent.speed ) ; //nilai x dan y
        }
        else
        {
            return COMMAND;
        }
    }

    // BASIC DAMAGE CHECK
    private boolean damage_check(GameState gameState){
        Car myCar = gameState.player;
        int maxSpeed = max_speed_check(myCar);
        List<Integer> numofPowerUps = getNumofPowerUps(gameState); //Jumlah boost == 1 baru difix
        int numOfBoost = numofPowerUps.get(1);

        return (myCar.speed == maxSpeed && (maxSpeed < 8 || (numOfBoost > 0)));
    }

    //Random Lane
    private int getRandomNumber(int min, int max) {
        return (int) ((Math.random() * (max - min)) + min);
    }

    //USING POWER UP AND CHECKING POWERUPS POINT
    private int total_point_using_powerups(PowerUps powerUpToCheck, GameState gameState) {
        //Player & Enemy Car Condition
        Car myCar = gameState.player;
        Car opponent = gameState.opponent;
        int speedIf;
        List<Object> blocks;
        List<Integer> numofPowerUps = getNumofPowerUps(gameState);

        //Default Point
        int point = 0;

        //Block positions difference
        int diff;

        //Count point per power_ups
        switch (powerUpToCheck) {
            case EMP:
                diff = abs(opponent.position.lane - myCar.position.lane);
                if ( opponent.speed > 3 && diff <= 1 && opponent.position.block > myCar.position.block ) {
                    point = 8;
                }
                break;
            case BOOST:
                int currBlockPos = myCar.position.block;
                int lastBlock = gameState.lanes.get(0)[gameState.lanes.get(0).length-1].position.block;
                int currSpeed = Math.min(lastBlock-currBlockPos,15);
                blocks = getBlocksInFront(myCar.position.lane, myCar.position.block, gameState, currSpeed);
                if (!(contains_obstacle(blocks)) && myCar.damage == 0)
                {
                    point = 15;
                }
                break;
            case OIL:
                if (myCar.position.lane == opponent.position.lane && opponent.position.block < myCar.position.block)
                {
                    point = 2;
                }
                break;
            case TWEET:
                if (opponent.speed > 6 && myCar.speed > 8)
                {
                    point = 5;
                }
                break;
            case LIZARD:
                speedIf = current_speed_if(myCar, LIZARD);
                blocks = getBlocksInFront(myCar.position.lane, myCar.position.block, gameState, speedIf);

                int distanceLeft = gameState.lanes.get(0)[gameState.lanes.get(0).length-1].position.block - myCar.position.block;
                List<Object> landingBlocks = blocks.subList(0, Math.max(0, Math.min(distanceLeft, myCar.speed)-1));
                int currBlock=myCar.position.block;

                if (!(contains_obstacle(landingBlocks)))
                {

                    //List all object
                    List<Integer> allObstacles = getNumOfBlockInFront(3, currBlock, 0, gameState);

                    for (int i = 0; i < allObstacles.size(); i++)
                    {
                        //Mengandung MUD/OILSPILL/WALL
                        if (Arrays.asList(0, 1, 5).contains(i)) //ekuivalen sama (if i in [0, 1, 5])
                        {
                            point += allObstacles.get(i) * 3;
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

    private Boolean contains_obstacle(List<Object> blocks){
        return blocks.contains(Terrain.MUD) || blocks.contains(Terrain.OIL_SPILL) || blocks.contains(Terrain.WALL);
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

    private List<Integer> getNumofPowerUps(GameState gameState){
        //Game and player state
        Car myCar = gameState.player;

        //Number of each block
        List<Integer> NumOfPowerUps = Arrays.asList(0, 0, 0, 0, 0);

        // Itterate through each powerups
        // cant use swithc case, bcs we comparing objects
        for (Object powerup : myCar.powerups) {
            if (powerup == PowerUps.OIL) {
                NumOfPowerUps.set(0, NumOfPowerUps.get(0) + 1);
            }
            if (powerup == PowerUps.BOOST) {
                NumOfPowerUps.set(1, NumOfPowerUps.get(1) + 1);
            }
            if (powerup == PowerUps.LIZARD) {
                NumOfPowerUps.set(2, NumOfPowerUps.get(2) + 1);
            }
            if (powerup == PowerUps.TWEET) {
                NumOfPowerUps.set(3, NumOfPowerUps.get(3) + 1);
            }
            if (powerup == PowerUps.EMP) {
                NumOfPowerUps.set(4, NumOfPowerUps.get(4) + 1);
            }
        }

        return  NumOfPowerUps;
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
        if (myCar.speed == 15)
        {
            currSpeed = 15;
        }
        else if (command==ACCELERATE){
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
    private List<Integer> getPointsFromList(List<Integer> pointsPerLane, List<Integer> numOfPowerups){
        List<Integer> points = Arrays.asList(0,1);
        int point = 0;
        int obstacle = 0; //ngecek ada obstacle apa engga

        //Membatasi Jumlah Powerups
        if (numOfPowerups.get(0) < 0)
        {
            point += pointsPerLane.get(2)*(1);  // OIL POWER
        }
        if (numOfPowerups.get(1) < 5)
        {
            point += pointsPerLane.get(4)*(15);  //BOOST
        }
        if (numOfPowerups.get(2) < 3)
        {
            point += pointsPerLane.get(6)*(2);  //LIZARD
        }
        if (numOfPowerups.get(3) < 3)
        {
            point += pointsPerLane.get(7)*(3);  // TWEET
        }
        if (numOfPowerups.get(4) < 2)
        {
            point += pointsPerLane.get(8)*(3);  //EMP
        }

        point += pointsPerLane.get(0)*(-9); //MUD
        point += pointsPerLane.get(1)*(-9); //OIL SPILL
        point += pointsPerLane.get(5)*(-14); //WALL
        point += pointsPerLane.get(9)*(-12); //Enemy
        point += pointsPerLane.get(10)*(2) ; //Empty block

        obstacle += pointsPerLane.get(0)*(-7); //MUD
        obstacle += pointsPerLane.get(1)*(-7); //OIL SPILL
        obstacle += pointsPerLane.get(5)*(-12); //WALL
        obstacle += pointsPerLane.get(9)*(-12); //Enemy

        points.set(0, point);
        points.set(1, obstacle);


        return points;
    }

    private List<Integer> getPointsOfAllLane(List<Integer> power_ups_points, GameState gameState){
        Car myCar=gameState.player;
        int currLane=myCar.position.lane;
        int currBlock=myCar.position.block;
        int speedIf;
        int TURNING_POINT_REDUCTION = 0;
        int ACCELERATE_POINT_BONUS = 3;
        int BOOSTING_POINT_BONUS = 20;
        int bonus_point;
        Boolean isBoosting = (myCar.speed == 15);

        List<Integer> pointsPerLane;

        //Number of Powerups
        List<Integer> numOfPowerup = getNumofPowerUps(gameState);

        // List to store points per lane
        List<Integer> lanePoints = Arrays.asList(-999, -999, -999, -999);

        // Calculate points if car turns left
        if (currLane-1>0){
            bonus_point = 0;
            int leftLane = currLane - 1;
            speedIf = current_speed_if(myCar, TURN_LEFT);
            pointsPerLane = getNumOfBlockInFront(leftLane, currBlock-1, speedIf, gameState);
            bonus_point += TURNING_POINT_REDUCTION;
            if (isBoosting && getPointsFromList(pointsPerLane, numOfPowerup).get(1) == 0)
            {
                bonus_point += BOOSTING_POINT_BONUS;
            }
            lanePoints.set(1, getPointsFromList(pointsPerLane, numOfPowerup).get(0) + bonus_point);
        }
        // Calculate points if car turns right
        if (currLane+1 <= gameState.lanes.size()){
            bonus_point = 0;
            int rightLane = currLane+1;
            speedIf = current_speed_if(myCar, TURN_RIGHT);
            pointsPerLane = getNumOfBlockInFront(rightLane, currBlock-1, speedIf, gameState);
            bonus_point += TURNING_POINT_REDUCTION;
            if (isBoosting && getPointsFromList(pointsPerLane, numOfPowerup).get(1) == 0)
            {
                bonus_point += BOOSTING_POINT_BONUS;
            }
            lanePoints.set(2, getPointsFromList(pointsPerLane, numOfPowerup).get(0) + bonus_point);
        }

        // Calculate if stays in currrent lane
        int choosedLane;
        // Using PowerUp
        if (power_ups_points.get(1)!=null){
            speedIf=current_speed_if(myCar, use_powerups(power_ups_points.get(1)));
            choosedLane = 3;
            pointsPerLane=getNumOfBlockInFront(currLane, currBlock, speedIf, gameState);
            if (power_ups_points.get(0)==1 && pointsPerLane.get(1) < 0)
            {
                pointsPerLane.set(0, 0);
            }
            lanePoints.set(choosedLane, getPointsFromList(pointsPerLane, numOfPowerup).get(0) + power_ups_points.get(0));
        }
        // Accelerating
        speedIf = current_speed_if(myCar, ACCELERATE);
        choosedLane = 0;
        bonus_point = 0;
        pointsPerLane=getNumOfBlockInFront(currLane, currBlock, speedIf, gameState);
        if (getPointsFromList(pointsPerLane, numOfPowerup).get(1) == 0)
        {
            bonus_point += ACCELERATE_POINT_BONUS; //accelerate success when doesn't hit by obstacle
        }
        if (isBoosting && getPointsFromList(pointsPerLane, numOfPowerup).get(1) == 0)
        {
            bonus_point += BOOSTING_POINT_BONUS;
        }
        lanePoints.set(choosedLane, getPointsFromList(pointsPerLane, numOfPowerup).get(0) + bonus_point);

        return lanePoints;
    }

    private Command choosingLane(List<Integer> lane_points, List<Integer> power_ups_points, GameState gameState){
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
            return use_powerups(power_ups_points.get(1));
        }

        return ACCELERATE;
    }

    //BLOCK CHECKER
    private List<Integer> getNumOfBlockInFront(int pos_lane, int pos_block, int currSpeed, GameState gameState){
        //Game and player state
        Car myCar = gameState.player;
        Car opponent = gameState.opponent;
        List<Object> blocks = getBlocksInFront(pos_lane, pos_block, gameState, currSpeed);

        //Number of each block
        List<Integer> NumOfBlockInFront = Arrays.asList(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);

        if (myCar.position.lane == opponent.position.lane &&
                myCar.position.block < opponent.position.block &&
                myCar.position.block + currSpeed > opponent.position.block )
        {
            NumOfBlockInFront.set(9, 1);
        }
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
            else if (block == Terrain.EMPTY) {
                NumOfBlockInFront.set(10, NumOfBlockInFront.get(10) + 1);
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
        for (int i = max(block - startBlock + 1, 0); i <= block - startBlock + currSpeed; i++) {
            if (laneList[i] == null || laneList[i].terrain == Terrain.FINISH) {
                break;
            }

            blocks.add(laneList[i].terrain);

        }
        return blocks;
    }

}
