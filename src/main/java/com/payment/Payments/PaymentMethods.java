package com.payment.Payments;

import com.payment.dto.CompleteRequest;
import com.payment.dto.CompleteResponse;
import com.payment.dto.InitiateRequest;
import com.payment.dto.InitiateResponse;
import com.payment.entity.Transaction;
import com.payment.exceptions.PaymentServiceException;
import com.payment.exceptions.UserNotFoundException;

public interface PaymentMethods {

    InitiateResponse initiate(InitiateRequest initiateRequest) throws UserNotFoundException, PaymentServiceException;

    CompleteResponse completePaymentMethod(CompleteRequest completeRequest) throws UserNotFoundException, PaymentServiceException;

    default Transaction createTransaction(InitiateRequest initiateRequest, long id) {

        Transaction transaction = new Transaction();
        transaction.setTransactionType(initiateRequest.getType());
        transaction.setAccountId(id);
        transaction.setStatus("Initiated");
        transaction.setAmount(initiateRequest.getAmount());
        return transaction;
    }

    default InitiateResponse validationResponse(InitiateRequest initiateRequest, Transaction transaction, int otp) throws UserNotFoundException {
        InitiateResponse initiateResponse = new InitiateResponse();
        initiateResponse.setAmount(initiateRequest.getAmount());
        initiateResponse.setTransactionID(transaction.getTransactionId());
        initiateResponse.setUserAccountNo(transaction.getAccountId());
        initiateResponse.setOtp(otp);

        return initiateResponse;

    }

    default InitiateResponse validationResponse(InitiateRequest initiateRequest, Transaction transaction) throws UserNotFoundException {
        InitiateResponse initiateResponse = new InitiateResponse();
        initiateResponse.setAmount(initiateRequest.getAmount());
        initiateResponse.setTransactionID(transaction.getTransactionId());
        initiateResponse.setUserAccountNo(transaction.getAccountId());

        return initiateResponse;

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
