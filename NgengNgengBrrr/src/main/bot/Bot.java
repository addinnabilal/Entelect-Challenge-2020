package main.bot;

import jdk.nashorn.internal.ir.Block;
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
        List<Integer> powerups_to_use = get_total_points_using_powerups(gameState, blocks);

        //Check damage
        if (damage_check(myCar))
        {
            return FIX;
        }

        //Check power-ups
        if (powerups_to_use.get(0) > 0)
        {
            return use_powerups(powerups_to_use.get(1));
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

    private int total_point_using_powerups(PowerUps powerUpToCheck, GameState gameState, List<Object> blocks ) {
        //Player & Enemy Car Condition
        Car myCar = gameState.player;
        Car opponent = gameState.opponent;

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
                if (blocks.contains(Terrain.MUD) || blocks.contains(Terrain.OIL_SPILL))
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
            case LIZARD:
                List<Object> landingBlocks = blocks.subList(0,myCar.speed);

                //List all obstacles
                List<Object> obstacles = new ArrayList<>();
                obstacles.add(Terrain.MUD);
                obstacles.add(Terrain.OIL_SPILL);
                obstacles.add(Terrain.WALL); //belum nambah truck elon sama block lizard

                //List all power_ups
                List<Object> power_ups = new ArrayList<>();
                power_ups.add(Terrain.OIL_POWER);
                power_ups.add(Terrain.BOOST);
                power_ups.add(Terrain.EMP);
                power_ups.add(Terrain.TWEET);
                power_ups.add(Terrain.LIZARD);

                if (!(landingBlocks.containsAll(obstacles)))
                {
                    point = 3;
                }

                for (int i = 0; i < blocks.size(); i++)
                {
                    if (blocks.subList(i,i+1).containsAll(power_ups))
                    {
                        point -= 1;
                    }
                    if (blocks.subList(i,i+1).containsAll(obstacles))
                    {
                        point += 1;
                    }
                }

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

    private List<Integer> get_total_points_using_powerups(GameState gameState, List<Object> blocks ){
        //Looking for the best powerups to use, return in 2d array <points,command>
        List<Integer> points_using_powerups = new ArrayList<Integer>();

        //Points from EMP
        points_using_powerups.add(total_point_using_powerups(PowerUps.EMP, gameState, blocks));
        //Points from BOOST
        points_using_powerups.add(total_point_using_powerups(PowerUps.BOOST, gameState, blocks));
        //Points from OIL
        points_using_powerups.add(total_point_using_powerups(PowerUps.OIL, gameState, blocks));
        //Points from LIZARD
        points_using_powerups.add(total_point_using_powerups(PowerUps.LIZARD, gameState, blocks));

        //Looking for best powerups
        Integer max_points = 0, command = 0;
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
        powerups_to_use.add(max_points);
        powerups_to_use.add(command);

        return  powerups_to_use;
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
            default:
                return BOOST;
        }
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
        //Current Speed
        int currSpeed = gameState.player.speed;

        //Current map condition
        List<Lane[]> map = gameState.lanes;
        List<Object> blocks = new ArrayList<>();
        int startBlock = map.get(0)[0].position.block;

        Lane[] laneList = map.get(lane - 1);
        for (int i = max(block - startBlock, 0); i <= block - startBlock + currSpeed; i++) {
            if (laneList[i] == null || laneList[i].terrain == Terrain.FINISH) {
                break;
            }

            blocks.add(laneList[i].terrain);

        }
        return blocks;
    }

}
