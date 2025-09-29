package com.payment.Payments;

import com.payment.Repository.TransactionRepo;
import com.payment.Repository.UPIRepo;
import com.payment.Repository.UserRepo;
import com.payment.dto.CompleteRequest;
import com.payment.dto.CompleteResponse;
import com.payment.dto.InitiateRequest;
import com.payment.dto.InitiateResponse;
import com.payment.entity.PaymentTypes;
import com.payment.entity.Transaction;
import com.payment.entity.UPI;
import com.payment.entity.User;
import com.payment.exceptions.PaymentServiceException;
import com.payment.exceptions.UserNotFoundException;
import com.payment.helper.InMemoryData;
import com.payment.helper.RequestUserContext;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UPIMethod implements PaymentMethods {

    private final RequestUserContext requestUserContext;
    private final UserRepo userRepo;
    private final TransactionRepo transactionRepo;
    private final InMemoryData inMemoryData;
    private final UPIRepo upiRepo;

    @Override
    public InitiateResponse initiate(InitiateRequest initiateRequest) throws UserNotFoundException, PaymentServiceException {
        User user = userRepo.findUserByName(requestUserContext.getUsername())
                .orElseThrow(() -> new UserNotFoundException("Wrong User name passed"));

        UPI upi = upiRepo.findByUpiId(initiateRequest.getUpiId());
        if (upi == null) {
            throw new PaymentServiceException("Invalid UPI ID", HttpStatus.BAD_REQUEST);
        }

        if (!upi.getPassword().equals(initiateRequest.getUpiPassword())) {
            throw new PaymentServiceException("Invalid UPI Password", HttpStatus.BAD_REQUEST);
        }

        if (initiateRequest.getAmount() >= upi.getBalance()) {
            throw new PaymentServiceException("Insufficient Balance", HttpStatus.BAD_REQUEST);
        }

        Transaction transaction = createTransaction(initiateRequest, user.getAccountId());
        transaction.setAmount(initiateRequest.getAmount());
        Transaction savedTransaction = transactionRepo.save(transaction);

        upi.setBalance(upi.getBalance() - initiateRequest.getAmount());
        upiRepo.save(upi);

        log.info("Transaction initiated and validation completed for user {} with request id: {}",
                user.getAccountId(), requestUserContext.hashCode());

        // no OTP for UPI in your current logic
        return validationResponse(initiateRequest, savedTransaction);
    }

    @PostConstruct
    public void init() {
        log.info("UPI service registered in payments map");
        inMemoryData.payments.put(PaymentTypes.UPI, this);
    }

    @Override
    public CompleteResponse completePaymentMethod(CompleteRequest completeRequest) throws UserNotFoundException, PaymentServiceException {
        Transaction transaction = transactionRepo.findById(completeRequest.getTransactionId())
                .orElseThrow(() -> new PaymentServiceException("Wrong transaction Id enetered",HttpStatus.NOT_FOUND));

        transaction.setStatus("Completed");
        transactionRepo.save(transaction);

        log.info("Transaction completed for user {} with request id: {}",
                requestUserContext.getUsername(), requestUserContext.hashCode());

        return completeResonse(transaction);
    }
}
