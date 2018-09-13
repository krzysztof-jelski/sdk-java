/*
Copyright: Ambrosus Technologies GmbH
Email: tech@ambrosus.com

This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.

This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
*/


package com.ambrosus.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Holds metadata for AMBNet REST API responses deserialized by Gson.
 */
public final class MetaData implements Serializable {

    private final String bundleId;
    private final long entityUploadTimestamp;


    public MetaData(String bundleId, long entityUploadTimestamp) {
        this.bundleId = bundleId;
        this.entityUploadTimestamp = entityUploadTimestamp;
    }


    public String getBundleId() {
        return bundleId;
    }


    public long getEntityUploadTimestamp() {
        return entityUploadTimestamp;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MetaData metaData = (MetaData) o;
        return entityUploadTimestamp == metaData.entityUploadTimestamp &&
                Objects.equals(bundleId, metaData.bundleId);
    }


    @Override
    public int hashCode() {
        return Objects.hash(bundleId, entityUploadTimestamp);
    }
}
