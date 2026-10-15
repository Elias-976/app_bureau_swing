package Repository;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import Modele.Categorie;

public class CategorieRepository {

    // Grille tarifaire
    // categorie.xml ne contient pas les tarifs donc on les associe par id de catégorie

    private static HashMap<Integer, Double> tarifs = creerTarifs();

    private static HashMap<Integer, Double> creerTarifs() {
        HashMap<Integer, Double> t = new HashMap<Integer, Double>();
        t.put(1, 90.0); // Eveil
        t.put(2, 130.0); // Atomes
        t.put(3, 160.0); // Poussins
        t.put(4, 180.0); // Pupilles
        t.put(5, 210.0); // Benjamins
        t.put(6, 210.0); // Minimes
        t.put(7, 230.0); // Cadets
        t.put(8, 230.0); // Juniors
        t.put(9, 255.0); // Séniors
        t.put(10, 255.0); // Vétérans
        return t;
    }

    // Lit le fichier XML et retourne la liste de toutes les catégories, chacune complétée avec son tarif annuel
    public static List<Categorie> getLesCategories(String filename)
            throws ParserConfigurationException, SAXException, IOException {

        List<Categorie> lesCategories = new ArrayList<Categorie>();

        // Mise en place du lecteur XML (DOM)
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new File(filename));

        // On parcourt tous les nœuds enfants de la racine <categories>
        NodeList nodeList = document.getDocumentElement().getChildNodes();

        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);

            // On ignore les nœuds texte (espaces, retours à la ligne)
            // et on traite uniquement les balises <categorie>
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element elem = (Element) node;

                int id = Integer.parseInt(elem.getAttribute("id"));

                String nom = elem.getElementsByTagName("nom")
                                 .item(0).getChildNodes().item(0).getNodeValue();

                // La balise <code> est vide pour Séniors et Vétérans
                NodeList codeChildren = elem.getElementsByTagName("code")
                                            .item(0).getChildNodes();
                String code = (codeChildren.getLength() > 0)
                              ? codeChildren.item(0).getNodeValue()
                              : "";

                int anneeMin = Integer.parseInt(
                    elem.getElementsByTagName("annee_min")
                        .item(0).getChildNodes().item(0).getNodeValue());

                int anneeMax = Integer.parseInt(
                    elem.getElementsByTagName("annee_max")
                        .item(0).getChildNodes().item(0).getNodeValue());

                // Récupération du tarif depuis la grille
                double tarif = 0.0;
                if (tarifs.containsKey(id)) {
                    tarif = tarifs.get(id);
                }

                lesCategories.add(new Categorie(id, nom, code, anneeMin, anneeMax, tarif));
            }
        }

        return lesCategories;
    }

    // Recherche, dans une liste de catégories, celle qui correspond à une année de naissance donnée
    // Retourne null si aucune catégorie ne correspond

    public static Categorie trouverCategorie(int anneeNaissance, List<Categorie> lesCategories) {
        for (Categorie categorie : lesCategories) {
            if (categorie.correspond(anneeNaissance)) {
                return categorie;
            }
        }
        return null;
    }
}