package fr.campus.grog.SU.service;

import fr.campus.grog.SU.entity.UserEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.List;

public class CustomUserDetails extends User { // Takes our UserEntity directly as argument !

    private final String userId;
    public CustomUserDetails(UserEntity user) {
        super(user.pseudo, user.passwordHash, List.of(new SimpleGrantedAuthority(user.role)));
        this.userId = user.id;
    }
    public String getUserId() {
        return userId;
    }
}
