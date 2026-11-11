package manager;

import modele.Adherent;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputFilter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Gestionnaire sécurisé de sauvegarde et de restauration des données adhérents.
 *
 * <p>Les données sérialisées en mémoire sont chiffrées avant écriture sur disque
 * avec <b>AES-256-GCM</b> ({@code AES/GCM/NoPadding}) et une clé dérivée via
 * <b>PBKDF2WithHmacSHA256</b> à partir d'un mot de passe et d'un sel aléatoire.
 * Aucune donnée personnelle n'est ainsi stockée en clair dans le fichier {@code .gea}.</p>
 *
 * <p>À l'importation, l'intégrité et l'authenticité du flux sont vérifiées par
 * le tag GCM avant toute désérialisation, puis un {@link ObjectInputFilter} strict
 * restreint les classes autorisées.</p>
 */
public class SauvegardeManager {

    // ── Constantes cryptographiques et format de fichier ───────────────────────

    /** En-tête magique identifiant un fichier de sauvegarde chiffré GEA. */
    private static final byte[] EN_TETE_MAGIQUE =
            "GEA1".getBytes(StandardCharsets.US_ASCII);

    /** Numéro de version du format binaire chiffré. */
    private static final byte VERSION_FORMAT = 0x01;

    /** Taille du sel aléatoire pour PBKDF2 (en octets). */
    private static final int TAILLE_SEL = 16;

    /** Taille du vecteur d'initialisation GCM (en octets). */
    private static final int TAILLE_IV = 12;

    /** Taille du tag d'authentification GCM (en bits). */
    private static final int TAILLE_TAG_GCM_BITS = 128;

    /** Longueur de la clé AES dérivée (en bits). */
    private static final int TAILLE_CLE_BITS = 256;

    /** Nombre d'itérations pour PBKDF2WithHmacSHA256. */
    private static final int ITERATIONS_PBKDF2 = 65_536;

    /** Taille minimale d'un fichier chiffré valide (en-tête + sel + IV + tag GCM). */
    private static final int TAILLE_MIN_FICHIER =
            EN_TETE_MAGIQUE.length + 1 + TAILLE_SEL + TAILLE_IV + (TAILLE_TAG_GCM_BITS / 8);

    /** Taille maximale autorisée à l'importation (50 Mo) contre les fichiers anormaux. */
    private static final long TAILLE_MAX_FICHIER = 50L * 1024L * 1024L;

    /** Clé applicative utilisée uniquement par les surcharges sans mot de passe. */
    private static final char[] CLE_APPLICATIVE_PAR_DEFAUT =
            "GEA-M2L-Escrime-Vandoeuvre-2026!".toCharArray();

    /** Liste blanche stricte des classes autorisées lors de la désérialisation. */
    private static final Set<String> CLASSES_AUTORISEES = Set.of(
            ArrayList.class.getName(),
            Object[].class.getName(),
            Adherent.class.getName(),
            LocalDate.class.getName(),
            "java.time.Ser",
            String.class.getName()
    );

    private final SecureRandom generateurAleatoire = new SecureRandom();
    private String dernierMessageErreur;

    // ── Exportation (US10) ─────────────────────────────────────────────────────

    /**
     * Exporte et chiffre la liste des adhérents avec la clé applicative par défaut.
     *
     * @param chemin  chemin du fichier {@code .gea} de destination
     * @param donnees la liste des adhérents à exporter
     * @return {@code true} si l'écriture chiffrée a réussi, {@code false} sinon
     */
    public boolean exporterDonnees(String chemin, List<Adherent> donnees) {
        return exporterDonnees(chemin, donnees, CLE_APPLICATIVE_PAR_DEFAUT);
    }

    /**
     * Exporte et chiffre la liste des adhérents vers un fichier {@code .gea}
     * à l'aide du mot de passe fourni (PBKDF2 + AES-256-GCM).
     *
     * @param chemin     chemin du fichier de destination
     * @param donnees    liste des adhérents à exporter
     * @param motDePasse mot de passe utilisé pour dériver la clé de chiffrement
     * @return {@code true} si l'écriture chiffrée a réussi, {@code false} en cas d'erreur
     */
    public boolean exporterDonnees(String chemin, List<Adherent> donnees, char[] motDePasse) {
        dernierMessageErreur = null;

        if (chemin == null || chemin.isBlank()) {
            dernierMessageErreur = "Le chemin du fichier de sauvegarde est invalide.";
            return false;
        }
        if (donnees == null) {
            dernierMessageErreur = "La liste d'adhérents à exporter ne peut pas être null.";
            return false;
        }
        if (motDePasse == null || motDePasse.length == 0) {
            dernierMessageErreur = "Le mot de passe de chiffrement ne peut pas être vide.";
            return false;
        }
        for (Adherent a : donnees) {
            if (a == null) {
                dernierMessageErreur = "La liste contient une entrée adhérent null.";
                return false;
            }
        }

        try {
            byte[] donneesSerialisees = serialiserListe(donnees);
            byte[] contenuChiffre     = chiffrerDonnees(donneesSerialisees, motDePasse);
            Path   cible              = Path.of(chemin);
            Files.write(cible, contenuChiffre);
            return true;
        } catch (IOException | GeneralSecurityException | InvalidPathException e) {
            dernierMessageErreur = "Impossible d'écrire la sauvegarde chiffrée : " + e.getMessage();
            return false;
        }
    }

    // ── Importation (US11) ─────────────────────────────────────────────────────

    /**
     * Importe et déchiffre la liste des adhérents avec la clé applicative par défaut.
     *
     * @param chemin chemin du fichier {@code .gea} source
     * @return la liste restaurée (éventuellement vide si la sauvegarde contenait 0 adhérent),
     *         ou {@code null} si une erreur est survenue
     */
    public List<Adherent> importerDonnees(String chemin) {
        return importerDonnees(chemin, CLE_APPLICATIVE_PAR_DEFAUT);
    }

    /**
     * Importe, déchiffre et désérialise de manière sécurisée la liste des adhérents
     * depuis un fichier {@code .gea}.
     *
     * <p>Ne propage aucune exception non gérée : en cas d'erreur (fichier absent,
     * mauvais mot de passe, fichier corrompu, classe non autorisée, mauvais type),
     * retourne {@code null} et renseigne {@link #getDernierMessageErreur()}.</p>
     *
     * @param chemin     chemin du fichier source
     * @param motDePasse mot de passe utilisé lors de l'exportation
     * @return la liste restaurée (jamais {@code null} en cas de succès, même avec 0 adhérent),
     *         ou {@code null} en cas d'erreur
     */
    public List<Adherent> importerDonnees(String chemin, char[] motDePasse) {
        dernierMessageErreur = null;

        if (chemin == null || chemin.isBlank()) {
            dernierMessageErreur = "Le chemin du fichier de sauvegarde est invalide.";
            return null;
        }
        if (motDePasse == null || motDePasse.length == 0) {
            dernierMessageErreur = "Le mot de passe de déchiffrement ne peut pas être vide.";
            return null;
        }

        try {
            Path source = Path.of(chemin);
            if (!Files.exists(source) || Files.isDirectory(source)) {
                dernierMessageErreur = "Fichier introuvable ou invalide : " + chemin;
                return null;
            }
            long taille = Files.size(source);
            if (taille < TAILLE_MIN_FICHIER) {
                dernierMessageErreur = "Format de sauvegarde invalide ou fichier tronqué.";
                return null;
            }
            if (taille > TAILLE_MAX_FICHIER) {
                dernierMessageErreur = "Le fichier dépasse la taille maximale autorisée.";
                return null;
            }

            byte[] contenuChiffre = Files.readAllBytes(source);
            byte[] donneesClaires = dechiffrerDonnees(contenuChiffre, motDePasse);
            return deserialiserListeSecurisee(donneesClaires);

        } catch (FileNotFoundException | NoSuchFileException e) {
            dernierMessageErreur = "Fichier introuvable : " + chemin;
        } catch (GeneralSecurityException e) {
            dernierMessageErreur = "Déchiffrement impossible : mot de passe incorrect ou fichier altéré.";
        } catch (ClassCastException e) {
            dernierMessageErreur = "Contenu de sauvegarde invalide : " + e.getMessage();
        } catch (IOException | ClassNotFoundException | SecurityException | IllegalArgumentException e) {
            dernierMessageErreur = "Fichier de sauvegarde corrompu ou incompatible : " + e.getMessage();
        }
        return null;
    }

    /**
     * Désérialise une liste d'adhérents depuis des octets déjà déchiffrés en appliquant
     * un {@link ObjectInputFilter} strict et une vérification complète des types.
     *
     * @param donneesSerialisees flux d'octets produit par {@link ObjectOutputStream}
     * @return la liste typée d'adhérents
     * @throws IOException            si le flux est corrompu ou rejeté par le filtre
     * @throws ClassNotFoundException si une classe référencée est introuvable
     * @throws ClassCastException     si la racine est {@code null}, n'est pas une {@link List}
     *                                ou contient un élément qui n'est pas un {@link Adherent}
     */
    public List<Adherent> deserialiserListeSecurisee(byte[] donneesSerialisees)
            throws IOException, ClassNotFoundException {
        if (donneesSerialisees == null || donneesSerialisees.length == 0) {
            throw new IOException("Le flux sérialisé est vide.");
        }

        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(donneesSerialisees))) {
            ois.setObjectInputFilter(creerFiltreDeserialisation());
            Object objet = ois.readObject();

            if (!(objet instanceof List<?> listeBrute)) {
                throw new ClassCastException(
                        "L'objet restauré n'est pas une liste d'adhérents valide.");
            }

            List<Adherent> resultat = new ArrayList<>(listeBrute.size());
            for (Object element : listeBrute) {
                if (!(element instanceof Adherent adherent)) {
                    throw new ClassCastException(
                            "Élément inattendu dans la sauvegarde (attendu : Adherent).");
                }
                resultat.add(adherent);
            }
            return resultat;
        }
    }

    /**
     * Retourne le message descriptif de la dernière erreur rencontrée lors
     * d'un export ou d'un import, ou {@code null} si la dernière opération a réussi.
     *
     * @return le dernier message d'erreur ou {@code null}
     */
    public String getDernierMessageErreur() {
        return dernierMessageErreur;
    }

    // ── Chiffrement / Déchiffrement internes ──────────────────────────────────

    private byte[] serialiserListe(List<Adherent> donnees) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(new ArrayList<>(donnees));
        }
        return baos.toByteArray();
    }

    private byte[] chiffrerDonnees(byte[] donneesClaires, char[] motDePasse)
            throws GeneralSecurityException {
        byte[] sel = new byte[TAILLE_SEL];
        byte[] iv  = new byte[TAILLE_IV];
        generateurAleatoire.nextBytes(sel);
        generateurAleatoire.nextBytes(iv);

        byte[] enTete = construireEnTete(sel, iv);
        SecretKeySpec cle = deriverCleAES(motDePasse, sel);
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, cle, new GCMParameterSpec(TAILLE_TAG_GCM_BITS, iv));
            cipher.updateAAD(enTete);
            byte[] texteChiffre = cipher.doFinal(donneesClaires);

            byte[] resultat = new byte[enTete.length + texteChiffre.length];
            System.arraycopy(enTete, 0, resultat, 0, enTete.length);
            System.arraycopy(texteChiffre, 0, resultat, enTete.length, texteChiffre.length);
            return resultat;
        } finally {
            effacerCle(cle);
        }
    }

    private byte[] dechiffrerDonnees(byte[] contenu, char[] motDePasse)
            throws GeneralSecurityException, IOException {
        int longueurEnTete = EN_TETE_MAGIQUE.length + 1 + TAILLE_SEL + TAILLE_IV;
        if (contenu.length < TAILLE_MIN_FICHIER) {
            throw new IOException("Fichier de sauvegarde trop court ou incomplet.");
        }

        for (int i = 0; i < EN_TETE_MAGIQUE.length; i++) {
            if (contenu[i] != EN_TETE_MAGIQUE[i]) {
                throw new IOException("Le fichier n'est pas une sauvegarde GEA chiffrée valide.");
            }
        }
        if (contenu[EN_TETE_MAGIQUE.length] != VERSION_FORMAT) {
            throw new IOException("Version de format de sauvegarde non prise en charge.");
        }

        int offsetSel = EN_TETE_MAGIQUE.length + 1;
        int offsetIv  = offsetSel + TAILLE_SEL;
        byte[] sel    = Arrays.copyOfRange(contenu, offsetSel, offsetIv);
        byte[] iv     = Arrays.copyOfRange(contenu, offsetIv, longueurEnTete);
        byte[] enTete = Arrays.copyOfRange(contenu, 0, longueurEnTete);
        byte[] chiffre = Arrays.copyOfRange(contenu, longueurEnTete, contenu.length);

        SecretKeySpec cle = deriverCleAES(motDePasse, sel);
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, cle, new GCMParameterSpec(TAILLE_TAG_GCM_BITS, iv));
            cipher.updateAAD(enTete);
            return cipher.doFinal(chiffre);
        } finally {
            effacerCle(cle);
        }
    }

    private byte[] construireEnTete(byte[] sel, byte[] iv) {
        byte[] enTete = new byte[EN_TETE_MAGIQUE.length + 1 + TAILLE_SEL + TAILLE_IV];
        int pos = 0;
        System.arraycopy(EN_TETE_MAGIQUE, 0, enTete, pos, EN_TETE_MAGIQUE.length);
        pos += EN_TETE_MAGIQUE.length;
        enTete[pos++] = VERSION_FORMAT;
        System.arraycopy(sel, 0, enTete, pos, TAILLE_SEL);
        pos += TAILLE_SEL;
        System.arraycopy(iv, 0, enTete, pos, TAILLE_IV);
        return enTete;
    }

    private SecretKeySpec deriverCleAES(char[] motDePasse, byte[] sel)
            throws GeneralSecurityException {
        PBEKeySpec spec = new PBEKeySpec(motDePasse, sel, ITERATIONS_PBKDF2, TAILLE_CLE_BITS);
        byte[] octetsCle = null;
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            SecretKey secret = factory.generateSecret(spec);
            octetsCle = secret.getEncoded();
            return new SecretKeySpec(octetsCle, "AES");
        } finally {
            spec.clearPassword();
            if (octetsCle != null) {
                Arrays.fill(octetsCle, (byte) 0);
            }
        }
    }

    private static void effacerCle(SecretKeySpec cle) {
        if (cle != null) {
            byte[] tmp = cle.getEncoded();
            if (tmp != null) {
                Arrays.fill(tmp, (byte) 0);
            }
        }
    }

    private static ObjectInputFilter creerFiltreDeserialisation() {
        return info -> {
            if (info.depth() > 20
                    || info.references() > 100_000
                    || info.arrayLength() > 100_000) {
                return ObjectInputFilter.Status.REJECTED;
            }
            Class<?> classe = info.serialClass();
            if (classe == null) {
                return ObjectInputFilter.Status.UNDECIDED;
            }
            return CLASSES_AUTORISEES.contains(classe.getName())
                    ? ObjectInputFilter.Status.ALLOWED
                    : ObjectInputFilter.Status.REJECTED;
        };
    }
}
