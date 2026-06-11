/**
 * Anbindung an die OpenAI-API (z. B. GPT-5.1).
 *
 * Verantwortlichkeiten:
 * - Aufbau einer HTTP-Anfrage an das Chat-Completions-Endpunkt
 * - Übergabe des Nutzereingabetextes
 * - Parsing der Modellantwort
 * - Timeout-Handling und robuste Fehlerbehandlung
 *
 * Hinweis:
 * Die Klasse implementiert eine vereinfachte Form der Kommunikation.
 * Im späteren Ausbau wird sie das `LLMProvider`-Interface implementieren,
 * sodass KI-Anbieter austauschbar werden (OpenAI ↔ Uni-KI ↔ Skript).
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


object OpenAIProvider : LLMProvider {

    private val client = TrustConfig.apply(
        okhttp3.OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
    ).build()

    var apiKey: String = EnvConfig.openaiApiKey

    private const val URL = "https://api.openai.com/v1/chat/completions"

    var model: String = EnvConfig.openaiModel




    override fun ask(prompt: String): String {

        // Key-Check: ohne gueltigen Key keine Anfrage absetzen.
        if (apiKey.isBlank()) {
            System.err.println(
                "[OpenAIProvider] Kein OPENAI_API_KEY in .env hinterlegt. " +
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
            .put("model", OpenAIProvider.model)
            .put("messages", messageArray)

        val body = bodyJson
            .toString()
            .toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url(URL)
            .addHeader("Authorization", "Bearer ${OpenAIProvider.apiKey}")
            .addHeader("Content-Type", "application/json")
            .post(body)
            .build()

        // Gesamten Netzwerk-Aufruf absichern. Ein Timeout oder Netzwerkfehler
        // darf NICHT als Exception aus ask() herausfliegen, sonst blockiert der
        // Gespraechs-Flow (Roboter hoert auf zu reden). Stattdessen Ersatzsatz.
        val text: String = try {
            OpenAIProvider.client.newCall(request).execute().use { response ->
                response.body?.string()
            } ?: run {
                System.err.println("[OpenAIProvider] Leere Antwort von der OpenAI-KI.")
                return "Entschuldigung, da ist gerade etwas schiefgelaufen. Lass uns gleich weitermachen."
            }
        } catch (e: Exception) {
            System.err.println(
                "[OpenAIProvider] Anfrage an OpenAI fehlgeschlagen " +
                "(z.B. Timeout oder Netzwerkproblem): ${e.message}"
            )
            return "Entschuldigung, ich habe dich gerade nicht ganz erreicht. Magst du das nochmal sagen?"
        }

        println("===== RAW RESPONSE FROM OpenAI-KI =====")
        println(text)
        println("====================================")

        // Antwort robust auslesen. Wenn der Server keine gueltige JSON-Antwort
        // schickt (z. B. bei falschem Modellnamen), nicht abstuerzen, sondern
        // eine verstaendliche Warnung loggen.
        val reply = try {
            JSONObject(text)
                .getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
        } catch (e: Exception) {
            System.err.println(
                "[OpenAIProvider] Antwort der OpenAI-KI konnte nicht gelesen werden. " +
                "Haeufigste Ursache: falscher Modellname in OPENAI_MODEL (.env) " +
                "oder ungueltiger Schluessel. Server-Antwort war: $text"
            )
            return "Entschuldigung, da ist gerade etwas schiefgelaufen. Lass uns gleich weitermachen."
        }

        // Antwort in History speichern
        MessageHistory.addAssistant(reply)

        return reply
    }
}