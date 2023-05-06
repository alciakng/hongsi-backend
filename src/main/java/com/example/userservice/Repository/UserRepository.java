package com.example.userservice.Repository;


import com.example.userservice.Model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface UserRepository extends JpaRepository<AppUser, Integer> {


    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    AppUser findByEmail(String email);
    @Transactional
    void deleteByUsername(String username);

}
