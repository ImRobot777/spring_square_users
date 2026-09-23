package fr.campus.grog.SU.service;

import fr.campus.grog.SU.dao.UserDao;
import fr.campus.grog.SU.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UserDao userDao;

    private UserDetailsServiceImpl userDetailsService;

    @BeforeEach
    void setUp() {
        this.userDetailsService = new UserDetailsServiceImpl(this.userDao);
    }

    @Test
    void testLoadUserByUsername_WhenUserExists_ReturnsUserDetails() {
        // --- 1. ARRANGE ---
        UserEntity fakeUser = new UserEntity();
        fakeUser.pseudo = "Alice";
        fakeUser.passwordHash = "$2a$10$hashedPasswordValue";
        fakeUser.role = "ROLE_USER";

        when(this.userDao.findByPseudo("Alice")).thenReturn(Optional.of(fakeUser));

        // --- 2. ACT ---
        UserDetails userDetails = this.userDetailsService.loadUserByUsername("Alice");

        // --- 3. ASSERT ---
        assertNotNull(userDetails);
        assertEquals("Alice", userDetails.getUsername());
        assertEquals("$2a$10$hashedPasswordValue", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
        verify(this.userDao, times(1)).findByPseudo("Alice");
    }

    @Test
    void testLoadUserByUsername_WhenUserDoesNotExist_ThrowsUsernameNotFoundException() {
        // --- 1. ARRANGE ---
        when(this.userDao.findByPseudo("UnknownUser")).thenReturn(Optional.empty());

        // --- 2 & 3. ACT & ASSERT ---
        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> this.userDetailsService.loadUserByUsername("UnknownUser")
        );

        assertTrue(exception.getMessage().contains("UnknownUser"));
        verify(this.userDao, times(1)).findByPseudo("UnknownUser");
    }
}
