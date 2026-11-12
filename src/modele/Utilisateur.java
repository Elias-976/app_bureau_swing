package modele;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;

/**
 * Entité représentant un compte utilisateur de l'application GEA.
 *
 * <p>Conformément aux bonnes pratiques de sécurité, aucun mot de passe n'est stocké en clair :
 * seuls un sel cryptographique aléatoire de 16 octets et un hash calculé via
 * {@code PBKDF2WithHmacSHA256} (clé 256 bits, 65 536 itérations) sont conservés.</p>
 */
public class Utilisateur {

    private final String  identifiant;
    private String        nomComplet;
    private Role          role;
    private byte[]        sel;
    private byte[]        hashMotDePasse;
    private boolean       bloque;
    private int           tentativesEchouees;
    private LocalDateTime dateDerniereConnexion;
    private LocalDateTime dateDernierEchec;

    /**
     * Constructeur complet d'un compte utilisateur.
     *
     * @param identifiant        identifiant unique de connexion (insensible à la casse)
     * @param nomComplet         nom et prénom ou libellé de la personne
     * @param role               rôle RBAC attribué
     * @param sel                sel cryptographique aléatoire
     * @param hashMotDePasse     empreinte dérivée PBKDF2
     * @param bloque             {@code true} si le compte est verrouillé
     * @param tentativesEchouees nombre de tentatives infructueuses consécutives
     */
    public Utilisateur(String identifiant,
                       String nomComplet,
                       Role role,
                       byte[] sel,
                       byte[] hashMotDePasse,
                       boolean bloque,
                       int tentativesEchouees) {
        this.identifiant        = Objects.requireNonNull(identifiant, "L'identifiant est obligatoire").trim().toLowerCase();
        this.nomComplet         = Objects.requireNonNull(nomComplet, "Le nom complet est obligatoire").trim();
        this.role               = Objects.requireNonNull(role, "Le rôle est obligatoire");
        this.sel                = sel != null ? Arrays.copyOf(sel, sel.length) : new byte[0];
        this.hashMotDePasse     = hashMotDePasse != null ? Arrays.copyOf(hashMotDePasse, hashMotDePasse.length) : new byte[0];
        this.bloque             = bloque;
        this.tentativesEchouees = Math.max(0, tentativesEchouees);
    }

    /**
     * Indique si l'utilisateur possède un droit applicatif donné.
     *
     * @param droit le droit à vérifier
     * @return {@code true} si l'utilisateur possède ce droit
     */
    public boolean possedeDroit(Droit droit) {
        return role != null && role.possedeDroit(droit);
    }

    /**
     * Réinitialise le compteur d'échecs et met à jour l'horodatage de dernière connexion réussie.
     */
    public void enregistrerSuccesConnexion() {
        this.tentativesEchouees     = 0;
        this.dateDerniereConnexion  = LocalDateTime.now();
    }

    /**
     * Enregistre un échec de saisie du mot de passe.
     * Si le seuil maximal est atteint, le compte est automatiquement verrouillé.
     *
     * @param maxTentatives seuil maximal de tentatives avant verrouillage
     */
    public void enregistrerEchecConnexion(int maxTentatives) {
        this.tentativesEchouees++;
        this.dateDernierEchec = LocalDateTime.now();
        if (this.tentativesEchouees >= maxTentatives) {
            this.bloque = true;
        }
    }

    /**
     * Déverrouille le compte et réinitialise les tentatives infructueuses.
     */
    public void debloquer() {
        this.bloque             = false;
        this.tentativesEchouees = 0;
    }

    /**
     * Met à jour le sel et le hash lors d'un changement de mot de passe.
     *
     * @param nouveauSel  nouveau sel cryptographique
     * @param nouveauHash nouveau hash dérivé PBKDF2
     */
    public void mettreAJourMotDePasse(byte[] nouveauSel, byte[] nouveauHash) {
        this.sel                = Arrays.copyOf(nouveauSel, nouveauSel.length);
        this.hashMotDePasse     = Arrays.copyOf(nouveauHash, nouveauHash.length);
        this.bloque             = false;
        this.tentativesEchouees = 0;
    }

    /**
     * Alias pour réinitialiser le mot de passe.
     *
     * @param nouveauHash hash cryptographique
     * @param nouveauSel  sel aléatoire
     */
    public void reinitialiserMotDePasse(byte[] nouveauHash, byte[] nouveauSel) {
        mettreAJourMotDePasse(nouveauSel, nouveauHash);
    }

    /**
     * Incrémente les tentatives échouées de 1.
     */
    public void incrementerEchec() {
        this.tentativesEchouees++;
        this.dateDernierEchec = LocalDateTime.now();
    }

    // ── Getters & Setters ──────────────────────────────────────────────────────

    public String getIdentifiant() {
        return identifiant;
    }

    public String getNomComplet() {
        return nomComplet;
    }

    public void setNomComplet(String nomComplet) {
        this.nomComplet = Objects.requireNonNull(nomComplet, "Le nom complet ne peut pas être null").trim();
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = Objects.requireNonNull(role, "Le rôle ne peut pas être null");
    }

    public byte[] getSel() {
        return Arrays.copyOf(sel, sel.length);
    }

    public byte[] getHashMotDePasse() {
        return Arrays.copyOf(hashMotDePasse, hashMotDePasse.length);
    }

    public byte[] getMotDePasseHache() {
        return getHashMotDePasse();
    }

    public boolean isBloque() {
        return bloque;
    }

    public boolean estVerrouille() {
        return isBloque();
    }

    public void setBloque(boolean bloque) {
        this.bloque = bloque;
    }

    public int getTentativesEchouees() {
        return tentativesEchouees;
    }

    public LocalDateTime getDateDerniereConnexion() {
        return dateDerniereConnexion;
    }

    public LocalDateTime getDerniereConnexion() {
        return getDateDerniereConnexion();
    }

    public void setDateDerniereConnexion(LocalDateTime dateDerniereConnexion) {
        this.dateDerniereConnexion = dateDerniereConnexion;
    }

    public LocalDateTime getDateDernierEchec() {
        return dateDernierEchec;
    }

    public void setDateDernierEchec(LocalDateTime dateDernierEchec) {
        this.dateDernierEchec = dateDernierEchec;
    }

    @Override
    public String toString() {
        return String.format("Utilisateur{identifiant='%s', nomComplet='%s', role=%s, bloque=%b, echecs=%d}",
                identifiant, nomComplet, role != null ? role.getLibelleCourt() : "aucun", bloque, tentativesEchouees);
    }
}
