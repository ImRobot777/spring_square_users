package fr.campus.grog.SU.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class UserEntity {
    @Id
    public String id;

    @Column(unique = true, nullable = false)
    public String pseudo;

    @Column(unique = true, nullable = false)
    public String email;

    public String passwordHash;
    public String role;

    // Constructor without argument : Mandatory for Hibernate !
    public UserEntity() {}
}
