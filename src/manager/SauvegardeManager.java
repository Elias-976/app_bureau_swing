package manager;

import modele.Adherent;

import javax.swing.JOptionPane;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Gestionnaire de sauvegarde et de restauration des données adhérents.
 * Exporte et importe la liste des adhérents vers/depuis un fichier sérialisé Java.
 *
 * <p>La sérialisation Java natif chiffre les objets en binaire ; les données
 * ne sont donc pas stockées en clair dans le fichier de sauvegarde.</p>
 */
public class SauvegardeManager {

    /**
     * Exporte la liste des adhérents vers un fichier sérialisé.
     * Les données ne sont pas stockées en clair (sérialisation binaire Java).
     *
     * @param chemin  chemin du fichier de destination
     * @param donnees la liste des adhérents à exporter
     */
    public void exporterDonnees(String chemin, List<Adherent> donnees) {
        try (ObjectOutputStream oos =
                     new ObjectOutputStream(new FileOutputStream(chemin))) {
            oos.writeObject(new ArrayList<>(donnees));
        } catch (IOException e) {
            JOptionPane.showMessageDialog(
                    null,
                    "Erreur lors de l'exportation des données :\n" + e.getMessage(),
                    "Erreur de sauvegarde",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Importe la liste des adhérents depuis un fichier sérialisé.
     * Affiche un popup en cas d'erreur de désérialisation.
     *
     * @param chemin chemin du fichier source
     * @return la liste des adhérents restaurée, ou liste vide en cas d'erreur
     */
    @SuppressWarnings("unchecked")
    public List<Adherent> importerDonnees(String chemin) {
        try (ObjectInputStream ois =
                     new ObjectInputStream(new FileInputStream(chemin))) {
            return (List<Adherent>) ois.readObject();
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(
                    null,
                    "Fichier introuvable : " + chemin,
                    "Erreur d'importation",
                    JOptionPane.ERROR_MESSAGE);
        } catch (IOException | ClassNotFoundException e) {
            JOptionPane.showMessageDialog(
                    null,
                    "Erreur lors de l'importation des données :\n" + e.getMessage()
                            + "\nLe fichier est peut-être corrompu ou incompatible.",
                    "Erreur d'importation",
                    JOptionPane.ERROR_MESSAGE);
        }
        return new ArrayList<>();
    }
}

