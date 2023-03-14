/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.gl.ceir.config.model.constants;

public enum Alerts {
    ALERT_1103("alert1103"),
    ALERT_1104("alert1104"),
    ALERT_1105("alert1105");

    private String name;

    Alerts(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
