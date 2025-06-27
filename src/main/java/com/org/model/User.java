package com.org.model;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDate;

@Document(collection = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    private ObjectId id;

    private String username;

    @JsonIgnore
    private String password;

    private String email;

    private String phoneNumber;

    private String role;

    private String cnic;

    private LocalDate dob;

    private LocalDate createdAt;

    private String address;

    private String profilePictureUrl;
}
