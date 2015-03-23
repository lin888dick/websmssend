# Release Notes #

Die aktuellen Release Notes zu webSMSsend

## 0.80.1 ##

  * O2: funktioniert wieder


---


## 0.80.0 ##

  * GMX: funktioniert wieder. Die neue API der GMX-Android-App wird nun verwendet. Dafür Dank an Felix Bechstein's WebSMS-Projekt (http://code.google.com/p/websmsdroid/) für die Vorlage

## 0.75.0 ##

  * Sipgate: Es ist nun möglich SMS mit einem Sipgate-Account zu versenden ([Issue 34](https://code.google.com/p/websmssend/issues/detail?id=34))
  * Allgemein: Das Nokia N8 scheint WebSMSsend (auch alte Versionen) nicht ausführen zu können

## 0.71.0 ##

  * O2: Nach Änderungen auf der O2-Homepage musste der O2-Konnektor angepasst werden. ([Issue 52](https://code.google.com/p/websmssend/issues/detail?id=52))

## 0.70.0 ##

  * GMX: Text als Absenderkennung ist möglich. ([Issue 7](https://code.google.com/p/websmssend/issues/detail?id=7))
  * Allgemein: Telefonbuch-Funktion wurde hinzugefügt. Nun können auch Handys, die dies bisher noch nicht unterstützt haben, auf das Telefonbuch zugreifen ([Issue 38](https://code.google.com/p/websmssend/issues/detail?id=38))
  * Diverse kleinere Bugs und Anzeigefehler behoben

## 0.65.5 ##

  * Allgemein: Wenn die Frei-SMS verbraucht sind, zeigt die Benutzerkontenauswahl beim Accountwechsel "(Keine Daten)" anstatt der Benutzeraccounts. Dies funktioniert nun ([Issue32](https://code.google.com/p/websmssend/issues/detail?id=32))
  * Blackberry: Fehler beim Start "exception java.lang.nullpointerexception" behoben ([Issue31](https://code.google.com/p/websmssend/issues/detail?id=31))
  * Blackberry: Die Textfelder für die Telefonnummer und den SMS-Text werden nun linksbündig angezeigt
  * GMX: Abbruch des Versands unter Windows Mobile 6.5 mit AplixJBlend Java-VM behoben
  * GMX: Kodierung der zu übertragenden Zeichen eingeführt, so dass Sonderzeichen in SMS korrekt versendet werden (vgl. [Issue 14](https://code.google.com/p/websmssend/issues/detail?id=14)) und Fehler auf Plattformen, die UTF-8-Zeichenkodierung verwenden (z. B. LG GS 290 Cookie Fresh) behoben sind.
  * GMX: Berechnung der benötigten SMS bezieht Zeichen, die von GMX als zwei Zeichen gewertet werden, mit ein.

## 0.65.0 ##

  * Allgemein: Komplett neue Benutzerverwaltung. Es können nun beliebig viele Benuteraccounts erstellt werden und den Accounts einen Namen gegeben werden ([Issue 18](https://code.google.com/p/websmssend/issues/detail?id=18))
  * Allgemein: Beenden-Button im Bildschirm nach der erfolgreichen Übermittlung einer SMS eingefügt
  * Allgemein: [Issue 14](https://code.google.com/p/websmssend/issues/detail?id=14) (Sonderzeichen in temporär gespeicherten SMS wurden nicht richtig wiederhergestellt) ist nun gelöst
  * O2: Patch von Testverion 0.64.2 wurde eingefügt

**In die neue Benutzerverwaltung werden bisherige Benutzerkonten nicht importiert!**

## 0.64.2 ##
Testversion (kein offizielles Release)
  * O2: Accounts die keine Frei-SMS haben können jetzt wieder SMS versenden

## 0.64.1 ##

  * GMX: Es wird jetzt korrekt gewarnt, wenn nicht genügend Frei-SMS zum Versenden einer SMS vorhanden sind

## 0.64.0 ##

  * Allgemein: Falls alle Frei-SMS verbraucht sind wird der Benutzer gefragt, ob er trotzdem senden möchte oder ob er ein anderes Benutzerkonto benützen möchte
  * Allgemein: Auto-Speichern der SMS kann nun unter Optimierung deaktiviert werden (manche Geräte werden dadurch sehr langsam)
  * Allgemein: Einführung einer Update-Funktion. Nach dem Senden der ersten SMS nach dem Programmstart wird standardmäßig überprüft, ob es eine neue webSMSsend-Version zum Download gibt. Falls ja, erhält der Benutzer einen entsprechenden Hinweis angezeigt und kann die neue Version auch gleich installieren. Diese Funktion kann unter Optimierung deaktiviert werden. Es ist außerdem möglich, manuell über einen Eintrag unter Einstellungen nach einer neuen Version zu suchen.
  * GMX: Der GMX Connector benutzt seit diesem Release einen anderen Kommunikationsserver als den standardmäßig im GMX-SMS-Manager eingestellten (app3 statt app5). Tests haben gezeigt, dass die Verzögerung zwischen Absenden der SMS und Zustellung beim Empfänger im Mittel erheblich kleiner ist.

## 0.63.0 ##

  * GMX: Neues Kommunikations-Interface, basierend auf [LessIsMore](https://evolvis.org/scm/viewvc.php/lessismore/LessIsMore-MIDlet-Prototype/trunk/src/org/evolvis/lessismore/) von fkoester
  * GMX: Durch das neue Kommunikations-Interface wird der [SMS-Manager-Freischaltcode](GMXSMSManagerFreischaltcode.md) zum Login benötigt, **nicht mehr das Passwort des GMX-Postfachs**
  * GMX: GMX-Kunden müssen nicht mehr zwingend die Startansicht des Webmails auf "Standard" konfigurieren. Die Funktionalität von webSMSsend ist mit dieser Version davon unabhängig.
  * GMX: Fixed Anzeige der verbleibenden Frei-SMS
  * GMX: SMS ins Ausland möglich
  * GMX: Fixed SMS senden per GPRS, auch bei aktivierter Komprimierung, möglich
  * GMX: Übertragenes Datenvolumen auf ca. 3,5 KB (bei SMS mit max. 160 Zeichen) reduziert
  * GMX: Senden von MultiSMS bis zu einer Länge von 760 Zeichen (entspricht 5 SMS) möglich (vgl. http://www.gmx.net/gmx-sms)

  * Allgemein: Abspeichern der temporären SMS nach der Eingabe jedes einzelnen Buchstabens
  * Allgemein: Funktion "Letzte SMS" hinzugefügt. Damit kann die letzte SMS mit einem Klick zum Versenden an weitere Personen wiederhergestellt werden
  * Allgemein: Neues Applikationslogo


## 0.62.6 ##

  * GMX: Satzzeichen sollten nun wieder richtig kodiert werden, so dass sie auch in der SMS angezeigt werden

## 0.62.5 ##

  * GMX: Umlaute sollten nun richtig kodiert werden, so dass sie auch in der SMS angezeigt werden

## 0.62.4 ##

  * O2: Bei der Auswahl Text als Absender wird nun die Eingabe auf Richtigkeit geprüft (5-10 Zeichen und nur a-z,A-Z)
  * GMX: beim Versenden von langen SMS wird nun die korrekte Anzahl von den übrigen SMS abgezogen

## 0.62.3 ##

  * Fehler: "Systemspeicher voll" behoben

## 0.62.2 ##

  * Testversion

## 0.62.1 ##

  * GMX: viele Debug Meldungen hinzugefügt. Vielleicht hilfts.
  * Debug E-Mail: Die CC-Adresse wird nun richtig gesetzt

## 0.62.0 ##

  * O2 funktioniert wieder.
  * Der O2-Parser versucht sich nun bei kleineren Änderungen selbst zu aktualsieren
  * **Debug-Meldungen können nun per E-Mail versendet werden**