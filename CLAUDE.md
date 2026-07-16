# Hinweise für die Weiterentwicklung (WICHTIG)

Dieses Projekt läuft sowohl auf dem **virtuellen Furhat** (Start aus IntelliJ) als
auch auf dem **echten Roboter** (hochgeladene `Furhat.skill` / Shadow-Jar).
Der echte Roboter ist die kritische Umgebung: Was dort funktioniert, ist nicht
selbstverständlich, weil die `.skill` isoliert läuft (kein Zugriff auf das
Dateisystem des Entwicklungsrechners, evtl. altes Java, kein Windows-Zertifikats-
speicher).

**Die folgenden drei Dinge müssen immer funktionieren — bitte beim Ändern nicht
kaputt machen.**

## 1. API-Schlüssel / `.env` kommt mit auf den Roboter

Auf dem echten Roboter gibt es keine `.env` im Dateisystem. Damit der Schlüssel
ankommt:

- `build.gradle` → `shadowJar` packt die `.env` (falls vorhanden) in die `.skill`
  (`from('.env')`). Die `.env` bleibt durch `.gitignore` von GitHub
  ausgeschlossen.
- `EnvConfig.kt` sucht Schlüssel/Modell in dieser Reihenfolge: System-Umgebungs-
  variable → System-Property → `.env` im Dateisystem → in die Jar eingepackte
  `/.env` (Classpath).

Niemals den Schlüssel hart in den Code schreiben. Wer einen Build mit eingepacktem
Schlüssel weitergibt, gibt auch den Schlüssel weiter → `.skill` mit Schlüssel nicht
verteilen.

## 2. HTTPS-Zertifikate (SSL) müssen auch auf dem Roboter vertraut werden

Symptom bei Verstoß: `PKIX path building failed ... unable to find valid
certification path`. Ursache: Das Java des Roboters kennt das Stammzertifikat des
KI-Servers nicht; einen Windows-Zertifikatsspeicher gibt es dort nicht.

- `build.gradle` → Task `downloadCaCerts` lädt beim Bauen einen aktuellen
  Standard-Zertifikatssatz (`cacert.pem`, Mozilla/curl) herunter; `shadowJar` packt
  ihn unter `certs/` in die `.skill`. Der Download läuft auf dem Entwicklungs-PC,
  nicht auf dem Roboter.
- `TrustConfig.kt` kombiniert drei Vertrauensquellen (Standard-Java + Windows +
  eingepackte Zertifikate) und wird über `installGlobally()` **JVM-weit** gesetzt.
- `main.kt` → `FurhatSkill.start()` ruft `TrustConfig.installGlobally()` als
  Allererstes auf (bevor irgendeine HTTPS-Verbindung aufgebaut wird).
- Escape-Luke für Uni-/Firmen-Proxys, die HTTPS aufbrechen: eine Datei
  `extra-cert.pem` im Projektordner wird mit eingepackt und ebenfalls vertraut.

Beim Bauen sollte in der Konsole `[downloadCaCerts] geladen: cacert.pem (...)`
stehen, beim Start auf dem Roboter `[TrustConfig] Zusaetzliche Stammzertifikate
JVM-weit aktiviert.`.

## 3. KI-Aufrufe dürfen den Flow nicht blockieren und nicht abstürzen

Symptome bei Verstoß: `A flow's queue is full` und der Roboter „friert ein"
(redet nach einer längeren/langsameren Antwort nicht mehr weiter).

- Der KI-Aufruf (`llm.ask(...)`) läuft in `conversation.kt` in einem **Hintergrund-
  Thread**; das Ergebnis kommt per benanntem Event (`send("LLM_REPLY_READY")` →
  `onEvent(...)`) zurück in den Flow. Den Aufruf **nie** direkt synchron in
  `onEntry`/`onResponse`/`onNoResponse` machen.
- `send(...)` braucht den `FlowControlRunner` als Empfänger — dieser wird aus dem
  Trigger an die Hintergrund-Funktion übergeben (im Thread selbst ist er sonst nicht
  verfügbar). `raise(...)` funktioniert hier NICHT (nur für Intents/Responses).
- `UniProvider.ask` und `OpenAIProvider.ask` sind fehlertolerant: der gesamte
  Netzwerk-Aufruf ist in `try/catch`. `ask()` wirft **nie** eine Exception, sondern
  liefert im Problemfall einen Ersatzsatz. Das muss so bleiben, sonst bricht der
  Gesprächs-Flow ab.

## 4. Betriebsmodi und Steuerpult (Weboberfläche)

Es gibt zwei Modi (`BETRIEBS_MODUS` in `Einstellungen.kt`): `STUDIE` (wie das
ursprüngliche, standardisierte Verhalten) und `MESSE` (Auto-Start, fester
Einleitungssatz, Timer aus, Steuerung per Weboberfläche).

- Laufzeit-Status liegt in `steuerung/Steuerung.kt` (Zustand, Modus-Schalter,
  Zeitmessung mit Pause). Voreinstellungen je Modus: `Steuerung.initFromMode()`,
  aufgerufen in `init.kt`.
- Die Weboberfläche ist `steuerung/SteuerServer.kt` (`com.sun.net.httpserver`),
  gestartet aus `init.kt` über `starteSteuerpult(sendeEvent, port)`. Sie löst
  Events aus: `STEUER_START`, `STEUER_PAUSE`, `STEUER_WEITER`, `STEUER_BEENDEN`.
- Wichtig zum Thread-/Empfänger-Thema: Der Server bekommt einen
  `sendeEvent: (String) -> Unit`-Callback, der in `init.kt` im Flow-Kontext als
  `{ name -> send(name) }` erzeugt wird. Grund: `send`/`goto`/`furhat` brauchen
  den Flow-Receiver und sind in normalen Funktionen/Threads NICHT direkt
  verfügbar. Im Gesprächs-Flow (`conversation.kt`) wird derselbe Trick benutzt
  (Callback aus dem Trigger), statt einen `FlowControlRunner` herumzureichen.
- `conversation.kt` behandelt `STEUER_*`-Events, Einleitungs-/Schluss-Modus
  (fest vs. KI), den optionalen Timer und setzt nach dem Ende den Verlauf
  zurück (`MessageHistory.reset()`); MESSE → zurück zu `Idle`, STUDIE →
  `EndState`.

Beim Start sollte im Log `[Steuerpult] Weboberflaeche laeuft auf Port 8088`
stehen.

## Nach jeder Änderung

Für den echten Roboter immer `shadowJar` **neu bauen** und die neue `.skill`
hochladen. Änderungen am Code wirken sonst nicht.

## Anmerkung zum Stil

Das Projekt richtet sich an Menschen ohne Programmierkenntnisse. Anpassbare Werte
liegen bewusst in `_HIER_ANPASSEN/` (`Einstellungen.kt`, `PromptConfig.kt`),
Kommentare und README sind auf Deutsch. Diesen Stil bei Erweiterungen beibehalten.
