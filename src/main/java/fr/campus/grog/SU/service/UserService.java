package fr.campus.grog.SU.service;

import fr.campus.grog.SU.dto.UserCreationParams;
import fr.campus.grog.SU.entity.UserEntity;

import java.util.List;
import java.util.UUID;

public interface UserService {

    UserEntity createUser(UserCreationParams requestParams);
    UserEntity getUser(UUID id);
    void deleteUser(UUID id);
    boolean isUserValid(UUID id);
    List<UserEntity> getUsers();

}
