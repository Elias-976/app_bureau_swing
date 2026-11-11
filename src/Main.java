import vue.FenetrePrincipale;
import javax.swing.SwingUtilities;

/**
 * Point d'entrée de l'application GEA — Cercle d'Escrime de Vandœuvre.
 * Lance l'interface graphique Swing dans l'EDT (Event Dispatch Thread).
 */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(FenetrePrincipale::new);
    }
}
