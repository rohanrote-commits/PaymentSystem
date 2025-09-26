package com.payment.Payments;

import com.payment.Repository.NetBankingRepo;
import com.payment.Repository.TransactionRepo;
import com.payment.Repository.UserRepo;
import com.payment.dto.CompleteRequest;
import com.payment.dto.CompleteResponse;
import com.payment.dto.ValidateDto;
import com.payment.dto.ValidationReturnDto;
import com.payment.entity.NetBanking;
import com.payment.entity.PaymentTypes;
import com.payment.entity.Transaction;
import com.payment.entity.User;
import com.payment.exceptions.InsufficientAmmountException;
import com.payment.exceptions.ResourceNotFound;
import com.payment.exceptions.UserNotFoundException;
import com.payment.helper.InMemoryData;
import com.payment.helper.OTPGenerator;
import com.payment.helper.RequestUserContext;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
public class NetBankingMethods implements PaymentMethods {
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

    @Autowired
    NetBankingRepo netBankingRepo;


    @Override
    public ValidationReturnDto validatePaymentMethod(ValidateDto validateDto) throws UserNotFoundException {
        Optional<User> userOptional = userRepo.findUserByName(requestUserContext.getUsername());
        if (userOptional.isPresent()) {
            User user = userOptional.get();

            Optional<NetBanking> netBanking1 = Optional.ofNullable(netBankingRepo.findByUsername(validateDto.getUsername()));

            if (netBanking1.isPresent()) {
                NetBanking netBanking = netBanking1.get();
                if (netBanking.getPassword().equals(validateDto.getPassword())) {
                    if (validateDto.getAmount() < netBanking.getBalance()) {
                        Transaction transaction = initiateTransaction(validateDto, user.getAccountId());
                        transaction.setAmount(validateDto.getAmount());
                        Transaction t = transactionRepo.save(transaction);
                        int otp = otpGenerator.generate3DigitOTP();
                        log.info("Transaction Initiated and Validation completed for user " + user.getAccountId() + "with id : " + requestUserContext.hashCode());
                        inMemoryData.otp.put(t.getTransactionId(), otp);
                        netBanking.setBalance(netBanking.getBalance() - validateDto.getAmount());
                        netBankingRepo.save(netBanking);
                        return validationResponse(validateDto, t, otp);
                    } else {
                        throw new InsufficientAmmountException("Insufficient funds");
                    }
                } else {
                    throw new ResourceNotFound("Wrong password");
                }
            } else {
                throw new ResourceNotFound("NetBanking account not found");
            }


        } else {
            throw new UserNotFoundException("Wrong User name passed");
        }

    }

    @PostConstruct
    public void init() {
        log.info("NetBanking service inserted in map");
        inMemoryData.payments.put(PaymentTypes.NB, this);
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
                throw new ResourceNotFound("Invalid OTP");
            }
        } else {
            throw new ResourceNotFound("Invalid Transaction ID");
        }
    }
}
