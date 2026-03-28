package org.example.transportation.repository;

import org.example.transportation.dox.user.User;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {
    
    @Query("SELECT * FROM user WHERE username = :username")
    Optional<User> findByUsername(@Param("username") String username);
    
    @Query("SELECT * FROM user WHERE phone = :phone")
    Optional<User> findByPhone(@Param("phone") String phone);
    
    @Query("SELECT * FROM user WHERE email = :email")
    Optional<User> findByEmail(@Param("email") String email);
}
