/*
Copyright: Ambrosus Technologies GmbH
Email: tech@ambrosus.com

This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.

This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
*/

package model;

import com.ambrosus.model.Token;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class TokenTests {

    @Test
    public void tokenConstructorIsCorrect() {
        Token token = new Token("abc");

        assertEquals("abc", token.getValue());
    }
}
