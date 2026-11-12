package manager;

/**
 * Exception levée lors d'un échec d'authentification ou de gestion de compte.
 */
public class AuthentificationException extends Exception {

    public AuthentificationException(String message) {
        super(message);
    }

    public AuthentificationException(String message, Throwable cause) {
        super(message, cause);
    }
}

