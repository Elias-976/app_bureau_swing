package repository;

import modele.Club;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Dépôt de données pour les clubs d'escrime.
 * Charge les clubs depuis le fichier {@code club.xml} et gère l'anonymisation RGPD.
 */
public class ClubRepository {

    /**
     * Charge la liste des clubs depuis un fichier XML.
     *
     * <p>Structure XML attendue :
     * <pre>{@code
     * <clubs>
     *   <club id="1">
     *     <nom>...</nom>
     *     <adresse>...</adresse>
     *     <contact>...</contact>
     *     <tel>...</tel>
     *     <mail>...</mail>
     *     <site>...</site>   <!-- facultatif -->
     *   </club>
     * </clubs>
     * }</pre></p>
     *
     * @param cheminFichier chemin absolu ou relatif vers le fichier club.xml
     * @return la liste des clubs chargés (vide en cas d'erreur)
     */
    public List<Club> chargerClubs(String cheminFichier) {
        List<Club> clubs = new ArrayList<>();
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new File(cheminFichier));

            NodeList noeuds = doc.getDocumentElement().getChildNodes();
            for (int i = 0; i < noeuds.getLength(); i++) {
                Node noeud = noeuds.item(i);
                if (noeud.getNodeType() != Node.ELEMENT_NODE) continue;

                Element elem = (Element) noeud;

                String nom     = getTexte(elem, "nom");
                String adresse = getTexte(elem, "adresse");
                // La balise <adresse> contient souvent la ville (ex: "2 Grande rue 54210 AZELOT")
                // On extrait la ville depuis l'adresse (dernier mot en majuscules)
                String ville   = extraireVille(adresse);

                String contact = getTexte(elem, "contact");
                String tel     = getTexte(elem, "tel");
                String mail    = getTexte(elem, "mail");

                clubs.add(new Club(nom, ville, adresse, contact, tel, mail));
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            javax.swing.JOptionPane.showMessageDialog(
                    null,
                    "Erreur lors du chargement des clubs : " + e.getMessage(),
                    "Erreur XML",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        }
        return clubs;
    }

    /**
     * Anonymise les données personnelles des bénévoles dans la liste fournie
     * afin de respecter le RGPD (masquage du nom, téléphone et email).
     *
     * @param clubs la liste des clubs à anonymiser (modifiée en place)
     */
    public void anonymiserDonnees(List<Club> clubs) {
        for (Club club : clubs) {
            club.setBenevoleNom("*** ANONYMISÉ ***");
            club.setBenevoleTelephone("**.**.**.**.**");
            club.setBenevoleEmail("***@***.***");
        }
    }

    // ── Utilitaires privés ─────────────────────────────────────────────────────

    /**
     * Extrait le contenu textuel d'une balise XML fille, ou "" si absente/vide.
     */
    private String getTexte(Element parent, String balise) {
        NodeList nl = parent.getElementsByTagName(balise);
        if (nl.getLength() == 0) return "";
        NodeList enfants = nl.item(0).getChildNodes();
        if (enfants.getLength() == 0) return "";
        String val = enfants.item(0).getNodeValue();
        return (val == null) ? "" : val.trim();
    }

    /**
     * Tente d'extraire la ville depuis la chaîne d'adresse complète.
     * Exemple : "2 Grande rue 54210 AZELOT" → "AZELOT".
     * Si l'extraction échoue, retourne l'adresse brute.
     */
    private String extraireVille(String adresse) {
        if (adresse == null || adresse.isBlank()) return "";
        String[] parts = adresse.trim().split("\\s+");
        if (parts.length == 0) return adresse;
        return parts[parts.length - 1];
    }
}

