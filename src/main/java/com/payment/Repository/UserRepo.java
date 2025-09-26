package com.payment.Repository;

import com.payment.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<User,Long> {
    List<User> getUserByMobileNumber(String mobileNumber);

    boolean existsByMobileNumber(String mobileNumber);

    boolean existsByEmail(String email);

    Optional<User> findUserByName(String username);
}
