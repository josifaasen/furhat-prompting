/**
 * Gemeinsame Schnittstelle für alle Sprachmodelle (OpenAI, Uni-KI).
 * Sie erlaubt, dass der Skill dynamisch zwischen verschiedenen KI-Anbietern
 * wechseln kann, ohne den restlichen Code zu verändern.
 */

package furhatos.app.furhat.api

interface LLMProvider {
    fun ask(prompt: String): String
}