package com.payment.controller;

import com.payment.dto.CompleteRequest;
import com.payment.dto.CompleteResponse;
import com.payment.dto.ValidateDto;
import com.payment.dto.ValidationReturnDto;
import com.payment.exceptions.UserNotFoundException;
import com.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pay")
@Tag(name = "Payments", description = "Endpoints to initiate and complete payments")
public class Payment {

    private final PaymentService paymentService;

    public Payment(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/initiate")
    @Operation(
            summary = "Initiate a payment",
            description = "Validates payment details and initiates the transaction",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Validation successful",
                            content = @Content(schema = @Schema(implementation = ValidationReturnDto.class))),
                    @ApiResponse(responseCode = "404", description = "User not found")
            }
    )
    @Parameters({
            @Parameter(
                    name = "User-Name",
                    description = "Username of the logged-in user",
                    required = true,
                    in = ParameterIn.HEADER
            )
    })
    public ResponseEntity<ValidationReturnDto> initiate(@RequestBody ValidateDto validateDto) throws UserNotFoundException {
        return ResponseEntity.ok(paymentService.initiate(validateDto));
    }



    @PostMapping("/complete")
    @Operation(
            summary = "Complete a payment",
            description = "Completes a previously initiated payment",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Payment completed",
                            content = @Content(schema = @Schema(implementation = CompleteResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Transaction or OTP not found")
            }
    )
    @Parameters({
            @Parameter(
                    name = "User-Name",
                    description = "Username of the logged-in user",
                    required = true,
                    in = ParameterIn.HEADER
            )
    })
    public ResponseEntity<CompleteResponse> complete(@RequestBody CompleteRequest completeRequest) throws UserNotFoundException {
        return ResponseEntity.ok(paymentService.completePaymentMethod(completeRequest));
    }
}
