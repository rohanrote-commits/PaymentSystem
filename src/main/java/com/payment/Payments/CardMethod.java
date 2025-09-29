package com.payment.Payments;

import com.payment.Repository.CardRepo;
import com.payment.Repository.TransactionRepo;
import com.payment.Repository.UserRepo;
import com.payment.dto.CompleteRequest;
import com.payment.dto.CompleteResponse;
import com.payment.dto.InitiateRequest;
import com.payment.dto.InitiateResponse;
import com.payment.entity.*;
import com.payment.exceptions.PaymentServiceException;
import com.payment.exceptions.UserNotFoundException;
import com.payment.helper.InMemoryData;
import com.payment.helper.OTPGenerator;
import com.payment.helper.RequestUserContext;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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
    public InitiateResponse initiate(InitiateRequest initiateRequest) throws UserNotFoundException, PaymentServiceException {
        Optional<User> userOptional = userRepo.findUserByName(requestUserContext.getUsername());
        if (userOptional.isEmpty()) {
            log.error("Wrong username passed in header or User is not present in database");
            throw new UserNotFoundException("User not found");
        }

        User user = userOptional.get();
        Card card = cardRepo.findByCardNumber(initiateRequest.getCardNo());

        if (card == null) {
            log.error("Card not found in database");
            throw new PaymentServiceException("Card Not found in database", HttpStatus.BAD_REQUEST);
        }

        if (!card.getCvc().equals(initiateRequest.getCvc())) {
            log.error("Card has incorrect cvc");
            throw new PaymentServiceException("Card has incorrect cvc", HttpStatus.BAD_REQUEST);
        }

        log.debug("Card CVC validated");

        if (!card.getExp().equals(initiateRequest.getExp())) {
            log.error("Expiry date is wrong");
            throw new PaymentServiceException("Expiry date is wrong", HttpStatus.BAD_REQUEST);
        }

        log.debug("Card expiry validated");

        if (initiateRequest.getAmount() >= card.getBalance()) {
            log.error("Insufficient amount");
            throw new PaymentServiceException("Insufficient amount", HttpStatus.BAD_REQUEST);
        }

        log.debug("Sufficient balance, proceeding with transaction");

        Transaction transaction = createTransaction(initiateRequest, user.getAccountId());
        transaction.setAmount(initiateRequest.getAmount());
        Transaction savedTransaction = transactionRepo.save(transaction);

        int otp = otpGenerator.generate3DigitOTP();
        inMemoryData.otp.put(savedTransaction.getTransactionId(), otp);

        log.info("Transaction initiated and validation completed for user {} with request id: {}",
                user.getAccountId(), requestUserContext.hashCode());

        card.setBalance(card.getBalance() - initiateRequest.getAmount());
        cardRepo.save(card);
        InitiateResponse initiateResponse = new InitiateResponse();
        initiateResponse.setAmount(initiateRequest.getAmount());
        initiateResponse.setTransactionID(transaction.getTransactionId());
        initiateResponse.setUserAccountNo(transaction.getAccountId());
        initiateResponse.setOtp(otp);
        return initiateResponse;
    }

    @PostConstruct
    public void init() {
        log.info("CardMethod loaded into InMemoryData.payments map<PaymentType, CardMethod>");
        inMemoryData.payments.put(PaymentTypes.CARD, this);
    }

    @Override
    public CompleteResponse completePaymentMethod(CompleteRequest completeRequest) throws UserNotFoundException, PaymentServiceException {
        Integer transactionId = completeRequest.getTransactionId();

        if (!inMemoryData.otp.containsKey(transactionId)) {
            log.error("Incorrect transaction id/ transaction id is not present in database");
            throw new PaymentServiceException("Transaction ID is wrong",HttpStatus.BAD_REQUEST);
        }

        Integer expectedOtp = inMemoryData.otp.get(transactionId);
        if (!expectedOtp.equals(completeRequest.getOtp())) {
            log.error("Incorrect otp");
            throw new PaymentServiceException("Incorrect otp", HttpStatus.BAD_REQUEST);
        }

        log.debug("Correct OTP entered");

        Transaction transaction = transactionRepo.findById(transactionId)
                .orElseThrow(() -> new PaymentServiceException("Transaction not found", HttpStatus.NOT_FOUND));

        transaction.setStatus("Completed");
        transactionRepo.save(transaction);
        inMemoryData.otp.remove(transactionId);

        log.info("Transaction completed for user {} with request id: {}",
                requestUserContext.getUsername(), requestUserContext.hashCode());

        return completeResonse(transaction);
    }
}
