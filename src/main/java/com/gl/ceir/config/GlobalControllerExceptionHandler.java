 /*
  * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
  * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
  */
 package com.gl.ceir.config;
 import javax.servlet.http.HttpServletRequest;
 import org.json.simple.JSONObject;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.HttpStatus;
 import org.springframework.web.HttpMediaTypeNotSupportedException;
 import org.springframework.web.HttpRequestMethodNotSupportedException;
 import org.springframework.web.bind.annotation.ControllerAdvice;
 import org.springframework.web.bind.annotation.ExceptionHandler;
 import org.springframework.web.bind.annotation.ResponseStatus;
 import org.springframework.web.bind.annotation.RestControllerAdvice;
 import org.springframework.web.context.request.WebRequest;
 import org.springframework.web.servlet.NoHandlerFoundException;
 
 
   @ControllerAdvice
 @RestControllerAdvice
 public class GlobalControllerExceptionHandler {
     @Autowired
     HttpServletRequest req;
     @ExceptionHandler(NoHandlerFoundException.class)
   
     
     @ResponseStatus(HttpStatus.NOT_FOUND)
     public String handleNoHandlerFound(NoHandlerFoundException e, WebRequest request) {
         String title = req.getServletPath().contains("v1") ? "invalid url end point " : " version not correct";
         return customResponseWithCode("404", "not found", title);
     }
     @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
     @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
     public String handleMethodNotAllowed(HttpRequestMethodNotSupportedException e, WebRequest request) {
         return customResponseWithCode("405", "method not allowed", " method provided is not valid");
     }
     @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
     @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
     public String handleHttpMediaTypeNotSupported(Exception e, WebRequest request) {
         return customResponseWithCode("406", "not acceptable", "request format is not acceptable ");
     }
     @ExceptionHandler(Exception.class)
     @ResponseStatus(HttpStatus.BAD_REQUEST)
     public String handleBadRequestException(Exception e, WebRequest request) {
         return customResponseWithCode("400", "bad request", " request is not valid ");
     }
     
     @ExceptionHandler(IllegalArgumentException.class)
     @ResponseStatus(HttpStatus.BAD_REQUEST)
     public String handleIllegalRequestException(Exception e, WebRequest request) {
         return customResponseWithCode("400", "bad request", " request is not valid ");
     }
     String customResponseWithCode(String code, String title, String message) {
         JSONObject item = new JSONObject();
         item.put("statusCode", code);
         item.put("language", "en");
         item.put("statusMessage", title);
         item.put("result", message);
         return item.toJSONString();
     }
 }