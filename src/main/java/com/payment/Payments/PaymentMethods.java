package com.payment.Payments;

import com.payment.dto.CompleteRequest;
import com.payment.dto.CompleteResponse;
import com.payment.dto.ValidateDto;
import com.payment.dto.ValidationReturnDto;
import com.payment.entity.PaymentTypes;
import com.payment.entity.Transaction;
import com.payment.exceptions.UserNotFoundException;
import com.payment.helper.RequestUserContext;
import org.springframework.beans.factory.annotation.Autowired;

public interface PaymentMethods {

    ValidationReturnDto validatePaymentMethod(ValidateDto validateDto) throws UserNotFoundException;

    CompleteResponse completePaymentMethod(CompleteRequest completeRequest) throws UserNotFoundException;

    default Transaction initiateTransaction(ValidateDto validateDto, long id) {

        Transaction transaction = new Transaction();
        transaction.setTransactionType(validateDto.getType());
        transaction.setAccountId(id);
        transaction.setStatus("Initiated");
        transaction.setAmount(validateDto.getAmount());
        return transaction;
    }

    default ValidationReturnDto validationResponse(ValidateDto validateDto, Transaction transaction, int otp) throws UserNotFoundException {
        ValidationReturnDto validationReturnDto = new ValidationReturnDto();
        validationReturnDto.setAmount(validateDto.getAmount());
        validationReturnDto.setTransactionID(transaction.getTransactionId());
        validationReturnDto.setUserAccountNo(transaction.getAccountId());
        validationReturnDto.setOtp(otp);

        return validationReturnDto;

    }

    default ValidationReturnDto validationResponse(ValidateDto validateDto, Transaction transaction) throws UserNotFoundException {
        ValidationReturnDto validationReturnDto = new ValidationReturnDto();
        validationReturnDto.setAmount(validateDto.getAmount());
        validationReturnDto.setTransactionID(transaction.getTransactionId());
        validationReturnDto.setUserAccountNo(transaction.getAccountId());

        return validationReturnDto;

    }

    default CompleteResponse completeResonse(Transaction transaction) throws UserNotFoundException {
        CompleteResponse completeResponse = new CompleteResponse();
        completeResponse.setAmount(transaction.getAmount());
        completeResponse.setTransactionId(transaction.getTransactionId());
        completeResponse.setStatus(transaction.getStatus());
        completeResponse.setType(transaction.getTransactionType());
        return completeResponse;
    }


}
