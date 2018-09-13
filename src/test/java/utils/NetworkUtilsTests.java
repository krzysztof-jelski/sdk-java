/*
Copyright: Ambrosus Technologies GmbH
Email: tech@ambrosus.com

This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.

This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
*/

package utils;

import com.ambrosus.AmbrosusSDK;
import com.ambrosus.model.Asset;
import com.ambrosus.network.EventQueryResponse;
import com.ambrosus.network.ResponseWrapper;
import com.ambrosus.utils.BiConsumer;
import com.ambrosus.utils.Consumer;
import com.ambrosus.utils.NetworkUtils;
import org.junit.Before;
import org.junit.Test;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class NetworkUtilsTests extends NetworkUtils {

    private boolean trigger;


    @Before
    public void init() {
        trigger = false;
    }


    @Test
    public void callbackWithFailureConsumerIsCorrect() {


        NetworkUtils.DefaultCallback<Consumer<ResponseWrapper<Asset>>> call1 =
                new NetworkUtils.DefaultCallback<>(assetRW -> {
                }, (call, t) -> {
                    trigger();
                });

        NetworkUtils.RetrieveEventsForAsset call2 = new NetworkUtils.RetrieveEventsForAsset(null, optionalAsset -> {
            trigger();
        },
                (call, t) -> {
                    trigger();
                });

        call1.onFailure(null, new IOException("error"));
        assertTrue(trigger);

        trigger = false;
        call2.onFailure(null, new IOException("error"));
        assertTrue(trigger);

        trigger = false;
        call2.onResponse(null, Response.success(null));
        assertTrue(trigger);
    }


    @Test
    public void nestedFailureIsHandled() {
        NetworkUtils.RetrieveEventsForAsset call2 = new NetworkUtils.RetrieveEventsForAsset(
                new MockSDK(new AmbrosusSDK.Config("", "0x12345")),
                optionalAsset -> {
                },
                (call, t) -> {
                    trigger();
                });


        call2.onResponse(null, Response.success(new Asset.Builder().build()));
        assertTrue(trigger);
    }


    private void trigger() {
        trigger = true;
    }


    private static class MockSDK extends AmbrosusSDK {

        public MockSDK(AmbrosusSDK.Config c) {
            super(c);
        }


        @Override
        public void getEvents(String assetId, Consumer<ResponseWrapper<EventQueryResponse>> ambQueryResultConsumer,
                              BiConsumer<Call<EventQueryResponse>, Throwable> errorConsumer) {

            errorConsumer.accept(null, new Throwable("getEvents was called on mock SDK"));

        }
    }
}
