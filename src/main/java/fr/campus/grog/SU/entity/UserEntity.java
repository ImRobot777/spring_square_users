package fr.campus.grog.SU.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class UserEntity {
    @Id
    public String id;
    public String pseudo;
    public String email;

    // Constructor without argument : Mandatory for Hibernate !
    public UserEntity() {}
}
