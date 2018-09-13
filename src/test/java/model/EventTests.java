/*
Copyright: Ambrosus Technologies GmbH
Email: tech@ambrosus.com

This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.

This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
*/

package model;

import com.ambrosus.AmbrosusSDK;
import com.ambrosus.commons.Location;
import com.ambrosus.commons.Message;
import com.ambrosus.commons.RawJson;
import com.ambrosus.commons.Transport;
import com.ambrosus.model.Event;
import com.ambrosus.model.EventData;
import com.ambrosus.model.JsonProperties;
import com.ambrosus.model.MetaData;
import com.ambrosus.utils.CryptoUtils;
import com.ambrosus.utils.JsonUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import org.junit.Before;
import org.junit.Test;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import utils.TestUtils;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

public class EventTests {

    Event event;
    Event.Builder eventBuilder;
    Gson gson;
    RawJson raw1;
    Message message1;
    Location loc1, loc2, loc3;
    Transport transport1, transport2, transport3;

    private ECKeyPair keys;

    private String CREATED_BY = "0x9566AC7630F7075a981670709de09ff9c3032D9c";
    private String EVENT_ID = "0x5d3b2a65e3897283df06b5625cb85c8842c6a952e523c33ffcecb96ee4c53ea9";
    private String SIGNATURE =
            "0x4aacdf5e771a1b5724d99ebdfd6cdb33b2fbd7c893e5f8b509e2df4e36d004fd62015d67901bc7d78a92520c9536894b7042c73ad2821b7dff6a1b90782a184c1b";
    private Integer ACCESS_LEVEL = 0;
    private String ASSET_ID = "0x30dbe10b0596e03810a051542302488fd45f8cc5a441cf202fd4b3ece4317f5e";
    private String DATA_HASH = "0xfa3e252c199657c116e7ada2b8ae083fac7e0915c8abf059dc6af6b02f2f7d21";
    private Long TIMESTAMP = 1533824995L;
    private String BUNDLE_ID = "0xe7e226f8e5c1ecbb947d1a268b97e2eead7306b81d79c8e257188204ccc9e1c8";
    private Long ENTITY_UPLOAD_TIMESTAMP = 1533824998L;


    public static Event createEvent(String id, long timestamp, List<EventData> dataList) {
        Event.Builder eventBuilder = new Event.Builder();

        MetaData eventMetaData = new MetaData("bundleId", 1234L);

        eventBuilder
                .setEventId(id)
                .setAccessLevel(1)
                .setDataHash("dataHash")
                .setSignature("signature")
                .setCreatedBy("createdBy")
                .setTimestamp(timestamp)
                .setMetaData(eventMetaData);

        eventBuilder.addAllEventData(dataList);

        return eventBuilder.build();
    }


    @Before
    public void init() {

        Map<String, Type> typeMap = new HashMap<>();
        typeMap.put("ambrosus.asset.location", Location.class);
        typeMap.put("ambrosus.event.transport", Transport.class);
        typeMap.put("ambrosus.event.message", Message.class);

        GsonBuilder gb = new GsonBuilder();

        try {
            keys = Keys.createEcKeyPair();
        } catch (Exception e) {
            e.printStackTrace();
        }

        gb.registerTypeAdapter(Event.class, new Event.Adapter(keys));

        gb.registerTypeAdapter(RawJson.class, new RawJson.Adapter());
        gb.registerTypeAdapter(new TypeToken<List<EventData>>() {
        }.getType(), new EventData.Adapter(typeMap));
        gb.setExclusionStrategies(new AmbrosusSDK.AmbTypeExclusionStrategy());
        gson = gb.create();

        JsonObject innerJObj = new JsonObject();
        innerJObj.addProperty("customField", "customValue");
        innerJObj.addProperty("type", "ambrosus.event.customevent");

        raw1 = new RawJson(innerJObj);

        message1 = new Message("Product placed on shelf");

        loc1 = new Location(1, 1, "loc1", "city1", "country1");
        loc2 = new Location(2, 2, "loc2", "city2", "country2");
        loc3 = new Location(3, 3, "loc3", "city3", "country3");

        transport1 = new Transport("name1", "status1", "vehicle1");
        transport2 = new Transport("name2", "status2", "vehicle2");
        transport3 = new Transport("name3", "status3", "vehicle3");

        event = createEvent("event1", 1, Arrays.asList(loc1, loc2, loc3, transport1, transport2, transport3));

        eventBuilder = new Event.Builder();
        eventBuilder
                .setAssetId(ASSET_ID)
                .setDataHash(DATA_HASH)
                .setEventId(EVENT_ID)
                .setAccessLevel(ACCESS_LEVEL)
                .setDataHash(DATA_HASH)
                .setSignature(SIGNATURE)
                .addAllEventData(Arrays.asList(raw1, message1))
                .setCreatedBy(CREATED_BY)
                .setTimestamp(TIMESTAMP)
                .setMetaData(new MetaData(BUNDLE_ID, ENTITY_UPLOAD_TIMESTAMP));
    }


    @Test
    public void eventGettersAreCorrect() {
        assertTrue(event.hasDataOfType(Location.class));
        assertTrue(event.hasDataOfType(Transport.class));
        assertFalse(event.hasDataOfType(RawJson.class));
        assertEquals(loc1, event.firstOf(Location.class));
        assertEquals(Arrays.asList(loc1, loc2, loc3), event.eventDataWithType(Location.class));
        assertEquals(Arrays.asList(transport1, transport2, transport3), event.eventDataWithType(Transport.class));
    }


    @Test
    public void builderIsCorrect() {

        assertEquals(ASSET_ID, eventBuilder.getAssetId());
        assertEquals(DATA_HASH, eventBuilder.getDataHash());
        assertEquals(EVENT_ID, eventBuilder.getEventId());
        assertEquals(SIGNATURE, eventBuilder.getSignature());
        assertEquals(ACCESS_LEVEL, eventBuilder.getAccessLevel());
        assertEquals(Arrays.asList(raw1, message1), eventBuilder.getEventDataList());

        Event event = eventBuilder.build();

        assertEquals(ASSET_ID, event.getAssetId());
        assertEquals(DATA_HASH, event.getDataHash());
        assertEquals(EVENT_ID, event.getEventId());
        assertEquals(SIGNATURE, event.getSignature());
        assertEquals(ACCESS_LEVEL, event.getAccessLevel());
        assertEquals(Arrays.asList(raw1, message1), event.getEventDataList());
    }


    @Test
    public void serializerIsCorrect() {
        JsonObject jsonEvent = TestUtils.readJson(TestUtils.PATH_PREFIX + "valid_event.json");

        JsonObject content = jsonEvent.getAsJsonObject("content");
        content.add("idData", JsonUtils.recursiveSort(content.getAsJsonObject("idData")));

        assertEquals(jsonEvent.toString(), gson.toJson(eventBuilder.build()));
    }


    @Test
    public void missingHashAndSignatureGetComputed() {

        JsonObject jsonEvent = TestUtils.readJson(TestUtils.PATH_PREFIX + "valid_event.json");


        eventBuilder.setDataHash(null);
        eventBuilder.setSignature(null);

        Event event = gson.fromJson(gson.toJson(eventBuilder.build()), Event.class);

        JsonArray data = jsonEvent.getAsJsonObject("content").getAsJsonArray("data");
        JsonObject idData = jsonEvent.getAsJsonObject("content").getAsJsonObject("idData");

        String dataHash = CryptoUtils.computeHashString(JsonUtils.arraySort(data).toString());

        idData.addProperty(JsonProperties.DATA_HASH, dataHash);
        String signature = CryptoUtils.computeSignature(JsonUtils.recursiveSort(idData).toString(), keys);

        assertEquals(signature, event.getSignature());
    }


    @Test
    public void deserializerIsCorrect() {
        JsonObject jsonEvent = TestUtils.readJson(TestUtils.PATH_PREFIX + "valid_event.json");
        Event event = gson.fromJson(jsonEvent, Event.class);

        assertEquals(ASSET_ID, event.getAssetId());
        assertEquals(DATA_HASH, event.getDataHash());
        assertEquals(EVENT_ID, event.getEventId());
        assertEquals(SIGNATURE, event.getSignature());
        assertEquals(ACCESS_LEVEL, event.getAccessLevel());
        assertEquals(Arrays.asList(raw1, message1), event.getEventDataList());
    }


    @Test
    public void deserializerFailsGracefullyWithMalformedEventJson() {
        JsonObject jObj = new JsonObject();

        assertNull(new Event.Adapter(null).deserialize(jObj, Event.class, null));
    }


    @Test
    public void comparisonDoneByTimestampDecreasing() {
        Event e1 = createEvent("e1", 1, Arrays.asList(loc1, transport1));
        Event e2 = createEvent("e2", 1, Arrays.asList(loc1, transport1));
        Event e3 = createEvent("e3", 2, Arrays.asList(loc1, transport1));


        assertEquals(-1, e3.compareTo(e1));
        assertEquals(0, e1.compareTo(e2));
        assertEquals(0, e2.compareTo(e2));
        assertEquals(0, e3.compareTo(e3));
        assertEquals(1, e1.compareTo(e3));
    }


    @Test
    public void builderCopyIsCorrect() {

        Event.Builder eb = Event.Builder.fromExistingEvent(event);
        Event copyEvent = eb.build();

        assertEquals(event.getAssetId(), copyEvent.getAssetId());
        assertEquals(event.getDataHash(), copyEvent.getDataHash());
        assertEquals(event.getEventId(), copyEvent.getEventId());
        assertEquals(event.getSignature(), copyEvent.getSignature());
        assertEquals(event.getAccessLevel(), copyEvent.getAccessLevel());
        assertEquals(event.getEventDataList(), copyEvent.getEventDataList());
    }
}
