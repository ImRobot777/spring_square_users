package fr.campus.grog.SU.dao;

import fr.campus.grog.SU.entity.UserEntity;

import java.util.Optional;
import java.util.UUID;

public interface UserDao {
    Optional<UserEntity> find(UUID id);

    /**
     * Retrieves an existing user entity by its pseudo.
     *
     * @param pseudo The unique pseudo/username to look up
     * @return Optional containing the UserEntity if found, empty Optional otherwise
     */
    Optional<UserEntity> findByPseudo(String pseudo);

    UserEntity create(UserEntity user);
    void delete(UUID id);
    boolean isExisting(UUID id);
}
