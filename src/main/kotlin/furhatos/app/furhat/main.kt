/**
 * ═══════════════════════════════════════════════════════════════════════════
 * HAUPT-EINSTIEGSPUNKT DES FURHAT-SKILLS
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Diese Datei ist der **Startpunkt** des gesamten Systems.
 * Wenn der Skill auf Furhat geladen wird, wird diese Datei als erstes ausgeführt.
 *
 * ## Was passiert beim Start?
 *
 * 1. Furhat SDK lädt den Skill
 * 2. `main()` Funktion wird aufgerufen
 * 3. `FurhatSkill.start()` wird ausgeführt
 * 4. Flow-System startet mit `Init` State (siehe flow/init.kt)
 * 5. Von dort geht es weiter zu `Waiting` → `Conversation` → `EndState`
 *
 * ## Architektur-Überblick
 *
 * ```
 * main.kt (Startpunkt)
 *   └─> Init State (flow/init.kt)
 *        ├─> Waiting State (flow/main/waiting.kt)
 *        └─> Conversation State (flow/main/conversation.kt)
 *             └─> End State (flow/main/end.kt)
 * ```
 *
 * ## Wann diese Datei ändern?
 *
 * **SELTEN!** Diese Datei ist sehr stabil und muss normalerweise **nicht** geändert werden.
 *
 * Typische Änderungen sind:
 * - Niemals (diese Datei ist Standard-Furhat-Code)
 *
 * ## Was bei Problemen tun?
 *
 * Falls der Skill gar nicht startet:
 * 1. Prüfe, ob `Init` State in flow/init.kt existiert
 * 2. Prüfe Gradle-Build (shadowJar)
 * 3. Prüfe Furhat SDK Connection
 *
 * ---
 *
 * @see furhatos.app.furhat.flow.Init für die nächste Station im Flow
 */

package furhatos.app.furhat

import furhatos.app.furhat.api.TrustConfig
import furhatos.app.furhat.flow.Init
import furhatos.flow.kotlin.Flow
import furhatos.skills.Skill

class FurhatSkill : Skill() {
    override fun start() {
        // Zusaetzliche Stammzertifikate JVM-weit aktivieren, BEVOR irgendeine
        // HTTPS-Verbindung aufgebaut wird. Behebt "PKIX path building failed"
        // auf Systemen (z.B. dem echten Roboter), deren Java die neueren
        // Zertifikate nicht kennt.
        TrustConfig.installGlobally()

        Flow().run(Init)
    }
}

fun main(args: Array<String>) {
    Skill.main(args)
}
