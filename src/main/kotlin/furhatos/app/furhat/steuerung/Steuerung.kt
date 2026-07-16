package furhatos.app.furhat.steuerung

import furhatos.app.furhat.setting.BETRIEBS_MODUS
import furhatos.app.furhat.setting.Betriebsmodus
import furhatos.app.furhat.setting.SatzModus

/**
 * Laufzeit-Status der Steuerung. Hält die aktuell gültigen Einstellungen, die
 * über die Weboberfläche live verändert werden können, sowie den Gesprächs-
 * zustand und die Zeitmessung (für die Restzeit-Anzeige).
 *
 * Wird sowohl vom Gesprächs-Flow (conversation.kt) als auch vom Webserver
 * (SteuerServer.kt) aus verschiedenen Threads gelesen/geschrieben, daher
 * @Volatile.
 *
 * Diese Datei muss normalerweise NICHT verändert werden. Die anpassbaren
 * Texte und Voreinstellungen stehen in "_HIER_ANPASSEN/Einstellungen.kt".
 */
object Steuerung {

    enum class Zustand { BEREIT, LAEUFT, PAUSIERT }

    @Volatile var zustand: Zustand = Zustand.BEREIT
    @Volatile var einleitungModus: SatzModus = SatzModus.KI
    @Volatile var schlussModus: SatzModus = SatzModus.KI
    @Volatile var timerAktiv: Boolean = true

    @Volatile private var startMs: Long = 0L
    @Volatile private var pausiertGesamtMs: Long = 0L
    @Volatile private var pauseBeginnMs: Long = 0L

    /** Setzt die Voreinstellungen passend zum Betriebsmodus (einmal beim Start). */
    fun initFromMode() {
        when (BETRIEBS_MODUS) {
            Betriebsmodus.STUDIE -> {
                einleitungModus = SatzModus.KI
                schlussModus = SatzModus.KI
                timerAktiv = true
            }
            Betriebsmodus.MESSE -> {
                einleitungModus = SatzModus.FEST
                schlussModus = SatzModus.KI
                timerAktiv = false
            }
        }
    }

    /** Beginn eines neuen Gesprächs: Zeitmessung zurücksetzen. */
    fun gespraechBegonnen() {
        startMs = System.currentTimeMillis()
        pausiertGesamtMs = 0L
        pauseBeginnMs = 0L
        zustand = Zustand.LAEUFT
    }

    fun pausieren() {
        if (zustand == Zustand.LAEUFT) {
            pauseBeginnMs = System.currentTimeMillis()
            zustand = Zustand.PAUSIERT
        }
    }

    fun fortsetzen() {
        if (zustand == Zustand.PAUSIERT) {
            pausiertGesamtMs += System.currentTimeMillis() - pauseBeginnMs
            zustand = Zustand.LAEUFT
        }
    }

    fun bereit() {
        zustand = Zustand.BEREIT
    }

    /** Verstrichene Gesprächszeit in Millisekunden, Pausen herausgerechnet. */
    fun verstricheneMs(): Long {
        if (zustand == Zustand.BEREIT) return 0L
        val jetzt = System.currentTimeMillis()
        val pausiert = pausiertGesamtMs +
            if (zustand == Zustand.PAUSIERT) jetzt - pauseBeginnMs else 0L
        return jetzt - startMs - pausiert
    }
}
