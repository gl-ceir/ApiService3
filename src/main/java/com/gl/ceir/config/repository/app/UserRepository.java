package com.gl.ceir.config.repository.app;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gl.ceir.config.model.app.User;

public interface UserRepository extends JpaRepository<User, Integer> {

    public User getByUsernameAndPasswordAndParentId(String userName, String password, int parentId);
}
