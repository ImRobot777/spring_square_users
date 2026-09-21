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
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@Tag(name = "Gestion des Utilisateurs", description = "CRUD des utilisateurs et validation inter-services pour Square Games")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Créer un utilisateur", description = "Enregistre un nouvel utilisateur avec pseudo et email, et lui assigne un identifiant UUID unique.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Utilisateur créé avec succès"),
            @ApiResponse(responseCode = "400", description = "Données d'enregistrement invalides")
    })
    @PostMapping("/users")
    public UserEntity createUser(@RequestBody UserCreationParams requestParams) {
        return this.userService.createUser(requestParams);
    }

    @Operation(summary = "Consulter un utilisateur", description = "Recherche et retourne le profil d'un utilisateur à partir de son identifiant UUID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Utilisateur trouvé"),
            @ApiResponse(responseCode = "404", description = "Utilisateur introuvable pour l'UUID spécifié")
    })
    @GetMapping("/users/{userId}")
    public UserEntity getUser(
            @Parameter(description = "Identifiant UUID de l'utilisateur", required = true)
            @PathVariable UUID userId) {
        return this.userService.getUser(userId);
    }

    @Operation(summary = "Supprimer un utilisateur", description = "Supprime définitivement un compte utilisateur existant.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Utilisateur supprimé avec succès"),
            @ApiResponse(responseCode = "404", description = "Utilisateur introuvable pour l'UUID spécifié")
    })
    @DeleteMapping("/users/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(
            @Parameter(description = "Identifiant UUID de l'utilisateur à supprimer", required = true)
            @PathVariable UUID userId) {
        this.userService.deleteUser(userId);
    }

    @Operation(summary = "Vérifier la validité d'un utilisateur", description = "Endpoint léger dédié à la validation inter-services (Square Games -> Square Users).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Retourne true si le compte existe en base, false sinon")
    })
    @GetMapping("/users/{userId}/valid")
    public boolean isUserValid(
            @Parameter(description = "Identifiant UUID de l'utilisateur à vérifier", required = true)
            @PathVariable UUID userId) {
        return this.userService.isUserValid(userId);
    }

}
