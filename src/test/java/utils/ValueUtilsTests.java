/*
Copyright: Ambrosus Technologies GmbH
Email: tech@ambrosus.com

This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.

This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
*/

package utils;

import com.ambrosus.utils.ValueUtils;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class ValueUtilsTests extends ValueUtils {

    @Test
    public void thatOrEmptyIsCorrect() {

        assertEquals("abc", ValueUtils.thatOrEmpty("abc"));
        assertEquals("", ValueUtils.thatOrEmpty(null));
    }
}
