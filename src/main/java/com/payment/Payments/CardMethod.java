package com.payment.Payments;

import com.payment.Repository.CardRepo;
import com.payment.Repository.NetBankingRepo;
import com.payment.Repository.TransactionRepo;
import com.payment.Repository.UserRepo;
import com.payment.dto.CompleteRequest;
import com.payment.dto.CompleteResponse;
import com.payment.dto.ValidateDto;
import com.payment.dto.ValidationReturnDto;
import com.payment.entity.*;
import com.payment.exceptions.InsufficientAmmountException;
import com.payment.exceptions.ResourceNotFound;
import com.payment.exceptions.UserNotFoundException;
import com.payment.helper.InMemoryData;
import com.payment.helper.OTPGenerator;
import com.payment.helper.RequestUserContext;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@AllArgsConstructor
@Slf4j
@Component
public class CardMethod implements PaymentMethods {

    private final RequestUserContext requestUserContext;
    private final UserRepo userRepo;
    private final OTPGenerator otpGenerator;
    private final TransactionRepo transactionRepo;
    private final InMemoryData inMemoryData;
    private final CardRepo cardRepo;

    @Override
    public ValidationReturnDto validatePaymentMethod(ValidateDto validateDto) throws UserNotFoundException {
        Optional<User> userOptional = userRepo.findUserByName(requestUserContext.getUsername());
        if (userOptional.isEmpty()) {
            log.error("Wrong username passed in header or User is not present in database");
            throw new UserNotFoundException("Wrong User name passed");
        }

        User user = userOptional.get();
        Card card = cardRepo.findByCardNumber(validateDto.getCardNo());

        if (card == null) {
            log.error("Card not found in database");
            throw new ResourceNotFound("Card not found");
        }

        if (!card.getCvc().equals(validateDto.getCvc())) {
            log.error("Card has incorrect cvc");
            throw new ResourceNotFound("CVC is wrong");
        }

        log.debug("Card CVC validated");

        if (!card.getExp().equals(validateDto.getExp())) {
            log.error("Expiry date is wrong");
            throw new ResourceNotFound("Exp is wrong");
        }

        log.debug("Card expiry validated");

        if (validateDto.getAmount() >= card.getBalance()) {
            log.error("Insufficient amount");
            throw new InsufficientAmmountException("Insufficient amount for the transaction");
        }

        log.debug("Sufficient balance, proceeding with transaction");

        Transaction transaction = initiateTransaction(validateDto, user.getAccountId());
        transaction.setAmount(validateDto.getAmount());
        Transaction savedTransaction = transactionRepo.save(transaction);

        int otp = otpGenerator.generate3DigitOTP();
        inMemoryData.otp.put(savedTransaction.getTransactionId(), otp);

        log.info("Transaction initiated and validation completed for user {} with request id: {}",
                user.getAccountId(), requestUserContext.hashCode());

        card.setBalance(card.getBalance() - validateDto.getAmount());
        cardRepo.save(card);

        return validationResponse(validateDto, savedTransaction, otp);
    }

    @PostConstruct
    public void init() {
        log.info("CardMethod loaded into InMemoryData.payments map<PaymentType, CardMethod>");
        inMemoryData.payments.put(PaymentTypes.CARD, this);
    }

    @Override
    public CompleteResponse completePaymentMethod(CompleteRequest completeRequest) throws UserNotFoundException {
        Integer transactionId = completeRequest.getTransactionId();

        if (!inMemoryData.otp.containsKey(transactionId)) {
            log.error("Incorrect transaction id/ transaction id is not present in database");
            throw new ResourceNotFound("Transaction ID is wrong");
        }

        Integer expectedOtp = inMemoryData.otp.get(transactionId);
        if (!expectedOtp.equals(completeRequest.getOtp())) {
            log.error("Incorrect otp");
            throw new ResourceNotFound("OTP is wrong");
        }

        log.debug("Correct OTP entered");

        Transaction transaction = transactionRepo.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFound("Transaction not found"));

        transaction.setStatus("Completed");
        transactionRepo.save(transaction);
        inMemoryData.otp.remove(transactionId);

        log.info("Transaction completed for user {} with request id: {}",
                requestUserContext.getUsername(), requestUserContext.hashCode());

        return completeResonse(transaction);
    }
}
