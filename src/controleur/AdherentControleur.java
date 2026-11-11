package controleur;

import manager.AdherentManager;
import manager.SauvegardeManager;
import modele.Adherent;
import modele.Categorie;
import repository.CategorieRepository;
import repository.ClubRepository;
import modele.Club;
import vue.FenetrePrincipale;
import vue.FicheAdherentVue;
import vue.FormulaireAdherentVue;
import vue.RechercheAdherentVue;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Contrôleur MVC pour la gestion des adhérents et de toute la fenêtre principale.
 *
 * <p>Fait le lien entre les vues (formulaire, recherche, fiche, annuaire, finances, sauvegarde)
 * et les modèles ({@link AdherentManager}, {@link SauvegardeManager},
 * {@link CategorieRepository}, {@link ClubRepository}).</p>
 */
public class AdherentControleur {

    // ── Dépendances (MVC) ──────────────────────────────────────────────────────
    private final AdherentManager   manager;
    private final FenetrePrincipale fenetrePrincipale;
    private final SauvegardeManager sauvegardeManager;
    private final CategorieRepository categorieRepository;
    private final ClubRepository    clubRepository;

    /** Format de date attendu dans les formulaires */
    private static final DateTimeFormatter FORMAT_DATE =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /** Chemin relatif vers le fichier des catégories */
    private static final String CHEMIN_CATEGORIES = "document/categorie.xml";

    /** Chemin relatif vers le fichier des clubs */
    private static final String CHEMIN_CLUBS = "document/club.xml";

    // ── Constructeur ───────────────────────────────────────────────────────────

    public AdherentControleur(FenetrePrincipale fenetrePrincipale,
                               AdherentManager manager) {
        this.fenetrePrincipale   = fenetrePrincipale;
        this.manager             = manager;
        this.sauvegardeManager   = new SauvegardeManager();
        this.categorieRepository = new CategorieRepository();
        this.clubRepository      = new ClubRepository();
        brancherActions();
    }

    // ── Branchement des actions ────────────────────────────────────────────────

    private void brancherActions() {
        // ── Menu Adhérents ─────────────────────────────────────────────────────
        fenetrePrincipale.getMenuItemNouvelAdherent()
                .addActionListener(e -> ouvrirFormulaireAjout());

        fenetrePrincipale.getMenuItemRechercherAdherent()
                .addActionListener(e -> ouvrirRechercheAdherent());

        fenetrePrincipale.getMenuItemQuitter()
                .addActionListener(e -> System.exit(0));

        // ── Menu Clubs ─────────────────────────────────────────────────────────
        fenetrePrincipale.getMenuItemAnnuaireClubs()
                .addActionListener(e -> ouvrirAnnuaireClubs());

        // ── Menu Finances ──────────────────────────────────────────────────────
        fenetrePrincipale.getMenuItemRecapitulatif()
                .addActionListener(e -> ouvrirRecapitulatifFinancier());

        // ── Menu Sauvegarde ────────────────────────────────────────────────────
        fenetrePrincipale.getMenuItemExporter()
                .addActionListener(e -> exporterDonnees());

        fenetrePrincipale.getMenuItemImporter()
                .addActionListener(e -> importerDonnees());

        // ── Boutons d'accès rapide ─────────────────────────────────────────────
        fenetrePrincipale.getBtnNouvelAdherent()
                .addActionListener(e -> ouvrirFormulaireAjout());

        fenetrePrincipale.getBtnRecherche()
                .addActionListener(e -> ouvrirRechercheAdherent());

        fenetrePrincipale.getBtnAnnuaire()
                .addActionListener(e -> ouvrirAnnuaireClubs());

        fenetrePrincipale.getBtnFinances()
                .addActionListener(e -> ouvrirRecapitulatifFinancier());
    }

    // ── Action : Nouvel adhérent ───────────────────────────────────────────────

    public void ouvrirFormulaireAjout() {
        FormulaireAdherentVue formulaire =
                new FormulaireAdherentVue(fenetrePrincipale, "Nouvel adhérent");
        formulaire.ajouterEcouteurValider(e -> validerAjout(formulaire));
        formulaire.setVisible(true);
    }

    private void validerAjout(FormulaireAdherentVue formulaire) {
        formulaire.effacerErreur();

        String nom     = formulaire.getChampNom();
        String prenom  = formulaire.getChampPrenom();
        String dateStr = formulaire.getChampDateNaissance();

        if (nom.isBlank()) {
            formulaire.afficherErreur("Le nom est obligatoire.");
            return;
        }
        if (prenom.isBlank()) {
            formulaire.afficherErreur("Le prénom est obligatoire.");
            return;
        }
        if (dateStr.isBlank()) {
            formulaire.afficherErreur("La date de naissance est obligatoire (JJ/MM/AAAA).");
            return;
        }

        LocalDate dateNaissance;
        try {
            dateNaissance = LocalDate.parse(dateStr, FORMAT_DATE);
        } catch (DateTimeParseException ex) {
            formulaire.afficherErreur("Date invalide. Format attendu : JJ/MM/AAAA");
            return;
        }

        Adherent nouvelAdherent = new Adherent(
                nom, prenom,
                formulaire.getChampNomNaissance(),
                dateNaissance,
                formulaire.getChampAdresse(),
                formulaire.getChampCodePostal(),
                formulaire.getChampVille(),
                formulaire.getChampTelephone(),
                formulaire.getChampEmail(),
                formulaire.getArme(),
                formulaire.getPratique(),
                formulaire.getLateralite(),
                formulaire.getChampNomRespLegal(),
                formulaire.getChampPrenomRespLegal()
        );

        if (nouvelAdherent.estMineur()) {
            formulaire.afficherRespLegal(true);
            if (formulaire.getChampNomRespLegal().isBlank()
                    || formulaire.getChampPrenomRespLegal().isBlank()) {
                formulaire.afficherErreur(
                        "L'adhérent est mineur : renseignez le responsable légal.");
                return;
            }
        }

        try {
            manager.ajouterAdherent(nouvelAdherent);
        } catch (IllegalArgumentException ex) {
            formulaire.afficherErreur(ex.getMessage());
            return;
        }

        JOptionPane.showMessageDialog(
                formulaire,
                "Adhérent « " + nom + " " + prenom + " » enregistré avec succès !",
                "Succès",
                JOptionPane.INFORMATION_MESSAGE);

        formulaire.dispose();
    }

    // ── Action : Rechercher un adhérent ───────────────────────────────────────

    public void ouvrirRechercheAdherent() {
        RechercheAdherentVue vue = new RechercheAdherentVue(fenetrePrincipale);

        // Afficher tous les adhérents au démarrage
        vue.afficherResultats(manager.getListeAdherents());

        // Bouton Rechercher
        vue.ajouterEcouteurRechercher(e -> {
            String critere = vue.getCritereRecherche();
            vue.afficherResultats(manager.rechercherAdherent(critere));
        });

        // Bouton Voir fiche
        vue.ajouterEcouteurVoirFiche(e -> {
            int id = vue.getIdAdherentSelectionne();
            if (id < 0) return;
            Adherent a = trouverParId(id);
            if (a != null) {
                List<Categorie> cats = chargerCategories();
                Categorie cat = manager.trouverCategorie(a.getAge(), cats);
                FicheAdherentVue fiche = new FicheAdherentVue(fenetrePrincipale, a, cat);
                // Bouton Modifier depuis la fiche
                fiche.ajouterEcouteurModifier(ev -> {
                    fiche.dispose();
                    ouvrirFormulaireModification(a, vue);
                });
                fiche.setVisible(true);
            }
        });

        // Bouton Modifier
        vue.ajouterEcouteurModifier(e -> {
            int id = vue.getIdAdherentSelectionne();
            if (id < 0) return;
            Adherent a = trouverParId(id);
            if (a != null) ouvrirFormulaireModification(a, vue);
        });

        // Bouton Supprimer
        vue.ajouterEcouteurSupprimer(e -> {
            int id = vue.getIdAdherentSelectionne();
            if (id < 0) return;
            Adherent a = trouverParId(id);
            if (a == null) return;
            int choix = JOptionPane.showConfirmDialog(
                    vue,
                    "Supprimer l'adhérent « " + a.getPrenom() + " " + a.getNom() + " » ?",
                    "Confirmer la suppression",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (choix == JOptionPane.YES_OPTION) {
                try {
                    manager.supprimerAdherent(id);
                    vue.afficherResultats(manager.rechercherAdherent(vue.getCritereRecherche()));
                } catch (IllegalArgumentException ex) {
                    JOptionPane.showMessageDialog(vue, ex.getMessage(), "Erreur",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        vue.setVisible(true);
    }

    // ── Action : Modifier un adhérent ─────────────────────────────────────────

    private void ouvrirFormulaireModification(Adherent adherent,
                                               RechercheAdherentVue vueRecherche) {
        FormulaireAdherentVue formulaire =
                new FormulaireAdherentVue(fenetrePrincipale,
                        "Modifier — " + adherent.getPrenom() + " " + adherent.getNom());

        // Pré-remplissage
        formulaire.setChampNom(adherent.getNom());
        formulaire.setChampPrenom(adherent.getPrenom());
        formulaire.setChampNomNaissance(adherent.getNomNaissance());
        if (adherent.getDateNaissance() != null)
            formulaire.setChampDateNaissance(adherent.getDateNaissance().format(FORMAT_DATE));
        formulaire.setChampAdresse(adherent.getAdresse());
        formulaire.setChampCodePostal(adherent.getCodePostal());
        formulaire.setChampVille(adherent.getVille());
        formulaire.setChampTelephone(adherent.getTelephone());
        formulaire.setChampEmail(adherent.getEmail());
        formulaire.setArme(adherent.getArme());
        formulaire.setPratique(adherent.getPratique());
        formulaire.setLateralite(adherent.getLateralite());
        formulaire.setChampNomRespLegal(adherent.getNomRespLegal());
        formulaire.setChampPrenomRespLegal(adherent.getPrenomRespLegal());
        if (adherent.estMineur()) formulaire.afficherRespLegal(true);

        formulaire.ajouterEcouteurValider(e -> validerModification(adherent.getId(), formulaire, vueRecherche));
        formulaire.setVisible(true);
    }

    private void validerModification(int idOriginal,
                                      FormulaireAdherentVue formulaire,
                                      RechercheAdherentVue vueRecherche) {
        formulaire.effacerErreur();

        String nom     = formulaire.getChampNom();
        String prenom  = formulaire.getChampPrenom();
        String dateStr = formulaire.getChampDateNaissance();

        if (nom.isBlank()) { formulaire.afficherErreur("Le nom est obligatoire."); return; }
        if (prenom.isBlank()) { formulaire.afficherErreur("Le prénom est obligatoire."); return; }
        if (dateStr.isBlank()) { formulaire.afficherErreur("La date de naissance est obligatoire."); return; }

        LocalDate dateNaissance;
        try {
            dateNaissance = LocalDate.parse(dateStr, FORMAT_DATE);
        } catch (DateTimeParseException ex) {
            formulaire.afficherErreur("Date invalide. Format attendu : JJ/MM/AAAA");
            return;
        }

        Adherent modifie = new Adherent(
                idOriginal, nom, prenom,
                formulaire.getChampNomNaissance(),
                dateNaissance,
                formulaire.getChampAdresse(),
                formulaire.getChampCodePostal(),
                formulaire.getChampVille(),
                formulaire.getChampTelephone(),
                formulaire.getChampEmail(),
                formulaire.getArme(),
                formulaire.getPratique(),
                formulaire.getLateralite(),
                formulaire.getChampNomRespLegal(),
                formulaire.getChampPrenomRespLegal()
        );

        if (modifie.estMineur()) {
            formulaire.afficherRespLegal(true);
            if (formulaire.getChampNomRespLegal().isBlank()
                    || formulaire.getChampPrenomRespLegal().isBlank()) {
                formulaire.afficherErreur("L'adhérent est mineur : renseignez le responsable légal.");
                return;
            }
        }

        try {
            manager.modifierAdherent(modifie);
        } catch (IllegalArgumentException ex) {
            formulaire.afficherErreur(ex.getMessage());
            return;
        }

        JOptionPane.showMessageDialog(formulaire,
                "Adhérent « " + nom + " " + prenom + " » modifié avec succès !",
                "Succès", JOptionPane.INFORMATION_MESSAGE);

        formulaire.dispose();

        // Rafraîchir la vue de recherche si elle est ouverte
        if (vueRecherche != null && vueRecherche.isVisible()) {
            vueRecherche.afficherResultats(
                    manager.rechercherAdherent(vueRecherche.getCritereRecherche()));
        }
    }

    // ── Action : Annuaire des clubs ────────────────────────────────────────────

    private void ouvrirAnnuaireClubs() {
        List<Club> clubs = clubRepository.chargerClubs(CHEMIN_CLUBS);

        if (clubs.isEmpty()) {
            JOptionPane.showMessageDialog(fenetrePrincipale,
                    "Aucun club n'a pu être chargé depuis le fichier XML.",
                    "Annuaire clubs", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Demande d'anonymisation
        int choixAnon = JOptionPane.showConfirmDialog(fenetrePrincipale,
                "Afficher les données anonymisées (RGPD) ?",
                "Annuaire des clubs", JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (choixAnon == JOptionPane.YES_OPTION) {
            clubRepository.anonymiserDonnees(clubs);
        }

        // Construction du tableau
        String[] colonnes = {"Nom", "Ville", "Adresse", "Contact", "Téléphone", "Email"};
        Object[][] donnees = new Object[clubs.size()][6];
        for (int i = 0; i < clubs.size(); i++) {
            Club c = clubs.get(i);
            donnees[i][0] = c.getNom();
            donnees[i][1] = c.getVille();
            donnees[i][2] = c.getAdresse();
            donnees[i][3] = c.getBenevoleNom();
            donnees[i][4] = c.getBenevoleTelephone();
            donnees[i][5] = c.getBenevoleEmail();
        }

        JTable table = new JTable(donnees, colonnes) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table.setAutoCreateRowSorter(true);
        table.setRowHeight(22);
        table.getTableHeader().setReorderingAllowed(false);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setPreferredSize(new java.awt.Dimension(900, 350));

        JDialog dialog = new JDialog(fenetrePrincipale, "Annuaire des clubs d'escrime", true);
        dialog.setLayout(new java.awt.BorderLayout(8, 8));
        dialog.add(new JLabel("  " + clubs.size() + " club(s) chargé(s)"), java.awt.BorderLayout.NORTH);
        dialog.add(scroll, java.awt.BorderLayout.CENTER);

        JButton btnFermer = new JButton("✖ Fermer");
        btnFermer.addActionListener(e -> dialog.dispose());
        JPanel sud = new JPanel();
        sud.add(btnFermer);
        dialog.add(sud, java.awt.BorderLayout.SOUTH);

        dialog.pack();
        dialog.setLocationRelativeTo(fenetrePrincipale);
        dialog.setVisible(true);
    }

    // ── Action : Récapitulatif financier ──────────────────────────────────────

    private void ouvrirRecapitulatifFinancier() {
        List<Categorie> cats = chargerCategories();

        // Si les cotisations sont toutes à 0, on propose de les saisir
        boolean toutesNulles = cats.stream().allMatch(c -> c.getCotisation() == 0.0);
        if (toutesNulles && !cats.isEmpty()) {
            int choix = JOptionPane.showConfirmDialog(fenetrePrincipale,
                    "Les cotisations ne sont pas encore configurées.\n" +
                    "Voulez-vous saisir les montants maintenant ?",
                    "Cotisations manquantes", JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (choix == JOptionPane.YES_OPTION) {
                saisirCotisations(cats);
            }
        }

        if (manager.getListeAdherents().isEmpty()) {
            JOptionPane.showMessageDialog(fenetrePrincipale,
                    "Aucun adhérent enregistré. Ajoutez des adhérents avant de générer le récapitulatif.",
                    "Récapitulatif financier", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String rapport = manager.genererRapportParCategorie(cats);
        double total   = manager.calculerMontantTotal(cats);

        JTextArea textArea = new JTextArea(rapport);
        textArea.setEditable(false);
        textArea.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 12));
        JScrollPane scroll = new JScrollPane(textArea);
        scroll.setPreferredSize(new java.awt.Dimension(600, 400));

        JDialog dialog = new JDialog(fenetrePrincipale, "Récapitulatif financier", true);
        dialog.setLayout(new java.awt.BorderLayout(8, 8));

        JLabel lblTotal = new JLabel(
                String.format("  Total général des cotisations : %.2f €", total),
                SwingConstants.LEFT);
        lblTotal.setFont(lblTotal.getFont().deriveFont(java.awt.Font.BOLD, 13f));
        lblTotal.setForeground(new java.awt.Color(0, 100, 0));

        dialog.add(lblTotal, java.awt.BorderLayout.NORTH);
        dialog.add(scroll, java.awt.BorderLayout.CENTER);

        JButton btnFermer = new JButton("✖ Fermer");
        btnFermer.addActionListener(e -> dialog.dispose());
        JPanel sud = new JPanel();
        sud.add(btnFermer);
        dialog.add(sud, java.awt.BorderLayout.SOUTH);

        dialog.pack();
        dialog.setLocationRelativeTo(fenetrePrincipale);
        dialog.setVisible(true);
    }

    /** Boîte de dialogue pour saisir les cotisations par catégorie. */
    private void saisirCotisations(List<Categorie> cats) {
        for (Categorie cat : cats) {
            String saisie = JOptionPane.showInputDialog(fenetrePrincipale,
                    String.format("Cotisation pour « %s » (en €) :", cat.getLibelle()),
                    "0.00");
            if (saisie == null) break; // annulation
            try {
                double montant = Double.parseDouble(saisie.replace(",", "."));
                cat.setCotisation(montant);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(fenetrePrincipale,
                        "Montant invalide, 0 sera utilisé pour cette catégorie.",
                        "Erreur", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    // ── Action : Exporter les données ─────────────────────────────────────────

    private void exporterDonnees() {
        if (manager.getListeAdherents().isEmpty()) {
            JOptionPane.showMessageDialog(fenetrePrincipale,
                    "Aucun adhérent à exporter.",
                    "Exportation", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Exporter les données des adhérents");
        chooser.setFileFilter(new FileNameExtensionFilter("Fichiers de sauvegarde (*.gea)", "gea"));
        chooser.setSelectedFile(new File("adherents.gea"));

        if (chooser.showSaveDialog(fenetrePrincipale) == JFileChooser.APPROVE_OPTION) {
            File fichier = chooser.getSelectedFile();
            if (!fichier.getName().endsWith(".gea"))
                fichier = new File(fichier.getAbsolutePath() + ".gea");

            sauvegardeManager.exporterDonnees(fichier.getAbsolutePath(),
                    manager.getListeAdherents());

            JOptionPane.showMessageDialog(fenetrePrincipale,
                    "Données exportées avec succès vers :\n" + fichier.getAbsolutePath(),
                    "Exportation réussie", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // ── Action : Importer les données ─────────────────────────────────────────

    private void importerDonnees() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Importer les données des adhérents");
        chooser.setFileFilter(new FileNameExtensionFilter("Fichiers de sauvegarde (*.gea)", "gea"));

        if (chooser.showOpenDialog(fenetrePrincipale) == JFileChooser.APPROVE_OPTION) {
            File fichier = chooser.getSelectedFile();

            if (!manager.getListeAdherents().isEmpty()) {
                int choix = JOptionPane.showConfirmDialog(fenetrePrincipale,
                        "Cette importation remplacera les " +
                        manager.getListeAdherents().size() +
                        " adhérent(s) actuellement en mémoire.\nContinuer ?",
                        "Confirmer l'importation",
                        JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (choix != JOptionPane.YES_OPTION) return;
            }

            List<Adherent> importes = sauvegardeManager.importerDonnees(fichier.getAbsolutePath());
            if (!importes.isEmpty()) {
                manager.setListeAdherents(importes);
                JOptionPane.showMessageDialog(fenetrePrincipale,
                        importes.size() + " adhérent(s) importé(s) avec succès.",
                        "Importation réussie", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    // ── Utilitaires privés ─────────────────────────────────────────────────────

    /** Charge les catégories depuis le fichier XML. Retourne liste vide si erreur. */
    private List<Categorie> chargerCategories() {
        try {
            return categorieRepository.chargerCategories(CHEMIN_CATEGORIES);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(fenetrePrincipale,
                    "Impossible de charger les catégories :\n" + e.getMessage(),
                    "Erreur XML", JOptionPane.WARNING_MESSAGE);
            return new java.util.ArrayList<>();
        }
    }

    /** Recherche un adhérent par son identifiant dans la liste du manager. */
    private Adherent trouverParId(int id) {
        return manager.getListeAdherents().stream()
                .filter(a -> a.getId() == id)
                .findFirst()
                .orElse(null);
    }

    // ── Getter ─────────────────────────────────────────────────────────────────

    public AdherentManager getManager() {
        return manager;
    }
}

