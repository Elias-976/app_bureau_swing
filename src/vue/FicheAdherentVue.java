package vue;

import modele.Adherent;
import modele.Categorie;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.time.format.DateTimeFormatter;

/**
 * Boîte de dialogue affichant la fiche détaillée (consultation) d'un adhérent.
 * Tous les champs sont en lecture seule. Boutons : Modifier et Fermer.
 */
public class FicheAdherentVue extends JDialog {

    // ── Composants ─────────────────────────────────────────────────────────────
    private JLabel lblNom, lblPrenom, lblNomNaissance, lblDateNaissance;
    private JLabel lblAge, lblCategorie, lblMineur;
    private JLabel lblAdresse, lblCodePostal, lblVille;
    private JLabel lblTelephone, lblEmail;
    private JLabel lblArme, lblPratique, lblLateralite;
    private JLabel lblNomRespLegal, lblPrenomRespLegal;
    private JPanel panneauRespLegal;

    // ── Boutons ────────────────────────────────────────────────────────────────
    private JButton btnModifier;
    private JButton btnFermer;

    private static final DateTimeFormatter FORMAT_DATE =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ── Constructeur ───────────────────────────────────────────────────────────

    public FicheAdherentVue(Frame parent, Adherent adherent, Categorie categorie) {
        super(parent,
              "Fiche adhérent — " + adherent.getPrenom() + " " + adherent.getNom(),
              true);
        initialiserComposants(adherent, categorie);
        configurerFenetre();
    }

    // ── Initialisation ─────────────────────────────────────────────────────────

    private void initialiserComposants(Adherent a, Categorie cat) {
        JPanel panneauGlobal = new JPanel(new BorderLayout(10, 10));
        panneauGlobal.setBorder(BorderFactory.createEmptyBorder(14, 14, 10, 14));

        JPanel panneauContenu = new JPanel();
        panneauContenu.setLayout(new BoxLayout(panneauContenu, BoxLayout.Y_AXIS));

        // ── Section Identité ───────────────────────────────────────────────────
        JPanel secIdentite = creerSection("Identité");
        lblNom           = ajouterLigne(secIdentite, "Nom :",            a.getNom(),            0);
        lblPrenom        = ajouterLigne(secIdentite, "Prénom :",         a.getPrenom(),         1);
        lblNomNaissance  = ajouterLigne(secIdentite, "Nom de naissance :", a.getNomNaissance(), 2);
        String dateStr   = a.getDateNaissance() != null
                           ? a.getDateNaissance().format(FORMAT_DATE) : "—";
        lblDateNaissance = ajouterLigne(secIdentite, "Date de naissance :", dateStr,            3);
        lblAge           = ajouterLigne(secIdentite, "Âge :",            a.getAge() + " ans",   4);
        lblMineur        = ajouterLigne(secIdentite, "Statut :",
                           a.estMineur() ? "Mineur" : "Majeur",                                 5);
        lblCategorie     = ajouterLigne(secIdentite, "Catégorie :",
                           cat != null ? cat.getLibelle() + (cat.getCode().isBlank()
                               ? "" : " (" + cat.getCode() + ")")
                               : "Non déterminée",                                              6);

        // Mettre la catégorie en gras et en bleu
        lblCategorie.setFont(lblCategorie.getFont().deriveFont(Font.BOLD));
        lblCategorie.setForeground(new Color(0, 70, 140));

        // ── Section Coordonnées ────────────────────────────────────────────────
        JPanel secCoord = creerSection("Coordonnées");
        lblAdresse    = ajouterLigne(secCoord, "Adresse :",     a.getAdresse(),    0);
        lblCodePostal = ajouterLigne(secCoord, "Code postal :", a.getCodePostal(), 1);
        lblVille      = ajouterLigne(secCoord, "Ville :",       a.getVille(),      2);
        lblTelephone  = ajouterLigne(secCoord, "Téléphone :",   a.getTelephone(),  3);
        lblEmail      = ajouterLigne(secCoord, "Email :",       a.getEmail(),      4);

        // ── Section Pratique sportive ──────────────────────────────────────────
        JPanel secEscrime = creerSection("Pratique sportive");
        lblArme       = ajouterLigne(secEscrime, "Arme :",       a.getArme(),       0);
        lblPratique   = ajouterLigne(secEscrime, "Pratique :",   a.getPratique(),   1);
        lblLateralite = ajouterLigne(secEscrime, "Latéralité :", a.getLateralite(), 2);

        // ── Section Responsable légal (uniquement si mineur) ───────────────────
        panneauRespLegal = creerSection("Responsable légal");
        lblNomRespLegal    = ajouterLigne(panneauRespLegal, "Nom :",
                                          a.getNomRespLegal(),    0);
        lblPrenomRespLegal = ajouterLigne(panneauRespLegal, "Prénom :",
                                          a.getPrenomRespLegal(), 1);
        panneauRespLegal.setVisible(a.estMineur());

        // ── Assemblage ─────────────────────────────────────────────────────────
        panneauContenu.add(secIdentite);
        panneauContenu.add(Box.createVerticalStrut(8));
        panneauContenu.add(secCoord);
        panneauContenu.add(Box.createVerticalStrut(8));
        panneauContenu.add(secEscrime);
        panneauContenu.add(Box.createVerticalStrut(8));
        panneauContenu.add(panneauRespLegal);

        JScrollPane scroll = new JScrollPane(panneauContenu);
        scroll.setBorder(null);

        // ── Boutons ────────────────────────────────────────────────────────────
        btnModifier = new JButton("✏ Modifier");
        btnModifier.setBackground(new Color(70, 130, 180));
        btnModifier.setForeground(Color.WHITE);
        btnModifier.setFocusPainted(false);

        btnFermer = new JButton("✖ Fermer");
        btnFermer.setFocusPainted(false);
        btnFermer.addActionListener(e -> dispose());

        JPanel panneauBoutons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 4));
        panneauBoutons.add(btnModifier);
        panneauBoutons.add(btnFermer);

        panneauGlobal.add(scroll,          BorderLayout.CENTER);
        panneauGlobal.add(panneauBoutons,  BorderLayout.SOUTH);
        add(panneauGlobal);
    }

    // ── Utilitaires ────────────────────────────────────────────────────────────

    private JPanel creerSection(String titre) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), titre,
                TitledBorder.LEFT, TitledBorder.TOP));
        return p;
    }

    /**
     * Ajoute une ligne label/valeur dans un panneau GridBagLayout.
     * Retourne le JLabel de valeur pour usage ultérieur.
     */
    private JLabel ajouterLigne(JPanel panneau, String libelle, String valeur, int ligne) {
        GridBagConstraints gcLabel = new GridBagConstraints();
        gcLabel.gridx  = 0; gcLabel.gridy = ligne;
        gcLabel.insets = new Insets(3, 8, 3, 6);
        gcLabel.anchor = GridBagConstraints.EAST;

        GridBagConstraints gcValeur = new GridBagConstraints();
        gcValeur.gridx   = 1; gcValeur.gridy = ligne;
        gcValeur.insets  = new Insets(3, 2, 3, 8);
        gcValeur.fill    = GridBagConstraints.HORIZONTAL;
        gcValeur.weightx = 1.0;

        JLabel lblLibelle = new JLabel(libelle);
        lblLibelle.setFont(lblLibelle.getFont().deriveFont(Font.BOLD));

        String texte = (valeur == null || valeur.isBlank()) ? "—" : valeur;
        JLabel lblValeur = new JLabel(texte);

        panneau.add(lblLibelle, gcLabel);
        panneau.add(lblValeur,  gcValeur);
        return lblValeur;
    }

    private void configurerFenetre() {
        pack();
        setMinimumSize(new Dimension(480, 400));
        setSize(520, 580);
        setLocationRelativeTo(getParent());
        setResizable(true);
    }

    // ── API publique ───────────────────────────────────────────────────────────

    public void ajouterEcouteurModifier(ActionListener ecouteur) {
        btnModifier.addActionListener(ecouteur);
    }
}

