package com.ambrosus.model;

import com.ambrosus.utils.CryptoUtils;
import com.ambrosus.utils.JsonUtils;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.web3j.crypto.ECKeyPair;

import java.lang.reflect.Type;
import java.util.List;

public class GroupToken {


    private long validUntil;

    public GroupToken(long validUntil) {
        this.validUntil = validUntil;
    }

    public static class Adapter  implements JsonSerializer<GroupToken>{
        private final ECKeyPair keyPair;
        private final String address;

        public Adapter(ECKeyPair keyPair, String address) {
            this.keyPair = keyPair;
            this.address = address;
        }

        @Override
        public JsonElement serialize(GroupToken src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject body = new JsonObject();
            JsonObject idData = new JsonObject();

            idData.addProperty(JsonProperties.CREATED_BY, address);
            idData.addProperty("validUntil", src.validUntil);

            String signature = CryptoUtils.computeSignature(idData.toString(), keyPair);
            body.addProperty(JsonProperties.SIGNATURE, signature);
            body.add(JsonProperties.ID_DATA, idData);

            return body;
        }
    }
}
