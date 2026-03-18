package com.yourcaryourway.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "bookings")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Booking {

    public enum Status {
        PENDING_PAYMENT, CONFIRMED, MODIFIED, CANCELLED, COMPLETED
    }

    @Id
    @Column(length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "offer_id", nullable = false)
    private Offer offer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    // ── Logique métier ──────────────────────────────────────

    /**
     * Vérifie si la réservation est modifiable.
     * Règle ADD : modification possible jusqu'à 48h avant le début.
     */
    public boolean isModifiable() {
        return (status == Status.CONFIRMED || status == Status.MODIFIED)
            && LocalDateTime.now().isBefore(offer.getStartAt().minusHours(48));
    }

    /**
     * Calcule le montant du remboursement en cas d'annulation.
     * Règle ADD : moins de 7 jours avant le début → 25% remboursé seulement.
     */
    public BigDecimal calculateRefund() {
        boolean lessThan7Days = LocalDateTime.now()
            .isAfter(offer.getStartAt().minusDays(7));

        return lessThan7Days
            ? totalAmount.multiply(BigDecimal.valueOf(0.25))
            : totalAmount;
    }

    @PrePersist
    void onCreate() {
        this.id        = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status    = Status.PENDING_PAYMENT;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
