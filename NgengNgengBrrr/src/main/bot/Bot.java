package main.bot;

import main.bot.command.*;
import main.bot.entities.*;
import main.bot.enums.Terrain;
import main.bot.enums.PowerUps;

import java.security.SecureRandom;
import java.util.*;

import static java.lang.Math.max;

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
     * the amount of blocks that can be traversed at max speed.
     **/
    private List<Object> getBlocksInFront(int lane, int block, GameState gameState) {
        List<Lane[]> map = gameState.lanes;
        List<Object> blocks = new ArrayList<>();
        int startBlock = map.get(0)[0].position.block;

        Lane[] laneList = map.get(lane - 1);
        for (int i = max(block - startBlock, 0); i <= block - startBlock + Bot.maxSpeed; i++) {
            if (laneList[i] == null || laneList[i].terrain == Terrain.FINISH) {
                break;
            }

            blocks.add(laneList[i].terrain);

        }
        return blocks;
    }

}
