package furhatos.app.furhat.setting

/**
 * ═══════════════════════════════════════════════════════════════════════════
 *  EINSTELLUNGEN
 * ═══════════════════════════════════════════════════════════════════════════
 *
 *  Das ist eine der beiden Dateien, die angepasst werden können.
 *  Hier wird eingestellt:
// *  1. Welche KI verwendet wird (Uni-KI oder OpenAI)
 *    2. Wie das Gespräch startet (sofort oder erst nach einem Klick im Browser)
 *    3. Wie lange ein Gespräch maximal dauern darf
 *
 *  Es müssen nur die mit "HIER EINSTELLEN" markierten Zeilen geändert werden.
 *
 *  WICHTIG: Nach jeder Änderung muss das Projekt neu gebaut werden
 *  (im Gradle-Fenster: Tasks -> shadow -> shadowJar). Siehe README.
 * ═══════════════════════════════════════════════════════════════════════════
 */


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
//  2. WIE STARTET DAS GESPRÄCH?
// ───────────────────────────────────────────────────────────────────────────

/**
 * Mögliches Startverhalten:
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
 */
enum class StartBehavior {
    DIREKT,
    BEGRUESSUNG_DANN_START
}

/**
 * HIER EINSTELLEN: Startverhalten.
 *
 * Sofort starten:
 *     val START_BEHAVIOR = StartBehavior.DIREKT
 *
 * Erst Begrüßung, dann Start über Browser-Adresse:
 *     val START_BEHAVIOR = StartBehavior.BEGRUESSUNG_DANN_START
 */
val START_BEHAVIOR = StartBehavior.DIREKT

/**
 * Wartesatz, der nur im Modus BEGRUESSUNG_DANN_START gesagt wird,
 * bevor das eigentliche Gespräch über die Browser-Adresse gestartet wird.
 */
const val GREETING_TEXT =
    "Hi! Ich lade kurz das Gespräch, das dauert einen kleinen Moment. Setz dich gern schon mal hin."


// ───────────────────────────────────────────────────────────────────────────
//  3. GESPRÄCHSDAUER
// ───────────────────────────────────────────────────────────────────────────

/**
 * HIER EINSTELLEN: Maximale Gesprächsdauer in MINUTEN.
 * Nach dieser Zeit verabschiedet sich der Roboter und beendet das Gespräch.
 * Standard: 12 Minuten.
 */
const val CONVERSATION_TIME_LIMIT_MINUTES = 12

/** NICHT ÄNDERN: automatische Umrechnung in Millisekunden. */
const val CONVERSATION_TIME_LIMIT_MS = CONVERSATION_TIME_LIMIT_MINUTES * 60 * 1000


// ───────────────────────────────────────────────────────────────────────────
//  4. ERWEITERTE EINSTELLUNGEN (normalerweise unverändert lassen)
// ───────────────────────────────────────────────────────────────────────────

/**
 * Port für die Start-Adresse (Browser-Trigger im Modus
 * BEGRUESSUNG_DANN_START). Standard: 8088.
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
