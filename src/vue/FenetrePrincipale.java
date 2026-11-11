package vue;

import javax.swing.*;
import java.awt.*;

/**
 * Fenêtre principale de l'application GEA — Cercle d'Escrime de Vandœuvre.
 * Sert de conteneur racine de l'interface graphique (architecture MVC).
 */
public class FenetrePrincipale extends JFrame {

    // ── Composants de navigation ───────────────────────────────────────────────
    private JMenuBar  barreMenus;
    private JMenu     menuAdherents;
    private JMenu     menuClubs;
    private JMenu     menuFinances;
    private JMenu     menuSauvegarde;

    private JPanel    panneauPrincipal;
    private JLabel    labelBienvenue;
    private JPanel    panneauBoutons;

    // ── Constructeur ───────────────────────────────────────────────────────────

    public FenetrePrincipale() {
        super("GEA — Cercle d'Escrime de Vandœuvre (M2L)");
        initialiserComposants();
        configurerFenetre();
    }

    // ── Initialisation ─────────────────────────────────────────────────────────

    /**
     * Crée et dispose tous les composants graphiques de la fenêtre principale.
     */
    private void initialiserComposants() {
        // ── Barre de menus ─────────────────────────────────────────────────────
        barreMenus = new JMenuBar();

        menuAdherents  = new JMenu("Adhérents");
        menuClubs      = new JMenu("Clubs");
        menuFinances   = new JMenu("Finances");
        menuSauvegarde = new JMenu("Sauvegarde");

        // Items du menu Adhérents (remplis plus tard dans les US)
        menuAdherents.add(new JMenuItem("Nouvel adhérent"));
        menuAdherents.add(new JMenuItem("Rechercher un adhérent"));
        menuAdherents.addSeparator();
        menuAdherents.add(new JMenuItem("Quitter"));

        // Items du menu Clubs
        menuClubs.add(new JMenuItem("Annuaire des clubs"));

        // Items du menu Finances
        menuFinances.add(new JMenuItem("Récapitulatif financier"));

        // Items du menu Sauvegarde
        menuSauvegarde.add(new JMenuItem("Exporter les données"));
        menuSauvegarde.add(new JMenuItem("Importer les données"));

        barreMenus.add(menuAdherents);
        barreMenus.add(menuClubs);
        barreMenus.add(menuFinances);
        barreMenus.add(menuSauvegarde);

        setJMenuBar(barreMenus);

        // ── Panneau central ────────────────────────────────────────────────────
        panneauPrincipal = new JPanel(new BorderLayout(10, 10));
        panneauPrincipal.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panneauPrincipal.setBackground(Color.WHITE);

        // Message de bienvenue centré
        labelBienvenue = new JLabel(
                "<html><center><h2>Bienvenue dans l'application GEA</h2>"
                + "<p>Cercle d'Escrime de Vandœuvre — Maison des Ligues de Lorraine</p></center></html>",
                SwingConstants.CENTER);
        panneauPrincipal.add(labelBienvenue, BorderLayout.CENTER);

        // Panneau de boutons d'accès rapide (bas)
        panneauBoutons = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        panneauBoutons.setBackground(Color.WHITE);

        JButton btnNouvelAdherent  = new JButton("➕ Nouvel adhérent");
        JButton btnRecherche       = new JButton("🔍 Rechercher");
        JButton btnAnnuaire        = new JButton("📋 Annuaire clubs");
        JButton btnFinances        = new JButton("💰 Finances");

        panneauBoutons.add(btnNouvelAdherent);
        panneauBoutons.add(btnRecherche);
        panneauBoutons.add(btnAnnuaire);
        panneauBoutons.add(btnFinances);

        panneauPrincipal.add(panneauBoutons, BorderLayout.SOUTH);

        add(panneauPrincipal);
    }

    /**
     * Configure les propriétés générales de la JFrame.
     */
    private void configurerFenetre() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setMinimumSize(new Dimension(700, 450));
        setLocationRelativeTo(null); // centrer sur l'écran
        setVisible(true);
    }

    // ── Getters (pour les contrôleurs) ─────────────────────────────────────────

    public JMenu getMenuAdherents()  { return menuAdherents; }
    public JMenu getMenuClubs()      { return menuClubs; }
    public JMenu getMenuFinances()   { return menuFinances; }
    public JMenu getMenuSauvegarde() { return menuSauvegarde; }
}
