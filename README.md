# SpesaFacile 🛒

**SpesaFacile** è un'applicazione Android nativa progettata per rendere la gestione della lista della spesa un'attività semplice, rapida e piacevole. L'app è completamente in **italiano** ed è stata sviluppata seguendo le linee guida del **Material 3 Design**, con un'attenzione particolare alla cura estetica e all'usabilità.

---

## 🎨 Design & Palette Colori
La palette colori dell'app è ispirata direttamente al logo ufficiale (`SpesaFacileLogo.png`), utilizzando tonalità calde e premium:
- **Arancione Primario**: `#ED840C` - Rappresenta il brand ed è usato per gli elementi principali.
- **Accenti**: `#E25615` - Tonalità secondaria per contrasto.
- **Sfondo**: `#FFFDFB` (chiaro) e `#1E1B18` (scuro) - Assicura un contrasto ottimale e supporta nativamente il **Tema Scuro (Dark Mode)**.
- **Categorie**: Colori associati a ciascuna categoria merceologica (Verde per frutta/verdura, Giallo per latticini, ecc.) per una navigazione visiva immediata.

---

## ✨ Funzionalità Principali

### 1. 📋 Liste Multiple
* Crea e gestisci più liste contemporaneamente (es. "Spesa Settimanale", "Cena con Amici").
* Rinomina o elimina le liste con un semplice tocco prolungato.
* Ogni lista mostra il numero di prodotti ancora da acquistare.

### 2. ⚡ Inserimento Rapido & Suggerimenti
* Barra di inserimento inline sempre visibile in cima alla lista.
* Suggerimenti automatici basati sui prodotti inseriti in precedenza per velocizzare la scrittura.
* Assegnazione rapida della categoria tramite chip orizzontali.

### 3. 🔢 Quantità e Unità di Misura
* Indica la quantità di ciascun prodotto con controlli rapidi `+` e `-`.
* Specifica l'unità di misura preferita (es. `pz`, `kg`, `g`, `l`, `ml`).

### 4. 📂 Suddivisione per Categoria
* Visualizza i prodotti in una lista piatta classica o **raggruppati per reparto/categoria** (es. Frutta e Verdura, Latticini, Carne e Pesce).
* Le sezioni delle categorie sono collassabili con animazioni fluide per tenere la lista ordinata.

### 5. 📱 Widget Home Screen
* Widget interattivo realizzato con **Jetpack Glance** da posizionare sulla home screen del telefono.
* Consente di visualizzare i prodotti da acquistare e di **spuntarli direttamente dalla home** senza dover aprire l'app.
* Pulsante rapido `+` per aprire l'app direttamente sul campo di inserimento.

---

## 🏗️ Architettura del Codice
Il progetto segue i pattern di sviluppo Android moderni consigliati da Google:
* **UI**: Jetpack Compose con componenti Material 3 per layout flessibili e reattivi.
* **Database**: Room Database per la persistenza locale dei dati con supporto coroutine Flow.
* **Pattern**: Model-View-ViewModel (MVVM) per una netta separazione tra logica di business e interfaccia utente.
* **Widget**: Jetpack Glance per un widget moderno ed efficiente.

---

## 🛠️ Come Compilare e Avviare il Progetto

### Prerequisiti
* Android Studio (Koala o successivo consigliato).
* JDK 17 o superiore.
* Android SDK 34+.

### Istruzioni
1. Clona il repository:
   ```bash
   git clone https://github.com/CosmoNetinfo/SpesaFacile.git
   ```
2. Apri il progetto in Android Studio.
3. Attendi il completamento della sincronizzazione di Gradle.
4. Avvia l'applicazione su un emulatore o dispositivo fisico tramite il pulsante **Run** o usando la riga di comando:
   ```bash
   ./gradlew installDebug
   ```
