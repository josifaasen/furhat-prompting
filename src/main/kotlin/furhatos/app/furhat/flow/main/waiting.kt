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

import furhatos.app.furhat.setting.GREETING_TEXT
import furhatos.app.furhat.setting.START_BEHAVIOR
import furhatos.app.furhat.setting.StartBehavior
import furhatos.flow.kotlin.*

object StudyFlags {
    var greetingShown: Boolean = false
}

val Waiting = state {
    onEntry {
        when (START_BEHAVIOR) {
            StartBehavior.DIREKT -> {
                goto(Conversation)
            }
            StartBehavior.BEGRUESSUNG_DANN_START -> {
                if (!StudyFlags.greetingShown) {
                    StudyFlags.greetingShown = true
                    furhat.say(GREETING_TEXT)
                }
                // Warten auf den Start-Trigger (Browser-Adresse /start),
                // siehe onEvent unten.
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

    onEvent("START_STUDY_DIALOGUE") {
        StudyFlags.greetingShown = false
        goto(Conversation)
    }
}
