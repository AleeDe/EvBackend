package com.org.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class SignupRequest {

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;
    private String phoneNumber;
    private String email;
    @JsonIgnore
    @NotBlank(message = "Password is required")
    private String role;
    private String cnic;
    private LocalDate dob;
    private LocalDate createdAt = LocalDate.now();
    private String profilePictureUrl;
    private String address;
}