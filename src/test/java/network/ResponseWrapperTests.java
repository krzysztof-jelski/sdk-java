/*
Copyright: Ambrosus Technologies GmbH
Email: tech@ambrosus.com

This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.

This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
*/

package network;

import com.ambrosus.model.Asset;
import com.ambrosus.network.ResponseWrapper;
import okhttp3.MediaType;
import okhttp3.ResponseBody;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import retrofit2.Response;

import java.util.NoSuchElementException;

import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;

public class ResponseWrapperTests {

    private Asset asset;
    private ResponseBody errorBody;
    private Response successResponse;
    private Response errorResponse;
    private ResponseWrapper<Asset> successResponseWrapper;
    private ResponseWrapper<Asset> errorResponseWrapper;
    private boolean trigger;


    @Before
    public void init() {
        trigger = false;

        Asset.Builder assetBuilder = new Asset.Builder();
        asset = assetBuilder.build();
        errorBody = ResponseBody.create(MediaType.parse(""), "");
        successResponse = Response.success(asset);
        errorResponse = Response.error(500, errorBody);
        successResponseWrapper = new ResponseWrapper<Asset>(successResponse);
        errorResponseWrapper = new ResponseWrapper<Asset>(errorResponse);
    }


    @Test
    public void ifBodyPresentIsCorrect() {
        successResponseWrapper.ifBodyPresent(asset -> {
            trigger = true;
        });

        assertTrue(trigger);

        errorResponseWrapper.ifBodyPresent(asset -> {
            trigger = false;
        });

        assertTrue(trigger);
    }


    @Test
    public void ifBodyPresentOrElseIsCorrect() {

        successResponseWrapper.ifBodyPresentOrElse(asset -> {
            trigger = true;
        }, Assert::fail);

        assertTrue(trigger);

        trigger = false;

        errorResponseWrapper.ifBodyPresentOrElse(asset -> {
                    fail();
                },
                () ->
                        trigger = true
        );

        assertTrue(trigger);
    }


    @Test
    public void bodyReturnsAsset() {
        Asset body = successResponseWrapper.body();
        assertEquals(asset, body);
    }


    @Test(expected = NoSuchElementException.class)
    public void bodyThrowsExceptionIfNotPresent() {
        errorResponseWrapper.body();
    }


    @Test
    public void errorBodyReturnsCorrectErrorBody() {
        assertEquals(errorBody, errorResponseWrapper.errorBody());
    }


    @Test(expected = NoSuchElementException.class)
    public void errorBodyThrowsExceptionIfResponseSuccessful() {
        successResponseWrapper.errorBody();
    }


    @Test
    public void unmodifiedMethodsCallDelegateObject() {
        assertEquals(successResponse.raw(), successResponseWrapper.raw());
        assertEquals(successResponse.code(), successResponseWrapper.code());
        assertEquals(successResponse.toString(), successResponseWrapper.toString());
        assertEquals(successResponse.message(), successResponseWrapper.message());
        assertEquals(successResponse.headers(), successResponseWrapper.headers());
        assertEquals(successResponse.isSuccessful(), successResponseWrapper.isSuccessful());
    }
}
