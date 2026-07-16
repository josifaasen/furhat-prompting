/**
 * Wartemodus, wenn niemand anwesend ist.
 * Furhat bleibt passiv, bis eine Person in Reichweite erkannt wird
 * (oder das Gespräch über die Weboberfläche gestartet wird).
 *
 * Nach einem beendeten Gespräch (Modus MESSE) landet der Roboter wieder hier
 * und ist damit bereit für die nächste Person.
 *
 * Diese Datei muss normalerweise NICHT verändert werden.
 */

package furhatos.app.furhat.flow.main

import furhatos.flow.kotlin.*

val Idle: State = state {
    onEntry {
        furhat.attendNobody()
    }

    onUserEnter {
        furhat.attend(it)
        goto(Waiting)
    }

    // Start-Knopf der Weboberflaeche: Gespraech auch ohne neue Person starten.
    onEvent("STEUER_START") {
        goto(Conversation)
    }
}
