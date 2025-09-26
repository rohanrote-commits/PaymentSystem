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
import org.springframework.beans.factory.annotation.Autowired;
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
        if (userOptional.isPresent()) {
            log.debug("User is present in the database");
            User user = userOptional.get();

            Optional<Card> card1 = Optional.ofNullable(cardRepo.findByCardNumber(validateDto.getCardNo()));
            if (card1.isPresent()) {
                Card card = card1.get();
                if (card.getCvc().equals(validateDto.getCvc())) {
                    log.debug("Card has correct cvc");
                    if (card.getExp().equals(validateDto.getExp())) {
                        log.debug("Expiry date of card has been validated");
                        if (validateDto.getAmount() < card.getBalance()) {
                            log.debug("Sufficient balance, can proceed in transaction");
                            Transaction transaction = initiateTransaction(validateDto, user.getAccountId());
                            transaction.setAmount(validateDto.getAmount());
                            Transaction t = transactionRepo.save(transaction);
                            int otp = otpGenerator.generate3DigitOTP();
                            log.info("Transaction Initiated and Validation completed for user " + user.getAccountId() + "with id : " + requestUserContext.hashCode());
                            inMemoryData.otp.put(t.getTransactionId(), otp);

                            card.setBalance(card.getBalance() - validateDto.getAmount());
                            cardRepo.save(card);

                            return validationResponse(validateDto, t, otp);
                        } else {
                            log.error("Insufficient amount");
                            throw new InsufficientAmmountException("Insufficient ammount for the transaction");
                        }
                    } else {
                        log.error("Expiry date is wrong");
                        throw new ResourceNotFound("Exp is wrong");
                    }
                } else {
                    log.error("Card has incorrect cvc");
                    throw new ResourceNotFound("CVC is wrong");
                }
            } else {
                log.error("Card not found in database");
                throw new ResourceNotFound("Card not found");
            }


        } else {
            log.error("Wrong username passed in header or User is not present in database");
            throw new UserNotFoundException("Wrong User name passed");
        }
    }

    @PostConstruct
    public void init() {
        log.info("class of CardMethod loaded in InMemory.payments map<PaymentType,CardMethod>");
        inMemoryData.payments.put(PaymentTypes.CARD, this);
    }

    @Override
    public CompleteResponse completePaymentMethod(CompleteRequest completeRequest) throws UserNotFoundException {
        if (inMemoryData.otp.containsKey(completeRequest.getTransactionId())) {
            log.debug("Transaction is present in the database");
            if (inMemoryData.otp.get(completeRequest.getTransactionId()).equals(completeRequest.getOtp())) {
                log.debug("Correct otp is entered");
                Transaction transaction = transactionRepo.findById(completeRequest.getTransactionId()).get();
                transaction.setStatus("Completed");
                transactionRepo.save(transaction);
                log.info("Transaction Completed for user " + requestUserContext.getUsername() + " request id : " + requestUserContext.hashCode());
                inMemoryData.otp.remove(completeRequest.getTransactionId());

                return completeResonse(transaction);
            } else {
                log.error("Incorrect otp");

                throw new ResourceNotFound("OTP is wrong");
            }
        } else {
            log.error("Incorrect transaction id/ transaction id is not present in database");
            throw new ResourceNotFound("Transaction ID is wrong");
        }
    }
}
