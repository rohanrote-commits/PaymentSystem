package com.payment.Repository;

import com.payment.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CardRepo extends JpaRepository<Card,Long> {
    Card findByCardNumber(Long cardNo);

    Card getCardByCardNumber(Long cardNo);
}
