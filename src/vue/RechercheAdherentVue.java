package vue;

import modele.Adherent;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Panneau de recherche et d'affichage des adhérents (architecture MVC).
 *
 * <p>Contient un champ de saisie, un bouton de recherche et un tableau
 * de résultats. Affichable dans un {@link JDialog} ou directement dans
 * la fenêtre principale.</p>
 */
public class RechercheAdherentVue extends JDialog {

    // ── Composants ─────────────────────────────────────────────────────────────
    private JTextField          champRecherche;
    private JButton             btnRechercher;
    private JButton             btnEffacer;
    private JTable              tableauResultats;
    private DefaultTableModel   modeleTableau;
    private JLabel              labelNbResultats;

    // ── Boutons d'action sur la sélection ──────────────────────────────────────
    private JButton btnVoirFiche;
    private JButton btnModifier;
    private JButton btnSupprimer;

    // ── En-têtes des colonnes du tableau ──────────────────────────────────────
    private static final String[] COLONNES = {
            "ID", "Nom", "Prénom", "Date de naissance", "Ville", "Téléphone", "Email", "Arme"
    };

    // ── Constructeur ───────────────────────────────────────────────────────────

    public RechercheAdherentVue(Frame parent) {
        super(parent, "Rechercher un adhérent", false); // non modale
        initialiserComposants();
        configurerFenetre();
    }

    // ── Initialisation ─────────────────────────────────────────────────────────

    private void initialiserComposants() {
        JPanel panneauGlobal = new JPanel(new BorderLayout(10, 10));
        panneauGlobal.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // ── Barre de recherche (haut) ──────────────────────────────────────────
        JPanel panneauRecherche = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panneauRecherche.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Recherche",
                TitledBorder.LEFT, TitledBorder.TOP));

        panneauRecherche.add(new JLabel("Critère (nom, prénom, email) :"));

        champRecherche = new JTextField(25);
        champRecherche.setToolTipText("Tapez un nom, un prénom ou un email puis appuyez sur Entrée");
        panneauRecherche.add(champRecherche);

        btnRechercher = new JButton("🔍 Rechercher");
        btnRechercher.setBackground(new Color(70, 130, 180));
        btnRechercher.setForeground(Color.WHITE);
        btnRechercher.setFocusPainted(false);
        panneauRecherche.add(btnRechercher);

        btnEffacer = new JButton("✖ Effacer");
        btnEffacer.setFocusPainted(false);
        btnEffacer.addActionListener(e -> {
            champRecherche.setText("");
            champRecherche.requestFocus();
            btnRechercher.doClick();
        });
        panneauRecherche.add(btnEffacer);

        labelNbResultats = new JLabel("  ");
        labelNbResultats.setForeground(Color.DARK_GRAY);
        panneauRecherche.add(labelNbResultats);

        // ── Tableau de résultats (centre) ──────────────────────────────────────
        modeleTableau = new DefaultTableModel(COLONNES, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false; // tableau en lecture seule
            }
        };

        tableauResultats = new JTable(modeleTableau);
        tableauResultats.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableauResultats.setRowHeight(22);
        tableauResultats.setAutoCreateRowSorter(true); // tri par colonne au clic
        tableauResultats.getTableHeader().setReorderingAllowed(false);

        // Masquer la colonne ID (index 0) visuellement tout en la conservant dans le modèle
        tableauResultats.getColumnModel().getColumn(0).setMinWidth(0);
        tableauResultats.getColumnModel().getColumn(0).setMaxWidth(0);
        tableauResultats.getColumnModel().getColumn(0).setWidth(0);

        // Largeurs relatives des colonnes visibles
        tableauResultats.getColumnModel().getColumn(1).setPreferredWidth(120); // Nom
        tableauResultats.getColumnModel().getColumn(2).setPreferredWidth(120); // Prénom
        tableauResultats.getColumnModel().getColumn(3).setPreferredWidth(100); // Date naissance
        tableauResultats.getColumnModel().getColumn(4).setPreferredWidth(100); // Ville
        tableauResultats.getColumnModel().getColumn(5).setPreferredWidth(110); // Téléphone
        tableauResultats.getColumnModel().getColumn(6).setPreferredWidth(160); // Email
        tableauResultats.getColumnModel().getColumn(7).setPreferredWidth(70);  // Arme

        JScrollPane scrollTableau = new JScrollPane(tableauResultats);
        scrollTableau.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Résultats",
                TitledBorder.LEFT, TitledBorder.TOP));

        // ── Boutons d'action sur la sélection (bas) ────────────────────────────
        btnVoirFiche  = new JButton("👁 Voir fiche");
        btnModifier   = new JButton("✏ Modifier");
        btnSupprimer  = new JButton("🗑 Supprimer");

        btnVoirFiche.setEnabled(false);
        btnModifier.setEnabled(false);
        btnSupprimer.setEnabled(false);
        btnSupprimer.setForeground(new Color(178, 34, 34));

        // Activer les boutons dès qu'une ligne est sélectionnée
        tableauResultats.getSelectionModel().addListSelectionListener(e -> {
            boolean selection = tableauResultats.getSelectedRow() >= 0;
            btnVoirFiche.setEnabled(selection);
            btnModifier.setEnabled(selection);
            btnSupprimer.setEnabled(selection);
        });

        // Double-clic → voir la fiche
        tableauResultats.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tableauResultats.getSelectedRow() >= 0) {
                    btnVoirFiche.doClick();
                }
            }
        });

        JPanel panneauActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        panneauActions.add(btnVoirFiche);
        panneauActions.add(btnModifier);
        panneauActions.add(btnSupprimer);

        // ── Assemblage ─────────────────────────────────────────────────────────
        panneauGlobal.add(panneauRecherche,  BorderLayout.NORTH);
        panneauGlobal.add(scrollTableau,     BorderLayout.CENTER);
        panneauGlobal.add(panneauActions,    BorderLayout.SOUTH);

        add(panneauGlobal);
    }

    private void configurerFenetre() {
        setSize(900, 540);
        setMinimumSize(new Dimension(700, 400));
        setLocationRelativeTo(getParent());
        setResizable(true);
    }

    // ── API publique pour le contrôleur ────────────────────────────────────────

    /**
     * Met à jour le tableau avec la liste d'adhérents fournie.
     *
     * @param adherents la liste à afficher
     */
    public void afficherResultats(List<Adherent> adherents) {
        modeleTableau.setRowCount(0); // vider le tableau
        for (Adherent a : adherents) {
            modeleTableau.addRow(new Object[]{
                    a.getId(),
                    a.getNom(),
                    a.getPrenom(),
                    a.getDateNaissance() != null
                            ? a.getDateNaissance().format(
                                    java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                            : "",
                    a.getVille(),
                    a.getTelephone(),
                    a.getEmail(),
                    a.getArme()
            });
        }
        int nb = adherents.size();
        labelNbResultats.setText(nb + " résultat" + (nb > 1 ? "s" : ""));
    }

    /**
     * Retourne l'identifiant de l'adhérent sélectionné dans le tableau,
     * ou {@code -1} si aucune ligne n'est sélectionnée.
     */
    public int getIdAdherentSelectionne() {
        int ligneVue = tableauResultats.getSelectedRow();
        if (ligneVue < 0) return -1;
        // Convertir indice vue → indice modèle (en cas de tri actif)
        int ligneModele = tableauResultats.convertRowIndexToModel(ligneVue);
        return (int) modeleTableau.getValueAt(ligneModele, 0);
    }

    // ── Enregistrement des écouteurs ───────────────────────────────────────────

    /** Branche l'action "Rechercher" (bouton ET touche Entrée dans le champ). */
    public void ajouterEcouteurRechercher(ActionListener ecouteur) {
        btnRechercher.addActionListener(ecouteur);
        champRecherche.addActionListener(ecouteur); // Entrée dans le champ
    }

    public void ajouterEcouteurVoirFiche(ActionListener ecouteur) {
        btnVoirFiche.addActionListener(ecouteur);
    }

    public void ajouterEcouteurModifier(ActionListener ecouteur) {
        btnModifier.addActionListener(ecouteur);
    }

    public void ajouterEcouteurSupprimer(ActionListener ecouteur) {
        btnSupprimer.addActionListener(ecouteur);
    }

    // ── Getter du champ de recherche ───────────────────────────────────────────

    public String getCritereRecherche() {
        return champRecherche.getText().trim();
    }
}

