package vue;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Boîte de dialogue permettant à l'utilisateur connecté de modifier son mot de passe.
 */
public class ChangementMotDePasseVue extends JDialog {

    private final JPasswordField txtAncienMdp;
    private final JPasswordField txtNouveauMdp;
    private final JPasswordField txtConfirmationMdp;
    private final JLabel         lblErreur;
    private final JButton        btnValider;
    private final JButton        btnAnnuler;

    public ChangementMotDePasseVue(Frame parent, String identifiant) {
        super(parent, "Changer de mot de passe", true);

        JPanel panneauRacine = new JPanel(new BorderLayout(0, 14));
        panneauRacine.setBorder(new EmptyBorder(18, 22, 18, 22));
        panneauRacine.setBackground(Color.WHITE);

        // En-tête
        JPanel panneauEnTete = new JPanel(new BorderLayout(0, 4));
        panneauEnTete.setBackground(Color.WHITE);

        JLabel lblTitre = new JLabel("🔑 Modification de votre mot de passe");
        lblTitre.setFont(lblTitre.getFont().deriveFont(Font.BOLD, 15f));
        lblTitre.setForeground(new Color(15, 23, 42));

        JLabel lblInfo = new JLabel("Compte actif : " + identifiant + " (8 caractères minimum)");
        lblInfo.setFont(lblInfo.getFont().deriveFont(Font.PLAIN, 12f));
        lblInfo.setForeground(new Color(100, 116, 139));

        panneauEnTete.add(lblTitre, BorderLayout.NORTH);
        panneauEnTete.add(lblInfo, BorderLayout.SOUTH);
        panneauRacine.add(panneauEnTete, BorderLayout.NORTH);

        // Formulaire
        JPanel panneauForm = new JPanel(new GridBagLayout());
        panneauForm.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill   = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        panneauForm.add(new JLabel("Mot de passe actuel :"), gbc);
        gbc.gridx = 1;
        txtAncienMdp = new JPasswordField(18);
        panneauForm.add(txtAncienMdp, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panneauForm.add(new JLabel("Nouveau mot de passe :"), gbc);
        gbc.gridx = 1;
        txtNouveauMdp = new JPasswordField(18);
        panneauForm.add(txtNouveauMdp, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panneauForm.add(new JLabel("Confirmer le nouveau :"), gbc);
        gbc.gridx = 1;
        txtConfirmationMdp = new JPasswordField(18);
        panneauForm.add(txtConfirmationMdp, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        lblErreur = new JLabel(" ");
        lblErreur.setForeground(new Color(185, 28, 28));
        lblErreur.setFont(lblErreur.getFont().deriveFont(Font.BOLD, 11.5f));
        panneauForm.add(lblErreur, gbc);

        panneauRacine.add(panneauForm, BorderLayout.CENTER);

        // Boutons
        JPanel panneauBoutons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panneauBoutons.setBackground(Color.WHITE);

        btnAnnuler = new JButton("Annuler");
        btnAnnuler.addActionListener(e -> dispose());

        btnValider = new JButton("Enregistrer");
        btnValider.setBackground(new Color(37, 99, 235));
        btnValider.setForeground(Color.WHITE);
        btnValider.setFont(btnValider.getFont().deriveFont(Font.BOLD, 12f));

        panneauBoutons.add(btnAnnuler);
        panneauBoutons.add(btnValider);
        panneauRacine.add(panneauBoutons, BorderLayout.SOUTH);

        add(panneauRacine);

        KeyAdapter ecouteurEntree = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    btnValider.doClick();
                }
            }
        };
        txtAncienMdp.addKeyListener(ecouteurEntree);
        txtNouveauMdp.addKeyListener(ecouteurEntree);
        txtConfirmationMdp.addKeyListener(ecouteurEntree);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        pack();
        setLocationRelativeTo(parent);
    }

    public char[] getAncienMotDePasse() {
        return txtAncienMdp.getPassword();
    }

    public char[] getNouveauMotDePasse() {
        return txtNouveauMdp.getPassword();
    }

    public char[] getConfirmation() {
        return txtConfirmationMdp.getPassword();
    }

    public void afficherErreur(String message) {
        lblErreur.setText(message != null ? message : " ");
        pack();
    }

    public void ajouterEcouteurValider(ActionListener l) {
        btnValider.addActionListener(l);
    }
}

