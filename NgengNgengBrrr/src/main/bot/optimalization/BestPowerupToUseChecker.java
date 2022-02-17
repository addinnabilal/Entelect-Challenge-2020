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

public class BestPowerupToUse {
    public static List<Integer> get_total_points_using_powerups(GameState gameState){
        //Looking for the best powerups to use, return in 2d array <points,command>
        List<Integer> points_using_powerups = new ArrayList<Integer>();

        //Points from EMP
        points_using_powerups.add(TotalPointUsingPowerupChecker.total_point_using_powerups(PowerUps.EMP, gameState));
        //Points from BOOST
        points_using_powerups.add(TotalPointUsingPowerupChecker.total_point_using_powerups(PowerUps.BOOST, gameState));
        //Points from OIL
        points_using_powerups.add(TotalPointUsingPowerupChecker.total_point_using_powerups(PowerUps.OIL, gameState));
        //Points from LIZARD
        points_using_powerups.add(TotalPointUsingPowerupChecker.total_point_using_powerups(PowerUps.LIZARD, gameState));
        //Point from TWEET
        points_using_powerups.add(TotalPointUsingPowerupChecker.total_point_using_powerups(PowerUps.TWEET, gameState));

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
}
