/**
 * Wartemodus, wenn niemand anwesend ist.
 * Furhat bleibt passiv, bis eine Person in Reichweite erkannt wird.
 *
 * Diese Datei muss normalerweise NICHT verändert werden.
 */

package furhatos.app.furhat.flow.main

import furhatos.flow.kotlin.State
import furhatos.flow.kotlin.furhat
import furhatos.flow.kotlin.onUserEnter
import furhatos.flow.kotlin.state

val Idle: State = state {
    onEntry {
        furhat.attendNobody()
    }

    onUserEnter {
        furhat.attend(it)
        goto(Waiting)
    }
}
