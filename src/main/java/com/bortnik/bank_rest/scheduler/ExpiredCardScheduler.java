package com.bortnik.bank_rest.scheduler;

import com.bortnik.bank_rest.entity.Card;
import com.bortnik.bank_rest.entity.CardStatus;
import com.bortnik.bank_rest.repository.CardRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExpiredCardScheduler {

    private final CardRepository cardRepository;

    @Transactional
    @Scheduled(cron = "0 0 0 * * *")
    public void checkExpiredCard() {
        try (Stream<Card> stream = cardRepository.findExpiredCards()) {
            stream.forEach(it -> {
                it.setStatus(CardStatus.EXPIRED);
                it.setUpdatedAt(LocalDateTime.now());
            });
        }
        log.info("Expired cards have been updated");
    }
}
