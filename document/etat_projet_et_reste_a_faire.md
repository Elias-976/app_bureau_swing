# État du projet TP-GEA & Reste à faire (Guide par blocs pour Claude)

> **Contexte :** Application de bureau Java Swing (**GEA — Gestion des Adhérents**) pour le *Cercle d'Escrime de Vandœuvre* (Maison des Ligues de Lorraine — BTS SIO 2SLAM, Mission 1).
> **Objectif de ce document :** Dresser l'inventaire exact de l'existant dans le dépôt et regrouper tout ce qui reste à faire **par blocs thématiques cohérents**, afin de pouvoir demander à Claude d'implémenter et de corriger les fonctionnalités bloc par bloc sans casser l'architecture en place.

---

## 0. Contraintes & règles d'exécution pour Claude

1. **Architecture réelle à respecter :** MVC en **Java Swing pur (`.java` uniquement — pas de fichiers `.form`)**, organisée dans `src/` (`Main`, `modele`, `repository`, `manager`, `controleur`, `vue`).
2. **Langue :** Code, Javadoc, commentaires, interfaces et documentation obligatoirement en **français**.
3. **Périmètre strict (hors périmètre interdit) :** Application de bureau uniquement. Ne pas ajouter de client web/mobile, d'authentification, de planning d'entraînements/compétitions ni de gestion d'achats/locations.
4. **Données sources XML :** Conserver l'usage de `document/categorie.xml` et `document/club.xml` (ne pas modifier les fichiers XML fournis).
5. **Méthode de travail :** Traiter **un bloc à la fois**, vérifier la compilation (`javac`) et l'exécution des tests après chaque bloc, puis effectuer un `git commit` atomique (ex. `feat(US6-US3): tarifs Annexe 1, calcul par annee et affichage fiche`).

---

## PARTIE 1 — État de l'existant (Ce qui est déjà implémenté)

Le projet compile actuellement sans erreur (JDK 21 / Temurin 25). Toutefois, **seules les US0, US1 et US2 sont commitées** sur la branche `Elias` (`HEAD = b746791`). Les implémentations des US3 à US11 sont présentes **en modifications non commitées** dans le répertoire de travail.

### 1.1 Socle MVC & Modèle métier (`src/Main.java`, `src/modele/`, `src/vue/FenetrePrincipale.java`)
- **`src/Main.java`** : Lance l'application dans l'EDT (`SwingUtilities.invokeLater`), instancie `AdherentManager`, `FenetrePrincipale` et `AdherentControleur`.
- **`src/modele/Adherent.java`** : Implémente `Serializable` (`serialVersionUID = 1L`). Contient les 14 champs de l'Annexe 2 (`id`, `nom`, `prenom`, `nomNaissance`, `dateNaissance`, `adresse`, `codePostal`, `ville`, `telephone`, `email`, `arme`, `pratique`, `lateralite`, `nomRespLegal`, `prenomRespLegal`) + méthodes `getAge()` et `estMineur()`.
- **`src/modele/Categorie.java`** : Champs `code`, `libelle`, `ageMin`, `ageMax`, `cotisation` + méthode `correspondAge(int age)`.
- **`src/modele/Club.java`** : Champs `nom`, `ville`, `adresse`, `benevoleNom`, `benevoleTelephone`, `benevoleEmail` + méthode `afficherCoordonnees()`.
- **`src/vue/FenetrePrincipale.java`** : `JFrame` principale avec barre de menus (*Adhérents*, *Clubs*, *Finances*, *Sauvegarde*) et 4 boutons d'accès rapide (*Nouvel adhérent*, *Rechercher*, *Annuaire clubs*, *Finances*).

### 1.2 Gestion des adhérents — CRUD (`AdherentManager`, `AdherentControleur`, Vues adhérents)
- **US1 (Création)** — `FormulaireAdherentVue` + `AdherentControleur.ouvrirFormulaireAjout()` / `validerAjout()` + `AdherentManager.ajouterAdherent()` :
  - Saisie de tous les champs de l'Annexe 2, auto-incrémentation de l'`id` via `AtomicInteger`.
  - Contrôle basique : `nom`, `prenom`, `dateNaissance` non vides, format `dd/MM/yyyy`, et `nomRespLegal`/`prenomRespLegal` obligatoires si `estMineur()`.
- **US2 (Recherche)** — `RechercheAdherentVue` + `AdherentManager.rechercherAdherent(String critere)` :
  - Recherche insensible à la casse sur `nom`, `prenom`, `nomNaissance`, `email`.
  - Affichage dans un `JTable` triable (colonne `ID` masquée), double-clic ouvrant la fiche.
- **US3 (Consultation — partiel)** — `FicheAdherentVue` :
  - Affiche en lecture seule l'identité, l'âge, le statut mineur/majeur, la catégorie (libellé + code), les coordonnées, la pratique sportive et le responsable légal (si mineur), avec bouton *Modifier*.
  - *Manque encore :* l'affichage du tarif/cotisation (`#50358`).
- **US4 (Modification — partiel)** — `AdherentControleur.ouvrirFormulaireModification()` / `validerModification()` + `AdherentManager.modifierAdherent()` :
  - Pré-remplit le formulaire, conserve l'`id` d'origine, met à jour la liste et rafraîchit `RechercheAdherentVue`.
  - *Manque encore :* validation complète des champs et formats (`#50360`).
- **US5 (Suppression — complet)** — `AdherentControleur` + `AdherentManager.supprimerAdherent(int id)` :
  - Demande confirmation via `JOptionPane.YES_NO_OPTION` avant suppression et rafraîchit le tableau.

### 1.3 Catégories & Récapitulatif financier (`CategorieRepository`, `AdherentManager`, `AdherentControleur`)
- **US6 (Partiel)** — `CategorieRepository.chargerCategories("document/categorie.xml")` :
  - Lit les 10 catégories XML (`Eveil` à `Vétérans`, gère les balises `<code>` vides).
  - *Problèmes existants :* met `cotisation = 0.0` partout (Annexe 1 non implémentée), convertit `annee_min`/`annee_max` en âges via `LocalDate.now().getYear()` (bug de décalage avec `getAge()`), aucun test unitaire.
- **US9 (Partiel)** — `AdherentManager.calculerMontantTotal()`, `genererRapportParCategorie()` et `AdherentControleur.ouvrirRecapitulatifFinancier()` :
  - Génère le récapitulatif détaillé par catégorie + total général dans une `JDialog`.
  - *Problème existant :* comme toutes les cotisations valent `0.0`, le contrôleur demande à l'utilisateur de saisir 10 montants manuellement via `JOptionPane` à chaque ouverture. Aucun test d'acceptation.

### 1.4 Annuaire des clubs (`ClubRepository`, `AdherentControleur`)
- **US7 (Partiel)** — `ClubRepository.chargerClubs("document/club.xml")` + `anonymiserDonnees(List<Club>)` + `AdherentControleur.ouvrirAnnuaireClubs()` :
  - Charge les 10 clubs, extrait la ville depuis l'adresse, propose l'anonymisation via popup Oui/Non, et affiche les clubs dans une `JTable` non éditable avec tri.
  - *Manque encore :* bouton d'impression (US8), note de justification RGPD (`#50379`).

### 1.5 Sauvegarde & Restauration (`SauvegardeManager`, `AdherentControleur`)
- **US10 / US11 (Partiels — faille sécurité)** — `SauvegardeManager.exporterDonnees()` / `importerDonnees()` + `AdherentControleur.exporterDonnees()` / `importerDonnees()` :
  - Sérialise `List<Adherent>` dans un fichier `.gea` via `ObjectOutputStream` / `ObjectInputStream`.
  - *Problèmes existants :* **les données personnelles sont lisibles en clair** dans le fichier `.gea` (l'`ObjectOutputStream` ne chiffre pas) ; gestion incomplète des erreurs à la désérialisation (`ClassCastException`, `null`, absence de filtre de désérialisation) ; faux message de succès en cas d'erreur d'export.

### 1.6 Documentation & Git existants
- `document/GEA.drawio` : diagramme de classes présent (mais certaines signatures divergent du code).
- État Git : fichiers modifiés/non suivis dans le working tree (`AdherentControleur.java`, `AdherentManager.java`, `SauvegardeManager.java`, `Adherent.java`, `CategorieRepository.java`, `ClubRepository.java`, `FenetrePrincipale.java`, `FormulaireAdherentVue.java`, `FicheAdherentVue.java`, `RechercheAdherentVue.java`), fichier doublon `.gitignore copy` présent à la racine, `document/prompt_pour_claude.md` marqué supprimé (`D`).

---

## PARTIE 2 — Ce qui reste à faire (Classé par blocs de ressemblance)

---

### BLOC 1 — Catégories d'âge, Tarifs Annexe 1, Fiche adhérent & Récapitulatif financier
**User Stories regroupées :** `US6` (`#50363`, `#50364`, `#50365`) · `US3` (`#50358`) · `US9` (`#50370`, `#50371`)
**Fichiers concernés :** `src/modele/Categorie.java`, `src/repository/CategorieRepository.java`, `src/manager/AdherentManager.java`, `src/vue/FicheAdherentVue.java`, `src/controleur/AdherentControleur.java`

> *Pourquoi ce regroupement :* US6 (calcul catégorie + tarifs Annexe 1), US3 (affichage catégorie/tarif) et US9 (récapitulatif financier) partagent exactement la même logique métier et les mêmes objets `Categorie`.

#### Travail à réaliser :
1. **Associer la grille tarifaire de l'Annexe 1 (US6 `#50365`)** dans `CategorieRepository` (via une table/correspondance par code ou libellé normalisé) lors du chargement de `document/categorie.xml` :

   | Catégorie (`nom`) | Code | Années XML (`annee_min` – `annee_max`) | Cotisation annuelle |
   | :--- | :--- | :--- | :---: |
   | Eveil | `M5` | 2022 – 2026 | **90 €** |
   | Atomes | `M7` | 2020 – 2021 | **130 €** |
   | Poussins | `M9` | 2018 – 2019 | **160 €** |
   | Pupilles | `M11` | 2016 – 2017 | **180 €** |
   | Benjamins | `M13` | 2014 – 2015 | **210 €** |
   | Minimes | `M15` | 2012 – 2013 | **210 €** |
   | Cadets | `M17` | 2010 – 2011 | **230 €** |
   | Juniors | `M20` | 2007 – 2009 | **230 €** |
   | Séniors | *(vide)* | 1988 – 2006 | **255 €** |
   | Vétérans | *(vide)* | 1930 – 1987 | **255 €** |

2. **Corriger le bug de calcul de catégorie (US6 `#50363` / `#50364`) :**
   - *Bug actuel :* `categorie.xml` raisonne en **années de naissance** (`2018–2019` pour Poussins), mais `CategorieRepository` convertit en `ageMin`/`ageMax` avec `LocalDate.now().getYear()`, puis `AdherentManager.trouverCategorie()` compare avec `Adherent.getAge()` (âge exact au jour près). Résultat : un adhérent né le `15/12/2019` a `6 ans` en novembre 2026 et tombe à tort en `Atomes (M7)` au lieu de `Poussins (M9)` ; en 2027, un enfant né en 2026 (`0 an`) n'aura plus aucune catégorie.
   - *Correction attendue :* Conserver `anneeMin` et `anneeMax` sur `Categorie` (ou ajouter `correspondAnneeNaissance(int annee)` / `trouverCategorie(Adherent, List<Categorie>)` basé sur `adherent.getDateNaissance().getYear()`, tout en gardant `correspondAge(int)` si nécessaire pour compatibilité).
3. **Afficher le tarif dans la fiche adhérent (US3 `#50358`) :**
   - Dans `FicheAdherentVue`, ajouter un champ/label **« Cotisation annuelle : »** affichant `cat.getCotisation()` formaté en euros (ex. `180,00 €`), ou `"Non déterminée"` si aucune catégorie ne correspond.
4. **Simplifier et fiabiliser le récapitulatif financier (US9 `#50370` / `#50371`) :**
   - Supprimer l'obligation de saisir les 10 tarifs manuellement à chaque ouverture de `ouvrirRecapitulatifFinancier()` puisque les tarifs Annexe 1 sont maintenant chargés automatiquement.
   - Optionnellement, conserver un bouton ou menu séparé pour ajuster les tarifs en mémoire si besoin, mais s'assurer que par défaut les calculs utilisent les tarifs Annexe 1 sans interruption.
   - Vérifier que `genererRapportParCategorie` et `calculerMontantTotal` utilisent la nouvelle méthode de correspondance par adhérent/année de naissance.

---

### BLOC 2 — Validation stricte des saisies, Formulaire & Recherche
**User Stories regroupées :** `US13` (`#50377`) · `US4` (`#50360`) · ergonomie `US1` / `US2`
**Fichiers concernés :** nouveau `src/manager/ValidateurAdherent.java` (ou méthode dédiée dans `manager`/`modele`), `src/manager/AdherentManager.java`, `src/controleur/AdherentControleur.java`, `src/vue/FormulaireAdherentVue.java`, `src/vue/RechercheAdherentVue.java`

> *Pourquoi ce regroupement :* US1, US4 et US13 partagent la même logique de validation des formulaires (ajout et modification) et la même séparation métier nécessaire pour pouvoir tester unitairement en US13.

#### Travail à réaliser :
1. **Créer un validateur métier testable unitairement (US13 `#50377`, US4 `#50360`) :**
   - **Champs obligatoires cohérents avec les `*` du formulaire :** `nom`, `prenom`, `dateNaissance`, `telephone`, `email` (+ `nomRespLegal` et `prenomRespLegal` si mineur).
   - **Formats et regex :**
     - `email` : format email valide (ex. `^[\\w.%+-]+@[\\w.-]+\\.[A-Za-z]{2,}$`).
     - `telephone` : format téléphone valide (ex. numéro français à 10 chiffres, avec espaces/points/tirets tolérés, ou format international `+33...`).
     - `codePostal` : si renseigné (ou obligatoire selon choix), 5 chiffres (`^\\d{5}$`).
     - `nom`, `prenom`, `nomNaissance`, `ville`, `nomRespLegal`, `prenomRespLegal` : longueur raisonnable, caractères alphabétiques/accents/espaces/tirets/apostrophes valides (pas de chiffres ni caractères de contrôle).
     - `arme`, `pratique`, `lateralite` : vérifier l'appartenance aux valeurs autorisées (`Fleuret`/`Épée`/`Sabre`, `Compétition`/`Loisir`, `Droitier`/`Gaucher`).
   - **Date de naissance stricte :**
     - Utiliser `DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT)` pour rejeter les dates inexistantes (ex. `31/02/2020`).
     - Refuser toute date dans le futur (`dateNaissance.isAfter(LocalDate.now())`) ou antérieure à `1900`.
2. **Sécuriser la couche `AdherentManager` et dédupliquer `AdherentControleur` :**
   - Appliquer cette validation dans `AdherentManager.ajouterAdherent()` **et** `AdherentManager.modifierAdherent()`.
   - Factoriser la logique commune entre `validerAjout` et `validerModification` dans `AdherentControleur`.
3. **Corriger l'ergonomie des vues `FormulaireAdherentVue` et `RechercheAdherentVue` :**
   - Dans `FormulaireAdherentVue` : mettre à jour dynamiquement la visibilité du panneau `panneauRespLegal` dès que la date de naissance saisie change (et le masquer en modification si un adhérent devient majeur). Placer `labelErreur` au-dessus de la barre de boutons (`BorderLayout.NORTH` du panneau sud) pour ne pas tronquer les messages d'erreur.
   - Dans `RechercheAdherentVue` : lorsque l'utilisateur clique sur `✖ Effacer`, réafficher immédiatement tous les adhérents dans le tableau (brancher l'action depuis le contrôleur ou déclencher `btnRechercher`).

---

### BLOC 3 — Sécurité des sauvegardes : Chiffrement Export & Désérialisation robuste
**User Stories regroupées :** `US10` (`#50373`, `#50374`) · `US11` (`#50375`, `#50376`)
**Fichiers concernés :** `src/manager/SauvegardeManager.java`, `src/controleur/AdherentControleur.java`

> *Pourquoi ce regroupement :* L'export (US10) et la réimportation (US11) forment un couple symétrique qui doit partager le même format chiffré et les mêmes contrôles de sécurité.

#### Travail à réaliser :
1. **Chiffrer réellement les fichiers exportés `.gea` (US10 `#50373` / `#50374`) :**
   - Remplacer la sérialisation directe en clair (`new ObjectOutputStream(new FileOutputStream(chemin))`) par une écriture **chiffrée** (ex. `AES/GCM/NoPadding` avec `javax.crypto.Cipher`, sel/IV aléatoire stocké en en-tête du fichier, ou clé dérivée par PBKDF2 d'un mot de passe demandé à l'utilisateur ou clé applicative dédiée).
   - **Exigence impérative :** aucune donnée personnelle (nom, prénom, adresse, téléphone, email, date) ne doit apparaître en clair dans le fichier `.gea` (vérifiable avec la commande `strings` ou un éditeur de texte).
2. **Renforcer la gestion des erreurs de désérialisation (US11 `#50375` / `#50376`) :**
   - Déchiffrer le flux avant de passer à `ObjectInputStream` et intercepter proprement `GeneralSecurityException` (fichier altéré, mauvais mot de passe ou mauvais format).
   - Configurer un `ObjectInputFilter` sur l'`ObjectInputStream` n'autorisant que les classes attendues (`java.util.ArrayList`, `modele.Adherent`, `java.time.LocalDate`, `java.time.Ser`, `java.lang.String`, etc.) afin de bloquer toute désérialisation de classes arbitraires.
   - Capturer `ClassCastException`, vérifier que l'objet lu n'est pas `null`, et contrôler que c'est une `List<?>` dont chaque élément est bien `instanceof Adherent`.
3. **Corriger le retour d'état vers `AdherentControleur` :**
   - Modifier `exporterDonnees` pour retourner un `boolean` (ou lever une exception capturée par le contrôleur) afin que le popup *« Exportation réussie »* ne s'affiche **jamais** si l'écriture a échoué.
   - Modifier `importerDonnees` pour distinguer une erreur d'import (annulation/exception) d'une sauvegarde valide contenant `0` adhérent.

---

### BLOC 4 — Annuaire des clubs : Impression & Cohérence RGPD
**User Stories regroupées :** `US8` (`#50369`) · `US7` (`#50367`, `#50368`)
**Fichiers concernés :** `src/modele/Club.java`, `src/repository/ClubRepository.java`, `src/controleur/AdherentControleur.java` (et éventuellement une classe `vue/AnnuaireClubsVue.java` si souhaité)

> *Pourquoi ce regroupement :* US7 et US8 portent sur la même fenêtre d'annuaire des clubs chargée depuis `document/club.xml`.

#### Travail à réaliser :
1. **Implémenter l'impression de l'annuaire (US8 `#50369`) :**
   - Ajouter un bouton **« 🖨 Imprimer »** dans la boîte de dialogue de l'annuaire des clubs (`ouvrirAnnuaireClubs()`).
   - Utiliser `table.print(JTable.PrintMode.FIT_WIDTH, headerFormat, footerFormat)` avec gestion propre de `PrinterException` (message d'erreur utilisateur si aucune imprimante / erreur système, et confirmation ou silence en cas d'annulation utilisateur).
   - Optionnel : ajouter un bouton d'export texte/HTML imprimable en complément si utile pour la démonstration sans imprimante physique.
2. **Mettre l'annuaire en cohérence avec le choix RGPD (US7) :**
   - Actuellement, `ouvrirAnnuaireClubs()` pose la question *« Afficher les données anonymisées (RGPD) ? »* — ce qui laisse tout utilisateur voir les coordonnées réelles des bénévoles en cliquant sur « Non ».
   - Adapter ce comportement au choix retenu par l'équipe dans la note RGPD (ex. anonymisation partielle par défaut ou masquage des données personnelles sensibles sans contournement trivial, tout en laissant visibles les coordonnées publiques du club).
   - Bonus propre : parser également la balise `<site>` présente dans `document/club.xml` et l'ajouter à `Club` / au tableau de l'annuaire.

---

### BLOC 5 — Suite de tests (Unitaires & Acceptation)
**User Stories regroupées :** `US6` (`#50366`) · `US9` (`#50372`) · `US13` (`#50378`) · vérification `US10`/`US11`
**Fichiers concernés :** nouveau dossier `test/` (à configurer comme *Test Sources Root* dans `TP-GEA.iml`, avec JUnit 5 ou une classe de tests autonome exécutable sans dépendance externe si pas de Maven/Gradle)

> *Pourquoi ce regroupement :* Tous les critères de recette de tests sont aujourd'hui à `0`. Les regrouper permet d'avoir une suite complète et rejouable facilement.

#### Travail à réaliser :
1. **Tests unitaires calcul de catégorie & tarif (US6 `#50366`) :**
   - Tester le chargement de `document/categorie.xml` (10 catégories, tarifs Annexe 1 exacts).
   - Tester l'attribution de catégorie et de tarif sur les cas limites (naissance début/fin d'année comme `15/12/2019` → *Poussins M9 / 160 €*, `2026` → *Éveil M5 / 90 €*, `1987` → *Vétérans / 255 €*, `1988` → *Séniors / 255 €*, hors bornes → `null`).
   - Tester `Adherent.getAge()` et `Adherent.estMineur()`.
2. **Tests unitaires validation des champs (US13 `#50378`) :**
   - Tester tous les cas valides et invalides : champs obligatoires vides (`nom`, `prenom`, `dateNaissance`, `telephone`, `email`), emails malformés, téléphones invalides, codes postaux invalides, date `31/02/2020`, date future, adhérent mineur sans/avec responsable légal.
3. **Test d'acceptation récapitulatif financier (US9 `#50372`) :**
   - Constituer un jeu de données représentatif (plusieurs adhérents répartis sur plusieurs catégories) et vérifier `calculerMontantTotal()` et `genererRapportParCategorie()` par rapport aux totaux attendus.
4. **Tests sécurité sauvegarde/restauration (US10 / US11) :**
   - Exporter une liste d'adhérents dans un fichier temporaire, lire les octets bruts du fichier et **asserter l'absence en clair** des chaînes sensibles (nom, email, téléphone, adresse).
   - Réimporter ce fichier et vérifier l'égalité des données restaurées.
   - Tenter d'importer un fichier inexistant, un fichier texte corrompu et un objet sérialisé d'un mauvais type, en vérifiant qu'aucune exception non gérée ne remonte.

---

### BLOC 6 — Documentation, Notes RGPD, Nettoyage Git & Suivi Tuleap
**User Stories / Livrables regroupés :** `US0` · `US7` (`#50379`) · `US12` (`#50380`, `#50351`) · `UST0` · Livrables PDF
**Fichiers concernés :** `document/GEA.drawio`, nouveaux documents dans `document/`, gestion Git & Tuleap

> *Pourquoi ce regroupement :* Ce bloc rassemble tous les livrables non-code et la mise en ordre finale pour la soutenance / revue de Sprint 2.

#### Travail à réaliser :
1. **Documentation technique (Livrable 2) :**
   - Rédiger un document (ex. `document/documentation_technique.md`) décrivant l'architecture MVC, le modèle de classes retenu (`Adherent`, `Categorie`, `Club`, managers et repositories) et le choix de sérialisation + chiffrement AES.
   - Mettre à jour `document/GEA.drawio` pour aligner les signatures des méthodes (`modifierAdherent`, `calculerMontantTotal`, `genererRapportParCategorie`, `SauvegardeManager`, `ValidateurAdherent`, etc.) avec le code final.
2. **Note de justification RGPD & inventaire des données (US7 `#50379` + US12 `#50380` / `#50351`) :**
   - Rédiger une note (ex. `document/note_rgpd.md`) contenant :
     - L'inventaire des données personnelles manipulées (adhérents + responsables légaux + bénévoles dans `club.xml`).
     - La justification du traitement pour `club.xml` (pourquoi anonymiser/restreindre les coordonnées personnelles des bénévoles tout en conservant les infos utiles à la mutualisation entre clubs).
     - Les mesures de protection appliquées aux adhérents (validation des entrées, chiffrement des exports `.gea`, pas de stockage en clair, finalité limitée à la gestion des adhésions).
3. **Hygiène Git & mise à jour Tuleap :**
   - Supprimer le fichier parasite `.gitignore copy` à la racine.
   - Commiter et pousser proprement tout le travail sur GitLab (`Elias` puis merge sur `main` selon l'organisation d'équipe).
   - Mettre à jour les statuts des tâches dans **Tuleap Campus** pour qu'ils reflètent l'état réel après réalisation des Blocs 1 à 6.

---

## Annexe — Prompt prêt à copier pour lancer Claude bloc par bloc

```text
Lis d'abord le fichier document/etat_projet_et_reste_a_faire.md ainsi que les fichiers source existants dans src/.
Respecte strictement les règles de la section 0 (Java Swing pur en .java, MVC, tout en français, périmètre strict).

Nous allons traiter le reste à faire BLOC PAR BLOC (dans l'ordre Bloc 1 → Bloc 2 → Bloc 3 → Bloc 4 → Bloc 5 → Bloc 6).
Commence UNIQUEMENT par le BLOC 1 (Catégories d'âge, Tarifs Annexe 1, Fiche adhérent & Récapitulatif financier).
Applique les modifications propres nécessaires, vérifie que le projet compile sans erreur avec javac, résume exactement ce que tu as modifié/testé, et attends mon "GO" avant de passer au bloc suivant.
```

