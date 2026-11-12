import manager.AuthentificationException;
import manager.AuthentificationManager;
import modele.Droit;
import modele.Role;
import modele.Utilisateur;
import repository.UtilisateurRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;

/**
 * Suite de tests unitaires et d'intégration du sous-système d'authentification et de contrôle d'accès RBAC.
 * Vérifie :
 * <ul>
 *     <li>Le hachage cryptographique PBKDF2-HMAC-SHA256 et l'unicité des sels.</li>
 *     <li>L'initialisation et la connexion des 4 comptes officiels personas.</li>
 *     <li>Le rejet des identifiants erronés et la protection anti-force brute (verrouillage après 5 échecs).</li>
 *     <li>Le changement sécurisé de mot de passe et l'effacement mémoire.</li>
 *     <li>La matrice stricte des habilitations par rôle (RBAC).</li>
 *     <li>La persistance atomique sur disque et le rechargement fidèle du référentiel.</li>
 * </ul>
 */
public final class AuthentificationTest {

    private AuthentificationTest() {
    }

    public static void main(String[] args) throws Exception {
        executer();
        System.out.println("AuthentificationTest : OK (" + OutilAssertions.getNombreAssertions() + " assertions)");
    }

    public static void executer() throws Exception {
        testerInitialisationEtHachageSecurise();
        testerConnexionComptesParDefaut();
        testerRejetIdentifiantsInvalides();
        testerProtectionForceBruteEtVerrouillage();
        testerChangementMotDePasseEtHygieneMemoire();
        testerMatriceHabilitationsRBAC();
        testerPersistanceFichierEtRechargement();
    }

    private static void testerInitialisationEtHachageSecurise() throws Exception {
        Path tempFile = Files.createTempFile("auth_init_test_", ".dat");
        try {
            UtilisateurRepository repo = new UtilisateurRepository(tempFile);

            OutilAssertions.assertEquals(4, repo.tous().size(),
                    "Le référentiel doit être initialisé avec exactement 4 comptes personas par défaut");

            Utilisateur secr = repo.parIdentifiant("secretaire").orElseThrow();
            Utilisateur tres = repo.parIdentifiant("tresorier").orElseThrow();
            Utilisateur pres = repo.parIdentifiant("president").orElseThrow();
            Utilisateur adm = repo.parIdentifiant("admin").orElseThrow();

            OutilAssertions.assertEquals(Role.SECRETAIRE, secr.getRole(), "Le rôle de secretaire doit être SECRETAIRE");
            OutilAssertions.assertEquals(Role.TRESORIER, tres.getRole(), "Le rôle de tresorier doit être TRESORIER");
            OutilAssertions.assertEquals(Role.PRESIDENT, pres.getRole(), "Le rôle de president doit être PRESIDENT");
            OutilAssertions.assertEquals(Role.ADMINISTRATEUR, adm.getRole(), "Le rôle de admin doit être ADMINISTRATEUR");

            // Vérification de la non-divulgation des mots de passe en clair et taille cryptographique
            OutilAssertions.assertTrue(secr.getSel().length >= 16, "Le sel doit comporter au moins 16 octets aléatoires");
            OutilAssertions.assertTrue(secr.getMotDePasseHache().length >= 32, "L'empreinte PBKDF2 doit faire au moins 32 octets (256 bits)");

            // Sels distincts entre comptes
            OutilAssertions.assertFalse(Arrays.equals(secr.getSel(), tres.getSel()),
                    "Chaque compte doit obligatoirement avoir un sel cryptographique distinct");
            OutilAssertions.assertFalse(Arrays.equals(pres.getSel(), adm.getSel()),
                    "Chaque compte doit obligatoirement avoir un sel cryptographique distinct");
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    private static void testerConnexionComptesParDefaut() throws Exception {
        Path tempFile = Files.createTempFile("auth_conn_test_", ".dat");
        try {
            AuthentificationManager auth = new AuthentificationManager(new UtilisateurRepository(tempFile));

            OutilAssertions.assertFalse(auth.estConnecte(), "Aucun utilisateur ne doit être connecté initialement");
            OutilAssertions.assertNull(auth.getUtilisateurConnecte(), "L'utilisateur connecté initial doit être null");

            // 1. Connexion secrétaire
            Utilisateur uSecr = auth.connecter("secretaire", "Secretaire2026!".toCharArray());
            OutilAssertions.assertTrue(auth.estConnecte(), "La session doit être active après connexion réussie");
            OutilAssertions.assertContains(uSecr.getNomComplet(), "Sophie Bernard", "Nom complet du compte secrétaire attendu");
            OutilAssertions.assertEquals(Role.SECRETAIRE, auth.getRoleActif(), "Le rôle actif doit être SECRETAIRE");
            OutilAssertions.assertNotNull(uSecr.getDerniereConnexion(), "La date de dernière connexion doit être mise à jour");
            auth.deconnecter();
            OutilAssertions.assertFalse(auth.estConnecte(), "La session doit être terminée après déconnexion");

            // 2. Connexion trésorier
            Utilisateur uTres = auth.connecter("tresorier", "Tresorier2026!".toCharArray());
            OutilAssertions.assertEquals(Role.TRESORIER, auth.getRoleActif(), "Le rôle actif doit être TRESORIER");
            OutilAssertions.assertContains(uTres.getNomComplet(), "Marc Lefebvre", "Nom complet du trésorier attendu");
            auth.deconnecter();

            // 3. Connexion président
            Utilisateur uPres = auth.connecter("president", "President2026!".toCharArray());
            OutilAssertions.assertEquals(Role.PRESIDENT, auth.getRoleActif(), "Le rôle actif doit être PRESIDENT");
            OutilAssertions.assertContains(uPres.getNomComplet(), "Claire Thomas", "Nom complet de la présidente attendu");
            auth.deconnecter();

            // 4. Connexion administrateur
            Utilisateur uAdm = auth.connecter("admin", "AdminM2L2026!".toCharArray());
            OutilAssertions.assertEquals(Role.ADMINISTRATEUR, auth.getRoleActif(), "Le rôle actif doit être ADMINISTRATEUR");
            OutilAssertions.assertContains(uAdm.getNomComplet(), "Informaticien bénévole M2L", "Nom complet de l'administrateur attendu");
            auth.deconnecter();
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    private static void testerRejetIdentifiantsInvalides() throws Exception {
        Path tempFile = Files.createTempFile("auth_rejet_test_", ".dat");
        try {
            AuthentificationManager auth = new AuthentificationManager(new UtilisateurRepository(tempFile));

            // Utilisateur inconnu
            AuthentificationException exInconnu = OutilAssertions.assertThrows(
                    AuthentificationException.class,
                    () -> auth.connecter("utilisateur_inconnu", "Password123!".toCharArray()),
                    "Une tentative avec un identifiant inexistant doit lever AuthentificationException"
            );
            OutilAssertions.assertContains(exInconnu.getMessage(), "Identifiant ou mot de passe incorrect",
                    "Le message ne doit pas dévoiler si l'identifiant existe ou non (prévention énumération)");

            // Mot de passe incorrect
            AuthentificationException exMdpFaux = OutilAssertions.assertThrows(
                    AuthentificationException.class,
                    () -> auth.connecter("secretaire", "MauvaisMotDePasse".toCharArray()),
                    "Un mauvais mot de passe doit être rejeté"
            );
            OutilAssertions.assertContains(exMdpFaux.getMessage(), "Identifiant ou mot de passe incorrect",
                    "Message uniforme en cas de mauvais mot de passe");

            // Mot de passe null ou vide
            OutilAssertions.assertThrows(
                    AuthentificationException.class,
                    () -> auth.connecter("secretaire", new char[0]),
                    "Un mot de passe vide doit être rejeté"
            );
            OutilAssertions.assertThrows(
                    AuthentificationException.class,
                    () -> auth.connecter(null, "Pass".toCharArray()),
                    "Un identifiant null doit être rejeté"
            );
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    private static void testerProtectionForceBruteEtVerrouillage() throws Exception {
        Path tempFile = Files.createTempFile("auth_bruteforce_test_", ".dat");
        try {
            AuthentificationManager auth = new AuthentificationManager(new UtilisateurRepository(tempFile));
            String cible = "secretaire";

            // 4 tentatives infructueuses : le compte reste actif
            for (int i = 1; i <= 4; i++) {
                final int tentative = i;
                try {
                    auth.connecter(cible, ("FauxMdp" + tentative).toCharArray());
                } catch (AuthentificationException ignored) {
                }
            }
            Utilisateur u = auth.getRepository().parIdentifiant(cible).orElseThrow();
            OutilAssertions.assertEquals(4, u.getTentativesEchouees(), "Le compteur doit enregistrer 4 tentatives infructueuses");
            OutilAssertions.assertFalse(u.estVerrouille(), "Le compte ne doit pas encore être verrouillé à 4 échecs");

            // 5ème tentative infructueuse : déclenchement du verrouillage
            try {
                auth.connecter(cible, "FauxMdp5".toCharArray());
            } catch (AuthentificationException ignored) {
            }
            OutilAssertions.assertTrue(u.estVerrouille(), "Le compte doit être verrouillé après exactement 5 échecs consécutifs");

            // 6ème tentative même avec le BON mot de passe : bloquée par sécurité
            AuthentificationException exBloque = OutilAssertions.assertThrows(
                    AuthentificationException.class,
                    () -> auth.connecter(cible, "Secretaire2026!".toCharArray()),
                    "Une tentative sur un compte verrouillé doit être rejetée même avec le bon mot de passe"
            );
            OutilAssertions.assertContains(exBloque.getMessage(), "verrouillé",
                    "Le message doit signaler que le compte est temporairement verrouillé");

            // Déverrouillage administratif
            auth.debloquerCompte(cible);
            OutilAssertions.assertFalse(u.estVerrouille(), "Le compte doit être actif après déverrouillage");
            OutilAssertions.assertEquals(0, u.getTentativesEchouees(), "Le compteur d'échecs doit être remis à zéro");

            // La connexion doit à nouveau être acceptée
            auth.connecter(cible, "Secretaire2026!".toCharArray());
            OutilAssertions.assertTrue(auth.estConnecte(), "Connexion autorisée après déverrouillage");
            auth.deconnecter();
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    private static void testerChangementMotDePasseEtHygieneMemoire() throws Exception {
        Path tempFile = Files.createTempFile("auth_mdp_test_", ".dat");
        try {
            AuthentificationManager auth = new AuthentificationManager(new UtilisateurRepository(tempFile));
            auth.connecter("tresorier", "Tresorier2026!".toCharArray());

            char[] ancienFaux = "MauvaisAncien!".toCharArray();
            char[] nouveauValide = "NouveauTresorier2027!".toCharArray();
            char[] confirmationValide = "NouveauTresorier2027!".toCharArray();

            // 1. Rejet si ancien mot de passe invalide
            OutilAssertions.assertThrows(
                    AuthentificationException.class,
                    () -> auth.changerMotDePasse(ancienFaux, nouveauValide, confirmationValide),
                    "Le changement doit échouer si l'ancien mot de passe est erroné"
            );

            // 2. Rejet si confirmation discordante
            char[] ancienVrai = "Tresorier2026!".toCharArray();
            char[] nouveau1 = "Nouveau123!".toCharArray();
            char[] confirmDiscordante = "Autre123!".toCharArray();
            AuthentificationException exDiscordant = OutilAssertions.assertThrows(
                    AuthentificationException.class,
                    () -> auth.changerMotDePasse(ancienVrai, nouveau1, confirmDiscordante),
                    "Le changement doit échouer si le nouveau mot de passe et sa confirmation diffèrent"
            );
            OutilAssertions.assertContains(exDiscordant.getMessage(), "ne correspond pas",
                    "Message explicitant la non-correspondance des mots de passe");

            // 3. Rejet si trop court (< 8 caractères)
            char[] mdpTropCourt = "Court1!".toCharArray();
            char[] confirmTropCourt = "Court1!".toCharArray();
            AuthentificationException exCourt = OutilAssertions.assertThrows(
                    AuthentificationException.class,
                    () -> auth.changerMotDePasse("Tresorier2026!".toCharArray(), mdpTropCourt, confirmTropCourt),
                    "Un mot de passe de moins de 8 caractères doit être refusé"
            );
            OutilAssertions.assertContains(exCourt.getMessage(), "8 caractères", "Message signalant la longueur minimale");

            // 4. Succès du changement
            char[] mdpActuel = "Tresorier2026!".toCharArray();
            char[] futurMdp = "SuperNouveauMdp2026!".toCharArray();
            char[] confirmFutur = "SuperNouveauMdp2026!".toCharArray();
            auth.changerMotDePasse(mdpActuel, futurMdp, confirmFutur);

            // Vérification de l'hygiène mémoire : les tableaux doivent avoir été remis à zéro (\0)
            for (char c : mdpActuel) {
                OutilAssertions.assertEquals('\0', c, "L'ancien mot de passe en mémoire doit être effacé");
            }
            for (char c : futurMdp) {
                OutilAssertions.assertEquals('\0', c, "Le nouveau mot de passe en mémoire doit être effacé");
            }
            for (char c : confirmFutur) {
                OutilAssertions.assertEquals('\0', c, "La confirmation en mémoire doit être effacée");
            }

            auth.deconnecter();

            // L'ancien mot de passe ne fonctionne plus
            OutilAssertions.assertThrows(
                    AuthentificationException.class,
                    () -> auth.connecter("tresorier", "Tresorier2026!".toCharArray()),
                    "L'ancien mot de passe doit être invalide"
            );

            // Le nouveau fonctionne
            auth.connecter("tresorier", "SuperNouveauMdp2026!".toCharArray());
            OutilAssertions.assertTrue(auth.estConnecte(), "Connexion valide avec le nouveau mot de passe");
            auth.deconnecter();
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    private static void testerMatriceHabilitationsRBAC() throws Exception {
        Path tempFile = Files.createTempFile("auth_rbac_test_", ".dat");
        try {
            AuthentificationManager auth = new AuthentificationManager(new UtilisateurRepository(tempFile));

            // 1. SECRETAIRE
            auth.connecter("secretaire", "Secretaire2026!".toCharArray());
            OutilAssertions.assertTrue(auth.aLeDroit(Droit.GESTION_ADHERENTS), "Secrétaire peut gérer les adhérents");
            OutilAssertions.assertTrue(auth.aLeDroit(Droit.CONSULTATION_ADHERENTS), "Secrétaire peut consulter les adhérents");
            OutilAssertions.assertTrue(auth.aLeDroit(Droit.CONSULTATION_CLUBS), "Secrétaire peut consulter les clubs");
            OutilAssertions.assertFalse(auth.aLeDroit(Droit.GESTION_FINANCES), "Secrétaire NE peut PAS gérer les finances");
            OutilAssertions.assertFalse(auth.aLeDroit(Droit.SAUVEGARDE_RESTAURATION), "Secrétaire NE peut PAS exporter/importer les sauvegardes");
            OutilAssertions.assertFalse(auth.aLeDroit(Droit.IMPRESSION_CLUBS), "Secrétaire NE peut PAS imprimer l'annuaire");
            OutilAssertions.assertFalse(auth.aLeDroit(Droit.ADMINISTRATION), "Secrétaire NE peut PAS administrer");
            // Vérification de exigerDroit
            auth.exigerDroit(Droit.GESTION_ADHERENTS); // Doit passer sans exception
            OutilAssertions.assertThrows(
                    SecurityException.class,
                    () -> auth.exigerDroit(Droit.GESTION_FINANCES),
                    "exigerDroit doit lever SecurityException en cas de droit non accordé"
            );
            auth.deconnecter();

            // 2. TRESORIER
            auth.connecter("tresorier", "Tresorier2026!".toCharArray());
            OutilAssertions.assertTrue(auth.aLeDroit(Droit.GESTION_FINANCES), "Trésorier a accès aux finances");
            OutilAssertions.assertTrue(auth.aLeDroit(Droit.SAUVEGARDE_RESTAURATION), "Trésorier peut sauvegarder/restaurer");
            OutilAssertions.assertTrue(auth.aLeDroit(Droit.CONSULTATION_ADHERENTS), "Trésorier peut consulter les adhérents");
            OutilAssertions.assertFalse(auth.aLeDroit(Droit.GESTION_ADHERENTS), "Trésorier NE peut PAS modifier les adhérents");
            OutilAssertions.assertFalse(auth.aLeDroit(Droit.IMPRESSION_CLUBS), "Trésorier NE peut PAS imprimer les clubs");
            auth.deconnecter();

            // 3. PRESIDENT
            auth.connecter("president", "President2026!".toCharArray());
            OutilAssertions.assertTrue(auth.aLeDroit(Droit.CONSULTATION_CLUBS), "Président consulte les clubs");
            OutilAssertions.assertTrue(auth.aLeDroit(Droit.IMPRESSION_CLUBS), "Président imprime les clubs");
            OutilAssertions.assertTrue(auth.aLeDroit(Droit.CONSULTATION_ADHERENTS), "Président consulte les adhérents");
            OutilAssertions.assertFalse(auth.aLeDroit(Droit.GESTION_ADHERENTS), "Président NE modifie PAS les adhérents");
            OutilAssertions.assertFalse(auth.aLeDroit(Droit.GESTION_FINANCES), "Président NE gère PAS les finances");
            auth.deconnecter();

            // 4. ADMINISTRATEUR (tous les droits)
            auth.connecter("admin", "AdminM2L2026!".toCharArray());
            for (Droit droit : Droit.values()) {
                OutilAssertions.assertTrue(auth.aLeDroit(droit),
                        "L'administrateur doit disposer de tous les droits applicatifs : " + droit);
            }
            auth.deconnecter();
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    private static void testerPersistanceFichierEtRechargement() throws Exception {
        Path fichierTemp = null;
        try {
            fichierTemp = Files.createTempFile("utilisateurs_test_", ".dat");
            // Crée un repository sur ce fichier temporaire
            UtilisateurRepository repo1 = new UtilisateurRepository(fichierTemp);

            // Vérifie qu'il a bien créé les 4 utilisateurs et sauvegardé
            OutilAssertions.assertEquals(4, repo1.tous().size(), "Repo 1 doit contenir 4 comptes par défaut");
            OutilAssertions.assertTrue(Files.size(fichierTemp) > 0, "Le fichier de persistance doit contenir des données");

            // Modifie un mot de passe et change le rôle d'un utilisateur pour tester la persistance
            Utilisateur secr = repo1.parIdentifiant("secretaire").orElseThrow();
            byte[] nouveauSel = UtilisateurRepository.genererSel();
            byte[] nouveauHash = UtilisateurRepository.hacherMotDePasse("NouveauMdpSecr2026!".toCharArray(), nouveauSel);
            secr.reinitialiserMotDePasse(nouveauHash, nouveauSel);
            secr.incrementerEchec();
            repo1.sauvegarder();

            // Instancie un nouveau repository sur le même fichier : relecture intégrale
            UtilisateurRepository repo2 = new UtilisateurRepository(fichierTemp);
            OutilAssertions.assertEquals(4, repo2.tous().size(), "Repo 2 doit avoir rechargé les 4 comptes");

            Optional<Utilisateur> optSecr = repo2.parIdentifiant("secretaire");
            OutilAssertions.assertTrue(optSecr.isPresent(), "Secrétaire doit être rechargé avec succès");
            Utilisateur secrRecharge = optSecr.get();

            OutilAssertions.assertEquals(Role.SECRETAIRE, secrRecharge.getRole(), "Rôle rechargé identique");
            OutilAssertions.assertEquals(1, secrRecharge.getTentativesEchouees(), "Nombre de tentatives échouées conservé");

            // Vérification du mot de passe modifié
            OutilAssertions.assertTrue(
                    UtilisateurRepository.verifierMotDePasse("NouveauMdpSecr2026!".toCharArray(),
                            secrRecharge.getMotDePasseHache(),
                            secrRecharge.getSel()),
                    "L'authentification sur le mot de passe modifié rechargé doit réussir"
            );
            OutilAssertions.assertFalse(
                    UtilisateurRepository.verifierMotDePasse("Secretaire2026!".toCharArray(),
                            secrRecharge.getMotDePasseHache(),
                            secrRecharge.getSel()),
                    "L'ancien mot de passe ne doit plus matcher"
            );

        } finally {
            if (fichierTemp != null) {
                try {
                    Files.deleteIfExists(fichierTemp);
                } catch (IOException ignored) {
                }
            }
        }
    }
}
