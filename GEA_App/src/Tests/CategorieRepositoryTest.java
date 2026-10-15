package Tests;

import java.util.List;
import Modele.Categorie;
import Repository.CategorieRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

public class CategorieRepositoryTest {

    private List<Categorie> lesCategories;

    @BeforeEach
    void setUp() throws Exception {
        lesCategories = CategorieRepository.getLesCategories("categorie.xml");
    }

    @Test
    void TrouverCategorie_existe() {
        int anneeNaissance = 2015; // Doit correspondre aux Benjamins (M13)

        Categorie resultat = CategorieRepository.trouverCategorie(anneeNaissance, lesCategories);

        Assertions.assertNotNull(resultat);
        Assertions.assertEquals("Benjamins", resultat.getNom());
        Assertions.assertEquals(210.0, resultat.getTarif());
    }

    @Test
    void trouverCategorie_existePas() {
        int anneeNaissance = 1910; // Aucune catégorie pour cette année

        Categorie resultat = CategorieRepository.trouverCategorie(anneeNaissance, lesCategories);

        Assertions.assertNull(resultat);
    }
}