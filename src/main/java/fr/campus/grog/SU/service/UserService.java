package fr.campus.grog.SU.service;

import fr.campus.grog.SU.dto.UserCreationParams;
import fr.campus.grog.SU.entity.UserEntity;

public interface UserService {

    public UserEntity createUser(UserCreationParams requestParams);
    //public UserEntity getUser(String id);
    //public void deleteUser(String id);
    //public boolean isUserValid(String id);

}
