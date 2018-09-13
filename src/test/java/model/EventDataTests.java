/*
Copyright: Ambrosus Technologies GmbH
Email: tech@ambrosus.com

This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.

This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
*/

package model;

import com.ambrosus.commons.Location;
import com.ambrosus.commons.Transport;
import com.ambrosus.model.Event;
import com.ambrosus.model.EventData;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

public class EventDataTests {

    private Location loc;
    private Transport transport;
    private Gson gson;
    private Type datalistType;


    @Before
    public void init() {

        datalistType = new TypeToken<List<EventData>>() {
        }.getType();

        Map<String, Type> typeMap = new HashMap<>();
        typeMap.put("ambrosus.asset.location", Location.class);
        typeMap.put("ambrosus.event.transport", Transport.class);

        GsonBuilder gb = new GsonBuilder();
        gb.registerTypeAdapter(Location.class, new Location.Adapter());
        gb.registerTypeAdapter(datalistType, new EventData.Adapter(typeMap));
        gson = gb.create();

        loc = new Location(1, 1, "loc1", "city1", "country1");
        transport = new Transport("name1", "status1", "vehicle1");
    }


    @Test
    public void dataCorrectlyLinkedToParent() {
        Event event = EventTests.createEvent("e1", 1L, Arrays.asList(transport, loc));

        assertEquals(event, transport.getParentEvent());
        assertEquals(event, loc.getParentEvent());
    }


    @Test
    public void invalidInputResultsInEmptyList() {

        List<EventData> eventData = gson.fromJson(new JsonObject(), datalistType);
        assertTrue(eventData.isEmpty());
    }


    @Test
    public void failsGracefullyOnInvalidArrayContentType() {
        JsonArray array = new JsonArray();
        array.add(new JsonPrimitive(2));

        List<EventData> eventData = gson.fromJson(array, datalistType);
        assertTrue(eventData.isEmpty());
    }


    @Test
    public void failsGracefullyOnUntypedJsonObject() {
        JsonObject object = new JsonObject();
        object.addProperty("prop1", "value1");

        JsonArray array = new JsonArray();

        array.add(object);

        List<EventData> eventData = gson.fromJson(array, datalistType);
        assertTrue(eventData.isEmpty());
    }


    @Test
    public void failsGracefullyOnInvalidObject() {
        JsonObject object = new JsonObject();
        object.addProperty("type", "ambrosus.asset.location");
        object.addProperty("prop1", "value1");

        JsonArray array = new JsonArray();

        array.add(object);

        List<EventData> eventData = gson.fromJson(array, datalistType);
        assertTrue(eventData.isEmpty());
    }


    @Test
    public void deserializesRegisteredTypeCorrectly() {

        JsonArray array = new JsonArray();

        array.add(gson.toJsonTree(transport));
        array.add(gson.toJsonTree(loc));

        List<EventData> eventData = gson.fromJson(array, datalistType);

        assertEquals(2, eventData.size());
        assertEquals(transport, eventData.get(0));
        assertEquals(loc, eventData.get(1));
    }
}
