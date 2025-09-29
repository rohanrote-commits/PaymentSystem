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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class COD implements PaymentMethods {

    private final RequestUserContext requestUserContext;
    private final UserRepo userRepo;
    private final OTPGenerator otpGenerator;
    private final TransactionRepo transactionRepo;
    private final InMemoryData inMemoryData;

    @Override
    public ValidationReturnDto validatePaymentMethod(ValidateDto validateDto) throws UserNotFoundException {
        User user = userRepo.findUserByName(requestUserContext.getUsername())
                .orElseThrow(() -> new UserNotFoundException("Wrong User name passed"));

        if (!user.getMobileNumber().equals(validateDto.getMobileNo())) {
            log.error("Invalid mobile number for user {}", requestUserContext.getUsername());
            throw new RuntimeException("Invalid mobile number");
        }

        log.info("Mobile number validated for user {}", requestUserContext.getUsername());

        int otp = otpGenerator.generate3DigitOTP();
        Transaction transaction = initiateTransaction(validateDto, user.getAccountId());
        Transaction savedTransaction = transactionRepo.save(transaction);

        inMemoryData.otp.put(savedTransaction.getTransactionId(), otp);

        log.info("Transaction initiated and validation completed for user {} with request id: {}",
                user.getAccountId(), requestUserContext.hashCode());

        return validationResponse(validateDto, savedTransaction, otp);
    }

    @Override
    public CompleteResponse completePaymentMethod(CompleteRequest completeRequest) throws UserNotFoundException {
        Integer transactionId = completeRequest.getTransactionId();

        if (!inMemoryData.otp.containsKey(transactionId)) {
            log.error("Invalid transaction ID: {}", transactionId);
            throw new RuntimeException("Invalid Transaction ID");
        }

        Integer expectedOtp = inMemoryData.otp.get(transactionId);
        if (!expectedOtp.equals(completeRequest.getOtp())) {
            log.error("Invalid OTP for transaction {}", transactionId);
            throw new RuntimeException("Invalid OTP");
        }

        Transaction transaction = transactionRepo.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        transaction.setStatus("Completed");
        transactionRepo.save(transaction);
        inMemoryData.otp.remove(transactionId);

        log.info("Transaction completed for user {} with request id: {}",
                requestUserContext.getUsername(), requestUserContext.hashCode());

        return completeResonse(transaction);
    }

    @PostConstruct
    public void init() {
        log.info("COD service registered in payments map");
        inMemoryData.payments.put(PaymentTypes.COD, this);
    }
}
