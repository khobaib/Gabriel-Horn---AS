package com.devotify.gabrielhorn.model;

import com.parse.ParseClassName;
import com.parse.ParseObject;

@ParseClassName("Rewards")
public class Reward extends ParseObject {
    public Reward() {

    }

    public String getName() {
        return getString("name");
    }

    public int getPointsNeeded() {
        return getInt("pointsNeeded");
    }
}
