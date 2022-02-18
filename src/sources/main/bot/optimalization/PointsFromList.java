package main.bot.optimalization;


import java.util.*;


public class PointsFromList {
    //LANES POINT CHECKER
    public static List<Integer> getPointsFromList(List<Integer> pointsPerLane, List<Integer> numOfPowerups){
        List<Integer> points = Arrays.asList(0,1);
        int point = 0;
        int obstacle = 0; // to store point from obstacle 

        //Membatasi Jumlah Powerups 
        if (numOfPowerups.get(0) < 0)
        {
            point += pointsPerLane.get(2)*(1);  // OIL POWER
        }
        if (numOfPowerups.get(1) < 5)
        {
            point += pointsPerLane.get(4)*(15);  //BOOST
        }
        if (numOfPowerups.get(2) < 3)
        {
            point += pointsPerLane.get(6)*(2);  //LIZARD
        }
        if (numOfPowerups.get(3) < 3)
        {
            point += pointsPerLane.get(7)*(3);  // TWEET
        }
        if (numOfPowerups.get(4) < 2)
        {
            point += pointsPerLane.get(8)*(3);  //EMP
        }

        point += pointsPerLane.get(0)*(-9); //MUD
        point += pointsPerLane.get(1)*(-9); //OIL SPILL
        point += pointsPerLane.get(5)*(-14); //WALL
        point += pointsPerLane.get(9)*(-12); //Enemy
        point += pointsPerLane.get(10)*(2) ; //Empty block

        obstacle += pointsPerLane.get(0)*(-7); //MUD
        obstacle += pointsPerLane.get(1)*(-7); //OIL SPILL
        obstacle += pointsPerLane.get(5)*(-12); //WALL
        obstacle += pointsPerLane.get(9)*(-12); //Enemy

        points.set(0, point);
        points.set(1, obstacle);


        return points;
    }
}
