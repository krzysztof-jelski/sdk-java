/*
Copyright: Ambrosus Technologies GmbH
Email: tech@ambrosus.com

This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.

This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
*/

package commons;

import com.ambrosus.commons.RawJson;
import com.google.gson.JsonObject;
import org.junit.Before;
import org.junit.Test;
import utils.TestUtils;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static utils.TestUtils.PATH_PREFIX;

public class RawJsonTests {

    @Before
    public void init() {
    }


    @Test
    public void toStringIsCorrect() {
        JsonObject jsonObject = TestUtils.readJson(PATH_PREFIX + "valid_asset.json");
        RawJson rawJson = new RawJson(jsonObject);

        assertEquals(jsonObject.toString(), rawJson.toString());
    }


    @Test
    public void equalsIsRedefined() {
        JsonObject inner1 = new JsonObject();
        inner1.addProperty("prop1", "value1");
        JsonObject inner2 = new JsonObject();
        inner2.addProperty("prop1", "value1");

        RawJson raw1 = new RawJson(inner1);
        RawJson raw2 = new RawJson(inner2);
        RawJson raw3 = new RawJson(new JsonObject());

        assertEquals(raw1, raw2);
        assertEquals(raw1.hashCode(), raw2.hashCode());
        assertNotEquals(raw1, raw3);
        assertNotEquals(raw1.hashCode(), raw3.hashCode());
    }
}
