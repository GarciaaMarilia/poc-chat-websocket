package com.yourcaryourway.repository;

import com.yourcaryourway.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {

    // ── Méthodes dérivées — Spring génère le SQL ──────────────────────────

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    // ── R-07 : Nettoyage des tokens expirés ──────────────────────────────

    /**
     * Supprime tous les tokens expirés ou révoqués.
     * À exécuter via un @Scheduled quotidien.
     *
     * Spring génère : DELETE FROM refresh_tokens
     *                 WHERE expires_at < ? OR revoked = TRUE
     */
    @Modifying
    @Transactional
    void deleteByExpiresAtBeforeOrRevokedTrue(LocalDateTime now);

    /**
     * Même logique avec JPQL explicite — équivalent à la méthode dérivée ci-dessus.
     * Utilisé si l'on veut un contrôle total sur la query.
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :now OR rt.revoked = true")
    int deleteExpiredAndRevoked(@Param("now") LocalDateTime now);
}
