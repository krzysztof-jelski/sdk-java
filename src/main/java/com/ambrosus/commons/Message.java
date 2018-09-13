/*
Copyright: Ambrosus Technologies GmbH
Email: tech@ambrosus.com

This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.

This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
*/

package com.ambrosus.commons;

import com.ambrosus.model.EventData;

import java.util.Objects;

/**
 * Class modelling simple messages to be stored on AMBNet
 */
public class Message extends EventData {

    public final static String API_DATA_TYPE = "ambrosus.event.message";
    private final String name;


    public Message(String message) {
        super(API_DATA_TYPE);
        this.name = message;
    }


    public String getMessage() {
        return name;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return Objects.equals(name, message.name);
    }


    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
