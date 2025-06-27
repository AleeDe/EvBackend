package com.org.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.bson.types.ObjectId;
import java.time.LocalDateTime;

@Document
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentInvoice {
    @Id
    private ObjectId id;

    private ObjectId sessionId;
    private ObjectId userId;

    private double amount;
    private String stripePaymentIntentId;
    private String status; // Paid, Failed
    private LocalDateTime createdAt;
    private String paymentMethod; // Card, Bank Transfer, etc.
}
