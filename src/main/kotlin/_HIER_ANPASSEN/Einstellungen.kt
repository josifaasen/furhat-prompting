package furhatos.app.furhat.setting

/**
 * ═══════════════════════════════════════════════════════════════════════════
 *  EINSTELLUNGEN
 * ═══════════════════════════════════════════════════════════════════════════
 *
 *  Das ist eine der beiden Dateien, die angepasst werden können.
 *  Hier wird eingestellt:
 *    0. Betriebsmodus: STUDIE oder MESSE
 *    1. Welche KI verwendet wird (Uni-KI oder OpenAI)
 *    2. Wie das Gespräch startet (sofort oder erst nach einem Klick im Browser)
 *    3. Wie lange ein Gespräch maximal dauern darf
 *    4. Feste Sätze (Einleitung, Schluss, Pause-Überbrückung, Wartesatz)
 *
 *  Es müssen nur die mit "HIER EINSTELLEN" markierten Zeilen geändert werden.
 *
 *  WICHTIG: Nach jeder Änderung muss das Projekt neu gebaut werden
 *  (im Gradle-Fenster: Tasks -> shadow -> shadowJar). Siehe README.
 * ═══════════════════════════════════════════════════════════════════════════
 */


// ───────────────────────────────────────────────────────────────────────────
//  0. BETRIEBSMODUS
// ───────────────────────────────────────────────────────────────────────────

/**
 * Zwei Betriebsmodi:
 *
 *  - STUDIE: Verhält sich wie das ursprüngliche Programm. Standardisiert für
 *            die wissenschaftliche Untersuchung: Start über START_BEHAVIOR,
 *            Einleitung und Schluss erzeugt die KI, der Timer ist an.
 *
 *  - MESSE:  Für Öffentlichkeitsarbeit. Wird über die Weboberfläche gesteuert
 *            (http://<roboter-ip>:8088/). Start automatisch bei Personen-
 *            erkennung, fester Einleitungssatz (umschaltbar), KI-Schluss
 *            (umschaltbar), Timer standardmäßig aus. Pause/Weiter/Beenden per
 *            Knopf; nach dem Ende ist der Roboter sofort bereit für die
 *            nächste Person.
 *
 * Die Voreinstellungen je Modus (Einleitung/Schluss fest oder KI, Timer an/aus)
 * lassen sich in der Weboberfläche jederzeit live umschalten.
 */
enum class Betriebsmodus {
    STUDIE,
    MESSE
}

/**
 * HIER EINSTELLEN: Betriebsmodus.
 *     val BETRIEBS_MODUS = Betriebsmodus.STUDIE
 * oder
 *     val BETRIEBS_MODUS = Betriebsmodus.MESSE
 */
val BETRIEBS_MODUS = Betriebsmodus.STUDIE


// ───────────────────────────────────────────────────────────────────────────
//  1. WELCHE KI?
// ───────────────────────────────────────────────────────────────────────────

/**
 * Verfügbare KI-Anbieter:
 *  - UNI:    Academic Cloud der Universität Duisburg-Essen
 *            (für UDE-Angehörige kostenlos, empfohlen)
 *  - OPENAI: OpenAI (kommerziell, kostenpflichtig; in der Studie: gpt-5.1)
 *
 * Der passende API-Schlüssel muss in der Datei ".env" hinterlegt sein
 * (siehe README, Abschnitt "API-Schlüssel einrichten").
 */
enum class LLMProviderType {
    UNI,
    OPENAI
}

/**
 * HIER EINSTELLEN: KI-Anbieter.
 *
 * Standard ist die Uni-KI:
 *     val LLM_PROVIDER = LLMProviderType.UNI
 *
 * Für OpenAI stattdessen:
 *     val LLM_PROVIDER = LLMProviderType.OPENAI
 */
val LLM_PROVIDER = LLMProviderType.UNI


// ───────────────────────────────────────────────────────────────────────────
//  2. WIE STARTET DAS GESPRÄCH?  (nur Modus STUDIE)
// ───────────────────────────────────────────────────────────────────────────

/**
 * Mögliches Startverhalten im Modus STUDIE:
 *
 *  - DIREKT:
 *      Das Gespräch beginnt sofort. Der Roboter sagt direkt seinen ersten
 *      Satz und hört dann zu. Am einfachsten zum Ausprobieren.
 *
 *  - BEGRUESSUNG_DANN_START:
 *      Der Roboter sagt zuerst nur einen kurzen Wartesatz (siehe GREETING_TEXT
 *      weiter unten). Das eigentliche Gespräch beginnt erst, wenn im Browser
 *      die Start-Adresse aufgerufen wird:
 *          http://localhost:8088/start        (virtueller Furhat)
 *          http://<roboter-ip>:8088/start     (physischer Roboter)
 *
 * Im Modus MESSE wird diese Einstellung ignoriert: Dort startet das Gespräch
 * automatisch, sobald eine Person erkannt wird (bzw. über den Start-Knopf der
 * Weboberfläche).
 */
enum class StartBehavior {
    DIREKT,
    BEGRUESSUNG_DANN_START
}

/**
 * HIER EINSTELLEN: Startverhalten (nur Modus STUDIE).
 *
 * Sofort starten:
 *     val START_BEHAVIOR = StartBehavior.DIREKT
 *
 * Erst Begrüßung, dann Start über Browser-Adresse:
 *     val START_BEHAVIOR = StartBehavior.BEGRUESSUNG_DANN_START
 */
val START_BEHAVIOR = StartBehavior.DIREKT


// ───────────────────────────────────────────────────────────────────────────
//  3. GESPRÄCHSDAUER (Timer)
// ───────────────────────────────────────────────────────────────────────────

/**
 * HIER EINSTELLEN: Maximale Gesprächsdauer in MINUTEN.
 * Nach dieser Zeit verabschiedet sich der Roboter und beendet das Gespräch.
 * Standard: 12 Minuten.
 *
 * Ob der Timer überhaupt aktiv ist, hängt vom Betriebsmodus ab (STUDIE: an,
 * MESSE: aus) und lässt sich in der Weboberfläche live umschalten.
 */
const val CONVERSATION_TIME_LIMIT_MINUTES = 12

/** NICHT ÄNDERN: automatische Umrechnung in Millisekunden. */
const val CONVERSATION_TIME_LIMIT_MS = CONVERSATION_TIME_LIMIT_MINUTES * 60 * 1000


// ───────────────────────────────────────────────────────────────────────────
//  4. FESTE SÄTZE
// ───────────────────────────────────────────────────────────────────────────

/**
 * Auswahl, ob ein Satz fest vorgegeben oder von der KI erzeugt wird.
 * Wird für Einleitung und Schluss genutzt.
 */
enum class SatzModus {
    FEST,   // der unten hinterlegte feste Satz
    KI      // die KI erzeugt den Satz (__START__ bzw. __END__)
}

/**
 * HIER EINSTELLEN: Fester Einleitungssatz.
 * Wird gesprochen, wenn die Einleitung auf "fest" steht (z. B. im Messe-Modus).
 */
const val EINLEITUNGSSATZ =
    "Hallo! Schön, dass du da bist. Ich bin Furhat. Erzähl doch mal: Was führt dich heute her?"

/**
 * HIER EINSTELLEN: Fester Schlusssatz.
 * Wird gesprochen, wenn der Schluss auf "fest" steht oder beim Beenden per Knopf.
 */
const val SCHLUSSSATZ =
    "Das war ein schönes Gespräch. Vielen Dank und bis bald!"

/**
 * HIER EINSTELLEN: Überbrückungssatz nach einer Pause.
 * Wird gesprochen, wenn das Gespräch über die Weboberfläche fortgesetzt wird.
 */
const val UEBERBRUECKUNGSSATZ =
    "So, wo waren wir? Erzähl gern weiter."

/**
 * Wartesatz, der nur im Modus STUDIE mit START_BEHAVIOR =
 * BEGRUESSUNG_DANN_START gesagt wird, bevor das eigentliche Gespräch über die
 * Browser-Adresse gestartet wird.
 */
const val GREETING_TEXT =
    "Hi! Ich lade kurz das Gespräch, das dauert einen kleinen Moment. Setz dich gern schon mal hin."


// ───────────────────────────────────────────────────────────────────────────
//  5. ERWEITERTE EINSTELLUNGEN (normalerweise unverändert lassen)
// ───────────────────────────────────────────────────────────────────────────

/**
 * Port für die Weboberfläche / Start-Adresse. Standard: 8088.
 * Aufruf: http://localhost:8088/ (virtuell) bzw. http://<roboter-ip>:8088/.
 */
const val START_TRIGGER_PORT = 8088

/**
 * Maximale Anzahl gleichzeitig erkannter Personen. Standard: 2.
 */
const val MAX_NUMBER_OF_USERS = 2

/**
 * Mindestabstand (in Metern), ab dem Furhat eine Person als
 * Gesprächspartner erkennt. Standard: 1.0.
 */
const val DISTANCE_TO_ENGAGE = 1.0
