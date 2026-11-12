package vue;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Arrays;

/**
 * Boîte de dialogue de connexion sécurisée (architecture MVC en Java Swing pur).
 *
 * <p>Intègre :
 * <ul>
 *   <li>Saisie d'identifiant et de mot de passe masqué ({@link JPasswordField})</li>
 *   <li>Menu déroulant des 4 profils pré-remplis pour faciliter les recettes et démonstrations</li>
 *   <li>Affichage dynamique des erreurs (identifiants invalides, compte verrouillé, etc.)</li>
 *   <li>Validation directe par la touche Entrée</li>
 * </ul>
 * </p>
 */
public class ConnexionVue extends JDialog {

    private JTextField     txtIdentifiant;
    private JPasswordField txtMotDePasse;
    private JComboBox<String> comboProfilsRapides;
    private JLabel         lblErreur;
    private JButton        btnConnexion;
    private JButton        btnQuitter;

    private boolean        connexionValidee = false;

    /**
     * Crée la fenêtre modale de connexion.
     *
     * @param parent fenêtre parente (peut être {@code null})
     */
    public ConnexionVue(Frame parent) {
        super(parent, "Connexion — Application GEA", true);
        initialiserComposants();
        configurerFenetre();
    }

    private void initialiserComposants() {
        JPanel panneauRacine = new JPanel(new BorderLayout(0, 14));
        panneauRacine.setBorder(new EmptyBorder(20, 26, 20, 26));
        panneauRacine.setBackground(Color.WHITE);

        // ── En-tête ────────────────────────────────────────────────────────────
        JPanel panneauEnTete = new JPanel(new BorderLayout(0, 4));
        panneauEnTete.setBackground(Color.WHITE);

        JLabel lblTitre = new JLabel("🔐 Connexion à l'application GEA");
        lblTitre.setFont(lblTitre.getFont().deriveFont(Font.BOLD, 17f));
        lblTitre.setForeground(new Color(15, 23, 42));

        JLabel lblSousTitre = new JLabel("Cercle d'Escrime de Vandœuvre — Maison des Ligues de Lorraine");
        lblSousTitre.setFont(lblSousTitre.getFont().deriveFont(Font.PLAIN, 12f));
        lblSousTitre.setForeground(new Color(100, 116, 139));

        panneauEnTete.add(lblTitre, BorderLayout.NORTH);
        panneauEnTete.add(lblSousTitre, BorderLayout.SOUTH);
        panneauRacine.add(panneauEnTete, BorderLayout.NORTH);

        // ── Formulaire central ─────────────────────────────────────────────────
        JPanel panneauFormulaire = new JPanel(new GridBagLayout());
        panneauFormulaire.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill   = GridBagConstraints.HORIZONTAL;

        // Sélecteur rapide pour la démonstration
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0;
        JLabel lblProfil = new JLabel("Profil rapide :");
        lblProfil.setFont(lblProfil.getFont().deriveFont(Font.BOLD, 12f));
        panneauFormulaire.add(lblProfil, gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        comboProfilsRapides = new JComboBox<>(new String[]{
                "-- Choisir un profil de démonstration --",
                "Secrétaire — Sophie Bernard (secretaire)",
                "Trésorier — Marc Lefebvre (tresorier)",
                "Président — Claire Thomas (president)",
                "Administrateur — Informaticien M2L (admin)"
        });
        comboProfilsRapides.setFont(comboProfilsRapides.getFont().deriveFont(Font.PLAIN, 12f));
        panneauFormulaire.add(comboProfilsRapides, gbc);

        // Identifiant
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0;
        JLabel lblId = new JLabel("Identifiant :");
        lblId.setFont(lblId.getFont().deriveFont(Font.BOLD, 12f));
        panneauFormulaire.add(lblId, gbc);

        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0;
        txtIdentifiant = new JTextField(18);
        txtIdentifiant.setFont(txtIdentifiant.getFont().deriveFont(Font.PLAIN, 13f));
        panneauFormulaire.add(txtIdentifiant, gbc);

        // Mot de passe
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.0;
        JLabel lblMdp = new JLabel("Mot de passe :");
        lblMdp.setFont(lblMdp.getFont().deriveFont(Font.BOLD, 12f));
        panneauFormulaire.add(lblMdp, gbc);

        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 1.0;
        txtMotDePasse = new JPasswordField(18);
        txtMotDePasse.setFont(txtMotDePasse.getFont().deriveFont(Font.PLAIN, 13f));
        panneauFormulaire.add(txtMotDePasse, gbc);

        // Message d'erreur
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        lblErreur = new JLabel(" ");
        lblErreur.setForeground(new Color(185, 28, 28));
        lblErreur.setFont(lblErreur.getFont().deriveFont(Font.BOLD, 11.5f));
        panneauFormulaire.add(lblErreur, gbc);

        panneauRacine.add(panneauFormulaire, BorderLayout.CENTER);

        // ── Barre de boutons (Bas) ─────────────────────────────────────────────
        JPanel panneauBoutons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panneauBoutons.setBackground(Color.WHITE);

        btnQuitter = new JButton("✖ Quitter");
        btnQuitter.setFocusPainted(false);

        btnConnexion = new JButton("🔓 Se connecter");
        btnConnexion.setBackground(new Color(37, 99, 235));
        btnConnexion.setForeground(Color.WHITE);
        btnConnexion.setFont(btnConnexion.getFont().deriveFont(Font.BOLD, 12.5f));
        btnConnexion.setFocusPainted(false);

        panneauBoutons.add(btnQuitter);
        panneauBoutons.add(btnConnexion);
        panneauRacine.add(panneauBoutons, BorderLayout.SOUTH);

        add(panneauRacine);

        // ── Événements internes ────────────────────────────────────────────────
        comboProfilsRapides.addActionListener(e -> appliquerProfilSelectionne());

        KeyAdapter ecouteurEntree = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    btnConnexion.doClick();
                }
            }
        };
        txtIdentifiant.addKeyListener(ecouteurEntree);
        txtMotDePasse.addKeyListener(ecouteurEntree);

        // Par défaut, sélectionner Secrétaire pour accélérer les tests
        comboProfilsRapides.setSelectedIndex(1);
    }

    private void configurerFenetre() {
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(false);
        pack();
        setLocationRelativeTo(getParent());
    }

    private void appliquerProfilSelectionne() {
        int idx = comboProfilsRapides.getSelectedIndex();
        switch (idx) {
            case 1 -> { // Secrétaire
                txtIdentifiant.setText("secretaire");
                txtMotDePasse.setText("Secretaire2026!");
            }
            case 2 -> { // Trésorier
                txtIdentifiant.setText("tresorier");
                txtMotDePasse.setText("Tresorier2026!");
            }
            case 3 -> { // Président
                txtIdentifiant.setText("president");
                txtMotDePasse.setText("President2026!");
            }
            case 4 -> { // Administrateur
                txtIdentifiant.setText("admin");
                txtMotDePasse.setText("AdminM2L2026!");
            }
            default -> {
                txtIdentifiant.setText("");
                txtMotDePasse.setText("");
            }
        }
        lblErreur.setText(" ");
    }

    // ── API publique pour le contrôleur ────────────────────────────────────────

    public String getIdentifiant() {
        return txtIdentifiant.getText().trim();
    }

    public char[] getMotDePasse() {
        return txtMotDePasse.getPassword();
    }

    public void effacerMotDePasse() {
        txtMotDePasse.setText("");
    }

    public void afficherErreur(String message) {
        lblErreur.setText(message != null ? message : " ");
        pack();
    }

    public void setConnexionValidee(boolean validee) {
        this.connexionValidee = validee;
    }

    public boolean isConnexionValidee() {
        return connexionValidee;
    }

    public void ajouterEcouteurConnexion(ActionListener ecouteur) {
        btnConnexion.addActionListener(ecouteur);
    }

    public void ajouterEcouteurQuitter(ActionListener ecouteur) {
        btnQuitter.addActionListener(ecouteur);
    }
}

