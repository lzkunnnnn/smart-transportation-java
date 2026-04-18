package org.example.transportation.repository;

import org.example.transportation.dox.visitor.Visitor;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VisitorRepository extends CrudRepository<Visitor, Long> {
    
    @Query("SELECT * FROM visitor WHERE username = :username")
    Optional<Visitor> findByUsername(@Param("username") String username);
    
    @Query("SELECT * FROM visitor WHERE phone = :phone")
    Optional<Visitor> findByPhone(@Param("phone") String phone);
    
    @Query("SELECT * FROM visitor WHERE email = :email")
    Optional<Visitor> findByEmail(@Param("email") String email);
}
