package fr.campus.grog.SU.service;

import fr.campus.grog.SU.dao.UserDao;
import fr.campus.grog.SU.dto.UserCreationParams;
import fr.campus.grog.SU.entity.UserEntity;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private final UserDao userDao;

    public UserServiceImpl(UserDao userDao){
        this.userDao = userDao;
    }


    public UserEntity createUser(UserCreationParams requestParams){
        UserEntity user = new UserEntity();
        user.id = UUID.randomUUID().toString();
        user.pseudo = requestParams.pseudo();
        user.email = requestParams.email();
        return this.userDao.create(user);
    }

}
