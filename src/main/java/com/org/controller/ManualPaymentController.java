package com.org.controller;

import com.org.model.ChargingSession;
import com.org.model.PaymentInvoice;
import com.org.repository.ChargingSessionRepository;
import com.org.repository.PaymentInvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class ManualPaymentController {

    private final ChargingSessionRepository sessionRepository;
    private final PaymentInvoiceRepository invoiceRepository;

    // ✅ Create manual (cash) payment
    @PostMapping("/cash")
    public ResponseEntity<String> markCashPayment(@RequestParam String sessionId) {
        try {
            ChargingSession session = sessionRepository.findById(new ObjectId(sessionId))
                    .orElseThrow(() -> new RuntimeException("Session not found"));

            if ("Paid".equalsIgnoreCase(session.getPaymentStatus())) {
                return ResponseEntity.badRequest().body("Session already paid.");
            }

            // Mark session paid
            session.setPaymentStatus("Paid");
            sessionRepository.save(session);

            // Create invoice manually
            PaymentInvoice invoice = PaymentInvoice.builder()
                    .sessionId(session.getId())
                    .userId(session.getUserId())
                    .amount(session.getCost())
                    .paymentMethod("Cash")
                    .status("Paid")
                    .createdAt(LocalDateTime.now())
                    .build();

            invoiceRepository.save(invoice);

            return ResponseEntity.ok("Cash payment recorded successfully.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}
