/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.gl.ceir.config.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;

/**
 *
 * @author maverick
 */

@Getter
@Setter
@AllArgsConstructor

public class ExceptionResponse {
    
    	private int statusCode;
	private String statusMessage;
	private String language ="en";
	private String result;

    public ExceptionResponse(int statusCode, String statusMessage, String result) {
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
        this.result = result;
    }
        
        
        
        
    
}
