# 🤺 Application GEA — Cercle d'Escrime de Vandœuvre

[![Java 21 LTS](https://img.shields.io/badge/Java-21%20LTS-ED8B00?style=flat-square&logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/21/)
[![GUI Swing Pur](https://img.shields.io/badge/GUI-Swing%20Pur%20(sans%20.form)-5382a1?style=flat-square&logo=java&logoColor=white)](https://docs.oracle.com/en/java/javase/21/docs/api/java.desktop/javax/swing/package-summary.html)
[![Dépendances](https://img.shields.io/badge/D%C3%A9pendances-0%20(Standard%20JDK)-brightgreen?style=flat-square)](https://github.com/)
[![Tests Passés](https://img.shields.io/badge/Tests-343%2F343%20Assertions%20(100%25)-success?style=flat-square)](test/LanceurTests.java)
[![Sécurité & Chiffrement](https://img.shields.io/badge/S%C3%A9curit%C3%A9-AES--256--GCM%20%7C%20PBKDF2%20%7C%20RBAC-blue?style=flat-square)](document/note_rgpd.md)
[![RGPD](https://img.shields.io/badge/RGPD-Article%2032%20Conforme-informational?style=flat-square)](document/note_rgpd.md)

Application desktop de gestion associative (licenciés, cotisations, annuaire partenarial et sauvegardes chiffrées) développée pour le Cercle d'Escrime de Vandœuvre (Maison des Ligues de Lorraine - M2L).


---

## 📌 Fonctionnalités

* **Gestion des adhérents (CRUD)** : saisie, modification, suppression, fiches détaillées et recherche multi-critères (nom, prénom, arme, catégorie).
* **Calcul automatique des catégories & tarifs** : application des règles fédérales (M9 à Vétérans) et calcul dynamique de la cotisation selon la grille officielle (Annexe 1).
* **Tableau de bord financier** : synthèse des effectifs et des recettes prévisionnelles ventilée par tranche d'âge.
* **Annuaire des clubs partenaires** : consultation, impression et export HTML responsive des 10 clubs d'escrime de Meurthe-et-Moselle.
* **Sauvegarde & Restauration chiffrée** : export/import binaire propriétaire (`.gea`) protégé par chiffrement fort.
* **Authentification & Contrôle d'accès (RBAC)** : cloisonnement des privilèges selon 4 rôles opérationnels (Secrétaire, Trésorier, Président, Administrateur).

---

## 🏗️ Arborescence (MVC)

```
TP-GEA/
├── src/
│   ├── modele/          # Entités métiers : Adherent, Categorie, Club, Utilisateur, Role, Droit
│   ├── repository/      # Persistance : UtilisateurRepository (atomique), ClubRepository (XML)
│   ├── manager/         # Logique métier : AdherentManager, AuthentificationManager, ValidateurAdherent, SauvegardeManager
│   ├── vue/             # IHM Swing pure : FenetrePrincipale, ConnexionVue, ChangementMotDePasseVue, etc.
│   └── controleur/      # Orchestration et garde-fous RBAC : AdherentControleur
├── test/                # Banc d'essai autonome : LanceurTests, OutilAssertions (5 suites, 343 assertions)
└── document/            # Documentation technique, note RGPD, utilisateurs.dat et présentations
```

---

## ⚡ Principales Difficultés Techniques & Solutions Apportées

### 1. Zéro dépendance externe pour la cryptographie & l'authentification
* **Défi** : Implémenter un système d'authentification robuste et un contrôle d'accès multi-rôles sans bibliothèques tierces (Spring Security, Apache Shiro, BouncyCastle).
* **Solution** : 
  * Dérivation de clé avec `PBKDF2WithHmacSHA256` (65 536 itérations, clé 256 bits).
  * Génération de sels cryptographiques aléatoires de 16 octets (`SecureRandom`) uniques par utilisateur.
  * Comparaison en temps constant via `MessageDigest.isEqual()` pour neutraliser les attaques temporelles (*timing attacks*).
  * Hygiène stricte de la mémoire vive : effacement immédiat des mots de passe en mémoire (`Arrays.fill(..., '\0')` sur tous les `char[]` et `byte[]`).
  * Protection anti-force brute avec verrouillage automatique après 5 tentatives infructueuses.

### 2. Sécurisation des sauvegardes chiffrées & Prévention des failles de désérialisation
* **Défi** : Garantir la confidentialité des données adhérents exportées sur disque sans exposer l'application aux attaques par injection d'objets lors de l'import.
* **Solution** :
  * Format binaire propriétaire `GEA1` chiffré en **AES/GCM/NoPadding** (clé 256 bits, IV aléatoire de 12 octets, tag d'intégrité 128 bits, en-tête validé en données associées authentifiées - AAD).
  * Filtrage strict par liste blanche (`ObjectInputFilter`) à la désérialisation : seules les classes autorisées (`ArrayList`, `Adherent`, `LocalDate`, `String`) sont acceptées ; tout bytecode tiers ou altéré est immédiatement rejeté.

### 3. Dynamisme de l'IHM Swing pur sans générateur de code
* **Défi** : Maintenir une IHM Swing claire, réactive et sécurisée sans éditeur visuel ni fichiers `.form` générés par un IDE.
* **Solution** :
  * Assemblage 100 % manuel des layouts (`GridBagLayout`, `BorderLayout`, `FlowLayout`).
  * Application dynamique des droits RBAC : les actions non autorisées sont soit désactivées (`setEnabled(false)`), soit masquées dynamiquement (`setVisible(false)`) selon le rôle actif dès la connexion.

### 4. Respect du RGPD sur l'annuaire externe (`club.xml`)
* **Défi** : Exploiter les coordonnées des clubs partenaires tout en empêchant la fuite de données personnelles de bénévoles (noms, téléphones, e-mails).
* **Solution** :
  * Parser XML durci contre les attaques XXE (`XMLConstants.FEATURE_SECURE_PROCESSING`).
  * Anonymisation et masquage partiel automatique dès la lecture en mémoire (`03.**.**.**.**`, `c***@domaine.fr`).
  * Aucune fonction ne permet d'afficher ou d'exporter les coordonnées réelles en clair.

### 5. Validation stricte des données & Détection de minorité
* **Défi** : Empêcher les incohérences de saisie sans framework de validation (comme Hibernate Validator).
* **Solution** :
  * Moteur de validation centralisé (`ValidateurAdherent`) appliquant des expressions régulières strictes (téléphone national/international, e-mail RFC 5322, code postal).
  * Détection automatique de la minorité (date de naissance) imposant la saisie obligatoire des responsables légaux.

---

## 🧪 Banc de Tests Automatisé (100 % Autonome)

Le projet intègre un moteur de test dédié ([test/OutilAssertions.java](test/OutilAssertions.java)) exécutable sans JUnit :

```text
======================================================================
RÉSULTATS DE LA SUITE DE TESTS (LanceurTests)
======================================================================
[SUCCÈS] CategorieEtTarifTest (US6)                 (51 assertions)
[SUCCÈS] ValidateurAdherentTest (US13)              (68 assertions)
[SUCCÈS] RecapitulatifFinancierTest (US9)           (20 assertions)
[SUCCÈS] SauvegardeEtAnnuaireTest (US10/US11/US7)   (74 assertions)
[SUCCÈS] AuthentificationTest (Sécurité & RBAC)     (130 assertions)
======================================================================
Total assertions vérifiées : 343 | Échecs : 0 (100% SUCCÈS)
======================================================================
```

---

## 🚀 Démarrage Rapide

### Prérequis
* **JDK 21** ou supérieur.

### 1. Compilation
```bash
# Compilation du code source applicatif
javac -encoding UTF-8 -d out/production/TP-GEA src/*.java src/**/*.java

# Compilation de la suite de tests
javac -encoding UTF-8 -cp out/production/TP-GEA -d out/test/TP-GEA test/*.java
```

### 2. Exécution des tests
```bash
java -Djava.awt.headless=true -cp out/production/TP-GEA:out/test/TP-GEA LanceurTests
```

### 3. Lancement de l'application
```bash
java -cp out/production/TP-GEA FenetrePrincipale
```

---

## 🔑 Comptes de Démonstration (RBAC)

L'écran de connexion propose un menu déroulant pré-remplissant les comptes officiels :

| Identifiant | Mot de passe | Rôle | Périmètre d'accès |
| :--- | :--- | :--- | :--- |
| `secretaire` | `Secretaire2026!` | `SECRETAIRE` | CRUD Adhérents complet, fiches, consultation clubs. *(Finances et sauvegardes verrouillées).* |
| `tresorier` | `Tresorier2026!` | `TRESORIER` | Tableau de bord financier, sauvegardes/restaurations `.gea`. *(Modification d'adhérents verrouillée).* |
| `president` | `President2026!` | `PRESIDENT` | Annuaire des clubs, impression et export HTML, consultation adhérents. |
| `admin` | `AdminM2L2026!` | `ADMINISTRATEUR` | Accès intégral à tous les modules + déblocage des comptes verrouillés. |

---

## 📂 Documentation Associée

* [document/documentation_technique.md](document/documentation_technique.md) : Spécifications complètes d'architecture et de sécurité.
* [document/note_rgpd.md](document/note_rgpd.md) : Audit de conformité réglementaire RGPD (Art. 5, 17, 32).
* [document/etat_projet_et_reste_a_faire.md](document/etat_projet_et_reste_a_faire.md) : État d'avancement des User Stories.
