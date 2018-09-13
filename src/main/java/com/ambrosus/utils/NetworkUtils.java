/*
Copyright: Ambrosus Technologies GmbH
Email: tech@ambrosus.com

This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.

This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
*/

package com.ambrosus.utils;

import com.ambrosus.AmbrosusSDK;
import com.ambrosus.model.Asset;
import com.ambrosus.network.ResponseWrapper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * Default retrofit2 callbacks for the SDK
 */
public abstract class NetworkUtils {

    public static class DefaultCallback<T> implements Callback<T> {

        private final Consumer<ResponseWrapper<T>> successConsumer;
        private final BiConsumer<Call<T>, Throwable> errorConsumer;

        public DefaultCallback(Consumer<ResponseWrapper<T>> successConsumer,
                               BiConsumer<Call<T>, Throwable> errorConsumer) {

            this.successConsumer = successConsumer;
            this.errorConsumer = errorConsumer;

        }


        public void onResponse(Call<T> call, Response<T> response) {
            successConsumer.accept(new ResponseWrapper<>(response));
        }


        public void onFailure(Call<T> call, Throwable throwable) {
            throwable.printStackTrace();

            if (errorConsumer != null)
                errorConsumer.accept(call, throwable);
        }
    }

    public static class RetrieveEventsForAsset implements Callback<Asset> {

        final AmbrosusSDK context;
        private final Consumer<Asset> successConsumer;
        private final BiConsumer<Call<?>, Throwable> errorConsumer;

        public RetrieveEventsForAsset(AmbrosusSDK context,
                                      Consumer<Asset> successConsumer,
                                      BiConsumer<Call<?>, Throwable> errorConsumer) {

            this.context = context;
            this.successConsumer = successConsumer;
            this.errorConsumer = errorConsumer;
        }


        public void onResponse(Call<Asset> call, Response<Asset> response) {

            Optional<Asset> optionalAsset = Optional.ofNullable(response.body());

            if (response.isSuccessful() && response.body() != null) {
                Asset asset = optionalAsset.get();
                context.getEvents(asset.getAssetId(), qResults -> {

                    Asset.Builder assetBuilder = Asset.Builder.fromExistingAsset(asset);
                    if (qResults.hasBody() && qResults.body().getResultCount() > 0) {
                        assetBuilder.addAllEvents(qResults.body().getResults());
                    }
                    successConsumer.accept(assetBuilder.build());
                }, errorConsumer::accept);
            } else {
                errorConsumer.accept(call, new Throwable("Response body was empty after getAsset."));
            }
        }


        public void onFailure(Call<Asset> call, Throwable throwable) {

            throwable.printStackTrace();

            if (errorConsumer != null)
                errorConsumer.accept(call, throwable);
        }
    }
}
