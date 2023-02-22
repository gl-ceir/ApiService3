package com.gl.ceir.config.model;

import com.gl.ceir.config.model.DeviceDetails;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class Result {

    public Result(boolean isValidImei, String message, DeviceDetails deviceDetails) {
        this.isValidImei = isValidImei;
        this.message = message;
        this.deviceDetails = deviceDetails;
    }

    private boolean isValidImei;
    private String message;
    private DeviceDetails deviceDetails;

}