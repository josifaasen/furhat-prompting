/**
 * Der eigentliche Gesprächszustand (KI-gesteuert, "Prompting").
 *
 * Hier läuft das Gespräch: Der Roboter beginnt (fester Einleitungssatz oder
 * KI), hört zu und antwortet auf die Eingaben der Person. Über die
 * Weboberfläche kann das Gespräch pausiert, fortgesetzt und beendet werden.
 * Das Gespräch endet außerdem automatisch, wenn der Timer aktiv ist und die
 * maximale Dauer erreicht wurde (siehe Einstellungen.kt).
 *
 * WAS die KI sagt, steht NICHT in dieser Datei, sondern im Prompt
 * (Datei "_HIER_ANPASSEN/PromptConfig.kt"). Feste Sätze stehen in
 * "_HIER_ANPASSEN/Einstellungen.kt".
 *
 * Diese Datei muss normalerweise NICHT verändert werden.
 */

package furhatos.app.furhat.flow.main

import furhatos.app.furhat.api.LLMProvider
import furhatos.app.furhat.api.MessageHistory
import furhatos.app.furhat.api.OpenAIProvider
import furhatos.app.furhat.api.UniProvider
import furhatos.app.furhat.setting.BETRIEBS_MODUS
import furhatos.app.furhat.setting.Betriebsmodus
import furhatos.app.furhat.setting.CONVERSATION_TIME_LIMIT_MS
import furhatos.app.furhat.setting.EINLEITUNGSSATZ
import furhatos.app.furhat.setting.LLM_PROVIDER
import furhatos.app.furhat.setting.LLMProviderType
import furhatos.app.furhat.setting.SCHLUSSSATZ
import furhatos.app.furhat.setting.SatzModus
import furhatos.app.furhat.setting.UEBERBRUECKUNGSSATZ
import furhatos.app.furhat.steuerung.Steuerung
import furhatos.flow.kotlin.*
import kotlin.concurrent.thread

// Erkennt einen Ende-Marker wie "__DONE__" (auch mit Sternchen oder Leerzeichen).
private val DONE_MARKER = Regex("[_*]{1,}\\s*DONE\\s*[_*]{1,}", RegexOption.IGNORE_CASE)

/**
 * Bereitet den KI-Text fürs Vorlesen auf: entfernt Steuer-Marker und einzelne
 * Sternchen oder Unterstriche, damit der Roboter nie Sonderzeichen vorliest.
 */
private fun cleanForSpeech(text: String): String {
    return text
        .replace(DONE_MARKER, " ")
        .replace(Regex("[_*]+"), " ")
        .replace(Regex("\\s+"), " ")
        .trim()
}

/** Was der Roboter NACH dem Vorlesen der Antwort tun soll. */
private enum class AfterReply { LISTEN, END }

/** Name des internen Events, das den fertigen Text zurückmeldet. */
private const val REPLY_READY_EVENT = "LLM_REPLY_READY"

val Conversation = state {

    // Den in den Einstellungen gewählten KI-Anbieter verwenden.
    val llm: LLMProvider = when (LLM_PROVIDER) {
        LLMProviderType.UNI -> UniProvider
        LLMProviderType.OPENAI -> OpenAIProvider
    }

    // Zwischenspeicher für den im Hintergrund erzeugten/gesetzten Text. Es ist
    // immer nur eine Anfrage gleichzeitig unterwegs, daher genügt eine Variable.
    var pendingReply: String = ""
    var pendingAfter: AfterReply = AfterReply.LISTEN

    /**
     * Holt die KI-Antwort in einem Hintergrund-Thread und meldet sie per
     * benanntem Event zurück in den Flow (über den übergebenen sendEvent-
     * Callback). So bleibt der Flow-Thread frei (keine "queue is full"-Warnung,
     * kein Einfrieren). llm.ask() ist fehlertolerant und liefert im Problemfall
     * selbst einen Ersatzsatz.
     *
     * Der Callback wird im Trigger erzeugt (dort ist send() verfügbar) und ist
     * threadsicher aufrufbar.
     */
    fun startLlm(prompt: String, after: AfterReply, sendEvent: (String) -> Unit) {
        thread(name = "llm-request") {
            pendingReply = cleanForSpeech(llm.ask(prompt))
            pendingAfter = after
            sendEvent(REPLY_READY_EVENT)
        }
    }

    /** Schluss einleiten: fester Schlusssatz ODER KI-Schluss (__END__). */
    fun einleitenSchluss(sendEvent: (String) -> Unit) {
        if (Steuerung.schlussModus == SatzModus.FEST) {
            pendingReply = cleanForSpeech(SCHLUSSSATZ)
            pendingAfter = AfterReply.END
            sendEvent(REPLY_READY_EVENT)
        } else {
            startLlm("__END__", AfterReply.END, sendEvent)
        }
    }

    /** Ist der Timer aktiv und die maximale Dauer überschritten? */
    fun zeitlimitErreicht(): Boolean =
        Steuerung.timerAktiv && Steuerung.verstricheneMs() > CONVERSATION_TIME_LIMIT_MS

    onEntry {
        Steuerung.gespraechBegonnen()
        if (Steuerung.einleitungModus == SatzModus.FEST) {
            furhat.say(cleanForSpeech(EINLEITUNGSSATZ))
            furhat.listen()
        } else {
            startLlm("__START__", AfterReply.LISTEN) { ev -> send(ev) }
        }
    }

    // Text ist fertig -> vorlesen und passend weitermachen.
    onEvent(REPLY_READY_EVENT) {
        furhat.say(pendingReply)
        when (pendingAfter) {
            AfterReply.LISTEN ->
                if (Steuerung.zustand != Steuerung.Zustand.PAUSIERT) furhat.listen()
            AfterReply.END -> {
                // Verlauf löschen und in den passenden Zustand:
                // MESSE -> zurück in Wartebereitschaft (nächste Person ohne Neustart),
                // STUDIE -> Programm beenden (wie bisher).
                MessageHistory.reset()
                Steuerung.bereit()
                if (BETRIEBS_MODUS == Betriebsmodus.MESSE) goto(Idle) else goto(EndState)
            }
        }
    }

    // Steuerung über die Weboberfläche:
    onEvent("STEUER_PAUSE") {
        furhat.stopListening()
        Steuerung.pausieren()
    }

    onEvent("STEUER_WEITER") {
        if (Steuerung.zustand == Steuerung.Zustand.PAUSIERT) {
            Steuerung.fortsetzen()
            furhat.say(cleanForSpeech(UEBERBRUECKUNGSSATZ))
            furhat.listen()
        }
    }

    onEvent("STEUER_BEENDEN") {
        furhat.stopListening()
        einleitenSchluss { ev -> send(ev) }
    }

    onResponse {
        if (Steuerung.zustand == Steuerung.Zustand.PAUSIERT) return@onResponse
        furhat.stopListening()

        // Zeitlimit erreicht: höflich verabschieden und beenden.
        if (zeitlimitErreicht()) {
            einleitenSchluss { ev -> send(ev) }
            return@onResponse
        }

        startLlm(it.text, AfterReply.LISTEN) { ev -> send(ev) }
    }

    onNoResponse {
        if (Steuerung.zustand == Steuerung.Zustand.PAUSIERT) return@onNoResponse
        furhat.stopListening()

        // Zeitlimit erreicht: höflich verabschieden und beenden.
        if (zeitlimitErreicht()) {
            einleitenSchluss { ev -> send(ev) }
            return@onNoResponse
        }

        // Nichts verstanden: um Wiederholung bitten.
        startLlm("__NO_INPUT__", AfterReply.LISTEN) { ev -> send(ev) }
    }
}
