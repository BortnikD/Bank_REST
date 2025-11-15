package com.bortnik.bank_rest.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "cards")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    UUID id;

    @Column(name = "user_id")
    UUID userId;

    @Column(name = "expiration_date")
    LocalDate expirationDate;

    @Column
    @Enumerated(EnumType.STRING)
    CardStatus status;

    @Column(name = "card_number")
    String cardNumber;

    @Column(name = "last_four_digits")
    String lastFourDigits;

    @Column
    BigDecimal balance;

    @CreationTimestamp
    @Column(name = "created_at")
    LocalDateTime createdAt;

    @Column(name = "updated_at")
    LocalDateTime updatedAt;
}
