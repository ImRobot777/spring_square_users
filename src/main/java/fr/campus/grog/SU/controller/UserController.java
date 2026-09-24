package fr.campus.grog.SU.controller;

import fr.campus.grog.SU.dto.UserCreationParams;
import fr.campus.grog.SU.entity.UserEntity;
import fr.campus.grog.SU.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@Tag(name = "User Management", description = "CRUD operations for users and inter-service validation for Square Games")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Create a user", description = "Registers a new user with pseudo and email, and assigns a unique UUID identifier.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid registration data")
    })
    @PostMapping("/users")
    public UserEntity createUser(@RequestBody UserCreationParams requestParams) {
        return this.userService.createUser(requestParams);
    }

    @Operation(summary = "List all users", description = "Finds and retrieves all user profiles.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users")
    public List<UserEntity> getAllUsers(){
        return this.userService.getUsers();
    }

    @Operation(summary = "Get user by ID", description = "Finds and returns a user profile by their UUID identifier.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "404", description = "User not found for the specified UUID")
    })
    @PostAuthorize("hasRole('ADMIN') or returnObject.pseudo == authentication.name") //authentication <==> UsernamePasswordAuthenticationToken
    @GetMapping("/users/{userId}")
    public UserEntity getUser(
            @Parameter(description = "UUID identifier of the user", required = true)
            @PathVariable UUID userId) {
        return this.userService.getUser(userId);
    }

    @Operation(summary = "Delete a user", description = "Permanently deletes an existing user account.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User deleted successfully"),
            @ApiResponse(responseCode = "404", description = "User not found for the specified UUID")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/users/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(
            @Parameter(description = "UUID identifier of the user to delete", required = true)
            @PathVariable UUID userId) {
        this.userService.deleteUser(userId);
    }

    @Operation(summary = "Verify user validity", description = "Lightweight endpoint dedicated to inter-service validation (Square Games -> Square Users).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Returns true if the account exists in database, false otherwise")
    })
    @GetMapping("/users/{userId}/valid")
    public boolean isUserValid(
            @Parameter(description = "UUID identifier of the user to verify", required = true)
            @PathVariable UUID userId) {
        return this.userService.isUserValid(userId);
    }

}
