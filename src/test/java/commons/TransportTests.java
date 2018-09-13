/*
Copyright: Ambrosus Technologies GmbH
Email: tech@ambrosus.com

This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.

This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
*/

package commons;

import com.ambrosus.commons.Transport;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class TransportTests {

    private Transport transport;


    @Before
    public void init() {
        transport = new Transport("shipmentName", "status", "vehicle");
    }


    @Test
    public void gettersAreCorrect() {
        assertEquals("shipmentName", transport.getName());
        assertEquals("status", transport.getStatus());
        assertEquals("vehicle", transport.getVehicle());
        assertEquals(Transport.API_DATA_TYPE, transport.getType());
    }


    @Test
    public void equalsIsRedefined() {
        assertEquals(new Transport("a", "b", "c"), new Transport("a", "b", "c"));
        assertEquals(new Transport("a", "b", "c").hashCode(), new Transport("a", "b", "c").hashCode());
        assertNotEquals(new Transport("a", "c", "c"), new Transport("a", "b", "c"));
        assertNotEquals(new Transport("a", "c", "c").hashCode(), new Transport("a", "b", "c").hashCode());
    }
}
