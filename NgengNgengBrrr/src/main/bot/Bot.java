package main.bot;

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

public class Bot {

    public static final int maxSpeed = 9;
    public List<Command> directionList = new ArrayList<>();

    public static final int MINIMUM_SPEED = 0;
    public static final int SPEED_STATE_1 = 3;
    public static final int INITIAL_SPEED = 5;
    public static final int SPEED_STATE_2 = 6;
    public static final int SPEED_STATE_3 = 8;
    public static final int MAXIMUM_SPEED = 9;
    public static final int BOOST_SPEED = 15;
    List<Integer> SPEEDS = Arrays.asList(MINIMUM_SPEED, SPEED_STATE_1, INITIAL_SPEED, SPEED_STATE_2, SPEED_STATE_3, MAXIMUM_SPEED);

    public final Random random;

    public final static Command ACCELERATE = new AccelerateCommand();
    public final static Command LIZARD = new LizardCommand();
    public final static Command OIL = new OilCommand();
    public final static Command BOOST = new BoostCommand();
    public final static Command EMP = new EmpCommand();
    public final static Command FIX = new FixCommand();
    public static Command TWEET_COMMAND;

    public final static Command TURN_RIGHT = new ChangeLaneCommand(1);
    public final static Command TURN_LEFT = new ChangeLaneCommand(-1);

    public Bot() {
        this.random = new SecureRandom();
        directionList.add(TURN_LEFT);
        directionList.add(TURN_RIGHT);
    }
    private int getRandomNumber(int min, int max) {
        return (int) ((Math.random() * (max - min)) + min);
    }
    public Command run(GameState gameState) {
        Car myCar = gameState.player;
        Car opponent = gameState.opponent;

        //Basic fix logic
        List<Integer> power_ups_points = BestPowerupToUseChecker.get_total_points_using_powerups(gameState);
        List<Integer> lane_points = PointsOfAllLaneChecker.getPointsOfAllLane(power_ups_points, gameState);
        Command COMMAND = LaneChooser.choosingLane(lane_points, power_ups_points, gameState);

        //Check damage

        if (myCar.damage>0 && (DamageChecker.damage_check(gameState) && myCar.speed < 15))
        {
            return FIX;
        }

        //Check speed
        if (myCar.speed == 0)
        {
            return  ACCELERATE;
        }

        //Check power-ups
        //Cek juga ga lagi ngeboost/power up lain
        if (COMMAND == TWEET_COMMAND){
            int randomLane = getRandomNumber(-1,1);
            return new TweetCommand(randomLane,opponent.position.block + opponent.speed ) ; //nilai x dan y
        }
        else
        {
            return COMMAND;
        }
    }
}
