import manager.SauvegardeManager;
import modele.Adherent;
import modele.Club;
import repository.ClubRepository;
import vue.AnnuaireClubsVue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Tests de sécurité pour la sauvegarde/restauration chiffrée (US10 / US11)
 * et vérification de l'anonymisation RGPD de l'annuaire des clubs (US7).
 */
public final class SauvegardeEtAnnuaireTest {

    private SauvegardeEtAnnuaireTest() {
    }

    public static void main(String[] args) throws Exception {
        executer();
        System.out.println("SauvegardeEtAnnuaireTest : OK (" + OutilAssertions.getNombreAssertions() + " assertions)");
    }

    public static void executer() throws Exception {
        testerExportChiffreEtReimportation();
        testerErreursImportationEtDeserialisation();
        testerAnnuaireClubsEtRgpd();
    }

    private static byte[] serialiserBrut(Object objet) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(objet);
        }
        return baos.toByteArray();
    }

    private static void testerExportChiffreEtReimportation() throws Exception {
        SauvegardeManager sm = new SauvegardeManager();
        Path dossierTemp = Files.createTempDirectory("gea_unit_test_");
        try {
            Adherent majeur = new Adherent(
                    1, "DupontConfidentiel", "JeanPhilippe", "MartinOrigine",
                    LocalDate.of(1992, 7, 14),
                    "42 Avenue Confidentialite", "54500", "VandoeuvreSecret",
                    "0612345678", "jean.dupont.secret@escrime-m2l.fr",
                    "Fleuret", "Compétition", "Droitier", "", "");

            Adherent mineur = new Adherent(
                    2, "PetitEnfant", "CamilleTest", "",
                    LocalDate.now().minusYears(10),
                    "9 Rue des Lilas", "54000", "NancyVille",
                    "0698765432", "camille.parent@exemple.fr",
                    "Sabre", "Loisir", "Gaucher", "TuteurNom", "TuteurPrenom");

            List<Adherent> originaux = List.of(majeur, mineur);
            char[] motDePasse = "CleSecreteEscrime2026!".toCharArray();

            Path fichier1 = dossierTemp.resolve("sauvegarde1.gea");
            Path fichier2 = dossierTemp.resolve("sauvegarde2.gea");

            OutilAssertions.assertTrue(sm.exporterDonnees(fichier1.toString(), originaux, motDePasse),
                    "Export chiffré réussi");
            OutilAssertions.assertNull(sm.getDernierMessageErreur(), "Aucune erreur après export");
            OutilAssertions.assertTrue(sm.exporterDonnees(fichier2.toString(), originaux, motDePasse),
                    "Second export chiffré réussi");

            byte[] bruts1 = Files.readAllBytes(fichier1);
            byte[] bruts2 = Files.readAllBytes(fichier2);
            OutilAssertions.assertFalse(Arrays.equals(bruts1, bruts2),
                    "Sel et IV aléatoires produisent des fichiers différents");

            // Vérifier l'absence de données en clair (UTF-8 et ISO-8859-1)
            String contenuUtf8 = new String(bruts1, StandardCharsets.UTF_8);
            String contenuIso  = new String(bruts1, StandardCharsets.ISO_8859_1);
            for (String sensible : List.of(
                    "DupontConfidentiel", "JeanPhilippe", "MartinOrigine",
                    "42 Avenue Confidentialite", "54500", "VandoeuvreSecret",
                    "0612345678", "jean.dupont.secret@escrime-m2l.fr",
                    "PetitEnfant", "CamilleTest", "9 Rue des Lilas", "NancyVille",
                    "0698765432", "camille.parent@exemple.fr", "TuteurNom", "TuteurPrenom",
                    "modele.Adherent", "java.util.ArrayList")) {
                OutilAssertions.assertNotContains(contenuUtf8, sensible,
                        "Absence en clair (UTF-8) : " + sensible);
                OutilAssertions.assertNotContains(contenuIso, sensible,
                        "Absence en clair (ISO-8859-1) : " + sensible);
            }

            // Réimportation et vérification d'égalité des données
            List<Adherent> restaures = sm.importerDonnees(fichier1.toString(), motDePasse);
            OutilAssertions.assertNotNull(restaures, "Réimportation avec bon mot de passe réussit");
            OutilAssertions.assertEquals(2, restaures.size(), "Nombre d'adhérents restaurés");
            OutilAssertions.assertEquals("DupontConfidentiel", restaures.get(0).getNom(), "Nom restauré");
            OutilAssertions.assertEquals(LocalDate.of(1992, 7, 14), restaures.get(0).getDateNaissance(),
                    "Date naissance restaurée");
            OutilAssertions.assertEquals("jean.dupont.secret@escrime-m2l.fr", restaures.get(0).getEmail(),
                    "Email restauré");
            OutilAssertions.assertEquals("TuteurNom", restaures.get(1).getNomRespLegal(),
                    "Responsable légal restauré");

            // Sauvegarde valide contenant 0 adhérent distincte d'une erreur (retourne liste vide, non null)
            Path fichierVide = dossierTemp.resolve("vide.gea");
            OutilAssertions.assertTrue(sm.exporterDonnees(fichierVide.toString(), List.of(), motDePasse),
                    "Export de 0 adhérent réussi");
            List<Adherent> restauresVide = sm.importerDonnees(fichierVide.toString(), motDePasse);
            OutilAssertions.assertNotNull(restauresVide, "Import d'une sauvegarde vide valide retourne non-null");
            OutilAssertions.assertEquals(0, restauresVide.size(), "Sauvegarde vide contient 0 adhérent");

            // Surcharges par défaut (2 arguments)
            Path fichierDefaut = dossierTemp.resolve("defaut.gea");
            OutilAssertions.assertTrue(sm.exporterDonnees(fichierDefaut.toString(), originaux),
                    "Export clé applicative par défaut");
            List<Adherent> restauresDef = sm.importerDonnees(fichierDefaut.toString());
            OutilAssertions.assertNotNull(restauresDef, "Import clé applicative par défaut");
            OutilAssertions.assertEquals(2, restauresDef.size(), "2 adhérents restaurés par défaut");
        } finally {
            supprimerRecursivement(dossierTemp);
        }
    }

    private static void testerErreursImportationEtDeserialisation() throws Exception {
        SauvegardeManager sm = new SauvegardeManager();
        Path dossierTemp = Files.createTempDirectory("gea_err_test_");
        char[] mdp = "MotDePasseTest123".toCharArray();

        try {
            // 1. Fichier inexistant -> retourne null sans exception non gérée
            List<Adherent> resInexistant = sm.importerDonnees(
                    dossierTemp.resolve("absent.gea").toString(), mdp);
            OutilAssertions.assertNull(resInexistant, "Fichier inexistant retourne null");
            OutilAssertions.assertNotNull(sm.getDernierMessageErreur(), "Message d'erreur renseigné");

            // 2. Fichier texte corrompu
            Path fichierTexte = dossierTemp.resolve("texte.gea");
            Files.writeString(fichierTexte, "Ceci est un fichier texte corrompu non chiffré.");
            OutilAssertions.assertNull(sm.importerDonnees(fichierTexte.toString(), mdp),
                    "Fichier texte corrompu retourne null");
            OutilAssertions.assertNotNull(sm.getDernierMessageErreur(), "Erreur renseignée pour fichier corrompu");

            // 3. Objet sérialisé non chiffré (mauvais format)
            Path fichierOosClair = dossierTemp.resolve("oos_clair.gea");
            Files.write(fichierOosClair, serialiserBrut("MauvaisType"));
            OutilAssertions.assertNull(sm.importerDonnees(fichierOosClair.toString(), mdp),
                    "Objet sérialisé non chiffré retourne null");

            // 4. Mauvais mot de passe et fichier altéré
            Path fichierValide = dossierTemp.resolve("valide.gea");
            Adherent sample = new Adherent(
                    1, "Martin", "Paul", "",
                    LocalDate.of(1995, 1, 1),
                    "", "", "", "0612345678", "paul@club.fr",
                    "Fleuret", "Loisir", "Droitier", "", "");
            OutilAssertions.assertTrue(sm.exporterDonnees(fichierValide.toString(), List.of(sample), mdp),
                    "Export témoin réussi");

            OutilAssertions.assertNull(sm.importerDonnees(fichierValide.toString(), "MauvaisMdp".toCharArray()),
                    "Mauvais mot de passe retourne null");

            byte[] octetsAlteres = Files.readAllBytes(fichierValide);
            octetsAlteres[octetsAlteres.length - 4] ^= 0x42;
            Path fichierAltere = dossierTemp.resolve("altere.gea");
            Files.write(fichierAltere, octetsAlteres);
            OutilAssertions.assertNull(sm.importerDonnees(fichierAltere.toString(), mdp),
                    "Fichier chiffré altéré retourne null");

            // 5. Export vers un répertoire inexistant -> retourne false
            OutilAssertions.assertFalse(
                    sm.exporterDonnees(dossierTemp.resolve("sous_dossier_absent/out.gea").toString(),
                            List.of(sample), mdp),
                    "Export vers chemin impossible retourne false");
            OutilAssertions.assertNotNull(sm.getDernierMessageErreur(), "Erreur renseignée après échec export");

            // 6. Contrôle direct du filtre ObjectInputFilter et des types inattendus
            boolean invalidClassLevee = false;
            try {
                sm.deserialiserListeSecurisee(serialiserBrut(new HashMap<String, String>()));
            } catch (InvalidClassException expected) {
                invalidClassLevee = true;
            }
            OutilAssertions.assertTrue(invalidClassLevee,
                    "ObjectInputFilter rejette HashMap avec InvalidClassException");

            OutilAssertions.assertThrows(ClassCastException.class, () -> {
                try {
                    sm.deserialiserListeSecurisee(serialiserBrut(new ArrayList<>(List.of("intrus"))));
                } catch (IOException | ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }, "Liste contenant String lève ClassCastException");

            OutilAssertions.assertThrows(ClassCastException.class, () -> {
                try {
                    sm.deserialiserListeSecurisee(serialiserBrut(null));
                } catch (IOException | ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }, "Flux null lève ClassCastException");

            // 7. Payload chiffré contenant un mauvais type (HashMap) -> aucune exception non gérée ne remonte
            Method mChiffrer = SauvegardeManager.class.getDeclaredMethod(
                    "chiffrerDonnees", byte[].class, char[].class);
            mChiffrer.setAccessible(true);
            byte[] payloadHashMap = (byte[]) mChiffrer.invoke(
                    sm, serialiserBrut(new HashMap<String, String>()), mdp);
            Path fichierRogue = dossierTemp.resolve("rogue_hashmap.gea");
            Files.write(fichierRogue, payloadHashMap);
            OutilAssertions.assertNull(sm.importerDonnees(fichierRogue.toString(), mdp),
                    "Import d'un objet chiffré HashMap retourne null sans propager d'exception");
            OutilAssertions.assertNotNull(sm.getDernierMessageErreur(),
                    "Message d'erreur renseigné pour objet de mauvais type");
        } finally {
            supprimerRecursivement(dossierTemp);
        }
    }

    private static void testerAnnuaireClubsEtRgpd() {
        ClubRepository repo = new ClubRepository();
        List<Club> clubs = repo.chargerClubs("document/club.xml");
        OutilAssertions.assertEquals(10, clubs.size(), "10 clubs chargés depuis club.xml");
        OutilAssertions.assertEquals("http://www.escrime-laxou.net/", clubs.get(0).getSite(),
                "Balise <site> parsée");

        repo.anonymiserDonnees(clubs);
        Club c1 = clubs.get(0);
        OutilAssertions.assertEquals("M. S*** G.", c1.getBenevoleNom(), "Nom bénévole partiellement anonymisé");
        OutilAssertions.assertEquals("03.**.**.**.**", c1.getBenevoleTelephone(), "Téléphone anonymisé");
        OutilAssertions.assertEquals("c***@escrime-laxou.net", c1.getBenevoleEmail(), "Email anonymisé");

        String html = AnnuaireClubsVue.genererHtmlImprimable(clubs);
        OutilAssertions.assertContains(html, "C.E LAXOVIEN", "HTML contient le nom public du club");
        OutilAssertions.assertNotContains(html, "SILBERSCHMIDT", "HTML ne contient pas le nom réel du bénévole");
    }

    private static void supprimerRecursivement(Path dossier) {
        if (dossier == null || !Files.exists(dossier)) {
            return;
        }
        try (var flux = Files.walk(dossier)) {
            flux.sorted((a, b) -> b.compareTo(a))
                .forEach(p -> {
                    try {
                        Files.deleteIfExists(p);
                    } catch (IOException ignored) {
                    }
                });
        } catch (IOException ignored) {
        }
    }
}
