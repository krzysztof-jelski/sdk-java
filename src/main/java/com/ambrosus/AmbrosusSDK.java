/*
Copyright: Ambrosus Technologies GmbH
Email: tech@ambrosus.com

This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.

This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
*/

package com.ambrosus;

import com.ambrosus.commons.Location;
import com.ambrosus.commons.Message;
import com.ambrosus.commons.RawJson;
import com.ambrosus.commons.Transport;
import com.ambrosus.model.*;
import com.ambrosus.network.*;
import com.ambrosus.utils.*;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.utils.Numeric;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ambrosus.network.AmbrosusService.SECRET_PREAMBLE;

/**
 * Core class from the Ambrosus SDK which enables interactions with the Ambrosus REST API.
 * The REST API expects and sends JSON objects. For convenience, the SDK uses Google's gson library to automatically
 * deserialize HTTP responses sent by the REST API.
 * <p>
 * To leverage gson serialization/deserialization features, user-defined classes can be registered by calling
 * {@link #registerEventDataType(String, Class, Object)} )}. Doing so will allow the gson library to deserialize
 * event data objects
 * into plain Java objects. If you do not provide an adapter, event data extracted from the HTTP response will be
 * stored as a {@link JsonObject}. See <a href="https://github.com/google/gson">github.com/google/gson</a>
 * for additional information on how to define custom adapters for HTTP responses.
 * <p>
 * Request calls made from the SDK are non-blocking; they execute concurrently and you should therefore put code
 * handling the results inside a callback. The SDK uses the retrofit library to format and send the requests to the
 * REST API. See for <a href="https://square.github.io/retrofit/">square.github.io/retrofit/</a> additional information.
 */
public class AmbrosusSDK {

    private final static int MAX_SEQUENCE_NUMBER = 1_000_000;
    private final Map<String, Type> eventTypes;
    private final Map<Type, Object> customAdapters;
    private final Config config;
    private final ECKeyPair keyPair;
    private final String address;
    private boolean initialized;
    private Gson gson;
    private AmbrosusService ambrosusService;
    private int sequenceNumber;


    /**
     * Constructs a SDK instance
     *
     * @param config The config object holding connexion parameters and credentials for the SDK
     */
    public AmbrosusSDK(Config config) {
        this.initialized = false;
        this.config = config;
        this.keyPair = ECKeyPair.create(Numeric.toBigInt(config.privateKey));
        this.address = Keys.toChecksumAddress(Keys.getAddress(keyPair));
        this.sequenceNumber = 0;
        this.gson = new Gson();
        eventTypes = new HashMap<>();
        customAdapters = new HashMap<>();
    }


    public <T> T fromJson(String filePath, Class<T> clazz) {

        T t = null;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath),
                StandardCharsets.UTF_8))) {

            t = gson.fromJson(br, clazz);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return t;
    }

    public String toJson(Object src) {
        return gson.toJson(src);
    }


    private void createGson() {

        registerTypeIfNotOverriden(Location.API_DATA_TYPE, Location.class, new Location.Adapter());
        registerTypeIfNotOverriden(Transport.API_DATA_TYPE, Transport.class);
        registerTypeIfNotOverriden(Message.API_DATA_TYPE, Message.class);

        GsonBuilder gsonBuilder = new GsonBuilder();

        for (Map.Entry<Type, Object> entry : customAdapters.entrySet()) {
            gsonBuilder.registerTypeAdapter(entry.getKey(), entry.getValue());
        }

        // Register model adapters
        gsonBuilder.registerTypeAdapter(RawJson.class, new RawJson.Adapter());
        gsonBuilder.registerTypeAdapter(Asset.class, new Asset.Adapter(keyPair));
        gsonBuilder.registerTypeAdapter(Event.class, new Event.Adapter(keyPair));
        gsonBuilder.registerTypeAdapter(Account.class, new Account.Adapter());
        gsonBuilder.registerTypeAdapter(GroupToken.class, new GroupToken.Adapter(keyPair, getAddress()));
        gsonBuilder.registerTypeAdapter(new TypeToken<List<EventData>>() {
        }.getType(), new EventData.Adapter(eventTypes));

        gsonBuilder.setExclusionStrategies(new AmbTypeExclusionStrategy());

        this.gson = gsonBuilder.create();
    }


    /**
     * Initializes the web service for sending and receiving HTTP messages. The custom type adapter must be set
     * before calling this function.
     */
    public void init() {
        if (initialized) {
            throw new IllegalStateException("This SDK instance has already been initialized.");
        } else {
            initialized = true;
        }

        createGson();

        // Instantiate Http service
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(config.baseUrl)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        this.ambrosusService = retrofit.create(AmbrosusService.class);
    }


    /**
     * @return The hexadecimal string address derived from the private key, prefixed by 0x
     */
    public String getAddress() {

        return address;
    }


    /**
     * Registers an event data type to be used by the Json serializer/deserializer. Whenever an event data object is
     * encoutered with the corresponding type name, the deserializer will try to use the provided class to store the
     * JSON properties.
     *
     * @param typename  The Ambrosus type name of the event data object. For example, {@code ambrosus.asset.info} is a
     *                  valid event data type name.
     * @param typeClass The class to be used to host the JSON properties extracted from the object.
     */
    public void registerEventDataType(String typename, Class<? extends EventData> typeClass) {
        eventTypes.put(typename, typeClass);
    }


    /**
     * Registers an event data type to be used by the Json serializer/deserializer. Whenever an event data object is
     * encoutered with the corresponding type name, the deserializer will try to use the provided class to store the
     * JSON properties. When the JSON object is complex or when the result of automatic deserialization/serialization
     * is unsatisfactory, use this method to provide a type adapter which can translate back and forth between plain
     * Java and JSON representations.
     *
     * @param typename  The Ambrosus type name of the event data object. For example, {@code ambrosus.asset.info} is a
     *                  valid event data type name.
     * @param typeClass The class to be used to host the JSON properties extracted from the object.
     */
    public void registerEventDataType(String typename, Class<? extends EventData> typeClass, Object adapter) {

        throwIfInitialized();

        if (!((adapter instanceof JsonDeserializer<?>) || (adapter instanceof JsonSerializer<?>))) {
            throw new IllegalArgumentException("Invalid adapter object, should implement JsonSerializer or " +
                    "JsonDeserializer.");
        }

        registerEventDataType(typename, typeClass);
        customAdapters.put(typeClass, adapter);
    }


    /**
     * Requests from the API the asset whose ID matches the one given in parameter.
     *
     * @param assetId       The identifier of the asset to retrieve
     * @param assetConsumer A consumer able to handle the asset if the request is successful or null otherwise
     * @throws IllegalStateException if the SDK was not initialized before this call
     */
    public void getAsset(String assetId, Consumer<ResponseWrapper<Asset>> assetConsumer, BiConsumer<Call<Asset>,
            Throwable> errorConsumer) {

        throwIfNotInitialized();
        ambrosusService.getAsset(assetId).enqueue(new NetworkUtils.DefaultCallback<>(assetConsumer, errorConsumer));
    }


    /**
     * Requests from the API the asset whose ID matches the one given in parameter. Additionally retrieve all event
     * associated to this asset before delivering the asset to the consumer callback.
     *
     * @param assetId         The identifier of the asset to retrieve
     * @param successConsumer A consumer able to handle the asset if the request is successful
     * @throws IllegalStateException if the SDK was not initialized before this call
     */
    public void getAssetWithEvents(String assetId, Consumer<Asset> successConsumer, BiConsumer<Call<?>, Throwable>
            errorConsumer) {

        throwIfNotInitialized();

        ambrosusService
                .getAsset(assetId)
                .enqueue(new NetworkUtils.RetrieveEventsForAsset(this, successConsumer,
                errorConsumer));
    }


    /**
     * Requests the Ambrosus API to create an empty asset.
     *
     * @param assetBuilder An asset builder. The SDK will complete the builder if needed with the creator's address,
     *                     the timestamp and the sequence number.
     * @param assetConsumer A consumer able to handle the newly created asset or null if an error
     *                      occurred during the operation
     * @param errorConsumer A consumer able to handle two arguments, the first one being the retrofit call made by
     *                      the SDK to the API and the second the throwable that was raised during the execution of
     *                      this call.
     * @throws IllegalStateException if the SDK was not initialized before this call
     */
    public void createAsset(Asset.Builder assetBuilder, Consumer<ResponseWrapper<Asset>> assetConsumer,
                            BiConsumer<Call<Asset>, Throwable> errorConsumer) {

        throwIfNotInitialized();

        if (assetBuilder.getCreatedBy() == null)
            assetBuilder.setCreatedBy(address);

        if (assetBuilder.getTimestamp() == null)
            assetBuilder.setTimestamp(getUnixTimeStamp());

        if (assetBuilder.getSequenceNumber() == null)
            assetBuilder.setSequenceNumber(getSequenceNumber());

        ambrosusService
                .createAsset(assetBuilder.build())
                .enqueue(new NetworkUtils.DefaultCallback<>(assetConsumer, errorConsumer));

    }


    /**
     * Requests the Ambrosus API to retrieve all the events associated to an asset.
     *
     * @param assetId                The identifier of the asset from which to retrieve the events
     * @param ambQueryResultConsumer A consumer able to handle the retrieved events if the request is successful or
     *                               null otherwise
     * @param errorConsumer          A consumer able to handle two arguments, the first one
     *                               being the retrofit call made by
     *                               the SDK to the API and the second the throwable that was raised during the
     *                               execution of
     *                               this call.
     * @throws IllegalStateException if the SDK was not initialized before this call
     */
    public void getEvents(String assetId, Consumer<ResponseWrapper<EventQueryResponse>> ambQueryResultConsumer,
                          BiConsumer<Call<EventQueryResponse>, Throwable> errorConsumer) {

        throwIfNotInitialized();

        ambrosusService
                .getEvents(assetId)
                .enqueue(new NetworkUtils.DefaultCallback<>(ambQueryResultConsumer, errorConsumer));

    }


    /**
     * Requests the Ambrosus API to store a new event
     *
     * @param eventBuilder An event builder with the asset ID already set. The SDK will complete id needed the
     *                     creator's address, the timestamp and the access level (defaults to 0).
     * @param eventConsumer A consumer able to handle the newly created event
     * @param errorConsumer A consumer able to handle two arguments, the first one being the
     *                      retrofit call made by
     *                      the SDK to the API and the second the throwable that was raised during the execution of
     *                      this call.
     * @throws IllegalStateException if the SDK was not initialized before this call
     * @throws IllegalArgumentException if the asset ID was not set in the builder
     */
    public void createEvent(Event.Builder eventBuilder, Consumer<ResponseWrapper<Event>> eventConsumer,
                            BiConsumer<Call<Event>, Throwable> errorConsumer) {

        throwIfNotInitialized();

        if (eventBuilder.getAssetId() == null)
            throw new IllegalArgumentException("Missing asset ID in event builder.");

        if (eventBuilder.getCreatedBy() == null)
            eventBuilder.setCreatedBy(address);

        if (eventBuilder.getTimestamp() == null)
            eventBuilder.setTimestamp(getUnixTimeStamp());

        if (eventBuilder.getAccessLevel() == null)
            eventBuilder.setAccessLevel(0);

        Event event = eventBuilder.build();

        ambrosusService
                .createEvent(event.getAssetId(), event)
                .enqueue(new NetworkUtils.DefaultCallback<>(eventConsumer, errorConsumer));
    }


    /**
     * Retrieves an account from the API
     *
     * @param token           An authorization token for the request
     * @param address         The address of the account to retrieve
     * @param accountConsumer A consumer callback for an account
     * @param errorConsumer   A consumer able to handle two arguments, the first one being the
     *                        retrofit call made by
     *                        the SDK to the API and the second the throwable that was raised during the execution of
     *                        this call.
     */
    public void getAccount(String token,
                           String address,
                           Consumer<ResponseWrapper<Account>> accountConsumer,
                           BiConsumer<Call<Account>, Throwable> errorConsumer) {

        throwIfNotInitialized();

        //TODO: perform validation on input string
        ambrosusService
                .getAccount(AmbrosusService.TOKEN_PREAMBLE + token, address)
                .enqueue(new NetworkUtils.DefaultCallback<>(accountConsumer, errorConsumer));
    }


    /**
     * Creates an account using the API
     *
     * @param token           An authorization token for the request
     * @param accountBuilder  The account object to be serialized and sent to the API
     * @param accountConsumer A consumer callback for the newly created account
     * @param errorConsumer   A consumer able to handle two arguments, the first one being the
     *                        retrofit call made by
     *                        the SDK to the API and the second the throwable that was raised during the execution of
     *                        this call.
     * @throws IllegalStateException if the SDK was not initialized before this call
     */
    public void createAccount(String token,
                              Account.Builder accountBuilder,
                              Consumer<ResponseWrapper<Account>> accountConsumer,
                              BiConsumer<Call<Account>, Throwable> errorConsumer) {

        throwIfNotInitialized();

        if (accountBuilder.getAddress() == null)
            throw new IllegalArgumentException("Missing address in account builder.");

        if (accountBuilder.getAccessLevel() == null)
            accountBuilder.setAccessLevel(0);

        ambrosusService
                .createAccount(AmbrosusService.TOKEN_PREAMBLE + token, accountBuilder.build())
                .enqueue(new NetworkUtils.DefaultCallback<>(accountConsumer, errorConsumer));
    }


    /**
     * Creates a token using the account provided to the SDK configuration object.
     *
     * @param validUntil    Validity of the token, as a Unix timestamp with a resolution of one second.
     * @param tokenConsumer A consumer callback for the newly created token.
     * @param errorConsumer A consumer able to handle two arguments, the first one being the
     *                      retrofit call made by
     *                      the SDK to the API and the second the throwable that was raised during the execution of
     *                      this call.
     */
    public void createToken(Long validUntil, Consumer<ResponseWrapper<Token>> tokenConsumer, BiConsumer<Call<Token>,
            Throwable> errorConsumer) {

        throwIfNotInitialized();

        JsonObject jObj = new JsonObject();
        jObj.addProperty("validUntil", validUntil);

        ambrosusService
                .createToken(SECRET_PREAMBLE + config.privateKey, jObj)
                .enqueue(new NetworkUtils.DefaultCallback<>(tokenConsumer, errorConsumer));
    }


    /**
     * Queries the API for assets
     *
     * @param params             A query parameters map. Accepted parameters are:
     *                           - perPage: number of assets to return per page
     *                           - page: page number
     *                           - createdBy: address of the account that created the targeted events
     *                           - fromTimestamp: earliest timestamp for the asset
     *                           - toTimestamp: latest timestamp for the asset
     *                           - identifier: This syntax allows to query for assets that have an associated event
     *                           containing data type ambrosus.event.identifier (see Events Data field section) with
     *                           same identifier
     *                           of same type. It is possible to find identifiers matching a pattern with help of the
     *                           pattern decorator. You can use like this: identifier[vin]=pattern(3FRNF65N*). It
     *                           supports 2 kinds of special characters: * matches any string, including the null
     *                           string. ? matches any single character Note that patterns cannot have a special
     *                           character as the first symbol for performance reasons. It is  not possible to escape
     *                           special characters.
     *                           <p>
     *                           Example: identifier[{identifierType}]={identifierValue}
     * @param assetQueryConsumer A consumer callback for assets
     * @param errorConsumer      A consumer able to handle two arguments, the first one being
     *                           the retrofit call made by
     *                           the SDK to the API and the second the throwable that was raised during the execution of
     *                           this call.
     * @throws IllegalStateException if the SDK was not initialized before this call
     */
    public void findAssets(Map<String, String> params,
                           Consumer<ResponseWrapper<AssetQueryResponse>> assetQueryConsumer,
                           BiConsumer<Call<AssetQueryResponse>, Throwable> errorConsumer) {

        throwIfNotInitialized();

        ambrosusService
                .findAssets(params)
                .enqueue(new NetworkUtils.DefaultCallback<>(assetQueryConsumer, errorConsumer));
    }


    /**
     * Queries the API for events
     *
     * @param params             A query parameters map. Accepted parameters are:
     *                           - assetId: ID of the asset the events are targeting
     *                           - fromTimestamp: earlisest timestamp for the events
     *                           - toTimestamp: latest timestamp for the events
     *                           - perPage: number of events to return per page
     *                           - page: page number
     *                           - createdBy: address of the account that created the targeted events
     *                           - data: This syntax allows to query for events by any fields in data array.
     *                           QueryResponse for nested fields is possible, e.g. data[acceleration.x]=10. By
     *                           default the
     *                           type of the value is string. To provide a number, use data[acceleration.x]=number
     *                           (10). To provide geo coordinates use data[geoJson]=geo(longitude, latitude, radius).
     *                           It is possible to match data values by the pattern using the pattern decorator like
     *                           so data[type]=pattern(ambrosus.event.*). It support 2 kinds of special characters: *
     *                           matches any string, including the null string. ? matches any single character Note
     *                           that patterns cannot have a special character as the first  symbol for performance
     *                           reasons. It is not possible to escape special characters.
     *                           <p>
     *                           Example: data[type]=ambrosus.event.custom
     * @param eventQueryConsumer A consumer callback for events
     * @param errorConsumer      A consumer able to handle two arguments, the first one being
     *                           the retrofit call made by
     *                           the SDK to the API and the second the throwable that was raised during the execution of
     *                           this call.
     * @throws IllegalStateException if the SDK was not initialized before this call
     */
    public void findEvents(Map<String, String> params,
                           Consumer<ResponseWrapper<EventQueryResponse>> eventQueryConsumer,
                           BiConsumer<Call<EventQueryResponse>, Throwable> errorConsumer) {

        throwIfNotInitialized();

        ambrosusService
                .findEvents(params)
                .enqueue(new NetworkUtils.DefaultCallback<>(eventQueryConsumer, errorConsumer));
    }


    /**
     * Queries the API for accounts.
     *
     * @param token                An authorization token to be transmitted to the API
     * @param params               A query parameters map. Accepted parameters are:
     *                             - accessLevel: Minimum access level of accounts to look for
     *                             - perPage: number of assets to return per page
     *                             - page: page number
     * @param accountQueryConsumer A consumer callback for accounts
     * @param errorConsumer        A consumer able to handle two arguments, the first one being
     *                             the retrofit call made by
     *                             the SDK to the API and the second the throwable that was raised during the
     *                             execution of
     *                             this call.
     * @throws IllegalStateException if the SDK was not initialized before this call
     */
    public void findAccounts(String token,
                             Map<String, String> params,
                             Consumer<ResponseWrapper<AccountQueryResponse>> accountQueryConsumer,
                             BiConsumer<Call<AccountQueryResponse>, Throwable> errorConsumer) {

        throwIfNotInitialized();

        ambrosusService
                .findAccounts(AmbrosusService.TOKEN_PREAMBLE + token, params)
                .enqueue(new NetworkUtils.DefaultCallback<>(accountQueryConsumer, errorConsumer));
    }


    /**
     * Recomputes the data hash and compare it against the one stored in the event idData property
     *
     * @param event The event for which to verify the data hash
     * @return True if the hashes match, false otherwise
     * @throws IllegalStateException if the SDK was not initialized before this call
     */
    public boolean verifyDatahash(Event event) {

        JsonElement jsonRepr = gson.toJsonTree(event);
        JsonObject jsonObj = jsonRepr.getAsJsonObject();
        JsonObject content = jsonObj.getAsJsonObject(JsonProperties.CONTENT);
        JsonArray data = content.getAsJsonArray(JsonProperties.DATA);
        JsonObject idData = content.getAsJsonObject(JsonProperties.ID_DATA);
        String candidatehash = idData.get(JsonProperties.DATA_HASH).getAsString();

        return candidatehash.equals(
                CryptoUtils.computeHashString(JsonUtils.arraySort(data).toString())
        );
    }


    /**
     * Recovers the public address from the stored signature and compares it against the event's createdBy property.
     *
     * @param event The event for which to verify the signature
     * @return True if the signatures match, false otherwise
     * @throws IllegalStateException if the SDK was not initialized before this call
     */
    public boolean verifySignature(Event event) {

        return verifySignature(gson.toJsonTree(event));
    }


    /**
     * Recovers the public address from the stored signature and compares it against the asset's createdBy property.
     *
     * @param asset The asset for which to verify the signature
     * @return True if the signatures match, false otherwise
     * @throws IllegalStateException if the SDK was not initialized before this call
     */
    public boolean verifySignature(Asset asset) {

        return verifySignature(gson.toJsonTree(asset));
    }


    private void throwIfNotInitialized() {
        if (!initialized) {
            throw new IllegalStateException("The SDK was not initialized. Call init() to initialize the SDK.");
        }
    }


    private void throwIfInitialized() {
        if (initialized) {
            throw new IllegalStateException("The SDK was already initialized. Create a new SDK before performing this" +
                    " action.");
        }
    }


    private boolean verifySignature(JsonElement jsonRepr) {

        JsonObject jsonObj = jsonRepr.getAsJsonObject();
        JsonObject content = jsonObj.getAsJsonObject(JsonProperties.CONTENT);
        String signature = content.get(JsonProperties.SIGNATURE).getAsString();
        JsonObject idData = content.getAsJsonObject(JsonProperties.ID_DATA);
        String address = idData.get(JsonProperties.CREATED_BY).getAsString();

        try {
            return CryptoUtils.signatureMatches(idData.toString(), address, signature);
        } catch (RuntimeException e) {
            // Depending on the signature hex value, decoding could fail
            e.printStackTrace();
            return false;
        }
    }


    private void registerTypeIfNotOverriden(String typename, Type typeClass, Object adapter) {
        if (!eventTypes.containsKey(typename)) {
            eventTypes.put(typename, typeClass);
            customAdapters.put(typeClass, adapter);
        }
    }


    private void registerTypeIfNotOverriden(String typename, Type typeClass) {
        if (!eventTypes.containsKey(typename)) {
            eventTypes.put(typename, typeClass);
        }
    }


    private long getUnixTimeStamp() {

        return System.currentTimeMillis() / 1000L;
    }


    /**
     * Increment and return the asset creation sequence number. Make sure the sequence number remains within
     * reasonable bounds.
     *
     * @return The sequence number
     */
    private int getSequenceNumber() {

        sequenceNumber = (sequenceNumber + 1) % MAX_SEQUENCE_NUMBER;
        return sequenceNumber;
    }

    public String createGroupToken(long validUntil) {
        GroupToken groupToken = new GroupToken(validUntil);
        String serializedGroupTokenJson = gson.toJson(groupToken);
        return java.util.Base64.getEncoder().encodeToString(serializedGroupTokenJson.getBytes());
    }


    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD})
    public @interface HiddenFromJSONAdapter {
        // Field tag only annotation
    }

    public static class AmbTypeExclusionStrategy implements ExclusionStrategy {

        public AmbTypeExclusionStrategy() {
        }


        public boolean shouldSkipClass(Class<?> clazz) {
            return false;
        }


        public boolean shouldSkipField(FieldAttributes f) {
            return f.getAnnotation(HiddenFromJSONAdapter.class) != null;
        }
    }

    /**
     * Configuration holder for the SDK
     */
    public static final class Config {

        private final String baseUrl;
        private final String privateKey;


        /**
         * Constructs an instance of the configuration class
         *
         * @param restAPIBaseUrl The url of the REST API to which the requests of the SDK will be sent
         * @param privateKey     Private key (secret) of an account held by the user to digitally sign the data sent to
         *                       the REST API
         */
        public Config(final String restAPIBaseUrl, final String privateKey) {

            this.baseUrl = restAPIBaseUrl;
            this.privateKey = privateKey;
        }
    }
}
