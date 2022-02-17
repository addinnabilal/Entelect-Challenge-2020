package main.bot.optimalization;

import main.bot.entities.*;

import java.util.*;

public class DamageChecker {
    // BASIC DAMAGE CHECKER
    public static boolean damage_check(GameState gameState){
        Car myCar = gameState.player;
        int maxSpeed = ConditionChecker.max_speed_check(myCar);
        List<Integer> numofPowerUps = NumOfPowerupChecker.getNumofPowerUps(gameState); //Jumlah boost == 1 baru difix
        int numOfBoost = numofPowerUps.get(1);

        return (myCar.speed == maxSpeed && (maxSpeed < 8 || (numOfBoost > 0)));
    }
}
