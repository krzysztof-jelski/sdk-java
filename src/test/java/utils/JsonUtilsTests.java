/*
Copyright: Ambrosus Technologies GmbH
Email: tech@ambrosus.com

This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.

This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
*/

package utils;

import com.ambrosus.utils.JsonUtils;
import com.ambrosus.utils.Optional;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class JsonUtilsTests extends JsonUtils {

    private JsonObject jObj;
    private JsonObject asset;


    @Before
    public void init() {

        asset = TestUtils.readJson(TestUtils.PATH_PREFIX + "valid_asset.json");

        JsonObject jsonOuter = new JsonObject();
        JsonObject jsonInner = new JsonObject();
        JsonObject jsonArrayInner = new JsonObject();
        JsonObject jsonArrayInner2 = new JsonObject();
        JsonArray jsonArray = new JsonArray();

        jsonOuter.addProperty("z", 0);
        jsonOuter.addProperty("a", 0);
        jsonOuter.add("innerJsonObject", jsonInner);
        jsonOuter.add("innerJsonArray", jsonArray);

        jsonInner.addProperty("z", 0);
        jsonInner.addProperty("a", 0);

        jsonArray.add(jsonArrayInner);
        jsonArray.add(jsonArrayInner2);

        jsonArrayInner.addProperty("z", 0);
        jsonArrayInner2.addProperty("z", 0);
        jsonArrayInner.addProperty("a", 0);
        jsonArrayInner2.addProperty("a", 0);

        jObj = jsonOuter;
    }


    @Test
    public void jsonRecursiveSortIsCorrect() {


        String expected = "{" +
                "\"a\":0," +
                "\"innerJsonArray\":[" +
                "{\"a\":0,\"z\":0}," +
                "{\"a\":0,\"z\":0}" +
                "]," +
                "\"innerJsonObject\":{" +
                "\"a\":0," +
                "\"z\":0" +
                "}," +
                "\"z\":0" +
                "}";

        assertEquals(expected, new Gson().toJson(JsonUtils.recursiveSort(jObj)));
    }


    @Test
    public void arraySortIsCorrect() {
        JsonObject json = TestUtils.readJson(TestUtils.PATH_PREFIX + "nested_arrays.json");

        JsonArray array = json.getAsJsonArray("outerArray");
        JsonArray sortedArray = JsonUtils.arraySort(array);

        JsonObject jObj = new JsonObject();
        jObj.addProperty("a", 1);
        jObj.addProperty("b", 2);

        assertEquals(
                jObj.toString(),
                sortedArray
                        .get(0).getAsJsonArray()
                        .get(0).getAsJsonObject()
                        .toString()
        );

    }


    @Test
    public void getElementWithPathIsCorrect() {
        assertEquals(0, JsonUtils.elementWithPath(jObj, "z").get().getAsInt());
        assertEquals(0, JsonUtils.elementWithPath(jObj, "innerJsonObject|a").get().getAsInt());

        JsonObject innerJObj = new JsonObject();
        JsonArray jsonArray = new JsonArray();

        jsonArray.add("first string");
        jsonArray.add("second string");


        innerJObj.add("string array", jsonArray);
        jObj.add("inner", innerJObj);

        assertEquals(jsonArray, JsonUtils.elementWithPath(jObj, "inner|string array").get().getAsJsonArray());
    }


    @Test
    public void getInvalidElementWithPathReturnsEmpty() {

        assertEquals(Optional.empty(),
                JsonUtils.elementWithPath(jObj, "innerJsonObject|nonexistingelement"));


        assertEquals(Optional.empty(),
                JsonUtils.elementWithPath(jObj, "innerJsonObject|nonexistinglevel|nonexistingelement"));

    }


    @Test
    public void getElementWithPathWithRegexIsCorrect() {

        assertEquals(0, JsonUtils.elementWithPath(jObj, "innerJsonObject|a", '|').get().getAsInt());
        assertEquals(3,
                JsonUtils.elementWithPath(asset, "content.idData.sequenceNumber", '.').get().getAsInt());
    }
}
