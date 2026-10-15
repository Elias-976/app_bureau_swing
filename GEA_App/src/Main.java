import java.util.List;
import Modele.Categorie;
import Repository.CategorieRepository;

public class Main {
    public static void main(String[] args) throws Exception {

        // Chargement des catégories depuis le fichier XML
        List<Categorie> categories = CategorieRepository.getLesCategories("categorie.xml");

        // Affichage de toutes les catégories avec leur tarif
        for (Categorie c : categories) {
            System.out.println(c);
        }

        // Test de recherche pour une année de naissance
        Categorie categorieTrouvee = CategorieRepository.trouverCategorie(2015, categories);
        System.out.println("Catégorie trouvée pour 2015 : " + categorieTrouvee);
    }
}