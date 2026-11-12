package modele;

/**
 * Permissions et privilèges applicatifs (RBAC - Role-Based Access Control)
 * alignés sur les personas de la mission BTS SIO (Secrétaire, Trésorier, Président, Administrateur).
 */
public enum Droit {

    /** Création, modification et suppression des adhérents (Epic 1 — Secrétaire / Administrateur). */
    GESTION_ADHERENTS("Création, modification et suppression d'adhérents"),

    /** Recherche multicritère et consultation détaillée de la fiche adhérent (Tous profils). */
    CONSULTATION_ADHERENTS("Recherche et consultation des fiches adhérents"),

    /** Consultation du récapitulatif financier et totaux des cotisations (Epic 3 — Trésorier / Administrateur). */
    GESTION_FINANCES("Consultation du récapitulatif financier et cotisations"),

    /** Sauvegarde chiffrée .gea et réimportation en mémoire (Epic 3 — Trésorier / Administrateur). */
    SAUVEGARDE_RESTAURATION("Exportation et importation de sauvegardes chiffrées"),

    /** Consultation de l'annuaire des clubs d'escrime (Epic 2 — Président / Secrétaire / Administrateur). */
    CONSULTATION_CLUBS("Consultation de l'annuaire des clubs"),

    /** Impression papier et export HTML imprimable de l'annuaire (Epic 2 — Président / Administrateur). */
    IMPRESSION_CLUBS("Impression et export de l'annuaire des clubs"),

    /** Administration des comptes utilisateurs, déblocage et audit (Epic 4 — Informaticien bénévole M2L). */
    ADMINISTRATION("Administration des comptes et sécurité du système");

    private final String description;

    Droit(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

