# NFC Emulator — Design Document

## 1. Vue d'ensemble

**NFC Emulator** est une application Android native en Kotlin, gratuite et open source (GPL v3), qui permet de cloner des badges d'acces (immeuble, parking, porte) et de les emuler depuis le telephone.

### Objectifs
- Detecter automatiquement le type de tag NFC (Mifare Classic 1K/4K, Ultralight, DESFire)
- Importer des dumps depuis fichiers `.mfd`, `.bin`, `.dump`, `.mct` + lecture directe NFC
- Emettre/emuler les badges clones via NFC du telephone
- Interface avancee : gestion de badges, editeur hexadecimal, comparaison de dumps, analyse de cles
- Root optionnel (sans root = HCE ISO 14443-4 ; avec root = emulation Mifare Classic complete)

### Cible
Utilisateurs Android souhaitant consolider leurs badges d'acces sur leur telephone.

### Non-goals
- Pas de backend/serveur
- Pas de monetisation
- Pas de support iOS

---

## 2. Contraintes techniques

| Contrainte | Detail |
|-----------|--------|
| Android minimum | API 26 (Android 8.0) |
| Target SDK | 35 (Android 15) |
| Langage | Kotlin 2.0+ |
| Taille cible | < 20 MB |
| Licence | GPL v3 |
| Distribution | Google Play Store + F-Droid + APK GitHub |
| Langues | Anglais (defaut) + Francais |

### Limitations hardware
- Le chipset NFC du telephone determine les capacites d'emulation
- L'emulation Mifare Classic complete necessite root + chipset NXP compatible
- Sans root, seule l'emulation ISO-DEP / NDEF est possible via HCE

---

## 3. Architecture

### Approche : Architecture modulaire par couches

```
+-----------------------------------+
|       UI (Jetpack Compose)         |
+-----------------------------------+
|    ViewModel / StateFlow           |
+----------+----------+-------------+
|  NFC     |  Dump    |  Crypto     |
|  Engine  |  Manager |  Storage    |
+----------+----------+-------------+
|   Hardware Abstraction Layer       |
|   (HCE standard / Root NXP)       |
+-----------------------------------+
```

### Structure du projet

```
nfc-emulator/
+-- app/
|   +-- src/main/
|   |   +-- java/com/nfcemulator/
|   |   |   +-- ui/                    # Jetpack Compose screens
|   |   |   |   +-- home/              # Liste des badges sauvegardes
|   |   |   |   +-- reader/            # Ecran lecture NFC
|   |   |   |   +-- emulator/          # Ecran emulation active
|   |   |   |   +-- editor/            # Editeur hexadecimal
|   |   |   |   +-- compare/           # Comparaison de dumps
|   |   |   |   +-- settings/          # Parametres
|   |   |   |   +-- theme/             # Charte graphique, couleurs, typo
|   |   |   +-- nfc/                   # NFC Engine
|   |   |   |   +-- reader/            # Lecture de tags (detection auto)
|   |   |   |   +-- emulator/          # HCE service + root bridge
|   |   |   |   +-- hal/               # Abstraction chipset (HCE vs Root)
|   |   |   +-- dump/                  # Dump Manager
|   |   |   |   +-- parser/            # Parsers (.mfd, .bin, .mct, .dump)
|   |   |   |   +-- model/             # Data classes (Tag, Sector, Block)
|   |   |   |   +-- analyzer/          # Analyse cles, diff entre dumps
|   |   |   +-- storage/               # Crypto Storage
|   |   |   |   +-- local/             # Room DB + fichiers chiffres
|   |   |   |   +-- backup/            # Export/import cloud chiffre
|   |   |   +-- util/                  # Extensions, helpers
|   |   +-- res/                       # Resources Android
|   |   +-- AndroidManifest.xml
|   +-- build.gradle.kts
+-- gradle/
+-- build.gradle.kts
+-- settings.gradle.kts
+-- LICENSE                            # GPL v3
```

---

## 4. NFC Engine & HAL

### Detection automatique du type de tag

```
Tag approche
  -> IsoDep detecte ?
  |   +-- Oui -> Lire ATR/ATS -> identifier DESFire / ISO 14443-4
  |   +-- Non -> MifareClassic detecte ?
  |       +-- Oui -> Identifier 1K/4K via SAK byte
  |       +-- Non -> MifareUltralight detecte ?
  |           +-- Oui -> Lire version -> NTAG/UL-C/EV1
  |           +-- Non -> Tag inconnu -> afficher infos brutes (UID, SAK, ATQA)
```

### Abstraction HAL

```kotlin
interface NfcEmulatorHal {
    fun getCapabilities(): EmulationCapabilities
    fun startEmulation(dump: TagDump): EmulationResult
    fun stopEmulation()
    fun isEmulating(): Boolean
}

// Mode standard (sans root)
class HceEmulatorHal : NfcEmulatorHal
  // Emule ISO-DEP uniquement via HostApduService natif Android

// Mode root (avec root)
class RootNxpEmulatorHal : NfcEmulatorHal
  // Accede au chipset NXP via libnfc-nxp
  // Emule Mifare Classic complet (UID + donnees secteurs)
```

### Selection automatique

```
App demarre
  -> Verifie root disponible (SU check)
  |   +-- Root OK -> Verifie chipset NXP compatible
  |   |   +-- NXP OK -> RootNxpEmulatorHal (full)
  |   |   +-- Pas NXP -> HceEmulatorHal (limite) + avertissement
  |   +-- Pas root -> HceEmulatorHal (limite) + info utilisateur
```

---

## 5. Dump Manager

### Modele de donnees unifie

```kotlin
data class TagDump(
    val id: String,
    val name: String,
    val category: TagCategory,    // BUILDING, PARKING, OFFICE, OTHER
    val type: TagType,            // MIFARE_CLASSIC_1K, 4K, ULTRALIGHT, DESFIRE
    val uid: ByteArray,
    val atqa: ByteArray,
    val sak: Byte,
    val sectors: List<Sector>,
    val sourceFormat: DumpFormat,  // MFD, BIN, MCT, DUMP, NFC_READ
    val createdAt: Instant,
    val notes: String
)

data class Sector(
    val index: Int,
    val blocks: List<Block>
)

data class Block(
    val index: Int,
    val data: ByteArray,          // 16 bytes
    val keyA: ByteArray?,         // 6 bytes
    val keyB: ByteArray?,         // 6 bytes
    val accessBits: ByteArray?    // 4 bytes
)
```

### Parsers multi-format

| Format | Source | Structure |
|--------|--------|-----------|
| `.mfd` / `.bin` | Flipper Zero, ACR122U, MFOC | Raw binary 1024/4096 bytes |
| `.mct` | MIFARE Classic Tool | Texte hex par secteur avec `+Sector:` headers |
| `.dump` | libnfc, proxmark | Raw binary, variantes possibles |
| `.json` | Export NFC Emulator (format natif) | JSON structure |

### Fonctionnalites avancees
- **Editeur hex** : modification bloc par bloc avec validation des access bits
- **Comparaison** : diff visuel entre 2 dumps (bloc par bloc, couleur rouge/vert)
- **Analyse des cles** : detection des cles par defaut, affichage cles trouvees vs manquantes

---

## 6. Dictionnaires de cles & Cracking

### Dictionnaires integres (~6 500 cles uniques)

| Dictionnaire | Source projet | Cles approx. |
|-------------|-------------|--------------|
| `default_keys.txt` | Standard NXP | ~20 |
| `mct_extended.txt` | MIFARE Classic Tool | ~1 400 |
| `flipper_mf_classic.txt` | Flipper Zero firmware | ~1 800 |
| `proxmark_default.txt` | Proxmark3 RRG/Iceman | ~1 200 |
| `mfoc_keys.txt` | MFOC | ~50 |
| `libnfc_keys.txt` | libnfc | ~30 |
| `icopy_x.txt` | iCopy-X/XS | ~900 |
| `rfidresearchgroup.txt` | RFID Research Group | ~600 |
| `magic_chinese.txt` | Tags magic UID modifiable | ~100 |
| `vigik_public.txt` | Communaute FR | ~200 |
| `urmet.txt` | Communaute | ~50 |
| `came.txt` | Communaute | ~50 |
| `saflok.txt` | Communaute | ~30 |
| `salto.txt` | Communaute | ~40 |
| `dormakaba.txt` | Communaute | ~40 |
| `intratone.txt` | Communaute FR | ~80 |

### Moteur de cracking progressif

```
Badge approche
  -> Lecture UID, SAK, ATQA
  -> Tentative lecture secteur 0 (souvent non protege)
  -> Pour chaque secteur verrouille :
      1. Tester cles par defaut (rapide, < 1s)
      2. Tester dictionnaire etendu (quelques secondes)
      3. Tester tous les dictionnaires fusionnes (30s-2min)
      4. Si root : nested attack / darkside attack (libnfc)
      5. Rapport : cles trouvees / secteurs dechiffres / secteurs echoues
```

### Gestion des dictionnaires
- Fusion intelligente avec deduplication
- Apprentissage : cles trouvees ajoutees a `user_keys.txt`
- Priorite par frequence de succes
- Import de dictionnaires personnalises

---

## 7. Stockage chiffre & Backup

### Architecture stockage

```
+-------------------------------+
|    Room Database (SQLite)      |  <- Metadonnees
+-------------------------------+
|  Fichiers chiffres (AES-256)   |  <- Dumps bruts
+-------------------------------+
|     Android KeyStore           |  <- Cle maitre hardware-backed
+-------------------------------+
```

### Schema Room DB

```kotlin
@Entity(tableName = "tags")
data class TagEntity(
    @PrimaryKey val id: String,
    val name: String,
    val category: String,
    val type: String,
    val uid: String,
    val sak: Int,
    val atqa: String,
    val sectorCount: Int,
    val keysFound: Int,
    val keysTotal: Int,
    val sourceFormat: String,
    val filePath: String,
    val notes: String,
    val createdAt: Long,
    val lastEmulatedAt: Long?
)
```

### Backup cloud (optionnel)
- Export : dumps chiffres -> archive ZIP chiffree (mot de passe utilisateur) -> Google Drive / fichier local
- Import : download -> dechiffrement ZIP -> injection Room + fichiers locaux
- Double chiffrement : AES fichier + mot de passe ZIP
- Aucun serveur tiers, tout via SAF Android

---

## 8. Stack technique

### Dependances principales

| Couche | Techno |
|--------|--------|
| UI | Jetpack Compose + Material 3 |
| Navigation | Compose Navigation |
| State | ViewModel + StateFlow |
| DB | Room |
| Chiffrement | AndroidKeyStore + AES-256-GCM |
| NFC | android.nfc.* natif + HostApduService |
| Root | libsu (topjohnwu) |
| DI | Koin |
| Async | Kotlin Coroutines + Flow |
| Fichiers | SAF (Storage Access Framework) |
| Biometrie | androidx.biometric |
| Tests | JUnit + MockK + Coroutines Test + Compose UI Test |

### Exclusions (YAGNI)
- Retrofit / OkHttp (pas d'API reseau)
- Hilt / Dagger (Koin suffit)
- RxJava (Coroutines suffisent)
- Firebase (pas de backend)
- SQLCipher (KeyStore suffit)

---

## 9. Decision Log

| # | Decision | Alternatives | Raison |
|---|----------|-------------|--------|
| 1 | Architecture modulaire par couches | Monolithique, Clean Architecture complete | Equilibre structure/simplicite |
| 2 | Mono-module Gradle | Multi-module | Pas de complexite build inutile |
| 3 | HAL avec 2 implementations (HCE / Root NXP) | Root obligatoire, HCE seul | Degradation gracieuse sans root |
| 3b | Moteur de cracking progressif | Brute-force seul | Maximise le dechiffrement |
| 3c | 16 dictionnaires open source (~6 500 cles) | Dictionnaire unique | Efficacite des la premiere utilisation |
| 4 | KeyStore + AES-256-GCM + backup ZIP chiffre | SQLCipher, Firebase | Securite hardware, zero serveur |
| 5 | Stack minimale (Compose + Room + Koin + libsu) | Hilt, RxJava, Retrofit | App legere, peu de dependances |

---

## 10. Hypotheses

- Le chipset NFC du telephone determine les capacites d'emulation (limitation hardware)
- L'emulation Mifare Classic complete necessite root + chipset NXP compatible
- Sans root, seule l'emulation ISO-DEP / NDEF est possible via HCE
- Les dictionnaires de cles proviennent de projets open source publics
- L'app ne necessite aucune connexion reseau pour fonctionner

---

## 11. Risques identifies

| Risque | Impact | Mitigation |
|--------|--------|------------|
| Chipset NFC incompatible | Emulation limitee | Detection auto + message clair a l'utilisateur |
| Google Play Store refuse l'app | Pas de distribution Play | F-Droid + APK GitHub comme alternatives |
| Certains badges resistant au cracking | Secteurs illisibles | Rapport clair, suggestions (Proxmark3, Flipper) |
| APIs NFC Android evoluent | Maintenance | Target SDK a jour, tests sur plusieurs versions |
