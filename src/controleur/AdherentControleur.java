package controleur;

import manager.AdherentManager;
import modele.Adherent;
import vue.FormulaireAdherentVue;
import vue.FenetrePrincipale;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Contrôleur MVC pour la gestion des adhérents.
 *
 * <p>Fait le lien entre {@link FormulaireAdherentVue} (vue)
 * et {@link AdherentManager} (modèle) : il récupère les saisies,
 * les valide côté format, construit l'objet {@link Adherent}
 * et délègue au manager.</p>
 */
public class AdherentControleur {

    // ── Dépendances (MVC) ──────────────────────────────────────────────────────
    private final AdherentManager     manager;
    private final FenetrePrincipale   fenetrePrincipale;

    /** Format de date attendu dans le formulaire */
    private static final DateTimeFormatter FORMAT_DATE =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ── Constructeur ───────────────────────────────────────────────────────────

    public AdherentControleur(FenetrePrincipale fenetrePrincipale,
                               AdherentManager manager) {
        this.fenetrePrincipale = fenetrePrincipale;
        this.manager           = manager;
        brancherActions();
    }

    // ── Branchement des actions sur la FenetrePrincipale ──────────────────────

    /**
     * Branche les actions des menus et boutons de la fenêtre principale
     * qui concernent les adhérents.
     */
    private void brancherActions() {
        // Menu "Nouvel adhérent"
        fenetrePrincipale.getMenuAdherents()
                .getItem(0)
                .addActionListener(e -> ouvrirFormulaireAjout());
    }

    // ── Action : ouvrir le formulaire d'ajout ──────────────────────────────────

    /**
     * Crée et affiche le formulaire de saisie pour un nouvel adhérent,
     * puis branche le bouton "Valider" sur {@link #validerAjout(FormulaireAdherentVue)}.
     */
    public void ouvrirFormulaireAjout() {
        FormulaireAdherentVue formulaire =
                new FormulaireAdherentVue(fenetrePrincipale, "Nouvel adhérent");

        formulaire.ajouterEcouteurValider(e -> validerAjout(formulaire));
        formulaire.setVisible(true);
    }

    // ── Action : valider le formulaire d'ajout ─────────────────────────────────

    /**
     * Lit les champs du formulaire, valide les données de format,
     * construit un {@link Adherent} et l'ajoute via le manager.
     *
     * <p>En cas d'erreur de saisie, affiche le message dans la vue
     * sans fermer le formulaire.</p>
     *
     * @param formulaire la vue dont on lit les champs
     */
    private void validerAjout(FormulaireAdherentVue formulaire) {
        formulaire.effacerErreur();

        // ── 1. Champs obligatoires ─────────────────────────────────────────────
        String nom    = formulaire.getChampNom();
        String prenom = formulaire.getChampPrenom();
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

        // ── 2. Validation format date ─────────────────────────────────────────
        LocalDate dateNaissance;
        try {
            dateNaissance = LocalDate.parse(dateStr, FORMAT_DATE);
        } catch (DateTimeParseException ex) {
            formulaire.afficherErreur("Date invalide. Format attendu : JJ/MM/AAAA");
            return;
        }

        // ── 3. Construction de l'adhérent ─────────────────────────────────────
        Adherent nouvelAdherent = new Adherent(
                nom,
                prenom,
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

        // ── 4. Afficher section responsable légal si mineur ───────────────────
        if (nouvelAdherent.estMineur()) {
            formulaire.afficherRespLegal(true);
            // Si les champs resp. légal sont encore vides, on demande de les remplir
            if (formulaire.getChampNomRespLegal().isBlank()
                    || formulaire.getChampPrenomRespLegal().isBlank()) {
                formulaire.afficherErreur(
                        "L'adhérent est mineur : renseignez le responsable légal.");
                return;
            }
        }

        // ── 5. Délégation au manager ──────────────────────────────────────────
        try {
            manager.ajouterAdherent(nouvelAdherent);
        } catch (IllegalArgumentException ex) {
            formulaire.afficherErreur(ex.getMessage());
            return;
        }

        // ── 6. Succès ─────────────────────────────────────────────────────────
        javax.swing.JOptionPane.showMessageDialog(
                formulaire,
                "Adhérent « " + nom + " " + prenom + " » enregistré avec succès !",
                "Succès",
                javax.swing.JOptionPane.INFORMATION_MESSAGE);

        formulaire.dispose();
    }

    // ── Getter (utile pour les autres contrôleurs) ─────────────────────────────

    public AdherentManager getManager() {
        return manager;
    }
}
