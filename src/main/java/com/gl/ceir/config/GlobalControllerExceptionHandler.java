/*
  * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
  * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.gl.ceir.config;

import com.gl.ceir.config.dto.ApiResponse;
import com.gl.ceir.config.dto.ExceptionResponse;
import com.gl.ceir.config.exceptions.FileStorageException;
import com.gl.ceir.config.exceptions.InternalServicesException;
import com.gl.ceir.config.exceptions.MissingRequestParameterException;
import com.gl.ceir.config.exceptions.MyFileNotFoundException;
import com.gl.ceir.config.exceptions.ResourceNotFoundException;
import com.gl.ceir.config.exceptions.UnprocessableEntityException;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpServerErrorException.InternalServerError;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;

//@EnableWebMvc   //to be remove FOR SWAGGER
@ControllerAdvice
@RestControllerAdvice
public class GlobalControllerExceptionHandler {

    @Autowired
    HttpServletRequest req;

    /* Global Exceptions*/
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ExceptionResponse handleNoHandlerFound(NoHandlerFoundException e, WebRequest request) {
        return new ExceptionResponse(
                HttpStatus.NOT_FOUND.value(), "not found", "invalid url end point");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ExceptionResponse handleMethodNotAllowed(HttpRequestMethodNotSupportedException e, WebRequest request) {
        return new ExceptionResponse(
                HttpStatus.METHOD_NOT_ALLOWED.value(), "method not allowed", " method provided is not valid");
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    public ExceptionResponse handleHttpMediaTypeNotSupported(Exception e, WebRequest request) {
        return new ExceptionResponse(
                HttpStatus.NOT_ACCEPTABLE.value(), "not acceptable", "request format is not acceptable ");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionResponse handleBadRequestException(Exception e, WebRequest request) {
        return new ExceptionResponse(
                HttpStatus.BAD_REQUEST.value(), "bad request", " request is not valid ");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionResponse handleIllegalRequestException(Exception e, WebRequest request) {
        return new ExceptionResponse(
                HttpStatus.NOT_FOUND.value(), "bad request", " request is not valid ");
    }

    @ExceptionHandler(InternalServerError.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ExceptionResponse handleInternalServerException(Exception e, WebRequest request) {
        return new ExceptionResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(), "server error", "Something Went Wrong");
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionResponse handleMissingServletRequestParameterException(Exception e, WebRequest request) {
        return new ExceptionResponse(
                HttpStatus.BAD_REQUEST.value(), "bad request", "parameter missing");
    }

    
    
    /* Custom Exceptions */
    
    
    
    @ExceptionHandler(value = MissingRequestParameterException.class)
    public ResponseEntity<Object> exception(MissingRequestParameterException exception) {
        return new ResponseEntity<>(
                new ExceptionResponse(
                        HttpStatus.BAD_REQUEST.value(), "bad request", "en", "parameter missing"),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = InternalServicesException.class)
    public ResponseEntity<Object> exception(InternalServicesException exception) {
        return new ResponseEntity<>(
                new ExceptionResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(), " server error", "en", exception.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = UnprocessableEntityException.class)
    public ResponseEntity<Object> exception(UnprocessableEntityException exception) {
        return new ResponseEntity<>(
                new ExceptionResponse(
                        HttpStatus.UNPROCESSABLE_ENTITY.value(), "unprocessable entity", "en", exception.getMessage()),
                HttpStatus.UNPROCESSABLE_ENTITY);

    }

    @ExceptionHandler(value = ResourceNotFoundException.class)
    public ResponseEntity<Object> exception(ResourceNotFoundException exception) {
        return new ResponseEntity<>(
                new ExceptionResponse(HttpStatus.NOT_FOUND.value(), "not found", "en", exception.getMessage()),
                HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = MyFileNotFoundException.class)
    public ResponseEntity<Object> exception(MyFileNotFoundException exception) {
        return new ResponseEntity<>(new ApiResponse(HttpStatus.NOT_FOUND.value(), "FAIL", exception.getMessage()),
                HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = FileStorageException.class)
    public ResponseEntity<Object> exception(FileStorageException exception) {
        return new ResponseEntity<>(new ApiResponse(HttpStatus.NOT_FOUND.value(), "FAIL", exception.getMessage()),
                HttpStatus.NOT_FOUND);
    }

}

//    JSONObject jsonObject = new JSONObject(jsonString) {
//    /**
//     * changes the value of JSONObject.map to a LinkedHashMap in order to maintain
//     * order of keys.
//     */
//    @Override
//    public JSONObject put(String key, Object value) throws JSONException {
//        try {
//            Field map = JSONObject.class.getDeclaredField("map");
//            map.setAccessible(true);
//            Object mapValue = map.get(this);
//            if (!(mapValue instanceof LinkedHashMap)) {
//                map.set(this, new LinkedHashMap<>());
//            }
//        } catch (NoSuchFieldException | IllegalAccessException e) {
//            throw new RuntimeException(e);
//        }
//        return super.put(key, value);
//    }
//};
