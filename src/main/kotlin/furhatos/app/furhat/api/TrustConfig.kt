package furhatos.app.furhat.api

import okhttp3.OkHttpClient
import java.security.KeyStore
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

/**
 * Sorgt dafür, dass die HTTPS-Verbindung zur KI auch dann funktioniert, wenn
 * das verwendete Java zu alt ist und das Zertifikat des Servers nicht kennt.
 * Typischer Fehler in dem Fall:
 *   "PKIX path building failed ... unable to find valid certification path".
 *
 * Es werden bis zu drei Vertrauensquellen kombiniert (nichts wird unsicher
 * abgeschaltet, es kommen nur zusätzliche, vertrauenswürdige Quellen dazu):
 *
 *   1. Der normale, im Java hinterlegte Zertifikatsspeicher.
 *   2. Der Windows-Zertifikatsspeicher (nur auf Windows vorhanden; hilft beim
 *      virtuellen Furhat auf einem Windows-PC).
 *   3. Der in die Skill eingepackte, aktuelle Standard-Zertifikatssatz
 *      (Datei "certs/cacert.pem" in der Jar, siehe build.gradle ->
 *      downloadCaCerts). Das ist die plattformunabhängige Quelle und behebt
 *      den PKIX-Fehler auch auf dem echten Roboter, der keinen Windows-
 *      Speicher hat und dessen Java die neueren Zertifikate evtl. nicht kennt.
 *
 * Wichtig: Über `installGlobally()` wird dieses kombinierte Vertrauen als
 * JVM-Standard gesetzt. Dadurch profitieren ALLE HTTPS-Verbindungen davon,
 * nicht nur unser eigener okhttp-Client – also auch Aufrufe, die das Furhat-
 * Framework selbst über `HttpsURLConnection` macht.
 */
object TrustConfig {

    private val compositeTrustManager: X509TrustManager by lazy { buildCompositeTrustManager() }

    private fun newSslContext(): SSLContext {
        val ssl = SSLContext.getInstance("TLS")
        ssl.init(null, arrayOf<TrustManager>(compositeTrustManager), null)
        return ssl
    }

    /** Hängt die zusätzlichen Vertrauensquellen an einen OkHttp-Client-Builder. */
    fun apply(builder: OkHttpClient.Builder): OkHttpClient.Builder {
        return try {
            builder.sslSocketFactory(newSslContext().socketFactory, compositeTrustManager)
        } catch (e: Exception) {
            System.err.println(
                "[TrustConfig] Zusatz-Vertrauensspeicher nicht verfuegbar, " +
                "nutze Standard. " + e.message
            )
            builder
        }
    }

    /**
     * Setzt das kombinierte Vertrauen als JVM-weiten Standard. Sollte einmal
     * möglichst früh beim Start aufgerufen werden (siehe Init.kt). So gilt es
     * auch für HTTPS-Verbindungen außerhalb unseres okhttp-Clients.
     */
    fun installGlobally() {
        try {
            val ssl = newSslContext()
            SSLContext.setDefault(ssl)
            HttpsURLConnection.setDefaultSSLSocketFactory(ssl.socketFactory)
            System.err.println("[TrustConfig] Zusaetzliche Stammzertifikate JVM-weit aktiviert.")
        } catch (e: Exception) {
            System.err.println(
                "[TrustConfig] JVM-weites Vertrauen konnte nicht gesetzt werden, " +
                "nutze Standard. " + e.message
            )
        }
    }

    private fun trustManagerFor(ks: KeyStore?): X509TrustManager {
        val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        tmf.init(ks)
        return tmf.trustManagers.filterIsInstance<X509TrustManager>().first()
    }

    /** Trust-Manager aus dem Windows-Zertifikatsspeicher (falls vorhanden). */
    private fun windowsTrustManager(): X509TrustManager? = try {
        val ks = KeyStore.getInstance("Windows-ROOT")
        ks.load(null, null)
        trustManagerFor(ks)
    } catch (e: Exception) {
        null
    }

    /**
     * Trust-Manager aus dem in die Skill eingepackten Zertifikatssatz
     * (Classpath "/certs/cacert.pem"). Liefert null, wenn nichts eingepackt
     * wurde oder die Datei nicht gelesen werden kann.
     */
    private fun bundledTrustManager(): X509TrustManager? = try {
        val factory = CertificateFactory.getInstance("X.509")
        val ks = KeyStore.getInstance(KeyStore.getDefaultType())
        ks.load(null, null)
        var count = 0
        // Mehrere moegliche Namen unterstuetzen (Bundle oder einzelne Roots).
        for (resource in listOf(
            "/certs/cacert.pem",
            "/certs/isrgrootx1.pem",
            "/certs/isrg-root-x2.pem",
            // Optionales, selbst hinterlegtes Zertifikat (z.B. ein Uni-/
            // Firmen-Proxy-Stammzertifikat). Siehe README. Wird nur genutzt,
            // wenn die Datei "extra-cert.pem" vorhanden war.
            "/certs/extra.pem"
        )) {
            val stream = TrustConfig::class.java.getResourceAsStream(resource) ?: continue
            stream.use { s ->
                for (cert in factory.generateCertificates(s)) {
                    ks.setCertificateEntry("bundled-$count", cert)
                    count++
                }
            }
        }
        if (count == 0) null else trustManagerFor(ks)
    } catch (e: Exception) {
        System.err.println("[TrustConfig] Gebuendelte Zertifikate nicht lesbar: " + e.message)
        null
    }

    private fun buildCompositeTrustManager(): X509TrustManager {
        val defaultTm = trustManagerFor(null)
        val extraTms = listOfNotNull(windowsTrustManager(), bundledTrustManager())

        return object : X509TrustManager {
            override fun checkServerTrusted(chain: Array<out X509Certificate>, authType: String) {
                try {
                    defaultTm.checkServerTrusted(chain, authType)
                    return
                } catch (primary: Exception) {
                    for (tm in extraTms) {
                        try {
                            tm.checkServerTrusted(chain, authType)
                            return
                        } catch (ignored: Exception) {
                            // naechste Quelle versuchen
                        }
                    }
                    throw primary
                }
            }

            override fun checkClientTrusted(chain: Array<out X509Certificate>, authType: String) {
                defaultTm.checkClientTrusted(chain, authType)
            }

            override fun getAcceptedIssuers(): Array<X509Certificate> {
                var issuers = defaultTm.acceptedIssuers
                for (tm in extraTms) {
                    issuers += tm.acceptedIssuers
                }
                return issuers
            }
        }
    }
}
