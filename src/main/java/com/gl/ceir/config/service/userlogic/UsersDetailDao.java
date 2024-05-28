package com.gl.ceir.config.service.userlogic;

import com.gl.ceir.config.repository.app.UsersRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;



@Component
public class UsersDetailDao implements UserInterface {
    private  final Logger logger = LogManager.getLogger(getClass());

    @Autowired
    UsersRepository usersRepository;

    @Override
    public <T> T getUserDetailDao(String userName, String password, int parentId) {
        logger.info("Dao started for "+userName);
        return (T) usersRepository
                .getByUsernameAndPasswordAndParentId(userName, password, parentId);
    }


    @Override
    public <T> T getUserDetailDao(String userName, String password) {
        logger.info("Dao started for "+userName);
        return (T) usersRepository
                .getByUsernameAndPassword(userName, password);
    }
}
