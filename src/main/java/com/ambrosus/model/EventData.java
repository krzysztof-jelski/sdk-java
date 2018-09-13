/*
Copyright: Ambrosus Technologies GmbH
Email: tech@ambrosus.com

This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.

This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
*/

package com.ambrosus.model;

import com.ambrosus.AmbrosusSDK.HiddenFromJSONAdapter;
import com.ambrosus.commons.RawJson;
import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.*;

/**
 * Holds event data for AMBNet REST API responses deserialized by Gson.
 */
public abstract class EventData {

    private final String type;

    @HiddenFromJSONAdapter
    private Event parentEvent;


    public EventData(String type) {
        this.type = type;
    }


    public String getType() {
        return type;
    }


    public Event getParentEvent() {
        return parentEvent;
    }


    public void setParentEvent(Event _parentEvent) {
        this.parentEvent = _parentEvent;
    }


    public static class Adapter implements JsonSerializer<List<EventData>>, JsonDeserializer<List<EventData>> {

        private final static String TYPE_STR = "type";
        private final Map<String, Type> userTypes;


        public Adapter(Map<String, Type> userTypes) {

            this.userTypes = Collections.unmodifiableMap(new HashMap<>(userTypes));
        }


        @Override
        public List<EventData> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {

            if (!json.isJsonArray())
                return Collections.emptyList();

            JsonArray dataArray = json.getAsJsonArray();
            List<EventData> dataList = new ArrayList<>();


            for (JsonElement jsonElement : dataArray) {
                if (jsonElement.isJsonObject()) {
                    JsonObject jsonObject = jsonElement.getAsJsonObject();
                    if (jsonObject.has(TYPE_STR) && jsonObject.get(TYPE_STR).isJsonPrimitive()) {
                        String typeStr = jsonObject.get(TYPE_STR).getAsString();
                        if (userTypes.containsKey(typeStr)) {
                            Type type = userTypes.get(typeStr);
                            EventData deserialized = context.deserialize(jsonElement, type);
                            if (deserialized != null) {
                                dataList.add(deserialized);
                            } else {
                                System.err.println("Could not deserialize " + jsonElement.toString());
                            }
                        } else {
                            // User did not provide an adapter for this event type, return a wrapper for the JSON
                            // element
                            dataList.add(context.deserialize(jsonElement, RawJson.class));
                        }
                    } else {
                        // Event data element had no type
                        System.err.println("Unable to find type for event data element.");
                    }
                } else {
                    // Event array contains something other that strict json object, pass
                    System.err.println("Ignoring event data element that is not a json object");
                }
            }

            return Collections.unmodifiableList(dataList);
        }


        @Override
        public JsonElement serialize(List<EventData> src, Type typeOfSrc, JsonSerializationContext context) {

            JsonArray dataArray = new JsonArray();

            for (EventData eventData : src) {
                dataArray.add(context.serialize(eventData, eventData.getClass()));
            }

            return dataArray;

        }
    }
}
