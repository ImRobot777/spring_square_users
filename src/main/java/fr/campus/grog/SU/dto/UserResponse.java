package fr.campus.grog.SU.dto;

import fr.campus.grog.SU.entity.UserEntity;

/**
 * Immutable DTO delivering public user profile information.
 * Strips out sensitive persistence data such as password credentials.
 */
public record UserResponse(
        String id,
        String pseudo,
        String email,
        String role
) {
    public UserResponse(UserEntity userEntity) {
        this(userEntity.id, userEntity.pseudo, userEntity.email, userEntity.role);
    }
}
