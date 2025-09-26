package com.payment.service;


import com.payment.Payments.PaymentMethods;
import com.payment.dto.CompleteRequest;
import com.payment.dto.CompleteResponse;
import com.payment.dto.ValidateDto;
import com.payment.dto.ValidationReturnDto;
import com.payment.exceptions.UserNotFoundException;
import com.payment.helper.InMemoryData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {
@Autowired
InMemoryData inMemoryData;



public ValidationReturnDto initiate(ValidateDto validateDto) throws UserNotFoundException {
   PaymentMethods method = inMemoryData.payments.get(validateDto.getType());

    return method.validatePaymentMethod(validateDto);
}
public CompleteResponse completePaymentMethod(CompleteRequest completeRequest) throws UserNotFoundException {
    PaymentMethods method = inMemoryData.payments.get(completeRequest.getType());
    return method.completePaymentMethod(completeRequest);
}

}
