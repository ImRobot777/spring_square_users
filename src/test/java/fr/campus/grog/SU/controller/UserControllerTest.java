package fr.campus.grog.SU.controller;

import fr.campus.grog.SU.dto.UserCreationParams;
import fr.campus.grog.SU.entity.UserEntity;
import fr.campus.grog.SU.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        // Standalone setup: tests HTTP routing and controller logic without booting full Spring context
        UserController controller = new UserController(userService);
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void testCreateUser_ReturnsHttp200() throws Exception {
        UserEntity createdUser = new UserEntity();
        createdUser.id = UUID.randomUUID().toString();
        createdUser.pseudo = "Bob";
        createdUser.email = "bob@example.com";
        createdUser.passwordHash = "fakeHashPassword";

        when(userService.createUser(any(UserCreationParams.class))).thenReturn(createdUser);

        String jsonPayload = """
        {
            "pseudo": "Bob",
            "email": "bob@example.com",
            "password" : "fakeHashPassword"
        }
        """;

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pseudo").value("Bob"))
                .andExpect(jsonPath("$.email").value("bob@example.com"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist()); // Confirms hash exclusion;

        verify(userService).createUser(any(UserCreationParams.class));
    }

    @Test
    void testCreateUser_WhenInvalidParams_ReturnsHttp400() throws Exception {

        String jsonPayload1 = """
        {
            "pseudo": null,
            "email": "bob@example.com",
            "password" : "fakeHashPassword"
        }
        """;

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload1))
                        .andExpect(status().isBadRequest()); // Asserts HTTP 400 Bad Request

        String jsonPayload2 = """
        {
            "pseudo": "Bob",
            "email": "bob-at-invalid",
            "password" : "fakeHashPassword"
        }
        """;

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload2))
                .andExpect(status().isBadRequest()); // Asserts HTTP 400 Bad Request


        String jsonPayload3 = """
        {
            "pseudo": "Bob",
            "email": "bob@example.com",
            "password" : null
        }
        """;

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload3))
                .andExpect(status().isBadRequest()); // Asserts HTTP 400 Bad Request

        verifyNoInteractions(userService);
    }


    @Test
    void testGetAllUser_ReturnsHttp200() throws Exception {
        List<UserEntity> users = new ArrayList<>();
        UserEntity user =  new UserEntity();
        user.pseudo = "Bob";
        user.email = "bob@example.com";
        user.passwordHash = "fakeHashPassword";
        users.add(user);

        when(userService.getUsers()).thenReturn(users);

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].pseudo").value("Bob"))
                .andExpect(jsonPath("$[0].email").value("bob@example.com"))
                .andExpect(jsonPath("$[0].passwordHash").doesNotExist()); // Confirms hash exclusion;

        verify(userService).getUsers();
    }


    @Test
    void testGetUser_WhenExists_ReturnsHttp200() throws Exception {
        UUID userId = UUID.randomUUID();
        UserEntity user = new UserEntity();
        user.id = userId.toString();
        user.pseudo = "Bob";
        user.email = "bob@example.com";
        user.passwordHash = "fakeHashPassword";

        when(userService.getUser(userId)).thenReturn(user);

        mockMvc.perform(get("/users/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pseudo").value("Bob"))
                .andExpect(jsonPath("$.email").value("bob@example.com"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist()); // Confirms hash exclusion;

        verify(userService).getUser(userId);
    }

    @Test
    void testGetUser_WhenNotFound_ReturnsHttp404() throws Exception {
        UUID userId = UUID.randomUUID();
        when(userService.getUser(userId))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        mockMvc.perform(get("/users/{userId}", userId))
                .andExpect(status().isNotFound());

        verify(userService).getUser(userId);
    }

    @Test
    void testDeleteUser_WhenExists_ReturnsHttp204() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(delete("/users/{userId}", userId))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(userId);
    }

    @Test
    void testIsUserValid_WhenValid_ReturnsHttp200WithTrue() throws Exception {
        UUID userId = UUID.randomUUID();
        when(userService.isUserValid(userId)).thenReturn(true);

        mockMvc.perform(get("/users/{userId}/valid", userId))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(userService).isUserValid(userId);
    }
}
