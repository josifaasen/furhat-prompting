/**
 * Zustand direkt vor dem eigentlichen Gespräch.
 *
 * Das Verhalten hier hängt von der Einstellung START_BEHAVIOR ab
 * (siehe Datei "Einstellungen.kt"):
 *
 * - DIREKT: Das Gespräch startet sofort. Der Roboter sagt direkt seinen
 *   ersten Satz und hört dann zu.
 *
 * - BEGRUESSUNG_DANN_START: Der Roboter sagt erst einen kurzen Begrüßungs-
 *   bzw. Wartesatz und beginnt das eigentliche Gespräch erst, wenn die
 *   Start-Adresse im Browser aufgerufen wird
 *   (http://localhost:<PORT>/start).
 */

package furhatos.app.furhat.flow.main

import furhatos.app.furhat.setting.BETRIEBS_MODUS
import furhatos.app.furhat.setting.Betriebsmodus
import furhatos.app.furhat.setting.GREETING_TEXT
import furhatos.app.furhat.setting.START_BEHAVIOR
import furhatos.app.furhat.setting.StartBehavior
import furhatos.flow.kotlin.*

object StudyFlags {
    var greetingShown: Boolean = false
}

val Waiting = state {
    onEntry {
        when (BETRIEBS_MODUS) {
            // MESSE: automatisch starten, sobald eine Person da ist.
            Betriebsmodus.MESSE -> goto(Conversation)

            // STUDIE: wie bisher, je nach Startverhalten.
            Betriebsmodus.STUDIE -> when (START_BEHAVIOR) {
                StartBehavior.DIREKT -> goto(Conversation)
                StartBehavior.BEGRUESSUNG_DANN_START -> {
                    if (!StudyFlags.greetingShown) {
                        StudyFlags.greetingShown = true
                        furhat.say(GREETING_TEXT)
                    }
                    // Warten auf den Start (Knopf in der Weboberflaeche bzw.
                    // Adresse /start), siehe onEvent unten.
                }
            }
        }
    }

    onUserEnter {
        furhat.attend(it)
    }

    onUserLeave {
        if (!users.hasAny()) {
            furhat.attendNobody()
            goto(Idle)
        }
    }

    // Start-Knopf der Weboberflaeche (bzw. Adresse /start).
    onEvent("STEUER_START") {
        StudyFlags.greetingShown = false
        goto(Conversation)
    }
}
