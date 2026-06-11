/**
 * Der eigentliche Gesprächszustand (KI-gesteuert, "Prompting").
 *
 * Hier läuft das Gespräch: Der Roboter erzeugt mithilfe der KI seinen
 * ersten Satz, hört dann zu und antwortet auf die Eingaben der Person.
 * Das Gespräch endet, wenn die maximale Dauer erreicht ist
 * (siehe CONVERSATION_TIME_LIMIT_MINUTES in "Einstellungen.kt").
 *
 * WAS die KI sagt, steht NICHT in dieser Datei, sondern im Prompt
 * (Datei "_HIER_ANPASSEN/PromptConfig.kt").
 *
 * Diese Datei muss normalerweise NICHT verändert werden.
 */

package furhatos.app.furhat.flow.main

import furhatos.app.furhat.api.LLMProvider
import furhatos.app.furhat.api.OpenAIProvider
import furhatos.app.furhat.api.UniProvider
import furhatos.app.furhat.setting.CONVERSATION_TIME_LIMIT_MS
import furhatos.app.furhat.setting.LLM_PROVIDER
import furhatos.app.furhat.setting.LLMProviderType
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

/** Was der Roboter NACH dem Vorlesen der KI-Antwort tun soll. */
private enum class AfterReply { LISTEN, END }

/** Name des internen Events, das die fertige KI-Antwort zurueckmeldet. */
private const val REPLY_READY_EVENT = "LLM_REPLY_READY"

val Conversation = state {

    // Den in den Einstellungen gewählten KI-Anbieter verwenden.
    val llm: LLMProvider = when (LLM_PROVIDER) {
        LLMProviderType.UNI -> UniProvider
        LLMProviderType.OPENAI -> OpenAIProvider
    }

    var conversationStart = 0L

    // Zwischenspeicher fuer die im Hintergrund erzeugte Antwort. Es ist immer
    // nur eine Anfrage gleichzeitig unterwegs (waehrend gewartet wird, hoert
    // der Roboter nicht zu), daher genuegt eine einfache Variable.
    var pendingReply: String = ""
    var pendingAfter: AfterReply = AfterReply.LISTEN

    /**
     * Holt die KI-Antwort in einem Hintergrund-Thread und meldet sie per
     * benanntem Event zurueck in den Flow. Dadurch bleibt der Flow-Thread frei
     * und kann weiter Sensor-/Blick-Events verarbeiten. Das verhindert sowohl
     * die Warnung "A flow's queue is full" als auch die Blockade, wenn die KI
     * mal laenger braucht. llm.ask() ist fehlertolerant und liefert im
     * Problemfall selbst einen Ersatzsatz (kein Absturz).
     *
     * Der "runner" wird aus dem aufrufenden Trigger uebergeben, weil send()
     * den FlowControlRunner als Empfaenger braucht (im Thread selbst ist er
     * sonst nicht verfuegbar).
     */
    fun askAsync(runner: FlowControlRunner, prompt: String, after: AfterReply) {
        thread(name = "llm-request") {
            val reply = cleanForSpeech(llm.ask(prompt))
            pendingReply = reply
            pendingAfter = after
            runner.send(REPLY_READY_EVENT)
        }
    }

    onEntry {
        conversationStart = System.currentTimeMillis()
        // Die KI erzeugt den ersten Satz (asynchron).
        askAsync(this, "__START__", AfterReply.LISTEN)
    }

    // Prüft, ob die maximale Gesprächsdauer überschritten ist.
    fun checkEndCondition(): Boolean {
        val elapsed = System.currentTimeMillis() - conversationStart
        return elapsed > CONVERSATION_TIME_LIMIT_MS
    }

    // Zentrale Stelle: KI-Antwort ist da -> vorlesen und passend weitermachen.
    onEvent(REPLY_READY_EVENT) {
        furhat.say(pendingReply)
        when (pendingAfter) {
            AfterReply.LISTEN -> furhat.listen()
            AfterReply.END -> goto(EndState)
        }
    }

    onResponse {
        furhat.stopListening()

        // Zeitlimit erreicht: höflich verabschieden und beenden.
        if (checkEndCondition()) {
            askAsync(this, "__END__", AfterReply.END)
            return@onResponse
        }

        askAsync(this, it.text, AfterReply.LISTEN)
    }

    onNoResponse {
        furhat.stopListening()

        // Zeitlimit erreicht: höflich verabschieden und beenden.
        if (checkEndCondition()) {
            askAsync(this, "__END__", AfterReply.END)
            return@onNoResponse
        }

        // Nichts verstanden: um Wiederholung bitten.
        askAsync(this, "__NO_INPUT__", AfterReply.LISTEN)
    }
}
