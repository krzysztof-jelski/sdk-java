/*
Copyright: Ambrosus Technologies GmbH
Email: tech@ambrosus.com

This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.

This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
*/

package com.ambrosus.model;

import com.ambrosus.utils.CryptoUtils;
import com.google.gson.*;
import org.web3j.crypto.ECKeyPair;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Holds asset information for AMBNet REST API responses deserialized by Gson.
 */
public final class Asset extends AmbrosusType implements Serializable {

    private final String assetId;
    private final Integer sequenceNumber;
    private final String signature;
    private final List<Event> eventsList;
    private final Map<Class<? extends EventData>, List<EventData>> dataMap;


    private Asset(Builder builder) {
        super(builder);

        this.assetId = builder.assetId;
        this.sequenceNumber = builder.sequenceNumber;
        this.signature = builder.signature;

        ArrayList<Event> events = new ArrayList<>(builder.eventsList);
        Collections.sort(events);

        this.eventsList = Collections.unmodifiableList(events);
        this.dataMap = buildMap();
    }


    public String getAssetId() {

        return assetId;
    }


    public List<Event> getEventsList() {

        return eventsList;
    }


    public Integer getSequenceNumber() {

        return sequenceNumber;
    }


    public String getSignature() {
        return signature;
    }


    /**
     * Retrieves every event data object linked to this asset whose type after deserialization corresponds to the
     * type given in parameter.
     *
     * @param clazz The class object derived from {@link EventData}
     * @param <T>   The class type derived from {@link EventData}
     * @return A list of the corresponding subtype of
     * {@link EventData} containing the event data objects or empty if there are no elements are of such type
     */
    public <T extends EventData> List<T> sectionsOfType(Class<T> clazz) {

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


    public <T extends EventData> boolean hasEventDataOfType(Class<T> clazz) {
        return dataMap.containsKey(clazz);
    }


    public <T extends EventData> T firstOf(Class<T> clazz) {

        return sectionAtPos(clazz, 0);
    }


    public <T extends EventData> T lastOf(Class<T> clazz) {

        return sectionAtPos(clazz, -1);
    }


    /**
     * @param clazz The class parameter of event data
     * @param <T>   The type parameter of event data
     * @return A set of events containing the given event data type.
     */
    public <T extends EventData> Set<Event> eventsContainingType(Class<T> clazz) {
        List<T> eventDataList = sectionsOfType(clazz);
        Set<Event> events = new HashSet<>();

        for (EventData eventData : eventDataList)
            events.add(eventData.getParentEvent());

        return events;
    }


    private <T extends EventData> T sectionAtPos(Class<T> clazz, int i) {

        List<EventData> eventData = dataMap.get(clazz);

        // Access to elements by counting from the end
        int index = (i < 0) ? mod(i, eventData.size()) : i;

        return clazz.cast(eventData.get(index));
    }


    private int mod(int x, int y) {
        int result = x % y;
        return result < 0 ? result + y : result;
    }


    private Map<Class<? extends EventData>, List<EventData>> buildMap() {
        Map<Class<? extends EventData>, List<EventData>> map = new HashMap<>();

        for (Event event : eventsList) {
            for (EventData dataSection : event.getEventDataList()) {

                if (!map.containsKey(dataSection.getClass()))
                    map.put(dataSection.getClass(), new ArrayList<>());

                map.get(dataSection.getClass()).add(dataSection);
            }
        }

        return Collections.unmodifiableMap(map);
    }


    public static class Builder extends AmbrosusType.Builder {

        private String assetId;
        private Integer sequenceNumber;
        private String signature;
        private List<Event> eventsList;


        public Builder() {

            eventsList = new ArrayList<>();
        }


        public static Builder fromExistingAsset(Asset asset) {

            Builder builder = new Builder();

            builder.createdBy = asset.createdBy;
            builder.timestamp = asset.timestamp;
            builder.metaData = asset.metaData;
            builder.assetId = asset.assetId;
            builder.sequenceNumber = asset.sequenceNumber;
            builder.eventsList = new ArrayList<>(asset.eventsList);

            return builder;
        }


        public String getAssetId() {

            return assetId;
        }


        public Builder setAssetId(String assetId) {

            this.assetId = assetId;
            return this;
        }


        public Integer getSequenceNumber() {

            return sequenceNumber;
        }


        public Builder setSequenceNumber(Integer sequenceNumber) {

            this.sequenceNumber = sequenceNumber;
            return this;
        }


        public String getSignature() {
            return signature;
        }


        public Builder setSignature(String signature) {

            this.signature = signature;
            return this;
        }


        public Builder addEvent(Event event) {

            this.eventsList.add(event);
            return this;
        }


        public Builder addAllEvents(List<Event> eventsList) {

            this.eventsList.addAll(eventsList);
            return this;
        }


        public List<Event> getEventsList() {
            return this.eventsList;
        }


        public Asset build() {

            return new Asset(this);
        }
    }

    public static class Adapter implements JsonSerializer<Asset>, JsonDeserializer<Asset> {

        private final ECKeyPair signatureKey;


        public Adapter(ECKeyPair signatureKey) {

            this.signatureKey = signatureKey;
        }


        @Override
        public JsonElement serialize(Asset src, Type typeOfSrc, JsonSerializationContext context) {

            // Build nested structure
            JsonObject body = new JsonObject();
            JsonObject content = new JsonObject();
            JsonObject idData = new JsonObject();

            // Populate fields in alphabetical order
            if (src.getAssetId() != null) {
                body.addProperty(JsonProperties.ASSET_ID, src.getAssetId());
            }

            body.add(JsonProperties.CONTENT, content);

            if (src.getMetaData() != null) {
                body.add(JsonProperties.META_DATA, context.serialize(src.getMetaData(), MetaData.class));
            }

            content.add(JsonProperties.ID_DATA, idData);

            idData.addProperty(JsonProperties.CREATED_BY, src.getCreatedBy());
            idData.addProperty(JsonProperties.SEQUENCE_NUMBER, src.getSequenceNumber());
            idData.addProperty(JsonProperties.TIMESTAMP, src.getTimestamp());

            if (src.getSignature() == null) {
                String signature = CryptoUtils.computeSignature(idData.toString(), signatureKey);
                content.addProperty(JsonProperties.SIGNATURE, signature);
            } else {
                content.addProperty(JsonProperties.SIGNATURE, src.getSignature());
            }

            return body;
        }


        @Override
        public Asset deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws
                JsonParseException {

            try {
                JsonObject jObj = json.getAsJsonObject();
                JsonObject content = jObj.getAsJsonObject(JsonProperties.CONTENT);
                JsonObject idData = content.getAsJsonObject(JsonProperties.ID_DATA);

                Asset.Builder assetBuilder = new Asset.Builder();

                assetBuilder
                        .setAssetId(jObj.get(JsonProperties.ASSET_ID).getAsString())
                        .setSequenceNumber(idData.get(JsonProperties.SEQUENCE_NUMBER).getAsInt())
                        .setSignature(content.get(JsonProperties.SIGNATURE).getAsString())
                        .setCreatedBy(idData.get(JsonProperties.CREATED_BY).getAsString())
                        .setTimestamp(context.deserialize(idData.get(JsonProperties.TIMESTAMP), Long.class))
                        .setMetaData(context.deserialize(jObj.get(JsonProperties.META_DATA), MetaData.class));

                return assetBuilder.build();

            } catch (IllegalStateException | NullPointerException | IllegalArgumentException e) {
                e.printStackTrace();
            }

            return null;
        }
    }
}



