package com.payment.controller;


import com.payment.dto.CompleteRequest;
import com.payment.dto.CompleteResponse;
import com.payment.dto.ValidateDto;
import com.payment.dto.ValidationReturnDto;
import com.payment.exceptions.UserNotFoundException;
import com.payment.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pay")
public class Payment {
    @Autowired
    private PaymentService paymentService;

    @PostMapping("/initiate")
    public ResponseEntity<ValidationReturnDto> initiate(@RequestBody ValidateDto validateDto ) throws UserNotFoundException {
        System.out.println(validateDto.toString());
        return new ResponseEntity<>(paymentService.initiate(validateDto),HttpStatus.OK);
    }

    @PostMapping("/complete")
    public ResponseEntity<CompleteResponse> complete(@RequestBody CompleteRequest completeRequest) throws UserNotFoundException {
        return new ResponseEntity<>(paymentService.completePaymentMethod(completeRequest),HttpStatus.OK);
    }

}
