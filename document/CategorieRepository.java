import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import Modele.Categorie;


public class CategorieRepository {

	static public List<Categorie> getLesCategories(String filename) throws ParserConfigurationException, SAXException, IOException{

		List<Categorie> lesCategories = new ArrayList<Categorie>();
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(new File(filename));

		NodeList nodeList = document.getDocumentElement().getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
		    Node node = nodeList.item(i);
		    if (node.getNodeType() == Node.ELEMENT_NODE) {
		        Element elem = (Element) node;
		        int id = Integer.parseInt(elem.getAttribute("id"));
		        String nom = elem.getElementsByTagName("nom").item(0).getChildNodes().item(0).getNodeValue();

		        // La balise <code> peut être vide (Séniors, Vétérans n'ont pas de code M...)
		        NodeList codeChildren = elem.getElementsByTagName("code").item(0).getChildNodes();
		        String code = (codeChildren.getLength() > 0) ? codeChildren.item(0).getNodeValue() : "";

		        int annee_min = Integer.parseInt(elem.getElementsByTagName("annee_min").item(0).getChildNodes().item(0).getNodeValue());
		        int annee_max = Integer.parseInt(elem.getElementsByTagName("annee_max").item(0).getChildNodes().item(0).getNodeValue());

		        lesCategories.add(new Categorie(id,nom,code,annee_min,annee_max));
		    }
		}
		for (Categorie categ: lesCategories)
		    System.out.println(categ.toString());

		return lesCategories;
	}
}
