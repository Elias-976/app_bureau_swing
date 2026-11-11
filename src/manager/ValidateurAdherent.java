package manager;

import modele.Adherent;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Validateur métier pour les données d'un {@link Adherent}.
 * Centralise le contrôle des champs obligatoires, des formats (email, téléphone,
 * code postal, chaînes alphabétiques, valeurs de pratique) et de la date de naissance
 * afin d'être réutilisé par le gestionnaire, le contrôleur et les tests unitaires.
 */
public final class ValidateurAdherent {

    // ── Constantes de validation ───────────────────────────────────────────────

    /** Format strict de date attendu dans les saisies : {@code JJ/MM/AAAA}. */
    public static final DateTimeFormatter FORMAT_DATE =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    /** Date de naissance minimale autorisée (1er janvier 1900). */
    public static final LocalDate DATE_NAISSANCE_MIN = LocalDate.of(1900, 1, 1);

    /** Longueur maximale pour les champs noms, prénoms et ville. */
    public static final int LONGUEUR_MAX_NOM = 100;

    /** Longueur maximale pour le champ adresse. */
    public static final int LONGUEUR_MAX_ADRESSE = 200;

    /** Longueur maximale standard RFC 5321 pour une adresse email. */
    public static final int LONGUEUR_MAX_EMAIL = 254;

    /** Valeurs autorisées pour l'arme pratiquée. */
    public static final Set<String> ARMES_AUTORISEES =
            Collections.unmodifiableSet(new LinkedHashSet<>(List.of("Fleuret", "Épée", "Sabre")));

    /** Valeurs autorisées pour le type de pratique. */
    public static final Set<String> PRATIQUES_AUTORISEES =
            Collections.unmodifiableSet(new LinkedHashSet<>(List.of("Compétition", "Loisir")));

    /** Valeurs autorisées pour la latéralité. */
    public static final Set<String> LATERALITES_AUTORISEES =
            Collections.unmodifiableSet(new LinkedHashSet<>(List.of("Droitier", "Gaucher")));

    /** Expression régulière pour les noms, prénoms et noms de ville (lettres, accents, espaces, tirets, apostrophes). */
    private static final Pattern REGEX_TEXTE_NOM =
            Pattern.compile("^[\\p{L}](?:[\\p{L} '\\u2019-]{0,98}[\\p{L}])?$");

    /** Expression régulière pour les adresses email. */
    private static final Pattern REGEX_EMAIL =
            Pattern.compile("^[\\w.%+-]+@[A-Za-z0-9-]+(?:\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$");

    /** Expression régulière pour les numéros de téléphone français (10 chiffres ou +33). */
    private static final Pattern REGEX_TELEPHONE =
            Pattern.compile("^(?:0[1-9](?:[ .-]?\\d){8}|\\+33[ .-]?(?:\\(0\\)[ .-]?)?[1-9](?:[ .-]?\\d){8})$");

    /** Expression régulière pour les codes postaux français à 5 chiffres. */
    private static final Pattern REGEX_CODE_POSTAL =
            Pattern.compile("^\\d{5}$");

    private ValidateurAdherent() {
        // Classe utilitaire non instanciable
    }

    // ── API publique de validation ─────────────────────────────────────────────

    /**
     * Valide l'ensemble des champs d'un adhérent et lève une exception au premier défaut constaté.
     *
     * @param adherent l'adhérent à valider
     * @throws IllegalArgumentException si l'adhérent est null ou contient au moins un champ invalide
     */
    public static void valider(Adherent adherent) {
        List<String> erreurs = collecterErreurs(adherent);
        if (!erreurs.isEmpty()) {
            throw new IllegalArgumentException(erreurs.get(0));
        }
    }

    /**
     * Collecte l'ensemble des erreurs de validation d'un adhérent.
     *
     * @param adherent l'adhérent à contrôler
     * @return la liste ordonnée des messages d'erreur (vide si tout est valide)
     */
    public static List<String> collecterErreurs(Adherent adherent) {
        List<String> erreurs = new ArrayList<>();
        if (adherent == null) {
            erreurs.add("L'adhérent ne peut pas être null.");
            return erreurs;
        }

        // Identité
        validerChampAlphabetique(adherent.getNom(), "Le nom", true, erreurs);
        validerChampAlphabetique(adherent.getPrenom(), "Le prénom", true, erreurs);
        validerChampAlphabetique(adherent.getNomNaissance(), "Le nom de naissance", false, erreurs);

        boolean dateValide = validerDateNaissanceInterne(adherent.getDateNaissance(), erreurs);

        // Coordonnées
        validerAdresse(adherent.getAdresse(), erreurs);
        validerCodePostal(adherent.getCodePostal(), erreurs);
        validerChampAlphabetique(adherent.getVille(), "La ville", false, erreurs);
        validerTelephone(adherent.getTelephone(), erreurs);
        validerEmail(adherent.getEmail(), erreurs);

        // Pratique sportive
        validerChoix(adherent.getArme(), ARMES_AUTORISEES, "L'arme pratiquée", erreurs);
        validerChoix(adherent.getPratique(), PRATIQUES_AUTORISEES, "Le type de pratique", erreurs);
        validerChoix(adherent.getLateralite(), LATERALITES_AUTORISEES, "La latéralité", erreurs);

        // Responsable légal (obligatoire uniquement si mineur)
        boolean mineur = dateValide && adherent.estMineur();
        validerChampResponsableLegal(adherent.getNomRespLegal(),
                "Le nom du responsable légal", mineur, erreurs);
        validerChampResponsableLegal(adherent.getPrenomRespLegal(),
                "Le prénom du responsable légal", mineur, erreurs);

        return erreurs;
    }

    /**
     * Parse et valide strictement une date de naissance saisie au format {@code JJ/MM/AAAA}.
     * Rejette les dates inexistantes (ex. {@code 31/02/2020}), futures ou antérieures à 1900.
     *
     * @param dateStr chaîne saisie
     * @return la date de naissance valide
     * @throws IllegalArgumentException si la chaîne est vide, mal formée ou hors bornes
     */
    public static LocalDate parserDateNaissance(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            throw new IllegalArgumentException(
                    "La date de naissance est obligatoire (JJ/MM/AAAA).");
        }
        if (contientCaractereControle(dateStr)) {
            throw new IllegalArgumentException(
                    "La date de naissance contient des caractères invalides.");
        }
        LocalDate date;
        try {
            date = LocalDate.parse(dateStr.trim(), FORMAT_DATE);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException(
                    "Date de naissance invalide. Format attendu : JJ/MM/AAAA (date réelle).");
        }
        validerDateNaissance(date);
        return date;
    }

    /**
     * Valide une date de naissance déjà parsée.
     *
     * @param dateNaissance date à vérifier
     * @throws IllegalArgumentException si la date est null, future ou antérieure à 1900
     */
    public static void validerDateNaissance(LocalDate dateNaissance) {
        List<String> erreurs = new ArrayList<>();
        validerDateNaissanceInterne(dateNaissance, erreurs);
        if (!erreurs.isEmpty()) {
            throw new IllegalArgumentException(erreurs.get(0));
        }
    }

    /**
     * Indique si une adresse email respecte le format attendu.
     *
     * @param email adresse à tester
     * @return {@code true} si l'email est non vide et valide
     */
    public static boolean estEmailValide(String email) {
        if (email == null || email.isBlank() || contientCaractereControle(email)) {
            return false;
        }
        String net = email.trim();
        if (net.length() > LONGUEUR_MAX_EMAIL || net.contains("..") || net.startsWith(".")) {
            return false;
        }
        int arobase = net.indexOf('@');
        if (arobase <= 0 || net.charAt(arobase - 1) == '.') {
            return false;
        }
        return REGEX_EMAIL.matcher(net).matches();
    }

    /**
     * Indique si un numéro de téléphone respecte le format français (10 chiffres)
     * ou international ({@code +33...}).
     *
     * @param telephone numéro à tester
     * @return {@code true} si le numéro est non vide et valide
     */
    public static boolean estTelephoneValide(String telephone) {
        if (telephone == null || telephone.isBlank() || contientCaractereControle(telephone)) {
            return false;
        }
        return REGEX_TELEPHONE.matcher(telephone.trim()).matches();
    }

    /**
     * Indique si un code postal respecte le format français à 5 chiffres.
     *
     * @param codePostal code postal à tester
     * @return {@code true} si le code postal est exactement composé de 5 chiffres
     */
    public static boolean estCodePostalValide(String codePostal) {
        if (codePostal == null || codePostal.isBlank() || contientCaractereControle(codePostal)) {
            return false;
        }
        return REGEX_CODE_POSTAL.matcher(codePostal.trim()).matches();
    }

    // ── Méthodes de validation internes ───────────────────────────────────────

    private static boolean validerDateNaissanceInterne(LocalDate date, List<String> erreurs) {
        if (date == null) {
            erreurs.add("La date de naissance est obligatoire.");
            return false;
        }
        if (date.isAfter(LocalDate.now())) {
            erreurs.add("La date de naissance ne peut pas être dans le futur.");
            return false;
        }
        if (date.isBefore(DATE_NAISSANCE_MIN)) {
            erreurs.add("La date de naissance ne peut pas être antérieure au 01/01/1900.");
            return false;
        }
        return true;
    }

    private static void validerChampAlphabetique(String valeur, String libelle,
                                                 boolean obligatoire, List<String> erreurs) {
        if (valeur == null || valeur.isBlank()) {
            if (obligatoire) {
                erreurs.add(libelle + " est obligatoire.");
            }
            return;
        }
        if (contientCaractereControle(valeur)) {
            erreurs.add(libelle + " contient des caractères de contrôle interdits.");
            return;
        }
        String net = valeur.trim();
        if (net.length() > LONGUEUR_MAX_NOM) {
            erreurs.add(libelle + " ne doit pas dépasser " + LONGUEUR_MAX_NOM + " caractères.");
            return;
        }
        if (!REGEX_TEXTE_NOM.matcher(net).matches()) {
            erreurs.add(libelle + " ne doit contenir que des lettres, espaces, tirets ou apostrophes.");
        }
    }

    private static void validerChampResponsableLegal(String valeur, String libelle,
                                                     boolean mineur, List<String> erreurs) {
        if (valeur == null || valeur.isBlank()) {
            if (mineur) {
                erreurs.add(libelle + " est obligatoire (adhérent mineur).");
            }
            return;
        }
        validerChampAlphabetique(valeur, libelle, false, erreurs);
    }

    private static void validerAdresse(String adresse, List<String> erreurs) {
        if (adresse == null || adresse.isBlank()) {
            return;
        }
        if (contientCaractereControle(adresse)) {
            erreurs.add("L'adresse contient des caractères de contrôle interdits.");
            return;
        }
        if (adresse.trim().length() > LONGUEUR_MAX_ADRESSE) {
            erreurs.add("L'adresse ne doit pas dépasser " + LONGUEUR_MAX_ADRESSE + " caractères.");
        }
    }

    private static void validerCodePostal(String codePostal, List<String> erreurs) {
        if (codePostal == null || codePostal.isBlank()) {
            return;
        }
        if (!estCodePostalValide(codePostal)) {
            erreurs.add("Le code postal doit comporter exactement 5 chiffres.");
        }
    }

    private static void validerTelephone(String telephone, List<String> erreurs) {
        if (telephone == null || telephone.isBlank()) {
            erreurs.add("Le numéro de téléphone est obligatoire.");
            return;
        }
        if (!estTelephoneValide(telephone)) {
            erreurs.add("Le numéro de téléphone est invalide (10 chiffres ou +33...).");
        }
    }

    private static void validerEmail(String email, List<String> erreurs) {
        if (email == null || email.isBlank()) {
            erreurs.add("L'adresse email est obligatoire.");
            return;
        }
        if (!estEmailValide(email)) {
            erreurs.add("L'adresse email est invalide (ex. nom@domaine.fr).");
        }
    }

    private static void validerChoix(String valeur, Set<String> autorises,
                                     String libelle, List<String> erreurs) {
        if (valeur == null || !autorises.contains(valeur)) {
            erreurs.add(libelle + " doit faire partie des valeurs autorisées : "
                    + String.join(", ", autorises) + ".");
        }
    }

    private static boolean contientCaractereControle(String texte) {
        for (int i = 0; i < texte.length(); i++) {
            if (Character.isISOControl(texte.charAt(i))) {
                return true;
            }
        }
        return false;
    }
}
