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
import java.util.Objects;

/**
 * Class for hosting any information contained in an event data section. This class is used whenever the event data
 * type encountered was unknown or irrelevant for the program.
 */

public class RawJson extends EventData {

    private final JsonObject jsonObject;


    public RawJson(JsonObject jsonObject) {
        super(null);
        this.jsonObject = jsonObject.deepCopy();
    }


    public JsonObject json() {

        return jsonObject;
    }


    @Override
    public String toString() {

        return jsonObject.toString();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RawJson rawJson = (RawJson) o;
        return Objects.equals(jsonObject, rawJson.jsonObject);
    }


    @Override
    public int hashCode() {
        return Objects.hash(jsonObject);
    }


    public final static class Adapter implements JsonDeserializer<RawJson>, JsonSerializer<RawJson> {

        @Override
        public RawJson deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws
                JsonParseException {

            return json.isJsonObject() ? new RawJson(json.getAsJsonObject()) : null;
        }


        @Override
        public JsonElement serialize(RawJson src, Type typeOfSrc, JsonSerializationContext context) {

            return src.json();
        }
    }
}
