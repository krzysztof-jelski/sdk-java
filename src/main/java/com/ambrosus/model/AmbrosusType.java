/*
Copyright: Ambrosus Technologies GmbH
Email: tech@ambrosus.com

This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.

This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
*/

package com.ambrosus.model;

/**
 * Base type for objects returned by the REST API. Contains only metadata.
 */
public abstract class AmbrosusType {

    final String createdBy;
    final Long timestamp;
    final MetaData metaData;


    AmbrosusType(Builder builder) {

        this.createdBy = builder.createdBy;
        this.timestamp = builder.timestamp;
        this.metaData = builder.metaData;
    }


    public String getCreatedBy() {

        return createdBy;
    }


    public Long getTimestamp() {

        return timestamp;
    }


    public MetaData getMetaData() {

        return metaData;
    }


    public abstract static class Builder {

        String createdBy;
        Long timestamp;
        MetaData metaData;


        public String getCreatedBy() {
            return createdBy;
        }


        public Builder setCreatedBy(String createdBy) {
            this.createdBy = createdBy;
            return this;
        }


        public Long getTimestamp() {
            return timestamp;
        }


        public Builder setTimestamp(Long timestamp) {
            this.timestamp = timestamp;
            return this;
        }


        public MetaData getMetaData() {
            return metaData;
        }


        public Builder setMetaData(MetaData metaData) {
            this.metaData = metaData;
            return this;
        }
    }
}
