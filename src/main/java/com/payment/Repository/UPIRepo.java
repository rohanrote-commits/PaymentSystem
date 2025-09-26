package com.payment.Repository;

import com.payment.entity.UPI;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UPIRepo extends JpaRepository<UPI,String> {
    UPI findByUpiId(String upiId);

    UPI getUPIByUpiId(String upiId);
}
