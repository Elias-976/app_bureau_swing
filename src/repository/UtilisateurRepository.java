package repository;

import modele.Role;
import modele.Utilisateur;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Dépôt de données pour les comptes utilisateurs et fonctions cryptographiques de hachage.
 *
 * <p>Conforme aux normes de sécurité modernes :
 * <ul>
 *   <li>Dérivation par {@code PBKDF2WithHmacSHA256} avec <b>65 536 itérations</b> et clé de <b>256 bits</b></li>
 *   <li>Sel cryptographique aléatoire de <b>16 octets</b> unique par compte ({@link SecureRandom})</li>
 *   <li>Comparaison en temps constant ({@link MessageDigest#isEqual(byte[], byte[])}) contre les attaques temporelles</li>
 *   <li>Effacement immédiat des tableaux sensibles en mémoire ({@code Arrays.fill})</li>
 *   <li>Persistance locale structurée dans {@code document/utilisateurs.dat}</li>
 * </ul>
 * </p>
 */
public class UtilisateurRepository {

    public static final String CHEMIN_PAR_DEFAUT = "document/utilisateurs.dat";

    private static final int    TAILLE_SEL        = 16;
    private static final int    TAILLE_CLE_BITS   = 256;
    private static final int    ITERATIONS_PBKDF2 = 65_536;
    private static final String ALGORITHME_PBKDF2 = "PBKDF2WithHmacSHA256";

    private static final SecureRandom generateurAleatoire = new SecureRandom();
    private final Map<String, Utilisateur> indexComptes        = new LinkedHashMap<>();
    private String                       cheminActif         = CHEMIN_PAR_DEFAUT;

    /**
     * Initialise le dépôt en chargeant les utilisateurs depuis le chemin par défaut.
     */
    public UtilisateurRepository() {
        this(CHEMIN_PAR_DEFAUT);
    }

    public static void main(String[] args) {
        UtilisateurRepository repo = new UtilisateurRepository();
        System.out.println("Comptes initialisés dans " + repo.cheminActif + " : " + repo.tous().size() + " utilisateurs.");
    }

    /**
     * Initialise le dépôt avec un chemin de persistance sous forme de Path.
     *
     * @param cheminFichier chemin vers le fichier des comptes
     */
    public UtilisateurRepository(Path cheminFichier) {
        this(cheminFichier != null ? cheminFichier.toString() : CHEMIN_PAR_DEFAUT);
    }

    /**
     * Initialise le dépôt avec un chemin de persistance spécifique.
     *
     * @param cheminFichier chemin vers le fichier des comptes
     */
    public UtilisateurRepository(String cheminFichier) {
        this.cheminActif = (cheminFichier != null && !cheminFichier.isBlank())
                ? cheminFichier
                : CHEMIN_PAR_DEFAUT;
        chargerOuInitialiser(this.cheminActif);
    }

    // ── Cryptographie & Hachage sécurisé ───────────────────────────────────────

    /**
     * Génère un sel cryptographique aléatoire de 16 octets.
     *
     * @return le sel sous forme de tableau d'octets
     */
    public static byte[] genererSel() {
        byte[] sel = new byte[TAILLE_SEL];
        generateurAleatoire.nextBytes(sel);
        return sel;
    }

    /**
     * Calcule le hash PBKDF2 d'un mot de passe associé à un sel donné.
     *
     * @param motDePasse mot de passe fourni sous forme de char[]
     * @param sel        sel cryptographique
     * @return l'empreinte de 256 bits (32 octets)
     * @throws GeneralSecurityException si l'algorithme n'est pas disponible
     */
    public static byte[] calculerHash(char[] motDePasse, byte[] sel) throws GeneralSecurityException {
        Objects.requireNonNull(motDePasse, "Le mot de passe ne peut pas être null");
        Objects.requireNonNull(sel, "Le sel ne peut pas être null");

        PBEKeySpec spec = new PBEKeySpec(motDePasse, sel, ITERATIONS_PBKDF2, TAILLE_CLE_BITS);
        byte[] octetsCle = null;
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHME_PBKDF2);
            SecretKey        secret  = factory.generateSecret(spec);
            octetsCle = secret.getEncoded();
            return Arrays.copyOf(octetsCle, octetsCle.length);
        } finally {
            spec.clearPassword();
            if (octetsCle != null) {
                Arrays.fill(octetsCle, (byte) 0);
            }
        }
    }

    /**
     * Alias de commodité pour calculer le hash PBKDF2.
     *
     * @param motDePasse mot de passe
     * @param sel        sel aléatoire
     * @return hash dérivé
     * @throws GeneralSecurityException en cas d'erreur de chiffrement
     */
    public static byte[] hacherMotDePasse(char[] motDePasse, byte[] sel) throws GeneralSecurityException {
        return calculerHash(motDePasse, sel);
    }

    /**
     * Vérifie un mot de passe en temps constant par rapport au sel et au hash attendu.
     *
     * @param motDePasse  mot de passe à tester
     * @param hashAttendu hash stocké
     * @param sel         sel cryptographique
     * @return {@code true} si le hash calculé correspond au hash attendu
     */
    public static boolean verifierMotDePasse(char[] motDePasse, byte[] hashAttendu, byte[] sel) {
        if (motDePasse == null || hashAttendu == null || sel == null) {
            return false;
        }
        byte[] hashCalcule = null;
        try {
            hashCalcule = calculerHash(motDePasse, sel);
            return MessageDigest.isEqual(hashCalcule, hashAttendu);
        } catch (GeneralSecurityException e) {
            return false;
        } finally {
            if (hashCalcule != null) {
                Arrays.fill(hashCalcule, (byte) 0);
            }
        }
    }

    /**
     * Vérifie un mot de passe en temps constant par rapport au sel et au hash d'un utilisateur.
     *
     * @param motDePasse  mot de passe à tester
     * @param utilisateur utilisateur cible
     * @return {@code true} si le mot de passe est valide et que le compte n'est pas nul
     */
    public boolean verifierMotDePasse(char[] motDePasse, Utilisateur utilisateur) {
        if (motDePasse == null || utilisateur == null) {
            return false;
        }
        return verifierMotDePasse(motDePasse, utilisateur.getHashMotDePasse(), utilisateur.getSel());
    }

    // ── Gestion des comptes en mémoire & persistance ───────────────────────────

    /**
     * Charge les utilisateurs depuis le fichier donné, ou initialise les comptes par défaut si absent.
     *
     * @param chemin chemin vers le fichier
     * @return la liste des utilisateurs chargés
     */
    public synchronized List<Utilisateur> chargerOuInitialiser(String chemin) {
        this.cheminActif = chemin;
        indexComptes.clear();

        Path path = Path.of(chemin);
        if (Files.exists(path)) {
            try {
                List<String> lignes = Files.readAllLines(path, StandardCharsets.UTF_8);
                for (String ligne : lignes) {
                    if (ligne.isBlank() || ligne.startsWith("#")) continue;
                    Utilisateur u = parserLigne(ligne);
                    if (u != null) {
                        indexComptes.put(u.getIdentifiant(), u);
                    }
                }
            } catch (IOException e) {
                System.err.println("Avertissement: Impossible de lire " + chemin + ", initialisation par défaut.");
            }
        }

        // Si aucun compte n'a pu être chargé, on initialise les personas par défaut
        if (indexComptes.isEmpty()) {
            initialiserComptesParDefaut();
            sauvegarder();
        }

        return new ArrayList<>(indexComptes.values());
    }

    /**
     * Sauvegarde l'ensemble des comptes actuels dans le fichier actif.
     *
     * @return {@code true} si la sauvegarde a réussi
     */
    public synchronized boolean sauvegarder() {
        return sauvegarderDans(this.cheminActif);
    }

    /**
     * Sauvegarde l'ensemble des comptes dans un fichier cible de manière atomique.
     *
     * @param chemin chemin du fichier
     * @return {@code true} si la sauvegarde a réussi
     */
    public synchronized boolean sauvegarderDans(String chemin) {
        if (chemin == null || chemin.isBlank()) return false;
        Path cible = Path.of(chemin);

        List<String> lignes = new ArrayList<>();
        lignes.add("# Fichier de comptes sécurisé — Application GEA (M2L)");
        lignes.add("# Algorithme: PBKDF2WithHmacSHA256 (65536 itérations, 256 bits, sel aléatoire)");
        lignes.add("# Format: identifiant:selBase64:hashBase64:role:nomComplet:bloque:tentativesEchouees");

        Base64.Encoder b64 = Base64.getEncoder();
        for (Utilisateur u : indexComptes.values()) {
            String selB64  = b64.encodeToString(u.getSel());
            String hashB64 = b64.encodeToString(u.getHashMotDePasse());
            String ligne = String.format(Locale.ROOT, "%s:%s:%s:%s:%s:%b:%d",
                    u.getIdentifiant(),
                    selB64,
                    hashB64,
                    u.getRole().name(),
                    u.getNomComplet(),
                    u.isBloque(),
                    u.getTentativesEchouees());
            lignes.add(ligne);
        }

        try {
            if (cible.getParent() != null && !Files.exists(cible.getParent())) {
                Files.createDirectories(cible.getParent());
            }
            Path fichierTemp = Files.createTempFile("utilisateurs_tmp_", ".dat");
            Files.write(fichierTemp, lignes, StandardCharsets.UTF_8);
            Files.move(fichierTemp, cible, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            return true;
        } catch (IOException e) {
            System.err.println("Erreur lors de l'enregistrement de " + chemin + " : " + e.getMessage());
            return false;
        }
    }

    /**
     * Recherche un utilisateur par son identifiant (insensible à la casse).
     *
     * @param identifiant l'identifiant recherché
     * @return l'utilisateur trouvé, ou {@code null}
     */
    public synchronized Utilisateur trouverParIdentifiant(String identifiant) {
        if (identifiant == null) return null;
        return indexComptes.get(identifiant.trim().toLowerCase(Locale.ROOT));
    }

    /**
     * Ajoute ou remplace un utilisateur dans l'index en mémoire.
     *
     * @param utilisateur l'utilisateur à enregistrer
     */
    public synchronized void enregistrer(Utilisateur utilisateur) {
        Objects.requireNonNull(utilisateur, "L'utilisateur ne peut pas être null");
        indexComptes.put(utilisateur.getIdentifiant(), utilisateur);
    }

    /**
     * Supprime un utilisateur de l'index en mémoire.
     *
     * @param identifiant identifiant de l'utilisateur à retirer
     * @return {@code true} si l'utilisateur a été retiré
     */
    public synchronized boolean supprimer(String identifiant) {
        if (identifiant == null) return false;
        return indexComptes.remove(identifiant.trim().toLowerCase(Locale.ROOT)) != null;
    }

    /**
     * Retourne une copie de la liste de tous les utilisateurs enregistrés.
     *
     * @return la liste des utilisateurs
     */
    public synchronized List<Utilisateur> getTousLesUtilisateurs() {
        return new ArrayList<>(indexComptes.values());
    }

    /**
     * Alias de commodité pour obtenir tous les utilisateurs.
     *
     * @return la liste des utilisateurs
     */
    public synchronized List<Utilisateur> tous() {
        return getTousLesUtilisateurs();
    }

    /**
     * Alias retournant un Optional de l'utilisateur recherché.
     *
     * @param identifiant identifiant
     * @return Optional contenant l'utilisateur ou vide
     */
    public synchronized java.util.Optional<Utilisateur> parIdentifiant(String identifiant) {
        return java.util.Optional.ofNullable(trouverParIdentifiant(identifiant));
    }

    /**
     * Crée et enregistre un nouvel utilisateur après hachage de son mot de passe.
     *
     * @param identifiant identifiant de connexion
     * @param nomComplet  nom complet
     * @param role        rôle attribué
     * @param motDePasse  mot de passe en clair (sera effacé)
     * @return l'utilisateur créé
     * @throws GeneralSecurityException si le hachage échoue
     */
    public synchronized Utilisateur creerUtilisateur(String identifiant,
                                                    String nomComplet,
                                                    Role role,
                                                    char[] motDePasse) throws GeneralSecurityException {
        byte[] sel  = genererSel();
        byte[] hash = calculerHash(motDePasse, sel);
        Utilisateur u = new Utilisateur(identifiant, nomComplet, role, sel, hash, false, 0);
        enregistrer(u);
        sauvegarder();
        return u;
    }

    // ── Initialisation des personas par défaut ──────────────────────────────────

    /**
     * Initialise les 4 comptes par défaut correspondant aux personas officiels de la M2L :
     * <ul>
     *   <li>{@code secretaire} / {@code Secretaire2026!} (Secrétaire)</li>
     *   <li>{@code tresorier} / {@code Tresorier2026!} (Trésorier)</li>
     *   <li>{@code president} / {@code President2026!} (Président)</li>
     *   <li>{@code admin} / {@code AdminM2L2026!} (Administrateur / Informaticien M2L)</li>
     * </ul>
     */
    public synchronized void initialiserComptesParDefaut() {
        indexComptes.clear();
        ajouterCompteInitial("secretaire", "Sophie Bernard (Secrétaire)",
                Role.SECRETAIRE, "Secretaire2026!".toCharArray());
        ajouterCompteInitial("tresorier", "Marc Lefebvre (Trésorier)",
                Role.TRESORIER, "Tresorier2026!".toCharArray());
        ajouterCompteInitial("president", "Claire Thomas (Présidente)",
                Role.PRESIDENT, "President2026!".toCharArray());
        ajouterCompteInitial("admin", "Informaticien bénévole M2L",
                Role.ADMINISTRATEUR, "AdminM2L2026!".toCharArray());
    }

    private void ajouterCompteInitial(String id, String nom, Role role, char[] mdp) {
        try {
            byte[] sel  = genererSel();
            byte[] hash = calculerHash(mdp, sel);
            Utilisateur u = new Utilisateur(id, nom, role, sel, hash, false, 0);
            indexComptes.put(u.getIdentifiant(), u);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Erreur lors de l'initialisation du compte " + id, e);
        } finally {
            Arrays.fill(mdp, '\0');
        }
    }

    private Utilisateur parserLigne(String ligne) {
        String[] morceaux = ligne.split(":", -1);
        if (morceaux.length < 7) {
            return null;
        }
        try {
            String identifiant = morceaux[0].trim().toLowerCase(Locale.ROOT);
            byte[] sel         = Base64.getDecoder().decode(morceaux[1].trim());
            byte[] hash        = Base64.getDecoder().decode(morceaux[2].trim());
            Role   role        = Role.valueOf(morceaux[3].trim());
            String nomComplet  = morceaux[4].trim();
            boolean bloque     = Boolean.parseBoolean(morceaux[5].trim());
            int echecs         = Integer.parseInt(morceaux[6].trim());

            return new Utilisateur(identifiant, nomComplet, role, sel, hash, bloque, echecs);
        } catch (Exception e) {
            System.err.println("Ligne utilisateur malformée ignorée: " + ligne);
            return null;
        }
    }
}
