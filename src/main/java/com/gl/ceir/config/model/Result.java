package com.gl.ceir.config.model;

import com.gl.ceir.config.model.DeviceDetails;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import org.json.simple.JSONObject;

@Getter
@Setter
public class Result {

    public Result(boolean isValidImei, String message, JSONObject deviceDetails) {
        this.isValidImei = isValidImei;
        this.message = message;
        this.deviceDetails = deviceDetails;
    }

    private boolean isValidImei;
    private String message;
    private JSONObject deviceDetails;

}
