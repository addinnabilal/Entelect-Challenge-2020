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


public class totalPointUsingPowerupChecker {
    //USING POWER UP AND CHECKING POWERUPS POINT
    public static int total_point_using_powerups(PowerUps powerUpToCheck, GameState gameState) {
        //Player & Enemy Car Condition
        Car myCar = gameState.player;
        Car opponent = gameState.opponent;
        int speedIf;
        List<Object> blocks;
        List<Integer> numofPowerUps = NumOfPowerupChecker.getNumofPowerUps(gameState);

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
                blocks = BlockChecker.getBlocksInFront(myCar.position.lane, myCar.position.block, gameState, currSpeed);
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
                blocks = blockChecker.getBlocksInFront(myCar.position.lane, myCar.position.block, gameState, opponent.speed);
                if (opponent.speed > 6 && myCar.speed > 8 && !(contains_obstacle(blocks)))
                {
                    point = 5;
                }
                break;
            case LIZARD:
                speedIf = conditionChecker.current_speed_if(myCar, LIZARD);
                blocks = BlockChecker.getBlocksInFront(myCar.position.lane, myCar.position.block, gameState, speedIf);

                int distanceLeft = gameState.lanes.get(0)[gameState.lanes.get(0).length-1].position.block - myCar.position.block;
                List<Object> landingBlocks = blocks.subList(0, Math.max(0, Math.min(distanceLeft, myCar.speed)-1));
                int currBlock=myCar.position.block;

                if (!(contains_obstacle(landingBlocks)))
                {

                    //List all object
                    List<Integer> allObstacles = BlockChecker.getNumOfBlockInFront(3, currBlock, 0, gameState);

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
        if (conditionChecker.hasPowerUp(powerUpToCheck, myCar.powerups))
        {
            return point;
        }
        else
        {
            return 0;
        }
    }
}
