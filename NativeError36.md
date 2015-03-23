# Problem #

Bei Symbian-Handys (Nokia) tritt beim Versenden einer SMS sporadisch der Fehler "Native Error-36" auf.


# Lösungsvorschlag #

Laut den [Symbian Fehlercodes](http://wiki.forum.nokia.com/index.php/Symbian_OS_Error_Codes) handelt es sich dabei um einen Verbindungsabbruch. Warum oder wieso wird nicht erklärt.

Im Blog [technospot.net](http://www.technospot.net/blogs/how-to-sync-google-calendar-with-your-smartphone/) wurde folgende Maßnahme zur Behebung des Problems beschrieben:

  * Den für WebSMSsend benützten Zugangspunkt auswählen (meist "Internet") (Menu -> Einstellungen -> Einstellungen -> Verbindung -> Ziele)
  * Danach in das Menü Erweiterte Einstellungen gehen (-> Bearbeiten -> Optionen -> Erweiterte Einstellungen)
  * Dort die Proxyserver-Adresse löschen

Danach sollte es funktionieren. Bitte unten in den Kommentaren Feedback geben.