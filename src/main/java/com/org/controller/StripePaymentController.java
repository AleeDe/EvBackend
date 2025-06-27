package com.org.controller;


import com.org.dto.PaymentRequest;
import com.org.dto.StripeResponse;
import com.org.service.StripeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class StripePaymentController {

    private final StripeService stripeService;

    // ✅ Create Stripe checkout session
    @PostMapping("/create")
    public ResponseEntity<StripeResponse> createPayment(@RequestBody PaymentRequest request) {
        StripeResponse response = stripeService.createCheckoutSession(request);
        return ResponseEntity.ok(response);
    }

}
