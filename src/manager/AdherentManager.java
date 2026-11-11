package manager;

import modele.Adherent;
import modele.Categorie;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Gestionnaire principal des adhérents du Cercle d'Escrime de Vandœuvre.
 * Contient toutes les opérations CRUD sur la liste des adhérents.
 */
public class AdherentManager {

    // ── Attributs ──────────────────────────────────────────────────────────────
    private List<Adherent>      listeAdherents;
    /** Compteur auto-incrémenté pour générer les identifiants uniques. */
    private final AtomicInteger compteurId;

    // ── Constructeur ───────────────────────────────────────────────────────────

    public AdherentManager() {
        this.listeAdherents = new ArrayList<>();
        this.compteurId     = new AtomicInteger(1);
    }

    // ── Méthodes CRUD ──────────────────────────────────────────────────────────

    /**
     * Ajoute un nouvel adhérent dans la liste après validation complète de ses champs.
     * Un identifiant unique lui est automatiquement attribué.
     *
     * @param adherent l'adhérent à ajouter
     * @throws IllegalArgumentException si l'adhérent est null ou invalide
     */
    public void ajouterAdherent(Adherent adherent) {
        ValidateurAdherent.valider(adherent);

        adherent.setId(compteurId.getAndIncrement());
        listeAdherents.add(adherent);
    }

    /**
     * Modifie les informations d'un adhérent existant identifié par son id,
     * après validation complète des nouvelles valeurs.
     * Remplace intégralement l'entrée correspondante dans la liste.
     *
     * @param adherentModifie l'adhérent avec les nouvelles valeurs (id doit correspondre)
     * @throws IllegalArgumentException si l'adhérent est null, invalide ou introuvable
     */
    public void modifierAdherent(Adherent adherentModifie) {
        ValidateurAdherent.valider(adherentModifie);

        for (int i = 0; i < listeAdherents.size(); i++) {
            if (listeAdherents.get(i).getId() == adherentModifie.getId()) {
                listeAdherents.set(i, adherentModifie);
                return;
            }
        }
        throw new IllegalArgumentException(
                "Aucun adhérent trouvé avec l'identifiant " + adherentModifie.getId());
    }

    /**
     * Supprime un adhérent de la liste par son identifiant.
     *
     * @param id l'identifiant de l'adhérent à supprimer
     * @throws IllegalArgumentException si aucun adhérent ne correspond à cet id
     */
    public void supprimerAdherent(int id) {
        boolean supprime = listeAdherents.removeIf(a -> a.getId() == id);
        if (!supprime)
            throw new IllegalArgumentException(
                    "Aucun adhérent trouvé avec l'identifiant " + id);
    }

    /**
     * Recherche des adhérents dont le nom, le prénom, le nom de naissance
     * ou l'email contient le critère fourni (insensible à la casse).
     *
     * <p>Si le critère est null ou vide, retourne l'intégralité de la liste.</p>
     *
     * @param critere la chaîne de recherche
     * @return la liste des adhérents correspondants (jamais null)
     */
    public List<Adherent> rechercherAdherent(String critere) {
        if (critere == null || critere.isBlank())
            return new ArrayList<>(listeAdherents);

        String c = critere.trim().toLowerCase();
        List<Adherent> resultats = new ArrayList<>();
        for (Adherent a : listeAdherents) {
            if ((a.getNom()          != null && a.getNom().toLowerCase().contains(c))
             || (a.getPrenom()       != null && a.getPrenom().toLowerCase().contains(c))
             || (a.getNomNaissance() != null && a.getNomNaissance().toLowerCase().contains(c))
             || (a.getEmail()        != null && a.getEmail().toLowerCase().contains(c))) {
                resultats.add(a);
            }
        }
        return resultats;
    }

    /**
     * Calcule le montant total des cotisations de tous les adhérents
     * en déduisant leur catégorie à partir de leur année de naissance (ou âge).
     *
     * @param categories la liste des catégories avec leurs cotisations
     * @return le montant total en euros
     */
    public double calculerMontantTotal(List<Categorie> categories) {
        if (categories == null) {
            return 0.0;
        }
        double total = 0.0;
        for (Adherent a : listeAdherents) {
            Categorie cat = trouverCategorie(a, categories);
            if (cat != null) {
                total += cat.getCotisation();
            }
        }
        return total;
    }

    /**
     * Génère un rapport textuel des adhérents regroupés par catégorie.
     *
     * @param categories la liste des catégories
     * @return le rapport sous forme de chaîne de caractères
     */
    public String genererRapportParCategorie(List<Categorie> categories) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Récapitulatif financier — Cercle d'Escrime de Vandœuvre ===\n\n");

        List<Categorie> listeCategories = (categories != null) ? categories : List.of();
        Map<Categorie, List<Adherent>> parCategorie = new IdentityHashMap<>();
        for (Categorie cat : listeCategories) {
            if (cat != null) {
                parCategorie.put(cat, new ArrayList<>());
            }
        }

        List<Adherent> sansCategorie = new ArrayList<>();
        for (Adherent a : listeAdherents) {
            Categorie cat = trouverCategorie(a, listeCategories);
            if (cat != null && parCategorie.containsKey(cat)) {
                parCategorie.get(cat).add(a);
            } else {
                sansCategorie.add(a);
            }
        }

        double totalGeneral = 0.0;

        for (Categorie cat : listeCategories) {
            if (cat == null) continue;
            List<Adherent> membres = parCategorie.get(cat);
            if (membres == null || membres.isEmpty()) continue;

            String suffixeCode = (cat.getCode() == null || cat.getCode().isBlank())
                    ? "" : " (" + cat.getCode() + ")";
            sb.append(String.format("── %s%s ──%n", cat.getLibelle(), suffixeCode));
            sb.append(String.format(Locale.FRANCE, "   Cotisation unitaire : %.2f €%n", cat.getCotisation()));
            sb.append(String.format("   Nombre d'adhérents  : %d%n", membres.size()));
            double sousTot = membres.size() * cat.getCotisation();
            sb.append(String.format(Locale.FRANCE, "   Sous-total          : %.2f €%n", sousTot));
            for (Adherent a : membres) {
                int age = (a.getDateNaissance() != null) ? a.getAge() : 0;
                sb.append(String.format("     • %s %s (ID %d, %d ans)%n",
                        a.getPrenom(), a.getNom(), a.getId(), age));
            }
            sb.append("\n");
            totalGeneral += sousTot;
        }

        if (!sansCategorie.isEmpty()) {
            sb.append(String.format("── Sans catégorie correspondante : %d adhérent(s) ──%n%n",
                    sansCategorie.size()));
        }

        sb.append(String.format(Locale.FRANCE, "TOTAL GÉNÉRAL : %.2f €%n", totalGeneral));
        sb.append(String.format("Nombre total d'adhérents : %d%n", listeAdherents.size()));
        return sb.toString();
    }

    /**
     * Trouve la catégorie correspondant à un adhérent (prioritairement selon son
     * année de naissance, conformément aux règles FFE et au fichier XML).
     *
     * @param adherent   l'adhérent à classer
     * @param categories la liste des catégories
     * @return la catégorie trouvée, ou {@code null} si aucune ne correspond
     */
    public Categorie trouverCategorie(Adherent adherent, List<Categorie> categories) {
        if (adherent == null || categories == null) {
            return null;
        }
        for (Categorie cat : categories) {
            if (cat != null && cat.correspondAdherent(adherent)) {
                return cat;
            }
        }
        return null;
    }

    /**
     * Trouve la catégorie correspondant à un âge donné.
     *
     * @param age        l'âge en années
     * @param categories la liste des catégories
     * @return la catégorie trouvée, ou {@code null} si aucune ne correspond
     */
    public Categorie trouverCategorie(int age, List<Categorie> categories) {
        if (categories == null) {
            return null;
        }
        for (Categorie cat : categories) {
            if (cat != null && cat.correspondAge(age)) {
                return cat;
            }
        }
        return null;
    }

    // ── Getters / Setters ──────────────────────────────────────────────────────

    public List<Adherent> getListeAdherents() {
        return listeAdherents;
    }

    /**
     * Remplace la liste complète des adhérents (utilisé lors d'une restauration).
     *
     * @param liste la nouvelle liste d'adhérents
     */
    public void setListeAdherents(List<Adherent> liste) {
        if (liste == null) {
            throw new IllegalArgumentException("La liste d'adhérents ne peut pas être null.");
        }
        this.listeAdherents = new ArrayList<>(liste);
        int maxId = this.listeAdherents.stream().mapToInt(Adherent::getId).max().orElse(0);
        compteurId.set(maxId + 1);
    }
}
