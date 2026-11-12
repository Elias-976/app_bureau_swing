package modele;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Rôles applicatifs conformément aux personas du cahier des charges
 * et du guide Tuleap Campus (M2L / Cercle d'Escrime de Vandœuvre).
 */
public enum Role {

    /** Le secrétaire du club (Epic 1) : création, modification, suppression et recherche d'adhérents. */
    SECRETAIRE(
            "Secrétaire",
            "Secrétaire du club — Gestion complète des adhérents",
            EnumSet.of(
                    Droit.GESTION_ADHERENTS,
                    Droit.CONSULTATION_ADHERENTS,
                    Droit.CONSULTATION_CLUBS
            )
    ),

    /** Le trésorier du club (Epic 3) : suivi des recettes, récapitulatif financier et sauvegardes .gea. */
    TRESORIER(
            "Trésorier",
            "Trésorier du club — Suivi financier et sauvegardes chiffrées",
            EnumSet.of(
                    Droit.CONSULTATION_ADHERENTS,
                    Droit.GESTION_FINANCES,
                    Droit.SAUVEGARDE_RESTAURATION
            )
    ),

    /** Le président du club (Epic 2) : relations inter-clubs, annuaire, impression et consultation. */
    PRESIDENT(
            "Président",
            "Président du club — Relations inter-clubs et impressions",
            EnumSet.of(
                    Droit.CONSULTATION_ADHERENTS,
                    Droit.CONSULTATION_CLUBS,
                    Droit.IMPRESSION_CLUBS
            )
    ),

    /** L'informaticien bénévole de la M2L (Epic 4) / Administrateur système : accès complet et sécurité. */
    ADMINISTRATEUR(
            "Administrateur",
            "Informaticien bénévole M2L — Administration et sécurité globale",
            EnumSet.allOf(Droit.class)
    );

    private final String     libelleCourt;
    private final String     description;
    private final Set<Droit> droits;

    Role(String libelleCourt, String description, Set<Droit> droits) {
        this.libelleCourt = libelleCourt;
        this.description  = description;
        this.droits       = Collections.unmodifiableSet(droits);
    }

    public String getLibelleCourt() {
        return libelleCourt;
    }

    public String getDescription() {
        return description;
    }

    public Set<Droit> getDroits() {
        return droits;
    }

    /**
     * Indique si ce rôle possède un droit donné.
     *
     * @param droit le droit à vérifier
     * @return {@code true} si le droit est accordé à ce rôle
     */
    public boolean possedeDroit(Droit droit) {
        return droit != null && droits.contains(droit);
    }
}

