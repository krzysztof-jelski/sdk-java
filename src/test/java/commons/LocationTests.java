/*
Copyright: Ambrosus Technologies GmbH
Email: tech@ambrosus.com

This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.

This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
*/

package commons;

import com.ambrosus.commons.Location;
import com.ambrosus.commons.Transport;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.junit.Before;
import org.junit.Test;
import utils.TestUtils;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;

public class LocationTests {

    private Location location;
    private Gson gson;

    private double LATITUDE;
    private double LONGITUDE;
    private String NAME;
    private String CITY;
    private String COUNTRY;


    @Before
    public void init() {

        LATITUDE = 46.779343;
        LONGITUDE = 6.641192;
        NAME = "Denner";
        CITY = "Yverdon-les-Bains";
        COUNTRY = "Switzerland";

        location = new Location(LATITUDE, LONGITUDE, NAME, CITY, COUNTRY);

        GsonBuilder gb = new GsonBuilder();
        gb.registerTypeAdapter(Location.class, new Location.Adapter());

        gson = gb.create();
    }


    @Test
    public void gettersAreCorrect() {
        assertEquals(LATITUDE, location.getLatitude());
        assertEquals(LONGITUDE, location.getLongitude());
        assertEquals(NAME, location.getName());
        assertEquals(CITY, location.getCity());
        assertEquals(COUNTRY, location.getCountry());
        assertEquals(Location.API_DATA_TYPE, location.getType());
    }


    @Test
    public void equalsIsRedefined() {
        Location loc = new Location(LATITUDE, LONGITUDE, NAME, CITY, COUNTRY);
        assertEquals(location, loc);
        assertEquals(location.hashCode(), loc.hashCode());

        Transport transport = new Transport("", "", "");
        assertNotEquals(location, transport);
    }


    @Test
    public void toStringIsRedefined() {
        assertEquals(String.format("Location event %s: latitude %f longitude %f", NAME, LATITUDE, LONGITUDE),
                location.toString());
    }


    @Test
    public void serializerIsCorrect() {

        JsonObject jsonLocation = TestUtils.readJson(TestUtils.PATH_PREFIX + "valid_location.json");

        Location loc = new Location(
                LATITUDE,
                LONGITUDE,
                NAME,
                CITY,
                COUNTRY
        );

        assertEquals(jsonLocation.toString(), gson.toJson(loc));
    }


    @Test
    public void deserializerIsCorrect() {
        JsonObject jsonLocation = TestUtils.readJson(TestUtils.PATH_PREFIX + "valid_location.json");

        Location loc = gson.fromJson(jsonLocation, Location.class);

        assertEquals(LATITUDE, loc.getLatitude());
        assertEquals(LONGITUDE, loc.getLongitude());
        assertEquals(NAME, loc.getName());
        assertEquals(CITY, loc.getCity());
        assertEquals(COUNTRY, loc.getCountry());
    }


    @Test
    public void deserializerFailsGracefullyWithMalformedLocation() {
        JsonObject jsonLocation = TestUtils.readJson(TestUtils.PATH_PREFIX + "invalid_location.json");

        Location loc = gson.fromJson(jsonLocation, Location.class);

        assertNull(loc);
    }
}
