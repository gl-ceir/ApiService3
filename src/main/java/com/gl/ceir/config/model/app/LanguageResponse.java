/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.gl.ceir.config.model.app;

// import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import org.json.simple.JSONObject;
//import springfox.documentation.spring.web.json.Json;

/**
 *
 * @author maverick
 */
/// response might has some annotation for default
@Getter
@Setter
public class LanguageResponse {

    private String languageType;
    private JSONObject labelDetails;

    public LanguageResponse(String languageType, JSONObject labelDetails) {
        this.languageType = languageType;
        this.labelDetails = labelDetails;
    }

}
