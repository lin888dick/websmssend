_**Die folgende Anleitung gilt bis einschließlich webSMSsend 0.62.7. Ab Version 0.63.0 funktioniert der SMS-Versand über GMX unabhängig von der eingestellten Startansicht.**_

# Einleitung #

Damit websmssend mit den GMX-Bezahl-Accounts funktioniert muss die Startansicht "Standard" aktiviert sein.

# Details #

Falls eine andere Startansicht ausgewählt ist, tritt der Fehler "RegexStringMatch" auf. Hier die kompletten Debug-Meldungen:

  * 22394: Senden pressed
  * 22501: SendSMSGMX()
  * 23041: Login wird geladen...
  * 23066: httpHandler( POST, http://service.gmx.net/de/cgi/login, service.gmx.net, postReq, true )
  * 24162: httpHandler( GET, http://service.gmx.net/de/cgi/g.fcgi/mail/index?site=greetings&folder=inbox&CUSTOMERNO=*******&t=de656231762.1287249775.3899f28a&lALIAS=&lDOMAIN=&lLASTLOGIN=2010%2D10%2D16+19%3A21%3A20, service.gmx.net, postReq, true )
  * 25472: httpHandler( POST, http://service.gmx.net/de/cgi/login, service.gmx.net, postReq, true )
  * 25900: httpHandler( GET, http://service.gmx.net/de/cgi/g.fcgi/mail/index?site=greetings&folder=inbox&CUSTOMERNO=********&t=de763134510.1287249777.32f83df6&lALIAS=&lDOMAIN=&lLASTLOGIN=2010%2D10%2D16+19%3A22%3A55, service.gmx.net, postReq, true )
  * 27258: Login erfolgreich
  * 27701: java.lang.Exception: Fehler! RegexStringMatch Fehler! RegexStringMatch

Lösung: Unter "Mein GMX" muss als Startansicht "Standard" aktiviert sein und nicht z.b. "Posteingang".