package com.yourcaryourway.service;

import com.yourcaryourway.entity.Booking;
import com.yourcaryourway.entity.Offer;
import com.yourcaryourway.projection.*;
import com.yourcaryourway.repository.BookingRepository;
import com.yourcaryourway.repository.OfferRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final OfferRepository   offerRepository;

    // ── R-01 : Recherche d'offres disponibles ────────────────────────────

    public List<OfferSearchResult> searchOffers(
            String city,
            LocalDateTime startAt,
            LocalDateTime endAt,
            String acrissCategory  // null si pas de filtre
    ) {
        if (acrissCategory != null) {
            return offerRepository.findAvailableOffersByCategory(
                city, startAt, endAt, acrissCategory.substring(0, 2).toUpperCase()
            );
        }
        return offerRepository.findAvailableOffers(city, startAt, endAt);
    }

    // ── R-02 : Historique des réservations ──────────────────────────────

    public List<BookingHistoryResult> getBookingHistory(String userId) {
        return bookingRepository.findBookingHistoryByUserId(userId);
    }

    // ── R-03 : Vérification et modification d'une réservation ───────────

    @Transactional
    public Booking modifyBooking(String bookingId, String userId, Offer newOffer) {
        Booking booking = bookingRepository.findByIdAndUserId(bookingId, userId)
            .orElseThrow(() -> new IllegalArgumentException("Réservation introuvable"));

        // Règle ADD : 48h avant le début
        if (!booking.isModifiable()) {
            throw new IllegalStateException(
                "Modification impossible — délai de 48h dépassé ou statut invalide"
            );
        }

        // Libérer l'ancienne offre
        booking.getOffer().markAsReserved(); // annule la marque RESERVED

        // Réserver la nouvelle offre
        if (!newOffer.isAvailable()) {
            throw new IllegalStateException("La nouvelle offre n'est plus disponible");
        }
        newOffer.markAsReserved();

        booking.setOffer(newOffer);
        booking.setTotalAmount(newOffer.getPrice());
        booking.setStatus(Booking.Status.MODIFIED);

        log.info("Réservation {} modifiée par l'utilisateur {}", bookingId, userId);
        return bookingRepository.save(booking);
    }

    // ── R-04 : Annulation avec calcul du remboursement ──────────────────

    @Transactional
    public BigDecimal cancelBooking(String bookingId, String userId) {
        Booking booking = bookingRepository.findByIdAndUserId(bookingId, userId)
            .orElseThrow(() -> new IllegalArgumentException("Réservation introuvable"));

        if (booking.getStatus() == Booking.Status.CANCELLED) {
            throw new IllegalStateException("Réservation déjà annulée");
        }

        // Règle ADD : calcul remboursement (logique dans l'entité Booking)
        BigDecimal refund = booking.calculateRefund();

        booking.setStatus(Booking.Status.CANCELLED);
        booking.setCancelledAt(LocalDateTime.now());
        booking.getOffer().setStatus(Offer.Status.AVAILABLE); // libère l'offre

        bookingRepository.save(booking);

        log.info("Réservation {} annulée — remboursement : {} {}",
            bookingId, refund, booking.getCurrency());

        return refund;
    }

    // ── R-04 via projection SQL — alternative ───────────────────────────

    public RefundResult getRefundPreview(String bookingId) {
        return bookingRepository.calculateRefund(bookingId)
            .orElseThrow(() -> new IllegalArgumentException("Réservation introuvable"));
    }

    // ── R-05 : API Agence ────────────────────────────────────────────────

    public List<AgencyBookingResult> getAllBookingsForAgency() {
        return bookingRepository.findAllForAgency();
    }

    // ── R-08 : Statistiques ──────────────────────────────────────────────

    public BookingStatsResult getStats() {
        return bookingRepository.getStatsLast30Days();
    }
}
