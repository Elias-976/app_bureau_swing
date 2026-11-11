package vue;

import javax.swing.*;
import java.awt.*;

/**
 * Fenêtre principale de l'application GEA — Cercle d'Escrime de Vandœuvre.
 * Sert de conteneur racine de l'interface graphique (architecture MVC).
 * Expose tous les composants interactifs via des getters pour les contrôleurs.
 */
public class FenetrePrincipale extends JFrame {

    // ── Éléments de menu (exposés aux contrôleurs) ─────────────────────────────
    private JMenuBar  barreMenus;

    // Menu Adhérents
    private JMenu     menuAdherents;
    private JMenuItem menuItemNouvelAdherent;
    private JMenuItem menuItemRechercherAdherent;
    private JMenuItem menuItemQuitter;

    // Menu Clubs
    private JMenu     menuClubs;
    private JMenuItem menuItemAnnuaireClubs;

    // Menu Finances
    private JMenu     menuFinances;
    private JMenuItem menuItemRecapitulatif;

    // Menu Sauvegarde
    private JMenu     menuSauvegarde;
    private JMenuItem menuItemExporter;
    private JMenuItem menuItemImporter;

    // ── Boutons d'accès rapide (exposés aux contrôleurs) ──────────────────────
    private JButton btnNouvelAdherent;
    private JButton btnRecherche;
    private JButton btnAnnuaire;
    private JButton btnFinances;

    // ── Constructeur ───────────────────────────────────────────────────────────

    public FenetrePrincipale() {
        super("GEA — Cercle d'Escrime de Vandœuvre (M2L)");
        initialiserComposants();
        configurerFenetre();
    }

    // ── Initialisation ─────────────────────────────────────────────────────────

    private void initialiserComposants() {
        // ── Barre de menus ─────────────────────────────────────────────────────
        barreMenus = new JMenuBar();

        // — Menu Adhérents —
        menuAdherents              = new JMenu("Adhérents");
        menuItemNouvelAdherent     = new JMenuItem("Nouvel adhérent");
        menuItemRechercherAdherent = new JMenuItem("Rechercher un adhérent");
        menuItemQuitter            = new JMenuItem("Quitter");

        menuAdherents.add(menuItemNouvelAdherent);
        menuAdherents.add(menuItemRechercherAdherent);
        menuAdherents.addSeparator();
        menuAdherents.add(menuItemQuitter);

        // — Menu Clubs —
        menuClubs             = new JMenu("Clubs");
        menuItemAnnuaireClubs = new JMenuItem("Annuaire des clubs");
        menuClubs.add(menuItemAnnuaireClubs);

        // — Menu Finances —
        menuFinances          = new JMenu("Finances");
        menuItemRecapitulatif = new JMenuItem("Récapitulatif financier");
        menuFinances.add(menuItemRecapitulatif);

        // — Menu Sauvegarde —
        menuSauvegarde    = new JMenu("Sauvegarde");
        menuItemExporter  = new JMenuItem("Exporter les données");
        menuItemImporter  = new JMenuItem("Importer les données");
        menuSauvegarde.add(menuItemExporter);
        menuSauvegarde.add(menuItemImporter);

        barreMenus.add(menuAdherents);
        barreMenus.add(menuClubs);
        barreMenus.add(menuFinances);
        barreMenus.add(menuSauvegarde);
        setJMenuBar(barreMenus);

        // ── Panneau central ────────────────────────────────────────────────────
        JPanel panneauPrincipal = new JPanel(new BorderLayout(10, 10));
        panneauPrincipal.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panneauPrincipal.setBackground(Color.WHITE);

        JLabel labelBienvenue = new JLabel(
                "<html><center><h2>Bienvenue dans l'application GEA</h2>"
                + "<p>Cercle d'Escrime de Vandœuvre — Maison des Ligues de Lorraine</p></center></html>",
                SwingConstants.CENTER);
        panneauPrincipal.add(labelBienvenue, BorderLayout.CENTER);

        // ── Boutons d'accès rapide ─────────────────────────────────────────────
        btnNouvelAdherent = new JButton("➕ Nouvel adhérent");
        btnRecherche      = new JButton("🔍 Rechercher");
        btnAnnuaire       = new JButton("📋 Annuaire clubs");
        btnFinances       = new JButton("💰 Finances");

        JPanel panneauBoutons = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        panneauBoutons.setBackground(Color.WHITE);
        panneauBoutons.add(btnNouvelAdherent);
        panneauBoutons.add(btnRecherche);
        panneauBoutons.add(btnAnnuaire);
        panneauBoutons.add(btnFinances);

        panneauPrincipal.add(panneauBoutons, BorderLayout.SOUTH);
        add(panneauPrincipal);
    }

    private void configurerFenetre() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setMinimumSize(new Dimension(700, 450));
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // ── Getters — Menus ────────────────────────────────────────────────────────

    public JMenu     getMenuAdherents()               { return menuAdherents; }
    public JMenu     getMenuClubs()                   { return menuClubs; }
    public JMenu     getMenuFinances()                { return menuFinances; }
    public JMenu     getMenuSauvegarde()              { return menuSauvegarde; }

    public JMenuItem getMenuItemNouvelAdherent()      { return menuItemNouvelAdherent; }
    public JMenuItem getMenuItemRechercherAdherent()  { return menuItemRechercherAdherent; }
    public JMenuItem getMenuItemQuitter()             { return menuItemQuitter; }
    public JMenuItem getMenuItemAnnuaireClubs()       { return menuItemAnnuaireClubs; }
    public JMenuItem getMenuItemRecapitulatif()       { return menuItemRecapitulatif; }
    public JMenuItem getMenuItemExporter()            { return menuItemExporter; }
    public JMenuItem getMenuItemImporter()            { return menuItemImporter; }

    // ── Getters — Boutons rapides ──────────────────────────────────────────────

    public JButton getBtnNouvelAdherent()  { return btnNouvelAdherent; }
    public JButton getBtnRecherche()       { return btnRecherche; }
    public JButton getBtnAnnuaire()        { return btnAnnuaire; }
    public JButton getBtnFinances()        { return btnFinances; }
}
