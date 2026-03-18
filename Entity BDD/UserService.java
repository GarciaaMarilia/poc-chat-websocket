package com.yourcaryourway.service;

import com.yourcaryourway.repository.RefreshTokenRepository;
import com.yourcaryourway.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository         userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    // ── R-06 : Suppression de compte — droit à l'oubli RGPD ─────────────

    /**
     * Supprime un compte utilisateur conformément au RGPD :
     * 1. Pseudonymise les données PII (email, nom, prénom, etc.)
     * 2. Révoque tous les tokens actifs
     * 3. Les adresses, permis et moyens de paiement sont
     *    supprimés automatiquement par CASCADE (défini dans le schéma SQL)
     */
    @Transactional
    public void deleteAccount(String userId) {
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("Utilisateur introuvable");
        }

        // 1. Révoquer tous les refresh tokens actifs
        userRepository.revokeAllRefreshTokens(userId);

        // 2. Pseudonymiser les données personnelles + soft delete
        userRepository.anonymizeUser(userId);

        log.info("Compte utilisateur {} supprimé conformément au RGPD", userId);
    }

    // ── R-07 : Nettoyage des tokens expirés ──────────────────────────────

    /**
     * Tâche planifiée — exécutée tous les jours à 02:00.
     * Supprime les refresh tokens expirés ou révoqués.
     *
     * @Scheduled utilise le format cron : seconde minute heure jour mois jour-semaine
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void cleanupExpiredTokens() {
        int deleted = refreshTokenRepository.deleteExpiredAndRevoked(LocalDateTime.now());
        log.info("Nettoyage tokens : {} tokens supprimés", deleted);
    }
}
