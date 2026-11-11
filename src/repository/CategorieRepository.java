package repository;

import modele.Categorie;

import java.util.List;
import java.util.ArrayList;

/**
 * Dépôt de données pour les catégories d'âge.
 * Charge les catégories depuis un fichier XML.
 */
public class CategorieRepository {

    /**
     * Charge la liste des catégories depuis un fichier XML.
     *
     * @param cheminFichier chemin absolu ou relatif vers le fichier categorie.xml
     * @return la liste des catégories chargées
     */
    public List<Categorie> chargerCategories(String cheminFichier) {
        // TODO : implémenter dans US6
        return new ArrayList<>();
    }
}
