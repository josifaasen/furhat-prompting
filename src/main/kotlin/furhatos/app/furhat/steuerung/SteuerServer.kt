package furhatos.app.furhat.steuerung

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import furhatos.app.furhat.setting.BETRIEBS_MODUS
import furhatos.app.furhat.setting.Betriebsmodus
import furhatos.app.furhat.setting.CONVERSATION_TIME_LIMIT_MS
import furhatos.app.furhat.setting.SatzModus
import java.net.InetSocketAddress
import java.util.concurrent.Executors

/**
 * Kleiner Webserver für das Steuerpult (Weboberfläche). Läuft auf demselben
 * Port wie früher die /start-Adresse. Die Knöpfe der Oberfläche lösen Events
 * im Gespräch aus (Start/Pause/Weiter/Beenden); die Schalter ändern den
 * Laufzeit-Status in "Steuerung".
 *
 * "sendeEvent" ist ein Callback, der ein benanntes Event in den Flow schickt.
 * Er wird in init.kt im Flow-Kontext erzeugt (dort ist send() verfügbar) und
 * ist threadsicher auch aus den HTTP-Handlern heraus aufrufbar.
 *
 * Diese Datei muss normalerweise NICHT verändert werden.
 */
fun starteSteuerpult(sendeEvent: (String) -> Unit, port: Int) {
    val server = HttpServer.create(InetSocketAddress(port), 0)

    // Startseite (Weboberfläche)
    server.createContext("/") { ex -> sende(ex, "text/html; charset=utf-8", SEITE_HTML) }

    // Aktionen -> Events in den Flow
    server.createContext("/start")        { ex -> sendeEvent("STEUER_START");   sende(ex, "text/plain", "ok") }
    server.createContext("/api/start")    { ex -> sendeEvent("STEUER_START");   sende(ex, "text/plain", "ok") }
    server.createContext("/api/pause")    { ex -> sendeEvent("STEUER_PAUSE");   sende(ex, "text/plain", "ok") }
    server.createContext("/api/weiter")   { ex -> sendeEvent("STEUER_WEITER");  sende(ex, "text/plain", "ok") }
    server.createContext("/api/beenden")  { ex -> sendeEvent("STEUER_BEENDEN"); sende(ex, "text/plain", "ok") }

    // Schalter -> Laufzeit-Status
    server.createContext("/api/timer") { ex ->
        val an = query(ex, "an")
        if (an != null) Steuerung.timerAktiv = (an == "true" || an == "an" || an == "1")
        sende(ex, "text/plain", "ok")
    }
    server.createContext("/api/einleitung") { ex ->
        modusAus(query(ex, "modus"))?.let { Steuerung.einleitungModus = it }
        sende(ex, "text/plain", "ok")
    }
    server.createContext("/api/schluss") { ex ->
        modusAus(query(ex, "modus"))?.let { Steuerung.schlussModus = it }
        sende(ex, "text/plain", "ok")
    }

    // Status (wird von der Oberfläche jede Sekunde abgefragt)
    server.createContext("/api/status") { ex -> sende(ex, "application/json; charset=utf-8", statusJson()) }

    server.executor = Executors.newSingleThreadExecutor()
    server.start()
    System.err.println("[Steuerpult] Weboberflaeche laeuft auf Port $port (http://localhost:$port/)")
}

private fun query(ex: HttpExchange, key: String): String? {
    val q = ex.requestURI.query ?: return null
    return q.split("&")
        .map { it.split("=", limit = 2) }
        .firstOrNull { it.size == 2 && it[0] == key }
        ?.get(1)
}

private fun modusAus(s: String?): SatzModus? = when (s?.lowercase()) {
    "fest" -> SatzModus.FEST
    "ki" -> SatzModus.KI
    else -> null
}

private fun sende(ex: HttpExchange, contentType: String, body: String) {
    val bytes = body.toByteArray(Charsets.UTF_8)
    ex.responseHeaders.add("Content-Type", contentType)
    ex.sendResponseHeaders(200, bytes.size.toLong())
    ex.responseBody.use { it.write(bytes) }
}

private fun statusJson(): String {
    val zustand = when (Steuerung.zustand) {
        Steuerung.Zustand.BEREIT -> "bereit"
        Steuerung.Zustand.LAEUFT -> "laeuft"
        Steuerung.Zustand.PAUSIERT -> "pausiert"
    }
    val restSek = if (Steuerung.timerAktiv) {
        ((CONVERSATION_TIME_LIMIT_MS - Steuerung.verstricheneMs()).coerceAtLeast(0L) / 1000L).toString()
    } else {
        "0"
    }
    val einl = if (Steuerung.einleitungModus == SatzModus.FEST) "fest" else "ki"
    val schl = if (Steuerung.schlussModus == SatzModus.FEST) "fest" else "ki"
    val modus = if (BETRIEBS_MODUS == Betriebsmodus.MESSE) "messe" else "studie"
    return """{"zustand":"$zustand","timerAktiv":${Steuerung.timerAktiv},"restSek":$restSek,"einleitung":"$einl","schluss":"$schl","modus":"$modus"}"""
}

/** Die komplette Weboberfläche als eine HTML-Seite (mobil bedienbar). */
private val SEITE_HTML = """
<!DOCTYPE html>
<html lang="de">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>Furhat-Steuerpult</title>
<style>
 body{font-family:system-ui,Arial,sans-serif;margin:0;background:#f3f4f6;color:#111;}
 .wrap{max-width:520px;margin:0 auto;padding:16px;}
 h1{font-size:20px;text-align:center;}
 .status{background:#fff;border-radius:12px;padding:14px;margin-bottom:16px;text-align:center;box-shadow:0 1px 3px rgba(0,0,0,.1);}
 .status .z{font-size:22px;font-weight:700;}
 .status .t{color:#555;margin-top:4px;}
 .btns{display:grid;grid-template-columns:1fr 1fr;gap:12px;margin-bottom:16px;}
 button{font-size:17px;padding:16px;border:none;border-radius:12px;color:#fff;cursor:pointer;}
 button:active{opacity:.8;}
 button:disabled{opacity:.4;cursor:not-allowed;}
 .start{background:#16a34a;} .pause{background:#d97706;} .weiter{background:#2563eb;} .beenden{background:#dc2626;}
 .full{grid-column:1/3;}
 .opt{background:#fff;border-radius:12px;padding:12px 14px;box-shadow:0 1px 3px rgba(0,0,0,.1);}
 .row{display:flex;justify-content:space-between;align-items:center;padding:10px 0;border-bottom:1px solid #eee;}
 .row:last-child{border-bottom:none;}
 .seg{display:flex;gap:6px;}
 .seg button{padding:8px 12px;font-size:14px;background:#e5e7eb;color:#111;}
 .seg button.on{background:#2563eb;color:#fff;}
 .hint{color:#777;font-size:12px;text-align:center;margin-top:14px;}
</style>
</head>
<body>
<div class="wrap">
 <h1>Furhat-Steuerpult</h1>
 <div class="status">
   <div class="z" id="zustand">…</div>
   <div class="t" id="zeit"></div>
 </div>
 <div class="btns">
   <button class="start full" id="btn-start" onclick="api('/start')">Start / Neues Gespräch</button>
   <button class="pause" onclick="api('/pause')">Pause</button>
   <button class="weiter" onclick="api('/weiter')">Weiter</button>
   <button class="beenden full" onclick="api('/beenden')">Beenden</button>
 </div>
 <div class="opt">
   <div class="row">
     <span>Timer</span>
     <div class="seg"><button id="timer-an">an</button><button id="timer-aus">aus</button></div>
   </div>
   <div class="row">
     <span>Einleitung</span>
     <div class="seg"><button id="einl-fest">fester Satz</button><button id="einl-ki">KI</button></div>
   </div>
   <div class="row">
     <span>Schluss</span>
     <div class="seg"><button id="schl-fest">fester Satz</button><button id="schl-ki">KI</button></div>
   </div>
 </div>
 <div class="hint" id="modus"></div>
</div>
<script>
function api(p){fetch('/api'+p).then(function(){setTimeout(refresh,150);});}
function setTimer(an){fetch('/api/timer?an='+an).then(refresh);}
function setEinl(m){fetch('/api/einleitung?modus='+m).then(refresh);}
function setSchl(m){fetch('/api/schluss?modus='+m).then(refresh);}
document.getElementById('timer-an').onclick=function(){setTimer('true');};
document.getElementById('timer-aus').onclick=function(){setTimer('false');};
document.getElementById('einl-fest').onclick=function(){setEinl('fest');};
document.getElementById('einl-ki').onclick=function(){setEinl('ki');};
document.getElementById('schl-fest').onclick=function(){setSchl('fest');};
document.getElementById('schl-ki').onclick=function(){setSchl('ki');};
function mmss(s){s=parseInt(s,10);var m=Math.floor(s/60),r=s%60;return m+':'+(r<10?'0':'')+r;}
function toggle(id,on){document.getElementById(id).classList.toggle('on',!!on);}
function refresh(){fetch('/api/status').then(function(r){return r.json();}).then(function(s){
  var z={bereit:'Bereit',laeuft:'Gespräch läuft',pausiert:'Pausiert'}[s.zustand]||s.zustand;
  document.getElementById('zustand').textContent=z;
  document.getElementById('zeit').textContent=s.timerAktiv?('Restzeit: '+mmss(s.restSek)):'Timer aus';
  toggle('timer-an',s.timerAktiv); toggle('timer-aus',!s.timerAktiv);
  toggle('einl-fest',s.einleitung==='fest'); toggle('einl-ki',s.einleitung==='ki');
  toggle('schl-fest',s.schluss==='fest'); toggle('schl-ki',s.schluss==='ki');
  document.getElementById('modus').textContent='Betriebsmodus: '+(s.modus==='messe'?'Messe':'Studie');
  document.getElementById('btn-start').disabled=(s.zustand==='laeuft'||s.zustand==='pausiert');
});}
setInterval(refresh,1000); refresh();
</script>
</body>
</html>
""".trimIndent()
