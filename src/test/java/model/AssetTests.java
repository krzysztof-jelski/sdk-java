/*
Copyright: Ambrosus Technologies GmbH
Email: tech@ambrosus.com

This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.

This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
*/

package model;

import com.ambrosus.commons.Location;
import com.ambrosus.commons.Message;
import com.ambrosus.commons.RawJson;
import com.ambrosus.commons.Transport;
import com.ambrosus.model.Asset;
import com.ambrosus.model.Event;
import com.ambrosus.model.MetaData;
import com.ambrosus.utils.JsonUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.junit.Before;
import org.junit.Test;
import utils.TestUtils;

import java.util.*;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

public class AssetTests {

    Asset asset;
    MetaData assetMetaData;

    Location loc1, loc2, loc3;
    Transport transport1, transport2, transport3;
    Event e1, e2, e3;

    private Gson gson;
    private Asset.Builder assetBuilder;
    private String ASSET_ID;
    private String SIGNATURE;
    private String CREATED_BY;
    private Integer SEQUENCE_NUMBER;
    private Long TIMESTAMP;
    private String BUNDLE_ID;
    private Long ENTITY_UPLOAD_TIMESTAMP;

    @Before
    public void init() {

        GsonBuilder gb = new GsonBuilder();
        gb.registerTypeAdapter(Asset.class, new Asset.Adapter(null));
        gson = gb.create();

        ASSET_ID = "0x30dbe10b0596e03810a051542302488fd45f8cc5a441cf202fd4b3ece4317f5e";
        SIGNATURE =
                "0x0698843ca3523b4c2975072acfc41bd2cb694531fb18595f1661234ce7a1bbd53e423aa96936ec8ff5aa6aae77979a9616a642e66749696a7abe608ab083f0371b";
        CREATED_BY = "0x9566AC7630F7075a981670709de09ff9c3032D9c";
        SEQUENCE_NUMBER = 3;
        TIMESTAMP = 1503424923L;
        BUNDLE_ID = "0x352d6548599186d04b20a1bbf269797fc1e5e2f2f887f12aba04a182cabd230b";
        ENTITY_UPLOAD_TIMESTAMP = 1531412126L;


        assetMetaData = new MetaData(BUNDLE_ID, ENTITY_UPLOAD_TIMESTAMP);

        assetBuilder = new Asset.Builder();

        assetBuilder
                .setAssetId("0x30dbe10b0596e03810a051542302488fd45f8cc5a441cf202fd4b3ece4317f5e")
                .setSequenceNumber(SEQUENCE_NUMBER)
                .setSignature(SIGNATURE)
                .setCreatedBy(CREATED_BY)
                .setTimestamp(TIMESTAMP)
                .setMetaData(assetMetaData);

        loc1 = new Location(1, 1, "loc1", "city1", "country1");
        loc2 = new Location(2, 2, "loc2", "city2", "country2");
        loc3 = new Location(3, 3, "loc3", "city3", "country3");

        transport1 = new Transport("name1", "status1", "vehicle1");
        transport2 = new Transport("name2", "status2", "vehicle2");
        transport3 = new Transport("name3", "status3", "vehicle3");

        e1 = EventTests.createEvent("event1", 1, Arrays.asList(transport1, loc1));
        e2 = EventTests.createEvent("event2", 2, Arrays.asList(transport2, loc2));
        e3 = EventTests.createEvent("event3", 3, Arrays.asList(transport3, loc3));

        assetBuilder
                .addEvent(e1)
                .addEvent(e3)
                .addEvent(e2);

        asset = assetBuilder.build();
    }


    @Test
    public void eventGettersAreCorrect() {
        assertEquals(ASSET_ID, asset.getAssetId());
        assertEquals(SEQUENCE_NUMBER, asset.getSequenceNumber());
        assertEquals(SIGNATURE, asset.getSignature());
        assertEquals(Arrays.asList(e3, e2, e1), asset.getEventsList());
        assertEquals(assetMetaData, asset.getMetaData());
        assertEquals(TIMESTAMP, asset.getTimestamp());
        assertEquals(CREATED_BY, asset.getCreatedBy());
    }


    @Test
    public void eventDataSortedByTimestamp() {

        List<Event> l = asset.getEventsList();

        assertTrue(l.get(0).getTimestamp() > l.get(1).getTimestamp());
        assertTrue(l.get(1).getTimestamp() > l.get(2).getTimestamp());
    }


    @Test
    public void getEventDataByClassIsCorrect() {
        List<Transport> transports = asset.sectionsOfType(Transport.class);

        assertEquals(transports, Arrays.asList(transport3, transport2, transport1));
        assertEquals(transport3, asset.firstOf(Transport.class));
        assertEquals(transport1, asset.lastOf(Transport.class));
    }


    @Test
    public void recognizesEventDataType() {
        assertTrue(asset.hasEventDataOfType(Transport.class));
        assertTrue(asset.hasEventDataOfType(Location.class));
        assertFalse(asset.hasEventDataOfType(RawJson.class));
    }


    @Test
    public void eventsContainingTypesIsCorrect() {
        Set<Event> transportEvents = asset.eventsContainingType(Transport.class);
        Set<Event> messageEvents = asset.eventsContainingType(Message.class);

        assertEquals(3, transportEvents.size());
        assertTrue(messageEvents.isEmpty());

        assertEquals(new HashSet<>(Arrays.asList(e1, e2, e3)), transportEvents);
    }


    @Test
    public void builderIsCorrect() {

        assertEquals(assetBuilder.getAssetId(), ASSET_ID);
        assertEquals(assetBuilder.getSequenceNumber(), SEQUENCE_NUMBER);
        assertEquals(assetBuilder.getSignature(), SIGNATURE);

        Asset asset = assetBuilder.build();

        assertEquals(assetBuilder.getAssetId(), asset.getAssetId());
        assertEquals(assetBuilder.getSequenceNumber(), asset.getSequenceNumber());
        assertEquals(assetBuilder.getSignature(), asset.getSignature());

        List<Event> sortedEvents = new ArrayList<>(assetBuilder.getEventsList());
        Collections.sort(sortedEvents);

        assertEquals(sortedEvents, asset.getEventsList());
    }


    @Test
    public void deserializationIsCorrect() {

        Asset asset = gson.fromJson(TestUtils.readJson(TestUtils.PATH_PREFIX + "valid_asset.json"), Asset.class);

        assertEquals(ASSET_ID, asset.getAssetId());
        assertEquals(SEQUENCE_NUMBER, asset.getSequenceNumber());
        assertEquals(SIGNATURE, asset.getSignature());
    }


    @Test
    public void serializationisCorrect() {

        JsonObject jsonAsset = TestUtils.readJson(TestUtils.PATH_PREFIX + "valid_asset.json");
        assertEquals(JsonUtils.recursiveSort(jsonAsset).toString(), gson.toJson(asset));
    }
}
