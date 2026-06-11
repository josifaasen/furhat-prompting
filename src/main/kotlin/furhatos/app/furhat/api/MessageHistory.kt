package furhatos.app.furhat.api


import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Verwaltet Nachrichten für die LLM-Kommunikation.
 * - speichert abwechselnd USER und ASSISTANT Nachrichten
 * - limitiert die Länge auf sinnvolle Anzahl (z. B. 10)
 */
object MessageHistory {

    private val logFile = File("conversation_log.txt")

    private val messages = mutableListOf<Message>()

    data class Message(val role: String, val content: String)

    /** Neues User-Input hinzufügen */
    fun addUser(text: String) {
        messages += Message("user", text)
        saveToFile()
    }

    /** Neue Assistant-Antwort hinzufügen */
    fun addAssistant(text: String) {
        messages += Message("assistant", text)
        saveToFile()
    }

    /** Kompletten Verlauf als JSON-kompatible Liste zurückgeben */
    fun jsonMessages(systemPrompt: String): List<Map<String, String>> {
        val list = mutableListOf<Map<String, String>>()

        // 1. Systemprompt immer zuerst
        list += mapOf("role" to "system", "content" to systemPrompt)

        // 2. Dann abwechselnd: user / assistant / user / assistant ...
        messages.takeLast(10).forEach {
            list += mapOf("role" to it.role, "content" to it.content)
        }

        return list
    }

    /** Verlauf löschen (z. B. am Gesprächsende) */
    fun reset() {
        messages.clear()
    }

    fun saveToFile() {
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        val content = buildString {
            appendLine("=== Conversation Log ($timestamp) ===")
            messages.forEach { msg ->
                appendLine("${msg.role.uppercase()}: ${msg.content}")
            }
            appendLine("\n")
        }
        logFile.appendText(content)
    }
}
