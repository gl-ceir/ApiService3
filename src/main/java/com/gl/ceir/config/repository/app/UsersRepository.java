package com.gl.ceir.config.repository.app;

import com.gl.ceir.config.model.app.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UsersRepository extends JpaRepository<Users, Integer> {

    @Query(value = "select U from Users U where P.userName=?1, and P.password= ?2 and p.parentId=?3", nativeQuery = true)
    public Users getByUsernameAndPasswordAndParentId(String userName, String password, int parentId);
}
