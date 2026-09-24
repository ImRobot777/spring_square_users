package fr.campus.grog.SU.dao;

import fr.campus.grog.SU.entity.UserEntity;
import fr.campus.grog.SU.entity.UserEntityRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@Primary
public class JpaUserDao implements UserDao{

    private final UserEntityRepository repository;

    public JpaUserDao(UserEntityRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserEntity create(UserEntity user) {
        return this.repository.save(user);
    }

    @Override
    public Optional<UserEntity> find(UUID userId) {
        return this.repository.findById(userId.toString());
    }

    @Override
    public Optional<UserEntity> findByPseudo(String pseudo) {
        // Delegates directly to Spring Data JPA derived query method
        return this.repository.findByPseudo(pseudo);
    }

    @Override
    public List<UserEntity> findAllUsers(){
        return this.repository.findAll();
    }

    @Override
    public void delete(UUID userId) {
        this.repository.deleteById(userId.toString());
    }

    @Override
    public boolean isExisting(UUID userId) {
        return this.repository.existsById(userId.toString());
    }


}
