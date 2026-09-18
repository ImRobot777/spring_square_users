package fr.campus.grog.SU.service;

import fr.campus.grog.SU.dto.UserCreationParams;
import fr.campus.grog.SU.entity.UserEntity;

import java.util.UUID;

public interface UserService {

    public UserEntity createUser(UserCreationParams requestParams);
    public UserEntity getUser(UUID id);
    public void deleteUser(UUID id);
    public boolean isUserValid(UUID id);

}
