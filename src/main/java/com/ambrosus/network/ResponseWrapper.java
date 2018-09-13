/*
Copyright: Ambrosus Technologies GmbH
Email: tech@ambrosus.com

This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.

This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
*/

package com.ambrosus.network;

import com.ambrosus.utils.Consumer;
import okhttp3.Headers;
import retrofit2.Response;

import java.util.NoSuchElementException;

/**
 * Decorator class for a retrofit2 response.
 *
 * @param <T> The body type of a response that succeeded and whose body could be deserialized into one of the SDK
 *            com.ambrosus.model types.
 */
public final class ResponseWrapper<T> {

    private final Response<T> delegateResponse;


    public ResponseWrapper(Response<T> response) {
        this.delegateResponse = response;
    }


    public boolean hasBody() {
        return delegateResponse.body() != null;
    }

    // Taken and adapted from Optional documentation


    /**
     * If the response contains a body, performs the given action with the body,
     * otherwise does nothing.
     *
     * @param action the action to be performed, if the response body is present
     * @throws NullPointerException if the response body is present and the given action is
     *                              {@code null}
     */
    public void ifBodyPresent(Consumer<? super T> action) {
        if (hasBody())
            action.accept(delegateResponse.body());

    }


    // Taken and adapted from Optional documentation


    /**
     * If the response contains a body, performs the given action with the body,
     * otherwise performs the given empty-based action.
     *
     * @param action      the action to be performed, if the response body is present
     * @param emptyAction the empty-based action to be performed, if no response body is
     *                    present
     * @throws NullPointerException if the response body is present and the given action
     *                              is {@code null}, or no response body is present and the given empty-based
     *                              action is {@code null}.
     */
    public void ifBodyPresentOrElse(Consumer<? super T> action, Runnable emptyAction) {
        if (hasBody())
            action.accept(delegateResponse.body());
        else
            emptyAction.run();

    }


    public T body() {

        if (!hasBody())
            throw new NoSuchElementException("Response has no body object, check for its presence before calling " +
                    "this method.");


        return delegateResponse.body();
    }


    /**
     * @return The error body associated to this response
     * @throws NoSuchElementException if there is no error body
     */
    public okhttp3.ResponseBody errorBody() {

        if (delegateResponse.isSuccessful())
            throw new NoSuchElementException("Request was successful and response does not contain an error body.");


        return delegateResponse.errorBody();
    }

    // Only delegate calls below //


    /**
     * The raw response from the HTTP client.
     */
    public okhttp3.Response raw() {
        return delegateResponse.raw();
    }


    /**
     * HTTP status code.
     */
    public int code() {
        return delegateResponse.code();
    }


    /**
     * HTTP status message or null if unknown.
     */
    public String message() {
        return delegateResponse.message();
    }


    /**
     * HTTP headers.
     */
    public Headers headers() {
        return delegateResponse.headers();
    }


    /**
     * Returns true if the delegateResponse code is in the range [200..300).
     * Note that this does not mean that the SDK was able to deserialize the contents of the delegateResponse.
     */
    public boolean isSuccessful() {
        return delegateResponse.isSuccessful();
    }


    @Override
    public String toString() {
        return delegateResponse.toString();
    }


}
