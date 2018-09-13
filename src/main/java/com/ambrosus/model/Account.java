/*
Copyright: Ambrosus Technologies GmbH
Email: tech@ambrosus.com

This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.

This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
*/

package com.ambrosus.model;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Holds account information for AMBNet REST API responses deserialized by Gson.
 */
public class Account {


    private Account(Builder builder) {
        this.address = builder.address;
        this.registeredBy = builder.registeredBy;
        this.registeredOn = builder.registeredOn;
        this.permissions = builder.permissions;
        this.accessLevel = builder.accessLevel;
    }


    private final Long registeredOn;
    private final List<Account.Permission> permissions;
    private final String address;
    private final String registeredBy;
    private final Integer accessLevel;


    public enum Permission {
        REGISTER_ACCOUNT, CREATE_ENTITY
    }


    public Long getRegisteredOn() {

        return registeredOn;
    }


    public List<Account.Permission> getPermissions() {

        return permissions;
    }


    public String getAddress() {

        return address;
    }


    public String getRegisteredBy() {

        return registeredBy;
    }


    public Integer getAccessLevel() {

        return accessLevel;
    }


    @Override
    public boolean equals(Object o) {

        if (o == this)
            return true;
        if (!(o instanceof Account))
            return false;


        Account that = (Account) o;
        return this.accessLevel.equals(that.accessLevel) &&
                this.address.equals(that.address) &&
                this.registeredBy.equals(that.registeredBy) &&
                this.registeredOn.equals(that.registeredOn) &&
                this.permissions.equals(that.permissions);
    }


    public static class Builder {

        private final List<Account.Permission> permissions;
        private String address;
        private String registeredBy;
        private Long registeredOn;
        private Integer accessLevel;


        public Builder() {
            this.permissions = new ArrayList<>();
        }


        public String getAddress() {
            return address;
        }


        public Builder setAddress(String address) {
            this.address = address;
            return this;
        }


        public String getRegisteredBy() {
            return registeredBy;
        }


        public Builder setRegisteredBy(String registeredBy) {
            this.registeredBy = registeredBy;
            return this;
        }


        public Long getRegisteredOn() {
            return registeredOn;
        }


        public Builder setRegisteredOn(Long registeredOn) {
            this.registeredOn = registeredOn;
            return this;
        }


        public Integer getAccessLevel() {
            return accessLevel;
        }


        public Builder setAccessLevel(Integer accessLevel) {
            this.accessLevel = accessLevel;
            return this;
        }


        public Builder addPermission(Account.Permission permission) {
            this.permissions.add(permission);
            return this;
        }


        public List<Permission> getPermissions() {
            return permissions;
        }


        public Account build() {
            return new Account(this);
        }
    }


    @Override
    public int hashCode() {

        return Objects.hash(address, registeredBy, registeredOn, permissions, accessLevel);
    }


    public static class Adapter implements JsonDeserializer<Account>, JsonSerializer<Account> {

        @Override
        public Account deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws
                JsonParseException {

            try {

                Builder builder = new Builder();
                JsonObject jObj = json.getAsJsonObject();
                JsonArray permissionsArray = jObj.getAsJsonArray(JsonProperties.PERMISSIONS);

                for (JsonElement element : permissionsArray) {
                    builder.addPermission(Account.Permission.valueOf(element.getAsString().toUpperCase()));
                }

                builder.setAccessLevel(jObj.get(JsonProperties.ACCESS_LEVEL).getAsInt());
                builder.setRegisteredBy(jObj.get(JsonProperties.REGISTERED_BY).getAsString());
                builder.setRegisteredOn(jObj.get(JsonProperties.REGISTERED_ON).getAsLong());
                builder.setAddress(jObj.get(JsonProperties.ADDRESS).getAsString());

                return builder.build();

            } catch (IllegalStateException | NullPointerException | IllegalArgumentException e) {
                e.printStackTrace();
            }

            return null;
        }


        @Override
        public JsonElement serialize(Account src, Type typeOfSrc, JsonSerializationContext context) {

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty(JsonProperties.ADDRESS, src.getAddress());

            JsonArray permissionsJsonArray = new JsonArray();

            for (Account.Permission permission : src.getPermissions()) {
                permissionsJsonArray.add(permission.name().toLowerCase());
            }

            jsonObject.addProperty(JsonProperties.ACCESS_LEVEL, src.getAccessLevel());
            jsonObject.add(JsonProperties.PERMISSIONS, permissionsJsonArray);

            if (src.registeredBy != null)
                jsonObject.addProperty(JsonProperties.REGISTERED_BY, src.getRegisteredBy());

            if (src.registeredOn != null)
                jsonObject.addProperty(JsonProperties.REGISTERED_ON, src.getRegisteredOn());

            return jsonObject;
        }
    }
}
