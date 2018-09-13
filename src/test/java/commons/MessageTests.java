/*
Copyright: Ambrosus Technologies GmbH
Email: tech@ambrosus.com

This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.

This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
*/

package commons;

import com.ambrosus.commons.Message;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class MessageTests {

    @Test
    public void constructorIsCorrect() {
        Message message = new Message("test");
        assertEquals("test", message.getMessage());
        assertEquals(Message.API_DATA_TYPE, message.getType());
    }


    @Test
    public void equalsIsRedefined() {
        Message m1 = new Message("test1");
        Message m2 = new Message("test1");
        Message m3 = new Message("test0");

        assertEquals(m1, m2);
        assertEquals(m1.hashCode(), m2.hashCode());
        assertNotEquals(m1, m3);
        assertNotEquals(m1.hashCode(), m3.hashCode());
    }
}
