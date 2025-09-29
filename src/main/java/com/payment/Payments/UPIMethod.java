package com.payment.Payments;

import com.payment.Repository.TransactionRepo;
import com.payment.Repository.UPIRepo;
import com.payment.Repository.UserRepo;
import com.payment.dto.CompleteRequest;
import com.payment.dto.CompleteResponse;
import com.payment.dto.ValidateDto;
import com.payment.dto.ValidationReturnDto;
import com.payment.entity.PaymentTypes;
import com.payment.entity.Transaction;
import com.payment.entity.UPI;
import com.payment.entity.User;
import com.payment.exceptions.InsufficientAmmountException;
import com.payment.exceptions.ResourceNotFound;
import com.payment.exceptions.UserNotFoundException;
import com.payment.helper.InMemoryData;
import com.payment.helper.RequestUserContext;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    public ValidationReturnDto validatePaymentMethod(ValidateDto validateDto) throws UserNotFoundException {
        User user = userRepo.findUserByName(requestUserContext.getUsername())
                .orElseThrow(() -> new UserNotFoundException("Wrong User name passed"));

        UPI upi = upiRepo.findByUpiId(validateDto.getUpiId());
        if (upi == null) {
            throw new ResourceNotFound("UPI account not found");
        }

        if (!upi.getPassword().equals(validateDto.getUpiPassword())) {
            throw new ResourceNotFound("Wrong password");
        }

        if (validateDto.getAmount() >= upi.getBalance()) {
            throw new InsufficientAmmountException("Insufficient funds");
        }

        Transaction transaction = initiateTransaction(validateDto, user.getAccountId());
        transaction.setAmount(validateDto.getAmount());
        Transaction savedTransaction = transactionRepo.save(transaction);

        upi.setBalance(upi.getBalance() - validateDto.getAmount());
        upiRepo.save(upi);

        log.info("Transaction initiated and validation completed for user {} with request id: {}",
                user.getAccountId(), requestUserContext.hashCode());

        // no OTP for UPI in your current logic
        return validationResponse(validateDto, savedTransaction);
    }

    @PostConstruct
    public void init() {
        log.info("UPI service registered in payments map");
        inMemoryData.payments.put(PaymentTypes.UPI, this);
    }

    @Override
    public CompleteResponse completePaymentMethod(CompleteRequest completeRequest) throws UserNotFoundException {
        Transaction transaction = transactionRepo.findById(completeRequest.getTransactionId())
                .orElseThrow(() -> new ResourceNotFound("Invalid Transaction ID"));

        transaction.setStatus("Completed");
        transactionRepo.save(transaction);

        log.info("Transaction completed for user {} with request id: {}",
                requestUserContext.getUsername(), requestUserContext.hashCode());

        return completeResonse(transaction);
    }
}
