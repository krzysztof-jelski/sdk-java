/*
Copyright: Ambrosus Technologies GmbH
Email: tech@ambrosus.com

This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.

This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
*/

package com.ambrosus.commons;

import com.ambrosus.model.EventData;
import com.ambrosus.utils.ValueUtils;

import java.util.Objects;

/**
 * Class modelling transportation events stored on AMBNet
 */
public class Transport extends EventData {

    public static final String API_DATA_TYPE = "ambrosus.event.transport";

    private final String name;
    private final String status;
    private final String vehicle;


    public Transport(String name, String status, String vehicle) {
        super(API_DATA_TYPE);
        this.name = name;
        this.status = status;
        this.vehicle = vehicle;
    }


    public String getName() {
        return ValueUtils.thatOrEmpty(name);
    }


    public String getStatus() {
        return ValueUtils.thatOrEmpty(status);
    }


    public String getVehicle() {
        return ValueUtils.thatOrEmpty(vehicle);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transport transport = (Transport) o;
        return Objects.equals(name, transport.name) &&
                Objects.equals(status, transport.status) &&
                Objects.equals(vehicle, transport.vehicle);
    }


    @Override
    public int hashCode() {
        return Objects.hash(name, status, vehicle);
    }
}
