/*
Copyright: Ambrosus Technologies GmbH
Email: tech@ambrosus.com

This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.

This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
*/

package com.ambrosus.commons;

import com.ambrosus.model.EventData;
import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.Locale;
import java.util.Objects;

/**
 * Class for hosting location information contained in an event data section. This class corresponds after
 * serialization/deserialization to an event data section of type "ambrosus.event.location".
 */
public class Location extends EventData {

    public final static String API_DATA_TYPE = "ambrosus.asset.location";

    private final static String TYPE_PROPERTY = "type";
    private final static String NAME_PROPERTY = "name";
    private final static String CITY_PROPERTY = "city";
    private final static String COUNTRY_PROPERTY = "country";
    private final static String GEOMETRY_PROPERTY = "geometry";
    private final static String LOCATION_PROPERTY = "location";
    private final static String COORDINATES_PROPERTY = "coordinates";

    private final static int X_COORD = 0;
    private final static int Y_COORD = 1;

    private final double latitude;
    private final double longitude;
    private final String name;
    private final String city;
    private final String country;


    public Location(double latitude, double longitude, String name, String city, String country) {
        super(API_DATA_TYPE);
        this.latitude = latitude;
        this.longitude = longitude;
        this.name = name;
        this.city = city;
        this.country = country;
    }


    public double getLatitude() {

        return latitude;
    }


    public double getLongitude() {

        return longitude;
    }


    public String getName() {

        return name;
    }


    public String getCity() {

        return city;
    }


    public String getCountry() {


        return country;
    }


    @Override
    public String toString() {

        return String.format(Locale.ENGLISH, "Location event %s: latitude %f longitude %f", name, latitude, longitude);

    }


    @Override
    public boolean equals(Object o) {

        if (o == this) return true;
        if (!(o instanceof Location)) {
            return false;
        }

        Location that = (Location) o;
        return this.latitude == that.latitude &&
                this.longitude == that.longitude &&
                this.name.equals(that.name) &&
                this.city.equals(that.city) &&
                this.country.equals(that.country);
    }


    @Override
    public int hashCode() {

        return Objects.hash(latitude, longitude, name, city, country);
    }


    public static class Adapter implements JsonSerializer<Location>, JsonDeserializer<Location> {

        @Override
        public Location deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws
                JsonParseException {

            try {
                JsonObject jObj = json.getAsJsonObject();
                JsonArray lonLat = jObj
                        .getAsJsonObject(LOCATION_PROPERTY)
                        .getAsJsonObject(GEOMETRY_PROPERTY)
                        .getAsJsonArray(COORDINATES_PROPERTY);

                if (lonLat.size() == 2) {
                    return new Location(
                            lonLat.get(Y_COORD).getAsDouble(),
                            lonLat.get(X_COORD).getAsDouble(),
                            jObj.get(NAME_PROPERTY).getAsString(),
                            jObj.get(CITY_PROPERTY).getAsString(),
                            jObj.get(COUNTRY_PROPERTY).getAsString()
                    );
                } else {
                    System.err.println("Coordinate JSON array is not of length 2");
                }

            } catch (IllegalStateException | NullPointerException e) {
                e.printStackTrace();
            }

            return null;
        }


        @Override
        public JsonElement serialize(Location src, Type typeOfSrc, JsonSerializationContext context) {

            JsonObject eventObject = new JsonObject();
            eventObject.addProperty(TYPE_PROPERTY, API_DATA_TYPE);
            eventObject.addProperty(NAME_PROPERTY, src.getName());
            eventObject.addProperty(CITY_PROPERTY, src.getCity());
            eventObject.addProperty(COUNTRY_PROPERTY, src.getCountry());

            JsonObject locationObject = new JsonObject();
            JsonObject geometryObject = new JsonObject();
            JsonArray coordinatesArray = new JsonArray();

            eventObject.add(LOCATION_PROPERTY, locationObject);
            locationObject.add(GEOMETRY_PROPERTY, geometryObject);
            geometryObject.addProperty(TYPE_PROPERTY, "Point");
            geometryObject.add(COORDINATES_PROPERTY, coordinatesArray);
            coordinatesArray.add(src.getLongitude());
            coordinatesArray.add(src.getLatitude());

            return eventObject;
        }
    }
}
