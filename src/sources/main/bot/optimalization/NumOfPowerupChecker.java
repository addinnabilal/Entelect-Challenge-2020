package main.bot.optimalization;

import main.bot.entities.*;
import main.bot.enums.PowerUps;


import java.util.*;


public class NumOfPowerupChecker {
    public static List<Integer> getNumofPowerUps(GameState gameState){
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
}
