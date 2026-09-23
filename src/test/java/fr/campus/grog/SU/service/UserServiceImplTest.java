package fr.campus.grog.SU.service;

import fr.campus.grog.SU.dao.UserDao;
import fr.campus.grog.SU.dto.UserCreationParams;
import fr.campus.grog.SU.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Allow JUnit 5 activating Mockito
class UserServiceImplTest {
    @Mock
    private UserDao userDao; // Mockito automatically create the FAKE userDao
    @Mock
    private PasswordEncoder passwordEncoder; // Fake passwordEncoder
    private UserServiceImpl userService; // The REAL Service we want to test !
    @BeforeEach
    void setUp() {
        // Executed before each test : Fake DAO is injected in the REAL service
        this.userService = new UserServiceImpl(userDao, passwordEncoder);
    }

    @Test
    public void testGetUser_WhenUserExists_ReturnUser(){
        // AAA Method
        // --- 1. ARRANGE (Prepare mock and test fixtures) ---
        // Step 1a: Generate a random UUID
        UUID userId = UUID.randomUUID();
        // Step 1b: Instantiate a dummy UserEntity
        UserEntity fakeUser = new UserEntity();
        // Step 1c: Train the mock:
        when(userDao.find(userId)).thenReturn(Optional.of(fakeUser));

        // --- 2. ACT (Call the real method under test) ---
        UserEntity trueUser = userService.getUser(userId);

        // --- 3. ASSERT (Verify outcomes) ---
        assertNotNull(trueUser);
        assertEquals(fakeUser, trueUser);
    }

    @Test
    public void testGetUser_WhenUserDoesNotExist_ThrowsNotFoundException() {
        // --- 1. ARRANGE (Prepare mock) ---
        // Step 1a: Generate an unknown UUID
        UUID unknownId = UUID.randomUUID();
        // Step 1b: Train the mock to return an empty Optional (not found)
        when(userDao.find(unknownId)).thenReturn(Optional.empty());

        // --- 2 & 3. ACT & ASSERT (Expect exception) ---
        // assertThrows executes the lambda and expects a ResponseStatusException
        ResponseStatusException exception =
                assertThrows(
                    ResponseStatusException.class,
                    () -> userService.getUser(unknownId)
                );
        // Optional check: verify the HTTP status is 404 NOT_FOUND
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    public void testCreateUser_SetsFieldsAndDelegatesToDao() {
        // --- 1. ARRANGE ---
        UserCreationParams params = new UserCreationParams("Alice", "alice@example.com", "12345");
        UserEntity savedUser = new UserEntity();
        savedUser.id = UUID.randomUUID().toString();
        savedUser.pseudo = "Alice";
        savedUser.email = "alice@example.com";

        when(userDao.create(any(UserEntity.class))).thenReturn(savedUser);

        // --- 2. ACT ---
        UserEntity result = userService.createUser(params);

        // --- 3. ASSERT ---
        assertNotNull(result);
        assertEquals("Alice", result.pseudo);
        assertEquals("alice@example.com", result.email);
        verify(userDao, times(1)).create(any(UserEntity.class));
    }

    @Test
    public void testDeleteUser_WhenUserExists_CallsDaoDelete() {
        // --- 1. ARRANGE ---
        UUID userId = UUID.randomUUID();
        when(userDao.isExisting(userId)).thenReturn(true);

        // --- 2. ACT ---
        userService.deleteUser(userId);

        // --- 3. ASSERT ---
        verify(userDao, times(1)).delete(userId);
    }

    @Test
    public void testDeleteUser_WhenUserDoesNotExist_ThrowsNotFoundException() {
        // --- 1. ARRANGE ---
        UUID userId = UUID.randomUUID();
        when(userDao.isExisting(userId)).thenReturn(false);

        // --- 2 & 3. ACT & ASSERT ---
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> userService.deleteUser(userId)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(userDao, never()).delete(userId);
    }

    @Test
    public void testIsUserValid_WhenUserExists_ReturnsTrue() {
        // --- 1. ARRANGE ---
        UUID userId = UUID.randomUUID();
        when(userDao.isExisting(userId)).thenReturn(true);

        // --- 2. ACT ---
        boolean result = userService.isUserValid(userId);

        // --- 3. ASSERT ---
        assertTrue(result);
        verify(userDao, times(1)).isExisting(userId);
    }

    @Test
    public void testIsUserValid_WhenUserDoesNotExist_ReturnsFalse() {
        // --- 1. ARRANGE ---
        UUID userId = UUID.randomUUID();
        when(userDao.isExisting(userId)).thenReturn(false);

        // --- 2. ACT ---
        boolean result = userService.isUserValid(userId);

        // --- 3. ASSERT ---
        assertFalse(result);
        verify(userDao, times(1)).isExisting(userId);
    }

}