package manager;

import modele.Adherent;
import modele.Categorie;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Gestionnaire principal des adhérents du Cercle d'Escrime de Vandœuvre.
 * Contient toutes les opérations CRUD sur la liste des adhérents.
 */
public class AdherentManager {

    // ── Attributs ──────────────────────────────────────────────────────────────
    private List<Adherent>        listeAdherents;
    /** Compteur auto-incrémenté pour générer les identifiants uniques. */
    private final AtomicInteger   compteurId;

    // ── Constructeur ───────────────────────────────────────────────────────────

    public AdherentManager() {
        this.listeAdherents = new ArrayList<>();
        this.compteurId     = new AtomicInteger(1);
    }

    // ── Méthodes CRUD ──────────────────────────────────────────────────────────

    /**
     * Ajoute un nouvel adhérent dans la liste.
     * Un identifiant unique lui est automatiquement attribué.
     *
     * @param adherent l'adhérent à ajouter (son id sera écrasé par le compteur)
     * @throws IllegalArgumentException si l'adhérent est null, ou si nom/prénom sont vides
     */
    public void ajouterAdherent(Adherent adherent) {
        if (adherent == null) {
            throw new IllegalArgumentException("L'adhérent ne peut pas être null.");
        }
        if (adherent.getNom() == null || adherent.getNom().isBlank()) {
            throw new IllegalArgumentException("Le nom de l'adhérent est obligatoire.");
        }
        if (adherent.getPrenom() == null || adherent.getPrenom().isBlank()) {
            throw new IllegalArgumentException("Le prénom de l'adhérent est obligatoire.");
        }
        if (adherent.getDateNaissance() == null) {
            throw new IllegalArgumentException("La date de naissance est obligatoire.");
        }

        // Attribution d'un identifiant unique
        adherent.setId(compteurId.getAndIncrement());
        listeAdherents.add(adherent);
    }

    /**
     * Modifie les informations d'un adhérent existant identifié par son id.
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
     * @param critere la chaîne de recherche (insensible à la casse)
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

    // ── Getters ────────────────────────────────────────────────────────────────

    public List<Adherent> getListeAdherents() {
        return listeAdherents;
    }

    /**
     * Remplace la liste complète des adhérents (utilisé lors d'une restauration).
     *
     * @param liste la nouvelle liste d'adhérents
     */
    public void setListeAdherents(List<Adherent> liste) {
        this.listeAdherents = liste;
        // Recalibrer le compteur après le dernier id chargé
        int maxId = liste.stream().mapToInt(Adherent::getId).max().orElse(0);
        compteurId.set(maxId + 1);
    }
}

