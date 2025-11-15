package com.bortnik.bank_rest.repository;

import com.bortnik.bank_rest.entity.Card;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.stream.Stream;

@Repository
public interface CardRepository extends JpaRepository<Card, UUID> {

    Page<Card> findAllByUserId(UUID userId, Pageable pageable);

    @Query("""
    SELECT c FROM Card c
    WHERE c.expirationDate < CURRENT_DATE
    AND c.status != 'EXPIRED'
""")
    Stream<Card> findExpiredCards();
}
