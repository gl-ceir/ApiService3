package com.gl.ceir.config.repository.app;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gl.ceir.config.model.app.User;


public interface UserRepository extends JpaRepository<User, Integer> {

    public User getByUsernameAndPasswordAndParentId(String userName, String password, int parentId);
}
