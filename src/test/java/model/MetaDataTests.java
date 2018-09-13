/*
Copyright: Ambrosus Technologies GmbH
Email: tech@ambrosus.com

This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.

This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
*/

package model;

import com.ambrosus.model.MetaData;
import org.junit.Test;

import java.util.Objects;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class MetaDataTests {


    @Test
    public void constructorIsCorrect() {
        MetaData md = new MetaData("bundleId", 123L);

        assertEquals("bundleId", md.getBundleId());
        assertEquals(123L, md.getEntityUploadTimestamp());

        assertEquals(new MetaData("a", 1L), new MetaData("a", 1L));
        assertNotEquals(new MetaData("b", 1L), new MetaData("a", 1L));

        assertEquals(md.hashCode(), Objects.hash("bundleId", 123L));
    }
}
