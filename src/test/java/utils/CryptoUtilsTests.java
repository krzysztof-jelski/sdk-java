/*
Copyright: Ambrosus Technologies GmbH
Email: tech@ambrosus.com

This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.

This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
*/

package utils;

import com.ambrosus.utils.CryptoUtils;
import org.junit.Test;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.utils.Numeric;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CryptoUtilsTests extends CryptoUtils {

    @Test
    public void signDataIsCorrect() {
        ECKeyPair keyPair = ECKeyPair.create(Numeric.toBigInt
                ("0x0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef"));
        String hash = "Test message";
        assertEquals
                ("0x8b127d7edf67e2579c9894b589e57dde5c58aeb7d86ea12500ae0086022d6aac16c90243e6dc5af5ca9868c8a7e736090f1030b24d6dd57cacda3d9e2692b16c1b",
                        CryptoUtils.computeSignature(hash, keyPair));
    }


    @Test
    public void dataHashIsCorrect() {
        assertEquals("0xd81bbffb92157b72ceae3da72eb8224976ba42a49621822789edb0735a0e0395",
                CryptoUtils.computeHashString("Test message"));
    }


    @Test
    public void signatureVerificationIsCorrect() {
        ECKeyPair keyPair = ECKeyPair.create(Numeric.toBigInt
                ("0x0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef"));
        String address = Keys.toChecksumAddress(Keys.getAddress(keyPair.getPublicKey()));

        String message1 = "Test message";
        String message2 = "Another test";

        System.out.println(CryptoUtils.computeSignature(message1, keyPair));
        assertTrue(CryptoUtils.signatureMatches(
                message1,
                address,
                CryptoUtils.computeSignature(message1, keyPair)));

        System.out.println(CryptoUtils.computeSignature(message2, keyPair));
        assertTrue(CryptoUtils.signatureMatches(
                message2,
                address,
                CryptoUtils.computeSignature(message2, keyPair)));
    }


    @Test
    public void hashVerificationIsCorrect() {
        String message = "Test message";

        assertTrue(CryptoUtils.hashMatches(message, CryptoUtils.computeHashString(message)));
    }

}
