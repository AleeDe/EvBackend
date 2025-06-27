package com.org.repository;

import com.org.model.PaymentInvoice;
import org.bson.types.ObjectId;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
public interface PaymentInvoiceRepository extends MongoRepository<PaymentInvoice, ObjectId> {

    List<PaymentInvoice> findByUserId(ObjectId userId);

    List<PaymentInvoice> findBySessionId(ObjectId sessionId);

    Optional<PaymentInvoice> findByStripePaymentIntentId(String stripePaymentIntentId);
}
