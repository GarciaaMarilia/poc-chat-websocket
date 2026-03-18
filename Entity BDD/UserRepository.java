package com.yourcaryourway.repository;

import com.yourcaryourway.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {

    // ── Méthodes dérivées ─────────────────────────────────────────────────

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    // ── R-06 : Suppression RGPD — droit à l'oubli ────────────────────────

    /**
     * Pseudonymise les données personnelles et marque le compte comme supprimé.
     * Les adresses, permis et moyens de paiement sont supprimés par CASCADE.
     *
     * Utilise SQL natif pour CONCAT (non supporté nativement en JPQL pour UPDATE).
     */
    @Modifying
    @Transactional
    @Query(value = """
        UPDATE users SET
            email         = CONCAT('deleted_', id, '@deleted.invalid'),
            password_hash = 'DELETED',
            first_name    = 'Utilisateur',
            last_name     = 'Supprimé',
            birth_date    = NULL,
            phone         = NULL,
            deleted_at    = NOW()
        WHERE id = :userId
    """, nativeQuery = true)
    void anonymizeUser(@Param("userId") String userId);

    /**
     * Révoquer tous les refresh tokens actifs de l'utilisateur.
     * Appelé lors de la suppression de compte.
     */
    @Modifying
    @Transactional
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.user.id = :userId")
    void revokeAllRefreshTokens(@Param("userId") String userId);

    // ── Vérification email ────────────────────────────────────────────────

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.emailVerified = true WHERE u.id = :userId")
    void markEmailAsVerified(@Param("userId") String userId);
}
