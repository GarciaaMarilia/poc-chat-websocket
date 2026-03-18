package com.yourcaryourway.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@SQLRestriction("deleted_at IS NULL")   // filtre global — exclut les comptes supprimés
public class User {

    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false, unique = true)
    @Convert(converter = PiiEncryptionConverter.class)  // chiffrement PII au repos
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "first_name", nullable = false)
    @Convert(converter = PiiEncryptionConverter.class)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    @Convert(converter = PiiEncryptionConverter.class)
    private String lastName;

    @Column(name = "birth_date")
    @Convert(converter = PiiEncryptionConverter.class)
    private LocalDate birthDate;

    @Column
    @Convert(converter = PiiEncryptionConverter.class)
    private String phone;

    @Column(nullable = false, length = 10)
    private String locale;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Address> addresses;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DriverLicense> driverLicenses;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PaymentMethod> paymentMethods;

    @PrePersist
    void onCreate() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
