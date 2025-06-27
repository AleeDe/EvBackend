package com.org.dto;

import lombok.Data;
import org.bson.types.ObjectId;
@Data
public class PaymentRequest {
    private ObjectId sessionId;
}
