import main.bot.command.*;
import main.bot.entities.*;
import main.bot.enums.State;
import main.bot.enums.Terrain;
import main.bot.enums.PowerUps;

import java.security.SecureRandom;
import java.util.*;

import static java.lang.Math.max;
import static java.lang.Math.abs;
public class DamageChecker {
    // BASIC DAMAGE CHECKER
    static boolean damage_check(GameState gameState){
        Car myCar = gameState.player;
        int maxSpeed = conditionChecker.max_speed_check(myCar);
        List<Integer> numofPowerUps = NumOfPoweupChecker.getNumofPowerUps(gameState); //Jumlah boost == 1 baru difix
        int numOfBoost = numofPowerUps.get(1);

        return (myCar.speed == maxSpeed && (maxSpeed < 8 || (numOfBoost > 0)));
    }
}
