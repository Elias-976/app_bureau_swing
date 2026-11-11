package modele;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.Period;

/**
 * Classe métier représentant un adhérent du Cercle d'Escrime de Vandœuvre.
 */
public class Adherent implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    // ── Attributs ──────────────────────────────────────────────────────────────
    private int       id;
    private String    nom;
    private String    prenom;
    private String    nomNaissance;
    private LocalDate dateNaissance;
    private String    adresse;
    private String    codePostal;
    private String    ville;
    private String    telephone;
    private String    email;
    /** Arme pratiquée : Fleuret, Épée ou Sabre */
    private String    arme;
    /** Type de pratique : Compétition ou Loisir */
    private String    pratique;
    /** Latéralité : Droitier ou Gaucher */
    private String    lateralite;
    private String    nomRespLegal;
    private String    prenomRespLegal;

    // ── Constructeurs ──────────────────────────────────────────────────────────

    /** Constructeur complet */
    public Adherent(int id, String nom, String prenom, String nomNaissance,
                    LocalDate dateNaissance, String adresse, String codePostal,
                    String ville, String telephone, String email,
                    String arme, String pratique, String lateralite,
                    String nomRespLegal, String prenomRespLegal) {
        this.id              = id;
        this.nom             = nom;
        this.prenom          = prenom;
        this.nomNaissance    = nomNaissance;
        this.dateNaissance   = dateNaissance;
        this.adresse         = adresse;
        this.codePostal      = codePostal;
        this.ville           = ville;
        this.telephone       = telephone;
        this.email           = email;
        this.arme            = arme;
        this.pratique        = pratique;
        this.lateralite      = lateralite;
        this.nomRespLegal    = nomRespLegal;
        this.prenomRespLegal = prenomRespLegal;
    }

    /** Constructeur sans identifiant (pour la création avant persistance) */
    public Adherent(String nom, String prenom, String nomNaissance,
                    LocalDate dateNaissance, String adresse, String codePostal,
                    String ville, String telephone, String email,
                    String arme, String pratique, String lateralite,
                    String nomRespLegal, String prenomRespLegal) {
        this(0, nom, prenom, nomNaissance, dateNaissance, adresse, codePostal,
             ville, telephone, email, arme, pratique, lateralite,
             nomRespLegal, prenomRespLegal);
    }

    // ── Méthodes métier ────────────────────────────────────────────────────────

    /**
     * Calcule l'âge de l'adhérent en années révolues à la date du jour.
     *
     * @return âge en années
     */
    public int getAge() {
        return Period.between(this.dateNaissance, LocalDate.now()).getYears();
    }

    /**
     * Indique si l'adhérent est mineur (moins de 18 ans).
     *
     * @return {@code true} si l'adhérent a moins de 18 ans
     */
    public boolean estMineur() {
        return getAge() < 18;
    }

    @Override
    public String toString() {
        return String.format("Adherent{id=%d, nom='%s', prenom='%s', dateNaissance=%s, age=%d, mineur=%b}",
                id, nom, prenom, dateNaissance, getAge(), estMineur());
    }

    // ── Getters & Setters ──────────────────────────────────────────────────────

    public int getId()                        { return id; }
    public void setId(int id)                 { this.id = id; }

    public String getNom()                    { return nom; }
    public void setNom(String nom)            { this.nom = nom; }

    public String getPrenom()                 { return prenom; }
    public void setPrenom(String prenom)      { this.prenom = prenom; }

    public String getNomNaissance()           { return nomNaissance; }
    public void setNomNaissance(String n)     { this.nomNaissance = n; }

    public LocalDate getDateNaissance()       { return dateNaissance; }
    public void setDateNaissance(LocalDate d) { this.dateNaissance = d; }

    public String getAdresse()                { return adresse; }
    public void setAdresse(String adresse)    { this.adresse = adresse; }

    public String getCodePostal()             { return codePostal; }
    public void setCodePostal(String cp)      { this.codePostal = cp; }

    public String getVille()                  { return ville; }
    public void setVille(String ville)        { this.ville = ville; }

    public String getTelephone()              { return telephone; }
    public void setTelephone(String tel)      { this.telephone = tel; }

    public String getEmail()                  { return email; }
    public void setEmail(String email)        { this.email = email; }

    public String getArme()                   { return arme; }
    public void setArme(String arme)          { this.arme = arme; }

    public String getPratique()               { return pratique; }
    public void setPratique(String pratique)  { this.pratique = pratique; }

    public String getLateralite()             { return lateralite; }
    public void setLateralite(String lat)     { this.lateralite = lat; }

    public String getNomRespLegal()           { return nomRespLegal; }
    public void setNomRespLegal(String n)     { this.nomRespLegal = n; }

    public String getPrenomRespLegal()        { return prenomRespLegal; }
    public void setPrenomRespLegal(String p)  { this.prenomRespLegal = p; }
}

