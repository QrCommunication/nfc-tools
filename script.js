/* ============================================
   NFC Emulator — Landing Page
   i18n + Scroll Animations
   ============================================ */

const translations = {
  en: {
    "nav.features": "Features",
    "nav.how": "How it works",
    "nav.security": "Security",
    "nav.github": "GitHub",
    "hero.badge": "Android \u2022 Open Source",
    "hero.title.1": "NFC",
    "hero.title.2": "Emulator",
    "hero.tagline": "Read. Clone. Emulate.",
    "hero.desc": "The most advanced NFC toolkit for Android. Read Mifare Classic tags, crack keys with extended dictionaries, write to magic cards, and emulate via HCE \u2014 all in one app.",
    "hero.cta.download": "Download APK",
    "hero.cta.source": "View Source",
    "features.label": "Features",
    "features.title": "Everything you need for NFC",
    "features.desc": "A complete toolkit built with Jetpack Compose and Material Design 3.",
    "feat.read.title": "Read Tags",
    "feat.read.desc": "Read Mifare Classic 1K/4K tags sector by sector with automatic key detection and extended dictionary support.",
    "feat.write.title": "Write Magic Cards",
    "feat.write.desc": "Write complete dumps to CUID/Gen1a magic cards with proper authentication and block-by-block verification.",
    "feat.emulate.title": "HCE Emulation",
    "feat.emulate.desc": "Emulate NFC tags via Host Card Emulation. Full emulation available with root + NXP chipset.",
    "feat.editor.title": "Hex Editor",
    "feat.editor.desc": "View and edit raw hex data with color-coded keys, access bits, and sector trailer highlighting.",
    "feat.keys.title": "Key Cracking",
    "feat.keys.desc": "Crack unknown keys using 9+ built-in dictionaries including Flipper Zero, Proxmark, and VIGIK databases.",
    "feat.encrypt.title": "Encrypted Storage",
    "feat.encrypt.desc": "All saved tags are encrypted with AES-256. Export and import encrypted backups with one tap.",
    "how.label": "How it works",
    "how.title": "Three simple steps",
    "how.desc": "From physical tag to digital clone in seconds.",
    "how.1.title": "Scan",
    "how.1.desc": "Hold your phone near a Mifare Classic tag. The app reads all sectors and cracks keys automatically.",
    "how.2.title": "Save",
    "how.2.desc": "The complete dump is saved and encrypted locally. View hex data, keys, and access bits in the editor.",
    "how.3.title": "Clone",
    "how.3.desc": "Write the dump to a magic card or emulate it directly via HCE. Your tag is now digital.",
    "security.label": "Security",
    "security.title": "Built with security first",
    "security.desc": "Your data stays on your device, encrypted and private.",
    "sec.aes.title": "AES-256 Encryption",
    "sec.aes.desc": "All tag data is encrypted at rest using hardware-backed AES-256.",
    "sec.local.title": "Local Storage Only",
    "sec.local.desc": "No cloud, no accounts, no telemetry. Everything stays on your device.",
    "sec.export.title": "Secure Backups",
    "sec.export.desc": "Export encrypted backup files. Import on any device with your key.",
    "sec.open.title": "Open Source",
    "sec.open.desc": "Full source code available on GitHub. Audit it yourself.",
    "tech.label": "Tech Stack",
    "tech.title": "Built with modern tools",
    "cta.title": "Ready to go digital?",
    "cta.desc": "Download NFC Emulator and take control of your NFC tags.",
    "footer.license": "MIT License",
    "footer.privacy": "Privacy",
    "footer.issues": "Issues"
  },
  fr: {
    "nav.features": "Fonctions",
    "nav.how": "Comment \u00e7a marche",
    "nav.security": "S\u00e9curit\u00e9",
    "nav.github": "GitHub",
    "hero.badge": "Android \u2022 Open Source",
    "hero.title.1": "NFC",
    "hero.title.2": "Emulator",
    "hero.tagline": "Lire. Cloner. \u00c9muler.",
    "hero.desc": "La bo\u00eete \u00e0 outils NFC la plus avanc\u00e9e pour Android. Lisez les tags Mifare Classic, craquez les cl\u00e9s, \u00e9crivez sur des cartes magiques et \u00e9mulez via HCE \u2014 tout en une seule app.",
    "hero.cta.download": "T\u00e9l\u00e9charger l\u2019APK",
    "hero.cta.source": "Voir le code",
    "features.label": "Fonctionnalit\u00e9s",
    "features.title": "Tout ce qu\u2019il faut pour le NFC",
    "features.desc": "Une bo\u00eete \u00e0 outils compl\u00e8te construite avec Jetpack Compose et Material Design 3.",
    "feat.read.title": "Lecture de tags",
    "feat.read.desc": "Lisez les tags Mifare Classic 1K/4K secteur par secteur avec d\u00e9tection automatique des cl\u00e9s et dictionnaires \u00e9tendus.",
    "feat.write.title": "\u00c9criture Magic Cards",
    "feat.write.desc": "\u00c9crivez des dumps complets sur des cartes CUID/Gen1a avec authentification et v\u00e9rification bloc par bloc.",
    "feat.emulate.title": "\u00c9mulation HCE",
    "feat.emulate.desc": "\u00c9mulez des tags NFC via Host Card Emulation. \u00c9mulation compl\u00e8te disponible avec root + chipset NXP.",
    "feat.editor.title": "\u00c9diteur Hex",
    "feat.editor.desc": "Visualisez et modifiez les donn\u00e9es hex brutes avec coloration des cl\u00e9s, bits d\u2019acc\u00e8s et trailers.",
    "feat.keys.title": "Craquage de cl\u00e9s",
    "feat.keys.desc": "Craquez les cl\u00e9s inconnues avec 9+ dictionnaires int\u00e9gr\u00e9s : Flipper Zero, Proxmark, VIGIK et plus.",
    "feat.encrypt.title": "Stockage chiffr\u00e9",
    "feat.encrypt.desc": "Tous les tags sont chiffr\u00e9s en AES-256. Exportez et importez des sauvegardes chiffr\u00e9es en un tap.",
    "how.label": "Comment \u00e7a marche",
    "how.title": "Trois \u00e9tapes simples",
    "how.desc": "Du tag physique au clone num\u00e9rique en quelques secondes.",
    "how.1.title": "Scanner",
    "how.1.desc": "Approchez votre t\u00e9l\u00e9phone d\u2019un tag Mifare Classic. L\u2019app lit tous les secteurs et craque les cl\u00e9s automatiquement.",
    "how.2.title": "Sauvegarder",
    "how.2.desc": "Le dump complet est sauvegard\u00e9 et chiffr\u00e9 localement. Consultez les donn\u00e9es hex, cl\u00e9s et bits d\u2019acc\u00e8s.",
    "how.3.title": "Cloner",
    "how.3.desc": "\u00c9crivez le dump sur une carte magique ou \u00e9mulez-le directement via HCE. Votre tag est d\u00e9sormais num\u00e9rique.",
    "security.label": "S\u00e9curit\u00e9",
    "security.title": "Con\u00e7u avec la s\u00e9curit\u00e9 en priorit\u00e9",
    "security.desc": "Vos donn\u00e9es restent sur votre appareil, chiffr\u00e9es et priv\u00e9es.",
    "sec.aes.title": "Chiffrement AES-256",
    "sec.aes.desc": "Toutes les donn\u00e9es sont chiffr\u00e9es au repos avec AES-256 mat\u00e9riel.",
    "sec.local.title": "Stockage local uniquement",
    "sec.local.desc": "Pas de cloud, pas de comptes, pas de t\u00e9l\u00e9m\u00e9trie. Tout reste sur votre appareil.",
    "sec.export.title": "Sauvegardes s\u00e9curis\u00e9es",
    "sec.export.desc": "Exportez des fichiers de sauvegarde chiffr\u00e9s. Importez sur tout appareil.",
    "sec.open.title": "Open Source",
    "sec.open.desc": "Code source complet disponible sur GitHub. Auditez-le vous-m\u00eame.",
    "tech.label": "Stack technique",
    "tech.title": "Construit avec des outils modernes",
    "cta.title": "Pr\u00eat \u00e0 passer au num\u00e9rique ?",
    "cta.desc": "T\u00e9l\u00e9chargez NFC Emulator et prenez le contr\u00f4le de vos tags NFC.",
    "footer.license": "Licence MIT",
    "footer.privacy": "Confidentialit\u00e9",
    "footer.issues": "Signaler un bug"
  }
};

let currentLang = (navigator.language || "en").startsWith("fr") ? "fr" : "en";

function setLang(lang) {
  currentLang = lang;
  document.documentElement.lang = lang;

  document.querySelectorAll("[data-i18n]").forEach(el => {
    const key = el.getAttribute("data-i18n");
    if (translations[lang][key]) {
      el.textContent = translations[lang][key];
    }
  });

  document.querySelectorAll(".lang-toggle button").forEach(btn => {
    btn.classList.toggle("active", btn.dataset.lang === lang);
  });

  try { localStorage.setItem("nfc-lang", lang); } catch {}
}

/* --- Scroll reveal --- */
function initScrollReveal() {
  const observer = new IntersectionObserver(
    entries => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          entry.target.classList.add("visible");
          observer.unobserve(entry.target);
        }
      });
    },
    { threshold: 0.15, rootMargin: "0px 0px -40px 0px" }
  );

  document.querySelectorAll(".reveal").forEach(el => observer.observe(el));
}

/* --- Smooth nav active state --- */
function initNavHighlight() {
  const sections = document.querySelectorAll("section[id]");
  const navLinks = document.querySelectorAll(".nav__links a[href^='#']");

  const observer = new IntersectionObserver(
    entries => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          const id = entry.target.id;
          navLinks.forEach(link => {
            link.style.color = link.getAttribute("href") === `#${id}`
              ? "var(--md-primary)"
              : "";
          });
        }
      });
    },
    { threshold: 0.3 }
  );

  sections.forEach(s => observer.observe(s));
}

/* --- Init --- */
document.addEventListener("DOMContentLoaded", () => {
  const saved = localStorage.getItem("nfc-lang");
  setLang(saved || currentLang);
  initScrollReveal();
  initNavHighlight();

  document.querySelectorAll(".lang-toggle button").forEach(btn => {
    btn.addEventListener("click", () => setLang(btn.dataset.lang));
  });
});
