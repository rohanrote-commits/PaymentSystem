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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NetBankingMethods implements PaymentMethods {

    private final RequestUserContext requestUserContext;
    private final UserRepo userRepo;
    private final OTPGenerator otpGenerator;
    private final TransactionRepo transactionRepo;
    private final InMemoryData inMemoryData;
    private final NetBankingRepo netBankingRepo;

    @Override
    public ValidationReturnDto validatePaymentMethod(ValidateDto validateDto) throws UserNotFoundException {
        User user = userRepo.findUserByName(requestUserContext.getUsername())
                .orElseThrow(() -> new UserNotFoundException("Wrong User name passed"));

        NetBanking netBanking = netBankingRepo.findByUsername(validateDto.getUsername());
        if (netBanking == null) {
            throw new ResourceNotFound("NetBanking account not found");
        }

        if (!netBanking.getPassword().equals(validateDto.getPassword())) {
            throw new ResourceNotFound("Wrong password");
        }

        if (validateDto.getAmount() >= netBanking.getBalance()) {
            throw new InsufficientAmmountException("Insufficient funds");
        }

        Transaction transaction = initiateTransaction(validateDto, user.getAccountId());
        transaction.setAmount(validateDto.getAmount());
        Transaction savedTransaction = transactionRepo.save(transaction);

        int otp = otpGenerator.generate3DigitOTP();
        inMemoryData.otp.put(savedTransaction.getTransactionId(), otp);

        netBanking.setBalance(netBanking.getBalance() - validateDto.getAmount());
        netBankingRepo.save(netBanking);

        log.info("Transaction initiated and validation completed for user {} with request id: {}",
                user.getAccountId(), requestUserContext.hashCode());

        return validationResponse(validateDto, savedTransaction, otp);
    }

    @PostConstruct
    public void init() {
        log.info("NetBanking service registered in payments map");
        inMemoryData.payments.put(PaymentTypes.NB, this);
    }

    @Override
    public CompleteResponse completePaymentMethod(CompleteRequest completeRequest) throws UserNotFoundException {
        Integer transactionId = completeRequest.getTransactionId();

        if (!inMemoryData.otp.containsKey(transactionId)) {
            throw new ResourceNotFound("Invalid Transaction ID");
        }

        Integer expectedOtp = inMemoryData.otp.get(transactionId);
        if (!expectedOtp.equals(completeRequest.getOtp())) {
            throw new ResourceNotFound("Invalid OTP");
        }

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
