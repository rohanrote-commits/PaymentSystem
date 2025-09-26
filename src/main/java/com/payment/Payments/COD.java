package com.payment.Payments;

import com.payment.Repository.TransactionRepo;
import com.payment.Repository.UserRepo;
import com.payment.dto.CompleteRequest;
import com.payment.dto.CompleteResponse;
import com.payment.dto.ValidateDto;
import com.payment.dto.ValidationReturnDto;
import com.payment.entity.PaymentTypes;
import com.payment.entity.Transaction;
import com.payment.entity.User;
import com.payment.exceptions.UserNotFoundException;
import com.payment.helper.InMemoryData;
import com.payment.helper.OTPGenerator;
import com.payment.helper.RequestUserContext;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Optional;

@Slf4j
@Component
public class COD implements PaymentMethods {
    @Autowired
    RequestUserContext requestUserContext;
    @Autowired
    UserRepo userRepo;

    @Autowired
    OTPGenerator otpGenerator;

    @Autowired
    TransactionRepo transactionRepo;
    @Autowired
    InMemoryData inMemoryData;


    @Override
    public ValidationReturnDto validatePaymentMethod(ValidateDto validateDto) throws UserNotFoundException {
        Optional<User> userOptional = userRepo.findUserByName(requestUserContext.getUsername());
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (!user.getMobileNumber().equals(validateDto.getMobileNo())) {
                throw new RuntimeException("Invalid mobile number");
            } else {
                log.info("Mobile number is valid for the request + " + requestUserContext.getUsername());
            }
            int otp = otpGenerator.generate3DigitOTP();
            Transaction transaction = initiateTransaction(validateDto, user.getAccountId());
            Transaction t = transactionRepo.save(transaction);
            log.info("Transaction Initiated and Validation completed for user " + user.getAccountId() + "with id : " + requestUserContext.hashCode());
            inMemoryData.otp.put(t.getTransactionId(), otp);


            return validationResponse(validateDto, t, otp);

        } else {
            throw new UserNotFoundException("Wrong User name passed");
        }

    }

    @Override
    public CompleteResponse completePaymentMethod(CompleteRequest completeRequest) throws UserNotFoundException {
        if (inMemoryData.otp.containsKey(completeRequest.getTransactionId())) {
            if (inMemoryData.otp.get(completeRequest.getTransactionId()).equals(completeRequest.getOtp())) {
                Transaction transaction = transactionRepo.findById(completeRequest.getTransactionId()).get();
                transaction.setStatus("Completed");
                transactionRepo.save(transaction);
                log.info("Transaction Completed for user " + requestUserContext.getUsername() + " request id : " + requestUserContext.hashCode());
                inMemoryData.otp.remove(completeRequest.getTransactionId());

                return completeResonse(transaction);
            } else {
                throw new RuntimeException("Invalid OTP");
            }
        } else {
            throw new RuntimeException("Invalid Transaction ID");
        }

    }


    @PostConstruct
    public void init() {
        log.info("COD service inserted in map");
        inMemoryData.payments.put(PaymentTypes.COD, this);
    }
}
