/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.gl.ceir.config.service.impl;

import com.gl.ceir.config.repository.app.UserRepository;
import com.gl.ceir.config.repository.app.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 *
 * @author maverick
 */
@Service
public class UserDiffImpl {

    @Autowired
    UserRepository userRepository;

    @Autowired
    UsersRepository usersRepository;
    
    @Value("${spring.jpa.properties.hibernate.dialect}")
    public String dialect;
    
    
    public <T> T getByUsernameAndPasswordAndParentId(String userName, String password, int parentId) {
        if (dialect.toLowerCase().contains("mysql")) {
            return (T) userRepository
                    .getByUsernameAndPasswordAndParentId(userName, password, parentId);
        } else {
            return (T) usersRepository
                    .getByUsernameAndPasswordAndParentId(userName, password, parentId);
        }
    }
    

}
