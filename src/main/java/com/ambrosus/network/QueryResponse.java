/*
Copyright: Ambrosus Technologies GmbH
Email: tech@ambrosus.com

This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.

This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
*/

package com.ambrosus.network;

import java.util.List;

/**
 * Utility class for holding results returned by AMBNet REST API.
 *
 * @param <T> The type of objects returned by the REST API. Note that this type refers to the Java type of objects
 *            after deserialization by Gson.
 */
public abstract class QueryResponse<T> {

    private int resultCount;
    private List<T> results;


    public int getResultCount() {

        return resultCount;
    }


    public List<T> getResults() {

        return results;
    }
}
