package org.YBR.app.model;

import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;

/**
 * Created by usama on 9/26/14.
 */
@ParseClassName("RetailLocation")
public class RetailLocation extends ParseObject {
    public String getName() {
        return getString("name");
    }

    public void setName(String newName) {
        put("name", newName);
    }

    public ParseGeoPoint getLocation() {
        return getParseGeoPoint("location");
    }

    public void setLocation(ParseGeoPoint newLocation) {
        put("location", newLocation);
    }

    public int getVicinityRadius() {
        return getInt("vicinityRadius");
    }

    public void setVicinityRadius(int newRadius) {
        put("vicinityRadius", newRadius);
    }
}
