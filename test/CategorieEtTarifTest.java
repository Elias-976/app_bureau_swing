import manager.AdherentManager;
import modele.Adherent;
import modele.Categorie;
import repository.CategorieRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Tests unitaires pour le calcul de catégorie, la grille tarifaire Annexe 1
 * et les méthodes d'âge/minorité de {@link Adherent} (US6 #50366).
 */
public final class CategorieEtTarifTest {

    private CategorieEtTarifTest() {
    }

    public static void main(String[] args) throws Exception {
        executer();
        System.out.println("CategorieEtTarifTest : OK (" + OutilAssertions.getNombreAssertions() + " assertions)");
    }

    public static void executer() throws Exception {
        testerChargementXmlEtTarifsAnnexe1();
        testerAttributionCategorieCasLimites();
        testerAgeEtMinoriteAdherent();
    }

    private static Adherent creerAdherentAvecNaissance(LocalDate dateNaissance) {
        return new Adherent(
                "TestNom", "TestPrenom", "",
                dateNaissance,
                "10 rue des Fleurs", "54500", "Vandœuvre",
                "0612345678", "test@escrime.fr",
                "Fleuret", "Compétition", "Droitier",
                "RespNom", "RespPrenom"
        );
    }

    private static void testerChargementXmlEtTarifsAnnexe1() throws Exception {
        CategorieRepository repo = new CategorieRepository();
        List<Categorie> categories = repo.chargerCategories("document/categorie.xml");

        OutilAssertions.assertEquals(10, categories.size(), "Nombre de catégories chargées depuis XML");

        Map<String, Double> tarifsAttendus = Map.ofEntries(
                Map.entry("Eveil",     90.0),
                Map.entry("Atomes",    130.0),
                Map.entry("Poussins",  160.0),
                Map.entry("Pupilles",  180.0),
                Map.entry("Benjamins", 210.0),
                Map.entry("Minimes",   210.0),
                Map.entry("Cadets",    230.0),
                Map.entry("Juniors",   230.0),
                Map.entry("Séniors",   255.0),
                Map.entry("Vétérans",  255.0)
        );

        for (Categorie cat : categories) {
            Double attendu = tarifsAttendus.get(cat.getLibelle());
            OutilAssertions.assertNotNull(attendu, "Tarif attendu présent pour " + cat.getLibelle());
            OutilAssertions.assertEqualsDouble(attendu, cat.getCotisation(), 0.001,
                    "Cotisation Annexe 1 pour " + cat.getLibelle());
        }

        // Vérifier la tolérance sans accent / insensible à la casse
        OutilAssertions.assertEqualsDouble(255.0,
                CategorieRepository.determinerCotisation("", "SÉNIORS"), 0.001,
                "Tarif Séniors normalisé");
        OutilAssertions.assertEqualsDouble(0.0,
                CategorieRepository.determinerCotisation("", "Inconnue"), 0.001,
                "Tarif catégorie inconnue");
    }

    private static void testerAttributionCategorieCasLimites() throws Exception {
        List<Categorie> categories = new CategorieRepository().chargerCategories("document/categorie.xml");
        AdherentManager manager = new AdherentManager();

        // Cas critique du bug Bloc 1 : né le 15/12/2019 -> Poussins (M9) / 160 €
        Categorie cPoussinFin = manager.trouverCategorie(
                creerAdherentAvecNaissance(LocalDate.of(2019, 12, 15)), categories);
        OutilAssertions.assertNotNull(cPoussinFin, "15/12/2019 doit avoir une catégorie");
        OutilAssertions.assertEquals("Poussins", cPoussinFin.getLibelle(), "15/12/2019 -> Poussins");
        OutilAssertions.assertEquals("M9", cPoussinFin.getCode(), "15/12/2019 -> M9");
        OutilAssertions.assertEqualsDouble(160.0, cPoussinFin.getCotisation(), 0.001, "15/12/2019 -> 160 €");

        // Borne basse Poussins : 01/01/2018
        Categorie cPoussinDeb = manager.trouverCategorie(
                creerAdherentAvecNaissance(LocalDate.of(2018, 1, 1)), categories);
        OutilAssertions.assertNotNull(cPoussinDeb, "01/01/2018 -> catégorie");
        OutilAssertions.assertEquals("Poussins", cPoussinDeb.getLibelle(), "01/01/2018 -> Poussins");

        // Éveil : 2026 et 2022 -> 90 €
        Categorie cEveil2026 = manager.trouverCategorie(
                creerAdherentAvecNaissance(LocalDate.of(2026, 3, 10)), categories);
        OutilAssertions.assertNotNull(cEveil2026, "2026 -> catégorie");
        OutilAssertions.assertEquals("Eveil", cEveil2026.getLibelle(), "2026 -> Eveil");
        OutilAssertions.assertEquals("M5", cEveil2026.getCode(), "2026 -> M5");
        OutilAssertions.assertEqualsDouble(90.0, cEveil2026.getCotisation(), 0.001, "2026 -> 90 €");

        Categorie cEveil2022 = manager.trouverCategorie(
                creerAdherentAvecNaissance(LocalDate.of(2022, 1, 1)), categories);
        OutilAssertions.assertNotNull(cEveil2022, "2022 -> catégorie");
        OutilAssertions.assertEquals("Eveil", cEveil2022.getLibelle(), "2022 -> Eveil");

        // Frontière Vétérans (1987) / Séniors (1988) -> 255 €
        Categorie cVeteran = manager.trouverCategorie(
                creerAdherentAvecNaissance(LocalDate.of(1987, 12, 31)), categories);
        OutilAssertions.assertNotNull(cVeteran, "1987 -> catégorie");
        OutilAssertions.assertEquals("Vétérans", cVeteran.getLibelle(), "1987 -> Vétérans");
        OutilAssertions.assertEqualsDouble(255.0, cVeteran.getCotisation(), 0.001, "1987 -> 255 €");

        Categorie cSenior = manager.trouverCategorie(
                creerAdherentAvecNaissance(LocalDate.of(1988, 1, 1)), categories);
        OutilAssertions.assertNotNull(cSenior, "1988 -> catégorie");
        OutilAssertions.assertEquals("Séniors", cSenior.getLibelle(), "1988 -> Séniors");
        OutilAssertions.assertEqualsDouble(255.0, cSenior.getCotisation(), 0.001, "1988 -> 255 €");

        // Hors bornes -> null
        Categorie cHorsBas = manager.trouverCategorie(
                creerAdherentAvecNaissance(LocalDate.of(1929, 12, 31)), categories);
        OutilAssertions.assertNull(cHorsBas, "1929 hors bornes XML -> null");

        Categorie cHorsHaut = manager.trouverCategorie(
                creerAdherentAvecNaissance(LocalDate.of(2027, 1, 1)), categories);
        OutilAssertions.assertNull(cHorsHaut, "2027 hors bornes XML -> null");

        OutilAssertions.assertNull(manager.trouverCategorie((Adherent) null, categories),
                "Adhérent null -> null");
        OutilAssertions.assertNull(manager.trouverCategorie(creerAdherentAvecNaissance(LocalDate.of(2015, 5, 5)), null),
                "Liste catégories null -> null");
    }

    private static void testerAgeEtMinoriteAdherent() {
        LocalDate aujourdhui = LocalDate.now();

        Adherent mineur10 = creerAdherentAvecNaissance(aujourdhui.minusYears(10));
        OutilAssertions.assertEquals(10, mineur10.getAge(), "Âge exact 10 ans");
        OutilAssertions.assertTrue(mineur10.estMineur(), "10 ans est mineur");

        // Veille des 18 ans -> encore 17 ans donc mineur
        Adherent veilleMajorite = creerAdherentAvecNaissance(aujourdhui.minusYears(18).plusDays(1));
        OutilAssertions.assertEquals(17, veilleMajorite.getAge(), "Veille des 18 ans -> 17 ans");
        OutilAssertions.assertTrue(veilleMajorite.estMineur(), "17 ans est mineur");

        // Jour exact des 18 ans -> majeur
        Adherent jourMajorite = creerAdherentAvecNaissance(aujourdhui.minusYears(18));
        OutilAssertions.assertEquals(18, jourMajorite.getAge(), "Jour des 18 ans -> 18 ans");
        OutilAssertions.assertFalse(jourMajorite.estMineur(), "18 ans est majeur");
    }
}
