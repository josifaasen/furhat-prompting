package furhatos.app.furhat.flow.main

import furhatos.flow.kotlin.furhat
import furhatos.flow.kotlin.state

val EndState = state {
    onEntry {
        // Conversation (oder UI) hat den Abschluss bereits gesprochen.
        terminate()
    }
}
