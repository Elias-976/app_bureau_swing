import manager.AdherentManager;
import manager.ValidateurAdherent;
import modele.Adherent;

import java.time.LocalDate;
import java.util.List;

/**
 * Tests unitaires de validation stricte des champs et formats d'un adhérent (US13 #50378).
 */
public final class ValidateurAdherentTest {

    private ValidateurAdherentTest() {
    }

    public static void main(String[] args) {
        executer();
        System.out.println("ValidateurAdherentTest : OK (" + OutilAssertions.getNombreAssertions() + " assertions)");
    }

    public static void executer() {
        testerAdherentsValides();
        testerChampsObligatoiresVides();
        testerFormatsEmailTelephoneCodePostal();
        testerChampsAlphabetiquesEtPratique();
        testerDatesNaissanceStrictes();
        testerMineurEtResponsableLegal();
        testerIntegrationAdherentManager();
    }

    private static Adherent creerMajeurValide() {
        return new Adherent(
                "Dupont-L'Écuyer", "Éléonore", "Martin",
                LocalDate.of(1995, 5, 15),
                "12 rue de la Paix", "54500", "Vandœuvre-lès-Nancy",
                "06 12 34 56 78", "eleonore.dupont@escrime-m2l.fr",
                "Fleuret", "Compétition", "Droitier",
                "", ""
        );
    }

    private static Adherent creerMineurValide() {
        return new Adherent(
                "O'Connor", "Jean-Luc", "",
                LocalDate.now().minusYears(10),
                "", "", "",
                "+33 (0)6.12.34.56.78", "parent+club@exemple.fr",
                "Sabre", "Loisir", "Gaucher",
                "O'Connor", "Marie-Claire"
        );
    }

    private static void testerAdherentsValides() {
        ValidateurAdherent.valider(creerMajeurValide());
        OutilAssertions.assertTrue(true, "Adhérent majeur valide accepté");

        ValidateurAdherent.valider(creerMineurValide());
        OutilAssertions.assertTrue(true, "Adhérent mineur valide accepté");

        Adherent minimal = new Adherent(
                "D’Artagnan", "Charles", "",
                LocalDate.of(1990, 1, 1),
                "", "", "Y",
                "0383467722", "charles@club.fr",
                "Épée", "Loisir", "Droitier",
                "", ""
        );
        OutilAssertions.assertTrue(ValidateurAdherent.collecterErreurs(minimal).isEmpty(),
                "Adhérent minimal avec champs optionnels vides valide");
    }

    private static void testerChampsObligatoiresVides() {
        OutilAssertions.assertThrows(IllegalArgumentException.class,
                () -> ValidateurAdherent.valider(null), "Adhérent null refusé");

        Adherent sansNom = creerMajeurValide();
        sansNom.setNom("   ");
        OutilAssertions.assertThrows(IllegalArgumentException.class,
                () -> ValidateurAdherent.valider(sansNom), "Nom vide refusé");

        Adherent sansPrenom = creerMajeurValide();
        sansPrenom.setPrenom("");
        OutilAssertions.assertThrows(IllegalArgumentException.class,
                () -> ValidateurAdherent.valider(sansPrenom), "Prénom vide refusé");

        Adherent sansDate = creerMajeurValide();
        sansDate.setDateNaissance(null);
        OutilAssertions.assertThrows(IllegalArgumentException.class,
                () -> ValidateurAdherent.valider(sansDate), "Date de naissance null refusée");

        Adherent sansTel = creerMajeurValide();
        sansTel.setTelephone(" ");
        OutilAssertions.assertThrows(IllegalArgumentException.class,
                () -> ValidateurAdherent.valider(sansTel), "Téléphone vide refusé");

        Adherent sansEmail = creerMajeurValide();
        sansEmail.setEmail(null);
        OutilAssertions.assertThrows(IllegalArgumentException.class,
                () -> ValidateurAdherent.valider(sansEmail), "Email null refusé");
    }

    private static void testerFormatsEmailTelephoneCodePostal() {
        for (String emailOk : List.of(
                "a@b.fr",
                "jean.dupont+1@club-escrime.org",
                "user_name@sub.domain.co.uk")) {
            OutilAssertions.assertTrue(ValidateurAdherent.estEmailValide(emailOk),
                    "Email valide attendu : " + emailOk);
        }

        for (String emailKo : List.of(
                "", "invalide", "@escrime.fr", "jean@", "jean@domaine",
                "jean@.fr", "jean..dupont@escrime.fr", ".jean@escrime.fr",
                "jean.@escrime.fr", "jean@dom_aine.fr", "a@b.c", "a@b.fr\n")) {
            OutilAssertions.assertFalse(ValidateurAdherent.estEmailValide(emailKo),
                    "Email invalide attendu : " + emailKo);
        }

        for (String telOk : List.of(
                "0612345678", "06 12 34 56 78", "06.12.34.56.78", "06-12-34-56-78",
                "+33612345678", "+33 6 12 34 56 78", "+33 (0)6 12 34 56 78")) {
            OutilAssertions.assertTrue(ValidateurAdherent.estTelephoneValide(telOk),
                    "Téléphone valide attendu : " + telOk);
        }

        for (String telKo : List.of(
                "", "12345", "061234567", "06123456789", "0012345678",
                "06abcdefgh", "+33012345678", "06..12.34.56.78", "06/12/34/56/78")) {
            OutilAssertions.assertFalse(ValidateurAdherent.estTelephoneValide(telKo),
                    "Téléphone invalide attendu : " + telKo);
        }

        OutilAssertions.assertTrue(ValidateurAdherent.estCodePostalValide("54500"), "CP 54500 valide");
        OutilAssertions.assertTrue(ValidateurAdherent.estCodePostalValide("01000"), "CP 01000 valide");
        OutilAssertions.assertFalse(ValidateurAdherent.estCodePostalValide("5450"), "CP 4 chiffres invalide");
        OutilAssertions.assertFalse(ValidateurAdherent.estCodePostalValide("545000"), "CP 6 chiffres invalide");
        OutilAssertions.assertFalse(ValidateurAdherent.estCodePostalValide("54A00"), "CP alphanumérique invalide");

        Adherent mauvaisCp = creerMajeurValide();
        mauvaisCp.setCodePostal("5450");
        OutilAssertions.assertThrows(IllegalArgumentException.class,
                () -> ValidateurAdherent.valider(mauvaisCp), "CP invalide rejeté par valider()");
    }

    private static void testerChampsAlphabetiquesEtPratique() {
        Adherent nomChiffre = creerMajeurValide();
        nomChiffre.setNom("Dupont123");
        OutilAssertions.assertThrows(IllegalArgumentException.class,
                () -> ValidateurAdherent.valider(nomChiffre), "Chiffres dans nom refusés");

        Adherent prenomControle = creerMajeurValide();
        prenomControle.setPrenom("Jean\nLuc");
        OutilAssertions.assertThrows(IllegalArgumentException.class,
                () -> ValidateurAdherent.valider(prenomControle), "Caractère contrôle dans prénom refusé");

        Adherent villeChiffre = creerMajeurValide();
        villeChiffre.setVille("Nancy 54");
        OutilAssertions.assertThrows(IllegalArgumentException.class,
                () -> ValidateurAdherent.valider(villeChiffre), "Chiffres dans ville refusés");

        Adherent adresseControle = creerMajeurValide();
        adresseControle.setAdresse("12 rue\tPaix");
        OutilAssertions.assertThrows(IllegalArgumentException.class,
                () -> ValidateurAdherent.valider(adresseControle), "Caractère contrôle dans adresse refusé");

        Adherent mauvaiseArme = creerMajeurValide();
        mauvaiseArme.setArme("Hallebarde");
        OutilAssertions.assertThrows(IllegalArgumentException.class,
                () -> ValidateurAdherent.valider(mauvaiseArme), "Arme non autorisée refusée");

        Adherent mauvaisePratique = creerMajeurValide();
        mauvaisePratique.setPratique("Professionnel");
        OutilAssertions.assertThrows(IllegalArgumentException.class,
                () -> ValidateurAdherent.valider(mauvaisePratique), "Pratique non autorisée refusée");

        Adherent mauvaiseLat = creerMajeurValide();
        mauvaiseLat.setLateralite("Ambidextre");
        OutilAssertions.assertThrows(IllegalArgumentException.class,
                () -> ValidateurAdherent.valider(mauvaiseLat), "Latéralité non autorisée refusée");
    }

    private static void testerDatesNaissanceStrictes() {
        OutilAssertions.assertThrows(IllegalArgumentException.class,
                () -> ValidateurAdherent.parserDateNaissance("31/02/2020"),
                "Date inexistante 31/02/2020 refusée");

        OutilAssertions.assertThrows(IllegalArgumentException.class,
                () -> ValidateurAdherent.parserDateNaissance("29/02/2021"),
                "Date non bissextile 29/02/2021 refusée");

        LocalDate bissextile = ValidateurAdherent.parserDateNaissance("29/02/2020");
        OutilAssertions.assertEquals(LocalDate.of(2020, 2, 29), bissextile,
                "Date bissextile 29/02/2020 acceptée");

        OutilAssertions.assertThrows(IllegalArgumentException.class,
                () -> ValidateurAdherent.parserDateNaissance("2020-01-01"),
                "Format ISO refusé");

        OutilAssertions.assertThrows(IllegalArgumentException.class,
                () -> ValidateurAdherent.parserDateNaissance("31/12/1899"),
                "Date antérieure à 1900 refusée");

        LocalDate min1900 = ValidateurAdherent.parserDateNaissance("01/01/1900");
        OutilAssertions.assertEquals(LocalDate.of(1900, 1, 1), min1900,
                "Date 01/01/1900 acceptée");

        LocalDate futur = LocalDate.now().plusDays(1);
        OutilAssertions.assertThrows(IllegalArgumentException.class,
                () -> ValidateurAdherent.validerDateNaissance(futur),
                "Date future refusée");
    }

    private static void testerMineurEtResponsableLegal() {
        Adherent sansNomResp = creerMineurValide();
        sansNomResp.setNomRespLegal("");
        OutilAssertions.assertThrows(IllegalArgumentException.class,
                () -> ValidateurAdherent.valider(sansNomResp),
                "Mineur sans nom de responsable légal refusé");

        Adherent sansPrenomResp = creerMineurValide();
        sansPrenomResp.setPrenomRespLegal("   ");
        OutilAssertions.assertThrows(IllegalArgumentException.class,
                () -> ValidateurAdherent.valider(sansPrenomResp),
                "Mineur sans prénom de responsable légal refusé");

        Adherent avecResp = creerMineurValide();
        ValidateurAdherent.valider(avecResp);
        OutilAssertions.assertTrue(true, "Mineur avec responsable légal complet accepté");
    }

    private static void testerIntegrationAdherentManager() {
        AdherentManager manager = new AdherentManager();

        Adherent invalide = creerMajeurValide();
        invalide.setEmail("email-invalide");
        OutilAssertions.assertThrows(IllegalArgumentException.class,
                () -> manager.ajouterAdherent(invalide),
                "ajouterAdherent rejette un adhérent invalide");
        OutilAssertions.assertTrue(manager.getListeAdherents().isEmpty(),
                "Liste reste vide après échec d'ajout");

        Adherent valide = creerMajeurValide();
        manager.ajouterAdherent(valide);
        OutilAssertions.assertEquals(1, valide.getId(), "Premier ID attribué = 1");

        Adherent modifInvalide = creerMajeurValide();
        modifInvalide.setId(1);
        modifInvalide.setTelephone("000");
        OutilAssertions.assertThrows(IllegalArgumentException.class,
                () -> manager.modifierAdherent(modifInvalide),
                "modifierAdherent rejette une modification invalide");
        OutilAssertions.assertEquals("06 12 34 56 78",
                manager.getListeAdherents().get(0).getTelephone(),
                "L'adhérent existant reste intact après rejet de modification");
    }
}
