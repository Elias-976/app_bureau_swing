# Script Oral & Répartition à 3 Développeurs — Application GEA

> **Support visuel HTML :** `document/presentation_gea.html` (8 slides, à ouvrir dans Chrome/Safari — raccourcis : **`F`** plein écran · **`→`** / **`Espace`** suivant · **`G`** grille · **`B`** écran noir pour masquer la diapo lors de la démo)
> **Durée cible (slides uniquement) :** ~7 min 25 (répartis équitablement entre **Elias**, **Wassim** et **Youness**)
> **Coupure Démonstration :** Aucune slide dédiée. Deux créneaux naturels sont prévus ci-dessous (**Option A** après la Slide 7, ou **Option B** après la Slide 8) — appuyez sur **`B`** (écran noir) ou quittez le plein écran (`Échap`) pour afficher l'application Java.

---

## Vue d'ensemble des tours de parole

| Slide / Temps | Sujet | Qui parle ? | Durée | Passe le relais à |
| :--- | :--- | :--- | :---: | :--- |
| **Slide 1** | Couverture | **Elias** *(Dev 1)* | 0:20 | *(continue)* |
| **Slide 2** | Sommaire | **Elias** *(Dev 1)* | 0:20 | *(continue)* |
| **Slide 3** | Contexte, périmètre & Tuleap | **Elias** *(Dev 1)* | 0:55 | *(continue)* |
| **Slide 4** | Architecture MVC & socle Java | **Elias** *(Dev 1)* | 0:55 | **Wassim** |
| **Slide 5** | Adhérents, tarifs FFE & validation | **Wassim** *(Dev 2)* | 1:20 | **Youness** |
| **Slide 6** | Annuaire des clubs & RGPD | **Youness** *(Dev 3)* | 1:05 | **Wassim** |
| **Slide 7** | Sauvegardes chiffrées `GEA1` | **Wassim** *(Dev 2)* | 1:15 | **Youness** *(ou Démo Option A)* |
| **Slide 8** | Tests (213 assert.), limites & bilan | **Youness** *(Dev 3)* | 1:15 | **Équipe** *(Démo Option B / Q&R)* |
| **Coupure Démo** | Démo live en 3 temps *(hors diapo)* | **Elias → Youness → Wassim** | ~2:00 | — |

**Temps total hors démo :** Elias **~2 min 30** · Wassim **~2 min 35** · Youness **~2 min 20** (et **~40 s chacun** en démo).

---

## Slide 1 — Couverture (`Application GEA`) — *~20 sec*
### 🎤 Tour de parole : **Elias** *(Dev 1)*

**À montrer sur l'écran :** Titre, commanditaire (Cercle d'Escrime de Vandœuvre), contexte M2L et les trois noms en bas de slide.

> **Elias :** « Bonjour à toutes et à tous. Dans le cadre des Ateliers de professionnalisation de BTS SIO SLAM, nous avons réalisé au sein de l'ESN fictive **LorInfo Solutions** l'application **GEA — Gestion des Entraînements et des Adhérents**.
>
> Je suis accompagné aujourd'hui de **Wassim** et **Youness**. Notre solution répond au besoin du **Cercle d'Escrime de Vandœuvre**, hébergé à la Maison des Ligues de Lorraine, avec une architecture pensée pour être étendue à terme aux autres clubs de la région. »

---

## Slide 2 — Sommaire — *~20 sec*
### 🎤 Tour de parole : **Elias** *(Dev 1)*

**À montrer sur l'écran :** Les 6 blocs numérotés (`01` à `06`).

> **Elias :** « Notre présentation s'articule autour de six axes :
> d'abord le contexte et notre organisation Agile sur Tuleap ; ensuite l'architecture MVC retenue ; puis le cœur métier avec les adhérents, la tarification FFE et la validation ; notre arbitrage RGPD sur l'annuaire des clubs ; la sécurisation cryptographique des sauvegardes `.gea` ; et enfin les tests automatisés et le bilan du projet. »

---

## Slide 3 — `01 · Besoin métier, périmètre & pilotage Agile` — *~55 sec*
### 🎤 Tour de parole : **Elias** *(Dev 1)*

**À montrer sur l'écran :** À gauche les livrables Sprint 1/2 et l'encadré *Hors périmètre assumé* ; à droite le tableau Tuleap à 12 pts par développeur.

> **Elias :** « Le cahier des charges définissait un client lourd Java Swing, sans client web ni mobile, sans base de données SQL et sans authentification — des éléments volontairement laissés hors périmètre pour concentrer nos efforts sur la fiabilité métier et la sécurité.
>
> Nous avons livré **13 User Stories sur 2 sprints** suivis dans **Tuleap Campus** et **GitLab** :
> - Le **Sprint 1** couvre le CRUD des adhérents, la gestion des mineurs, l'import de `categorie.xml` et la grille tarifaire Annexe 1.
> - Le **Sprint 2** apporte l'annuaire des clubs conforme RGPD, le récapitulatif financier, la sauvegarde chiffrée `.gea` et la validation stricte des saisies.
>
> La charge a été équilibrée à **12 points chacun**, complétée par trois jalons collectifs : la modélisation UML initiale, la note RGPD et l'audit sécurité de fin de Sprint 2. »

---

## Slide 4 — `02 · Architecture MVC & modèle objet` — *~55 sec*
### 🎤 Tour de parole : **Elias** *(Dev 1)* → passe la parole à **Wassim**

**À montrer sur l'écran :** Balayer le schéma de gauche à droite (`Vues` ↔ `Contrôleur` ↔ `Managers / Repositories` → `Modèle / XML`).

> **Elias :** « Techniquement, l'application repose sur une architecture **MVC stricte en 5 packages Java 21**, sans aucune dépendance externe.
>
> À gauche, nos cinq écrans Swing sont codés **100 % en Java pur**, sans fichier `.form` propriétaire : ils restent passifs et exposent uniquement leurs getters et écouteurs.
>
> Au centre, `AdherentControleur` joue le rôle de médiateur exclusif entre les vues, les **Managers** (logique mémoire, validation, chiffrement) et les **Repositories** (lecture XML).
>
> Nous avons retenu deux choix structurants : une portabilité totale compilable avec le seul JDK 21, et le durcissement des parseurs DOM via `FEATURE_SECURE_PROCESSING` contre les failles XXE, tout en conservant `categorie.xml` et `club.xml` intacts avec prise en charge de la balise `<site>`.
>
> *Je passe maintenant la parole à **Wassim** pour le cœur métier, la tarification et la validation.* »

---

## Slide 5 — `03 · Adhérents, tarification FFE & validation` — *~1 min 20*
### 🎤 Tour de parole : **Wassim** *(Dev 2)* → passe la parole à **Youness**

**À montrer sur l'écran :** À gauche CRUD + mineurs + `ValidateurAdherent` ; à droite l'encadré ambre *Année de naissance vs Âge* et la grille Annexe 1.

> **Wassim :** « Merci Elias. Côté gestion, `AdherentManager` maintient les adhérents en mémoire avec un identifiant auto-incrémenté via `AtomicInteger` et une recherche multicritère instantanée sur le nom, le prénom, le nom de naissance et l'e-mail.
>
> Dès qu'un adhérent est **mineur** (moins de 18 ans), le formulaire affiche et rend obligatoire la saisie d'un **responsable légal**. Sur la fiche et dans le récapitulatif financier, la cotisation et les totaux par catégorie sont calculés automatiquement, sans aucune ressaisie manuelle.
>
> Pour ce calcul de tarif, nous avons résolu un piège métier important, mis en avant à droite : `categorie.xml` raisonne par **année de naissance** selon les règles FFE, et non par âge révolu au jour près. Sans cela, un enfant né le 15 décembre 2019 aurait encore 6 ans en novembre 2026 et tomberait à tort en *Atomes* à 130 € au lieu de *Poussins* à 160 €. En donnant la priorité à l'année de naissance, la grille Annexe 1 de 90 € à 255 € est appliquée avec exactitude.
>
> Enfin, j'ai centralisé tous les contrôles dans `ValidateurAdherent` : dates réelles en `ResolverStyle.STRICT` (qui rejette un 31 février, une date future ou antérieure à 1900), regex e-mail, téléphone français et code postal à 5 chiffres, ainsi que les listes fermées d'arme, de pratique et de latéralité. Cette même classe sert à l'ajout, à la modification et aux tests unitaires.
>
> *Youness va maintenant vous présenter l'annuaire des clubs et nos choix RGPD.* »

---

## Slide 6 — `04 · Annuaire des clubs & protection des bénévoles` — *~1 min 05*
### 🎤 Tour de parole : **Youness** *(Dev 3)* → passe la parole à **Wassim**

**À montrer sur l'écran :** Les deux cartes du haut, puis le tableau comparatif sur `C.E LAXOVIEN` en bas.

> **Youness :** « Merci Wassim. Pour la mutualisation de matériel entre clubs, l'annuaire charge les **10 clubs de Meurthe-et-Moselle** depuis `club.xml`, en extrayant automatiquement la ville et le site web `<site>`. Le tableau est triable, imprimable directement et exportable en HTML imprimable.
>
> Mais `club.xml` contient des données personnelles réelles de bénévoles. Comme justifié dans notre `note_rgpd.md`, nous avons appliqué un **arbitrage RGPD strict** :
> - Les informations institutionnelles du club — nom, ville, adresse et site web — restent en clair car ce sont des données publiques indispensables à la mise en relation.
> - En revanche, dès le chargement en mémoire, les coordonnées personnelles des bénévoles sont **anonymisées par défaut**, comme vous le voyez sur l'exemple du *C.E Laxovien* : `M. S*** G.`, `03.**.**.**.**` et `c***@escrime-laxou.net`.
>
> Surtout, aucun bouton « démasquer » n'existe dans l'interface : l'impression papier et l'export HTML n'exposent jamais les données brutes des bénévoles.
>
> *Je redonne la parole à **Wassim** pour la sécurisation des sauvegardes `.gea`.* »

---

## Slide 7 — `05 · Sauvegardes chiffrées au format GEA1` — *~1 min 15*
### 🎤 Tour de parole : **Wassim** *(Dev 2)* → passe la parole à **Youness** *(ou coupure Démo Option A)*

**À montrer sur l'écran :** La barre binaire `GEA1` (les 4 blocs d'en-tête + l'accolade violette AAD en dessous), puis les 3 cartes en bas.

> **Wassim :** « Merci Youness. Pour l'export et la restauration des adhérents, une simple sérialisation Java via `ObjectOutputStream` aurait laissé toutes les données personnelles lisibles en clair sur le disque et exposé l'application à des failles de désérialisation arbitraire.
>
> Dans `SauvegardeManager`, nous avons donc conçu le format binaire **`GEA1`** :
> - À l'export, une clé **256 bits** est dérivée du mot de passe via **`PBKDF2WithHmacSHA256` sur 65 536 itérations** avec un sel aléatoire de 16 octets.
> - Le flux sérialisé est chiffré en **AES-256-GCM** avec un IV de 12 octets et un tag d'authentification de 128 bits.
> - Comme le montre l'accolade sous le schéma, tout l'en-tête — magique `GEA1`, version, sel et IV — est authentifié en **AAD** : la moindre altération d'un seul octet ou une erreur de mot de passe invalide immédiatement le tag GCM.
> - Et par hygiène mémoire, le mot de passe et la clé sont effacés sitôt utilisés via `Arrays.fill`.
>
> À l'importation, après vérification du tag GCM, la désérialisation passe par un **`ObjectInputFilter` par liste blanche stricte** limité aux 6 classes attendues, avec des bornes anti-DoS sur la profondeur, le nombre de références et la taille maximale du fichier. En cas d'échec, `importerDonnees()` retourne `null` sans lever d'exception brute et laisse la liste en mémoire intacte.
>
> *(Si Option A — Démo au milieu :)* Avant de conclure avec les tests et le bilan, nous vous proposons de passer sur l'application pour voir ces fonctionnalités en direct.
> *(Si Option B — Démo à la fin :)* *Je passe la parole à **Youness** pour les tests automatisés et le bilan du projet.* »

---

### 🔀 COUPURE DÉMONSTRATION — *Option A (après Slide 7) ou Option B (après Slide 8)*
*(Appuyer sur **`B`** pour passer le projecteur en écran noir, ou quitter le plein écran **`Échap`** pour lancer l'application Java.)*

Répartition fluide à 3 pour la démo (~2 minutes au total, ~40 s chacun) :

1. **Elias (`US1, US9` — Saisie mineur, Fiche & Finances)** :
   - Clique sur **Nouvel adhérent**, saisit un **adhérent mineur** (ex. né en `2014`) → montre l'apparition dynamique du bloc *Responsable légal*.
   - Consulte sa fiche (`Benjamins (M13)` et `210,00 €` automatiques) puis ouvre **Finances → Récapitulatif**.
2. **Youness (`US7, US8, US12` — Annuaire RGPD)** :
   - Ouvre **Clubs → Annuaire des clubs**, montre les 10 clubs avec `<site>` et le masquage des bénévoles (`M. S*** G.`).
   - Clique sur **Exporter HTML** pour montrer que la sortie imprimable reste anonymisée (et mentionne l'impression directe `FIT_WIDTH`).
3. **Wassim (`US13, US10, US11` — Validation & Sauvegarde `.gea`)** :
   - Montre brièvement un rejet de validation (ex. date `31/02/2020` ou e-mail invalide).
   - Lance **Sauvegarde → Exporter**, saisit un mot de passe → montre le fichier `.gea` illisible (en-tête `GEA1`).
   - Tente un import avec un **mauvais mot de passe** (refus propre, liste intacte), puis réimporte avec le **bon mot de passe**.

*(Si Option A : appuyer sur `B` ou revenir sur le navigateur en Slide 8, puis Youness reprend.)*

---

## Slide 8 — `06 · Validation automatisée, limites & perspectives` — *~1 min 15*
### 🎤 Tour de parole : **Youness** *(Dev 3)*

**À montrer sur l'écran :** Le bandeau vert `213 assertions · 4 suites · 0 échec`, le tableau des 4 classes de test, puis les cartes *Limites* et *Perspectives* à droite.

> **Youness :** « *(Si reprise après démo Option A :* Revenons pour conclure sur la qualité logicielle et le bilan du projet.*)*
>
> Pour vérifier l'ensemble de ces règles de façon reproductible, nous avons écrit une suite de tests autonome exécutable sans JUnit directement sous JDK 21 via `LanceurTests`.
>
> Comme affiché à gauche, **213 assertions réparties en 4 suites passent avec 0 échec** :
> - 51 assertions sur les catégories XML et tarifs Annexe 1,
> - 68 sur `ValidateurAdherent` (dont le 29 février et la règle des mineurs),
> - 20 sur le récapitulatif financier,
> - et 74 sur la sécurité `.gea` (rejet du mauvais mot de passe, altération d'octet, filtre `ObjectInputFilter`) et le masquage RGPD de l'annuaire HTML.
>
> En toute transparence, nous assumons deux limites liées au cahier des charges : les données vivent en mémoire vive tant qu'on n'exporte pas en `.gea`, et l'absence d'authentification suppose un poste de confiance.
>
> Pour la suite, trois évolutions s'inscrivent naturellement dans le prolongement de la mission : ajouter une persistance locale chiffrée SQLite ou PostgreSQL, mettre en place une gestion des droits par profil (Secrétaire, Trésorier, Président), et déployer l'outil aux autres clubs lorrains de la M2L.
>
> La Mission 1 est ainsi livrée complète, sécurisée, testée et conforme RGPD.
>
> *(Si Option B — Démo à la fin :)* Nous vous proposons maintenant de passer à la démonstration en direct de l'application avant de répondre à vos questions.
> *(Si la démo a déjà eu lieu :)* Merci pour votre attention, nous sommes prêts pour vos questions. »

---

## Mémo Express Q/R Jury *(hors diapo — qui répond ?)*

| Question probable | Référent | Réponse flash |
| :--- | :--- | :--- |
| *Pourquoi les données disparaissent si on ferme sans exporter ?* | **Elias** | Architecture sans BDD SQL imposée par le CDC : `AdherentManager` travaille en mémoire et la persistance repose sur l'export chiffré `.gea`. Une BDD locale chiffrée est la 1re évolution prévue. |
| *Pourquoi pas d'authentification ni de rôles ?* | **Elias** | Explicitement hors périmètre de base dans le sujet. Nous avons concentré la sécurité sur `ValidateurAdherent` (`US13`), le masquage RGPD (`US12`) et AES-256-GCM (`US10`). |
| *Pourquoi classer par année de naissance plutôt que par âge exact ?* | **Wassim** | `categorie.xml` définit des millésimes FFE (`annee_min`/`annee_max`). L'âge révolu déclassait un enfant avant son anniversaire (ex. `15/12/2019`). `Categorie.correspondAdherent()` priorise l'année de naissance. |
| *Comment prouver que `.gea` ne fuit pas et résiste à la corruption ?* | **Wassim** | AES-256-GCM + PBKDF2 (65 536 itér.) + en-tête `GEA1` en AAD + `ObjectInputFilter` à 6 classes. `SauvegardeEtAnnuaireTest` vérifie l'absence de texte clair, le rejet d'un mauvais MdP et d'un octet altéré. |
| *Pourquoi laisser le nom et l'adresse du club en clair si vous parlez de RGPD ?* | **Youness** | Ce sont des données publiques d'une personne morale indispensables à la mutualisation. Seules les données personnelles des bénévoles (nom, tél., mail) sont masquées dans `ClubRepository.anonymiserDonnees()`. |
| *Pourquoi Swing pur et des tests sans JUnit ?* | **Youness / Tous** | Portabilité immédiate entre IntelliJ, Eclipse et CLI sans plugin `.form` ni JAR externe : tout compile et passe les 213 assertions avec le seul JDK 21. |
