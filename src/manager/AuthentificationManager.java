package manager;

import modele.Droit;
import modele.Role;
import modele.Utilisateur;
import repository.UtilisateurRepository;

import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Objects;

/**
 * Gestionnaire métier d'authentification, de contrôle d'accès RBAC
 * et de suivi de session active.
 *
 * <p>Fonctionnalités :
 * <ul>
 *   <li>Vérification cryptographique des identifiants (PBKDF2-HMAC-SHA256)</li>
 *   <li>Protection anti-bruteforce (verrouillage automatique après 5 échecs consécutifs)</li>
 *   <li>Hygiène mémoire stricte (effacement de tous les tableaux de caractères sensibles)</li>
 *   <li>Vérification unifiée des droits et privilèges applicatifs</li>
 * </ul>
 * </p>
 */
public class AuthentificationManager {

    /** Nombre maximal de tentatives infructueuses avant verrouillage du compte. */
    public static final int MAX_TENTATIVES_ECHOUEES = 5;

    /** Longueur minimale requise pour un nouveau mot de passe. */
    public static final int LONGUEUR_MIN_MOT_DE_PASSE = 8;

    private final UtilisateurRepository repository;
    private Utilisateur                 utilisateurConnecte;
    private String                      dernierMessageErreur;

    /**
     * Initialise le gestionnaire avec le dépôt d'utilisateurs par défaut.
     */
    public AuthentificationManager() {
        this(new UtilisateurRepository());
    }

    /**
     * Initialise le gestionnaire avec un dépôt d'utilisateurs spécifique.
     *
     * @param repository le dépôt de comptes
     */
    public AuthentificationManager(UtilisateurRepository repository) {
        this.repository = Objects.requireNonNull(repository, "Le dépôt d'utilisateurs ne peut pas être null");
    }

    // ── Connexion & Déconnexion ────────────────────────────────────────────────

    /**
     * Tente d'authentifier un utilisateur avec son identifiant et son mot de passe.
     * Efface immédiatement le tableau de mot de passe en mémoire après usage.
     *
     * @param identifiant identifiant saisi
     * @param motDePasse  mot de passe saisi sous forme de {@code char[]}
     * @return l'utilisateur connecté en cas de succès
     * @throws AuthentificationException si les identifiants sont invalides ou le compte verrouillé
     */
    public synchronized Utilisateur connecter(String identifiant, char[] motDePasse)
            throws AuthentificationException {
        dernierMessageErreur = null;

        if (identifiant == null || identifiant.isBlank()) {
            dernierMessageErreur = "Veuillez renseigner un identifiant.";
            throw new AuthentificationException(dernierMessageErreur);
        }
        if (motDePasse == null || motDePasse.length == 0) {
            dernierMessageErreur = "Veuillez saisir votre mot de passe.";
            throw new AuthentificationException(dernierMessageErreur);
        }

        Utilisateur u = repository.trouverParIdentifiant(identifiant);

        try {
            if (u == null) {
                dernierMessageErreur = "Identifiant ou mot de passe incorrect.";
                throw new AuthentificationException(dernierMessageErreur);
            }

            if (u.isBloque()) {
                dernierMessageErreur = "Ce compte est verrouillé suite à trop d'échecs. Contactez l'administrateur.";
                throw new AuthentificationException(dernierMessageErreur);
            }

            boolean valide = repository.verifierMotDePasse(motDePasse, u);
            if (!valide) {
                u.enregistrerEchecConnexion(MAX_TENTATIVES_ECHOUEES);
                repository.sauvegarder();

                if (u.isBloque()) {
                    dernierMessageErreur = "Compte verrouillé suite à " + MAX_TENTATIVES_ECHOUEES + " tentatives infructueuses.";
                } else {
                    dernierMessageErreur = String.format(
                            "Identifiant ou mot de passe incorrect (tentative %d/%d).",
                            u.getTentativesEchouees(), MAX_TENTATIVES_ECHOUEES);
                }
                throw new AuthentificationException(dernierMessageErreur);
            }

            u.enregistrerSuccesConnexion();
            repository.sauvegarder();
            this.utilisateurConnecte = u;
            return u;

        } finally {
            Arrays.fill(motDePasse, '\0');
        }
    }

    /**
     * Clôture la session en cours.
     */
    public synchronized void deconnecter() {
        this.utilisateurConnecte  = null;
        this.dernierMessageErreur = null;
    }

    /**
     * Indique si un utilisateur est actuellement authentifié.
     *
     * @return {@code true} si une session est ouverte
     */
    public synchronized boolean estConnecte() {
        return utilisateurConnecte != null;
    }

    /**
     * Retourne l'utilisateur actuellement connecté, ou {@code null}.
     *
     * @return le compte actif
     */
    public synchronized Utilisateur getUtilisateurConnecte() {
        return utilisateurConnecte;
    }

    /**
     * Retourne le rôle de l'utilisateur actif, ou {@code null} si déconnecté.
     *
     * @return le rôle actif
     */
    public synchronized Role getRoleActif() {
        return utilisateurConnecte != null ? utilisateurConnecte.getRole() : null;
    }

    // ── Contrôle d'accès RBAC (Role-Based Access Control) ──────────────────────

    /**
     * Vérifie si l'utilisateur actuellement connecté dispose du droit demandé.
     *
     * @param droit le droit à tester
     * @return {@code true} si l'utilisateur est connecté et possède le droit
     */
    public synchronized boolean aLeDroit(Droit droit) {
        return estConnecte() && utilisateurConnecte.possedeDroit(droit);
    }

    /**
     * Exige qu'un droit spécifique soit possédé par la session active.
     * Lève une {@link SecurityException} explicite si ce n'est pas le cas.
     *
     * @param droit le droit requis
     * @throws SecurityException si l'utilisateur n'est pas connecté ou non habilité
     */
    public synchronized void exigerDroit(Droit droit) {
        Objects.requireNonNull(droit, "Le droit exigé ne peut pas être null");
        if (!estConnecte()) {
            throw new SecurityException("Authentification requise pour effectuer cette opération.");
        }
        if (!utilisateurConnecte.possedeDroit(droit)) {
            String roleLibelle = utilisateurConnecte.getRole().getLibelleCourt();
            throw new SecurityException(String.format(
                    "Accès refusé : le profil « %s » n'est pas autorisé à exécuter cette action (%s).",
                    roleLibelle, droit.getDescription()));
        }
    }

    // ── Changement de mot de passe & administration ────────────────────────────

    /**
     * Permet à l'utilisateur actuellement connecté de modifier son mot de passe.
     *
     * @param ancienMdp    mot de passe actuel
     * @param nouveauMdp   nouveau mot de passe
     * @param confirmation confirmation du mot de passe
     * @throws AuthentificationException si la validation échoue ou si aucun utilisateur n'est connecté
     */
    public synchronized void changerMotDePasse(char[] ancienMdp, char[] nouveauMdp, char[] confirmation)
            throws AuthentificationException {
        if (!estConnecte()) {
            throw new AuthentificationException("Aucun utilisateur n'est actuellement connecté.");
        }
        changerMotDePasse(utilisateurConnecte.getIdentifiant(), ancienMdp, nouveauMdp, confirmation);
    }

    /**
     * Permet à un utilisateur de modifier son mot de passe.
     *
     * @param identifiant  identifiant du compte
     * @param ancienMdp    mot de passe actuel
     * @param nouveauMdp   nouveau mot de passe souhaité
     * @param confirmation confirmation du nouveau mot de passe
     * @throws AuthentificationException si une vérification échoue
     */
    public synchronized void changerMotDePasse(String identifiant,
                                              char[] ancienMdp,
                                              char[] nouveauMdp,
                                              char[] confirmation)
            throws AuthentificationException {
        try {
            if (identifiant == null || identifiant.isBlank()) {
                throw new AuthentificationException("Identifiant obligatoire.");
            }
            if (ancienMdp == null || ancienMdp.length == 0) {
                throw new AuthentificationException("Veuillez saisir votre mot de passe actuel.");
            }
            if (nouveauMdp == null || nouveauMdp.length < LONGUEUR_MIN_MOT_DE_PASSE) {
                throw new AuthentificationException(
                        "Le nouveau mot de passe doit comporter au moins " + LONGUEUR_MIN_MOT_DE_PASSE + " caractères.");
            }
            if (!Arrays.equals(nouveauMdp, confirmation)) {
                throw new AuthentificationException("La confirmation ne correspond pas au nouveau mot de passe.");
            }

            Utilisateur u = repository.trouverParIdentifiant(identifiant);
            if (u == null) {
                throw new AuthentificationException("Compte introuvable.");
            }
            if (!repository.verifierMotDePasse(ancienMdp, u)) {
                throw new AuthentificationException("L'ancien mot de passe est incorrect.");
            }

            byte[] sel  = repository.genererSel();
            byte[] hash = repository.calculerHash(nouveauMdp, sel);
            u.mettreAJourMotDePasse(sel, hash);
            repository.sauvegarder();

        } catch (GeneralSecurityException e) {
            throw new AuthentificationException("Erreur cryptographique lors du changement de mot de passe : " + e.getMessage(), e);
        } finally {
            if (ancienMdp != null) Arrays.fill(ancienMdp, '\0');
            if (nouveauMdp != null) Arrays.fill(nouveauMdp, '\0');
            if (confirmation != null) Arrays.fill(confirmation, '\0');
        }
    }

    /**
     * Déverrouille un compte (action administrative).
     *
     * @param identifiant identifiant du compte à débloquer
     * @return {@code true} si le compte existait et a été débloqué
     */
    public synchronized boolean debloquerCompte(String identifiant) {
        Utilisateur u = repository.trouverParIdentifiant(identifiant);
        if (u != null) {
            u.debloquer();
            repository.sauvegarder();
            return true;
        }
        return false;
    }

    public UtilisateurRepository getRepository() {
        return repository;
    }

    public String getDernierMessageErreur() {
        return dernierMessageErreur;
    }
}
