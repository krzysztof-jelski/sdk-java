/*
Copyright: Ambrosus Technologies GmbH
Email: tech@ambrosus.com

This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.

This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
*/

import com.ambrosus.AmbrosusSDK;
import com.ambrosus.commons.Location;
import com.ambrosus.commons.RawJson;
import com.ambrosus.model.Account;
import com.ambrosus.model.Asset;
import com.ambrosus.model.Event;
import com.ambrosus.model.EventData;
import com.ambrosus.network.ResponseWrapper;
import com.ambrosus.utils.Consumer;
import com.ambrosus.utils.JsonUtils;
import com.ambrosus.utils.NetworkUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.Before;
import org.junit.Test;
import utils.TestUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static com.ambrosus.network.AmbrosusService.SECRET_PREAMBLE;
import static com.ambrosus.network.AmbrosusService.TOKEN_PREAMBLE;
import static org.junit.Assert.*;
import static utils.TestUtils.PATH_PREFIX;
import static utils.TestUtils.noOp;

public class SDKTests {

    private final static String PRIVATE_KEY = "0x012345";
    private final static String TEST_ADDRESS = "0xABCDEF";
    private final static String TEST_ASSET_ID = "0xABC123";
    private final static String TEST_TOKEN = "0x543210";
    private AmbrosusSDK.Config config;
    private AmbrosusSDK ambrosus;
    private Gson gson;
    private MockWebServer mockWebServer;
    private Object asyncCallBackResult;


    @Before
    public void init() throws IOException {

        mockWebServer = new MockWebServer();
        mockWebServer.start();

        config = new AmbrosusSDK.Config(
                mockWebServer.url("/").toString(),
                PRIVATE_KEY
        );

        ambrosus = new AmbrosusSDK(config);
        ambrosus.registerEventDataType("ambrosus.event.location", Location.class, new Location
                .Adapter());
        ambrosus.init();

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Asset.class, new Asset.Adapter(null));
        gsonBuilder.registerTypeAdapter(Event.class, new Event.Adapter(null));
        gsonBuilder.registerTypeAdapter(RawJson.class, new RawJson.Adapter());
        gsonBuilder.registerTypeAdapter(new TypeToken<List<EventData>>() {
        }.getType(), new EventData.Adapter(new HashMap<>()));
        gson = gsonBuilder.create();

    }


    @Test
    public void addressComputationBySDKIsCorrect() {
        assertEquals("0xae0478140036d14e93A7B7482512e1d91745B650", ambrosus.getAddress());
    }


    @Test
    public void getAssetIsCorrect() throws InterruptedException {


        mockWebServer.enqueue(new MockResponse().setBody("{}"));
        ambrosus.getAsset(TEST_ASSET_ID, assetResponseWrapper -> {
        }, noOp());

        RecordedRequest recordedRequest = mockWebServer.takeRequest();

        assertEquals(String.format("GET /assets/%s HTTP/1.1", TEST_ASSET_ID),
                recordedRequest.getRequestLine());

    }


    @Test
    public void getEventsIsCorrect() throws InterruptedException {


        mockWebServer.enqueue(new MockResponse().setBody("{}"));
        ambrosus.getEvents(TEST_ASSET_ID, optionalQueryResults -> {
        }, noOp());

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals(String.format("GET /events?assetId=%s HTTP/1.1", TEST_ASSET_ID),
                recordedRequest.getRequestLine());

    }


    @Test
    public void getAssetWithEventsIsCorrect() throws InterruptedException {


        CountDownLatch latch = new CountDownLatch(1);

        JsonObject assetJson = TestUtils.readJson(PATH_PREFIX + "valid_asset.json");
        JsonObject eventJson = TestUtils.readJson(PATH_PREFIX + "event_query_response.json");

        mockWebServer.enqueue(new MockResponse().setBody(assetJson.toString()));
        mockWebServer.enqueue(new MockResponse().setBody(eventJson.toString()));

        ambrosus.getAssetWithEvents(TEST_ASSET_ID, assetResponseWrapper -> {
            latch.countDown();
        }, (o, t) -> {
        });

        latch.await();

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals(String.format("GET /assets/%s HTTP/1.1", TEST_ASSET_ID),
                recordedRequest.getRequestLine());

        recordedRequest = mockWebServer.takeRequest();
        assertEquals(String.format("GET /events?assetId=%s HTTP/1.1",
                JsonUtils.elementWithPath(assetJson, "assetId").get().getAsString()),
                recordedRequest.getRequestLine());

    }


    @Test
    public void getAccountIsCorrect() throws InterruptedException {

        mockWebServer.enqueue(new MockResponse().setBody("{}"));
        ambrosus.getAccount(TEST_TOKEN,
                TEST_ADDRESS,
                optionalQueryResults -> {
                }, noOp());

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals(String.format("GET /accounts/%s HTTP/1.1", TEST_ADDRESS),
                recordedRequest.getRequestLine());

        assertEquals(recordedRequest.getHeader("Authorization"), TOKEN_PREAMBLE + TEST_TOKEN);
    }


    @Test
    public void findAssetIsCorrect() throws InterruptedException {

        Map<String, String> params = new HashMap<>();

        String p1 = "param1", p2 = "param2";
        String v1 = "value1", v2 = "value2";

        params.put(p1, v1);
        params.put(p2, v2);

        mockWebServer.enqueue(new MockResponse().setBody("{}"));
        ambrosus.findAssets(params,
                optionalQueryResults -> {
                }, noOp());

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals(String.format("GET /assets?%s=%s&%s=%s HTTP/1.1", p1, v1, p2, v2),
                recordedRequest.getRequestLine());
    }


    @Test
    public void findEventsIsCorrect() throws InterruptedException {

        Map<String, String> params = new HashMap<>();

        String p1 = "param1", p2 = "param2";
        String v1 = "value1", v2 = "value2";

        params.put(p1, v1);
        params.put(p2, v2);

        mockWebServer.enqueue(new MockResponse().setBody("{}"));
        ambrosus.findEvents(params,
                optionalQueryResults -> {
                }, noOp());

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals(String.format("GET /events?%s=%s&%s=%s HTTP/1.1", p1, v1, p2, v2),
                recordedRequest.getRequestLine());

    }


    @Test
    public void findAccountIsCorrect() throws InterruptedException {

        Map<String, String> params = new HashMap<>();

        String p1 = "param1", p2 = "param2";
        String v1 = "value1", v2 = "value2";

        params.put(p1, v1);
        params.put(p2, v2);

        mockWebServer.enqueue(new MockResponse().setBody("{}"));
        ambrosus.findAccounts(TEST_TOKEN,
                params,
                optionalQueryResults -> {
                }, noOp());

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals(String.format("GET /accounts?%s=%s&%s=%s HTTP/1.1", p1, v1, p2, v2),
                recordedRequest.getRequestLine());

        assertEquals(recordedRequest.getHeader("Authorization"), TOKEN_PREAMBLE + TEST_TOKEN);
    }


    @Test
    public void dataHashVerificationIsCorrect() {

        JsonObject validObj = TestUtils.readJson(PATH_PREFIX + "valid_event2.json");
        JsonObject invalidObj = TestUtils.readJson(PATH_PREFIX + "invalid_datahash_event.json");

        assertTrue(ambrosus.verifyDatahash(gson.fromJson(validObj, Event.class)));
        assertFalse(ambrosus.verifyDatahash(gson.fromJson(invalidObj, Event.class)));
    }


    @Test
    public void assetSignatureVerificationIsCorrect() {
        JsonObject validObj = TestUtils.readJson(PATH_PREFIX + "valid_asset.json");
        JsonObject invalidObj = TestUtils.readJson(PATH_PREFIX + "invalid_signature_asset.json");

        assertTrue(ambrosus.verifySignature(gson.fromJson(validObj, Asset.class)));
        assertFalse(ambrosus.verifySignature(gson.fromJson(invalidObj, Asset.class)));
    }


    @Test
    public void eventSignatureVerificationIsCorrect() {
        JsonObject validObj = TestUtils.readJson(PATH_PREFIX + "valid_event2.json");
        JsonObject invalidObj = TestUtils.readJson(PATH_PREFIX + "invalid_signature_event.json");

        assertTrue(ambrosus.verifySignature(gson.fromJson(validObj, Event.class)));
        assertFalse(ambrosus.verifySignature(gson.fromJson(invalidObj, Event.class)));
    }


    @Test
    public void exceptionInSignatureDecodeIsCaught() {

        JsonObject invalidObj = TestUtils.readJson(PATH_PREFIX + "invalid_signature_format_event.json");
        assertFalse(ambrosus.verifySignature(gson.fromJson(invalidObj, Event.class)));
    }


    @Test
    public void createAssetIsCorrect() throws InterruptedException {

        Asset.Builder assetBuilder = new Asset.Builder();
        assetBuilder.setSequenceNumber(0);
        assetBuilder.setCreatedBy(ambrosus.getAddress());
        assetBuilder.setTimestamp(123L);

        Asset asset = assetBuilder.build();

        mockWebServer.enqueue(new MockResponse().setBody("{}"));
        ambrosus.createAsset(assetBuilder, assetResponseWrapper -> {
        }, noOp());

        RecordedRequest recordedRequest = mockWebServer.takeRequest();

        assertEquals("POST /assets HTTP/1.1",
                recordedRequest.getRequestLine());


        JsonObject jsonAsset = gson.fromJson(recordedRequest.getBody().readUtf8(), JsonObject.class);

        assertEquals(
                JsonUtils.elementWithPath(jsonAsset, "content|idData|createdBy").get().getAsString(),
                asset.getCreatedBy()
        );

        assertEquals(
                Integer.valueOf(JsonUtils.elementWithPath(jsonAsset, "content|idData|sequenceNumber").get().getAsInt()),
                asset.getSequenceNumber()
        );

        assertEquals(
                Long.valueOf(JsonUtils.elementWithPath(jsonAsset, "content|idData|timestamp").get().getAsLong()),
                asset.getTimestamp()
        );
    }


    @Test
    public void missingAssetInfoIsCompletedBySDK() throws InterruptedException {
        Asset.Builder assetBuilder = new Asset.Builder();

        mockWebServer.enqueue(new MockResponse().setBody("{}"));
        ambrosus.createAsset(assetBuilder, assetResponseWrapper -> {
        }, noOp());

        RecordedRequest recordedRequest = mockWebServer.takeRequest();

        JsonObject jsonAsset = gson.fromJson(recordedRequest.getBody().readUtf8(), JsonObject.class);

        assertTrue(
                JsonUtils.elementWithPath(jsonAsset, "content|idData|createdBy").isPresent()
        );

        assertTrue(
                JsonUtils.elementWithPath(jsonAsset, "content|idData|sequenceNumber").isPresent()
        );

        assertTrue(
                JsonUtils.elementWithPath(jsonAsset, "content|idData|timestamp").isPresent()
        );
    }


    @Test
    public void missingEventInfoIsCompletedBySDK() throws InterruptedException {
        Event.Builder eventBuilder = new Event.Builder();

        eventBuilder.setAssetId(TEST_ASSET_ID);

        mockWebServer.enqueue(new MockResponse().setBody("{}"));
        ambrosus.createEvent(eventBuilder, assetResponseWrapper -> {
        }, noOp());

        RecordedRequest recordedRequest = mockWebServer.takeRequest();

        JsonObject jsonEvent = gson.fromJson(recordedRequest.getBody().readUtf8(), JsonObject.class);

        assertTrue(
                JsonUtils.elementWithPath(jsonEvent, "content|idData|createdBy").isPresent()
        );

        assertTrue(
                JsonUtils.elementWithPath(jsonEvent, "content|idData|timestamp").isPresent()
        );

        assertTrue(
                JsonUtils.elementWithPath(jsonEvent, "content|idData|accessLevel").isPresent()
        );
    }


    @Test(expected = IllegalArgumentException.class)
    public void missingAssetIdForEventThrowsException() {

        Event.Builder eventBuilder = new Event.Builder();
        mockWebServer.enqueue(new MockResponse().setBody("{}"));
        ambrosus.createEvent(eventBuilder, assetResponseWrapper -> {
        }, noOp());
    }


    @Test
    public void createEventIsCorrect() throws InterruptedException {

        Event.Builder eventBuilder = new Event.Builder();
        eventBuilder.setCreatedBy(ambrosus.getAddress());
        eventBuilder.setTimestamp(123L);
        eventBuilder.setAccessLevel(5);
        eventBuilder.setAssetId(TEST_ASSET_ID);

        eventBuilder.addEventData(new RawJson(new JsonObject()));

        Event event = eventBuilder.build();

        mockWebServer.enqueue(new MockResponse().setBody("{}"));
        ambrosus.createEvent(eventBuilder, eventResponseWrapper -> {
        }, noOp());

        RecordedRequest recordedRequest = mockWebServer.takeRequest();

        assertEquals(String.format("POST /assets/%s/events HTTP/1.1", TEST_ASSET_ID),
                recordedRequest.getRequestLine());


        JsonObject jsonEvent = gson.fromJson(recordedRequest.getBody().readUtf8(), JsonObject.class);


        assertEquals(
                JsonUtils.elementWithPath(jsonEvent, "content|idData|createdBy").get().getAsString(),
                event.getCreatedBy()
        );

        assertEquals(
                JsonUtils.elementWithPath(jsonEvent, "content|idData|assetId").get().getAsString(),
                event.getAssetId()
        );

        assertEquals(
                Long.valueOf(JsonUtils.elementWithPath(jsonEvent, "content|idData|timestamp").get().getAsLong()),
                event.getTimestamp()
        );

        assertEquals(
                Integer.valueOf(JsonUtils.elementWithPath(jsonEvent, "content|idData|accessLevel").get().getAsInt()),
                event.getAccessLevel()
        );

    }


    @Test
    public void createAccountIsCorrect() throws InterruptedException {

        Account.Builder accountBuilder = new Account.Builder();

        accountBuilder.setAddress("Account Address");
        accountBuilder.setAccessLevel(5);
        accountBuilder.addPermission(Account.Permission.REGISTER_ACCOUNT);

        Account account = accountBuilder.build();

        mockWebServer.enqueue(new MockResponse().setBody("{}"));
        ambrosus.createAccount(TEST_TOKEN, accountBuilder, eventResponseWrapper -> {
        }, noOp());

        RecordedRequest recordedRequest = mockWebServer.takeRequest();

        assertEquals(String.format("POST /accounts HTTP/1.1", TEST_ASSET_ID),
                recordedRequest.getRequestLine());

        assertEquals(recordedRequest.getHeader("Authorization"), TOKEN_PREAMBLE + TEST_TOKEN);


        JsonObject jsonAccount = gson.fromJson(recordedRequest.getBody().readUtf8(), JsonObject.class);

        assertEquals(
                JsonUtils.elementWithPath(jsonAccount, "address").get().getAsString(),
                account.getAddress()
        );

        assertEquals(
                Integer.valueOf(JsonUtils.elementWithPath(jsonAccount, "accessLevel").get().getAsInt()),
                account.getAccessLevel()
        );

        List<Account.Permission> javaPermissions = account.getPermissions();
        JsonArray jsonPermissions = JsonUtils.elementWithPath(jsonAccount, "permissions").get().getAsJsonArray();
        assertEquals(account.getPermissions().size(), jsonPermissions.size());

        for (int i = 0; i < jsonPermissions.size(); i++) {
            assertEquals(javaPermissions.get(i).name().toLowerCase(), jsonPermissions.getAsString());
        }
    }


    @Test
    public void missingAccountInfoIsCompletedBySDK() throws InterruptedException {

        Account.Builder accountBuilder = new Account.Builder();

        accountBuilder.setAddress("Account Address");

        mockWebServer.enqueue(new MockResponse().setBody("{}"));
        ambrosus.createAccount(TEST_TOKEN, accountBuilder, eventResponseWrapper -> {
        }, noOp());

        RecordedRequest recordedRequest = mockWebServer.takeRequest();

        JsonObject jsonAccount = gson.fromJson(recordedRequest.getBody().readUtf8(), JsonObject.class);

        assertTrue(
                JsonUtils.elementWithPath(jsonAccount, "accessLevel").isPresent()
        );
    }


    @Test(expected = IllegalArgumentException.class)
    public void missingAddressForAccountThrowsException() {

        Account.Builder accountBuilder = new Account.Builder();
        mockWebServer.enqueue(new MockResponse().setBody("{}"));
        ambrosus.createAccount(TEST_TOKEN, accountBuilder, eventResponseWrapper -> {
        }, noOp());
    }


    @Test
    public void createTokenIsCorrect() throws InterruptedException {

        mockWebServer.enqueue(new MockResponse().setBody("{}"));

        Long validUntil = 123L;

        ambrosus.createToken(validUntil, tokenResponseWrapper -> {

        }, noOp());

        RecordedRequest recordedRequest = mockWebServer.takeRequest();

        assertEquals("POST /token HTTP/1.1",
                recordedRequest.getRequestLine());

        assertEquals(recordedRequest.getHeader("Authorization"), SECRET_PREAMBLE + PRIVATE_KEY);

        JsonObject jsonAccount = gson.fromJson(recordedRequest.getBody().readUtf8(), JsonObject.class);

        assertEquals(Long.valueOf(JsonUtils.elementWithPath(jsonAccount, "validUntil").get().getAsLong()),
                validUntil);
    }


    @Test
    public void defaultCallBackDoesNothingOnFailure() {

        asyncCallBackResult = null;

        NetworkUtils.DefaultCallback<Consumer<ResponseWrapper<Asset>>> consumerDefaultCallback =
                new NetworkUtils.DefaultCallback<>(assetRW -> {

                    // Verify call
                    setAsyncCallBackResult(true);
                }, noOp());

        assertNull(asyncCallBackResult);

        consumerDefaultCallback.onFailure(null, new IOException("error"));
    }


    @Test(expected = IllegalStateException.class)
    public void initTwiceThrowsException() {
        ambrosus.init();
    }


    @Test(expected = IllegalStateException.class)
    public void uninitializedSDKThrowsExceptionOnAssetFind() {
        ambrosus = new AmbrosusSDK(config);
        ambrosus.findAssets(null, null, null);
    }


    @Test(expected = IllegalStateException.class)
    public void uninitializedSDKThrowsExceptionOnEventFind() {
        ambrosus = new AmbrosusSDK(config);
        ambrosus.findEvents(null, null, null);
    }


    @Test(expected = IllegalStateException.class)
    public void uninitializedSDKThrowsExceptionOnAccountFind() {
        ambrosus = new AmbrosusSDK(config);
        ambrosus.findAccounts(null, null, null, null);
    }


    @Test(expected = IllegalStateException.class)
    public void uninitializedSDKThrowsExceptionOnAssetGet() {
        ambrosus = new AmbrosusSDK(config);
        ambrosus.getAsset(null, null, null);
    }


    @Test(expected = IllegalStateException.class)
    public void uninitializedSDKThrowsExceptionOnEventGet() {
        ambrosus = new AmbrosusSDK(config);
        ambrosus.getEvents(null, null, null);
    }


    @Test(expected = IllegalStateException.class)
    public void uninitializedSDKThrowsExceptionOnAccountGet() {
        ambrosus = new AmbrosusSDK(config);
        ambrosus.getAccount(null, null, null, null);
    }


    @Test(expected = IllegalStateException.class)
    public void uninitializedSDKThrowsExceptionOnAssetCreate() {
        ambrosus = new AmbrosusSDK(config);
        ambrosus.createAsset(null, null, null);
    }


    @Test(expected = IllegalStateException.class)
    public void uninitializedSDKThrowsExceptionOnEventCreate() {
        ambrosus = new AmbrosusSDK(config);
        ambrosus.createEvent(null, null, null);
    }


    @Test(expected = IllegalStateException.class)
    public void uninitializedSDKThrowsExceptionOnAccountCreate() {
        ambrosus = new AmbrosusSDK(config);
        ambrosus.createAccount(null, null, null, null);
    }


    @Test(expected = IllegalStateException.class)
    public void uninitializedSDKThrowsExceptionOnTokenCreate() {
        ambrosus = new AmbrosusSDK(config);
        ambrosus.createToken(null, null, null);
    }


    @Test
    public void fromJsonIsCorrect() {
        JsonObject a = TestUtils.readJson(PATH_PREFIX + "valid_asset.json");
        JsonObject b = ambrosus.fromJson(PATH_PREFIX + "valid_asset.json", JsonObject.class);

        assertEquals(a, b);
    }


    @Test
    public void fromJsonHandlesInvalidFiles() {
        JsonObject a = ambrosus.fromJson(PATH_PREFIX + "nonexistingfile.invalidextension", JsonObject.class);
        assertNull(a);
    }


    @Test(expected = IllegalArgumentException.class)
    public void invalidAdapterThrowsException() {
        ambrosus = new AmbrosusSDK(config);
        ambrosus.registerEventDataType("dummy", EventData.class, new InvalidAdapter());
    }


    @Test(expected = IllegalStateException.class)
    public void registerTypeAfterInitThrowsException() {
        ambrosus.registerEventDataType("dummy", EventData.class, new InvalidAdapter());
    }


    private void setAsyncCallBackResult(Object o) {
        asyncCallBackResult = o;
    }


    private static class InvalidAdapter {

    }
}
