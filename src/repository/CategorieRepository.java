package repository;

import modele.Categorie;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Dépôt de données pour les catégories d'âge en escrime.
 * Charge les catégories depuis le fichier {@code categorie.xml} et leur associe
 * automatiquement les cotisations annuelles de l'Annexe 1.
 *
 * <p>Le XML stocke des intervalles d'<b>années de naissance</b>
 * ({@code annee_min} / {@code annee_max}). Ces bornes sont conservées sur
 * {@link Categorie} pour un classement exact selon l'année de naissance
 * (règle FFE), tout en renseignant les âges indicatifs correspondants.</p>
 */
public class CategorieRepository {

    /** Grille tarifaire Annexe 1 indexée par code de catégorie FFE. */
    private static final Map<String, Double> TARIFS_PAR_CODE = Map.of(
            "M5",  90.0,
            "M7",  130.0,
            "M9",  160.0,
            "M11", 180.0,
            "M13", 210.0,
            "M15", 210.0,
            "M17", 230.0,
            "M20", 230.0
    );

    /** Grille tarifaire Annexe 1 indexée par libellé normalisé (sans accents). */
    private static final Map<String, Double> TARIFS_PAR_LIBELLE = Map.ofEntries(
            Map.entry("eveil",     90.0),
            Map.entry("atomes",    130.0),
            Map.entry("poussins",  160.0),
            Map.entry("pupilles",  180.0),
            Map.entry("benjamins", 210.0),
            Map.entry("minimes",   210.0),
            Map.entry("cadets",    230.0),
            Map.entry("juniors",   230.0),
            Map.entry("seniors",   255.0),
            Map.entry("veterans",  255.0)
    );

    /**
     * Charge la liste des catégories depuis un fichier XML et applique les tarifs Annexe 1.
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
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document        doc     = builder.parse(new File(cheminFichier));

        int anneeActuelle = LocalDate.now().getYear();

        NodeList noeuds = doc.getDocumentElement().getChildNodes();
        for (int i = 0; i < noeuds.getLength(); i++) {
            Node noeud = noeuds.item(i);
            if (noeud.getNodeType() != Node.ELEMENT_NODE) continue;

            Element elem = (Element) noeud;

            String libelle = extraireTexte(elem, "nom");
            String code    = extraireTexte(elem, "code");

            int anneeMin = Integer.parseInt(extraireTexte(elem, "annee_min"));
            int anneeMax = Integer.parseInt(extraireTexte(elem, "annee_max"));

            // Âges indicatifs pour compatibilité avec les vues/méthodes par âge
            int ageMin = Math.max(0, anneeActuelle - anneeMax);
            int ageMax = Math.max(0, anneeActuelle - anneeMin);

            double cotisation = determinerCotisation(code, libelle);

            categories.add(new Categorie(code, libelle, ageMin, ageMax,
                    anneeMin, anneeMax, cotisation));
        }

        return categories;
    }

    /**
     * Détermine le tarif de cotisation annuel (Annexe 1) à partir du code FFE
     * ou du libellé de la catégorie.
     *
     * @param code    le code de la catégorie (ex. {@code "M9"}, ou vide pour Séniors/Vétérans)
     * @param libelle le libellé de la catégorie (ex. {@code "Poussins"}, {@code "Séniors"})
     * @return le montant de la cotisation en euros, ou {@code 0.0} si inconnu
     */
    public static double determinerCotisation(String code, String libelle) {
        if (code != null && !code.isBlank()) {
            Double tarifParCode = TARIFS_PAR_CODE.get(code.trim().toUpperCase(Locale.ROOT));
            if (tarifParCode != null) {
                return tarifParCode;
            }
        }
        if (libelle != null && !libelle.isBlank()) {
            Double tarifParLibelle = TARIFS_PAR_LIBELLE.get(normaliserTexte(libelle));
            if (tarifParLibelle != null) {
                return tarifParLibelle;
            }
        }
        return 0.0;
    }

    private static String extraireTexte(Element parent, String balise) {
        NodeList liste = parent.getElementsByTagName(balise);
        if (liste.getLength() == 0 || liste.item(0) == null) {
            return "";
        }
        String texte = liste.item(0).getTextContent();
        return texte != null ? texte.trim() : "";
    }

    private static String normaliserTexte(String texte) {
        return Normalizer.normalize(texte.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
    }
}
