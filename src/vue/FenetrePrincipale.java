package vue;

import modele.Droit;
import modele.Utilisateur;

import javax.swing.*;
import java.awt.*;

/**
 * Fenêtre principale de l'application GEA — Cercle d'Escrime de Vandœuvre.
 *
 * <p>Sert de conteneur racine de l'interface graphique (architecture MVC en Java Swing pur).
 * Affiche l'identité de l'utilisateur connecté, son rôle, et adapte l'accessibilité
 * des menus et raccourcis selon ses permissions RBAC.</p>
 */
public class FenetrePrincipale extends JFrame {

    // ── Éléments de menu (exposés aux contrôleurs) ─────────────────────────────
    private JMenuBar  barreMenus;

    // Menu Session
    private JMenu     menuSession;
    private JMenuItem menuItemDeconnexion;
    private JMenuItem menuItemChangerMdp;

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

    // ── Bandeau de session utilisateur ─────────────────────────────────────────
    private JPanel    panneauSession;
    private JLabel    lblInfoSession;
    private JButton   btnDeconnexion;
    private JButton   btnChangerMotDePasse;

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

        // — Menu Session —
        menuSession         = new JMenu("Session");
        menuItemChangerMdp  = new JMenuItem("Changer de mot de passe");
        menuItemDeconnexion = new JMenuItem("Déconnexion");
        menuSession.add(menuItemChangerMdp);
        menuSession.addSeparator();
        menuSession.add(menuItemDeconnexion);

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

        barreMenus.add(menuSession);
        barreMenus.add(menuAdherents);
        barreMenus.add(menuClubs);
        barreMenus.add(menuFinances);
        barreMenus.add(menuSauvegarde);
        setJMenuBar(barreMenus);

        // ── Panneau central ────────────────────────────────────────────────────
        JPanel panneauPrincipal = new JPanel(new BorderLayout(12, 12));
        panneauPrincipal.setBorder(BorderFactory.createEmptyBorder(12, 14, 16, 14));
        panneauPrincipal.setBackground(Color.WHITE);

        // ── Bandeau de session (Nord) ──────────────────────────────────────────
        panneauSession = new JPanel(new BorderLayout(10, 0));
        panneauSession.setBackground(new Color(241, 245, 249));
        panneauSession.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
                BorderFactory.createEmptyBorder(7, 12, 7, 12)
        ));

        lblInfoSession = new JLabel("👤 Non connecté");
        lblInfoSession.setFont(lblInfoSession.getFont().deriveFont(Font.BOLD, 12.5f));
        lblInfoSession.setForeground(new Color(30, 41, 59));

        JPanel panneauActionsSession = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        panneauActionsSession.setOpaque(false);

        btnChangerMotDePasse = new JButton("🔑 Mot de passe");
        btnChangerMotDePasse.setFocusPainted(false);

        btnDeconnexion = new JButton("🚪 Déconnexion");
        btnDeconnexion.setFocusPainted(false);
        btnDeconnexion.setForeground(new Color(185, 28, 28));

        panneauActionsSession.add(btnChangerMotDePasse);
        panneauActionsSession.add(btnDeconnexion);

        panneauSession.add(lblInfoSession, BorderLayout.WEST);
        panneauSession.add(panneauActionsSession, BorderLayout.EAST);
        panneauPrincipal.add(panneauSession, BorderLayout.NORTH);

        // ── Corps de bienvenue ─────────────────────────────────────────────────
        JLabel labelBienvenue = new JLabel(
                "<html><center><h2>Bienvenue dans l'application GEA</h2>"
                + "<p>Cercle d'Escrime de Vandœuvre — Maison des Ligues de Lorraine</p></center></html>",
                SwingConstants.CENTER);
        panneauPrincipal.add(labelBienvenue, BorderLayout.CENTER);

        // ── Boutons d'accès rapide (Sud) ───────────────────────────────────────
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
        setSize(960, 620);
        setMinimumSize(new Dimension(740, 480));
        setLocationRelativeTo(null);
    }

    /**
     * Affiche la fenêtre principale.
     */
    public void afficher() {
        setVisible(true);
    }

    /**
     * Adapte l'état des composants de l'interface en fonction de l'utilisateur connecté
     * et de ses privilèges RBAC.
     *
     * @param utilisateur l'utilisateur connecté, ou {@code null} si déconnecté
     */
    public void appliquerDroits(Utilisateur utilisateur) {
        if (utilisateur != null) {
            lblInfoSession.setText(String.format(
                    "<html>👤 Connecté : <b>%s</b> &nbsp;|&nbsp; Profil : <font color='#1D4ED8'><b>%s</b></font></html>",
                    utilisateur.getNomComplet(),
                    utilisateur.getRole().getLibelleCourt()));

            boolean peutGererAdherents     = utilisateur.possedeDroit(Droit.GESTION_ADHERENTS);
            boolean peutConsulterAdherents = utilisateur.possedeDroit(Droit.CONSULTATION_ADHERENTS);
            boolean peutFinances           = utilisateur.possedeDroit(Droit.GESTION_FINANCES);
            boolean peutSauvegarder        = utilisateur.possedeDroit(Droit.SAUVEGARDE_RESTAURATION);
            boolean peutClubs              = utilisateur.possedeDroit(Droit.CONSULTATION_CLUBS);

            // Gestion adhérents (Secrétaire / Admin)
            btnNouvelAdherent.setEnabled(peutGererAdherents);
            menuItemNouvelAdherent.setEnabled(peutGererAdherents);
            btnNouvelAdherent.setToolTipText(peutGererAdherents
                    ? "Enregistrer un nouvel adhérent"
                    : "Réservé au secrétariat du club");

            // Consultation adhérents (Tous)
            btnRecherche.setEnabled(peutConsulterAdherents);
            menuItemRechercherAdherent.setEnabled(peutConsulterAdherents);

            // Finances (Trésorier / Admin)
            btnFinances.setEnabled(peutFinances);
            menuFinances.setEnabled(peutFinances);
            menuItemRecapitulatif.setEnabled(peutFinances);
            btnFinances.setToolTipText(peutFinances
                    ? "Consulter le récapitulatif financier"
                    : "Réservé au trésorier du club");

            // Sauvegarde & Restauration (Trésorier / Admin)
            menuSauvegarde.setEnabled(peutSauvegarder);
            menuItemExporter.setEnabled(peutSauvegarder);
            menuItemImporter.setEnabled(peutSauvegarder);
            menuSauvegarde.setToolTipText(peutSauvegarder
                    ? "Sauvegardes chiffrées"
                    : "Réservé au trésorier du club");

            // Clubs (Président / Secrétaire / Admin)
            btnAnnuaire.setEnabled(peutClubs);
            menuClubs.setEnabled(peutClubs);
            menuItemAnnuaireClubs.setEnabled(peutClubs);
            btnAnnuaire.setToolTipText(peutClubs
                    ? "Consulter l'annuaire des clubs"
                    : "Accès restreint");

            menuSession.setEnabled(true);
            btnDeconnexion.setEnabled(true);
            btnChangerMotDePasse.setEnabled(true);

        } else {
            lblInfoSession.setText("👤 Non connecté");
            btnNouvelAdherent.setEnabled(false);
            btnRecherche.setEnabled(false);
            btnAnnuaire.setEnabled(false);
            btnFinances.setEnabled(false);

            menuAdherents.setEnabled(false);
            menuClubs.setEnabled(false);
            menuFinances.setEnabled(false);
            menuSauvegarde.setEnabled(false);
            menuSession.setEnabled(false);
            btnDeconnexion.setEnabled(false);
            btnChangerMotDePasse.setEnabled(false);
        }
    }

    // ── Getters — Menus ────────────────────────────────────────────────────────

    public JMenu     getMenuSession()                 { return menuSession; }
    public JMenuItem getMenuItemDeconnexion()         { return menuItemDeconnexion; }
    public JMenuItem getMenuItemChangerMdp()          { return menuItemChangerMdp; }

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

    public JButton getBtnNouvelAdherent()    { return btnNouvelAdherent; }
    public JButton getBtnRecherche()         { return btnRecherche; }
    public JButton getBtnAnnuaire()          { return btnAnnuaire; }
    public JButton getBtnFinances()          { return btnFinances; }

    // ── Getters — Session ──────────────────────────────────────────────────────

    public JButton getBtnDeconnexion()       { return btnDeconnexion; }
    public JButton getBtnChangerMotDePasse() { return btnChangerMotDePasse; }
}
