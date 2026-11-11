package vue;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * Boîte de dialogue (JDialog) permettant la saisie d'un nouvel adhérent
 * ou la modification d'un adhérent existant.
 *
 * <p>Architecture MVC : cette vue ne contient aucune logique métier.
 * Elle expose des getters pour chaque champ et des méthodes pour
 * brancher les écouteurs (contrôleur).</p>
 */
public class FormulaireAdherentVue extends JDialog {

    // ── Champs d'identité ──────────────────────────────────────────────────────
    private JTextField champNom;
    private JTextField champPrenom;
    private JTextField champNomNaissance;
    private JTextField champDateNaissance;   // format attendu : JJ/MM/AAAA

    // ── Champs de coordonnées ──────────────────────────────────────────────────
    private JTextField champAdresse;
    private JTextField champCodePostal;
    private JTextField champVille;
    private JTextField champTelephone;
    private JTextField champEmail;

    // ── Champs spécifiques escrime ─────────────────────────────────────────────
    private JComboBox<String> comboArme;
    private JComboBox<String> comboPratique;
    private JComboBox<String> comboLateralite;

    // ── Champs responsable légal (mineur) ─────────────────────────────────────
    private JTextField champNomRespLegal;
    private JTextField champPrenomRespLegal;
    private JPanel     panneauRespLegal;

    // ── Boutons ────────────────────────────────────────────────────────────────
    private JButton btnValider;
    private JButton btnAnnuler;

    // ── Message d'erreur ───────────────────────────────────────────────────────
    private JLabel labelErreur;

    // ── Constantes ─────────────────────────────────────────────────────────────
    private static final String[] ARMES       = {"Fleuret", "Épée", "Sabre"};
    private static final String[] PRATIQUES   = {"Compétition", "Loisir"};
    private static final String[] LATERALITES = {"Droitier", "Gaucher"};

    // ── Constructeurs ──────────────────────────────────────────────────────────

    /**
     * Crée un formulaire de saisie modale rattaché à la fenêtre parente.
     *
     * @param parent  la fenêtre parente
     * @param titre   titre de la boîte de dialogue
     */
    public FormulaireAdherentVue(Frame parent, String titre) {
        super(parent, titre, true);
        initialiserComposants();
        configurerFenetre();
    }

    // ── Initialisation ─────────────────────────────────────────────────────────

    private void initialiserComposants() {
        JPanel panneauGlobal = new JPanel(new BorderLayout(10, 10));
        panneauGlobal.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));

        // ── Formulaire central (grille) ────────────────────────────────────────
        JPanel panneauFormulaire = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets  = new Insets(5, 5, 5, 5);
        gbc.fill    = GridBagConstraints.HORIZONTAL;
        gbc.anchor  = GridBagConstraints.WEST;

        // --- Section Identité -------------------------------------------------
        JPanel panneauIdentite = new JPanel(new GridBagLayout());
        panneauIdentite.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Identité",
                TitledBorder.LEFT, TitledBorder.TOP));

        champNom            = new JTextField(20);
        champPrenom         = new JTextField(20);
        champNomNaissance   = new JTextField(20);
        champDateNaissance  = new JTextField(10);
        champDateNaissance.setToolTipText("Format : JJ/MM/AAAA");

        ajouterLigneFormulaire(panneauIdentite, "Nom * :",         champNom,           0);
        ajouterLigneFormulaire(panneauIdentite, "Prénom * :",      champPrenom,        1);
        ajouterLigneFormulaire(panneauIdentite, "Nom de naissance :", champNomNaissance, 2);
        ajouterLigneFormulaire(panneauIdentite, "Date de naissance * (JJ/MM/AAAA) :",
                               champDateNaissance, 3);

        // --- Section Coordonnées ---------------------------------------------
        JPanel panneauCoordonnees = new JPanel(new GridBagLayout());
        panneauCoordonnees.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Coordonnées",
                TitledBorder.LEFT, TitledBorder.TOP));

        champAdresse    = new JTextField(30);
        champCodePostal = new JTextField(10);
        champVille      = new JTextField(20);
        champTelephone  = new JTextField(15);
        champEmail      = new JTextField(25);

        ajouterLigneFormulaire(panneauCoordonnees, "Adresse :",      champAdresse,    0);
        ajouterLigneFormulaire(panneauCoordonnees, "Code postal :",  champCodePostal, 1);
        ajouterLigneFormulaire(panneauCoordonnees, "Ville :",        champVille,      2);
        ajouterLigneFormulaire(panneauCoordonnees, "Téléphone * :",  champTelephone,  3);
        ajouterLigneFormulaire(panneauCoordonnees, "Email * :",      champEmail,      4);

        // --- Section Escrime -------------------------------------------------
        JPanel panneauEscrime = new JPanel(new GridBagLayout());
        panneauEscrime.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Pratique sportive",
                TitledBorder.LEFT, TitledBorder.TOP));

        comboArme       = new JComboBox<>(ARMES);
        comboPratique   = new JComboBox<>(PRATIQUES);
        comboLateralite = new JComboBox<>(LATERALITES);

        ajouterLigneFormulaire(panneauEscrime, "Arme :",        comboArme,       0);
        ajouterLigneFormulaire(panneauEscrime, "Pratique :",    comboPratique,   1);
        ajouterLigneFormulaire(panneauEscrime, "Latéralité :",  comboLateralite, 2);

        // --- Section Responsable légal (initialement masquée) ----------------
        panneauRespLegal = new JPanel(new GridBagLayout());
        panneauRespLegal.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Responsable légal (mineur)",
                TitledBorder.LEFT, TitledBorder.TOP));
        panneauRespLegal.setVisible(false);

        champNomRespLegal    = new JTextField(20);
        champPrenomRespLegal = new JTextField(20);

        ajouterLigneFormulaire(panneauRespLegal, "Nom :",    champNomRespLegal,    0);
        ajouterLigneFormulaire(panneauRespLegal, "Prénom :", champPrenomRespLegal, 1);

        // ── Assemblage vertical des sections ───────────────────────────────────
        panneauFormulaire.setLayout(new BoxLayout(panneauFormulaire, BoxLayout.Y_AXIS));
        panneauFormulaire.add(panneauIdentite);
        panneauFormulaire.add(Box.createVerticalStrut(8));
        panneauFormulaire.add(panneauCoordonnees);
        panneauFormulaire.add(Box.createVerticalStrut(8));
        panneauFormulaire.add(panneauEscrime);
        panneauFormulaire.add(Box.createVerticalStrut(8));
        panneauFormulaire.add(panneauRespLegal);

        // ── Zone de défilement ─────────────────────────────────────────────────
        JScrollPane scrollPane = new JScrollPane(panneauFormulaire);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(null);

        // ── Étiquette d'erreur ─────────────────────────────────────────────────
        labelErreur = new JLabel(" ");
        labelErreur.setForeground(Color.RED);
        labelErreur.setFont(labelErreur.getFont().deriveFont(Font.BOLD));

        // ── Panneau de boutons ─────────────────────────────────────────────────
        btnValider = new JButton("✔ Valider");
        btnValider.setBackground(new Color(46, 139, 87));
        btnValider.setForeground(Color.WHITE);
        btnValider.setFocusPainted(false);

        btnAnnuler = new JButton("✖ Annuler");
        btnAnnuler.setBackground(new Color(178, 34, 34));
        btnAnnuler.setForeground(Color.WHITE);
        btnAnnuler.setFocusPainted(false);

        // L'annuler ferme simplement la fenêtre
        btnAnnuler.addActionListener(e -> dispose());

        JPanel panneauBoutons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panneauBoutons.add(labelErreur);
        panneauBoutons.add(btnAnnuler);
        panneauBoutons.add(btnValider);

        // ── Assemblage global ──────────────────────────────────────────────────
        panneauGlobal.add(scrollPane,     BorderLayout.CENTER);
        panneauGlobal.add(panneauBoutons, BorderLayout.SOUTH);

        add(panneauGlobal);
    }

    /**
     * Méthode utilitaire : ajoute une paire label/composant sur une ligne
     * d'un panneau utilisant {@link GridBagLayout}.
     */
    private void ajouterLigneFormulaire(JPanel panneau, String libelle,
                                         JComponent composant, int ligne) {
        GridBagConstraints gbcLabel = new GridBagConstraints();
        gbcLabel.gridx   = 0;
        gbcLabel.gridy   = ligne;
        gbcLabel.insets  = new Insets(4, 6, 4, 6);
        gbcLabel.anchor  = GridBagConstraints.EAST;

        GridBagConstraints gbcChamp = new GridBagConstraints();
        gbcChamp.gridx   = 1;
        gbcChamp.gridy   = ligne;
        gbcChamp.insets  = new Insets(4, 2, 4, 6);
        gbcChamp.fill    = GridBagConstraints.HORIZONTAL;
        gbcChamp.weightx = 1.0;

        panneau.add(new JLabel(libelle), gbcLabel);
        panneau.add(composant,           gbcChamp);
    }

    private void configurerFenetre() {
        pack();
        setMinimumSize(new Dimension(560, 500));
        setSize(600, 650);
        setLocationRelativeTo(getParent());
        setResizable(true);
    }

    // ── API publique pour le contrôleur ────────────────────────────────────────

    /** Rend la section Responsable légal visible ou non (selon si l'adhérent est mineur). */
    public void afficherRespLegal(boolean visible) {
        panneauRespLegal.setVisible(visible);
        pack();
    }

    /** Affiche un message d'erreur en rouge dans la zone dédiée. */
    public void afficherErreur(String message) {
        labelErreur.setText(message);
    }

    /** Efface le message d'erreur. */
    public void effacerErreur() {
        labelErreur.setText(" ");
    }

    /** Vide tous les champs du formulaire. */
    public void reinitialiser() {
        champNom.setText("");
        champPrenom.setText("");
        champNomNaissance.setText("");
        champDateNaissance.setText("");
        champAdresse.setText("");
        champCodePostal.setText("");
        champVille.setText("");
        champTelephone.setText("");
        champEmail.setText("");
        champNomRespLegal.setText("");
        champPrenomRespLegal.setText("");
        comboArme.setSelectedIndex(0);
        comboPratique.setSelectedIndex(0);
        comboLateralite.setSelectedIndex(0);
        labelErreur.setText(" ");
    }

    // ── Enregistrement des écouteurs (pour le contrôleur) ─────────────────────

    /** Branche l'action déclenchée au clic sur "Valider". */
    public void ajouterEcouteurValider(ActionListener ecouteur) {
        btnValider.addActionListener(ecouteur);
    }

    // ── Getters des valeurs saisies ────────────────────────────────────────────

    public String getChampNom()            { return champNom.getText().trim(); }
    public String getChampPrenom()         { return champPrenom.getText().trim(); }
    public String getChampNomNaissance()   { return champNomNaissance.getText().trim(); }
    public String getChampDateNaissance()  { return champDateNaissance.getText().trim(); }
    public String getChampAdresse()        { return champAdresse.getText().trim(); }
    public String getChampCodePostal()     { return champCodePostal.getText().trim(); }
    public String getChampVille()          { return champVille.getText().trim(); }
    public String getChampTelephone()      { return champTelephone.getText().trim(); }
    public String getChampEmail()          { return champEmail.getText().trim(); }
    public String getArme()               { return (String) comboArme.getSelectedItem(); }
    public String getPratique()            { return (String) comboPratique.getSelectedItem(); }
    public String getLateralite()          { return (String) comboLateralite.getSelectedItem(); }
    public String getChampNomRespLegal()   { return champNomRespLegal.getText().trim(); }
    public String getChampPrenomRespLegal(){ return champPrenomRespLegal.getText().trim(); }

    // ── Setters (pré-remplissage pour la modification) ─────────────────────────

    public void setChampNom(String v)            { champNom.setText(v); }
    public void setChampPrenom(String v)         { champPrenom.setText(v); }
    public void setChampNomNaissance(String v)   { champNomNaissance.setText(v); }
    public void setChampDateNaissance(String v)  { champDateNaissance.setText(v); }
    public void setChampAdresse(String v)        { champAdresse.setText(v); }
    public void setChampCodePostal(String v)     { champCodePostal.setText(v); }
    public void setChampVille(String v)          { champVille.setText(v); }
    public void setChampTelephone(String v)      { champTelephone.setText(v); }
    public void setChampEmail(String v)          { champEmail.setText(v); }
    public void setArme(String v)               { comboArme.setSelectedItem(v); }
    public void setPratique(String v)            { comboPratique.setSelectedItem(v); }
    public void setLateralite(String v)          { comboLateralite.setSelectedItem(v); }
    public void setChampNomRespLegal(String v)   { champNomRespLegal.setText(v); }
    public void setChampPrenomRespLegal(String v){ champPrenomRespLegal.setText(v); }
}
