# NFC Emulator — Implementation Plan

## Phase 1 : Foundation (parallele)
- [1.1] Project setup : Gradle, AndroidManifest, structure dossiers
- [1.2] Data models : TagDump, Sector, Block, enums, DumpFormat
- [1.3] Theme Compose : NfcColors, NfcTypography, NfcTheme, NfcDimensions
- [1.4] Dictionnaires de cles : 16 fichiers .txt dans assets/dictionaries/

## Phase 2 : Core (parallele, depend Phase 1)
- [2.1] Room Database : TagEntity, TagDao, AppDatabase
- [2.2] NFC HAL : interface NfcEmulatorHal, HceEmulatorHal, RootNxpEmulatorHal
- [2.3] NFC Reader : TagReader, TagTypeDetector, detection automatique
- [2.4] Dump Parsers : MfdParser, MctParser, BinParser, DumpParser, JsonExporter
- [2.5] Key Cracker : DictionaryManager, KeyCracker, moteur progressif
- [2.6] Crypto Storage : CryptoManager (AES-256-GCM + KeyStore), EncryptedFileManager

## Phase 3 : UI Screens (parallele, depend Phase 2)
- [3.1] Home screen : liste des badges, recherche, categories
- [3.2] Reader screen : lecture NFC avec pulse animation
- [3.3] Emulator screen : selection badge + activation emulation
- [3.4] Editor screen : editeur hexadecimal + comparaison + analyse cles
- [3.5] Settings screen : root status, dictionnaires, backup, securite

## Phase 4 : Integration
- [4.1] Navigation : Compose Navigation, bottom nav bar
- [4.2] DI : Koin modules
- [4.3] HCE Service : HostApduService Android
- [4.4] MainActivity + App entry point
