package com.payment.Payments;

import com.payment.Repository.NetBankingRepo;
import com.payment.Repository.TransactionRepo;
import com.payment.Repository.UserRepo;
import com.payment.dto.CompleteRequest;
import com.payment.dto.CompleteResponse;
import com.payment.dto.InitiateRequest;
import com.payment.dto.InitiateResponse;
import com.payment.entity.NetBanking;
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
public class NetBankingMethods implements PaymentMethods {

    private final RequestUserContext requestUserContext;
    private final UserRepo userRepo;
    private final OTPGenerator otpGenerator;
    private final TransactionRepo transactionRepo;
    private final InMemoryData inMemoryData;
    private final NetBankingRepo netBankingRepo;

    @Override
    public InitiateResponse initiate(InitiateRequest initiateRequest) throws UserNotFoundException, PaymentServiceException {
        User user = userRepo.findUserByName(requestUserContext.getUsername())
                .orElseThrow(() -> new UserNotFoundException("Wrong User name passed"));

        NetBanking netBanking = netBankingRepo.findByUsername(initiateRequest.getUsername());
        if (netBanking == null) {
            throw new PaymentServiceException("Net Banking account not found", HttpStatus.NOT_FOUND);
        }

        if (!netBanking.getPassword().equals(initiateRequest.getPassword())) {
            throw new PaymentServiceException("Invalid password", HttpStatus.BAD_REQUEST);
        }

        if (initiateRequest.getAmount() >= netBanking.getBalance()) {
            throw new PaymentServiceException("Insufficient balance", HttpStatus.BAD_REQUEST);
        }

        Transaction transaction = createTransaction(initiateRequest, user.getAccountId());
        transaction.setAmount(initiateRequest.getAmount());
        Transaction savedTransaction = transactionRepo.save(transaction);

        int otp = otpGenerator.generate3DigitOTP();
        inMemoryData.otp.put(savedTransaction.getTransactionId(), otp);

        netBanking.setBalance(netBanking.getBalance() - initiateRequest.getAmount());
        netBankingRepo.save(netBanking);

        log.info("Transaction initiated and validation completed for user {} with request id: {}",
                user.getAccountId(), requestUserContext.hashCode());

        return validationResponse(initiateRequest, savedTransaction, otp);
    }

    @PostConstruct
    public void init() {
        log.info("NetBanking service registered in payments map");
        inMemoryData.payments.put(PaymentTypes.NB, this);
    }

    @Override
    public CompleteResponse completePaymentMethod(CompleteRequest completeRequest) throws UserNotFoundException, PaymentServiceException {
        Integer transactionId = completeRequest.getTransactionId();

        if (!inMemoryData.otp.containsKey(transactionId)) {
            throw new PaymentServiceException("Invalid Transaction Id entered",HttpStatus.NOT_FOUND);
        }

        Integer expectedOtp = inMemoryData.otp.get(transactionId);
        if (!expectedOtp.equals(completeRequest.getOtp())) {
            throw new PaymentServiceException("Incorrect otp", HttpStatus.BAD_REQUEST);
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
}
