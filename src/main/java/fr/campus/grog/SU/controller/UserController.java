package fr.campus.grog.SU.controller;


import fr.campus.grog.SU.dto.UserCreationParams;
import fr.campus.grog.SU.entity.UserEntity;
import fr.campus.grog.SU.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/users")
    public UserEntity createUser(@RequestBody UserCreationParams requestParams) {
        return this.userService.createUser(requestParams);
    }

    @GetMapping("/users/{userId}")
    public UserEntity getUser(@PathVariable UUID userId) {
        return this.userService.getUser(userId);
    }

    @DeleteMapping("/users/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT) // Optional: signals HTTP 204 (No Content)
    public void deleteUser(@PathVariable UUID userId) {
        this.userService.deleteUser(userId);
    }

    @GetMapping("/users/{userId}/valid")
    public boolean isUserValid(@PathVariable UUID userId) {
        return this.userService.isUserValid(userId);
    }

}
