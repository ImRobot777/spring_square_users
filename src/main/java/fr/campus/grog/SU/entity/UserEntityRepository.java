package fr.campus.grog.SU.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserEntityRepository extends JpaRepository<UserEntity, String> {
    //Empty Class, SPRING + HIBERNATE
}
