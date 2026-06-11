package furhatos.app.furhat.util

import io.github.cdimascio.dotenv.dotenv

/**
 * Liest die Zugangsdaten der KI (API-Keys, Modellnamen).
 *
 * Die Werte werden in dieser Reihenfolge gesucht (erster Treffer gewinnt):
 *   1. System-Umgebungsvariable      (z.B. UNI_API_KEY in der Shell gesetzt)
 *   2. JVM-System-Property            (z.B. -DUNI_API_KEY=... beim Start)
 *   3. ".env"-Datei im Dateisystem    (lokal in IntelliJ / virtueller Furhat)
 *   4. ".env"-Datei in der Jar/Skill  (eingepackt fuer den echten Roboter)
 *
 * Punkt 4 ist der entscheidende Unterschied: Auf dem echten Roboter laeuft
 * nur die hochgeladene ".skill"-Datei. Dort gibt es keine ".env" im
 * Dateisystem. Damit der Key trotzdem ankommt, wird die ".env" beim Bauen
 * (shadowJar) mit in die Skill-Datei gepackt und hier aus dem Classpath
 * gelesen.
 */
object EnvConfig {

    // ".env" aus dem Dateisystem (Arbeitsverzeichnis). Funktioniert lokal.
    private val dotenv = dotenv {
        ignoreIfMissing = true
    }

    // ".env", die beim Bauen in die Jar/Skill gepackt wurde (Classpath).
    private val bundled: Map<String, String> by lazy { loadBundledEnv() }

    // Platzhalter-Werte aus der .env.example. Wenn sie nicht ersetzt
    // wurden, gilt der Key als nicht gesetzt.
    private val placeholders = setOf(
        "",
        "your-api-key-here",
        "your-openai-api-key-here",
        "your-uni-api-key-here"
    )

    /** Sucht einen Wert in allen Quellen (siehe Klassen-Doku). */
    private fun lookup(key: String): String? =
        System.getenv(key)
            ?: System.getProperty(key)
            ?: dotenv[key]
            ?: bundled[key]

    private fun cleanKey(raw: String?): String {
        val trimmed = raw?.trim() ?: return ""
        return if (trimmed in placeholders) "" else trimmed
    }

    /** Liest eine eingepackte ".env" aus dem Classpath (falls vorhanden). */
    private fun loadBundledEnv(): Map<String, String> {
        val stream = EnvConfig::class.java.getResourceAsStream("/.env")
            ?: return emptyMap()
        return stream.bufferedReader(Charsets.UTF_8).useLines { lines ->
            lines
                .map { it.trim() }
                .filter { it.isNotEmpty() && !it.startsWith("#") && it.contains("=") }
                .mapNotNull { line ->
                    val idx = line.indexOf('=')
                    val k = line.substring(0, idx).trim()
                    var v = line.substring(idx + 1).trim()
                    // Optionale Anfuehrungszeichen entfernen.
                    if (v.length >= 2 &&
                        ((v.startsWith("\"") && v.endsWith("\"")) ||
                         (v.startsWith("'") && v.endsWith("'")))
                    ) {
                        v = v.substring(1, v.length - 1)
                    }
                    if (k.isEmpty()) null else k to v
                }
                .toMap()
        }
    }

    val openaiApiKey: String
        get() = cleanKey(lookup("OPENAI_API_KEY"))

    val openaiModel: String
        get() = lookup("OPENAI_MODEL")?.trim().orEmpty().ifBlank { "gpt-5.1" }

    val uniApiKey: String
        get() = cleanKey(lookup("UNI_API_KEY"))

    val uniModel: String
        get() = lookup("UNI_MODEL")?.trim().orEmpty().ifBlank { "qwen3-30b-a3b-instruct-2507" }

    /** Convenience-Checks fuer Init-Warnungen */
    val hasOpenaiKey: Boolean get() = openaiApiKey.isNotEmpty()
    val hasUniKey:    Boolean get() = uniApiKey.isNotEmpty()
}
