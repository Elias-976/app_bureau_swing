/**
 * Point d'entrée principal de la suite de tests (unitaires et d'acceptation) du projet TP-GEA.
 * Exécutable sans dépendance externe via {@code javac} et {@code java}.
 */
public final class LanceurTests {

    private LanceurTests() {
    }

    public static void main(String[] args) {
        OutilAssertions.reinitialiserCompteur();
        int echecs = 0;

        echecs += executerSuite("CategorieEtTarifTest (US6)", CategorieEtTarifTest::executer);
        echecs += executerSuite("ValidateurAdherentTest (US13)", ValidateurAdherentTest::executer);
        echecs += executerSuite("RecapitulatifFinancierTest (US9)", RecapitulatifFinancierTest::executer);
        echecs += executerSuite("SauvegardeEtAnnuaireTest (US10/US11/US7)", SauvegardeEtAnnuaireTest::executer);

        System.out.println("============================================================");
        System.out.printf("Total assertions vérifiées : %d | Suites en échec : %d%n",
                OutilAssertions.getNombreAssertions(), echecs);
        if (echecs > 0) {
            System.exit(1);
        }
    }

    @FunctionalInterface
    private interface SuiteTest {
        void lancer() throws Exception;
    }

    private static int executerSuite(String nom, SuiteTest suite) {
        int avant = OutilAssertions.getNombreAssertions();
        try {
            suite.lancer();
            int nb = OutilAssertions.getNombreAssertions() - avant;
            System.out.printf("[SUCCÈS] %-42s (%d assertions)%n", nom, nb);
            return 0;
        } catch (Throwable t) {
            System.err.printf("[ÉCHEC]  %s : %s%n", nom, t.getMessage());
            t.printStackTrace(System.err);
            return 1;
        }
    }
}

