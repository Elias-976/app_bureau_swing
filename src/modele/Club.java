package modele;

/**
 * Classe métier représentant un club d'escrime de la Ligue de Lorraine.
 */
public class Club {

    // ── Attributs ──────────────────────────────────────────────────────────────
    private String nom;
    private String ville;
    private String adresse;
    private String benevoleNom;
    private String benevoleTelephone;
    private String benevoleEmail;
    private String site;

    // ── Constructeurs ──────────────────────────────────────────────────────────

    /** Constructeur complet incluant le site web du club */
    public Club(String nom, String ville, String adresse,
                String benevoleNom, String benevoleTelephone,
                String benevoleEmail, String site) {
        this.nom               = nom;
        this.ville             = ville;
        this.adresse           = adresse;
        this.benevoleNom       = benevoleNom;
        this.benevoleTelephone = benevoleTelephone;
        this.benevoleEmail     = benevoleEmail;
        this.site              = site;
    }

    /** Constructeur complet (compatibilité sans site web) */
    public Club(String nom, String ville, String adresse,
                String benevoleNom, String benevoleTelephone, String benevoleEmail) {
        this(nom, ville, adresse, benevoleNom, benevoleTelephone, benevoleEmail, "");
    }

    // ── Méthodes métier ────────────────────────────────────────────────────────

    /**
     * Retourne les coordonnées du club sous forme lisible.
     *
     * @return chaîne de caractères formatée avec nom, adresse, site et contact
     */
    public String afficherCoordonnees() {
        String siteAffiche = (site == null || site.isBlank()) ? "—" : site;
        return String.format(
                "Club : %s%nAdresse : %s%nSite web : %s%nContact bénévole : %s%nTéléphone : %s%nEmail : %s",
                nom, adresse, siteAffiche, benevoleNom, benevoleTelephone, benevoleEmail);
    }

    @Override
    public String toString() {
        return String.format("Club{nom='%s', ville='%s', adresse='%s'}",
                nom, ville, adresse);
    }

    // ── Getters & Setters ──────────────────────────────────────────────────────

    public String getNom()                            { return nom; }
    public void setNom(String nom)                    { this.nom = nom; }

    public String getVille()                          { return ville; }
    public void setVille(String ville)                { this.ville = ville; }

    public String getAdresse()                        { return adresse; }
    public void setAdresse(String adresse)            { this.adresse = adresse; }

    public String getBenevoleNom()                    { return benevoleNom; }
    public void setBenevoleNom(String n)              { this.benevoleNom = n; }

    public String getBenevoleTelephone()              { return benevoleTelephone; }
    public void setBenevoleTelephone(String tel)      { this.benevoleTelephone = tel; }

    public String getBenevoleEmail()                  { return benevoleEmail; }
    public void setBenevoleEmail(String email)        { this.benevoleEmail = email; }

    public String getSite()                           { return site; }
    public void setSite(String site)                  { this.site = site; }
}

