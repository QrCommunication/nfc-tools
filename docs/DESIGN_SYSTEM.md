# NFC Emulator — Design System & Charte Graphique

## 1. Identite visuelle

### Nom
**NFC Emulator**

### Tagline
*Read. Clone. Emulate.*

### Style
**Cyberpunk / Dark OLED** — esthetique hacker/terminal, performant sur ecrans AMOLED.

---

## 2. Palette de couleurs

### Couleurs principales

| Role | Nom | Hex | Usage |
|------|-----|-----|-------|
| **Primary** | Neon Cyan | `#00FFFF` | Accents principaux, icones actives, NFC pulse |
| **Secondary** | Matrix Green | `#00FF41` | Succes, cles trouvees, emulation active |
| **Accent** | Neon Magenta | `#FF00FF` | Alertes, actions importantes, CTA |
| **Background** | Deep Black | `#000000` | Fond principal (OLED-friendly) |
| **Surface** | Dark Surface | `#0D0D0D` | Cartes, panneaux, modales |
| **Surface Variant** | Charcoal | `#1A1A1A` | Elevation 2, sous-panneaux |
| **Text Primary** | Light Gray | `#E0E0E0` | Texte principal |
| **Text Secondary** | Medium Gray | `#808080` | Texte secondaire, labels |
| **Border** | Dark Border | `#1F1F1F` | Bordures subtiles |
| **Error** | Alert Red | `#FF3B30` | Erreurs, secteurs echoues |
| **Warning** | Amber | `#FFB800` | Avertissements, root requis |

### Couleurs semantiques NFC

| Etat | Couleur | Usage |
|------|---------|-------|
| Tag detecte | `#00FFFF` (Cyan) | Pulse animation lors de la detection |
| Lecture en cours | `#00FFFF` + pulse | Animation de lecture |
| Cle trouvee | `#00FF41` (Green) | Secteur dechiffre |
| Cle manquante | `#FF3B30` (Red) | Secteur non dechiffre |
| Emulation active | `#00FF41` + glow | Badge en cours d'emulation |
| Root actif | `#FF00FF` (Magenta) | Indicateur mode root |
| HCE seul | `#FFB800` (Amber) | Indicateur mode limite |

### Mode sombre uniquement
L'app est **dark-only** (pas de mode clair). Justification :
- Coherent avec l'esthetique cyberpunk/terminal
- Optimal pour AMOLED (economie batterie)
- Meilleure lisibilite des donnees hex
- Public cible habitue aux interfaces sombres

---

## 3. Typographie

### Police principale : Exo 2
- **Usage** : Titres, navigation, boutons, labels
- **Poids** : 400 (Regular), 500 (Medium), 600 (SemiBold), 700 (Bold)
- **Caractere** : Geometrique, futuriste, tech

### Police monospace : JetBrains Mono
- **Usage** : Donnees hex, UID, cles, editeur, logs
- **Poids** : 400 (Regular), 700 (Bold)
- **Caractere** : Lisible, optimise pour le code et les donnees

### Hierarchie typographique

| Element | Police | Taille | Poids | Couleur |
|---------|--------|--------|-------|---------|
| H1 (ecran titre) | Exo 2 | 28sp | Bold | `#E0E0E0` |
| H2 (section) | Exo 2 | 22sp | SemiBold | `#E0E0E0` |
| H3 (sous-section) | Exo 2 | 18sp | Medium | `#E0E0E0` |
| Body | Exo 2 | 16sp | Regular | `#E0E0E0` |
| Caption | Exo 2 | 12sp | Regular | `#808080` |
| Hex data | JetBrains Mono | 14sp | Regular | `#00FFFF` |
| UID / Cle | JetBrains Mono | 16sp | Bold | `#00FF41` |
| Bouton | Exo 2 | 14sp | SemiBold | `#000000` |

---

## 4. Iconographie

### Style d'icones
- **Set** : Material Symbols Outlined (coherent avec Material 3)
- **Poids** : 300 (light, style cyberpunk)
- **Taille** : 24dp (standard), 48dp (actions principales)
- **Couleur** : `#00FFFF` (actif), `#808080` (inactif)

### Icones specifiques NFC

| Action | Icone Material | Description |
|--------|---------------|-------------|
| Lecture NFC | `contactless` | Badge + ondes |
| Emulation | `tap_and_play` | Telephone + ondes |
| Badge sauvegarde | `credit_card` | Carte/badge |
| Editeur hex | `data_object` | Donnees brutes |
| Comparer | `compare_arrows` | Diff entre dumps |
| Dictionnaires | `key` | Cles de dechiffrement |
| Parametres | `settings` | Engrenage |
| Root | `security` | Bouclier |
| Import | `file_upload` | Import fichier |
| Export | `file_download` | Export fichier |

---

## 5. Composants UI

### Cartes de badge

```
+------------------------------------------+
|  [icone NFC]  Nom du badge          [>]  |
|  UID: AA:BB:CC:DD                        |
|  Type: Mifare Classic 1K                 |
|  Cles: 12/16 trouvees    [===========]  |
|  Derniere emulation: il y a 2h          |
+------------------------------------------+
```

- Fond : `#0D0D0D`
- Bordure : `#1F1F1F` avec glow subtil `#00FFFF` au hover
- Coins arrondis : 12dp
- Elevation : 0dp (flat, style cyberpunk)
- Padding : 16dp

### Boutons

| Type | Fond | Texte | Bordure | Usage |
|------|------|-------|---------|-------|
| Primary | `#00FFFF` | `#000000` | aucune | Action principale (Emuler) |
| Secondary | transparent | `#00FFFF` | `#00FFFF` 1px | Action secondaire (Lire) |
| Danger | `#FF3B30` | `#FFFFFF` | aucune | Supprimer |
| Ghost | transparent | `#808080` | aucune | Actions tertiaires |

### Barre de progression (cracking)

```
Secteur 3/16  [=====>---------]  19%
Cle trouvee : FF:FF:FF:FF:FF:FF
```

- Fond barre : `#1A1A1A`
- Progression : gradient `#00FFFF` -> `#00FF41`
- Texte : JetBrains Mono `#00FF41`

### Editeur hexadecimal

```
Bloc 00: AA BB CC DD  EE FF 00 11  22 33 44 55  66 77 88 99
Bloc 01: 00 00 00 00  00 00 FF 07  80 69 FF FF  FF FF FF FF
         ^^^^^^^^^^^                    ^^^^^^^^^^
         Donnees modifiees              Access bits
```

- Fond : `#000000`
- Donnees : JetBrains Mono `#00FFFF` 14sp
- Donnees modifiees : `#FF00FF` (magenta)
- Access bits : `#FFB800` (amber)
- Separateurs : `#1F1F1F`
- Grille : colonnes de 4 octets, espacement 8dp

---

## 6. Animations & Effets

### NFC Pulse (detection de tag)
- Cercles concentriques cyan qui s'expandent depuis le centre
- Duree : 1.5s, repeat
- Opacite : 1.0 -> 0.0
- Taille : 48dp -> 200dp

### Emulation active
- Glow vert subtil pulsant autour de l'icone du badge
- Duree : 2s, ease-in-out, repeat
- `box-shadow: 0 0 20px #00FF41`

### Cracking en cours
- Barre de progression animee avec particules
- Chaque cle trouvee = flash vert bref (150ms)

### Transitions d'ecran
- Slide horizontal 250ms, ease-out
- Fade in/out pour les modales 200ms

### Respect de `prefers-reduced-motion`
- Si active : desactiver pulse, glow, particules
- Conserver uniquement les transitions de base (fade)

---

## 7. Navigation

### Bottom Navigation Bar

```
+------+--------+---------+--------+----------+
| Home | Reader | Emulate | Editor | Settings |
+------+--------+---------+--------+----------+
```

- Fond : `#0D0D0D`
- Icone active : `#00FFFF` + label visible
- Icone inactive : `#808080` + label masque
- Separateur : `#1F1F1F` 1px en haut
- Hauteur : 64dp

### Ecrans principaux

| Ecran | Description |
|-------|-------------|
| **Home** | Liste des badges sauvegardes (cartes) |
| **Reader** | Ecran lecture NFC avec pulse animation |
| **Emulate** | Selection badge + activation emulation |
| **Editor** | Editeur hex + comparaison + analyse cles |
| **Settings** | Root status, dictionnaires, backup, securite |

---

## 8. Logo

### Description
Le logo combine un **bouclier** (securite) avec des **ondes NFC** (3 arcs concentriques) et un symbole de **lecture/ecriture** (fleches bidirectionnelles).

### Fichiers
- `assets/logo/nfc_emulator_logo.svg` — Logo vectoriel principal
- `assets/logo/nfc_emulator_icon.svg` — Icone app (sans texte)

### Regles d'utilisation
- Fond minimum : noir ou tres sombre (< `#1A1A1A`)
- Taille minimum : 32dp (icone), 120dp (logo complet)
- Espace de securite : 16dp autour du logo
- Ne jamais deformer, tourner ou changer les couleurs

---

## 9. Tokens Android (Compose)

```kotlin
object NfcColors {
    val Primary = Color(0xFF00FFFF)        // Neon Cyan
    val Secondary = Color(0xFF00FF41)       // Matrix Green
    val Accent = Color(0xFFFF00FF)          // Neon Magenta
    val Background = Color(0xFF000000)      // Deep Black
    val Surface = Color(0xFF0D0D0D)         // Dark Surface
    val SurfaceVariant = Color(0xFF1A1A1A)  // Charcoal
    val TextPrimary = Color(0xFFE0E0E0)     // Light Gray
    val TextSecondary = Color(0xFF808080)   // Medium Gray
    val Border = Color(0xFF1F1F1F)          // Dark Border
    val Error = Color(0xFFFF3B30)           // Alert Red
    val Warning = Color(0xFFFFB800)         // Amber
}

object NfcTypography {
    // Headings : Exo 2
    // Monospace : JetBrains Mono
}

object NfcDimensions {
    val CornerRadius = 12.dp
    val CardPadding = 16.dp
    val IconSize = 24.dp
    val IconSizeLarge = 48.dp
    val BottomNavHeight = 64.dp
    val TouchTarget = 48.dp
}
```
