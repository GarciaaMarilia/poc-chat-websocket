package com.yourcaryourway.repository;

import com.yourcaryourway.entity.Booking;
import com.yourcaryourway.entity.Booking.Status;
import com.yourcaryourway.projection.BookingHistoryResult;
import com.yourcaryourway.projection.AgencyBookingResult;
import com.yourcaryourway.projection.RefundResult;
import com.yourcaryourway.projection.BookingStatsResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, String> {

    // ── R-02 : Historique des réservations d'un utilisateur ──────────────

    /**
     * Retourne toutes les réservations d'un utilisateur, de la plus récente
     * à la plus ancienne, avec toutes les informations nécessaires à l'affichage.
     */
    @Query("""
        SELECT
            b.id             AS bookingId,
            b.status         AS status,
            b.totalAmount    AS totalAmount,
            b.currency       AS currency,
            b.createdAt      AS createdAt,
            b.cancelledAt    AS cancelledAt,
            o.startAt        AS startAt,
            o.endAt          AS endAt,
            v.brand          AS brand,
            v.model          AS model,
            v.acrissCode     AS acrissCode,
            ad.name          AS agencyDeparture,
            ar.name          AS agencyReturn,
            p.status         AS paymentStatus,
            p.paidAt         AS paidAt
        FROM Booking b
        JOIN b.offer o
        JOIN o.vehicle v
        JOIN o.agencyDeparture ad
        JOIN o.agencyReturn ar
        LEFT JOIN Payment p ON p.booking = b
        WHERE b.user.id = :userId
        ORDER BY b.createdAt DESC
    """)
    List<BookingHistoryResult> findBookingHistoryByUserId(@Param("userId") String userId);

    // ── R-03 : Vérification règle de modification (48h) ──────────────────

    /**
     * Méthode dérivée — Spring génère la query automatiquement.
     * Utilisée pour charger la réservation avant d'appeler booking.isModifiable().
     */
    Optional<Booking> findByIdAndUserId(String id, String userId);

    // ── R-04 : Calcul du remboursement ────────────────────────────────────

    /**
     * Retourne les données nécessaires au calcul du remboursement.
     * La logique métier (25% vs 100%) est dans Booking.calculateRefund().
     *
     * Utilise SQL natif pour DATEDIFF (fonction MySQL spécifique).
     */
    @Query(value = """
        SELECT
            b.id                                                      AS bookingId,
            b.total_amount                                            AS totalAmount,
            o.start_at                                                AS startAt,
            DATEDIFF(o.start_at, NOW())                               AS daysUntilStart,
            CASE
                WHEN DATEDIFF(o.start_at, NOW()) < 7
                THEN ROUND(b.total_amount * 0.25, 2)
                ELSE b.total_amount
            END                                                       AS refundAmount,
            CASE
                WHEN DATEDIFF(o.start_at, NOW()) < 7
                THEN '25%'
                ELSE '100%'
            END                                                       AS refundRate
        FROM bookings b
        JOIN offers o ON o.id = b.offer_id
        WHERE b.id = :bookingId
    """, nativeQuery = true)
    Optional<RefundResult> calculateRefund(@Param("bookingId") String bookingId);

    // ── R-05 : API Agence — liste des réservations ────────────────────────

    /**
     * Retourne les réservations pour le tableau de bord de l'agence.
     * Accessible uniquement via l'API agence (OAuth2 client credentials).
     */
    @Query("""
        SELECT
            b.id             AS bookingId,
            b.status         AS status,
            b.totalAmount    AS totalAmount,
            b.currency       AS currency,
            b.createdAt      AS createdAt,
            u.firstName      AS firstName,
            u.lastName       AS lastName,
            u.email          AS email,
            v.brand          AS brand,
            v.model          AS model,
            o.startAt        AS startAt,
            o.endAt          AS endAt,
            ad.name          AS agencyDeparture,
            ar.name          AS agencyReturn,
            p.status         AS paymentStatus
        FROM Booking b
        JOIN b.user u
        JOIN b.offer o
        JOIN o.vehicle v
        JOIN o.agencyDeparture ad
        JOIN o.agencyReturn ar
        LEFT JOIN Payment p ON p.booking = b
        ORDER BY b.createdAt DESC
    """)
    List<AgencyBookingResult> findAllForAgency();

    // ── R-08 : Statistiques tableau de bord ──────────────────────────────

    /**
     * Statistiques des 30 derniers jours pour le dashboard agence.
     * Utilise SQL natif pour les fonctions d'agrégation conditionnelles.
     */
    @Query(value = """
        SELECT
            COUNT(*)                                                   AS totalBookings,
            SUM(CASE WHEN status = 'CONFIRMED'       THEN 1 ELSE 0 END) AS confirmed,
            SUM(CASE WHEN status = 'COMPLETED'       THEN 1 ELSE 0 END) AS completed,
            SUM(CASE WHEN status = 'CANCELLED'       THEN 1 ELSE 0 END) AS cancelled,
            SUM(CASE WHEN status = 'PENDING_PAYMENT' THEN 1 ELSE 0 END) AS pending,
            ROUND(SUM(
                CASE WHEN status IN ('CONFIRMED','COMPLETED') AND currency = 'EUR'
                     THEN total_amount ELSE 0 END
            ), 2) AS revenueEur
        FROM bookings
        WHERE created_at >= DATE_SUB(NOW(), INTERVAL 30 DAY)
    """, nativeQuery = true)
    BookingStatsResult getStatsLast30Days();

    // ── Méthodes dérivées simples ─────────────────────────────────────────

    List<Booking> findByUserIdAndStatus(String userId, Status status);

    boolean existsByIdAndUserId(String id, String userId);
}
