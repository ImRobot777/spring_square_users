package fr.campus.grog.SU.service;

import fr.campus.grog.SU.dao.UserDao;
import fr.campus.grog.SU.dto.UserCreationParams;
import fr.campus.grog.SU.entity.UserEntity;
import fr.campus.grog.SU.entity.UserRole;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserDao userDao, PasswordEncoder passwordEncoder) {
        this.userDao = userDao;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserEntity createUser(UserCreationParams requestParams){
        UserEntity user = new UserEntity();
        user.id = UUID.randomUUID().toString();
        user.pseudo = requestParams.pseudo();
        user.email = requestParams.email();
        user.passwordHash = this.passwordEncoder.encode(requestParams.password()) ;
        user.role = UserRole.ROLE_USER.name();
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
