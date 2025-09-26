package com.payment.Repository;

import com.payment.Payments.NetBankingMethods;
import com.payment.entity.NetBanking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NetBankingRepo extends JpaRepository<NetBanking,String> {

    NetBanking findByUsername(String username);

    NetBanking getNetBankingByUsername(String username);
}
