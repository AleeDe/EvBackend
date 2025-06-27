package com.org.service;

import com.org.dto.PaymentRequest;
import com.org.dto.StripeResponse;
import com.org.model.ChargingSession;
import com.org.model.PaymentInvoice;
import com.org.repository.ChargingSessionRepository;
import com.org.repository.PaymentInvoiceRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class StripeService {

    @Autowired
    private PaymentInvoiceRepository invoiceRepository;
    @Autowired
    private ChargingSessionRepository sessionRepository;

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    public StripeResponse createCheckoutSession(PaymentRequest paymentRequest) {

        Stripe.apiKey = stripeSecretKey;

        ChargingSession session = sessionRepository.findById(paymentRequest.getSessionId())
                .orElseThrow(() -> new RuntimeException("Charging session not found"));

        long amount = (long) (session.getCost() * 100); // Stripe uses cents/paisa

        // Line item
        SessionCreateParams.LineItem.PriceData.ProductData productData =
                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                        .setName("EV Charging - Session")
                        .build();

        SessionCreateParams.LineItem.PriceData priceData =
                SessionCreateParams.LineItem.PriceData.builder()
                        .setCurrency("pkr")
                        .setUnitAmount(amount)
                        .setProductData(productData)
                        .build();

        SessionCreateParams.LineItem item =
                SessionCreateParams.LineItem.builder()
                        .setPriceData(priceData)
                        .setQuantity(1L)
                        .build();

        // Session
        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl("http://localhost:5173/payment-success")  // Your frontend success page
                .setCancelUrl("http://localhost:5173/payment-cancel")    // Your frontend cancel page
                .addLineItem(item)
                .build();

        try {
            Session stripeSession = Session.create(params);

            // Save invoice
            PaymentInvoice invoice = PaymentInvoice.builder()
                    .sessionId(session.getId())
                    .userId(session.getUserId())
                    .amount(session.getCost())
                    .paymentMethod("Card") // Assuming card payment for simplicity
                    .status("Pending")
                    .stripePaymentIntentId(stripeSession.getId())
                    .createdAt(LocalDateTime.now())
                    .build();

            invoiceRepository.save(invoice);

            // Set payment status on session
            session.setPaymentStatus("Pending");
            sessionRepository.save(session);

            return StripeResponse.builder()
                    .status("Success")
                    .message("Stripe session created")
                    .sessionId(stripeSession.getId())
                    .sessionUrl(stripeSession.getUrl())
                    .build();

        } catch (StripeException e) {
            return StripeResponse.builder()
                    .status("Failure")
                    .message("Stripe error: " + e.getMessage())
                    .build();
        }
    }

    // Stripe webhook: confirm payment
    public void markPaymentAsPaid(String paymentIntentId) {
        PaymentInvoice invoice = invoiceRepository.findByStripePaymentIntentId(paymentIntentId)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        invoice.setStatus("Paid");
        invoiceRepository.save(invoice);

        ChargingSession session = sessionRepository.findById(invoice.getSessionId())
                .orElseThrow(() -> new RuntimeException("Session not found"));
        session.setPaymentStatus("Paid");
        sessionRepository.save(session);
    }
}
