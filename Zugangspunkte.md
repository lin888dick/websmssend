# Problem #

Das Versenden von SMS funktioniert im WLAN problemlos, aber nicht per UMTS/EDGE

# Lösungsvorschläge #

Um die Geschwindigkeit und den Traffic beim mobilen Surfen zu reduzieren, komprimieren die meisten Provider (Vodafone, T-Mobile) die zu übertragende Website und entfernen bestimmte Inhalte (das nennt sich dann "optimieren").
<br>Dies führt jedoch dazu, dass WebSMSsend die Website nicht mehr erkennt, da sie verändert wurde.<br>
<br>
<b>T-Mobile</b>

<ul><li>Mit dem Handy auf <a href='http://speed.t-mobile.de'>http://speed.t-mobile.de</a> surfen<br>
</li><li>Dort den Speedmanager abschalten</li></ul>

Da die Entwickler keine T-Mobile-Kunden sind, konnte diese Anleitung nicht überprüft werden. Falls sie nicht funktioniert, einfach eine E-Mail schreiben oder einen Kommentar hinterlassen.<br>
<br>
<b>Vodafone</b>

<ul><li>Die Paketdaten-Verbindung so konfigurieren, dass webSMSsend den APN web.vodafone.de benutzt<br>
</li><li>Mit dem Handy auf <a href='http://performance.vodafone.de'>http://performance.vodafone.de</a> surfen<br>
</li><li>Dort die Komprimierung komplett deaktivieren<br>
</li><li>Ab Version 0.63.0 können GMX-SMS mit webSMSsend unabhängig von der Wahl des Zugangspunktes und der Komprimierungseinstellung versendet werden