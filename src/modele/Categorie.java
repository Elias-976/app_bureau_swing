package modele;

/**
 * Classe métier représentant une catégorie d'âge en escrime
 * (Éveil, Atomes, Poussins, etc.).
 */
public class Categorie {

    // ── Attributs ──────────────────────────────────────────────────────────────
    private String code;
    private String libelle;
    private int    ageMin;
    private int    ageMax;
    private double cotisation;

    // ── Constructeurs ──────────────────────────────────────────────────────────

    /** Constructeur complet */
    public Categorie(String code, String libelle, int ageMin, int ageMax, double cotisation) {
        this.code       = code;
        this.libelle    = libelle;
        this.ageMin     = ageMin;
        this.ageMax     = ageMax;
        this.cotisation = cotisation;
    }

    /** Constructeur sans cotisation (pour chargement depuis XML) */
    public Categorie(String code, String libelle, int ageMin, int ageMax) {
        this(code, libelle, ageMin, ageMax, 0.0);
    }

    // ── Méthodes métier ────────────────────────────────────────────────────────

    /**
     * Vérifie si un âge donné correspond à cette catégorie.
     *
     * @param age l'âge à tester (en années)
     * @return {@code true} si ageMin <= age <= ageMax
     */
    public boolean correspondAge(int age) {
        return age >= ageMin && age <= ageMax;
    }

    @Override
    public String toString() {
        return String.format("Categorie{code='%s', libelle='%s', ageMin=%d, ageMax=%d, cotisation=%.2f}",
                code, libelle, ageMin, ageMax, cotisation);
    }

    // ── Getters & Setters ──────────────────────────────────────────────────────

    public String getCode()                   { return code; }
    public void setCode(String code)          { this.code = code; }

    public String getLibelle()                { return libelle; }
    public void setLibelle(String libelle)    { this.libelle = libelle; }

    public int getAgeMin()                    { return ageMin; }
    public void setAgeMin(int ageMin)         { this.ageMin = ageMin; }

    public int getAgeMax()                    { return ageMax; }
    public void setAgeMax(int ageMax)         { this.ageMax = ageMax; }

    public double getCotisation()             { return cotisation; }
    public void setCotisation(double c)       { this.cotisation = c; }
}

