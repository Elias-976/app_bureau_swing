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
    private int    anneeMin;
    private int    anneeMax;
    private double cotisation;

    // ── Constructeurs ──────────────────────────────────────────────────────────

    /** Constructeur complet incluant l'intervalle d'années de naissance */
    public Categorie(String code, String libelle, int ageMin, int ageMax,
                     int anneeMin, int anneeMax, double cotisation) {
        this.code       = code;
        this.libelle    = libelle;
        this.ageMin     = ageMin;
        this.ageMax     = ageMax;
        this.anneeMin   = anneeMin;
        this.anneeMax   = anneeMax;
        this.cotisation = cotisation;
    }

    /** Constructeur complet (compatibilité par âges) */
    public Categorie(String code, String libelle, int ageMin, int ageMax, double cotisation) {
        this(code, libelle, ageMin, ageMax, 0, 0, cotisation);
    }

    /** Constructeur sans cotisation (pour chargement depuis XML) */
    public Categorie(String code, String libelle, int ageMin, int ageMax) {
        this(code, libelle, ageMin, ageMax, 0, 0, 0.0);
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

    /**
     * Vérifie si une année de naissance donnée correspond à cette catégorie.
     *
     * @param anneeNaissance l'année de naissance à tester
     * @return {@code true} si l'intervalle d'années est défini et que
     *         {@code anneeMin <= anneeNaissance <= anneeMax}
     */
    public boolean correspondAnneeNaissance(int anneeNaissance) {
        if (anneeMin <= 0 || anneeMax <= 0) {
            return false;
        }
        return anneeNaissance >= anneeMin && anneeNaissance <= anneeMax;
    }

    /**
     * Vérifie si un adhérent correspond à cette catégorie.
     * Utilise en priorité l'intervalle d'années de naissance défini dans le XML
     * (règle FFE), et se rabat sur l'intervalle d'âge sinon.
     *
     * @param adherent l'adhérent à tester
     * @return {@code true} si l'adhérent appartient à cette catégorie
     */
    public boolean correspondAdherent(Adherent adherent) {
        if (adherent == null || adherent.getDateNaissance() == null) {
            return false;
        }
        if (anneeMin > 0 && anneeMax > 0) {
            return correspondAnneeNaissance(adherent.getDateNaissance().getYear());
        }
        return correspondAge(adherent.getAge());
    }

    @Override
    public String toString() {
        return String.format(
                "Categorie{code='%s', libelle='%s', anneeMin=%d, anneeMax=%d, ageMin=%d, ageMax=%d, cotisation=%.2f}",
                code, libelle, anneeMin, anneeMax, ageMin, ageMax, cotisation);
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

    public int getAnneeMin()                  { return anneeMin; }
    public void setAnneeMin(int anneeMin)     { this.anneeMin = anneeMin; }

    public int getAnneeMax()                  { return anneeMax; }
    public void setAnneeMax(int anneeMax)     { this.anneeMax = anneeMax; }

    public double getCotisation()             { return cotisation; }
    public void setCotisation(double c)       { this.cotisation = c; }
}

