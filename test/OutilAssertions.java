/**
 * Utilitaire léger d'assertions pour la suite de tests autonome (sans dépendance externe).
 * Comptabilise les vérifications exécutées et produit des messages d'échec explicites en français.
 */
public final class OutilAssertions {

    private static int nombreAssertions = 0;

    private OutilAssertions() {
    }

    public static void reinitialiserCompteur() {
        nombreAssertions = 0;
    }

    public static int getNombreAssertions() {
        return nombreAssertions;
    }

    public static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError("Échec assertion (attendu true) : " + message);
        }
        nombreAssertions++;
    }

    public static void assertFalse(boolean condition, String message) {
        if (condition) {
            throw new AssertionError("Échec assertion (attendu false) : " + message);
        }
        nombreAssertions++;
    }

    public static void assertNull(Object valeur, String message) {
        if (valeur != null) {
            throw new AssertionError("Échec assertion (attendu null, obtenu : " + valeur + ") : " + message);
        }
        nombreAssertions++;
    }

    public static void assertNotNull(Object valeur, String message) {
        if (valeur == null) {
            throw new AssertionError("Échec assertion (attendu non-null) : " + message);
        }
        nombreAssertions++;
    }

    public static void assertEquals(Object attendu, Object obtenu, String message) {
        if (attendu == null ? obtenu != null : !attendu.equals(obtenu)) {
            throw new AssertionError(
                    String.format("Échec assertion (%s) — attendu : <%s>, obtenu : <%s>",
                            message, attendu, obtenu));
        }
        nombreAssertions++;
    }

    public static void assertEqualsDouble(double attendu, double obtenu, double delta, String message) {
        if (Double.isNaN(obtenu) || Math.abs(attendu - obtenu) > delta) {
            throw new AssertionError(
                    String.format("Échec assertion (%s) — attendu : <%.4f>, obtenu : <%.4f>",
                            message, attendu, obtenu));
        }
        nombreAssertions++;
    }

    public static void assertContains(String texte, String sousChaine, String message) {
        if (texte == null || !texte.contains(sousChaine)) {
            throw new AssertionError(
                    String.format("Échec assertion (%s) — sous-chaîne absente : <%s>",
                            message, sousChaine));
        }
        nombreAssertions++;
    }

    public static void assertNotContains(String texte, String sousChaine, String message) {
        if (texte != null && texte.contains(sousChaine)) {
            throw new AssertionError(
                    String.format("Échec assertion (%s) — sous-chaîne interdite présente : <%s>",
                            message, sousChaine));
        }
        nombreAssertions++;
    }

    @FunctionalInterface
    public interface ActionTest {
        void executer() throws Throwable;
    }

    public static <T extends Throwable> T assertThrows(Class<T> typeAttendu,
                                                       ActionTest action,
                                                       String message) {
        try {
            action.executer();
        } catch (Throwable t) {
            if (typeAttendu.isInstance(t)) {
                nombreAssertions++;
                return typeAttendu.cast(t);
            }
            throw new AssertionError(
                    String.format("Échec assertion (%s) — exception attendue : %s, obtenue : %s (%s)",
                            message, typeAttendu.getSimpleName(),
                            t.getClass().getSimpleName(), t.getMessage()),
                    t);
        }
        throw new AssertionError(
                String.format("Échec assertion (%s) — aucune exception levée (attendu : %s)",
                        message, typeAttendu.getSimpleName()));
    }
}

