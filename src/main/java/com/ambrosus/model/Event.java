/*
Copyright: Ambrosus Technologies GmbH
Email: tech@ambrosus.com

This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.

This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
*/

package com.ambrosus.model;

import com.ambrosus.utils.CryptoUtils;
import com.ambrosus.utils.JsonUtils;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.web3j.crypto.ECKeyPair;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Holds event information for AMBNet REST API responses deserialized by Gson.
 */
public final class Event extends AmbrosusType implements Serializable, Comparable<Event> {

    private final String eventId;
    private final Integer accessLevel;
    private final String assetId;
    private final String dataHash;
    private final String signature;
    private final List<EventData> eventDataList;
    private final Map<Class<? extends EventData>, List<EventData>> dataMap;


    private Event(Builder builder) {

        super(builder);
        this.eventId = builder.eventId;
        this.accessLevel = builder.accessLevel;
        this.assetId = builder.assetId;
        this.dataHash = builder.dataHash;
        this.signature = builder.signature;

        for (int i = 0; i < builder.eventDataList.size(); i++)
            builder.eventDataList.get(i).setParentEvent(this);

        this.eventDataList = Collections.unmodifiableList(new ArrayList<>(builder.eventDataList));
        this.dataMap = buildMap();
    }


    public String getEventId() {

        return eventId;
    }


    public Integer getAccessLevel() {

        return accessLevel;
    }


    public String getAssetId() {

        return assetId;
    }


    public String getDataHash() {

        return dataHash;
    }


    public String getSignature() {
        return signature;
    }


    public List<EventData> getEventDataList() {

        return eventDataList;
    }


    public <T extends EventData> boolean hasDataOfType(Class<T> clazz) {
        return dataMap.containsKey(clazz);
    }


    /**
     * Retrieves every event data object linked to this event whose type after deserialization corresponds to the
     * type given in parameter.
     *
     * @param clazz The class object derived from {@link EventData}
     * @param <T>   The class type derived from {@link EventData}
     * @return A list of the corresponding subtype of
     * {@link EventData} containing the event data objects or empty if there are no elements are of such type
     */
    public <T extends EventData> List<T> eventDataWithType(Class<T> clazz) {

        List<T> castedList = new ArrayList<>();

        List<EventData> eventData = dataMap.get(clazz);

        if (eventData != null) {
            for (EventData data : eventData) {
                assert data.getClass().equals(clazz) : "Stored event data of incorrect type, this should not happen";
                castedList.add(clazz.cast(data));
            }
        }
        return castedList;
    }


    public <T extends EventData> T firstOf(Class<T> clazz) {
        return clazz.cast(dataMap.get(clazz).get(0));
    }


    private Map<Class<? extends EventData>, List<EventData>> buildMap() {
        Map<Class<? extends EventData>, List<EventData>> map = new HashMap<>();

        for (EventData dataSection : eventDataList) {

            if (!map.containsKey(dataSection.getClass()))
                map.put(dataSection.getClass(), new ArrayList<>());

            map.get(dataSection.getClass()).add(dataSection);

        }

        return Collections.unmodifiableMap(map);
    }


    @Override
    public int compareTo(Event that) {
        return this.timestamp < that.timestamp ? 1 : (this.timestamp.equals(that.timestamp)) ? 0 : -1;
    }


    /**
     * Builder class for Event objects
     */
    public static class Builder extends AmbrosusType.Builder {

        private String eventId;
        private Integer accessLevel;
        private String assetId;
        private List<EventData> eventDataList;
        private String dataHash;
        private String signature;


        public Builder() {

            eventDataList = new ArrayList<>();
        }


        public static Builder fromExistingEvent(Event event) {

            Builder builder = new Builder();

            builder.eventId = event.eventId;
            builder.accessLevel = event.accessLevel;
            builder.assetId = event.assetId;
            builder.eventDataList = event.eventDataList;
            builder.dataHash = event.dataHash;
            builder.signature = event.signature;

            return builder;
        }


        public String getEventId() {

            return eventId;
        }


        public Builder setEventId(String eventId) {

            this.eventId = eventId;
            return this;
        }


        public Integer getAccessLevel() {

            return accessLevel;
        }


        public Builder setAccessLevel(Integer accessLevel) {

            this.accessLevel = accessLevel;
            return this;
        }


        public String getAssetId() {

            return assetId;
        }


        public Builder setAssetId(String assetId) {

            this.assetId = assetId;
            return this;
        }


        public List<EventData> getEventDataList() {

            return eventDataList;
        }


        public Builder addEventData(EventData eventData) {

            this.eventDataList.add(eventData);
            return this;
        }


        public Builder addAllEventData(List<EventData> eventData) {

            this.eventDataList.addAll(eventData);
            return this;
        }


        public String getDataHash() {

            return dataHash;
        }


        public Builder setDataHash(String dataHash) {

            this.dataHash = dataHash;
            return this;
        }


        public String getSignature() {
            return signature;
        }


        public Builder setSignature(String signature) {

            this.signature = signature;
            return this;
        }


        public Event build() {

            return new Event(this);
        }
    }

    /**
     * Adapter class for serializing and deserializing Event instances into and from {@link JsonElement}.
     */
    public static class Adapter implements JsonSerializer<Event>, JsonDeserializer<Event> {

        // Required to sign the idData field upon serialization
        private final ECKeyPair signatureKey;


        public Adapter(ECKeyPair signatureKey) {

            this.signatureKey = signatureKey;
        }


        @Override
        public JsonElement serialize(Event src, Type typeOfSrc, JsonSerializationContext context) {

            // Build nested structure
            JsonObject body = new JsonObject();
            JsonObject content = new JsonObject();
            JsonObject idData = new JsonObject();

            body.add(JsonProperties.CONTENT, content);
            if (src.getEventId() != null)
                body.addProperty(JsonProperties.EVENT_ID, src.getEventId());


            content.add(JsonProperties.ID_DATA, idData);

            // Populate fields in alphabetical order
            idData.addProperty(JsonProperties.ACCESS_LEVEL, src.getAccessLevel());
            idData.addProperty(JsonProperties.ASSET_ID, src.getAssetId());
            idData.addProperty(JsonProperties.CREATED_BY, src.getCreatedBy());

            JsonArray data = context
                    .serialize(src.getEventDataList(), new TypeToken<List<EventData>>() {
                    }.getType())
                    .getAsJsonArray();

            if (src.getDataHash() == null) {

                JsonArray sortedData = JsonUtils.arraySort(data);
                String dataHash = CryptoUtils.computeHashString(sortedData.toString());

                idData.addProperty(JsonProperties.DATA_HASH, dataHash);
            } else {
                idData.addProperty(JsonProperties.DATA_HASH, src.getDataHash());
            }

            idData.addProperty(JsonProperties.TIMESTAMP, src.getTimestamp());
            content.add(JsonProperties.DATA, data);

            if (src.getSignature() == null) {
                String signature = CryptoUtils.computeSignature(idData.toString(), signatureKey);
                content.addProperty(JsonProperties.SIGNATURE, signature);
            } else {
                content.addProperty(JsonProperties.SIGNATURE, src.getSignature());
            }


            if (src.getMetaData() != null)
                body.add(JsonProperties.META_DATA, context.serialize(src.getMetaData(), MetaData.class));

            return body;
        }


        public Event deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws
                JsonParseException {

            try {
                JsonObject jObj = json.getAsJsonObject();
                JsonObject content = jObj.getAsJsonObject(JsonProperties.CONTENT);
                JsonObject idData = content.getAsJsonObject(JsonProperties.ID_DATA);

                Event.Builder eventBuilder = new Event.Builder();
                eventBuilder
                        .setAccessLevel(idData.get(JsonProperties.ACCESS_LEVEL).getAsInt())
                        .setAssetId(idData.get(JsonProperties.ASSET_ID).getAsString())
                        .addAllEventData(context.deserialize(content.getAsJsonArray(JsonProperties.DATA),
                                new TypeToken<List<EventData>>() {
                                }.getType()))
                        .setCreatedBy(idData.get(JsonProperties.CREATED_BY).getAsString())
                        .setTimestamp(context.deserialize(idData.get(JsonProperties.TIMESTAMP), long.class))
                        .setMetaData(context.deserialize(jObj.get(JsonProperties.META_DATA), MetaData.class));

                if (jObj.has(JsonProperties.EVENT_ID))
                    eventBuilder.setEventId(jObj.get(JsonProperties.EVENT_ID).getAsString());

                if (idData.has(JsonProperties.DATA_HASH))
                    eventBuilder.setDataHash(idData.get(JsonProperties.DATA_HASH).getAsString());

                if (content.has(JsonProperties.SIGNATURE))
                    eventBuilder.setSignature(content.get(JsonProperties.SIGNATURE).getAsString());

                return eventBuilder.build();

            } catch (IllegalStateException | NullPointerException | IllegalArgumentException e) {
                e.printStackTrace();
            }

            return null;
        }
    }
}
