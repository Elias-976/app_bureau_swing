package repository;

import modele.Club;

import java.util.List;
import java.util.ArrayList;

/**
 * Dépôt de données pour les clubs d'escrime.
 * Charge les clubs depuis un fichier XML et gère l'anonymisation RGPD.
 */
public class ClubRepository {

    /**
     * Charge la liste des clubs depuis un fichier XML.
     *
     * @param cheminFichier chemin absolu ou relatif vers le fichier club.xml
     * @return la liste des clubs chargés
     */
    public List<Club> chargerClubs(String cheminFichier) {
        // TODO : implémenter dans US7
        return new ArrayList<>();
    }

    /**
     * Anonymise les données personnelles des bénévoles dans la liste fournie
     * afin de respecter le RGPD (masquage du nom, téléphone et email).
     *
     * @param clubs la liste des clubs à anonymiser (modifiée en place)
     */
    public void anonymiserDonnees(List<Club> clubs) {
        // TODO : implémenter dans US8
    }
}
