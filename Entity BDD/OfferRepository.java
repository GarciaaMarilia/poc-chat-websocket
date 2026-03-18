package com.yourcaryourway.repository;

import com.yourcaryourway.entity.Offer;
import com.yourcaryourway.projection.OfferSearchResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OfferRepository extends JpaRepository<Offer, String> {

    /**
     * R-01 — Recherche d'offres disponibles.
     * Équivalent de la query SQL R-01.
     *
     * Utilise une projection OfferSearchResult pour éviter
     * de charger toutes les relations inutiles.
     */
    @Query("""
        SELECT
            o.id               AS offerId,
            o.price            AS price,
            o.currency         AS currency,
            o.startAt          AS startAt,
            o.endAt            AS endAt,
            v.acrissCode       AS acrissCode,
            v.brand            AS brand,
            v.model            AS model,
            v.year             AS year,
            v.seats            AS seats,
            v.gearType         AS gearType,
            v.fuelType         AS fuelType,
            ad.name            AS agencyDepartureName,
            ad.city            AS departureCity,
            ar.name            AS agencyReturnName,
            ar.city            AS returnCity
        FROM Offer o
        JOIN o.vehicle v
        JOIN o.agencyDeparture ad
        JOIN o.agencyReturn ar
        WHERE o.status    = 'AVAILABLE'
        AND   ad.city     = :city
        AND   o.startAt  >= :startAt
        AND   o.endAt    <= :endAt
        ORDER BY o.price ASC
    """)
    List<OfferSearchResult> findAvailableOffers(
        @Param("city")    String city,
        @Param("startAt") LocalDateTime startAt,
        @Param("endAt")   LocalDateTime endAt
    );

    /**
     * R-01 avec filtre optionnel sur la catégorie ACRISS (2 premiers caractères).
     * Ex: "EC" pour économique, "CC" pour compacte.
     */
    @Query("""
        SELECT
            o.id               AS offerId,
            o.price            AS price,
            o.currency         AS currency,
            o.startAt          AS startAt,
            o.endAt            AS endAt,
            v.acrissCode       AS acrissCode,
            v.brand            AS brand,
            v.model            AS model,
            v.gearType         AS gearType,
            v.fuelType         AS fuelType,
            ad.name            AS agencyDepartureName,
            ad.city            AS departureCity,
            ar.name            AS agencyReturnName,
            ar.city            AS returnCity
        FROM Offer o
        JOIN o.vehicle v
        JOIN o.agencyDeparture ad
        JOIN o.agencyReturn ar
        WHERE o.status     = 'AVAILABLE'
        AND   ad.city      = :city
        AND   o.startAt   >= :startAt
        AND   o.endAt     <= :endAt
        AND   LEFT(v.acrissCode, 2) = :acrissPrefix
        ORDER BY o.price ASC
    """)
    List<OfferSearchResult> findAvailableOffersByCategory(
        @Param("city")         String city,
        @Param("startAt")      LocalDateTime startAt,
        @Param("endAt")        LocalDateTime endAt,
        @Param("acrissPrefix") String acrissPrefix
    );
}
