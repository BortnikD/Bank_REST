package com.bortnik.bank_rest.repository;

import com.bortnik.bank_rest.entity.Card;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CardRepository extends JpaRepository<Card, UUID> {

    Optional<Card> findByCardNumber(String cardNumber);

    Optional<Card> findByUserIdAndCardNumber(UUID userId, String cardNumber);

    Page<Card> findAllByUserId(UUID userId, Pageable pageable);

    boolean existsByCardNumber(String cardNumber);
}
