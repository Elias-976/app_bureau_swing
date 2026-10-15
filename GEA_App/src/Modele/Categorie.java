package Modele;

public class Categorie {
    // Attributs
    private int id;
    private String nom; // ex : "Poussins"
    private String code; // ex : "M9"  (vide pour Séniors et Vétérans)
    private int anneeMin; // année de naissance minimale de la tranche
    private int anneeMax; // année de naissance maximale de la tranche
    private double tarif; // cotisation annuel en euros

    // Constructeur
    public Categorie(int id, String nom, String code, int anneeMin, int anneeMax, double tarif) {
        this.id = id;
        this.nom = nom;
        this.code = code;
        this.anneeMin = anneeMin;
        this.anneeMax = anneeMax;
        this.tarif = tarif;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public String getCode() {
        return code;
    }

    public int getAnneeMin() {
        return anneeMin;
    }

    public int getAnneeMax() {
        return anneeMax;
    }

    public double getTarif() {
        return tarif;
    }

    // Méthode
    // Retourne true si l'année de naissance passée en paramètre est dans la tranche de cette catégorie.
    // Exemple : catégorie Poussins (anneeMin=2018, anneeMax=2019)
    //   -> correspond(2018) retourne true
    //   -> correspond(2020) retourne false
    public boolean correspond(int anneeNaissance) {
        return anneeNaissance >= anneeMin && anneeNaissance <= anneeMax;
    }

    @Override
    public String toString() {
        return nom + " (" + code + ") | nés entre " + anneeMin + " et " + anneeMax + " | " + tarif + " €";
    }
}