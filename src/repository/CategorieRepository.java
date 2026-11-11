package repository;

import modele.Categorie;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Dépôt de données pour les catégories d'âge en escrime.
 * Charge les catégories depuis le fichier {@code categorie.xml}.
 *
 * <p>Le XML stocke des intervalles d'<b>années de naissance</b>
 * ({@code annee_min} / {@code annee_max}). Ce dépôt les convertit
 * en <b>intervalles d'âge</b> (ageMin / ageMax) en soustrayant
 * les années de naissance de l'année en cours, afin d'être cohérent
 * avec {@link modele.Adherent#getAge()} et
 * {@link modele.Categorie#correspondAge(int)}.</p>
 */
public class CategorieRepository {

    /**
     * Charge la liste des catégories depuis un fichier XML.
     *
     * @param cheminFichier chemin vers {@code categorie.xml}
     * @return la liste des catégories chargées
     * @throws ParserConfigurationException si la configuration du parser échoue
     * @throws SAXException                 si le XML est malformé
     * @throws IOException                  si le fichier est inaccessible
     */
    public List<Categorie> chargerCategories(String cheminFichier)
            throws ParserConfigurationException, SAXException, IOException {

        List<Categorie> categories = new ArrayList<>();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder        builder = factory.newDocumentBuilder();
        Document               doc     = builder.parse(new File(cheminFichier));

        int anneeActuelle = LocalDate.now().getYear();

        NodeList noeuds = doc.getDocumentElement().getChildNodes();
        for (int i = 0; i < noeuds.getLength(); i++) {
            Node noeud = noeuds.item(i);
            if (noeud.getNodeType() != Node.ELEMENT_NODE) continue;

            Element elem = (Element) noeud;

            // — Libellé (balise <nom>) —
            String libelle = elem.getElementsByTagName("nom")
                    .item(0).getChildNodes().item(0).getNodeValue().trim();

            // — Code (balise <code>, peut être vide pour Séniors/Vétérans) —
            NodeList codeNodes = elem.getElementsByTagName("code").item(0).getChildNodes();
            String code = (codeNodes.getLength() > 0)
                    ? codeNodes.item(0).getNodeValue().trim()
                    : "";

            // — Années de naissance → conversion en âges —
            int anneeMin = Integer.parseInt(
                    elem.getElementsByTagName("annee_min").item(0)
                            .getChildNodes().item(0).getNodeValue().trim());
            int anneeMax = Integer.parseInt(
                    elem.getElementsByTagName("annee_max").item(0)
                            .getChildNodes().item(0).getNodeValue().trim());

            // anneeMax (naissance récente) ↔ ageMin (plus jeune)
            // anneeMin (naissance ancienne) ↔ ageMax (plus vieux)
            int ageMin = anneeActuelle - anneeMax;
            int ageMax = anneeActuelle - anneeMin;

            // Cotisation à 0 par défaut (pas fournie dans le XML — à paramétrer en US9)
            categories.add(new Categorie(code, libelle, ageMin, ageMax, 0.0));
        }

        return categories;
    }
}
