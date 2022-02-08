package main.bot;

import main.bot.command.*;
import main.bot.entities.*;
import main.bot.enums.Terrain;
import main.bot.enums.PowerUps;

import java.security.SecureRandom;
import java.util.*;

import static java.lang.Math.max;
import static java.lang.Math.abs;

public class Bot {

    private static final int maxSpeed = 9;
    private List<Command> directionList = new ArrayList<>();

    private final Random random;

    private final static Command ACCELERATE = new AccelerateCommand();
    private final static Command LIZARD = new LizardCommand();
    private final static Command OIL = new OilCommand();
    private final static Command BOOST = new BoostCommand();
    private final static Command EMP = new EmpCommand();
    private final static Command FIX = new FixCommand();

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
        List<Object> blocks = getBlocksInFront(myCar.position.lane, myCar.position.block, gameState);
        List<Object> nextBlocks = blocks.subList(0,1);

        //Check damage
        if (damage_check(myCar))
        {
            return FIX;
        }

        //Else accelerate
        return ACCELERATE;
        /*
       //Fix first if too damaged to move
       if(myCar.damage == 5) {
           return FIX;
       }
       //Accelerate first if going to slow
       if(myCar.speed <= 3) {
           return ACCELERATE;
       }

       //Basic fix logic
       if(myCar.damage >= 5) {
           return FIX;
       }

       //Basic avoidance logic
       if (blocks.contains(Terrain.MUD) || nextBlocks.contains(Terrain.WALL)) {
           if (hasPowerUp(PowerUps.LIZARD, myCar.powerups)) {
               return LIZARD;
           }
           if (nextBlocks.contains(Terrain.MUD) || nextBlocks.contains(Terrain.WALL)) {
               int i = random.nextInt(directionList.size());
               return directionList.get(i);
           }
       }

       //Basic improvement logic
       if (hasPowerUp(PowerUps.BOOST, myCar.powerups)) {
           return BOOST;
       }

       //Basic aggression logic
       if (myCar.speed == maxSpeed) {
           if (hasPowerUp(PowerUps.OIL, myCar.powerups)) {
               return OIL;
           }
           if (hasPowerUp(PowerUps.EMP, myCar.powerups)) {
               return EMP;
           }
       }
       */
    }

    private int total_point_using_powerups(PowerUps powerUpToCheck, GameState gameState) {
        Car myCar = gameState.player;
        Car opponent = gameState.opponent;
        int point = 0;
        switch (powerUpToCheck) {
            case EMP:
                int diff = abs(opponent.position.lane - myCar.position.lane);
                if ( diff <= 1 ) {
                    point = 8;
                }
                break;
            case BOOST:


        }
        return point;
    }

    private boolean damage_check(Car myCar){
        int maxSpeed = max_speed_check(myCar);
        if (myCar.speed == maxSpeed && (maxSpeed < 8 || (hasPowerUp(PowerUps.BOOST, myCar.powerups))))
        {
            return true;
        }

        return  false;
    }

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
    
    private int current_speed_if(Car myCar,State carState){
        int currSpeed, maxSpeed=max_speed_check(myCar);
        switch(carState){
            case ACCELERATE:
                if (myCar.speed != maxSpeed && myCar.speed != BOOST_SPEED) {
                    currSpeed = SPEEDS[SPEEDS.indexOf(this.speed) + 1];
                }
                else {
                    currSpeed=myCar.speed;
                }
                break;
            case TURN_LEFT:
                currSpeed=myCar.speed-1;
                break;
            case TURN_RIGHT:
                currSpeed=myCar.speed-1;
                break;
            case USE_BOOST:
                currSpeed=15;
                break;
            default:
                currSpeed=myCar.speed;
                break;
        }
        return currSpeed;
    }
    private int point_per_block(Object blockToBeChecked){
        int blockPoint;
        switch(blockToBeChecked.contains(obstacleOrPowerUp)) {
            case Terrain.MUD:
                blockPoint=-3;
                break;
            case Terrain.OIL_SPILL:
                blockPoint=-3;
                break;
            case Terrain.WALL:
                blockPoint=-5;
                break;
            case Terrain.EMP:
                blockPoint=5;
                break;
            case Terrain.BOOST:
                blockPoint=5;
                break;
            case Terrain.LIZARD:
                blockPoint=3;
                break;
                break;
            case Terrain.TWEET:
                blockPoint=3;
                break;
            case Terrain.OIL_POWER:
                blockPoint=3;
                break;
            default:
                blockPoint=0;
                break;
        }
        return blockPoint;
    }

    private int total_points_in_a_lane(int y,int x, GameState gameState, State carState){
        GameState tempGameState = new GameState(gameState);        
        tempGameState.player.state=carState;
        speed=current_speed_if(myCar, carState);
        List<Object> blocks = getBlocksInFront(y, x, tempGameState, speed);
        List<Object> nextBlock;
        int points=0;
        while (fromIndex >= 0  && toIndex <= blocks.size && fromIndex < toIndex){
            nextBlock=blocks.subList(fromIndex,toIndex);
            points+=point_per_block(nextBlock);
            fromIndex++;toIndex++;
        }
        delete(tempGameState);
        return points;
    }

    private int[] total_points_per_lanes(Car myCar, GameState gameState){
        int lanesPoint[];
        lanesPoint = new int[4];
        int y=myCar.position.lane;
        int x=myCar.position.block;
        State carState;
        // Move forward belum dibikin, nunggu total_point_using_powerups
        lanesPoint[y]=total_points_in_a_lane(y, x, gameState, carState);

        // check if the car can turn left
        if (y-1<0){
            lanesPoint[y]= -999; // sementara kalo gabisa belok kiri pointnya diinisialisasi -999
        }
        else {
            lanesPoint[y]=total_points_in_a_lane(y-1, x, gameState, TURN_LEFT);
        }

        // check if the car can turn right
        if (y+1>=4){
            lanesPoint[y]= -999; // sementara kalo gabisa belok kanan pointnya diinisialisasi -999
        }
        else {
            lanesPoint[y]=total_points_in_a_lane(y+1, x, gameState, TURN_RIGHT);
        }
        return lanesPoint;
    }
    private Boolean hasPowerUp(PowerUps powerUpToCheck, PowerUps[] available) {
        for (PowerUps powerUp: available) {
            if (powerUp.equals(powerUpToCheck)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns map of blocks and the objects in the for the current lanes, returns
     * the amount of blocks that can be traversed at current speed.
     **/
    private List<Object> getBlocksInFront(int lane, int block, GameState gameState, int speed) {
        List<Lane[]> map = gameState.lanes;
        List<Object> blocks = new ArrayList<>();
        int startBlock = map.get(0)[0].position.block;

        Lane[] laneList = map.get(lane - 1);
        for (int i = max(block - startBlock, 0); i <= block - startBlock + speed; i++) {
            if (laneList[i] == null || laneList[i].terrain == Terrain.FINISH) {
                break;
            }

            blocks.add(laneList[i].terrain);

        }
        return blocks;
    }

}
