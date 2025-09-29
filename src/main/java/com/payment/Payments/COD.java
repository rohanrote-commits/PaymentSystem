package com.payment.Payments;

import com.payment.Repository.TransactionRepo;
import com.payment.Repository.UserRepo;
import com.payment.dto.CompleteRequest;
import com.payment.dto.CompleteResponse;
import com.payment.dto.InitiateRequest;
import com.payment.dto.InitiateResponse;
import com.payment.entity.PaymentTypes;
import com.payment.entity.Transaction;
import com.payment.entity.User;
import com.payment.exceptions.PaymentServiceException;
import com.payment.exceptions.UserNotFoundException;
import com.payment.helper.InMemoryData;
import com.payment.helper.OTPGenerator;
import com.payment.helper.RequestUserContext;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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
    public InitiateResponse initiate(InitiateRequest initiateRequest) throws UserNotFoundException, PaymentServiceException {
        User user = userRepo.findUserByName(requestUserContext.getUsername())
                .orElseThrow(() -> new UserNotFoundException("Wrong User name passed"));

        if (!user.getMobileNumber().equals(initiateRequest.getMobileNo())) {
            log.error("Invalid mobile number for user {}", requestUserContext.getUsername());
            throw new PaymentServiceException("Invalid Mobile Number Exception", HttpStatus.BAD_REQUEST);
        }

        log.info("Mobile number validated for user {}", requestUserContext.getUsername());

        int otp = otpGenerator.generate3DigitOTP();
        Transaction transaction = createTransaction(initiateRequest, user.getAccountId());
        Transaction savedTransaction = transactionRepo.save(transaction);

        inMemoryData.otp.put(savedTransaction.getTransactionId(), otp);

        log.info("Transaction initiated and validation completed for user {} with request id: {}",
                user.getAccountId(), requestUserContext.hashCode());

        return validationResponse(initiateRequest, savedTransaction, otp);
    }

    @Override
    public CompleteResponse completePaymentMethod(CompleteRequest completeRequest) throws UserNotFoundException, PaymentServiceException {
        Integer transactionId = completeRequest.getTransactionId();

        if (!inMemoryData.otp.containsKey(transactionId)) {
            log.error("Invalid transaction ID: {}", transactionId);
            throw new PaymentServiceException("Invalid Transaction ID Exception",HttpStatus.BAD_REQUEST);
        }

        Integer expectedOtp = inMemoryData.otp.get(transactionId);
        if (!expectedOtp.equals(completeRequest.getOtp())) {
            log.error("Invalid OTP for transaction {}", transactionId);
            throw new PaymentServiceException("Invalid OTP Exception",HttpStatus.BAD_REQUEST);
        }

        Transaction transaction = transactionRepo.findById(transactionId)
                .orElseThrow(() -> new PaymentServiceException("Invalid Transaction ID", HttpStatus.BAD_REQUEST));

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
