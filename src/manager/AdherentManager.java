package manager;

import modele.Adherent;
import modele.Categorie;

import java.util.List;
import java.util.ArrayList;

/**
 * Gestionnaire principal des adhérents du Cercle d'Escrime de Vandœuvre.
 * Contient toutes les opérations CRUD sur la liste des adhérents.
 */
public class AdherentManager {

    // ── Attributs ──────────────────────────────────────────────────────────────
    private List<Adherent> listeAdherents;

    // ── Constructeur ───────────────────────────────────────────────────────────

    public AdherentManager() {
        this.listeAdherents = new ArrayList<>();
    }

    // ── Méthodes CRUD ──────────────────────────────────────────────────────────

    /**
     * Ajoute un nouvel adhérent dans la liste.
     *
     * @param adherent l'adhérent à ajouter
     */
    public void ajouterAdherent(Adherent adherent) {
        // TODO : implémenter dans US1
    }

    /**
     * Modifie les informations d'un adhérent existant.
     *
     * @param adherentModifie l'adhérent avec les nouvelles valeurs (l'id doit correspondre)
     */
    public void modifierAdherent(Adherent adherentModifie) {
        // TODO : implémenter dans US3/US4
    }

    /**
     * Supprime un adhérent de la liste par son identifiant.
     *
     * @param id l'identifiant de l'adhérent à supprimer
     */
    public void supprimerAdherent(int id) {
        // TODO : implémenter dans US5
    }

    /**
     * Recherche des adhérents selon un critère (nom, prénom ou email).
     *
     * @param critere la chaîne de recherche
     * @return la liste des adhérents correspondants
     */
    public List<Adherent> rechercherAdherent(String critere) {
        // TODO : implémenter dans US2
        return new ArrayList<>();
    }

    /**
     * Calcule le montant total des cotisations de tous les adhérents.
     *
     * @param categories la liste des catégories avec leurs cotisations
     * @return le montant total en euros
     */
    public double calculerMontantTotal(List<Categorie> categories) {
        // TODO : implémenter dans US9
        return 0.0;
    }

    /**
     * Génère un rapport textuel des adhérents regroupés par catégorie.
     *
     * @param categories la liste des catégories
     * @return le rapport sous forme de chaîne de caractères
     */
    public String genererRapportParCategorie(List<Categorie> categories) {
        // TODO : implémenter dans US9
        return "";
    }

    // ── Getter ─────────────────────────────────────────────────────────────────

    public List<Adherent> getListeAdherents() {
        return listeAdherents;
    }
}

