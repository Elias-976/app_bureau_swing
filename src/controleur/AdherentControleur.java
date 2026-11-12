package controleur;

import manager.AdherentManager;
import manager.AuthentificationException;
import manager.AuthentificationManager;
import manager.SauvegardeManager;
import manager.ValidateurAdherent;
import modele.Adherent;
import modele.Categorie;
import modele.Club;
import modele.Droit;
import modele.Utilisateur;
import repository.CategorieRepository;
import repository.ClubRepository;
import vue.AnnuaireClubsVue;
import vue.ChangementMotDePasseVue;
import vue.ConnexionVue;
import vue.FenetrePrincipale;
import vue.FicheAdherentVue;
import vue.FormulaireAdherentVue;
import vue.RechercheAdherentVue;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.Period;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Contrôleur MVC pour la gestion des adhérents, de l'authentification et de toute la fenêtre principale.
 *
 * <p>Fait le lien entre les vues (connexion, formulaire, recherche, fiche, annuaire, finances, sauvegarde)
 * et les modèles ({@link AuthentificationManager}, {@link AdherentManager}, {@link SauvegardeManager},
 * {@link CategorieRepository}, {@link ClubRepository}).</p>
 */
public class AdherentControleur {

    // ── Dépendances (MVC) ──────────────────────────────────────────────────────
    private final AdherentManager        manager;
    private final FenetrePrincipale      fenetrePrincipale;
    private final AuthentificationManager authManager;
    private final SauvegardeManager      sauvegardeManager;
    private final CategorieRepository    categorieRepository;
    private final ClubRepository         clubRepository;

    /** Chemin relatif vers le fichier des catégories */
    private static final String CHEMIN_CATEGORIES = "document/categorie.xml";

    /** Chemin relatif vers le fichier des clubs */
    private static final String CHEMIN_CLUBS = "document/club.xml";

    // ── Constructeurs ──────────────────────────────────────────────────────────

    public AdherentControleur(FenetrePrincipale fenetrePrincipale,
                               AdherentManager manager) {
        this(fenetrePrincipale, manager, new AuthentificationManager());
    }

    public AdherentControleur(FenetrePrincipale fenetrePrincipale,
                               AdherentManager manager,
                               AuthentificationManager authManager) {
        this.fenetrePrincipale   = fenetrePrincipale;
        this.manager             = manager;
        this.authManager         = authManager;
        this.sauvegardeManager   = new SauvegardeManager();
        this.categorieRepository = new CategorieRepository();
        this.clubRepository      = new ClubRepository();
        brancherActions();
        initialiserSession();
    }

    // ── Initialisation de la session & Authentification ────────────────────────

    /**
     * Lance le dialogue de connexion si nécessaire puis configure les accès de la fenêtre.
     */
    public void initialiserSession() {
        if (GraphicsEnvironment.isHeadless()) {
            return;
        }

        if (!authManager.estConnecte()) {
            boolean connecte = ouvrirDialogueConnexion();
            if (!connecte) {
                System.exit(0);
                return;
            }
        }

        fenetrePrincipale.appliquerDroits(authManager.getUtilisateurConnecte());
        fenetrePrincipale.afficher();
    }

    /**
     * Ouvre la fenêtre modale de connexion.
     *
     * @return {@code true} si la connexion a été établie avec succès
     */
    public boolean ouvrirDialogueConnexion() {
        ConnexionVue vueConnexion = new ConnexionVue(fenetrePrincipale);
        vueConnexion.ajouterEcouteurConnexion(e -> {
            String identifiant = vueConnexion.getIdentifiant();
            char[] motDePasse  = vueConnexion.getMotDePasse();
            try {
                authManager.connecter(identifiant, motDePasse);
                vueConnexion.setConnexionValidee(true);
                vueConnexion.dispose();
            } catch (AuthentificationException ex) {
                vueConnexion.afficherErreur(ex.getMessage());
                vueConnexion.effacerMotDePasse();
            }
        });

        vueConnexion.ajouterEcouteurQuitter(e -> {
            vueConnexion.setConnexionValidee(false);
            vueConnexion.dispose();
        });

        vueConnexion.setVisible(true);
        return vueConnexion.isConnexionValidee();
    }

    /**
     * Déconnecte l'utilisateur en cours et réaffiche l'écran de connexion.
     */
    public void deconnecter() {
        int choix = JOptionPane.showConfirmDialog(
                fenetrePrincipale,
                "Voulez-vous vraiment vous déconnecter de l'application ?",
                "Déconnexion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (choix != JOptionPane.YES_OPTION) {
            return;
        }

        authManager.deconnecter();
        fenetrePrincipale.setVisible(false);
        fenetrePrincipale.appliquerDroits(null);

        boolean reconnecte = ouvrirDialogueConnexion();
        if (reconnecte) {
            fenetrePrincipale.appliquerDroits(authManager.getUtilisateurConnecte());
            fenetrePrincipale.afficher();
        } else {
            System.exit(0);
        }
    }

    /**
     * Ouvre la boîte de dialogue de changement de mot de passe pour la session active.
     */
    public void ouvrirChangementMotDePasse() {
        if (!authManager.estConnecte()) return;
        Utilisateur user = authManager.getUtilisateurConnecte();

        ChangementMotDePasseVue vue = new ChangementMotDePasseVue(
                fenetrePrincipale, user.getIdentifiant());

        vue.ajouterEcouteurValider(e -> {
            char[] ancienMdp       = vue.getAncienMotDePasse();
            char[] nouveauMdp      = vue.getNouveauMotDePasse();
            char[] confirmationMdp = vue.getConfirmation();
            try {
                authManager.changerMotDePasse(user.getIdentifiant(), ancienMdp, nouveauMdp, confirmationMdp);
                JOptionPane.showMessageDialog(vue,
                        "Votre mot de passe a été modifié avec succès !",
                        "Mot de passe mis à jour",
                        JOptionPane.INFORMATION_MESSAGE);
                vue.dispose();
            } catch (AuthentificationException ex) {
                vue.afficherErreur(ex.getMessage());
            }
        });

        vue.setVisible(true);
    }

    /**
     * Vérifie si la session active possède le droit demandé.
     * Affiche un message d'avertissement explicite en cas de refus.
     *
     * @param droit le privilège requis
     * @return {@code true} si l'action est autorisée
     */
    public boolean verifierDroit(Droit droit) {
        if (authManager == null) return true;
        if (!authManager.estConnecte()) {
            JOptionPane.showMessageDialog(fenetrePrincipale,
                    "Veuillez vous connecter pour accéder à cette fonctionnalité.",
                    "Authentification requise", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (!authManager.aLeDroit(droit)) {
            String roleStr = authManager.getUtilisateurConnecte().getRole().getLibelleCourt();
            JOptionPane.showMessageDialog(fenetrePrincipale,
                    String.format("Accès refusé :\nVotre profil « %s » n'est pas autorisé à effectuer cette opération (%s).",
                            roleStr, droit.getDescription()),
                    "Accès restreint (RBAC)", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    // ── Branchement des actions ────────────────────────────────────────────────

    private void brancherActions() {
        // ── Menu Session ───────────────────────────────────────────────────────
        fenetrePrincipale.getMenuItemDeconnexion()
                .addActionListener(e -> deconnecter());
        fenetrePrincipale.getMenuItemChangerMdp()
                .addActionListener(e -> ouvrirChangementMotDePasse());

        // ── Boutons du bandeau de session ──────────────────────────────────────
        fenetrePrincipale.getBtnDeconnexion()
                .addActionListener(e -> deconnecter());
        fenetrePrincipale.getBtnChangerMotDePasse()
                .addActionListener(e -> ouvrirChangementMotDePasse());

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
        if (!verifierDroit(Droit.GESTION_ADHERENTS)) {
            return;
        }

        FormulaireAdherentVue formulaire =
                new FormulaireAdherentVue(fenetrePrincipale, "Nouvel adhérent");
        brancherMiseAJourRespLegal(formulaire);
        formulaire.ajouterEcouteurValider(
                e -> enregistrerAdherentDepuisFormulaire(formulaire, null, null));
        formulaire.setVisible(true);
    }

    // ── Action : Rechercher un adhérent ───────────────────────────────────────

    public void ouvrirRechercheAdherent() {
        if (!verifierDroit(Droit.CONSULTATION_ADHERENTS)) {
            return;
        }

        RechercheAdherentVue vue = new RechercheAdherentVue(fenetrePrincipale);
        boolean peutModifier = authManager.aLeDroit(Droit.GESTION_ADHERENTS);
        vue.setDroitModification(peutModifier);

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
                Categorie cat = manager.trouverCategorie(a, cats);
                FicheAdherentVue fiche = new FicheAdherentVue(fenetrePrincipale, a, cat);
                fiche.setDroitModification(peutModifier);

                // Bouton Modifier depuis la fiche
                fiche.ajouterEcouteurModifier(ev -> {
                    if (!verifierDroit(Droit.GESTION_ADHERENTS)) return;
                    fiche.dispose();
                    ouvrirFormulaireModification(a, vue);
                });
                fiche.setVisible(true);
            }
        });

        // Bouton Modifier
        vue.ajouterEcouteurModifier(e -> {
            if (!verifierDroit(Droit.GESTION_ADHERENTS)) return;
            int id = vue.getIdAdherentSelectionne();
            if (id < 0) return;
            Adherent a = trouverParId(id);
            if (a != null) ouvrirFormulaireModification(a, vue);
        });

        // Bouton Supprimer
        vue.ajouterEcouteurSupprimer(e -> {
            if (!verifierDroit(Droit.GESTION_ADHERENTS)) return;
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
        if (!verifierDroit(Droit.GESTION_ADHERENTS)) {
            return;
        }

        FormulaireAdherentVue formulaire =
                new FormulaireAdherentVue(fenetrePrincipale,
                        "Modifier un adhérent — " + adherent.getPrenom() + " " + adherent.getNom());

        formulaire.setChampNom(adherent.getNom());
        formulaire.setChampPrenom(adherent.getPrenom());
        formulaire.setChampNomNaissance(adherent.getNomNaissance());
        if (adherent.getDateNaissance() != null) {
            formulaire.setChampDateNaissance(
                    adherent.getDateNaissance().format(
                            java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        }
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
        formulaire.afficherRespLegal(
                adherent.getDateNaissance() != null && adherent.estMineur());

        brancherMiseAJourRespLegal(formulaire);
        formulaire.ajouterEcouteurValider(
                e -> enregistrerAdherentDepuisFormulaire(formulaire, adherent.getId(), vueRecherche));
        formulaire.setVisible(true);
    }

    /**
     * Factorise la validation et l'enregistrement (création ou modification)
     * d'un adhérent depuis le formulaire Swing.
     */
    private void enregistrerAdherentDepuisFormulaire(FormulaireAdherentVue formulaire,
                                                    Integer idOriginal,
                                                    RechercheAdherentVue vueRecherche) {
        if (!verifierDroit(Droit.GESTION_ADHERENTS)) {
            return;
        }

        formulaire.effacerErreur();

        LocalDate dateNaissance;
        try {
            dateNaissance = ValidateurAdherent.parserDateNaissance(
                    formulaire.getChampDateNaissance());
        } catch (IllegalArgumentException ex) {
            formulaire.afficherErreur(ex.getMessage());
            return;
        }

        boolean mineur = Period.between(dateNaissance, LocalDate.now()).getYears() < 18;
        formulaire.afficherRespLegal(mineur);

        String nomRespLegal    = mineur ? formulaire.getChampNomRespLegal()    : "";
        String prenomRespLegal = mineur ? formulaire.getChampPrenomRespLegal() : "";

        int id = (idOriginal != null) ? idOriginal : 0;
        Adherent adherent = new Adherent(
                id,
                formulaire.getChampNom(),
                formulaire.getChampPrenom(),
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
                nomRespLegal,
                prenomRespLegal
        );

        try {
            if (idOriginal == null) {
                manager.ajouterAdherent(adherent);
            } else {
                manager.modifierAdherent(adherent);
            }
        } catch (IllegalArgumentException ex) {
            formulaire.afficherErreur(ex.getMessage());
            return;
        }

        String action = (idOriginal == null) ? "enregistré" : "modifié";
        JOptionPane.showMessageDialog(
                formulaire,
                "Adhérent « " + adherent.getNom() + " " + adherent.getPrenom()
                        + " » " + action + " avec succès !",
                "Succès",
                JOptionPane.INFORMATION_MESSAGE);

        formulaire.dispose();

        if (idOriginal != null && vueRecherche != null && vueRecherche.isVisible()) {
            vueRecherche.afficherResultats(
                    manager.rechercherAdherent(vueRecherche.getCritereRecherche()));
        }
    }

    /** Met à jour dynamiquement la visibilité du responsable légal selon la date saisie. */
    private void brancherMiseAJourRespLegal(FormulaireAdherentVue formulaire) {
        formulaire.ajouterEcouteurDateNaissance(() -> mettreAJourVisibiliteRespLegal(formulaire));
    }

    private void mettreAJourVisibiliteRespLegal(FormulaireAdherentVue formulaire) {
        String dateStr = formulaire.getChampDateNaissance();
        if (dateStr.isBlank()) {
            formulaire.afficherRespLegal(false);
            return;
        }
        try {
            LocalDate dateNaissance = ValidateurAdherent.parserDateNaissance(dateStr);
            boolean mineur = Period.between(dateNaissance, LocalDate.now()).getYears() < 18;
            formulaire.afficherRespLegal(mineur);
        } catch (IllegalArgumentException ignored) {
            // Saisie intermédiaire incomplète ou invalide : ne pas clignoter
        }
    }

    // ── Action : Annuaire des clubs ────────────────────────────────────────────

    private void ouvrirAnnuaireClubs() {
        if (!verifierDroit(Droit.CONSULTATION_CLUBS)) {
            return;
        }

        List<Club> clubs = clubRepository.chargerClubs(CHEMIN_CLUBS);

        if (clubs.isEmpty()) {
            JOptionPane.showMessageDialog(fenetrePrincipale,
                    "Aucun club n'a pu être chargé depuis le fichier XML.",
                    "Annuaire clubs", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Anonymisation partielle par défaut des données personnelles des bénévoles (RGPD)
        clubRepository.anonymiserDonnees(clubs);

        AnnuaireClubsVue vue = new AnnuaireClubsVue(fenetrePrincipale, clubs);
        boolean peutImprimer = authManager.aLeDroit(Droit.IMPRESSION_CLUBS);
        vue.setDroitImpression(peutImprimer);

        vue.ajouterEcouteurImprimer(e -> {
            if (!verifierDroit(Droit.IMPRESSION_CLUBS)) return;
            vue.imprimerTableau();
        });

        vue.ajouterEcouteurExporterHtml(e -> {
            if (!verifierDroit(Droit.IMPRESSION_CLUBS)) return;
            exporterAnnuaireHtml(vue, clubs);
        });

        vue.setVisible(true);
    }

    /** Exporte l'annuaire anonymisé au format HTML imprimable. */
    private void exporterAnnuaireHtml(AnnuaireClubsVue parent, List<Club> clubs) {
        if (!verifierDroit(Droit.IMPRESSION_CLUBS)) {
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Exporter l'annuaire des clubs en HTML");
        chooser.setFileFilter(new FileNameExtensionFilter("Pages HTML (*.html)", "html"));
        chooser.setSelectedFile(new File("annuaire_clubs.html"));

        if (chooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
            File fichier = chooser.getSelectedFile();
            if (!fichier.getName().toLowerCase(Locale.ROOT).endsWith(".html")) {
                fichier = new File(fichier.getAbsolutePath() + ".html");
            }
            try {
                String html = AnnuaireClubsVue.genererHtmlImprimable(clubs);
                Files.writeString(fichier.toPath(), html, StandardCharsets.UTF_8);
                JOptionPane.showMessageDialog(parent,
                        "Annuaire exporté en HTML vers :\n" + fichier.getAbsolutePath(),
                        "Export HTML réussi", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(parent,
                        "Erreur lors de l'exportation HTML :\n" + ex.getMessage(),
                        "Erreur d'exportation", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ── Action : Récapitulatif financier ──────────────────────────────────────

    private void ouvrirRecapitulatifFinancier() {
        if (!verifierDroit(Droit.GESTION_FINANCES)) {
            return;
        }

        List<Categorie> cats = chargerCategories();
        if (cats.isEmpty()) {
            return;
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
                String.format(Locale.FRANCE, "  Total général des cotisations : %.2f €", total),
                SwingConstants.LEFT);
        lblTotal.setFont(lblTotal.getFont().deriveFont(java.awt.Font.BOLD, 13f));
        lblTotal.setForeground(new java.awt.Color(0, 100, 0));

        dialog.add(lblTotal, java.awt.BorderLayout.NORTH);
        dialog.add(scroll, java.awt.BorderLayout.CENTER);

        JButton btnAjuster = new JButton("⚙ Ajuster les tarifs");
        btnAjuster.addActionListener(e -> {
            saisirCotisations(cats);
            textArea.setText(manager.genererRapportParCategorie(cats));
            textArea.setCaretPosition(0);
            lblTotal.setText(String.format(Locale.FRANCE,
                    "  Total général des cotisations : %.2f €",
                    manager.calculerMontantTotal(cats)));
        });

        JButton btnFermer = new JButton("✖ Fermer");
        btnFermer.addActionListener(e -> dialog.dispose());
        JPanel sud = new JPanel();
        sud.add(btnAjuster);
        sud.add(btnFermer);
        dialog.add(sud, java.awt.BorderLayout.SOUTH);

        dialog.pack();
        dialog.setLocationRelativeTo(fenetrePrincipale);
        dialog.setVisible(true);
    }

    /** Boîte de dialogue pour ajuster les cotisations par catégorie en mémoire. */
    private void saisirCotisations(List<Categorie> cats) {
        for (Categorie cat : cats) {
            String valeurInitiale = String.format(Locale.FRANCE, "%.2f", cat.getCotisation());
            String saisie = JOptionPane.showInputDialog(fenetrePrincipale,
                    String.format("Cotisation pour « %s » (en €) :", cat.getLibelle()),
                    valeurInitiale);
            if (saisie == null) break; // annulation
            try {
                double montant = Double.parseDouble(saisie.trim().replace(",", "."));
                if (montant < 0) {
                    JOptionPane.showMessageDialog(fenetrePrincipale,
                            "Un montant négatif n'est pas autorisé, le tarif précédent est conservé.",
                            "Erreur", JOptionPane.WARNING_MESSAGE);
                } else {
                    cat.setCotisation(montant);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(fenetrePrincipale,
                        "Montant invalide, le tarif précédent est conservé.",
                        "Erreur", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    // ── Action : Exporter les données ─────────────────────────────────────────

    private void exporterDonnees() {
        if (!verifierDroit(Droit.SAUVEGARDE_RESTAURATION)) {
            return;
        }

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
            if (!fichier.getName().toLowerCase(Locale.ROOT).endsWith(".gea")) {
                fichier = new File(fichier.getAbsolutePath() + ".gea");
            }

            char[] motDePasse = demanderMotDePasse(true, "Chiffrement de la sauvegarde");
            if (motDePasse == null) {
                return;
            }

            boolean succes;
            try {
                succes = sauvegardeManager.exporterDonnees(
                        fichier.getAbsolutePath(),
                        manager.getListeAdherents(),
                        motDePasse);
            } finally {
                Arrays.fill(motDePasse, '\0');
            }

            if (succes) {
                JOptionPane.showMessageDialog(fenetrePrincipale,
                        "Données exportées et chiffrées avec succès vers :\n"
                                + fichier.getAbsolutePath(),
                        "Exportation réussie", JOptionPane.INFORMATION_MESSAGE);
            } else {
                String details = sauvegardeManager.getDernierMessageErreur();
                JOptionPane.showMessageDialog(fenetrePrincipale,
                        "Erreur lors de l'exportation des données :\n"
                                + (details != null ? details : "Erreur inconnue."),
                        "Erreur de sauvegarde", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ── Action : Importer les données ─────────────────────────────────────────

    private void importerDonnees() {
        if (!verifierDroit(Droit.SAUVEGARDE_RESTAURATION)) {
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Importer les données des adhérents");
        chooser.setFileFilter(new FileNameExtensionFilter("Fichiers de sauvegarde (*.gea)", "gea"));

        if (chooser.showOpenDialog(fenetrePrincipale) == JFileChooser.APPROVE_OPTION) {
            File fichier = chooser.getSelectedFile();

            if (!manager.getListeAdherents().isEmpty()) {
                int choix = JOptionPane.showConfirmDialog(fenetrePrincipale,
                        "Cette importation remplacera les "
                                + manager.getListeAdherents().size()
                                + " adhérent(s) actuellement en mémoire.\nContinuer ?",
                        "Confirmer l'importation",
                        JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (choix != JOptionPane.YES_OPTION) return;
            }

            char[] motDePasse = demanderMotDePasse(false, "Déchiffrement de la sauvegarde");
            if (motDePasse == null) {
                return;
            }

            List<Adherent> importes;
            try {
                importes = sauvegardeManager.importerDonnees(
                        fichier.getAbsolutePath(), motDePasse);
            } finally {
                Arrays.fill(motDePasse, '\0');
            }

            if (importes == null) {
                String details = sauvegardeManager.getDernierMessageErreur();
                JOptionPane.showMessageDialog(fenetrePrincipale,
                        "Erreur lors de l'importation des données :\n"
                                + (details != null ? details : "Erreur inconnue."),
                        "Erreur d'importation", JOptionPane.ERROR_MESSAGE);
                return;
            }

            manager.setListeAdherents(importes);
            JOptionPane.showMessageDialog(fenetrePrincipale,
                    importes.size() + " adhérent(s) importé(s) avec succès.",
                    "Importation réussie", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Affiche une boîte de dialogue demandant un mot de passe pour chiffrer
     * ou déchiffrer la sauvegarde.
     *
     * @param avecConfirmation {@code true} pour exiger une saisie en double (export)
     * @param titre            titre de la fenêtre de dialogue
     * @return le mot de passe saisi, ou {@code null} si l'utilisateur a annulé
     */
    private char[] demanderMotDePasse(boolean avecConfirmation, String titre) {
        while (true) {
            JPasswordField champMdp = new JPasswordField(20);
            champMdp.addAncestorListener(new AncestorListener() {
                @Override
                public void ancestorAdded(AncestorEvent event) {
                    SwingUtilities.invokeLater(champMdp::requestFocusInWindow);
                }
                @Override public void ancestorRemoved(AncestorEvent event) {}
                @Override public void ancestorMoved(AncestorEvent event) {}
            });

            JPanel panneau = new JPanel(new GridLayout(0, 1, 4, 4));
            panneau.add(new JLabel(avecConfirmation
                    ? "Mot de passe de chiffrement :"
                    : "Mot de passe de la sauvegarde :"));
            panneau.add(champMdp);

            JPasswordField champConfirm = null;
            if (avecConfirmation) {
                champConfirm = new JPasswordField(20);
                panneau.add(new JLabel("Confirmer le mot de passe :"));
                panneau.add(champConfirm);
            }

            int resultat = JOptionPane.showConfirmDialog(
                    fenetrePrincipale,
                    panneau,
                    titre,
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE);

            if (resultat != JOptionPane.OK_OPTION) {
                champMdp.setText("");
                if (champConfirm != null) {
                    champConfirm.setText("");
                }
                return null;
            }

            char[] mdp1 = champMdp.getPassword();
            char[] mdp2 = (champConfirm != null) ? champConfirm.getPassword() : null;
            champMdp.setText("");
            if (champConfirm != null) {
                champConfirm.setText("");
            }

            if (mdp1.length == 0) {
                Arrays.fill(mdp1, '\0');
                if (mdp2 != null) Arrays.fill(mdp2, '\0');
                JOptionPane.showMessageDialog(fenetrePrincipale,
                        "Le mot de passe ne peut pas être vide.",
                        titre, JOptionPane.WARNING_MESSAGE);
                continue;
            }

            if (avecConfirmation && !Arrays.equals(mdp1, mdp2)) {
                Arrays.fill(mdp1, '\0');
                Arrays.fill(mdp2, '\0');
                JOptionPane.showMessageDialog(fenetrePrincipale,
                        "Les deux mots de passe ne correspondent pas.",
                        titre, JOptionPane.WARNING_MESSAGE);
                continue;
            }

            if (mdp2 != null) {
                Arrays.fill(mdp2, '\0');
            }
            return mdp1;
        }
    }

    // ── Utilitaires privés ─────────────────────────────────────────────────────

    private List<Categorie> chargerCategories() {
        try {
            return categorieRepository.chargerCategories(CHEMIN_CATEGORIES);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(fenetrePrincipale,
                    "Impossible de charger les catégories (" + CHEMIN_CATEGORIES + ") :\n"
                            + ex.getMessage(),
                    "Erreur XML", JOptionPane.ERROR_MESSAGE);
            return List.of();
        }
    }

    private Adherent trouverParId(int id) {
        return manager.getListeAdherents().stream()
                .filter(a -> a.getId() == id)
                .findFirst()
                .orElse(null);
    }

    public AuthentificationManager getAuthentificationManager() {
        return authManager;
    }
}
