package fr.campus.grog.SU.service;

import fr.campus.grog.SU.dao.UserDao;
import fr.campus.grog.SU.dto.UserCreationParams;
import fr.campus.grog.SU.entity.UserEntity;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private final UserDao userDao;

    public UserServiceImpl(UserDao userDao){
        this.userDao = userDao;
    }

    @Override
    public UserEntity createUser(UserCreationParams requestParams){
        UserEntity user = new UserEntity();
        user.id = UUID.randomUUID().toString();
        user.pseudo = requestParams.pseudo();
        user.email = requestParams.email();
        return this.userDao.create(user);
    }

    @Override
    public UserEntity getUser(UUID userId){
        return this.userDao.find(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    @Override
    public void deleteUser(UUID userId){
        if(this.userDao.isExisting(userId)){
            this.userDao.delete(userId);
        }
        else{
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
    }

    @Override
    public boolean isUserValid(UUID userId){
        return this.userDao.isExisting(userId);
    }

}
