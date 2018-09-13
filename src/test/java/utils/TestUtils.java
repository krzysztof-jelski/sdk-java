/*
Copyright: Ambrosus Technologies GmbH
Email: tech@ambrosus.com

This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.

This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
*/

package utils;

import com.ambrosus.utils.BiConsumer;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import retrofit2.Call;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class TestUtils {

    public final static String PATH_PREFIX = "src/test/resources/";


    public static JsonObject readJson(String filePath) {

        Gson gson = new Gson();
        JsonObject jsonObject = null;

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {

            jsonObject = gson.fromJson(br, JsonObject.class);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }


    public static <T> BiConsumer<Call<T>, Throwable> noOp() {
        return (c, t) -> {
        };
    }

}
