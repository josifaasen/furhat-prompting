/**
 * Initialzustand des Gesprächssystems.
 *
 * Dieser Zustand setzt grundlegende Parameter der Interaktion:
 * - Sprache der Furhat-Stimme
 * - Engagement-Policy (wann Furhat eine Person wahrnimmt)
 * - kleiner HTTP-Server für den manuellen Start-Trigger
 * - Übergang in den passenden Startzustand (Idle oder Waiting)
 *
 * Diese Datei muss normalerweise NICHT verändert werden.
 * Alle einstellbaren Werte stehen in der Datei "Einstellungen.kt".
 */

package furhatos.app.furhat.flow

import furhatos.app.furhat.flow.main.Idle
import furhatos.app.furhat.flow.main.Waiting
import furhatos.app.furhat.setting.DISTANCE_TO_ENGAGE
import furhatos.app.furhat.setting.LLM_PROVIDER
import furhatos.app.furhat.setting.LLMProviderType
import furhatos.app.furhat.setting.MAX_NUMBER_OF_USERS
import furhatos.app.furhat.setting.START_TRIGGER_PORT
import furhatos.app.furhat.steuerung.Steuerung
import furhatos.app.furhat.steuerung.starteSteuerpult
import furhatos.app.furhat.util.EnvConfig
import furhatos.flow.kotlin.State
import furhatos.flow.kotlin.furhat
import furhatos.flow.kotlin.state
import furhatos.flow.kotlin.users
import furhatos.util.Language

val Init: State = state {
    init {
        // Pruefen, ob fuer den ausgewaehlten KI-Anbieter ein API-Key in der
        // .env hinterlegt ist. Wenn nicht: klare Warnung in der Konsole.
        val (providerName, hasKey, envVar) = when (LLM_PROVIDER) {
            LLMProviderType.UNI    -> Triple("UDE Academic Cloud", EnvConfig.hasUniKey,    "UNI_API_KEY")
            LLMProviderType.OPENAI -> Triple("OpenAI",             EnvConfig.hasOpenaiKey, "OPENAI_API_KEY")
        }
        if (!hasKey) {
            System.err.println(
                "[Init] WARNUNG: LLM_PROVIDER = $providerName, aber $envVar ist " +
                "nicht in der .env hinterlegt. Der Roboter kann keine KI-Antworten " +
                "erzeugen. Bitte .env.example nach .env kopieren und den Schluessel " +
                "eintragen."
            )
        }

        // Voreinstellungen passend zum Betriebsmodus (STUDIE/MESSE) setzen.
        Steuerung.initFromMode()

        // Wann nimmt Furhat eine Person als Gespraechspartner wahr?
        users.setSimpleEngagementPolicy(DISTANCE_TO_ENGAGE, MAX_NUMBER_OF_USERS)

        // Weboberflaeche / Steuerpult starten:
        //   http://localhost:<PORT>/        (virtueller Furhat)
        //   http://<roboter-ip>:<PORT>/     (physischer Furhat)
        // Die alte Adresse /start funktioniert weiterhin (startet das Gespraech).
        // Der send-Callback wird hier im Flow-Kontext erzeugt (send ist hier
        // verfuegbar) und ist threadsicher aus den HTTP-Handlern aufrufbar.
        starteSteuerpult({ eventName -> send(eventName) }, START_TRIGGER_PORT)
    }

    onEntry {
        furhat.setInputLanguage(Language.GERMAN)
        when {
            furhat.isVirtual() -> goto(Waiting)
            users.hasAny() -> {
                furhat.attend(users.random)
                goto(Waiting)
            }
            else -> goto(Idle)
        }
    }
}
