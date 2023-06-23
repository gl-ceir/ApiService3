package com.gl.ceir.config.model.app;

import com.gl.ceir.config.model.app.DeviceDetails;
import com.google.gson.JsonObject;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.json.simple.JSONObject;

@Getter
@Setter
public class Result {

    public Result(boolean isValidImei, String message, Map deviceDetails) {
        this.isValidImei = isValidImei;
        this.message = message;
        this.deviceDetails = deviceDetails;
    }

    private boolean isValidImei;
    private String message;
    private Map deviceDetails;

//    @Override
//    public String toString() {
//        return "Result{" + "isValidImei=" + isValidImei + ", message=" + message + ", deviceDetails=" + deviceDetails + '}';
//    }

    
}
