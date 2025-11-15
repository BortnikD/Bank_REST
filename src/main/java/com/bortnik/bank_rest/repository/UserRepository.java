package com.bortnik.bank_rest.repository;

import com.bortnik.bank_rest.entity.Role;
import com.bortnik.bank_rest.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    Page<User> findAllByRole(Role role, Pageable pageable);
}
