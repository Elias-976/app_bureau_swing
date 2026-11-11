import manager.AdherentManager;
import modele.Adherent;
import modele.Categorie;
import repository.CategorieRepository;

import java.time.LocalDate;
import java.util.List;

/**
 * Test d'acceptation du récapitulatif financier et du calcul total des cotisations (US9 #50372).
 */
public final class RecapitulatifFinancierTest {

    private RecapitulatifFinancierTest() {
    }

    public static void main(String[] args) throws Exception {
        executer();
        System.out.println("RecapitulatifFinancierTest : OK (" + OutilAssertions.getNombreAssertions() + " assertions)");
    }

    public static void executer() throws Exception {
        testerJeuDeDonneesRepresentatif();
        testerAdherentSansCategorieEtListeVide();
    }

    private static Adherent creerAdherent(String nom, String prenom, LocalDate dateNaissance,
                                          String arme, String email) {
        boolean mineur = java.time.Period.between(dateNaissance, LocalDate.now()).getYears() < 18;
        return new Adherent(
                nom, prenom, "",
                dateNaissance,
                "1 rue de Vandœuvre", "54500", "Vandœuvre",
                "0612345678", email,
                arme, "Compétition", "Droitier",
                mineur ? "ParentNom" : "",
                mineur ? "ParentPrenom" : ""
        );
    }

    private static void testerJeuDeDonneesRepresentatif() throws Exception {
        List<Categorie> categories = new CategorieRepository().chargerCategories("document/categorie.xml");
        AdherentManager manager = new AdherentManager();

        // Jeu de données représentatif (8 adhérents répartis sur 6 catégories) :
        // - 1 Eveil (2024)       :  1 x  90 € =   90 €
        // - 2 Poussins (2019,18) :  2 x 160 € =  320 €
        // - 1 Benjamins (2014)   :  1 x 210 € =  210 €
        // - 1 Cadets (2011)      :  1 x 230 € =  230 €
        // - 2 Séniors (2000,88)  :  2 x 255 € =  510 €
        // - 1 Vétérans (1980)    :  1 x 255 € =  255 €
        // Total attendu          : 1615,00 €
        manager.ajouterAdherent(creerAdherent("Martin",  "Léo",     LocalDate.of(2024, 4, 10), "Fleuret", "leo@club.fr"));
        manager.ajouterAdherent(creerAdherent("Bernard", "Zoé",     LocalDate.of(2019, 12, 15), "Fleuret", "zoe@club.fr"));
        manager.ajouterAdherent(creerAdherent("Petit",   "Lucas",   LocalDate.of(2018, 2, 20), "Épée",    "lucas@club.fr"));
        manager.ajouterAdherent(creerAdherent("Robert",  "Chloé",   LocalDate.of(2014, 7, 1),  "Sabre",   "chloe@club.fr"));
        manager.ajouterAdherent(creerAdherent("Richard", "Hugo",    LocalDate.of(2011, 11, 30),"Épée",    "hugo@club.fr"));
        manager.ajouterAdherent(creerAdherent("Durand",  "Camille", LocalDate.of(2000, 6, 15), "Fleuret", "camille@club.fr"));
        manager.ajouterAdherent(creerAdherent("Moreau",  "Thomas",  LocalDate.of(1988, 1, 5),  "Sabre",   "thomas@club.fr"));
        manager.ajouterAdherent(creerAdherent("Laurent", "Claire",  LocalDate.of(1980, 9, 22), "Épée",    "claire@club.fr"));

        double total = manager.calculerMontantTotal(categories);
        OutilAssertions.assertEqualsDouble(1615.0, total, 0.001,
                "Montant total des cotisations du jeu de données");

        String rapport = manager.genererRapportParCategorie(categories);
        OutilAssertions.assertContains(rapport, "Eveil (M5)", "Rapport contient Eveil");
        OutilAssertions.assertContains(rapport, "90,00 €", "Rapport contient tarif Eveil");
        OutilAssertions.assertContains(rapport, "Poussins (M9)", "Rapport contient Poussins");
        OutilAssertions.assertContains(rapport, "320,00 €", "Rapport contient sous-total Poussins");
        OutilAssertions.assertContains(rapport, "Benjamins (M13)", "Rapport contient Benjamins");
        OutilAssertions.assertContains(rapport, "Cadets (M17)", "Rapport contient Cadets");
        OutilAssertions.assertContains(rapport, "Séniors", "Rapport contient Séniors");
        OutilAssertions.assertContains(rapport, "510,00 €", "Rapport contient sous-total Séniors");
        OutilAssertions.assertContains(rapport, "Vétérans", "Rapport contient Vétérans");
        OutilAssertions.assertContains(rapport, "TOTAL GÉNÉRAL : 1615,00 €", "Rapport contient total général exact");
        OutilAssertions.assertContains(rapport, "Nombre total d'adhérents : 8", "Rapport contient effectif total");
        OutilAssertions.assertContains(rapport, "Zoé Bernard", "Rapport liste les membres");

        // Les catégories sans adhérent ne doivent pas apparaître
        OutilAssertions.assertNotContains(rapport, "Atomes", "Catégorie vide Atomes absente");
        OutilAssertions.assertNotContains(rapport, "Pupilles", "Catégorie vide Pupilles absente");
        OutilAssertions.assertNotContains(rapport, "Minimes", "Catégorie vide Minimes absente");
        OutilAssertions.assertNotContains(rapport, "Juniors", "Catégorie vide Juniors absente");
    }

    private static void testerAdherentSansCategorieEtListeVide() throws Exception {
        List<Categorie> categories = new CategorieRepository().chargerCategories("document/categorie.xml");
        AdherentManager manager = new AdherentManager();

        OutilAssertions.assertEqualsDouble(0.0, manager.calculerMontantTotal(categories), 0.001,
                "Total pour liste vide = 0");

        // Adhérent né en 1925 (hors intervalle 1930-2026 de categorie.xml)
        manager.ajouterAdherent(creerAdherent("Doyen", "Henri", LocalDate.of(1925, 3, 12), "Fleuret", "henri@club.fr"));
        OutilAssertions.assertEqualsDouble(0.0, manager.calculerMontantTotal(categories), 0.001,
                "Adhérent hors bornes contribue 0 €");
        String rapport = manager.genererRapportParCategorie(categories);
        OutilAssertions.assertContains(rapport, "Sans catégorie correspondante : 1 adhérent(s)",
                "Mention des adhérents sans catégorie");
    }
}
