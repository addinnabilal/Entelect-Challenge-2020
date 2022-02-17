package main.bot.optimalization;
import main.bot.entities.*;
import main.bot.enums.Terrain;

import java.util.*;

import static java.lang.Math.max;

public class BlockChecker {
    
    //BLOCK CHECKER
    public static List<Integer> getNumOfBlockInFront(int pos_lane, int pos_block, int currSpeed, GameState gameState){
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


    public static List<Object> getBlocksInFront(int lane, int block, GameState gameState, int currSpeed) {
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
