package com.yourcaryourway.projection;

import com.yourcaryourway.entity.Booking.Status;
import com.yourcaryourway.entity.Payment;
import com.yourcaryourway.entity.Vehicle.GearType;
import com.yourcaryourway.entity.Vehicle.FuelType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// ── R-01 : Résultat de recherche d'offres 

public interface OfferSearchResult {
    String       getOfferId();
    BigDecimal   getPrice();
    String       getCurrency();
    LocalDateTime getStartAt();
    LocalDateTime getEndAt();
    String       getAcrissCode();
    String       getBrand();
    String       getModel();
    Short        getYear();
    Byte         getSeats();
    GearType     getGearType();
    FuelType     getFuelType();
    String       getAgencyDepartureName();
    String       getDepartureCity();
    String       getAgencyReturnName();
    String       getReturnCity();
}


// ── R-02 : Historique des réservations d'un utilisateur 

public interface BookingHistoryResult {
    String          getBookingId();
    Status          getStatus();
    BigDecimal      getTotalAmount();
    String          getCurrency();
    LocalDateTime   getCreatedAt();
    LocalDateTime   getCancelledAt();
    LocalDateTime   getStartAt();
    LocalDateTime   getEndAt();
    String          getBrand();
    String          getModel();
    String          getAcrissCode();
    String          getAgencyDeparture();
    String          getAgencyReturn();
    Payment.Status  getPaymentStatus();
    LocalDateTime   getPaidAt();
}


// ── R-04 : Calcul du remboursement 

public interface RefundResult {
    String     getBookingId();
    BigDecimal getTotalAmount();
    LocalDateTime getStartAt();
    Integer    getDaysUntilStart();
    BigDecimal getRefundAmount();
    String     getRefundRate();   // "25%" ou "100%"
}


// ── R-05 : Liste des réservations pour l'API agence 

public interface AgencyBookingResult {
    String         getBookingId();
    Status         getStatus();
    BigDecimal     getTotalAmount();
    String         getCurrency();
    LocalDateTime  getCreatedAt();
    String         getFirstName();
    String         getLastName();
    String         getEmail();
    String         getBrand();
    String         getModel();
    LocalDateTime  getStartAt();
    LocalDateTime  getEndAt();
    String         getAgencyDeparture();
    String         getAgencyReturn();
    Payment.Status getPaymentStatus();
}


// ── R-08 : Statistiques tableau de bord 

public interface BookingStatsResult {
    Long       getTotalBookings();
    Long       getConfirmed();
    Long       getCompleted();
    Long       getCancelled();
    Long       getPending();
    BigDecimal getRevenueEur();
}
