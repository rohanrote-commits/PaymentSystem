package com.payment.Payments;

import com.payment.Repository.NetBankingRepo;
import com.payment.Repository.TransactionRepo;
import com.payment.Repository.UPIRepo;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.text.html.Option;
import java.util.Optional;

@Slf4j
@Component
public class UPIMethod implements PaymentMethods{
    @Autowired
    RequestUserContext requestUserContext;

    @Autowired
    UserRepo userRepo;


    @Autowired
    TransactionRepo transactionRepo;

    @Autowired
    InMemoryData inMemoryData;

    @Autowired
    UPIRepo upiRepo;


    @Override
    public ValidationReturnDto validatePaymentMethod(ValidateDto validateDto) throws UserNotFoundException {
        Optional<User> userOptional = userRepo.findUserByName(requestUserContext.getUsername());
        if (userOptional.isPresent()) {
            User user = userOptional.get();

            Optional<UPI> upi1 = Optional.ofNullable(upiRepo.findByUpiId(validateDto.getUpiId()));
            if (upi1.isPresent()) {

                UPI upi = upi1.get();
                if(upi.getPassword().equals(validateDto.getUpiPassword())){
                    if(validateDto.getAmount() < upi.getBalance()){
                        Transaction transaction = initiateTransaction(validateDto,user.getAccountId());
                        transaction.setAmount(validateDto.getAmount());
                        Transaction t = transactionRepo.save(transaction);
                        log.info("Transaction Initiated and Validation completed for user " + user.getAccountId() +"with id : " +requestUserContext.hashCode());
                        upi.setBalance(upi.getBalance()-validateDto.getAmount());
                        upiRepo.save(upi);

                        return validationResponse(validateDto,t);
                    }else {
                        throw new InsufficientAmmountException("Insufficient funds");
                    }
                }else{
                    throw new ResourceNotFound("Wrong password");
                }
            }else{
                throw new ResourceNotFound("UPI account not found");
            }


        }else{
            throw new UserNotFoundException("Wrong User name passed");
        }

    }
@PostConstruct
public void init(){
        inMemoryData.payments.put(PaymentTypes.UPI,this);
}
    @Override
    public CompleteResponse completePaymentMethod(CompleteRequest completeRequest) throws UserNotFoundException {


                Transaction transaction = transactionRepo.findById(completeRequest.getTransactionId()).get();
                if(transaction != null) {
                    transaction.setStatus("Completed");

                    transactionRepo.save(transaction);
                    log.info("Transaction Completed for user " + requestUserContext.getUsername() + " request id : " + requestUserContext.hashCode());

                    return completeResonse(transaction);
                }else{
                    throw new ResourceNotFound("Invalid Transaction ID");
                }

    }


}
