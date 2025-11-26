package com.bortnik.bank_rest.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "cards")
@NoArgsConstructor
@AllArgsConstructor
@Getter
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
    @Setter
    CardStatus status;

    @Column(name = "card_number")
    String cardNumber;

    @Column(name = "last_four_digits")
    String lastFourDigits;

    @Column
    @Setter
    BigDecimal balance;

    @CreationTimestamp
    @Column(name = "created_at")
    LocalDateTime createdAt;

    @Column(name = "updated_at")
    @Setter
    LocalDateTime updatedAt;
}
