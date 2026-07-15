# Furhat-Gesprächsroboter (Prompting)

Software für einen sozialen Roboter (Furhat), der ein lockeres Smalltalk-Gespräch führt. Der Roboter antwortet automatisch mithilfe einer KI (Sprachmodell). Diese Version stammt aus der Masterarbeit von Josephine Marie Faasen (Universität Duisburg-Essen, 2026) und wurde für die einfache Wiederverwendung aufbereitet.

## Diese Anleitung ist für Menschen ohne Programmierkenntnisse

Sie müssen nichts programmieren. Sie laden das Projekt herunter, installieren zwei Programme, tragen einen Zugangsschlüssel ein und drücken auf Start. Wenn Sie etwas am Verhalten des Roboters ändern möchten, gibt es genau zwei Dateien, die Sie anpassen müssen. Alles andere bleibt, wie es ist.

Nehmen Sie sich beim ersten Mal etwa eine Stunde Zeit, vor allem für die Installationen. Gehen Sie die Teile einfach der Reihe nach durch.

Kurzüberblick, was am Ende passiert: Der Roboter sieht eine Person, beginnt ein Gespräch, hört zu und antwortet mithilfe der KI. Nach einer einstellbaren Zeit verabschiedet er sich.

---

## Teil 0: Das Projekt auf den Computer holen

Sie brauchen zuerst die Projektdateien auf Ihrem Rechner. Es gibt zwei Wege. Weg A ist der einfachste.

### Weg A: Als ZIP herunterladen (einfachste Variante)

1. Öffnen Sie die Projektseite auf GitHub im Browser.
2. Klicken Sie auf den grünen Knopf **"Code"**.
3. Wählen Sie **"Download ZIP"**.
4. Öffnen Sie die heruntergeladene ZIP-Datei und **entpacken** Sie sie an einen Ort, den Sie wiederfinden (z.B. Dokumente). Sie haben jetzt einen Ordner mit allen Projektdateien.

### Weg B: Mit Git "klonen" (nur falls Sie Git nutzen möchten)

1. Installieren Sie Git von https://git-scm.com falls noch nicht vorhanden.
2. Öffnen Sie ein Terminal (Windows: "Git Bash" oder "Eingabeaufforderung"; Mac: "Terminal").
3. Wechseln Sie in den Ordner, in dem das Projekt landen soll, z.B.:
   ```
   cd Dokumente
   ```
4. Führen Sie aus (die Adresse bekommen Sie auf der GitHub-Seite über "Code" -> "HTTPS"):
   ```
   git clone https://github.com/DEIN-NAME/furhat-prompting.git
   ```

In beiden Fällen haben Sie danach einen Projektordner namens `furhat-prompting` auf dem Computer. Den brauchen Sie in Teil 2.

---

## Teil 1: Diese zwei Programme installieren (einmalig)

Mehr als diese zwei brauchen Sie nicht. Eine separate Java-Installation ist nicht nötig, denn die Furhat SDK bringt das passende Java selbst mit (siehe Hinweis unten).

### 1. IntelliJ IDEA (kostenlos nutzbar)

Das ist das Programm, in dem Sie das Projekt öffnen, einstellen und starten.

1. Gehen Sie auf https://www.jetbrains.com/idea/download/ und wählen Sie oben Ihr Betriebssystem (Windows, macOS oder Linux).
2. Klicken Sie einfach auf den blauen **"Download"**-Knopf.
3. Installieren Sie das Programm mit den Standardeinstellungen.

> **Sie müssen nichts abonnieren und nichts bezahlen.** Unter dem Download-Knopf steht ein kleiner Hinweis wie "Free – with a 30-day Ultimate subscription trial included". Diesen können Sie ignorieren. Die kostenlosen Funktionen für Java und Kotlin reichen für dieses Projekt vollständig aus, auch nach Ablauf der 30 Tage. Es entstehen keine Kosten, solange Sie kein Ultimate-Abo abschließen.

### 2. Furhat SDK (der Roboter-Simulator)

Damit können Sie den Roboter am Computer testen, auch ohne den echten Roboter ("Virtual Furhat"). Die Einrichtung hat ein paar Schritte, ist aber einmalig. In der Studie wurde die Furhat SDK 2.9.0 verwendet.

**a) SDK-Konto anfordern.** Gehen Sie auf https://www.furhatrobotics.com/requestsdk und füllen Sie das kurze Formular aus. Sie bekommen anschließend eine E-Mail mit den nächsten Schritten. Diese E-Mail kommt im Normalfall innerhalb weniger Minuten. Darüber legen Sie Ihr Konto an und haben dann sofort Zugriff auf den Download und den API-Token. Obwohl also erst ein Formular auszufüllen ist, ist danach alles direkt verfügbar.

**b) Launcher herunterladen.** Laden Sie den Installer für Ihr System von https://furhat.io/downloads herunter. Eventuell müssen Sie sich dort noch einmal mit dem soeben erstellten Konto anmelden.

**c) Installer ausführen.**

- Windows: Doppelklick auf die `.exe` und den Anweisungen folgen. Hinweis: Windows SmartScreen blockiert den Installer eventuell ("Der Computer wurde durch Windows geschützt"). Klicken Sie auf "Weitere Informationen" und dann auf "Trotzdem ausführen".
- Mac: Die `.dmg` öffnen und in den Programme-Ordner ("Applications") ziehen.
- Linux: Die `.AppImage` als ausführbar markieren und starten.

**d) Konto mit dem API-Token verbinden.**

1. Gehen Sie auf https://furhat.io/profile/user
2. Scrollen Sie zu "Furhat API Token".
3. Erzeugen Sie einen Token, falls noch keiner vorhanden ist.
4. Fügen Sie den Token im Launcher in das Feld "API Token" ein.

Damit kann der Launcher SDK-Updates laden und den virtuellen Furhat verwalten.

**e) Erste Einrichtung.**

1. Starten Sie die SDK (den Launcher).
2. Klicken Sie auf "Open Web Interface". Es öffnet sich `http://localhost:8080`.
3. Melden Sie sich mit dem Passwort `admin` an.
4. Stellen Sie unter "Settings" das Mikrofon (Settings > Microphone) und den Lautsprecher (Settings > Audio Output) ein.
5. Testen Sie kurz: Furhat sprechen lassen, den Blick (gaze) ausprobieren und eine Geste abspielen.

Die offizielle Anleitung dazu finden Sie unter https://docs.furhat.io/setup/sdk

> **Hinweis zu Java:** Die Furhat SDK installiert die benötigte Java-Version automatisch und richtet sie ein. Normalerweise müssen Sie Java also nicht extra installieren. Nur falls IntelliJ später nach einem "JDK" fragt, können Sie das von der SDK installierte Java auswählen oder ersatzweise "Temurin 17" von https://adoptium.net installieren.

---

## Teil 2: Projekt öffnen und was beim ersten Öffnen passiert

### Schritt 1: Öffnen

1. Starten Sie IntelliJ IDEA.
2. Klicken Sie auf **"Open"** (Öffnen).
3. Wählen Sie den **Projektordner** `furhat-prompting` aus, den Sie in Teil 0 angelegt haben. Wichtig: den **ganzen Ordner** wählen, nicht eine einzelne Datei.
4. Bestätigen Sie Nachfragen mit "Trust Project" / "Projekt vertrauen" und "OK".
5. Eventuell erscheint unten rechts ein kleiner Hinweis, dass ein Gradle-Projekt bzw. ein Gradle-Build gefunden wurde. Klicken Sie dort auf **"Load"** (bzw. "Load Gradle Project"). Damit startet das Laden des Projekts.

### Schritt 2: Warten, bis das Projekt geladen ist ("Gradle-Sync")

Unten in IntelliJ erscheint eine Lade-Anzeige (oft "Gradle sync" oder ein Fortschrittsbalken). Im Hintergrund werden jetzt automatisch alle benötigten Bausteine aus dem Internet heruntergeladen. **Das kann beim ersten Mal mehrere Minuten dauern.** Warten Sie, bis die Anzeige fertig ist. Sie brauchen dafür eine Internetverbindung.

### Was sind die vielen neuen Dateien und Ordner?

Beim Laden und beim späteren Bauen erzeugt das System automatisch zusätzliche Ordner, zum Beispiel `build`, `.gradle` und `.idea`. **Das ist völlig normal.** Das sind Zwischen- und Hilfsdateien, die das Programm und IntelliJ selbst anlegen. Sie müssen dort nichts anfassen.

Diese Ordner gehören **nicht** ins Internet / zu GitHub. Dafür ist gesorgt: Im Projekt liegt eine Datei namens `.gitignore`, die genau diese automatisch erzeugten Ordner sowie die geheime `.env`-Datei vom Hochladen ausschließt. Sie müssen sich darum nicht kümmern, das passiert automatisch.

---

## Teil 3: Die zwei Dateien, die Sie anpassen können

Fast alles im Projekt bleibt unverändert. Wenn Sie etwas ändern möchten, gibt es nur diese zwei Dateien (plus die `.env` für Ihren Schlüssel, siehe Teil 4). Beide liegen zusammen im Ordner **`_HIER_ANPASSEN`** ganz oben im Quellbaum, damit Sie nicht suchen müssen.

### Datei 1: Einstellungen (`Einstellungen.kt`)

So finden Sie sie: im Datei-Baum links nacheinander aufklappen:
`src` -> `main` -> `kotlin` -> `_HIER_ANPASSEN` -> **`Einstellungen.kt`**

Dort stellen Sie mit den klar markierten Zeilen ein:

1. **Welche KI** verwendet wird. Standard ist die Uni-KI:
   ```kotlin
   val LLM_PROVIDER = LLMProviderType.UNI
   ```
   Für OpenAI stattdessen `LLMProviderType.OPENAI`.

2. **Wie das Gespräch startet** (siehe Teil 5 für die Erklärung):
   ```kotlin
   val START_BEHAVIOR = StartBehavior.DIREKT
   ```
   Alternative: `StartBehavior.BEGRUESSUNG_DANN_START`.

3. **Wie lange** das Gespräch maximal dauern darf (in Minuten):
   ```kotlin
   const val CONVERSATION_TIME_LIMIT_MINUTES = 12
   ```

Jede Einstellung ist in der Datei auf Deutsch erklärt.

### Datei 2: Der Prompt (`PromptConfig.kt`)

So finden Sie sie: `src` -> `main` -> `kotlin` -> `_HIER_ANPASSEN` -> **`PromptConfig.kt`**

Hier steht, **was** der Roboter sagt: seine Rolle, sein Tonfall und die Regeln. Den Text zwischen den dreifachen Anführungszeichen können Sie frei bearbeiten.

Bitte lassen Sie drei kurze Bausteine im Text stehen, weil das Programm sie zur Steuerung braucht: `__START__`, `__NO_INPUT__` und `__END__`. In der Datei steht oben erklärt, wofür sie da sind.

> Nach jeder Änderung an einer dieser Dateien müssen Sie das Programm neu starten bzw. neu bauen (Teil 5). Sonst wirkt die Änderung nicht.

---

## Teil 4: API-Schlüssel einrichten (damit die KI antworten kann)

Die KI braucht einen Zugang. Sie haben zwei Möglichkeiten:

- **UDE Academic Cloud** (Standard, für Angehörige der Universität Duisburg-Essen kostenlos). Meist der einfachste und günstigste Weg.
- **OpenAI** (kommerziell, kostenpflichtig). In der Studie wurde das Modell `gpt-5.1` verwendet.

### So tragen Sie den Schlüssel ein

1. Im Projektordner liegt eine Datei namens **`.env.example`**.
2. Erstellen Sie eine **Kopie** davon und benennen Sie die Kopie in **`.env`** um (also nur `.env`, ohne `.example`).
3. Öffnen Sie die neue `.env`-Datei (z.B. per Doppelklick in IntelliJ oder mit einem einfachen Texteditor).
4. Tragen Sie **nur** den Schlüssel ein, den Sie nutzen.

   Für die Uni-KI:
   ```
   UNI_API_KEY=ihr-echter-schlüssel-hier
   UNI_MODEL=qwen3-30b-a3b-instruct-2507
   ```

   Für OpenAI:
   ```
   OPENAI_API_KEY=ihr-echter-schlüssel-hier
   OPENAI_MODEL=gpt-5.1
   ```
5. Speichern.
6. Achten Sie darauf, dass in `Einstellungen.kt` der passende Anbieter eingestellt ist (`UNI` oder `OPENAI`, siehe Teil 3).

> **Sicherheit:** Die `.env` mit Ihrem echten Schlüssel darf **niemals** ins Internet / zu GitHub hochgeladen werden. Die Datei `.gitignore` sorgt im Projekt automatisch dafür. Geben Sie Ihren Schlüssel auch nicht an Dritte weiter.

Wenn kein passender Schlüssel hinterlegt ist, startet das Programm trotzdem, schreibt aber eine Warnung in die Konsole, und der Roboter gibt statt einer KI-Antwort einen Ersatzsatz.

### Welches Modell? (`UNI_MODEL` bzw. `OPENAI_MODEL`)

Der Wert hinter `UNI_MODEL` muss ein Modell sein, das die Academic Cloud **aktuell** anbietet. Diese Liste ändert sich gelegentlich; ein veralteter Name führt zur Server-Antwort `Model Not Found`. Stand Juni 2026 funktionieren bei der Uni-KI zum Beispiel:

- `qwen3-30b-a3b-instruct-2507` (gute Qualität, schnell, mehrsprachig; empfohlen)
- `meta-llama-3.1-8b-instruct` (sehr schnell, etwas einfacher)
- `mistral-large-3-675b-instruct-2512` (stärkstes Modell, dafür langsamer)

Die jeweils aktuelle Liste finden Sie hier: https://docs.hpc.gwdg.de/services/ai-services/chat-ai/models/index.html (Spalte "Model Name" in der Tabelle "API Model Names").

Falls der Roboter nur einen Ersatzsatz sagt und in der Konsole `Model Not Found` steht, tragen Sie einfach einen aktuellen Modellnamen bei `UNI_MODEL` ein und starten neu.

---

## Teil 5: Den Roboter starten

Es gibt zwei Wege. Weg A ist zum Ausprobieren am Computer, Weg B für den echten Roboter.

### Weg A: Auf dem virtuellen Furhat (zum Testen, empfohlen für den Anfang)

1. Starten Sie die **Furhat SDK** und starten Sie darin den **virtuellen Furhat** (Schaltfläche "Launch"). **Warten Sie, bis er vollständig geladen ist** (der Roboterkopf ist sichtbar bzw. die Weboberfläche unter `http://localhost:8080` ist erreichbar) und lassen Sie ihn die ganze Zeit im Hintergrund laufen. Erst wenn der virtuelle Furhat läuft, kann sich der Skill damit verbinden.
2. Öffnen Sie in IntelliJ die Datei **`main.kt`**:
   `src` -> `main` -> `kotlin` -> `furhatos` -> `app` -> `furhat` -> **`main.kt`**
3. Schauen Sie oben rechts in IntelliJ auf das Startmenü. Dort muss **`Current File`** ausgewählt sein. Klicken Sie dann auf den grünen **Play-Knopf**.
4. Der Skill startet und verbindet sich mit dem virtuellen Furhat. Das Gespräch beginnt (je nach Einstellung sofort oder nach Aufruf der Start-Adresse, siehe unten).

> Falls beim Start eine Meldung wegen `skill.properties` erscheint: oben rechts auf das Startmenü -> "Edit Configurations" -> beim Eintrag "Working directory" den Projektordner `furhat-prompting` eintragen, dann erneut starten.

### Weg B: Auf dem echten Furhat-Roboter

1. Skill bauen: Öffnen Sie rechts das **Gradle-Fenster** (Elefanten-Symbol; falls nicht sichtbar: Menü `View` -> `Tool Windows` -> `Gradle`). Klappen Sie **`Tasks`** -> **`shadow`** auf und doppelklicken Sie auf **`shadowJar`**. Wenn unten **"BUILD SUCCESSFUL"** steht, hat es geklappt.
2. Es entsteht die Datei **`Furhat.skill`** im Ordner **`build/libs/`** im Projekt.
3. Öffnen Sie im Browser die **Weboberfläche des Roboters** (die Adresse des konkreten Furhat-Roboters) und gehen Sie zum Bereich **"Skills"**.
4. Laden Sie dort die Datei **`Furhat.skill`** hoch und starten Sie sie.

> **Wichtig zum API-Schlüssel auf dem echten Roboter:** Anders als beim virtuellen Furhat (der die `.env` aus dem Projektordner liest) hat der echte Roboter keinen Zugriff auf Ihre `.env`-Datei. Damit der Schlüssel mitkommt, wird die `.env` beim Bauen automatisch **in die `Furhat.skill` eingepackt**. Voraussetzung: Die `.env` muss **vor** dem `shadowJar`-Bauen im Projektordner liegen und Ihren echten Schlüssel enthalten (Teil 4). Ändern Sie den Schlüssel oder das Modell, müssen Sie die `.skill` **neu bauen** und erneut hochladen. Hinweis: Die so gebaute `.skill` enthält dann Ihren Schlüssel — geben Sie diese Datei nicht an Dritte weiter.

> Merke: **Jedes Mal**, wenn Sie etwas an den Einstellungen oder am Prompt ändern, müssen Sie den Skill neu starten (Weg A) bzw. neu bauen und neu hochladen (Weg B).

### Wann genau beginnt das Gespräch? (`START_BEHAVIOR`)

Das hängt von der Einstellung `START_BEHAVIOR` ab (Teil 3, Datei 1):

- **DIREKT:** Das Gespräch startet von selbst, sobald der Roboter eine Person erkennt. Der einfachste Weg.
- **BEGRUESSUNG_DANN_START:** Der Roboter sagt zuerst nur einen kurzen Wartesatz. Das eigentliche Gespräch beginnt erst, wenn Sie im Browser diese Adresse aufrufen:
  - virtueller Furhat: `http://localhost:8088/start`
  - physischer Roboter: `http://<roboter-ip>:8088/start`

---

## Teil 6: Betriebsmodi und Weboberfläche (Steuerpult)

Es gibt zwei Betriebsmodi. Sie stellen ihn ganz oben in `Einstellungen.kt` ein:

```kotlin
val BETRIEBS_MODUS = Betriebsmodus.STUDIE   // oder Betriebsmodus.MESSE
```

| | **Studie** | **Messe** |
|---|---|---|
| Start | manuell bzw. wie `START_BEHAVIOR` | automatisch, sobald eine Person erkannt wird |
| Einleitung | KI erzeugt den ersten Satz | **fester** Einleitungssatz (umschaltbar auf KI) |
| Schluss | KI erzeugt den Schluss | KI-Schluss (umschaltbar auf festen Satz) |
| Timer | **an** (12 Min, gleiche Dauer für alle) | **aus** (Betreuung beendet per Knopf) |
| Nach dem Ende | Programm endet | bereit für die nächste Person (ohne Neustart) |

Der **Studien-Modus** verhält sich wie das ursprüngliche Programm und ist bewusst standardisiert (gleiche Einleitung/Schluss und feste Dauer für alle Teilnehmenden). Der **Messe-Modus** ist flexibler, läuft eher autonom und wird über die Weboberfläche gesteuert.

### Die Weboberfläche (Steuerpult)

Sobald der Roboter läuft, öffnen Sie im Browser:

- virtueller Furhat: `http://localhost:8088/`
- physischer Roboter: `http://<roboter-ip>:8088/`

Die Seite ist für Handy/Tablet gemacht. Sie hat:

- **Start / Neues Gespräch** – beginnt ein Gespräch (bzw. startet für die nächste Person). Während ein Gespräch läuft, ist dieser Knopf ausgegraut; beenden Sie erst das laufende Gespräch, um ein neues zu starten.
- **Pause** und **Weiter** – hält das Gespräch mitten im Verlauf an und setzt es genau dort fort (z. B. wenn jemand eine Frage an die Betreuung hat). Der Timer pausiert mit; beim Fortsetzen sagt der Roboter einen kurzen Überbrückungssatz.
- **Beenden** – der Roboter sagt den Schlusssatz und ist danach bereit für die nächste Person
- Schalter **Timer an/aus**, **Einleitung fest/KI**, **Schluss fest/KI** – jederzeit live umschaltbar
- Statusanzeige (bereit / läuft / pausiert) und verbleibende Zeit

Diese Umschaltungen gelten nur für die laufende Sitzung; die Startwerte kommen aus dem Betriebsmodus bzw. aus `Einstellungen.kt`. Die alte Adresse `http://localhost:8088/start` funktioniert weiterhin und startet ein Gespräch.

> **Sicherheit:** Die Weboberfläche ist für jeden im selben Netzwerk erreichbar (ohne Passwort). Für eine Messe mit eigenem Router ist das in der Regel unproblematisch.

### Neue feste Sätze in `Einstellungen.kt`

Im Abschnitt „Feste Sätze" können Sie anpassen:

- `EINLEITUNGSSATZ` – fester Einleitungssatz (Messe bzw. wenn Einleitung auf „fest" steht)
- `SCHLUSSSATZ` – fester Schlusssatz (beim Beenden bzw. wenn Schluss auf „fest" steht)
- `UEBERBRUECKUNGSSATZ` – kurzer Satz nach einer Pause
- `GREETING_TEXT` – Wartesatz (nur Studie mit `BEGRUESSUNG_DANN_START`)

---

## Wenn etwas nicht funktioniert

**Der Gradle-Sync (Teil 2) bleibt hängen oder bricht ab.**
Internetverbindung prüfen und den Sync neu starten (im Gradle-Fenster auf das Aktualisieren-Symbol, zwei kreisförmige Pfeile).

**Beim Bauen kommt "BUILD FAILED" statt "BUILD SUCCESSFUL".**
Meist war der Gradle-Sync noch nicht fertig. Warten Sie, bis er abgeschlossen ist, und versuchen Sie es erneut. Wenn IntelliJ ein fehlendes "JDK" meldet, siehe den Java-Hinweis in Teil 1.

**Der Skill startet nicht und meldet etwas mit `skill.properties`.**
Working directory auf den Projektordner setzen (siehe Hinweis bei Weg A).

**Beim Start (Weg A) erscheint `No connection with broker` und der Skill bricht ab.**
Dann läuft der virtuelle Furhat noch nicht (oder noch nicht vollständig). Starten Sie zuerst in der Furhat SDK den virtuellen Furhat und warten Sie, bis er fertig geladen ist (Roboterkopf sichtbar). Erst danach den Skill in IntelliJ starten.

**Es kommt ein Fehler mit `PKIX path building failed` bzw. `unable to find valid certification path to requested target`.**
Das bedeutet, dass das verwendete Java zu alt ist und das Zertifikat des KI-Servers (Let's Encrypt) nicht kennt. Es liegt nicht am Programm. Das Programm nutzt unter Windows bereits zusätzlich den Windows-Zertifikatsspeicher, daher sollte dieser Fehler dort normalerweise gar nicht mehr auftreten. Falls er doch erscheint: Bei einem Gradle-Projekt startet IntelliJ den Skill standardmäßig über Gradle, der oft noch ein altes Java benutzt. Nur das "Project SDK" umzustellen reicht deshalb meist nicht. Gehen Sie so vor:

*Weg 1 (am einfachsten): ein aktuelles Java verwenden und IntelliJ direkt starten lassen.*

1. `File` → `Settings` → `Build, Execution, Deployment` → `Build Tools` → `Gradle`: bei "Build and run using" **"IntelliJ IDEA"** auswählen.
2. `File` → `Project Structure` → `Project` → `SDK`: ein aktuelles JDK wählen, am einfachsten über `Add SDK` → `Download JDK` → "Eclipse Temurin 17" (landet im Benutzerordner, kein Administrator nötig).
3. `File` → "Invalidate Caches…" → "Invalidate and Restart". Das beendet auch den alten Gradle-Hintergrundprozess.
4. Den Skill erneut starten.

*Weg 2 (falls Weg 1 nicht hilft): das Let's-Encrypt-Stammzertifikat von Hand hinzufügen.*

1. Im Browser https://letsencrypt.org/certs/isrgrootx1.pem öffnen und die Datei speichern (z.B. nach `Downloads`).
2. Den Pfad des verwendeten Java ablesen: `File` → `Project Structure` → `SDKs`, dort steht der Pfad des ausgewählten JDK (z.B. `C:\Users\...\.jdks\temurin-...`).
3. Eine PowerShell öffnen und folgenden Befehl ausführen (die beiden `<JDK-Pfad>` durch den abgelesenen Pfad und den Pfad zur gespeicherten Datei ersetzen):
   ```
   & "<JDK-Pfad>\bin\keytool.exe" -importcert -trustcacerts -alias isrgrootx1 -file "isrgrootx1.pem" -keystore "<JDK-Pfad>\lib\security\cacerts" -storepass changeit -noprompt
   ```
   Hinweis: Bei einem Java 8 liegt die Zertifikatsdatei unter `<JDK-Pfad>\jre\lib\security\cacerts` (also mit zusätzlichem `jre`). Falls eine Meldung über fehlende Schreibrechte kommt, die PowerShell als Administrator öffnen.
4. IntelliJ neu starten und den Skill erneut ausführen.

Auf einem Rechner mit aktuellem Java tritt dieser Fehler gar nicht erst auf.

**Der Fehler `PKIX path building failed` tritt auf dem echten Roboter auf (Weg B).**
Auf dem Roboter gibt es keinen Windows-Zertifikatsspeicher, und dessen Java kennt manche Stammzertifikate evtl. nicht. Deshalb wird beim Bauen automatisch ein aktueller Standard-Zertifikatssatz heruntergeladen und in die `Furhat.skill` eingepackt (`certs/cacert.pem`); zusätzlich wird dieses Vertrauen beim Start JVM-weit aktiviert. Vorgehen:

1. `Furhat.skill` mit `shadowJar` **neu bauen** (Ihr PC braucht dafür Internet, der Roboter nicht) und erneut hochladen.
2. In der **Build-Konsole** sollte stehen: `[downloadCaCerts] geladen: cacert.pem (...)`. Fehlt das oder steht dort eine WARNUNG, hatte der PC beim Bauen kein Internet → Verbindung prüfen, neu bauen.
3. Im **Roboter-Log** sollte beim Start stehen: `[TrustConfig] Zusaetzliche Stammzertifikate JVM-weit aktiviert.` Erscheint das nicht, läuft noch die alte Skill → wirklich die neue `.skill` hochladen.

**Wenn der Fehler danach immer noch kommt (wichtig bei Uni-/Firmennetzen):**
Dann bricht vermutlich ein **Netzwerk-Proxy** die HTTPS-Verbindung auf und zeigt sein **eigenes** Zertifikat vor. Das steht in keinem Standard-Satz, deshalb muss genau dieses Zertifikat mitgegeben werden:

1. Holen Sie das Zertifikat, das der Roboter sieht. Am einfachsten **vom selben Netz aus** (am Roboter oder einem Rechner im selben Netzwerk) in einem Terminal:
   ```
   openssl s_client -connect chat-ai.academiccloud.de:443 -servername chat-ai.academiccloud.de -showcerts
   ```
   Kopieren Sie den **letzten** ausgegebenen Block von `-----BEGIN CERTIFICATE-----` bis `-----END CERTIFICATE-----` (das ist das oberste/aussstellende Zertifikat).
   Alternativ im Browser im selben Netz: auf das Schloss-Symbol → Zertifikat → das oberste (Stamm-)Zertifikat als `.pem`/`.crt` exportieren.
2. Speichern Sie diesen Block als Datei **`extra-cert.pem`** direkt im Projektordner (neben `build.gradle`).
3. `Furhat.skill` neu bauen (in der Konsole erscheint dann `[downloadCaCerts] Eigenes Zertifikat 'extra-cert.pem' eingepackt.`) und erneut hochladen.

Damit vertraut der Roboter auch dem Proxy-Zertifikat, und die Verbindung funktioniert.

**Der Roboter sagt nur einen Ersatzsatz statt einer richtigen Antwort.**
Dann fehlt der API-Schlüssel oder der Modellname stimmt nicht. Prüfen Sie die `.env` (Teil 4) und ob in `Einstellungen.kt` der passende Anbieter steht. In der Konsole in IntelliJ steht eine Warnung mit dem Grund (z.B. `Model Not Found`).

**Auf dem echten Roboter erscheint die Warnung, dass kein Schlüssel in der `.env` hinterlegt ist (Weg B).**
Der Schlüssel wird beim Bauen aus der `.env` in die `Furhat.skill` eingepackt. Diese Warnung bedeutet meist, dass beim Bauen noch keine `.env` mit echtem Schlüssel im Projektordner lag. Legen Sie die `.env` an (Teil 4), bauen Sie die `Furhat.skill` mit `shadowJar` **neu** und laden Sie sie erneut auf den Roboter. Alternativ können Sie den Schlüssel auf dem Roboter als Umgebungsvariable (`UNI_API_KEY` bzw. `OPENAI_API_KEY`) setzen — diese hat Vorrang vor der eingepackten `.env`.

**Die Start-Adresse `http://localhost:8088/start` reagiert nicht.**
Der Skill muss zuerst laufen (Weg A oder B). Die Adresse ist außerdem nur im Modus `BEGRUESSUNG_DANN_START` nötig. Im Modus `DIREKT` startet das Gespräch von selbst.

**Ich habe etwas geändert, aber der Roboter verhält sich gleich.**
Nach jeder Änderung neu starten (Weg A) bzw. neu bauen und hochladen (Weg B).

**In der Konsole steht `A flow's queue is full` und/oder der Roboter sagt nach einer längeren Antwort nichts mehr (blockiert).**
Ursache war, dass der KI-Aufruf den Gesprächs-Ablauf blockierte, während auf die Antwort gewartet wurde, und dass ein Timeout/Netzwerkfehler den Ablauf abbrechen konnte. Das ist behoben: Der KI-Aufruf läuft jetzt im Hintergrund (der Roboter bleibt reaktionsfähig), und bei einem Problem mit der KI sagt der Roboter einen kurzen Ersatzsatz und hört einfach weiter zu, statt stehen zu bleiben. Falls Sie eine ältere Version nutzen: einmal neu bauen und hochladen.

---

## Was steckt wo? (Übersicht)

Diese zwei Dateien können Sie anpassen, beide im Ordner `_HIER_ANPASSEN`:

| Datei | Inhalt |
|---|---|
| `_HIER_ANPASSEN/Einstellungen.kt` | KI-Anbieter, Startverhalten, Gesprächsdauer. Auf Deutsch erklärt. |
| `_HIER_ANPASSEN/PromptConfig.kt` | Der Prompt: was der Roboter sagt und wie er sich verhält. |

Der Rest bleibt normalerweise unverändert (Ablaufsteuerung, KI-Anbindung, Bau-Konfiguration). Automatisch erzeugte Ordner wie `build`, `.gradle` und `.idea` entstehen beim Arbeiten von selbst und müssen nicht beachtet werden.

---

## Zitation

Bei Verwendung in Publikationen bitte zitieren:

> Faasen, J. M. (2026). *Prompting vs. Scripting in der Mensch-Roboter-Interaktion. Eine experimentelle Untersuchung zu Vertrauen, Natürlichkeit und Akzeptanz im Gespräch mit einem sozialen Roboter* (Masterarbeit). Universität Duisburg-Essen.

## Kontakt

josephine@faasen.de

Josephine Marie Faasen, Universität Duisburg-Essen, 2026
