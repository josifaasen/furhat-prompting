/**
 * Anbindung an die Universitäts-KI (AcademicCloud Chat-AI).
 *
 * Diese Implementierung folgt dem OpenAI-kompatiblen
 * Chat-Completions-Format der Academic Cloud.
 *
 * Sie ist vollständig austauschbar mit OpenAIProvider.
 */
package furhatos.app.furhat.api

import furhatos.app.furhat.prompt.PromptConfig
import furhatos.app.furhat.util.EnvConfig
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import org.json.JSONArray
import java.util.concurrent.TimeUnit

object UniProvider : LLMProvider {

    private val client = TrustConfig.apply(
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
    ).build()

    /** Uni-API-Key wird aus der .env-Datei geladen (Variable UNI_API_KEY) */
    var apiKey: String = EnvConfig.uniApiKey

    /** Basis-Endpoint der AcademicCloud Chat-AI */
    private const val BASE_URL = "https://chat-ai.academiccloud.de/v1/chat/completions"

    /** Modell wird aus .env geladen (UNI_MODEL), Default: qwen3-30b-a3b-instruct-2507 */
    var model: String = EnvConfig.uniModel

    override fun ask(prompt: String): String {

        // Key-Check: ohne gueltigen Key keine Anfrage absetzen.
        if (apiKey.isBlank()) {
            System.err.println(
                "[UniProvider] Kein UNI_API_KEY in .env hinterlegt. " +
                "Es kann keine KI-Antwort erzeugt werden. " +
                "Bitte .env.example nach .env kopieren und den Schluessel eintragen."
            )
            return "Entschuldigung, der Sprachdienst ist nicht erreichbar."
        }

        // 1) User-Eingabe in die History eintragen
        MessageHistory.addUser(prompt)

        // 2) Systemprompt laden
        val system = PromptConfig.systemPrompt

        // 3) komplette History als JSON-kompatible Struktur abrufen
        val history = MessageHistory.jsonMessages(system)

        // 4) JSONArray für API-Aufruf bauen
        val messageArray = org.json.JSONArray()
        history.forEach { msg ->
            messageArray.put(
                JSONObject()
                    .put("role", msg["role"])
                    .put("content", msg["content"])
            )
        }

        val bodyJson = JSONObject()
            .put("model", model)
            .put("messages", messageArray)

        val body = bodyJson
            .toString()
            .toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url(BASE_URL)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(body)
            .build()

        // Gesamten Netzwerk-Aufruf absichern. Ein Timeout oder Netzwerkfehler
        // (haeufig bei laengeren/langsameren Anfragen) darf NICHT als Exception
        // aus ask() herausfliegen, sonst blockiert der Gespraechs-Flow
        // (Roboter hoert auf zu reden). Stattdessen: Ersatzsatz zurueckgeben.
        val text: String = try {
            client.newCall(request).execute().use { response ->
                response.body?.string()
            } ?: run {
                System.err.println("[UniProvider] Leere Antwort von der Uni-KI.")
                return "Entschuldigung, da ist gerade etwas schiefgelaufen. Lass uns gleich weitermachen."
            }
        } catch (e: Exception) {
            System.err.println(
                "[UniProvider] Anfrage an die Uni-KI fehlgeschlagen " +
                "(z.B. Timeout oder Netzwerkproblem): ${e.message}"
            )
            return "Entschuldigung, ich habe dich gerade nicht ganz erreicht. Magst du das nochmal sagen?"
        }

        println("===== RAW RESPONSE FROM UNI-KI =====")
        println(text)
        println("====================================")

        // Antwort robust auslesen. Wenn der Server keine gueltige JSON-Antwort
        // schickt (z. B. "Model Not Found" bei falschem UNI_MODEL), nicht
        // abstuerzen, sondern eine verstaendliche Warnung loggen.
        val reply = try {
            JSONObject(text)
                .getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
        } catch (e: Exception) {
            System.err.println(
                "[UniProvider] Antwort der Uni-KI konnte nicht gelesen werden. " +
                "Haeufigste Ursache: falscher Modellname in UNI_MODEL (.env). " +
                "Aktuelle Modellnamen siehe README. Server-Antwort war: $text"
            )
            return "Entschuldigung, da ist gerade etwas schiefgelaufen. Lass uns gleich weitermachen."
        }

        // Antwort in History speichern
        MessageHistory.addAssistant(reply)

        return reply
    }
}
