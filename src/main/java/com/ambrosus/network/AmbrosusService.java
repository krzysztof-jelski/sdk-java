/*
Copyright: Ambrosus Technologies GmbH
Email: tech@ambrosus.com

This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.

This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
*/

package com.ambrosus.network;

import com.ambrosus.model.Account;
import com.ambrosus.model.Asset;
import com.ambrosus.model.Event;
import com.ambrosus.model.Token;
import com.google.gson.JsonObject;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.Map;


/**
 * Service interface for interacting with AMBNet REST API.
 */
public interface AmbrosusService {

    String TOKEN_PREAMBLE = "AMB_TOKEN ";
    String SECRET_PREAMBLE = "AMB ";


    @GET("assets/{assetId}")
    Call<Asset> getAsset(@Path("assetId") String assetId);


    @GET("events")
    Call<EventQueryResponse> getEvents(@Query("assetId") String assetId);


    @GET("assets")
    Call<AssetQueryResponse> findAssets(@QueryMap Map<String, String> params);


    @GET("events")
    Call<EventQueryResponse> findEvents(@QueryMap Map<String, String> params);


    @GET("accounts")
    Call<AccountQueryResponse> findAccounts(@Header("Authorization") String token,
                                            @QueryMap Map<String, String> params);


    @POST("assets")
    @Headers({
            "Content-Type:application/json",
            "Accept:application/json"
    })
    Call<Asset> createAsset(@Body Asset body);


    @POST("assets/{assetId}/events")
    @Headers({
            "Content-Type:application/json",
            "Accept:application/json"
    })
    Call<Event> createEvent(@Path("assetId") String assetId, @Body Event event);


    @POST("accounts")
    @Headers({
            "Content-Type:application/json",
            "Accept:application/json"
    })
    Call<Account> createAccount(@Header("Authorization") String token, @Body Account body);


    @POST("token")
    @Headers({
            "Content-Type:application/json",
            "Accept:application/json"
    })
    Call<Token> createToken(@Header("Authorization") String secret, @Body JsonObject body);


    @GET("accounts/{accountAddress}")
    @Headers({
            "Accept:application/json"
    })
    Call<Account> getAccount(@Header("Authorization") String token, @Path("accountAddress") String accountAddress);
}