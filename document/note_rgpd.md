# Note de conformité RGPD — Application GEA (Cercle d'Escrime de Vandœuvre)

## 1. Contexte et cadre réglementaire

Cette note présente les mesures mises en œuvre dans l'application **GEA (Gestion des Entraînements et des Adhérents)** du **Cercle d'Escrime de Vandœuvre** afin d'assurer la conformité avec les principes du **Règlement Général sur la Protection des Données (RGPD — Règlement UE 2016/679)** : licéité, minimisation des données, exactitude, limitation de la conservation, intégrité et confidentialité.

---

## 2. Inventaire des données à caractère personnel traitées

| Catégorie de personnes | Source / Classe concernée | Données manipulées | Finalité |
| :--- | :--- | :--- | :--- |
| **Adhérents** | [`Adherent`](../src/modele/Adherent.java) | Identifiant (`id`), nom, prénom, nom de naissance, date de naissance, adresse, code postal, ville, téléphone, adresse e-mail, arme, type de pratique, latéralité | Gestion administrative des adhésions, calcul automatique de catégorie d'âge et de cotisation Annexe 1, suivi financier par catégorie. |
| **Représentants légaux (adhérents mineurs)** | [`Adherent`](../src/modele/Adherent.java) | Nom (`nomRespLegal`) et prénom (`prenomRespLegal`) du responsable légal | Collecte conditionnelle obligatoire uniquement lorsque `estMineur()` est vrai, afin d'identifier le responsable civil du mineur. |
| **Bénévoles des clubs partenaires** | [`document/club.xml`](club.xml) / [`Club`](../src/modele/Club.java) | Nom du bénévole (`benevoleNom`), téléphone (`benevoleTelephone`), e-mail (`benevoleEmail`), ainsi que les coordonnées institutionnelles du club (`nom`, `ville`, `adresse`, `site`) | Consultation et impression de l'annuaire des 10 clubs d'escrime de Meurthe-et-Moselle. |

---

## 3. Protection des données de l'annuaire des clubs (`club.xml`)

Le fichier de référence [`document/club.xml`](club.xml), conservé intact dans le dépôt, contient des noms, numéros de téléphone et adresses électroniques nominatives de bénévoles. Afin d'éviter toute exposition ou diffusion incontrôlée de ces données personnelles :

1. **Anonymisation par défaut en mémoire (`ClubRepository.anonymiserDonnees`)**
   - La méthode `anonymiserDonnees(List<Club>)` de [`ClubRepository`](../src/repository/ClubRepository.java) applique un masquage partiel sur les coordonnées des bénévoles tout en préservant les informations publiques du club (nom, ville, adresse, `<site>`).
   - Exemples du masquage appliqué :
     - Nom du bénévole : `M. S*** G.`
     - Téléphone : `03.**.**.**.**`
     - E-mail : `c***@escrime-laxou.net`
2. **Absence de mécanisme de contournement**
   - L'interface [`AnnuaireClubsVue`](../src/vue/AnnuaireClubsVue.java) ne propose aucun bouton permettant d'afficher ou d'exporter les coordonnées en clair.
   - Les sorties externes — impression directe via `imprimerTableau()` et export HTML imprimable via `genererHtmlImprimable(List<Club>)` — reposent exclusivement sur les données déjà anonymisées.
3. **Justification du masquage partiel (minimisation)**
   - Ce choix garantit à la fois l'utilité informative de l'annuaire (localisation, adresse et site web du club, existence d'un contact référent) et l'impossibilité d'identifier directement le bénévole ou de collecter son numéro ou son adresse e-mail sur un document imprimé ou partagé.

---

## 4. Protection des données des adhérents et des mineurs

### 4.1 Minimisation et conservation en mémoire
- L'application fonctionne exclusivement en poste local sans base de données distante, sans journalisation des données personnelles et sans transmission réseau.
- Pendant l'exécution, la liste des adhérents est maintenue uniquement en mémoire vive par [`AdherentManager`](../src/manager/AdherentManager.java).

### 4.2 Exercice des droits (rectification et effacement)
- **Droit de rectification** : `AdherentManager.modifierAdherent(Adherent)` permet de corriger à tout moment les informations d'un adhérent.
- **Droit à l'effacement** : `AdherentManager.supprimerAdherent(int)` permet de supprimer définitivement un adhérent de la liste active en mémoire (et donc des exports ultérieurs).

### 4.3 Exactitude des données (article 5.1.d RGPD)
- La classe [`ValidateurAdherent`](../src/manager/ValidateurAdherent.java) impose des contrôles stricts avant tout ajout ou modification :
  - Format de date strict `dd/MM/uuuu` (`ResolverStyle.STRICT`) et borne minimale `1900-01-01` (`DATE_NAISSANCE_MIN`).
  - Validation syntaxique des e-mails (`estEmailValide`), téléphones (`estTelephoneValide`) et codes postaux (`estCodePostalValide`).
  - Contrôle des valeurs autorisées pour l'arme, la pratique et la latéralité (`ARMES_AUTORISEES`, `PRATIQUES_AUTORISEES`, `LATERALITES_AUTORISEES`).
  - Exigence stricte de l'identité du représentant légal pour les mineurs (`estMineur()`).

### 4.4 Confidentialité, intégrité et sécurité des exports (article 32 RGPD)
Lorsque l'utilisateur exporte les données vers un fichier local via [`SauvegardeManager`](../src/manager/SauvegardeManager.java) :
- **Chiffrement fort et authentifié** : chaque sauvegarde utilise le format binaire `GEA1` (version `0x01`) avec sel aléatoire de 16 octets, IV aléatoire de 12 octets, dérivation de clé `PBKDF2WithHmacSHA256` (65 536 itérations, clé 256 bits) et chiffrement authentifié `AES/GCM/NoPadding` (tag 128 bits, en-tête protégé en AAD).
- **Aucun fichier en clair** : les données personnelles (noms, adresses, dates de naissance, contacts des mineurs et représentants légaux) ne sont jamais écrites en texte clair ni en sérialisation Java brute sur le disque.
- **Protection contre les manipulations et injections** :
  - Toute altération du fichier chiffré ou erreur de mot de passe est détectée par AES-GCM et rejetée proprement (`importerDonnees` renvoie `null` sans écraser les adhérents déjà en mémoire).
  - La désérialisation applique un `ObjectInputFilter` strict par liste blanche (`ArrayList`, `Object[]`, `Adherent`, `LocalDate`, `java.time.Ser`, `String`) dans `deserialiserListeSecurisee(byte[])`.
  - Le parseur XML des clubs active `XMLConstants.FEATURE_SECURE_PROCESSING` dans [`ClubRepository`](../src/repository/ClubRepository.java).

---

## 5. Bonnes pratiques d'exploitation recommandées au club

1. **Information des personnes** : remettre une mention d'information RGPD lors de l'inscription (finalité associative, durée de conservation, contact du responsable de traitement au sein du Cercle d'Escrime de Vandœuvre).
2. **Gestion des sauvegardes** : saisir systématiquement un mot de passe robuste lors des exports (`exporterDonnees(..., char[] motDePasse)`) et conserver les fichiers de sauvegarde sur un support sécurisé à accès restreint.
3. **Purge périodique** : supprimer chaque saison les fiches des anciens adhérents qui ne renouvellent pas leur licence afin de respecter la limitation de la durée de conservation.

