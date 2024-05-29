package com.gamecenter.games;

import java.util.ArrayList;

import com.gamecenter.games.battleball.BattleBallRoom;
import com.gamecenter.games.battlebuild.BattleBuildGame;
import com.gamecenter.games.deliveryfood.DeliveryFoodGame;
import com.gamecenter.games.snowstorm.SnowStormGame;

public class Globals {
    public static ArrayList<BattleBuildGame> gameBattleBuild = new ArrayList<>();
    public static ArrayList<DeliveryFoodGame> gameDeliveryFood = new ArrayList<>();
    public static ArrayList<BattleBallRoom> gameBattleBall = new ArrayList<>();
    public static ArrayList<SnowStormGame> gameSnowStorm = new ArrayList<>();
}
