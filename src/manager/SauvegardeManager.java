package manager;

import modele.Adherent;

import java.util.List;

/**
 * Gestionnaire de sauvegarde et de restauration des données adhérents.
 * Exporte et importe la liste des adhérents vers/depuis un fichier.
 */
public class SauvegardeManager {

    /**
     * Exporte la liste des adhérents vers un fichier sérialisé.
     * Les données ne doivent pas être stockées en clair.
     *
     * @param chemin  chemin du fichier de destination
     * @param donnees la liste des adhérents à exporter
     */
    public void exporterDonnees(String chemin, List<Adherent> donnees) {
        // TODO : implémenter dans US10
    }

    /**
     * Importe la liste des adhérents depuis un fichier sérialisé.
     * Affiche un popup en cas d'erreur de désérialisation.
     *
     * @param chemin chemin du fichier source
     * @return la liste des adhérents restaurée, ou liste vide en cas d'erreur
     */
    public List<Adherent> importerDonnees(String chemin) {
        // TODO : implémenter dans US11
        return new java.util.ArrayList<>();
    }
}

