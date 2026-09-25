package fr.campus.grog.SU.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserEntityRepository extends JpaRepository<UserEntity, String> {
    /**
     * Derives a SQL query to find a user entity by its pseudo.
     * Spring Data JPA translates this method name into: SELECT * FROM user_entity WHERE pseudo = ?
     *
     * @param pseudo The unique username/pseudo to search for
     * @return Optional containing the UserEntity if found, empty Optional otherwise
     */
    Optional<UserEntity> findByPseudo(String pseudo);

    /**
     * Derives a SQL query to find a user entity by its email.
     * Spring Data JPA translates this method name into: SELECT * FROM user_entity WHERE email = ?
     *
     * @param email The unique email to search for
     * @return Optional containing the UserEntity if found, empty Optional otherwise
     */
    Optional<UserEntity> findByEmail(String email);
}
