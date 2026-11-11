# Documentation Technique — Application GEA (Cercle d'Escrime de Vandœuvre)

## 1. Présentation générale et périmètre

L'application **GEA (Gestion des Entraînements et des Adhérents)** est un outil bureautique développé en **Java / Swing pur** (sans concepteur de formulaires `.form`) destiné à l'administration des adhérents du **Cercle d'Escrime de Vandœuvre** (contexte M2L — BTS SIO 2SLAM).

### Fonctionnalités couvertes
- **Gestion des adhérents** : ajout, consultation détaillée, recherche multicritère en mémoire, modification et suppression d'adhérents (y compris la gestion conditionnelle du représentant légal pour les mineurs).
- **Contrôles de saisie et validation métier** : vérification stricte des dates, coordonnées, paramètres sportifs et identité du représentant légal avant enregistrement.
- **Catégorisation et tarification automatiques** : chargement du référentiel des 10 catégories depuis `document/categorie.xml` et application automatique de la grille tarifaire Annexe 1.
- **Récapitulatif financier** : calcul du montant total des cotisations et génération du rapport détaillé par catégorie.
- **Sauvegarde et restauration sécurisées** : export binaire chiffré (format `GEA1` — AES-256-GCM / PBKDF2) et import protégé par filtre de désérialisation.
- **Annuaire des clubs de Meurthe-et-Moselle** : chargement depuis `document/club.xml` avec prise en charge de la balise `<site>`, anonymisation RGPD par défaut des coordonnées des bénévoles, impression et export HTML imprimable.

### Périmètre exclu
Conformément au cahier des charges, l'application ne comporte ni interface web ou mobile, ni système d'authentification applicative, ni planification des cours/compétitions, ni gestion de vente ou location de matériel.

---

## 2. Environnement technique et organisation du dépôt

- **Langage et interface** : Java 21 (JDK 21 / OpenJDK 21) et bibliothèque standard `javax.swing` / `java.awt` / `javax.crypto` / `javax.xml.parsers`.
- **Configuration IDE** : [TP-GEA.iml](../TP-GEA.iml) déclare `src/` comme dossier source et `test/` comme dossier de tests.
- **Données de référence** (non modifiables) :
  - [`document/categorie.xml`](categorie.xml) : 10 catégories (`Eveil` / `M5` à `Vétérans`).
  - [`document/club.xml`](club.xml) : 10 clubs meurthe-et-mosellans avec balise `<site>` et coordonnées de bénévoles.
- **Point d'entrée** : [`Main.java`](../src/Main.java) initialise `AdherentManager`, `FenetrePrincipale` et `AdherentControleur` sur le thread de répartition d'événements Swing (`SwingUtilities.invokeLater`).

```text
TP-GEA/
├── document/
│   ├── categorie.xml
│   ├── club.xml
│   ├── documentation_technique.md
│   ├── note_rgpd.md
│   └── GEA.drawio
├── src/
│   ├── Main.java
│   ├── modele/
│   │   ├── Adherent.java
│   │   ├── Categorie.java
│   │   └── Club.java
│   ├── repository/
│   │   ├── CategorieRepository.java
│   │   └── ClubRepository.java
│   ├── manager/
│   │   ├── AdherentManager.java
│   │   ├── ValidateurAdherent.java
│   │   └── SauvegardeManager.java
│   ├── controleur/
│   │   └── AdherentControleur.java
│   └── vue/
│       ├── FenetrePrincipale.java
│       ├── FormulaireAdherentVue.java
│       ├── RechercheAdherentVue.java
│       ├── FicheAdherentVue.java
│       └── AnnuaireClubsVue.java
└── test/
    ├── OutilAssertions.java
    ├── CategorieEtTarifTest.java
    ├── ValidateurAdherentTest.java
    ├── RecapitulatifFinancierTest.java
    ├── SauvegardeEtAnnuaireTest.java
    └── LanceurTests.java
```

---

## 3. Architecture MVC et responsabilités des classes

### 3.1 Couche Modèle (`src/modele/`)

- **`Adherent`** ([`src/modele/Adherent.java`](../src/modele/Adherent.java))
  - Implémente `Serializable` avec `serialVersionUID = 1L`.
  - Porte les 15 attributs d'un membre : `id`, `nom`, `prenom`, `nomNaissance`, `dateNaissance` (`LocalDate`), `adresse`, `codePostal`, `ville`, `telephone`, `email`, `arme`, `pratique`, `lateralite`, `nomRespLegal`, `prenomRespLegal`.
  - Méthodes métier principales : `getAge()`, `estMineur()`, `toString()`.

- **`Categorie`** ([`src/modele/Categorie.java`](../src/modele/Categorie.java))
  - Porte les attributs `code`, `libelle`, `ageMin`, `ageMax`, `anneeMin`, `anneeMax`, `cotisation`.
  - Méthodes de correspondance : `correspondAge(int)`, `correspondAnneeNaissance(int)` et `correspondAdherent(Adherent)`.

- **`Club`** ([`src/modele/Club.java`](../src/modele/Club.java))
  - Porte les attributs `nom`, `ville`, `adresse`, `benevoleNom`, `benevoleTelephone`, `benevoleEmail`, `site` (constructeurs à 7 et 6 arguments).
  - Méthode d'affichage structuré : `afficherCoordonnees()`.

### 3.2 Couche Repository (`src/repository/`)

- **`CategorieRepository`** ([`src/repository/CategorieRepository.java`](../src/repository/CategorieRepository.java))
  - `chargerCategories(String)` : parse `document/categorie.xml` pour construire les 10 objets `Categorie`.
  - `determinerCotisation(String code, String libelle)` : applique la grille tarifaire Annexe 1 :
    - `M5` : 90 €
    - `M7` : 130 €
    - `M9` : 160 €
    - `M11` : 180 €
    - `M13` : 210 €
    - `M15` : 210 €
    - `M17` : 230 €
    - `M20` : 230 €
    - `Séniors` : 255 €
    - `Vétérans` : 255 €

- **`ClubRepository`** ([`src/repository/ClubRepository.java`](../src/repository/ClubRepository.java))
  - `chargerClubs(String)` : lit `document/club.xml` via DOM avec activation de `XMLConstants.FEATURE_SECURE_PROCESSING` (protection contre les entités externes / XXE) et extrait notamment la balise `<site>`.
  - `anonymiserDonnees(List<Club>)` : applique un masquage partiel irréversible en mémoire sur les coordonnées personnelles des bénévoles (exemples de rendu : `M. S*** G.`, `03.**.**.**.**`, `c***@escrime-laxou.net`), sans contournement possible côté utilisateur.

### 3.3 Couche Métier / Manager (`src/manager/`)

- **`AdherentManager`** ([`src/manager/AdherentManager.java`](../src/manager/AdherentManager.java))
  - Gère la collection en mémoire des adhérents (`ajouterAdherent(Adherent)`, `modifierAdherent(Adherent)`, `supprimerAdherent(int)`, `rechercherAdherent(String)`, `getListeAdherents()`, `setListeAdherents(List<Adherent>)`).
  - Détermine la catégorie applicable (`trouverCategorie(Adherent, List<Categorie>)` et `trouverCategorie(int, List<Categorie>)`).
  - Calcule les agrégats financiers : `calculerMontantTotal(List<Categorie>)` et `genererRapportParCategorie(List<Categorie>)`.

- **`ValidateurAdherent`** ([`src/manager/ValidateurAdherent.java`](../src/manager/ValidateurAdherent.java))
  - Centralise la validation stricte des champs obligatoires et formats métier :
    - `FORMAT_DATE` au format `dd/MM/uuuu` en mode `ResolverStyle.STRICT` et borne inférieure `DATE_NAISSANCE_MIN` (`1900-01-01`).
    - Listes contrôlées `ARMES_AUTORISEES`, `PRATIQUES_AUTORISEES`, `LATERALITES_AUTORISEES`.
    - Contrôles dédiés : `estEmailValide(String)`, `estTelephoneValide(String)`, `estCodePostalValide(String)`, `parserDateNaissance(String)`, `validerDateNaissance(LocalDate)`.
    - Règle conditionnelle pour les mineurs (`estMineur()`): exige le nom et le prénom du représentant légal.
    - Expose `collecterErreurs(Adherent)` (liste exhaustive des anomalies) et `valider(Adherent)` (validation bloquante).

- **`SauvegardeManager`** ([`src/manager/SauvegardeManager.java`](../src/manager/SauvegardeManager.java))
  - Assure la persistance binaire chiffrée des adhérents (`exporterDonnees`, `importerDonnees`, `deserialiserListeSecurisee`, `getDernierMessageErreur`). Les garanties cryptographiques et de désérialisation sont détaillées à la section 4.

### 3.4 Couche Vue (`src/vue/`) et Contrôleur (`src/controleur/`)

- **`AdherentControleur`** ([`src/controleur/AdherentControleur.java`](../src/controleur/AdherentControleur.java)) : assure le lien exclusif entre l'interface Swing, les dépôts XML (`CategorieRepository`, `ClubRepository`) et les gestionnaires (`AdherentManager`, `ValidateurAdherent`, `SauvegardeManager`).
- **`FenetrePrincipale`** ([`src/vue/FenetrePrincipale.java`](../src/vue/FenetrePrincipale.java)) : conteneur principal organisant la navigation entre les différentes vues de l'application.
- **`FormulaireAdherentVue`** ([`src/vue/FormulaireAdherentVue.java`](../src/vue/FormulaireAdherentVue.java)) : saisie et modification des informations de l'adhérent avec gestion des champs du représentant légal.
- **`RechercheAdherentVue`** ([`src/vue/RechercheAdherentVue.java`](../src/vue/RechercheAdherentVue.java)) : recherche textuelle, sélection, modification et suppression dans la liste des adhérents.
- **`FicheAdherentVue`** ([`src/vue/FicheAdherentVue.java`](../src/vue/FicheAdherentVue.java)) : affichage détaillé de la fiche d'un adhérent et de sa catégorie/cotisation calculée.
- **`AnnuaireClubsVue`** ([`src/vue/AnnuaireClubsVue.java`](../src/vue/AnnuaireClubsVue.java)) : tableau des clubs déjà anonymisés au chargement, doté de l'impression directe (`imprimerTableau()`, `ajouterEcouteurImprimer`) et de la génération/exportation HTML imprimable (`ajouterEcouteurExporterHtml`, `genererHtmlImprimable(List<Club>)`).

---

## 4. Choix de sérialisation et mesures de sécurité (`SauvegardeManager`)

L'objectif est de permettre la sauvegarde locale et la restauration d'une liste d'adhérents sans exposer le fichier en clair et sans vulnérabilité de désérialisation Java arbitraire.

### 4.1 Structure du format binaire `GEA1`

Chaque fichier exporté suit l'en-tête binaire suivant :
1. **Magique** : `GEA1` (4 octets) identifiant de manière univoque le format chiffré de l'application.
2. **Version** : octet `0x01`.
3. **Sel cryptographique** : 16 octets aléatoires par export (`SecureRandom`).
4. **Vecteur d'initialisation (IV)** : 12 octets aléatoires par export.
5. **Charge utile chiffrée + tag GCM** : flux sérialisé de la liste d'adhérents chiffré en `AES/GCM/NoPadding`.

### 4.2 Dérivation de clé et chiffrement authentifié

- **Dérivation de clé** : `PBKDF2WithHmacSHA256` avec **65 536 itérations** produisant une clé AES de **256 bits**.
- **Chiffrement authentifié** : `AES/GCM/NoPadding` avec tag d'authentification de **128 bits**. L'en-tête (magique, version, sel, IV) est fourni comme *Additional Authenticated Data* (AAD) afin que toute altération d'un seul octet du fichier (en-tête ou données chiffrées) ou toute erreur de mot de passe soit détectée immédiatement avant toute désérialisation.
- **API de sauvegarde/chargement** :
  - Surcharges acceptant un mot de passe explicite : `exporterDonnees(String chemin, List<Adherent> donnees, char[] motDePasse)` et `importerDonnees(String chemin, char[] motDePasse)`.
  - Surcharges avec secret applicatif par défaut : `exporterDonnees(String chemin, List<Adherent> donnees)` et `importerDonnees(String chemin)`.
  - Gestion d'erreurs non destructive : `exporterDonnees(...)` renvoie un `boolean`, `importerDonnees(...)` renvoie `null` en cas d'échec (sans écraser les adhérents déjà présents en mémoire), et `getDernierMessageErreur()` fournit un diagnostic clair à l'interface utilisateur.

### 4.3 Durcissement de la désérialisation (`ObjectInputFilter`)

Après vérification du tag AES-GCM et déchiffrement, la méthode `deserialiserListeSecurisee(byte[])` applique un `ObjectInputFilter` strict par **liste blanche explicite** avant l'instanciation des objets :
- **Types autorisés uniquement** : `ArrayList`, `Object[]`, `Adherent`, `LocalDate`, `java.time.Ser`, `String`.
- **Limites structurelles** : profondeur maximale `20`, nombre maximal de références `100 000`, taille maximale de tableau `100 000`.
- Toute classe hors liste blanche (type inconnu ou *gadget* de désérialisation) est immédiatement rejetée (`REJECTED`).

---

## 5. Stratégie de tests (`test/`)

Afin de garantir une exécution reproductible avec le seul JDK standard (sans dépendance JUnit externe), la suite de tests repose sur l'utilitaire [`test/OutilAssertions.java`](../test/OutilAssertions.java) et un lanceur unifié [`test/LanceurTests.java`](../test/LanceurTests.java).

| Suite de tests | Périmètre fonctionnel | Couverture principale |
| :--- | :--- | :--- |
| [`CategorieEtTarifTest`](../test/CategorieEtTarifTest.java) | US6 (`#50366`) | Chargement des 10 catégories XML, bornes d'âge et d'années, calcul exact des cotisations Annexe 1 (`90 €` à `255 €`). |
| [`ValidateurAdherentTest`](../test/ValidateurAdherentTest.java) | US13 (`#50378`) | Validation des champs obligatoires, dates strictes `dd/MM/uuuu` (dont 29/02 et borne `1900-01-01`), email, téléphone, code postal, armes/pratiques/latéralité, représentant légal pour les mineurs. |
| [`RecapitulatifFinancierTest`](../test/RecapitulatifFinancierTest.java) | US9 (`#50372`) | Agrégation du montant total des cotisations et génération du rapport financier par catégorie. |
| [`SauvegardeEtAnnuaireTest`](../test/SauvegardeEtAnnuaireTest.java) | US10, US11, US7 | Round-trip chiffré `GEA1`, rejet d'un mauvais mot de passe et d'un fichier altéré, filtrage `ObjectInputFilter`, chargement XML des clubs avec `<site>`, anonymisation RGPD et génération HTML imprimable. |

---

## 6. Compilation et exécution (JDK 21)

Les commandes suivantes compilent les sources, exécutent la suite complète de tests (en mode `headless` pour l'initialisation Swing en environnement sans écran) et lancent l'application graphique :

```bash
# 1. Compilation des sources applicatives
mkdir -p out/production/TP-GEA out/test/TP-GEA
javac -encoding UTF-8 -d out/production/TP-GEA src/*.java src/**/*.java

# 2. Compilation et exécution de la suite de tests
javac -encoding UTF-8 -cp out/production/TP-GEA -d out/test/TP-GEA test/*.java
java -Djava.awt.headless=true -cp out/production/TP-GEA:out/test/TP-GEA LanceurTests

# 3. Lancement de l'application Swing
java -cp out/production/TP-GEA Main
```
