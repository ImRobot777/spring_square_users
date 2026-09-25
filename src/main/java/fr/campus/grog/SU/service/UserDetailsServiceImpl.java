package fr.campus.grog.SU.service;

import fr.campus.grog.SU.dao.UserDao;
import fr.campus.grog.SU.entity.UserEntity;
//import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Custom Spring Security UserDetailsService implementation.
 * Bridges Spring Security authentication mechanisms with our JPA UserDao persistence layer.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserDao userDao;

    public UserDetailsServiceImpl(UserDao userDao) {
        this.userDao = userDao;
    }

    /**
     * Locates the user based on the username (pseudo in our domain model).
     *
     * @param username The pseudo identifying the user whose data is required
     * @return Fully populated UserDetails object for authentication and authorization checks
     * @throws UsernameNotFoundException If the user could not be found in the database
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user = this.userDao.findByPseudo(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with pseudo: " + username));

        return new CustomUserDetails(user);
    }
}
