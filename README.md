# SpesaFacile 🛒

**La tua lista della spesa, sempre con te.** SpesaFacile è un’app Android nativa in italiano: organizza i prodotti in liste e reparti, indica le quantità e spunta ciò che hai già acquistato.

Il progetto funziona con un database locale: per creare e consultare le liste non serve un account né una connessione Internet.

[Scarica l’ultima release](https://github.com/CosmoNetinfo/SpesaFacile/releases/latest) · [Landing page](https://cosmonetinfo.github.io/SpesaFacile-landing-page/) · [Segnala un problema](https://github.com/CosmoNetinfo/SpesaFacile/issues)

> **Stato della versione:** questo codice contiene la **1.1.1** (`versionCode = 3`). L’APK presente in Releases può essere precedente: verifica il numero della release prima di aspettarti tutte le funzioni descritte qui. Una modifica al codice non pubblica automaticamente una nuova versione.

## Cosa puoi fare

| Funzione | Comportamento |
| --- | --- |
| Liste multiple | Crea, rinomina, duplica ed elimina le liste. La home distingue liste vuote, completate e prodotti ancora da comprare. |
| Inserimento rapido | Scrivi il nome e premi `+` oppure il tasto di conferma della tastiera. Espandi i dettagli soltanto quando servono. |
| Suggerimenti persistenti | I prodotti usati rimangono nello storico anche dopo la cancellazione degli acquistati. Selezionando un suggerimento recuperi nome, categoria e unità. |
| Quantità decimali | Inserisci `0,5 kg`, `1,5 l` o quantità intere; sono accettati sia virgola sia punto. |
| Modifica prodotti | Tocca il nome di un prodotto per correggere nome, quantità, unità, reparto e prezzo. |
| Categorie | Passa dalla vista piatta alla vista per reparto. Ogni reparto può essere compresso. |
| Acquistati | Gli articoli spuntati vengono separati in fondo alla lista; la sezione può essere realmente chiusa e riaperta. |
| Cancellazioni | Per eliminare una lista o svuotare gli acquistati serve una conferma. Dopo l’eliminazione di un singolo prodotto puoi premere **Annulla** nella barra in basso. |
| Duplicazione | Il menu della lista crea una copia con gli stessi prodotti, quantità e prezzi, tutti nuovamente da acquistare. |
| Condivisione | Invia una copia testuale della lista con il pannello Android, ad esempio tramite WhatsApp o email. |
| Prezzi facoltativi | Registra il prezzo per unità e visualizza il totale stimato. I prezzi mancanti vengono segnalati e non sono inclusi nel totale. |
| Widget interattivo | Consulta una lista sulla home, cambia lista e spunta i prodotti. Il pulsante `+` apre la lista corretta con il campo di inserimento attivo. |
| Tema chiaro e scuro | L’aspetto segue il tema del dispositivo, mantenendo la palette arancione del progetto. |
| Controllo aggiornamenti | L’app consulta l’ultima release pubblica su GitHub e propone il download solo quando la versione è più recente. |

## Installazione su Android

1. Apri [l’ultima release](https://github.com/CosmoNetinfo/SpesaFacile/releases/latest).
2. Espandi **Assets** e scarica il file `.apk` della versione desiderata.
3. Apri il file sul telefono. Se Android lo richiede, autorizza l’installazione delle app da quella specifica sorgente, ad esempio il browser.
4. Avvia **SpesaFacile** e crea la prima lista con `+`.

È necessario **Android 8.0 / API 26 o superiore**. Non sono disponibili una versione iOS o un’app web equivalente.

Gli APK debug delle build di sviluppo si trovano negli artefatti di GitHub Actions. Servono per le verifiche e sono separati dagli APK release firmati.

### Aggiornare un’installazione esistente

Android richiede lo stesso identificativo applicativo, una firma compatibile e un `versionCode` crescente. Il progetto mantiene `com.example.spesafacile` per non cambiare l’identità dell’app esistente.

Le prime release del repository distribuivano APK debug: la relativa chiave potrebbe non coincidere con la futura firma di produzione. **Non disinstallare l’app per risolvere un conflitto di firma se devi conservare le liste:** la disinstallazione normalmente rimuove i dati locali. Occorre prima verificare la chiave usata dall’APK installato e pianificare il passaggio. Non è presente una funzione manuale di esportazione/importazione del database.

## Uso quotidiano

### Crea e organizza le liste

- Nella home premi `+`, scrivi un nome e salva.
- Apri il menu `⋮` della lista per rinominarla, duplicarla o eliminarla.
- La duplicazione crea una lista indipendente, senza sincronizzazione con l’originale.

### Aggiungi o modifica un prodotto

- Scrivi il nome nella barra compatta e premi `+`.
- Dopo almeno due caratteri possono comparire suggerimenti dallo storico.
- Espandi la freccia per scegliere reparto, quantità, unità e prezzo.
- Le unità disponibili sono `pz`, `kg`, `g`, `l`, `ml`, oppure nessuna.
- Tocca il nome di un prodotto già inserito per modificarlo.

La quantità deve essere maggiore di zero e al massimo 1.000.000. Il prezzo è facoltativo, non negativo, con al massimo due decimali e limite di 1.000.000 €. Il prezzo si riferisce all’unità selezionata: `0,5 kg × 2,00 €/kg = 1,00 €`.

I prezzi sono conservati come centesimi interi. Il totale usa calcoli decimali, moltiplica prezzo per quantità e arrotonda la somma ai centesimi. È una stima: non gestisce promozioni, sconti o il totale dello scontrino.

### Spunta, cancella e condividi

Spunta un prodotto per spostarlo tra gli acquistati. Tocca l’intestazione **Acquistati** per nasconderli o mostrarli. Il cestino in alto elimina gli acquistati solo dopo conferma, conservando lo storico dei suggerimenti.

Per eliminare un prodotto usa il cestino della riga o trascina la riga verso sinistra. La barra **Annulla** ripristina l’ultimo prodotto eliminato mentre il messaggio è visibile; non è un cestino permanente.

La condivisione invia testo con nomi, quantità e stato di acquisto, aggiungendo il totale se sono presenti prezzi. Non consente ad altri di modificare in tempo reale la tua lista.

## Widget sulla schermata iniziale

1. Tieni premuto uno spazio libero della home Android e apri **Widget**.
2. Cerca SpesaFacile e aggiungi il widget.
3. Se hai più liste, usa **Cambia lista** per scorrerle; ogni widget conserva la propria selezione.
4. Spunta un prodotto direttamente dal widget.
5. Tocca il titolo per aprire la lista oppure `+` per aggiungere un prodotto.

Il widget osserva i dati locali e viene aggiornato dopo le modifiche dell’app. È previsto anche un aggiornamento periodico richiesto al launcher; i tempi effettivi dipendono da Android. Se una lista selezionata viene eliminata, il widget usa una lista disponibile. Una lista vuota e una lista completata mostrano messaggi diversi.

## Dati e connessioni

- Liste, prodotti e suggerimenti sono salvati sul dispositivo tramite Room/SQLite.
- Non sono presenti un account, un backend applicativo, pubblicità o analytics integrati.
- L’app usa il permesso `INTERNET` per il controllo delle release su GitHub; per il download apre il browser.
- Non è implementata una sincronizzazione cloud o familiare.
- Il backup Android dipende dalle impostazioni del sistema e del dispositivo; non sostituisce un’esportazione manuale dell’utente.

## Architettura

```text
Schermate Compose → ViewModel → ShoppingRepository → Room / SQLite
Widget Glance ────────────────────────────────┘
```

| Percorso | Responsabilità |
| --- | --- |
| `data/model/` | Entità liste, prodotti e storico dei suggerimenti; enum delle categorie. |
| `data/dao/` | Query Room, conteggi aggregati e aggiornamenti mirati dei prodotti. |
| `data/AppDatabase.kt` | Database singleton e migrazione dalla versione 1 alla 2. |
| `data/ShoppingRepository.kt` | Validazione, transazioni, duplicazione e aggiornamento storico. |
| `data/AppContainer.kt` | Creazione del repository e aggiornamento dei widget dopo le scritture. |
| `ui/viewmodel/` | Flussi delle schermate, operazioni e messaggi di errore. |
| `ui/screens/` | Home delle liste e dettaglio della spesa. |
| `ui/components/` | Inserimento, editor, righe dei prodotti e sezioni per reparto. |
| `ui/navigation/` | Navigazione e collegamenti `spesafacile://shopping/{listId}?add=true`. |
| `widget/` | Widget Glance, selezione lista e callback delle spunte. |
| `util/` | Parsing di quantità/prezzi, totali, testo di condivisione e controllo versioni. |
| `theme/` | Colori e tipografia Material 3. |

Le schermate raccolgono i flussi rispettando il ciclo di vita Android. La home usa una query aggregata per i conteggi, evitando un nuovo flusso per ciascuna card a ogni ricomposizione. Le operazioni su più tabelle sono transazionali. Gli aggiornamenti di spunta e quantità sono mirati, per evitare di riscrivere dati non correlati.

### Migrazione del database

Lo schema **2** aggiunge quantità `REAL`, prezzi facoltativi e la tabella `product_suggestions`. La migrazione `1 → 2` conserva ID, appartenenza alle liste, categorie, quantità precedenti e stato degli acquistati; inizializza inoltre lo storico dai prodotti esistenti.

Non viene usata una migrazione distruttiva. Gli schemi Room vengono esportati in `app/schemas/` e vanno versionati. Per future modifiche al database aggiungi una migrazione esplicita e aggiorna i test.

## Ambiente di sviluppo

- JDK **17**.
- Android SDK **36**, Build Tools **36.0.0** e Platform Tools.
- Android Studio compatibile con Android Gradle Plugin **9.0.1**.
- Gradle **9.1.0**, fornito dal wrapper: non serve un’installazione globale di Gradle.
- Un dispositivo o emulatore Android API 26+ per eseguire l’app; la CI usa API 34 per i test strumentali.

Le versioni delle dipendenze sono centralizzate in `gradle/libs.versions.toml`.

```bash
git clone https://github.com/CosmoNetinfo/SpesaFacile.git
cd SpesaFacile
```

Apri il progetto in Android Studio e attendi la sincronizzazione. Configura il percorso SDK con `ANDROID_HOME` oppure in `local.properties` (file non versionato):

```properties
sdk.dir=/percorso/Android/Sdk
```

Su Windows usa un percorso come `sdk.dir=C:/Users/NOME/AppData/Local/Android/Sdk`. Assicurati che `JAVA_HOME` punti al JDK 17.

### Comandi principali

| Operazione | macOS / Linux | Windows PowerShell |
| --- | --- | --- |
| Compila debug | `./gradlew assembleDebug` | `.\gradlew.bat assembleDebug` |
| Installa debug | `./gradlew installDebug` | `.\gradlew.bat installDebug` |
| Test unitari | `./gradlew testDebugUnitTest` | `.\gradlew.bat testDebugUnitTest` |
| Test su dispositivo | `./gradlew connectedDebugAndroidTest` | `.\gradlew.bat connectedDebugAndroidTest` |
| Analisi Android | `./gradlew lintDebug` | `.\gradlew.bat lintDebug` |

L’APK debug viene creato in `app/build/outputs/apk/debug/app-debug.apk`. I report sono in `app/build/reports/`.

## Test e integrazione continua

La CI separa la verifica del codice dalla pubblicazione:

- **Android CI:** esegue test unitari, compilazione dei test Android, analisi Android e build debug. Salva APK e report come artefatti.
- **Device tests:** esegue test Compose e database su emulatore API 34, compresa la migrazione da dati della versione 1.
- **Signed Android release:** viene eseguita solo su un tag `v*`, verifica la corrispondenza con `versionName`, controlla i segreti di firma e pubblica l’APK release con checksum SHA-256.

I test coprono parsing italiano, valori invalidi, importi esatti, arrotondamenti, condivisione, confronto delle versioni, modifica dei prodotti, sezioni comprimibili, duplicazione, ripristino, conservazione dello storico, cancellazione a cascata e migrazione.

## Pubblicare una release firmata

### Configurazione iniziale

Usa una chiave di firma stabile, custodita con le relative password. Non inserire keystore o password nel repository. Se l’app è già distribuita, verifica prima la compatibilità con la firma precedente.

Configura questi **repository secrets** in GitHub → Settings → Secrets and variables → Actions:

| Secret | Contenuto |
| --- | --- |
| `SPESA_KEYSTORE_BASE64` | Keystore di produzione codificato in Base64. |
| `SPESA_STORE_PASSWORD` | Password del keystore. |
| `SPESA_KEY_ALIAS` | Alias della chiave. |
| `SPESA_KEY_PASSWORD` | Password della chiave. |

La pipeline ricostruisce il keystore temporaneamente, verifica la firma dell’APK e rimuove il file al termine. In mancanza di segreti si arresta senza pubblicare un APK debug come release.

Per compilare localmente una release firmata, imposta le variabili `SPESA_KEYSTORE_PATH`, `SPESA_STORE_PASSWORD`, `SPESA_KEY_ALIAS` e `SPESA_KEY_PASSWORD`, poi esegui `assembleRelease`. Senza queste variabili la build release locale è **non firmata**, utile solo per verifiche tecniche.

### Ogni nuova versione

1. Incrementa `versionCode` e aggiorna `versionName` in `app/build.gradle.kts`.
2. Aggiorna la documentazione e completa le verifiche CI, inclusi i test sul dispositivo.
3. Crea un tag corrispondente esattamente alla versione, per esempio `v1.1.1`.
4. Pubblica il tag: il workflow crea una release con `SpesaFacile-v1.1.1.apk` e `SHA256SUMS.txt`.

Non riutilizzare un tag esistente per distribuire una build diversa. Il controllo aggiornamenti legge `BuildConfig.VERSION_NAME`: non c’è una seconda costante da aggiornare manualmente.

## Landing page

La pagina promozionale è mantenuta separatamente in [SpesaFacile-landing-page](https://github.com/CosmoNetinfo/SpesaFacile-landing-page) ed è predisposta per **GitHub Pages**. Contiene una demo illustrativa interattiva, FAQ e link all’ultima release. Le funzioni in preparazione sono distinte da quelle già distribuite.

## Segnalazioni e limiti attuali

Per un bug indica versione dell’app, versione Android, dispositivo, passaggi per riprodurlo e risultato atteso. Evita di includere dati personali nelle segnalazioni pubbliche.

Limiti attuali: categorie predefinite, nessuna collaborazione in tempo reale, nessun import/export manuale del database e nessuna versione iOS. La condivisione è testuale. Il tema segue Android. La firma di produzione deve essere configurata prima di distribuire la prima release firmata.

Il repository non contiene attualmente un file `LICENSE`: la disponibilità del codice su GitHub non assegna automaticamente una licenza di riutilizzo.
