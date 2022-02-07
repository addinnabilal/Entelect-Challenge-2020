package main.bot.entities;

import com.google.gson.annotations.SerializedName;
import main.bot.enums.PowerUps;
import main.bot.enums.State;

public class Car {
    @SerializedName("id")
    public int id;

    @SerializedName("position")
    public Position position;

    @SerializedName("speed")
    public int speed;

    @SerializedName("damage")
    public int damage;

    @SerializedName("state")
    public State state;

    @SerializedName("powerups")
    public PowerUps[] powerups;

    @SerializedName("boosting")
    public Boolean boosting;

    @SerializedName("boostCounter")
    public int boostCounter;
}