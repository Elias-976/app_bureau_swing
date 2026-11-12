package vue;

import modele.Club;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.print.PrinterException;
import java.text.MessageFormat;
import java.util.List;

/**
 * Boîte de dialogue affichant l'annuaire des clubs d'escrime (architecture MVC).
 *
 * <p>Permet la consultation des coordonnées publiques des clubs (avec données
 * personnelles des bénévoles partiellement anonymisées conformément au RGPD),
 * l'impression du tableau et l'exportation au format HTML imprimable.</p>
 */
public class AnnuaireClubsVue extends JDialog {

    private static final String[] COLONNES = {
            "Nom", "Ville", "Adresse", "Site web", "Contact (RGPD)", "Téléphone", "Email"
    };

    private JTable      tableClubs;
    private JButton     btnImprimer;
    private JButton     btnExporterHtml;
    private JButton     btnFermer;

    /**
     * Crée et affiche la fenêtre modale de l'annuaire des clubs.
     *
     * @param parent fenêtre parente
     * @param clubs  liste des clubs (préalablement anonymisée par le dépôt)
     */
    public AnnuaireClubsVue(Frame parent, List<Club> clubs) {
        super(parent, "Annuaire des clubs d'escrime", true);
        initialiserComposants(clubs);
        configurerFenetre();
    }

    private void initialiserComposants(List<Club> clubs) {
        JPanel panneauGlobal = new JPanel(new BorderLayout(10, 10));
        panneauGlobal.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // ── En-tête d'information RGPD ─────────────────────────────────────────
        JLabel labelInfo = new JLabel(
                clubs.size() + " club(s) chargé(s) — Coordonnées personnelles des bénévoles "
                        + "partiellement anonymisées (RGPD)");
        labelInfo.setFont(labelInfo.getFont().deriveFont(Font.BOLD));
        labelInfo.setForeground(new Color(0, 70, 140));
        panneauGlobal.add(labelInfo, BorderLayout.NORTH);

        // ── Tableau non éditable et triable ────────────────────────────────────
        DefaultTableModel modele = new DefaultTableModel(COLONNES, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        for (Club c : clubs) {
            String siteAffiche = (c.getSite() == null || c.getSite().isBlank()) ? "—" : c.getSite();
            modele.addRow(new Object[]{
                    c.getNom(),
                    c.getVille(),
                    c.getAdresse(),
                    siteAffiche,
                    c.getBenevoleNom(),
                    c.getBenevoleTelephone(),
                    c.getBenevoleEmail()
            });
        }

        tableClubs = new JTable(modele);
        tableClubs.setAutoCreateRowSorter(true);
        tableClubs.setRowHeight(24);
        tableClubs.getTableHeader().setReorderingAllowed(false);
        tableClubs.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        tableClubs.getColumnModel().getColumn(0).setPreferredWidth(165); // Nom
        tableClubs.getColumnModel().getColumn(1).setPreferredWidth(100); // Ville
        tableClubs.getColumnModel().getColumn(2).setPreferredWidth(210); // Adresse
        tableClubs.getColumnModel().getColumn(3).setPreferredWidth(205); // Site web
        tableClubs.getColumnModel().getColumn(4).setPreferredWidth(120); // Contact
        tableClubs.getColumnModel().getColumn(5).setPreferredWidth(105); // Téléphone
        tableClubs.getColumnModel().getColumn(6).setPreferredWidth(165); // Email

        JScrollPane scroll = new JScrollPane(tableClubs);
        scroll.setPreferredSize(new Dimension(1040, 380));
        panneauGlobal.add(scroll, BorderLayout.CENTER);

        // ── Barre de boutons ───────────────────────────────────────────────────
        btnImprimer = new JButton("🖨 Imprimer");
        btnImprimer.setBackground(new Color(70, 130, 180));
        btnImprimer.setForeground(Color.WHITE);
        btnImprimer.setFocusPainted(false);

        btnExporterHtml = new JButton("📄 Exporter HTML");
        btnExporterHtml.setFocusPainted(false);

        btnFermer = new JButton("✖ Fermer");
        btnFermer.setFocusPainted(false);
        btnFermer.addActionListener(e -> dispose());

        JPanel panneauBoutons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        panneauBoutons.add(btnExporterHtml);
        panneauBoutons.add(btnImprimer);
        panneauBoutons.add(btnFermer);
        panneauGlobal.add(panneauBoutons, BorderLayout.SOUTH);

        add(panneauGlobal);
    }

    private void configurerFenetre() {
        pack();
        setMinimumSize(new Dimension(800, 360));
        setLocationRelativeTo(getParent());
        setResizable(true);
    }

    // ── Actions et écouteurs ───────────────────────────────────────────────────

    /**
     * Lance l'impression du tableau des clubs en mode ajusté à la largeur de page.
     * Gère proprement l'annulation par l'utilisateur et les erreurs {@link PrinterException}.
     */
    public void imprimerTableau() {
        MessageFormat entete = new MessageFormat(
                "Annuaire des clubs d'escrime — Ligue de Lorraine (M2L)");
        MessageFormat piedPage = new MessageFormat(
                "Page {0,number,integer} — Données bénévoles anonymisées (RGPD)");
        try {
            boolean imprime = tableClubs.print(JTable.PrintMode.FIT_WIDTH, entete, piedPage);
            if (!imprime) {
                return;
            }
            JOptionPane.showMessageDialog(
                    this,
                    "L'impression de l'annuaire a été transmise au système.",
                    "Impression",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (PrinterException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Impossible d'imprimer l'annuaire :\n" + ex.getMessage()
                            + "\nVérifiez qu'une imprimante est disponible ou utilisez « Exporter HTML ».",
                    "Erreur d'impression",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public void ajouterEcouteurImprimer(ActionListener ecouteur) {
        btnImprimer.addActionListener(ecouteur);
    }

    public void ajouterEcouteurExporterHtml(ActionListener ecouteur) {
        btnExporterHtml.addActionListener(ecouteur);
    }

    /**
     * Active ou désactive les boutons d'impression et d'export HTML selon les droits RBAC.
     *
     * @param autorise {@code true} si l'utilisateur possède le droit d'impression
     */
    public void setDroitImpression(boolean autorise) {
        btnImprimer.setEnabled(autorise);
        btnExporterHtml.setEnabled(autorise);
        if (!autorise) {
            btnImprimer.setToolTipText("Impression réservée au président du club");
            btnExporterHtml.setToolTipText("Export HTML réservé au président du club");
        }
    }

    /**
     * Génère une page HTML autonome et imprimable de l'annuaire des clubs.
     *
     * @param clubs la liste des clubs à mettre en forme
     * @return le document HTML complet en UTF-8
     */
    public static String genererHtmlImprimable(List<Club> clubs) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>\n<html lang=\"fr\">\n<head>\n")
          .append("  <meta charset=\"UTF-8\">\n")
          .append("  <title>Annuaire des clubs d'escrime — M2L</title>\n")
          .append("  <style>\n")
          .append("    body { font-family: Arial, sans-serif; margin: 24px; color: #222; }\n")
          .append("    h1 { color: #00468c; margin-bottom: 4px; }\n")
          .append("    p.rgpd { color: #555; font-size: 0.9em; margin-top: 0; }\n")
          .append("    table { border-collapse: collapse; width: 100%; font-size: 0.9em; }\n")
          .append("    th, td { border: 1px solid #ccc; padding: 6px 8px; text-align: left; }\n")
          .append("    th { background-color: #f0f4f8; }\n")
          .append("    @media print { body { margin: 8mm; } }\n")
          .append("  </style>\n</head>\n<body>\n")
          .append("  <h1>Annuaire des clubs d'escrime — Ligue de Lorraine (M2L)</h1>\n")
          .append("  <p class=\"rgpd\">").append(clubs.size())
          .append(" club(s) — Coordonnées personnelles des bénévoles partiellement anonymisées (RGPD).</p>\n")
          .append("  <table>\n    <thead>\n      <tr>")
          .append("<th>Nom</th><th>Ville</th><th>Adresse</th><th>Site web</th>")
          .append("<th>Contact (RGPD)</th><th>Téléphone</th><th>Email</th>")
          .append("</tr>\n    </thead>\n    <tbody>\n");

        for (Club c : clubs) {
            String site = (c.getSite() == null || c.getSite().isBlank()) ? "—" : c.getSite();
            sb.append("      <tr>")
              .append("<td>").append(echapperHtml(c.getNom())).append("</td>")
              .append("<td>").append(echapperHtml(c.getVille())).append("</td>")
              .append("<td>").append(echapperHtml(c.getAdresse())).append("</td>")
              .append("<td>").append(echapperHtml(site)).append("</td>")
              .append("<td>").append(echapperHtml(c.getBenevoleNom())).append("</td>")
              .append("<td>").append(echapperHtml(c.getBenevoleTelephone())).append("</td>")
              .append("<td>").append(echapperHtml(c.getBenevoleEmail())).append("</td>")
              .append("</tr>\n");
        }

        sb.append("    </tbody>\n  </table>\n</body>\n</html>\n");
        return sb.toString();
    }

    private static String echapperHtml(String texte) {
        if (texte == null) return "";
        return texte.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
