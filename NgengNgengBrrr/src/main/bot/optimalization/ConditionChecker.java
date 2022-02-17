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

public class ConditionChecker {
    public static Boolean contains_obstacle(List<Object> blocks){

        return blocks.contains(Terrain.MUD) || blocks.contains(Terrain.OIL_SPILL) || blocks.contains(Terrain.WALL);
    }
    

    public static Boolean hasPowerUp(PowerUps powerUpToCheck, PowerUps[] available) {
        for (PowerUps powerUp: available) {
            if (powerUp.equals(powerUpToCheck)) {
                return true;
            }
        }
        return false;
    }


    //SPEED CHECKER BASED ON DAMAGE & POWERUPS
    public static int max_speed_check(Car myCar){
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

    public static int current_speed_if(Car myCar, Command command){
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
    public static Command use_powerups(int commandNum){
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
}
