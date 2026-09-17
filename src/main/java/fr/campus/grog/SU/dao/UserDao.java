package fr.campus.grog.SU.dao;

import fr.campus.grog.SU.entity.UserEntity;

import java.util.Optional;
import java.util.UUID;

public interface UserDao {
    Optional<UserEntity> find(UUID id);
    UserEntity create(UserEntity user);
    void delete(UUID id);
    boolean isExisting(UUID id);
}
