/*
Copyright: Ambrosus Technologies GmbH
Email: tech@ambrosus.com

This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.

This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
*/

package com.ambrosus.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Pattern;

/**
 * Utility class for Json-related operations.
 */
public abstract class JsonUtils {


    /**
     * Sorts the {@link JsonObject} properties by alphabetical order of the keys. The passed
     * object is not modified. Inner {@link JsonObject} are sorted recursively. {@link JsonArray} are not sorted but
     * this function is called recursively on their contents.
     *
     * @param json The JsonObject to be sorted
     * @return A new JsonObject with identical properties but sorted
     */
    public static JsonObject recursiveSort(final JsonObject json) {

        JsonObject sortedJsonRepr = new JsonObject();
        ArrayList<String> keys = new ArrayList<>(json.keySet());
        Collections.sort(keys);

        for (String key : keys) {

            JsonElement val = json.get(key);

            if (val.isJsonObject()) {

                sortedJsonRepr.add(key, recursiveSort((JsonObject) val));

            } else if (val.isJsonArray()) {

                sortedJsonRepr.add(key, arraySort((JsonArray) val));

            } else {

                sortedJsonRepr.add(key, json.getAsJsonPrimitive(key));

            }
        }

        return sortedJsonRepr;
    }


    /**
     * Sort each {@link JsonElement} of the array individually. Note that the relative order of elements within the
     * array is not changed.
     *
     * @param jsonArray The array to be sorted.
     * @return A new array, whose elements were individually sorted.
     */
    public static JsonArray arraySort(final JsonArray jsonArray) {

        JsonArray sortedArray = new JsonArray();

        for (JsonElement arrayElement : jsonArray) {
            if (arrayElement.isJsonArray()) {
                sortedArray.add(arraySort(arrayElement.getAsJsonArray()));
            } else if (arrayElement.isJsonObject()) {
                sortedArray.add(recursiveSort(arrayElement.getAsJsonObject()));
            } else {
                sortedArray.add(arrayElement.getAsJsonPrimitive());
            }
        }

        return sortedArray;
    }


    /**
     * Call {@link #elementWithPath(JsonObject, String, Character)} with the '|' (pipe) separator
     */
    public static Optional<JsonElement> elementWithPath(JsonObject jsonObject, String path) {

        return elementWithPath(jsonObject, path, '|');
    }


    /**
     * Finds the {@link JsonElement} within a {@link JsonObject} whose position in the hierarchy matches the provided
     * path. A path is specified using a single string where each level is separated by a separation character.
     * <p>
     * Example:
     * <pre>
     * {
     *   "field1": "value",
     *   "field2": {
     *     "nestedField": "nestedValue"
     *   }
     * }</pre>
     * Assuming '|' is used as a separation character, the nested value can be accessed by providing {@code "field2
     * |nestedField"} as the path.
     *
     * @param jsonObject The {@link JsonObject} on which to perform the search.
     * @param path       The path to the {@link JsonElement}.
     * @param separator  The separation character used in the path to distinguish levels in the object's hierarchy.
     * @return An {@link Optional} {@link JsonElement} value.
     */
    public static Optional<JsonElement> elementWithPath(JsonObject jsonObject, String path, Character separator) {
        String[] paths = path.split(Pattern.quote(String.valueOf(separator)));

        com.google.gson.JsonElement currentElement = jsonObject;

        for (String section : paths) {

            if (currentElement == null || !currentElement.isJsonObject()) {
                return Optional.empty();
            } else {
                currentElement = currentElement.getAsJsonObject().get(section);
            }
        }

        return currentElement != null ? Optional.of(currentElement) : Optional.empty();
    }
}
