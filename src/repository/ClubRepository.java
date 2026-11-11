package repository;

import modele.Club;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.swing.JOptionPane;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Dépôt de données pour les clubs d'escrime.
 * Charge les clubs depuis le fichier {@code club.xml} (y compris la balise {@code <site>})
 * et applique l'anonymisation partielle RGPD sur les coordonnées personnelles des bénévoles.
 */
public class ClubRepository {

    private static final Set<String> CIVILITES = Set.of("m.", "mme", "mlle", "m");

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
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new File(cheminFichier));

            NodeList noeuds = doc.getDocumentElement().getChildNodes();
            for (int i = 0; i < noeuds.getLength(); i++) {
                Node noeud = noeuds.item(i);
                if (noeud.getNodeType() != Node.ELEMENT_NODE) continue;

                Element elem = (Element) noeud;

                String nom     = getTexte(elem, "nom");
                String adresse = getTexte(elem, "adresse");
                String ville   = extraireVille(adresse);
                String contact = getTexte(elem, "contact");
                String tel     = getTexte(elem, "tel");
                String mail    = getTexte(elem, "mail");
                String site    = getTexte(elem, "site");

                clubs.add(new Club(nom, ville, adresse, contact, tel, mail, site));
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            if (!GraphicsEnvironment.isHeadless()) {
                JOptionPane.showMessageDialog(
                        null,
                        "Erreur lors du chargement des clubs : " + e.getMessage(),
                        "Erreur XML",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
        return clubs;
    }

    /**
     * Anonymise partiellement les données personnelles des bénévoles dans la liste
     * fournie afin de respecter le RGPD tout en conservant les informations publiques
     * du club (nom, ville, adresse, site web).
     *
     * <p>Exemple de masquage :
     * <ul>
     *   <li>Nom : {@code "M. SILBERSCHMIDT Gilles"} → {@code "M. S*** G."}</li>
     *   <li>Téléphone : {@code "03.83.46.77.22"} → {@code "03.**.**.**.**"}</li>
     *   <li>Email : {@code "escrime.issartier@sfr.fr"} → {@code "e***@sfr.fr"}</li>
     * </ul></p>
     *
     * @param clubs la liste des clubs à anonymiser (modifiée en place)
     */
    public void anonymiserDonnees(List<Club> clubs) {
        if (clubs == null) {
            return;
        }
        for (Club club : clubs) {
            if (club == null) continue;
            club.setBenevoleNom(anonymiserNomBenevole(club.getBenevoleNom()));
            club.setBenevoleTelephone(anonymiserTelephone(club.getBenevoleTelephone()));
            club.setBenevoleEmail(anonymiserEmail(club.getBenevoleEmail()));
        }
    }

    // ── Utilitaires d'anonymisation partielle (RGPD) ──────────────────────────

    private static String anonymiserNomBenevole(String nomComplet) {
        if (nomComplet == null || nomComplet.isBlank()) {
            return "Non renseigné";
        }
        String net = nomComplet.trim();
        if (net.contains("***")) {
            return net;
        }
        String[] mots = net.split("\\s+");
        StringBuilder sb = new StringBuilder();
        int debutIdentite = 0;

        if (mots.length > 1 && CIVILITES.contains(mots[0].toLowerCase(Locale.ROOT))) {
            sb.append(mots[0]);
            debutIdentite = 1;
        }
        for (int i = debutIdentite; i < mots.length; i++) {
            String mot = mots[i];
            if (mot.isEmpty()) continue;
            if (sb.length() > 0) sb.append(' ');
            char initiale = mot.charAt(0);
            if (i == debutIdentite) {
                sb.append(initiale).append("***");
            } else {
                sb.append(initiale).append('.');
            }
        }
        return sb.length() > 0 ? sb.toString() : "*** ANONYMISÉ ***";
    }

    private static String anonymiserTelephone(String telephone) {
        if (telephone == null || telephone.isBlank()) {
            return "Non renseigné";
        }
        String net = telephone.trim();
        if (net.contains("**")) {
            return net;
        }
        String chiffres = net.replaceAll("\\D", "");
        if (chiffres.length() >= 2) {
            return chiffres.substring(0, 2) + ".**.**.**.**";
        }
        return "**.**.**.**.**";
    }

    private static String anonymiserEmail(String email) {
        if (email == null || email.isBlank()) {
            return "Non renseigné";
        }
        String net = email.trim();
        if (net.contains("***")) {
            return net;
        }
        int arobase = net.indexOf('@');
        if (arobase <= 0 || arobase >= net.length() - 1) {
            return "***@***.***";
        }
        String domaine = net.substring(arobase + 1);
        return net.charAt(0) + "***@" + domaine;
    }

    // ── Utilitaires XML privés ─────────────────────────────────────────────────

    /**
     * Extrait le contenu textuel d'une balise XML fille, ou "" si absente/vide.
     */
    private String getTexte(Element parent, String balise) {
        NodeList nl = parent.getElementsByTagName(balise);
        if (nl.getLength() == 0 || nl.item(0) == null) return "";
        String val = nl.item(0).getTextContent();
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
