import controleur.AdherentControleur;
import manager.AdherentManager;
import vue.FenetrePrincipale;

import javax.swing.SwingUtilities;

/**
 * Point d'entrée de l'application GEA — Cercle d'Escrime de Vandœuvre.
 * Instancie le modèle, la vue et les contrôleurs (architecture MVC),
 * puis lance l'interface graphique Swing dans l'EDT.
 */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // ── Modèle ────────────────────────────────────────────────────────
            AdherentManager adherentManager = new AdherentManager();

            // ── Vue principale ────────────────────────────────────────────────
            FenetrePrincipale fenetrePrincipale = new FenetrePrincipale();

            // ── Contrôleurs ───────────────────────────────────────────────────
            new AdherentControleur(fenetrePrincipale, adherentManager);
        });
    }
}
