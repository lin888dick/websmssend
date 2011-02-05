/*
 *
 *
Copyright 2009 Max Hänze --- maximum.blogsite.org
Copyright 2010 Christian Morlok --- cmorlok.de
modifziert 2010 von RedRocket ---
This file is part of WebSMSsend.

WebSMSsend is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

WebSMSsend is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILIT ublic License for more details.

You should have received a copy of the GNU General Public License
along with WebSMSsend.  If not, see <http://www.gnu.org/licenses/>.

 *
 *
 */
package webSMSsend;

import java.io.IOException;
import java.io.PrintStream;
import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;
import javax.microedition.pki.CertificateException;
import org.netbeans.microedition.lcdui.WaitScreen;
import org.netbeans.microedition.util.SimpleCancellableTask;

//</editor-fold>
//<editor-fold defaultstate="collapsed" desc=" Generated Getter: exitCommand ">
public class webSMSsend extends MIDlet implements CommandListener {

    //FOR DEBUG ONLY!! SMS will not be sent if true
    public static boolean simulation = false;
    public static final int SENDERMODE_STANDARD = 0;
    public static final int SENDERMODE_TEXT = 1;
    String recvNB;
    String text;
    String username;
    String password;
    int provider; //0 = O2, 1 = GMX
    boolean contentLoad;
    boolean debug;
    DebugOutputStream debugOutputStream;
    PrintStream debugPrintStream;
    int remSMS;
    private long startTime = 0;
    int ActiveAccount; //0=Account1 1=Account2 etc.
    String SenderName; //Name bei Text als Absender
    int SenderMode; //0=Phonenumber, 1=Text( SenderName )
    private boolean midletPaused = false;
    private webSMSsend GUI;
    //<editor-fold defaultstate="collapsed" desc=" Generated Fields ">//GEN-BEGIN:|fields|0|
    private java.util.Hashtable __previousDisplayables = new java.util.Hashtable();
    private Command exitCommand;
    private Command goToSettings;
    private Command writeSMS;
    private Command back;
    private Command okCommand;
    private Command exitCommand2;
    private Command exitCommand1;
    private Command okCommand3;
    private Command exitCommand3;
    private Command okCommand2;
    private Command nextSettings;
    private Command eingabeLeeren;
    private Command okCommand5;
    private Command loginScreenSend;
    private Command okCommand6;
    private Command startEmailClient;
    private Command Clear;
    private Command SendEmail;
    private Command okCommand1;
    private Form MainMenu;
    private TextField textField;
    private StringItem stringItem1;
    private TextField textField3;
    private WaitScreen waitScreen;
    private Form loginSettings;
    private TextField textField2;
    private TextField textField1;
    private ChoiceGroup choiceGroup1;
    private Alert smsSend;
    private Alert notSend;
    private Alert About;
    private List list;
    private Form optimSettings;
    private ChoiceGroup choiceGroup;
    private Form providerSettings;
    private ChoiceGroup choiceGroup3;
    private Form smsSettings;
    private StringItem stringItem2;
    private ChoiceGroup choiceGroup4;
    private TextField txtSenderName;
    private StringItem stringItem;
    private Form setup;
    private ChoiceGroup choiceGroup2;
    private TextField textField4;
    private TextField textField5;
    private Form loginScreen;
    private TextField textField7;
    private TextField textField6;
    private List ChooseAccount;
    private Alert BenutzerwahlBestaettigung;
    private TextBox Debug;
    private WaitScreen waitScreen1;
    private Alert EmailSent;
    private Alert EmailNotSent;
    private Form SendEmailForm;
    private TextField txtCCemail;
    private StringItem stringItem3;
    private TextField txtFehlerbeschreibung;
    private Alert WrongCharacter;
    private Alert NoEmail;
    private SimpleCancellableTask task;
    private Font font;
    private Image image1;
    private SimpleCancellableTask task1;
    //</editor-fold>//GEN-END:|fields|0|

        /**
         * The HelloMIDlet constructor.
         */
        public webSMSsend() throws IOException {
            GUI = this;
//#if DefaultConfiguration
    simulation = true;
//#else
//#     simulation = false;
//#endif
        }

        public int sendGMXsms() {
            return 0;
        }

    private boolean CheckCharacters(String text) {
        boolean correctcharacters=true;
        char[] checkname = text.toCharArray();
        for (int i = 0; i < checkname.length; i++) {
            if ((checkname[i] >= 'a' && checkname[i] <= 'z') || (checkname[i] >= 'A' && checkname[i] <= 'Z')) {
            } else {
                correctcharacters=false;
                break;
            }
        }
        return correctcharacters;
    }

        private String getVersion() {
            return "0.62.6";
        }

        private String getPasswordField() {
            if (!ioSettings.getPassword().equals("")) {
                return "****";
            } else {
                return "";
            }
        }

        private String getRemSMSText() {
            if (remSMS != -1 & remSMS != -2) {
                return "verbleibende Frei-SMS: " + remSMS + "\nBenutzerkonto " + (ActiveAccount + 1);
            } else {
                return "";
            }
        }

        private int countSMS(String smsText) {  //ceiled division
            return (smsText.length() >= 0) ? ((smsText.length() + 160 - 1) / 160) : (smsText.length() / 160);
        }

        public int sendSMS(String smsRecv, String smsText) throws Exception {
            if (provider == 0) {
                return sendSMSO2(smsRecv, smsText);
            } else {
                return sendSMSGMX(smsRecv, smsText);
            }
        }

        public int sendSMSGMX(String smsRecv, String smsText) throws Exception {
            debug("SendSMSGMX()");
            if (smsRecv.equals("")) {
                waitScreen.setText("kein Empfänger angegeben!");
                Thread.sleep(1500);
                throw new Exception("kein Empfänger!");
            }
            try {
                waitScreen.setText("Einstellungen werden geladen...");
                Thread.sleep(500);

                NetworkHandler connection = new NetworkHandler(username, password, this);
                smsRecv = connection.checkRecv(smsRecv);
                if (!smsRecv.startsWith("+49")) {
                    throw new Exception("Senden von Auslands-SMS nicht möglich!");
                }
                smsRecv = "0".concat(smsRecv.substring(3));
                String url = "http://www.gmx.net/";
                // geht wohl auch ohne...
                //connection.httpHandler("GET", url, "www.gmx.net", "", true);


                waitScreen.setText("Login wird geladen...");
                debug("Login wird geladen...");
                String localCookie = connection.getCookie();
                String postReq = "AREA=1&EXT=&EXT2=&uinguserid=&dlevel=c&id="
                        + URLEncoder.encode(username) + "&p=" + URLEncoder.encode(password);
                for (int i = 0; i < 2; i++) {
                    try {
                        connection.httpHandler("POST", "http://service.gmx.net/de/cgi/login", "service.gmx.net", postReq, true);
                        continue;

                    } catch (IOException ex) {
                        if (i == 1) {
                            throw ex;
                        }
                        waitScreen.setText("Netzwerkfehler, starte erneut...");
                        debug("Netzwerkfehler: " + ex.toString() + ex.getMessage());
                        Thread.sleep(3000);
                    } catch (Exception ex) {
                        waitScreen.setText(ex.getMessage());
                        debug("Netzwerkfehler: " + ex.toString() + ex.getMessage());
                        Thread.sleep(3000);
                        throw ex;
                    }
                }
                //System.out.println(connection.getContent());

                if (localCookie.equals(connection.getCookie())) {
                    Exception ex = new Exception("Zugangsdaten falsch!");
                    debug("Zugangsdaten falsch!");
                    throw ex;
                }

                waitScreen.setText("Login erfolgreich...");
                debug("Login erfolgreich");
                url = connection.getRegexStringMatch("<a href=\"(http://service.gmx.net/de/cgi/g.fcgi/sms\\?cc=subnavi_sms_mms&amp;sid=.*)\">SMS und MMS</a></li>", "<li>", 0, 1);
                debug("Lade SMS-Manager-URL: " + url);
                connection.httpHandler("GET", url, "service.gmx.net", "", true);
                //http://service.gmx.net/de/cgi/g.fcgi/sms?cc=subnavi_smsmms&sid=babhdee.1254499929.29227.jr09oorphd.73.ign
                waitScreen.setText("Lade SMS-Manager...");
                String sid = connection.getRegexMatch(url, "sid=(.*)", 1);//connection.getRegexStringMatch("<a href=\"http://service.gmx.net/de/cgi/g.fcgi/sms\\?sid=(.*)\">SMS und MMS</a></li>","<li>",0,1);
                debug("Cookie: " + connection.getCookie());

                url = "http://service.gmx.net/de/cgi/g.fcgi/sms/manager/popup?sid=" + sid;
                debug("httpHandler('GET'" + url);
                connection.httpHandler("GET", url, "service.gmx.net", "", true);

                waitScreen.setText("Senden wird vorbereitet...");
                url = connection.getRegexStringMatch("url = \"(http://www.sms-manager.info/wsm/login_action.jsp\\?resCustId=.*&password=.*&destination=&customer=GMX)\"", "\n", 0, 1);
                String customID = connection.getRegexMatch(url, "resCustId=(.*)&password", 1);
                debug("Senden wird vorbereitet: " + url);
                connection.httpHandler("GET", url, "www.sms-manager.info", "", true);

                try {
                    if (remSMS != -1) {
                        String sendSMSstring = connection.getRegexStringMatch("noch (.*) von (.+)", "\n", 0, 1);
                        remSMS = Integer.parseInt(sendSMSstring);
                        debug("" + remSMS);
                    }
                } catch (Exception ex) {
                    debug("Failed to receive remaining SMS: " + ex.toString() + ex.getMessage());
                }

                waitScreen.setText("SMS wird gesendet...");
                if (connection.getRegexStringMatch("<input type=\"radio\".* (.*)>", "\n", 0, 1).equals("checked")) {
                    postReq = "senderType=number&senderId=&receiver=" + URLEncoder.encode(smsRecv) + "&message="
                            + URLEncoderISO8859.encode(smsText) + "&sendLater=0";
                    debug("Absender Nummer erkannt, postReq: " + postReq);
                } else {
                    postReq = "senderType=text&senderId=SMS&receiver=" + URLEncoder.encode(smsRecv) + "&message="
                            + URLEncoderISO8859.encode(smsText) + "&sendLater=0";
                    debug("Absender nicht hinterlegt, postReq: " + postReq);
                }
                url = "http://www.sms-manager.info/wsm/send_sms_action.jsp?wsmCustomerId=" + customID;
                connection.httpHandler("POST", url, "www.sms-manager.info", postReq, true);
                debug("SMS gesendet");
                int SMSneeded = countSMS(smsText);
                if (remSMS > 0) {
                    remSMS = remSMS - SMSneeded; //Counting amount of used SMS
                }


                /*waitScreen.setText("Abmelden...");
                try{
                connection.httpHandler("GET", logoutUrl, "service.gmx.net", "", true);
                }catch (Exception e){
                waitScreen.setText("Abmeldung fehlgeschlagen!");
                Thread.sleep(500);

                }*/
                /*
                 *
                 * SMS-manager is used!
                 *
                 *
                 *
                 *
                String postCodeName;
                //System.out.println(connection.getContent());
                postCodeName="_"+connection.getRegexStringMatch("input type=\"hidden\" name=\"_(.*)\" value=\"_(.*)\" ", "/><", 0, 1);
                String postCodeValue;
                postCodeValue="_"+connection.getRegexStringMatch("input type=\"hidden\" name=\"_(.*)\" value=\"_(.*)\" ", "/><", 0, 2);
                String sid=connection.getRegexMatch(url, ".*sid=(.*)", 1);
                postReq="sid="+sid+"&fromaddrbk=0&"+postCodeName+"="+postCodeValue+"&number="
                +smsRecv+"&from="+"none"/*URLEncoder.encode(username)
                 *//*+"&message="+URLEncoder.encode(smsText)
                +"&buttonSubmit.x=108&buttonSubmit.y=14";
                //System.out.println(postReq);
                waitScreen.setText("SMS wird gesendet...");
                url="http://service.gmx.net/de/cgi/g.fcgi/sms/send";
                connection.httpHandler("POST", url, "service.gmx.net", postReq, true);

                if (remSMS>0){
                int sms=Integer.parseInt(connection.getRegexStringMatch(" *\\(Zeichenanzahl: (.*)\\)", '\n'+"", 0, 1));
                remSMS=remSMS-(sms/160+1);
                }
                //System.out.println(connection.getContent());
                postReq="sid="+sid+"&buttonSubmit=Ja%2C+SMS+senden";
                url="http://service.gmx.net/de/cgi/g.fcgi/sms/confirm";
                //connection.httpHandler("POST", url, "service.gmx.net", postReq, true);


                url=connection.getRegexStringMatch("a href=\"(http://service.gmx.net/de/cgi/nph-logout\\?CUSTOMERNO=.*)\"", "><", 0, 1);
                //System.out.println(url);
                connection.httpHandler("GET", url, "service.gmx.net", "", true);
                //System.out.println(connection.getContent());
                 */

            } catch (IOException ex) {
                waitScreen.setText(ex.getMessage());
                debug(ex.toString() + " " +ex.getMessage());
                Thread.sleep(7000);
                ex.printStackTrace();
                throw ex;
            } catch (Exception ex) {
                waitScreen.setText(ex.getMessage());
                debug(ex.toString() + " " +ex.getMessage());
                Thread.sleep(7000);
                ex.printStackTrace();
                throw ex;
            } catch (Throwable e) {
                waitScreen.setText(e.getMessage());
                debug(e.toString() + " " +e.getMessage());
                Thread.sleep(7000);
                e.printStackTrace();
                throw new Exception("Fehler!");
            }
            return 0;
        }

        public int sendSMSO2(String smsRecv, String smsText) throws Exception {
            try {
                debug("starte sendSMS02()");
                long totaltime = System.currentTimeMillis();
                if (smsRecv.equals("")) {
                    waitScreen.setText("kein Empfänger angegeben!");
                    Thread.sleep(1000);
                    throw new Exception("kein Empfänger!");
                }
                if (smsText.equals("")) {
                    waitScreen.setText("kein SMS-Text angegeben!");
                    Thread.sleep(1000);
                    throw new Exception("leere SMS!");
                }
                waitScreen.setText("Einstellungen werden geladen...");
                Thread.sleep(500);
                String url;

                NetworkHandler connection = new NetworkHandler(username, password, this);
                waitScreen.setText("Login wird geladen...");

                smsRecv = connection.checkRecv(smsRecv);
                debug("Login wird geladen: smsRecv: " + smsRecv);
                url = "https://login.o2online.de/loginRegistration/loginAction"
                        + ".do?_flowId=" + "login&o2_type=asp&o2_label=login/co"
                        + "mcenter-login&scheme=http&" + "port=80&server=email."
                        + "o2online.de&url=%2Fssomanager.osp%3FAPIID" + "%3DAUT"
                        + "H-WEBSSO%26TargetApp%3D%2Fsms_new.osp%3F%26o2_type%3"
                        + "Durl" + "%26o2_label%3Dweb2sms-o2online";
                for (int i = 0; i < 2; i++) {
                    try {
                        connection.httpHandler("GET", url, "login.o2online.de", "", true);
                        //      continue;
                        break;
                    } catch (CertificateException ex) {
                        debug(ex.toString());
                        if (i == 1) {
                            throw ex;
                        }
                        debug("SSL-Fehler, starte erneut...");
                        waitScreen.setText("SSL-Fehler, starte erneut...");
                        Thread.sleep(3000);
                    } catch (IOException ex) {
                        debug("IOException: " + ex.toString());
                        if (i == 1) {
                            throw ex;
                        }
                        debug("Netzwerkfehler, starte erneut...");
                        waitScreen.setText("Netzwerkfehler, starte erneut...");
                        Thread.sleep(3000);
                    } catch (Exception ex) {
                        debug("Keine Verbindung möglich :" + ex.toString() + " " + ex.getMessage());
                        waitScreen.setText("Keine Verbindung möglich :" + ex.toString() + "\n" + ex.getMessage());
                        Thread.sleep(3000);
                        throw ex;
                    }
                }

                String flowExecutionKey;

                flowExecutionKey = connection.getFlowExecutionKey();

                waitScreen.setText("Zugangsdaten werden gesendet...");
                debug("Zugangsdaten werden gesendet: Flow ExecutionKey: "+flowExecutionKey);
                url = "https://login.o2online.de/loginRegistration/loginAction.do";
                connection.httpHandler("POST", url, "login.o2online.de", "_flowExecutionKey="
                        + URLEncoder.encode(flowExecutionKey)
                        + "&loginName=" + URLEncoder.encode(connection.getUsername().trim())
                        + "&password=" + URLEncoder.encode(connection.getPassword().trim())
                        + "&_eventId=login", false);//False

                waitScreen.setText("Zugangsdaten werden geprüft...");
                url = "https://email.o2online.de/ssomanager.osp?APIID=AUTH-WEBSSO&"
                        + "TargetApp=/sms_new.osp%3f&o2_type=url&o2_label=web2sms-o2online";
                String localCookie = connection.getCookie();
                connection.httpHandler("GET", url, "email.o2online.de", "", false);//false

                if (localCookie.equals(connection.getCookie())) {
                    Exception ex = new Exception("Zugangsdaten falsch!");
                    throw ex;
                }
                waitScreen.setText("Senden wird vorbereitet...");
                url = "https://email.o2online.de/smscenter_new.osp?Autocompletion=1&MsgContentID=-1";
                long starttime = System.currentTimeMillis();
                connection.httpHandler("GET", url, "email.o2online.de", "", true);
                long httphandlertime = System.currentTimeMillis() - starttime;
                debug("Fertig mit connection.httpHandler, Dauer: " + httphandlertime + " ms");
                //System.out.println(connection.getContent()+"\n\n\n");
                String postRequest = "";
                waitScreen.setText("SMS wird gesendet...");
                url = "https://email.o2online.de/smscenter_send.osp";

                //Build SMS send request and get remaining SMS.

                String[] returnValue;
                starttime = System.currentTimeMillis();
                returnValue = connection.getSendPostRequest((remSMS != -1), SenderMode); //Sendermode: 0=phone number 1=text
                debug("Fertig mit getSendPostRequest, Dauer: " + (System.currentTimeMillis() - starttime) + " ms" + "HttpHandler: " + httphandlertime + " ms");
                postRequest = returnValue[0];

                try {
                    if (remSMS != -1) {
                        remSMS = Integer.parseInt(returnValue[1]);
                    }
                } catch (Exception ex) {
                    debug("Failed to receive remaining SMS: " + ex.toString() + ex.getMessage());
                }


                if (SenderMode == SENDERMODE_TEXT) { //Text as Sender
                    postRequest = postRequest + "SMSTo=" + URLEncoder.encode(smsRecv) + "&SMSText="
                            + URLEncoder.encode(smsText) + "&SMSFrom="
                            + URLEncoder.encode(SenderName) + "&Frequency=5";
                } else {
                    postRequest = postRequest + "SMSTo=" + URLEncoder.encode(smsRecv) + "&SMSText="
                            + URLEncoder.encode(smsText) + "&SMSFrom=&Frequency=5";
                }

                if (!simulation) {
                    connection.httpHandler("POST", url, "email.o2online.de", postRequest, false);//false
                }
                //if (remSMS>0) remSMS--;
                int SMSneeded = countSMS(smsText);
                if (remSMS > 0) {
                    remSMS = remSMS - SMSneeded; //Counting amount of used SMS
                }
                debug("Die SMS ist Zeichen lang: " + smsText.length());
                debug("Anzahl SMS: " + SMSneeded);
                debug("Fertig mit sendSMS02, Dauer: " + (System.currentTimeMillis() - totaltime) + " ms");
                waitScreen.setText("SMS wurde versandt!");
                return 0;

            } catch (OutOfMemoryError ex) {
                waitScreen.setText("Systemspeicher voll!");
                debug("Systemspeicher voll!" + ex.getMessage());
                Thread.sleep(7000);
                throw ex;
            } catch (Exception ex) {
                waitScreen.setText(ex.toString() + ": " + ex.getMessage());
                debug(ex.toString() + ": " + ex.getMessage());
                ex.printStackTrace();
                Thread.sleep(7000);
                throw ex;
            } catch (Throwable e) {
                waitScreen.setText("Unklarer Fehler: " + e.toString());
                debug("Unklarer Fehler: " + e.toString());
                Thread.sleep(10000);
                throw new Exception("Fehler!");
            }
        }

        private void SyncSettings() {
            username = ioSettings.getUsername();
            password = ioSettings.getPassword();
            provider = ioSettings.getSetup();
            remSMS = ioSettings.getRemSMS();
            contentLoad = ioSettings.getContentLoad().equals("true");
            ActiveAccount = Integer.parseInt(ioSettings.getActiveAccount());
            SenderMode = ioSettings.getSenderMode();
            SenderName = ioSettings.getSenderName();
            debug = ioSettings.getDebug().equals("true");
            if (simulation) {
                debug = true;
            }
            System.out.println("AcitveAccount: " + ActiveAccount);
        }

        private void SaveTempSMS() {
            ioSettings.saveTempSMS(textField.getString(), textField3.getString());
        }

        private void RetrieveTempSMS() {
            textField.setString("" + ioSettings.getTempSMSto());
            textField3.setString("" + ioSettings.getTempSMStext());
        }

        private void ClearSMSInput() {
            textField3.setString("");
            textField3.setLabel("Eingabe");
            textField.setString("");
        }
        //<editor-fold defaultstate="collapsed" desc=" Generated Methods ">//GEN-BEGIN:|methods|0|
        /**
         * Switches a display to previous displayable of the current displayable.
         * The <code>display</code> instance is obtain from the <code>getDisplay</code> method.
         */
        private void switchToPreviousDisplayable() {
            Displayable __currentDisplayable = getDisplay().getCurrent();
            if (__currentDisplayable != null) {
                Displayable __nextDisplayable = (Displayable) __previousDisplayables.get(__currentDisplayable);
                if (__nextDisplayable != null) {
                    switchDisplayable(null, __nextDisplayable);
                }
            }
        }
        //</editor-fold>//GEN-END:|methods|0|

        //<editor-fold defaultstate="collapsed" desc=" Generated Method: initialize ">//GEN-BEGIN:|0-initialize|0|0-preInitialize
        /**
         * Initilizes the application.
         * It is called only once when the MIDlet is started. The method is called before the <code>startMIDlet</code> method.
         */
        private void initialize() {//GEN-END:|0-initialize|0|0-preInitialize
        startTime = System.currentTimeMillis();


//GEN-LINE:|0-initialize|1|0-postInitialize
        // write post-initialize user code here
        }//GEN-BEGIN:|0-initialize|2|
        //</editor-fold>//GEN-END:|0-initialize|2|

        //<editor-fold defaultstate="collapsed" desc=" Generated Method: startMIDlet ">//GEN-BEGIN:|3-startMIDlet|0|3-preAction
        /**
         * Performs an action assigned to the Mobile Device - MIDlet Started point.
         */
        public void startMIDlet() {//GEN-END:|3-startMIDlet|0|3-preAction
        if (ioSettings.getUsername().equals("")) {
            switchDisplayable(null, getSetup());

        } else {


            switchDisplayable(null, getMainMenu());
            SyncSettings();
            stringItem1.setText(getRemSMSText());
        }
//GEN-LINE:|3-startMIDlet|1|3-postAction

        }//GEN-BEGIN:|3-startMIDlet|2|
        //</editor-fold>//GEN-END:|3-startMIDlet|2|

        //<editor-fold defaultstate="collapsed" desc=" Generated Method: resumeMIDlet ">//GEN-BEGIN:|4-resumeMIDlet|0|4-preAction
        /**
         * Performs an action assigned to the Mobile Device - MIDlet Resumed point.
         */
        public void resumeMIDlet() {//GEN-END:|4-resumeMIDlet|0|4-preAction
        // write pre-action user code here
//GEN-LINE:|4-resumeMIDlet|1|4-postAction
        // write post-action user code here
        }//GEN-BEGIN:|4-resumeMIDlet|2|
        //</editor-fold>//GEN-END:|4-resumeMIDlet|2|

        //<editor-fold defaultstate="collapsed" desc=" Generated Method: switchDisplayable ">//GEN-BEGIN:|5-switchDisplayable|0|5-preSwitch
        /**
         * Switches a current displayable in a display. The <code>display</code> instance is taken from <code>getDisplay</code> method. This method is used by all actions in the design for switching displayable.
         * @param alert the Alert which is temporarily set to the display; if <code>null</code>, then <code>nextDisplayable</code> is set immediately
         * @param nextDisplayable the Displayable to be set
         */
        public void switchDisplayable(Alert alert, Displayable nextDisplayable) {//GEN-END:|5-switchDisplayable|0|5-preSwitch
        // write pre-switch user code here
            Display display = getDisplay();//GEN-BEGIN:|5-switchDisplayable|1|5-postSwitch
            Displayable __currentDisplayable = display.getCurrent();
            if (__currentDisplayable != null  &&  nextDisplayable != null) {
                __previousDisplayables.put(nextDisplayable, __currentDisplayable);
            }
            if (alert == null) {
                display.setCurrent(nextDisplayable);
            } else {
                display.setCurrent(alert, nextDisplayable);
            }//GEN-END:|5-switchDisplayable|1|5-postSwitch
        // write post-switch user code here
        }//GEN-BEGIN:|5-switchDisplayable|2|
        //</editor-fold>//GEN-END:|5-switchDisplayable|2|

        //<editor-fold defaultstate="collapsed" desc=" Generated Method: commandAction for Displayables ">//GEN-BEGIN:|7-commandAction|0|7-preCommandAction
        /**
         * Called by a system to indicated that a command has been invoked on a particular displayable.
         * @param command the Command that was invoked
         * @param displayable the Displayable where the command was invoked
         */
        public void commandAction(Command command, Displayable displayable) {//GEN-END:|7-commandAction|0|7-preCommandAction
        // write pre-action user code here
            if (displayable == ChooseAccount) {//GEN-BEGIN:|7-commandAction|1|249-preAction
                if (command == List.SELECT_COMMAND) {//GEN-END:|7-commandAction|1|249-preAction
                // write pre-action user code here
                    ChooseAccountAction();//GEN-LINE:|7-commandAction|2|249-postAction
                // write post-action user code here
                switchDisplayable(getBenutzerwahlBestaettigung(), getList());
                BenutzerwahlBestaettigung = null;
                } else if (command == back) {//GEN-LINE:|7-commandAction|3|255-preAction
                // write pre-action user code here
                    switchToPreviousDisplayable();//GEN-LINE:|7-commandAction|4|255-postAction
                // write post-action user code here
                } else if (command == okCommand5) {//GEN-LINE:|7-commandAction|5|254-preAction
                ChooseAccountAction();
                // write pre-action user code here
                switchDisplayable(getBenutzerwahlBestaettigung(), getList());//GEN-LINE:|7-commandAction|6|254-postAction
                // write post-action user code here
                BenutzerwahlBestaettigung = null;
                }//GEN-BEGIN:|7-commandAction|7|294-preAction
            } else if (displayable == Debug) {
                if (command == Clear) {//GEN-END:|7-commandAction|7|294-preAction
                // write pre-action user code here
                    getDebug().setString(getDebug().size() +" Zeichen gelöscht");//GEN-LINE:|7-commandAction|8|294-postAction
                // write post-action user code here
                } else if (command == SendEmail) {//GEN-LINE:|7-commandAction|9|298-preAction
                // write pre-action user code here
                    switchDisplayable(null, getSendEmailForm());//GEN-LINE:|7-commandAction|10|298-postAction
                // write post-action user code here
                } else if (command == back) {//GEN-LINE:|7-commandAction|11|291-preAction
                // write pre-action user code here
                    switchDisplayable(null, getList());//GEN-LINE:|7-commandAction|12|291-postAction
                // write post-action user code here
                }//GEN-BEGIN:|7-commandAction|13|245-preAction
            } else if (displayable == MainMenu) {
                if (command == eingabeLeeren) {//GEN-END:|7-commandAction|13|245-preAction
                // write pre-action user code here
                    ClearSMSInput();//GEN-LINE:|7-commandAction|14|245-postAction
                // write post-action user code here
                } else if (command == exitCommand) {//GEN-LINE:|7-commandAction|15|19-preAction
                // write pre-action user code here
                    exitMIDlet();//GEN-LINE:|7-commandAction|16|19-postAction
                // write post-action user code here
                } else if (command == goToSettings) {//GEN-LINE:|7-commandAction|17|82-preAction
                // write pre-action user code here
                    switchDisplayable(null, getList());//GEN-LINE:|7-commandAction|18|82-postAction

                } else if (command == writeSMS) {//GEN-LINE:|7-commandAction|19|29-preAction
                recvNB = textField.getString();
                text = textField3.getString();
                debug("Senden pressed");
                if (!password.equals("")) {
                    switchDisplayable(null, getWaitScreen());//GEN-LINE:|7-commandAction|20|29-postAction
                } else {
                    switchDisplayable(null, getLoginScreen());
                    textField6.setString(username);
                }

                }//GEN-BEGIN:|7-commandAction|21|323-preAction
            } else if (displayable == NoEmail) {
                if (command == back) {//GEN-END:|7-commandAction|21|323-preAction
                // write pre-action user code here
                    switchDisplayable(null, getSendEmailForm());//GEN-LINE:|7-commandAction|22|323-postAction
                // write post-action user code here
                } else if (command == okCommand1) {//GEN-LINE:|7-commandAction|23|325-preAction
                // write pre-action user code here
                    switchDisplayable(null, getWaitScreen1());//GEN-LINE:|7-commandAction|24|325-postAction
                // write post-action user code here
                }//GEN-BEGIN:|7-commandAction|25|307-preAction
            } else if (displayable == SendEmailForm) {
                if (command == back) {//GEN-END:|7-commandAction|25|307-preAction
                // write pre-action user code here
                    switchDisplayable(null, getDebug());//GEN-LINE:|7-commandAction|26|307-postAction
                // write post-action user code here
                } else if (command == startEmailClient) {//GEN-LINE:|7-commandAction|27|306-preAction
                // write pre-action user code here
                    isEmailNotEntered();//GEN-LINE:|7-commandAction|28|306-postAction
                // write post-action user code here
                }//GEN-BEGIN:|7-commandAction|29|166-preAction
            } else if (displayable == list) {
                if (command == List.SELECT_COMMAND) {//GEN-END:|7-commandAction|29|166-preAction
                // write pre-action user code here
                    listAction();//GEN-LINE:|7-commandAction|30|166-postAction
                // write post-action user code here
                } else if (command == back) {//GEN-LINE:|7-commandAction|31|184-preAction
                // write pre-action user code here
                    switchDisplayable(null, getMainMenu());//GEN-LINE:|7-commandAction|32|184-postAction
                stringItem1.setText(getRemSMSText());
                } else if (command == nextSettings) {//GEN-LINE:|7-commandAction|33|173-preAction
                // write pre-action user code here
                    listAction();//GEN-LINE:|7-commandAction|34|173-postAction
                // write post-action user code here
                }//GEN-BEGIN:|7-commandAction|35|238-preAction
            } else if (displayable == loginScreen) {
                if (command == back) {//GEN-END:|7-commandAction|35|238-preAction


                    switchToPreviousDisplayable();//GEN-LINE:|7-commandAction|36|238-postAction
                // write post-action user code here
                } else if (command == loginScreenSend) {//GEN-LINE:|7-commandAction|37|241-preAction
                username = textField6.getString();
                password = textField7.getString();
                switchDisplayable(null, getWaitScreen());//GEN-LINE:|7-commandAction|38|241-postAction
                // write post-action user code here
                }//GEN-BEGIN:|7-commandAction|39|65-preAction
            } else if (displayable == loginSettings) {
                if (command == back) {//GEN-END:|7-commandAction|39|65-preAction
                // write pre-action user code here
                    switchToPreviousDisplayable();//GEN-LINE:|7-commandAction|40|65-postAction
                // write post-action user code here
                } else if (command == okCommand) {//GEN-LINE:|7-commandAction|41|85-preAction

                if (choiceGroup1.isSelected(0)) {
                    if (textField2.getString().equals("****")) {
                        password = ioSettings.getPassword();
                    } else {
                        password = textField2.getString();
                    }
                } else {
                    password = "";
                }
                username = textField1.getString();
                ioSettings.saveToRMS(username, password);
                switchToPreviousDisplayable();//GEN-LINE:|7-commandAction|42|85-postAction
                // write post-action user code here
                }//GEN-BEGIN:|7-commandAction|43|110-preAction
            } else if (displayable == notSend) {
                if (command == exitCommand3) {//GEN-END:|7-commandAction|43|110-preAction
                // write pre-action user code here
                    exitMIDlet();//GEN-LINE:|7-commandAction|44|110-postAction
                // write post-action user code here
                } else if (command == okCommand3) {//GEN-LINE:|7-commandAction|45|112-preAction
                // write pre-action user code here
                    switchDisplayable(null, getMainMenu());//GEN-LINE:|7-commandAction|46|112-postAction
                // write post-action user code here
                }//GEN-BEGIN:|7-commandAction|47|187-preAction
            } else if (displayable == optimSettings) {
                if (command == back) {//GEN-END:|7-commandAction|47|187-preAction
                // write pre-action user code here
                    switchToPreviousDisplayable();//GEN-LINE:|7-commandAction|48|187-postAction
                // write post-action user code here
                } else if (command == okCommand) {//GEN-LINE:|7-commandAction|49|208-preAction
                if (choiceGroup.isSelected(0)) {
                    ioSettings.saveOptim("true");
                    contentLoad = true;
                } else {
                    ioSettings.saveOptim("false");
                    contentLoad = false;
                }
                if (choiceGroup.isSelected(1)) {
                    if (remSMS < 0) {
                        ioSettings.saveRemSMS("-2");
                        remSMS = -2;
                    }
                } else {
                    ioSettings.saveRemSMS("-1");
                    remSMS = -1;
                }
                if (choiceGroup.isSelected(2)) {
                    ioSettings.saveDebug("true");
                    debug = true;
                } else {
                    ioSettings.saveDebug("false");
                    debug = false;
                }
                switchDisplayable(null, getList());//GEN-LINE:|7-commandAction|50|208-postAction
                // write post-action user code here
                }//GEN-BEGIN:|7-commandAction|51|189-preAction
            } else if (displayable == providerSettings) {
                if (command == back) {//GEN-END:|7-commandAction|51|189-preAction
                // write pre-action user code here
                    switchToPreviousDisplayable();//GEN-LINE:|7-commandAction|52|189-postAction
                // write post-action user code here
                } else if (command == okCommand) {//GEN-LINE:|7-commandAction|53|232-preAction
                if (choiceGroup3.getSelectedIndex() != -1) {
                    provider = choiceGroup3.getSelectedIndex();
                    ioSettings.saveSetup("" + provider);
                    if (remSMS != -1) {
                        remSMS = -2;
                        ioSettings.saveRemSMS("-2");
                    }
                }
                switchToPreviousDisplayable();//GEN-LINE:|7-commandAction|54|232-postAction




                }//GEN-BEGIN:|7-commandAction|55|227-preAction
            } else if (displayable == setup) {
                if (command == okCommand) {//GEN-END:|7-commandAction|55|227-preAction
                username = textField5.getString();
                password = textField4.getString();
                provider = choiceGroup2.getSelectedIndex();
                System.out.println("Provider: " + provider);
                remSMS = -2;
                contentLoad = false;
                debug = false;

                if (provider == -1) {
                    provider = 0;
                }
                ioSettings.saveOptim("false");
                ioSettings.saveDebug("false");
                ioSettings.saveRemSMS("" + remSMS);
                ioSettings.saveSetup("" + (provider));
                ioSettings.saveToRMS(username, password);
                SyncSettings();
                switchDisplayable(null, getMainMenu());//GEN-LINE:|7-commandAction|56|227-postAction
                // write post-action user code here
                }//GEN-BEGIN:|7-commandAction|57|104-preAction
            } else if (displayable == smsSend) {
                if (command == exitCommand2) {//GEN-END:|7-commandAction|57|104-preAction
                // write pre-action user code here
                    exitMIDlet();//GEN-LINE:|7-commandAction|58|104-postAction
                // write post-action user code here
                } else if (command == okCommand2) {//GEN-LINE:|7-commandAction|59|107-preAction
                // write pre-action user code here
                    switchDisplayable(null, getMainMenu());//GEN-LINE:|7-commandAction|60|107-postAction
                stringItem1.setText(getRemSMSText());
                }//GEN-BEGIN:|7-commandAction|61|183-preAction
            } else if (displayable == smsSettings) {
                if (command == back) {//GEN-END:|7-commandAction|61|183-preAction
                // write pre-action user code here
                    switchToPreviousDisplayable();//GEN-LINE:|7-commandAction|62|183-postAction
                // write post-action user code here
                } else if (command == okCommand6) {//GEN-LINE:|7-commandAction|63|270-preAction
                if (txtSenderName.getString().length() < 5 && choiceGroup4.getSelectedIndex() != 0) {
                    //Fehlermeldung "Name zu kurz" falls Text als Absender gewählt
                    stringItem2.setText("Der Absender muss mindestens 5 Buchstaben lang sein");
                    stringItem2.setLabel("Achtung!");
                } else {
                    // write pre-action user code here
                    CharactersCorrect();//GEN-LINE:|7-commandAction|64|270-postAction
                    // write post-action user code here
                }
                }//GEN-BEGIN:|7-commandAction|65|51-preAction
            } else if (displayable == waitScreen) {
                if (command == WaitScreen.FAILURE_COMMAND) {//GEN-END:|7-commandAction|65|51-preAction
                getNotSend().setString("SMS nicht gesendet!");
                switchDisplayable(getNotSend(), getMainMenu());//GEN-LINE:|7-commandAction|66|51-postAction

                } else if (command == WaitScreen.SUCCESS_COMMAND) {//GEN-LINE:|7-commandAction|67|50-preAction
                if (remSMS != -1) {
                    ioSettings.saveRemSMS("" + remSMS);
                }
                switchDisplayable(getSmsSend(), getMainMenu());//GEN-LINE:|7-commandAction|68|50-postAction
                smsSend.setString("SMS gesendet \n" + getRemSMSText());
                ClearSMSInput();
                } else if (command == exitCommand1) {//GEN-LINE:|7-commandAction|69|99-preAction
                // write pre-action user code here
                    exitMIDlet();//GEN-LINE:|7-commandAction|70|99-postAction
                // write post-action user code here
                }//GEN-BEGIN:|7-commandAction|71|312-preAction
            } else if (displayable == waitScreen1) {
                if (command == WaitScreen.FAILURE_COMMAND) {//GEN-END:|7-commandAction|71|312-preAction
                // write pre-action user code here
                    switchDisplayable(getEmailNotSent(), getSendEmailForm());//GEN-LINE:|7-commandAction|72|312-postAction
                // write post-action user code here
                } else if (command == WaitScreen.SUCCESS_COMMAND) {//GEN-LINE:|7-commandAction|73|311-preAction
                // write pre-action user code here
                    switchDisplayable(getEmailSent(), getDebug());//GEN-LINE:|7-commandAction|74|311-postAction
                // write post-action user code here
                }//GEN-BEGIN:|7-commandAction|75|7-postCommandAction
            }//GEN-END:|7-commandAction|75|7-postCommandAction
        // write post-action user code here
        }//GEN-BEGIN:|7-commandAction|76|
        //</editor-fold>//GEN-END:|7-commandAction|76|

        //<editor-fold defaultstate="collapsed" desc=" Generated Getter: exitCommand ">//GEN-BEGIN:|18-getter|0|18-preInit
        /**
         * Returns an initiliazed instance of exitCommand component.
         * @return the initialized component instance
         */
        public Command getExitCommand() {
            if (exitCommand == null) {//GEN-END:|18-getter|0|18-preInit
            // write pre-init user code here
                exitCommand = new Command("Exit", Command.EXIT, 0);//GEN-LINE:|18-getter|1|18-postInit
            // write post-init user code here
            }//GEN-BEGIN:|18-getter|2|
            return exitCommand;
        }
        //</editor-fold>//GEN-END:|18-getter|2|

        //<editor-fold defaultstate="collapsed" desc=" Generated Getter: MainMenu ">//GEN-BEGIN:|14-getter|0|14-preInit
        /**
         * Returns an initiliazed instance of MainMenu component.
         * @return the initialized component instance
         */
        public Form getMainMenu() {
            if (MainMenu == null) {//GEN-END:|14-getter|0|14-preInit

                MainMenu = new Form("webSMSsend", new Item[] { getTextField(), getTextField3(), getStringItem1() });//GEN-BEGIN:|14-getter|1|14-postInit
                MainMenu.addCommand(getExitCommand());
                MainMenu.addCommand(getWriteSMS());
                MainMenu.addCommand(getGoToSettings());
                MainMenu.addCommand(getEingabeLeeren());
                MainMenu.setCommandListener(this);//GEN-END:|14-getter|1|14-postInit
            // write post-init user code here
            ItemStateListener listener = new ItemStateListener() {

                public void itemStateChanged(Item item) {
                    if (item == textField3) {
                        textField3.setLabel("" + textField3.getString().length()
                                + " (" + countSMS(textField3.getString()) + " SMS)");
                    }
                }
            };
            MainMenu.setItemStateListener(listener);
            RetrieveTempSMS();
            }//GEN-BEGIN:|14-getter|2|
            return MainMenu;
        }
        //</editor-fold>//GEN-END:|14-getter|2|

        //<editor-fold defaultstate="collapsed" desc=" Generated Getter: goToSettings ">//GEN-BEGIN:|24-getter|0|24-preInit
        /**
         * Returns an initiliazed instance of goToSettings component.
         * @return the initialized component instance
         */
        public Command getGoToSettings() {
            if (goToSettings == null) {//GEN-END:|24-getter|0|24-preInit
            // write pre-init user code here
                goToSettings = new Command("Einstellungen", Command.CANCEL, 3);//GEN-LINE:|24-getter|1|24-postInit
            // write post-init user code here
            }//GEN-BEGIN:|24-getter|2|
            return goToSettings;
        }
        //</editor-fold>//GEN-END:|24-getter|2|

        //<editor-fold defaultstate="collapsed" desc=" Generated Getter: writeSMS ">//GEN-BEGIN:|28-getter|0|28-preInit
        /**
         * Returns an initiliazed instance of writeSMS component.
         * @return the initialized component instance
         */
        public Command getWriteSMS() {
            if (writeSMS == null) {//GEN-END:|28-getter|0|28-preInit
            // write pre-init user code here
                writeSMS = new Command("Senden", Command.OK, 1);//GEN-LINE:|28-getter|1|28-postInit

            }//GEN-BEGIN:|28-getter|2|
            return writeSMS;
        }
        //</editor-fold>//GEN-END:|28-getter|2|

        //<editor-fold defaultstate="collapsed" desc=" Generated Getter: back ">//GEN-BEGIN:|33-getter|0|33-preInit
        /**
         * Returns an initiliazed instance of back component.
         * @return the initialized component instance
         */
        public Command getBack() {
            if (back == null) {//GEN-END:|33-getter|0|33-preInit
            // write pre-init user code here
                back = new Command("Zur\u00FCck", Command.BACK, 0);//GEN-LINE:|33-getter|1|33-postInit
            // write post-init user code here
            }//GEN-BEGIN:|33-getter|2|
            return back;
        }
        //</editor-fold>//GEN-END:|33-getter|2|

        //<editor-fold defaultstate="collapsed" desc=" Generated Getter: waitScreen ">//GEN-BEGIN:|47-getter|0|47-preInit
        /**
         * Returns an initiliazed instance of waitScreen component.
         * @return the initialized component instance
         */
        public WaitScreen getWaitScreen() {
            if (waitScreen == null) {//GEN-END:|47-getter|0|47-preInit
            debug("waitscreen erstellen");
            // write pre-init user code here
            waitScreen = new WaitScreen(getDisplay());//GEN-BEGIN:|47-getter|1|47-postInit
            waitScreen.setTitle("Sende SMS...");
            waitScreen.addCommand(getExitCommand1());
            waitScreen.setCommandListener(this);
            waitScreen.setImage(getImage1());
            waitScreen.setText("Sende SMS...");
            waitScreen.setTask(getTask());//GEN-END:|47-getter|1|47-postInit


            }//GEN-BEGIN:|47-getter|2|
            return waitScreen;
        }
        //</editor-fold>//GEN-END:|47-getter|2|

        //<editor-fold defaultstate="collapsed" desc=" Generated Getter: loginSettings ">//GEN-BEGIN:|58-getter|0|58-preInit
        /**
         * Returns an initiliazed instance of loginSettings component.
         * @return the initialized component instance
         */
        public Form getLoginSettings() {
            if (loginSettings == null) {//GEN-END:|58-getter|0|58-preInit
            // write pre-init user code here
                loginSettings = new Form("Settings", new Item[] { getTextField1(), getTextField2(), getChoiceGroup1() });//GEN-BEGIN:|58-getter|1|58-postInit
                loginSettings.addCommand(getBack());
                loginSettings.addCommand(getOkCommand());
                loginSettings.setCommandListener(this);//GEN-END:|58-getter|1|58-postInit
            // write post-init user code here
            }//GEN-BEGIN:|58-getter|2|
            return loginSettings;
        }
        //</editor-fold>//GEN-END:|58-getter|2|

        //<editor-fold defaultstate="collapsed" desc=" Generated Getter: textField1 ">//GEN-BEGIN:|61-getter|0|61-preInit
        /**
         * Returns an initiliazed instance of textField1 component.
         * @return the initialized component instance
         */
        public TextField getTextField1() {
            if (textField1 == null) {//GEN-END:|61-getter|0|61-preInit
            // write pre-init user code here
                textField1 = new TextField("Benutzername:", " ", 32, TextField.ANY);//GEN-LINE:|61-getter|1|61-postInit
            // write post-init user code here
            }//GEN-BEGIN:|61-getter|2|
            return textField1;
        }
        //</editor-fold>//GEN-END:|61-getter|2|

        //<editor-fold defaultstate="collapsed" desc=" Generated Getter: textField2 ">//GEN-BEGIN:|62-getter|0|62-preInit
        /**
         * Returns an initiliazed instance of textField2 component.
         * @return the initialized component instance
         */
        public TextField getTextField2() {
            if (textField2 == null) {//GEN-END:|62-getter|0|62-preInit
            // write pre-init user code here
                textField2 = new TextField("Passwort:", " ", 32, TextField.ANY | TextField.PASSWORD);//GEN-LINE:|62-getter|1|62-postInit
            // write post-init user code here
            }//GEN-BEGIN:|62-getter|2|
            return textField2;
        }
        //</editor-fold>//GEN-END:|62-getter|2|

        //<editor-fold defaultstate="collapsed" desc=" Generated Getter: okCommand ">//GEN-BEGIN:|84-getter|0|84-preInit
        /**
         * Returns an initiliazed instance of okCommand component.
         * @return the initialized component instance
         */
        public Command getOkCommand() {
            if (okCommand == null) {//GEN-END:|84-getter|0|84-preInit
            // write pre-init user code here
                okCommand = new Command("Speichern", Command.OK, 0);//GEN-LINE:|84-getter|1|84-postInit
            // write post-init user code here
            }//GEN-BEGIN:|84-getter|2|
            return okCommand;
        }
        //</editor-fold>//GEN-END:|84-getter|2|

        //<editor-fold defaultstate="collapsed" desc=" Generated Getter: notSend ">//GEN-BEGIN:|90-getter|0|90-preInit
        /**
         * Returns an initiliazed instance of notSend component.
         * @return the initialized component instance
         */
        public Alert getNotSend() {
            if (notSend == null) {//GEN-END:|90-getter|0|90-preInit
            // write pre-init user code here
                notSend = new Alert("Fehler", "", null, null);//GEN-BEGIN:|90-getter|1|90-postInit
                notSend.addCommand(getExitCommand3());
                notSend.addCommand(getOkCommand3());
                notSend.setCommandListener(this);
                notSend.setTimeout(Alert.FOREVER);//GEN-END:|90-getter|1|90-postInit
            // write post-init user code here
            }//GEN-BEGIN:|90-getter|2|
            return notSend;
        }
        //</editor-fold>//GEN-END:|90-getter|2|

        //<editor-fold defaultstate="collapsed" desc=" Generated Getter: smsSend ">//GEN-BEGIN:|91-getter|0|91-preInit
        /**
         * Returns an initiliazed instance of smsSend component.
         * @return the initialized component instance
         */
        public Alert getSmsSend() {
            if (smsSend == null) {//GEN-END:|91-getter|0|91-preInit
            // write pre-init user code here
                smsSend = new Alert("Senden erfolgreich", "SMS gesendet\n"+getRemSMSText(), null, null);//GEN-BEGIN:|91-getter|1|91-postInit
                smsSend.addCommand(getExitCommand2());
                smsSend.addCommand(getOkCommand2());
                smsSend.setCommandListener(this);
                smsSend.setTimeout(Alert.FOREVER);//GEN-END:|91-getter|1|91-postInit
            // write post-init user code here
            }//GEN-BEGIN:|91-getter|2|
            return smsSend;
        }
        //</editor-fold>//GEN-END:|91-getter|2|

        //<editor-fold defaultstate="collapsed" desc=" Generated Getter: exitCommand1 ">//GEN-BEGIN:|98-getter|0|98-preInit
        /**
         * Returns an initiliazed instance of exitCommand1 component.
         * @return the initialized component instance
         */
        public Command getExitCommand1() {
            if (exitCommand1 == null) {//GEN-END:|98-getter|0|98-preInit
            // write pre-init user code here
                exitCommand1 = new Command("Exit", Command.EXIT, 0);//GEN-LINE:|98-getter|1|98-postInit
            // write post-init user code here
            }//GEN-BEGIN:|98-getter|2|
            return exitCommand1;
        }
        //</editor-fold>//GEN-END:|98-getter|2|

        //<editor-fold defaultstate="collapsed" desc=" Generated Getter: exitCommand2 ">//GEN-BEGIN:|103-getter|0|103-preInit
        /**
         * Returns an initiliazed instance of exitCommand2 component.
         * @return the initialized component instance
         */
        public Command getExitCommand2() {
            if (exitCommand2 == null) {//GEN-END:|103-getter|0|103-preInit
            // write pre-init user code here
                exitCommand2 = new Command("Exit", Command.EXIT, 0);//GEN-LINE:|103-getter|1|103-postInit
            // write post-init user code here
            }//GEN-BEGIN:|103-getter|2|
            return exitCommand2;
        }
        //</editor-fold>//GEN-END:|103-getter|2|

        //<editor-fold defaultstate="collapsed" desc=" Generated Getter: okCommand2 ">//GEN-BEGIN:|106-getter|0|106-preInit
        /**
         * Returns an initiliazed instance of okCommand2 component.
         * @return the initialized component instance
         */
        public Command getOkCommand2() {
            if (okCommand2 == null) {//GEN-END:|106-getter|0|106-preInit
            // write pre-init user code here
                okCommand2 = new Command("Ok", Command.OK, 0);//GEN-LINE:|106-getter|1|106-postInit
            // write post-init user code here
            }//GEN-BEGIN:|106-getter|2|
            return okCommand2;
        }
        //</editor-fold>//GEN-END:|106-getter|2|

        //<editor-fold defaultstate="collapsed" desc=" Generated Getter: exitCommand3 ">//GEN-BEGIN:|109-getter|0|109-preInit
        /**
         * Returns an initiliazed instance of exitCommand3 component.
         * @return the initialized component instance
         */
        public Command getExitCommand3() {
            if (exitCommand3 == null) {//GEN-END:|109-getter|0|109-preInit
            // write pre-init user code here
                exitCommand3 = new Command("Exit", Command.EXIT, 0);//GEN-LINE:|109-getter|1|109-postInit
            // write post-init user code here
            }//GEN-BEGIN:|109-getter|2|
            return exitCommand3;
        }
        //</editor-fold>//GEN-END:|109-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: okCommand3 ">//GEN-BEGIN:|111-getter|0|111-preInit
    /**
     * Returns an initiliazed instance of okCommand3 component.
     * @return the initialized component instance
     */
    public Command getOkCommand3() {
        if (okCommand3 == null) {//GEN-END:|111-getter|0|111-preInit
            // write pre-init user code here
            okCommand3 = new Command("Ok", Command.OK, 0);//GEN-LINE:|111-getter|1|111-postInit
            // write post-init user code here
        }//GEN-BEGIN:|111-getter|2|
        return okCommand3;
    }
    //</editor-fold>//GEN-END:|111-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: About ">//GEN-BEGIN:|116-getter|0|116-preInit
    /**
     * Returns an initiliazed instance of About component.
     * @return the initialized component instance
     */
    public Alert getAbout() {
        if (About == null) {//GEN-END:|116-getter|0|116-preInit
            // write pre-init user code here
            About = new Alert("About", "© 2009 Max H\u00E4nze\n© 2010 Christian Morlok\n© 2010 RedRocket\nLizenz: GNU GPL 3.0\nVersion: "+getVersion(), null, null);//GEN-BEGIN:|116-getter|1|116-postInit
            About.setTimeout(Alert.FOREVER);//GEN-END:|116-getter|1|116-postInit
            // write post-init user code here
        }//GEN-BEGIN:|116-getter|2|
        return About;
    }
    //</editor-fold>//GEN-END:|116-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: task ">//GEN-BEGIN:|140-getter|0|140-preInit
    /**
     * Returns an initiliazed instance of task component.
     * @return the initialized component instance
     */
    public SimpleCancellableTask getTask() {
        if (task == null) {//GEN-END:|140-getter|0|140-preInit
            // write pre-init user code here
            task = new SimpleCancellableTask();//GEN-BEGIN:|140-getter|1|140-execute
            task.setExecutable(new org.netbeans.microedition.util.Executable() {
                public void execute() throws Exception {//GEN-END:|140-getter|1|140-execute
                    sendSMS(recvNB, text);
                }//GEN-BEGIN:|140-getter|2|140-postInit
            });//GEN-END:|140-getter|2|140-postInit
            // write post-init user code here
        }//GEN-BEGIN:|140-getter|3|
        return task;
    }
    //</editor-fold>//GEN-END:|140-getter|3|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: textField ">//GEN-BEGIN:|156-getter|0|156-preInit
    /**
     * Returns an initiliazed instance of textField component.
     * @return the initialized component instance
     */
    public TextField getTextField() {
        if (textField == null) {//GEN-END:|156-getter|0|156-preInit
            // write pre-init user code here
            textField = new TextField("Handynummer:", null, 32, TextField.PHONENUMBER);//GEN-BEGIN:|156-getter|1|156-postInit
            textField.setLayout(ImageItem.LAYOUT_CENTER | Item.LAYOUT_TOP | Item.LAYOUT_BOTTOM | Item.LAYOUT_VCENTER | Item.LAYOUT_2);
            textField.setPreferredSize(-1, -1);//GEN-END:|156-getter|1|156-postInit
            // write post-init user code here
        }//GEN-BEGIN:|156-getter|2|
        return textField;
    }
    //</editor-fold>//GEN-END:|156-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: nextSettings ">//GEN-BEGIN:|172-getter|0|172-preInit
    /**
     * Returns an initiliazed instance of nextSettings component.
     * @return the initialized component instance
     */
    public Command getNextSettings() {
        if (nextSettings == null) {//GEN-END:|172-getter|0|172-preInit
            // write pre-init user code here
            nextSettings = new Command("Ok", Command.OK, 0);//GEN-LINE:|172-getter|1|172-postInit
            // write post-init user code here
        }//GEN-BEGIN:|172-getter|2|
        return nextSettings;
    }
    //</editor-fold>//GEN-END:|172-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: list ">//GEN-BEGIN:|165-getter|0|165-preInit
    /**
     * Returns an initiliazed instance of list component.
     * @return the initialized component instance
     */
    public List getList() {
        if (list == null) {//GEN-END:|165-getter|0|165-preInit
            // write pre-init user code here
            list = new List("Einstellungen", Choice.IMPLICIT);//GEN-BEGIN:|165-getter|1|165-postInit
            list.append("Benutzer", null);
            list.append("Zugangsdaten", null);
            list.append("SMS Eigenschaften", null);
            list.append("Optimierung", null);
            list.append("SMS-Anbieter", null);
            list.append("Debug Meldungen", null);
            list.append("About", null);
            list.addCommand(getNextSettings());
            list.addCommand(getBack());
            list.setCommandListener(this);
            list.setSelectedFlags(new boolean[] { false, false, false, false, false, false, false });//GEN-END:|165-getter|1|165-postInit
            // write post-init user code here
        }//GEN-BEGIN:|165-getter|2|
        return list;
    }
    //</editor-fold>//GEN-END:|165-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Method: listAction ">//GEN-BEGIN:|165-action|0|165-preAction
    /**
     * Performs an action assigned to the selected list element in the list component.
     */
    public void listAction() {//GEN-END:|165-action|0|165-preAction
        // enter pre-action user code here
        String __selectedString = getList().getString(getList().getSelectedIndex());//GEN-BEGIN:|165-action|1|260-preAction
        if (__selectedString != null) {
            if (__selectedString.equals("Benutzer")) {//GEN-END:|165-action|1|260-preAction
                // write pre-action user code here
                switchDisplayable(null, getChooseAccount());//GEN-LINE:|165-action|2|260-postAction
                // write post-action user code here
                ChooseAccount.setSelectedIndex(ActiveAccount, true);
            } else if (__selectedString.equals("Zugangsdaten")) {//GEN-LINE:|165-action|3|168-preAction
                // write pre-action user code here
                switchDisplayable(null, getLoginSettings());//GEN-LINE:|165-action|4|168-postAction
                textField1.setString(username);
                textField2.setString(password);
            } else if (__selectedString.equals("SMS Eigenschaften")) {//GEN-LINE:|165-action|5|169-preAction
                // write pre-action user code here
                switchDisplayable(null, getSmsSettings());//GEN-LINE:|165-action|6|169-postAction
                // write post-action user code here
                choiceGroup4.setSelectedIndex(SenderMode, true);
                txtSenderName.setString(SenderName);
                stringItem2.setText("");
                stringItem2.setLabel("");
                //hier muss die der richtige Eintrag markiert werden und der Absender eingetragen werden
            } else if (__selectedString.equals("Optimierung")) {//GEN-LINE:|165-action|7|170-preAction
                // write pre-action user code here
                switchDisplayable(null, getOptimSettings());//GEN-LINE:|165-action|8|170-postAction

                boolean[] selected = {contentLoad, (remSMS != -1), debug};
                choiceGroup.setSelectedFlags(selected);
            } else if (__selectedString.equals("SMS-Anbieter")) {//GEN-LINE:|165-action|9|171-preAction
                // write pre-action user code here
                switchDisplayable(null, getProviderSettings());//GEN-LINE:|165-action|10|171-postAction
                choiceGroup3.setSelectedIndex(provider, true);
            } else if (__selectedString.equals("Debug Meldungen")) {//GEN-LINE:|165-action|11|285-preAction
                // write pre-action user code here
                switchDisplayable(null, getDebug());//GEN-LINE:|165-action|12|285-postAction
                // write post-action user code here
            } else if (__selectedString.equals("About")) {//GEN-LINE:|165-action|13|191-preAction
                // write pre-action user code here
                switchDisplayable(null, getAbout());//GEN-LINE:|165-action|14|191-postAction
                // write post-action user code here
            }//GEN-BEGIN:|165-action|15|165-postAction
        }//GEN-END:|165-action|15|165-postAction
        // enter post-action user code here
    }//GEN-BEGIN:|165-action|16|
    //</editor-fold>//GEN-END:|165-action|16|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: smsSettings ">//GEN-BEGIN:|177-getter|0|177-preInit
    /**
     * Returns an initiliazed instance of smsSettings component.
     * @return the initialized component instance
     */
    public Form getSmsSettings() {
        if (smsSettings == null) {//GEN-END:|177-getter|0|177-preInit
            // write pre-init user code here
            smsSettings = new Form("SMS Eigenschaften", new Item[] { getStringItem(), getChoiceGroup4(), getTxtSenderName(), getStringItem2() });//GEN-BEGIN:|177-getter|1|177-postInit
            smsSettings.addCommand(getBack());
            smsSettings.addCommand(getOkCommand6());
            smsSettings.setCommandListener(this);//GEN-END:|177-getter|1|177-postInit
            // write post-init user code here
        }//GEN-BEGIN:|177-getter|2|
        return smsSettings;
    }
    //</editor-fold>//GEN-END:|177-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: optimSettings ">//GEN-BEGIN:|178-getter|0|178-preInit
    /**
     * Returns an initiliazed instance of optimSettings component.
     * @return the initialized component instance
     */
    public Form getOptimSettings() {
        if (optimSettings == null) {//GEN-END:|178-getter|0|178-preInit
            // write pre-init user code here
            optimSettings = new Form("Optimierung", new Item[] { getChoiceGroup() });//GEN-BEGIN:|178-getter|1|178-postInit
            optimSettings.addCommand(getBack());
            optimSettings.addCommand(getOkCommand());
            optimSettings.setCommandListener(this);//GEN-END:|178-getter|1|178-postInit
            // write post-init user code here
        }//GEN-BEGIN:|178-getter|2|
        return optimSettings;
    }
    //</editor-fold>//GEN-END:|178-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: providerSettings ">//GEN-BEGIN:|179-getter|0|179-preInit
    /**
     * Returns an initiliazed instance of providerSettings component.
     * @return the initialized component instance
     */
    public Form getProviderSettings() {
        if (providerSettings == null) {//GEN-END:|179-getter|0|179-preInit
            // write pre-init user code here
            providerSettings = new Form("Provider Auswahl", new Item[] { getChoiceGroup3() });//GEN-BEGIN:|179-getter|1|179-postInit
            providerSettings.addCommand(getBack());
            providerSettings.addCommand(getOkCommand());
            providerSettings.setCommandListener(this);//GEN-END:|179-getter|1|179-postInit
            // write post-init user code here
        }//GEN-BEGIN:|179-getter|2|
        return providerSettings;
    }
    //</editor-fold>//GEN-END:|179-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: choiceGroup1 ">//GEN-BEGIN:|194-getter|0|194-preInit
    /**
     * Returns an initiliazed instance of choiceGroup1 component.
     * @return the initialized component instance
     */
    public ChoiceGroup getChoiceGroup1() {
        if (choiceGroup1 == null) {//GEN-END:|194-getter|0|194-preInit
            // write pre-init user code here
            choiceGroup1 = new ChoiceGroup(" ", Choice.MULTIPLE);//GEN-BEGIN:|194-getter|1|194-postInit
            choiceGroup1.append("Passwort speichern", null);
            choiceGroup1.setSelectedFlags(new boolean[] { true });
            choiceGroup1.setFont(0, getFont());//GEN-END:|194-getter|1|194-postInit
            // write post-init user code here
        }//GEN-BEGIN:|194-getter|2|
        return choiceGroup1;
    }
    //</editor-fold>//GEN-END:|194-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: choiceGroup ">//GEN-BEGIN:|204-getter|0|204-preInit
    /**
     * Returns an initiliazed instance of choiceGroup component.
     * @return the initialized component instance
     */
    public ChoiceGroup getChoiceGroup() {
        if (choiceGroup == null) {//GEN-END:|204-getter|0|204-preInit
            // write pre-init user code here
            choiceGroup = new ChoiceGroup("Folgende Ver\u00E4nderungen k\u00F6nnen das Programm beschleunigen.\nSie funktionieren nicht auf allen Systemen!", Choice.MULTIPLE);//GEN-BEGIN:|204-getter|1|204-postInit
            choiceGroup.append("Seiten nicht komplett laden", null);
            choiceGroup.append("verbleibende SMS anzeigen", null);
            choiceGroup.append("Debug", null);
            choiceGroup.setFitPolicy(Choice.TEXT_WRAP_DEFAULT);
            choiceGroup.setSelectedFlags(new boolean[] { false, false, false });
            choiceGroup.setFont(0, getFont());
            choiceGroup.setFont(1, getFont());
            choiceGroup.setFont(2, null);//GEN-END:|204-getter|1|204-postInit
            // write post-init user code here
        }//GEN-BEGIN:|204-getter|2|
        return choiceGroup;
    }
    //</editor-fold>//GEN-END:|204-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: font ">//GEN-BEGIN:|211-getter|0|211-preInit
    /**
     * Returns an initiliazed instance of font component.
     * @return the initialized component instance
     */
    public Font getFont() {
        if (font == null) {//GEN-END:|211-getter|0|211-preInit
            // write pre-init user code here
            font = Font.getFont(Font.FONT_INPUT_TEXT);//GEN-LINE:|211-getter|1|211-postInit
            // write post-init user code here
        }//GEN-BEGIN:|211-getter|2|
        return font;
    }
    //</editor-fold>//GEN-END:|211-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: eingabeLeeren ">//GEN-BEGIN:|212-getter|0|212-preInit
    /**
     * Returns an initiliazed instance of eingabeLeeren component.
     * @return the initialized component instance
     */
    public Command getEingabeLeeren() {
        if (eingabeLeeren == null) {//GEN-END:|212-getter|0|212-preInit
            // write pre-init user code here
            eingabeLeeren = new Command("Eingabe l\u00F6schen", Command.CANCEL, 4);//GEN-LINE:|212-getter|1|212-postInit
            // write post-init user code here
        }//GEN-BEGIN:|212-getter|2|
        return eingabeLeeren;
    }
    //</editor-fold>//GEN-END:|212-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: stringItem1 ">//GEN-BEGIN:|215-getter|0|215-preInit
    /**
     * Returns an initiliazed instance of stringItem1 component.
     * @return the initialized component instance
     */
    public StringItem getStringItem1() {
        if (stringItem1 == null) {//GEN-END:|215-getter|0|215-preInit
            // write pre-init user code here
            stringItem1 = new StringItem(" ", null);//GEN-LINE:|215-getter|1|215-postInit
            // write post-init user code here
        }//GEN-BEGIN:|215-getter|2|
        return stringItem1;
    }
    //</editor-fold>//GEN-END:|215-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: setup ">//GEN-BEGIN:|217-getter|0|217-preInit
    /**
     * Returns an initiliazed instance of setup component.
     * @return the initialized component instance
     */
    public Form getSetup() {
        if (setup == null) {//GEN-END:|217-getter|0|217-preInit
            // write pre-init user code here
            setup = new Form("Setup", new Item[] { getTextField5(), getTextField4(), getChoiceGroup2() });//GEN-BEGIN:|217-getter|1|217-postInit
            setup.addCommand(getOkCommand());
            setup.setCommandListener(this);//GEN-END:|217-getter|1|217-postInit
            // write post-init user code here
        }//GEN-BEGIN:|217-getter|2|
        return setup;
    }
    //</editor-fold>//GEN-END:|217-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: textField4 ">//GEN-BEGIN:|219-getter|0|219-preInit
    /**
     * Returns an initiliazed instance of textField4 component.
     * @return the initialized component instance
     */
    public TextField getTextField4() {
        if (textField4 == null) {//GEN-END:|219-getter|0|219-preInit
            // write pre-init user code here
            textField4 = new TextField("Passwort:", null, 32, TextField.ANY | TextField.PASSWORD);//GEN-LINE:|219-getter|1|219-postInit
            // write post-init user code here
        }//GEN-BEGIN:|219-getter|2|
        return textField4;
    }
    //</editor-fold>//GEN-END:|219-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: choiceGroup2 ">//GEN-BEGIN:|220-getter|0|220-preInit
    /**
     * Returns an initiliazed instance of choiceGroup2 component.
     * @return the initialized component instance
     */
    public ChoiceGroup getChoiceGroup2() {
        if (choiceGroup2 == null) {//GEN-END:|220-getter|0|220-preInit
            // write pre-init user code here
            choiceGroup2 = new ChoiceGroup("Anbieter", Choice.EXCLUSIVE);//GEN-BEGIN:|220-getter|1|220-postInit
            choiceGroup2.append("O2-Internet-Pack", null);
            choiceGroup2.append("GMX", null);
            choiceGroup2.setSelectedFlags(new boolean[] { false, false });
            choiceGroup2.setFont(0, getFont());
            choiceGroup2.setFont(1, getFont());//GEN-END:|220-getter|1|220-postInit
            // write post-init user code here
        }//GEN-BEGIN:|220-getter|2|
        return choiceGroup2;
    }
    //</editor-fold>//GEN-END:|220-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: textField5 ">//GEN-BEGIN:|226-getter|0|226-preInit
    /**
     * Returns an initiliazed instance of textField5 component.
     * @return the initialized component instance
     */
    public TextField getTextField5() {
        if (textField5 == null) {//GEN-END:|226-getter|0|226-preInit
            // write pre-init user code here
            textField5 = new TextField("Benutzername:", null, 32, TextField.ANY);//GEN-LINE:|226-getter|1|226-postInit
            // write post-init user code here
        }//GEN-BEGIN:|226-getter|2|
        return textField5;
    }
    //</editor-fold>//GEN-END:|226-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: choiceGroup3 ">//GEN-BEGIN:|229-getter|0|229-preInit
    /**
     * Returns an initiliazed instance of choiceGroup3 component.
     * @return the initialized component instance
     */
    public ChoiceGroup getChoiceGroup3() {
        if (choiceGroup3 == null) {//GEN-END:|229-getter|0|229-preInit
            // write pre-init user code here
            choiceGroup3 = new ChoiceGroup("Bei einem Wechsel: \u00C4ndern der Zugangsdaten nicht vergessen!", Choice.EXCLUSIVE);//GEN-BEGIN:|229-getter|1|229-postInit
            choiceGroup3.append("O2-Internet-Pack", null);
            choiceGroup3.append("GMX", null);
            choiceGroup3.setSelectedFlags(new boolean[] { false, false });
            choiceGroup3.setFont(0, getFont());
            choiceGroup3.setFont(1, getFont());//GEN-END:|229-getter|1|229-postInit
            // write post-init user code here
        }//GEN-BEGIN:|229-getter|2|
        return choiceGroup3;
    }
    //</editor-fold>//GEN-END:|229-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: loginScreenSend ">//GEN-BEGIN:|240-getter|0|240-preInit
    /**
     * Returns an initiliazed instance of loginScreenSend component.
     * @return the initialized component instance
     */
    public Command getLoginScreenSend() {
        if (loginScreenSend == null) {//GEN-END:|240-getter|0|240-preInit
            // write pre-init user code here
            loginScreenSend = new Command("Ok", Command.OK, 0);//GEN-LINE:|240-getter|1|240-postInit
            // write post-init user code here
        }//GEN-BEGIN:|240-getter|2|
        return loginScreenSend;
    }
    //</editor-fold>//GEN-END:|240-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: loginScreen ">//GEN-BEGIN:|235-getter|0|235-preInit
    /**
     * Returns an initiliazed instance of loginScreen component.
     * @return the initialized component instance
     */
    public Form getLoginScreen() {
        if (loginScreen == null) {//GEN-END:|235-getter|0|235-preInit
            // write pre-init user code here
            loginScreen = new Form("Login", new Item[] { getTextField6(), getTextField7() });//GEN-BEGIN:|235-getter|1|235-postInit
            loginScreen.addCommand(getBack());
            loginScreen.addCommand(getLoginScreenSend());
            loginScreen.setCommandListener(this);//GEN-END:|235-getter|1|235-postInit
            // write post-init user code here
        }//GEN-BEGIN:|235-getter|2|
        return loginScreen;
    }
    //</editor-fold>//GEN-END:|235-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: textField6 ">//GEN-BEGIN:|236-getter|0|236-preInit
    /**
     * Returns an initiliazed instance of textField6 component.
     * @return the initialized component instance
     */
    public TextField getTextField6() {
        if (textField6 == null) {//GEN-END:|236-getter|0|236-preInit
            // write pre-init user code here
            textField6 = new TextField("Benutzername:", null, 32, TextField.ANY);//GEN-LINE:|236-getter|1|236-postInit
            // write post-init user code here
        }//GEN-BEGIN:|236-getter|2|
        return textField6;
    }
    //</editor-fold>//GEN-END:|236-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: textField7 ">//GEN-BEGIN:|237-getter|0|237-preInit
    /**
     * Returns an initiliazed instance of textField7 component.
     * @return the initialized component instance
     */
    public TextField getTextField7() {
        if (textField7 == null) {//GEN-END:|237-getter|0|237-preInit
            // write pre-init user code here
            textField7 = new TextField("Passwort:", null, 32, TextField.ANY | TextField.PASSWORD);//GEN-LINE:|237-getter|1|237-postInit
            // write post-init user code here
        }//GEN-BEGIN:|237-getter|2|
        return textField7;
    }
    //</editor-fold>//GEN-END:|237-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: image1 ">//GEN-BEGIN:|244-getter|0|244-preInit
    /**
     * Returns an initiliazed instance of image1 component.
     * @return the initialized component instance
     */
    public Image getImage1() {
        if (image1 == null) {//GEN-END:|244-getter|0|244-preInit
            // write pre-init user code here
            try {//GEN-BEGIN:|244-getter|1|244-@java.io.IOException
                image1 = Image.createImage("/webSMSsend/sendIcon.png");
            } catch (java.io.IOException e) {//GEN-END:|244-getter|1|244-@java.io.IOException
                e.printStackTrace();
            }//GEN-LINE:|244-getter|2|244-postInit
            // write post-init user code here
        }//GEN-BEGIN:|244-getter|3|
        return image1;
    }
    //</editor-fold>//GEN-END:|244-getter|3|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: textField3 ">//GEN-BEGIN:|247-getter|0|247-preInit
    /**
     * Returns an initiliazed instance of textField3 component.
     * @return the initialized component instance
     */
    public TextField getTextField3() {
        if (textField3 == null) {//GEN-END:|247-getter|0|247-preInit
            // write pre-init user code here
            textField3 = new TextField("Eingabe:", null, 1800, TextField.ANY);//GEN-BEGIN:|247-getter|1|247-postInit
            textField3.setLayout(ImageItem.LAYOUT_CENTER | Item.LAYOUT_VEXPAND);//GEN-END:|247-getter|1|247-postInit
            // write post-init user code here
        }//GEN-BEGIN:|247-getter|2|
        return textField3;
    }
    //</editor-fold>//GEN-END:|247-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: okCommand5 ">//GEN-BEGIN:|253-getter|0|253-preInit
    /**
     * Returns an initiliazed instance of okCommand5 component.
     * @return the initialized component instance
     */
    public Command getOkCommand5() {
        if (okCommand5 == null) {//GEN-END:|253-getter|0|253-preInit
            // write pre-init user code here
            okCommand5 = new Command("Ok", Command.OK, 0);//GEN-LINE:|253-getter|1|253-postInit
            // write post-init user code here
        }//GEN-BEGIN:|253-getter|2|
        return okCommand5;
    }
    //</editor-fold>//GEN-END:|253-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: ChooseAccount ">//GEN-BEGIN:|248-getter|0|248-preInit
    /**
     * Returns an initiliazed instance of ChooseAccount component.
     * @return the initialized component instance
     */
    public List getChooseAccount() {
        if (ChooseAccount == null) {//GEN-END:|248-getter|0|248-preInit
            // write pre-init user code here
            ChooseAccount = new List("Benutzerkonto ausw\u00E4hlen", Choice.IMPLICIT);//GEN-BEGIN:|248-getter|1|248-postInit
            ChooseAccount.append("Benutzerkonto 1", null);
            ChooseAccount.append("Benutzerkonto 2", null);
            ChooseAccount.append("Benutzerkonto 3", null);
            ChooseAccount.addCommand(getOkCommand5());
            ChooseAccount.addCommand(getBack());
            ChooseAccount.setCommandListener(this);
            ChooseAccount.setSelectedFlags(new boolean[] { true, false, false });//GEN-END:|248-getter|1|248-postInit
            // write post-init user code here
        }//GEN-BEGIN:|248-getter|2|
        return ChooseAccount;
    }
    //</editor-fold>//GEN-END:|248-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Method: ChooseAccountAction ">//GEN-BEGIN:|248-action|0|248-preAction
    /**
     * Performs an action assigned to the selected list element in the ChooseAccount component.
     */
    public void ChooseAccountAction() {//GEN-END:|248-action|0|248-preAction
        // enter pre-action user code here
        String __selectedString = getChooseAccount().getString(getChooseAccount().getSelectedIndex());//GEN-BEGIN:|248-action|1|251-preAction
        if (__selectedString != null) {
            if (__selectedString.equals("Benutzerkonto 1")) {//GEN-END:|248-action|1|251-preAction
                // write pre-action user code here
//GEN-LINE:|248-action|2|251-postAction
                // write post-action user code here
                ioSettings.saveActiveAccount("0");
            } else if (__selectedString.equals("Benutzerkonto 2")) {//GEN-LINE:|248-action|3|252-preAction
                // write pre-action user code here
//GEN-LINE:|248-action|4|252-postAction
                // write post-action user code here
                ioSettings.saveActiveAccount("1");
            } else if (__selectedString.equals("Benutzerkonto 3")) {//GEN-LINE:|248-action|5|284-preAction
                // write pre-action user code here
//GEN-LINE:|248-action|6|284-postAction
                // write post-action user code here
                ioSettings.saveActiveAccount("2");
            }//GEN-BEGIN:|248-action|7|248-postAction
        }//GEN-END:|248-action|7|248-postAction
        // enter post-action user code here
        //theoretisch wäre ioSettings.saveActiveAccount(""+getChooseAccount().getSelectedIndex()); sinnvoller
        SyncSettings();
    }//GEN-BEGIN:|248-action|8|
    //</editor-fold>//GEN-END:|248-action|8|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: choiceGroup4 ">//GEN-BEGIN:|263-getter|0|263-preInit
    /**
     * Returns an initiliazed instance of choiceGroup4 component.
     * @return the initialized component instance
     */
    public ChoiceGroup getChoiceGroup4() {
        if (choiceGroup4 == null) {//GEN-END:|263-getter|0|263-preInit
            // write pre-init user code here
            choiceGroup4 = new ChoiceGroup("Als Absender verwenden:", Choice.EXCLUSIVE);//GEN-BEGIN:|263-getter|1|263-postInit
            choiceGroup4.append("Meine Mobil-Telefonnummer", null);
            choiceGroup4.append("Text:", null);
            choiceGroup4.setSelectedFlags(new boolean[] { false, false });//GEN-END:|263-getter|1|263-postInit
            // write post-init user code here
        }//GEN-BEGIN:|263-getter|2|
        return choiceGroup4;
    }
    //</editor-fold>//GEN-END:|263-getter|2|
        //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: txtSenderName ">//GEN-BEGIN:|266-getter|0|266-preInit
    /**
     * Returns an initiliazed instance of txtSenderName component.
     * @return the initialized component instance
     */
    public TextField getTxtSenderName() {
        if (txtSenderName == null) {//GEN-END:|266-getter|0|266-preInit
            // write pre-init user code here
            txtSenderName = new TextField("", "", 10, TextField.ANY);//GEN-BEGIN:|266-getter|1|266-postInit
            txtSenderName.setLayout(ImageItem.LAYOUT_DEFAULT);//GEN-END:|266-getter|1|266-postInit
            // write post-init user code here
        }//GEN-BEGIN:|266-getter|2|
        return txtSenderName;
    }
    //</editor-fold>//GEN-END:|266-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: stringItem ">//GEN-BEGIN:|268-getter|0|268-preInit
    /**
     * Returns an initiliazed instance of stringItem component.
     * @return the initialized component instance
     */
    public StringItem getStringItem() {
        if (stringItem == null) {//GEN-END:|268-getter|0|268-preInit
            // write pre-init user code here
            stringItem = new StringItem("F\u00FCr O2-Internet-Pack:", null, Item.PLAIN);//GEN-BEGIN:|268-getter|1|268-postInit
            stringItem.setLayout(ImageItem.LAYOUT_DEFAULT | ImageItem.LAYOUT_NEWLINE_AFTER);//GEN-END:|268-getter|1|268-postInit
            // write post-init user code here
        }//GEN-BEGIN:|268-getter|2|
        return stringItem;
    }
    //</editor-fold>//GEN-END:|268-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: okCommand6 ">//GEN-BEGIN:|269-getter|0|269-preInit
    /**
     * Returns an initiliazed instance of okCommand6 component.
     * @return the initialized component instance
     */
    public Command getOkCommand6() {
        if (okCommand6 == null) {//GEN-END:|269-getter|0|269-preInit
            // write pre-init user code here
            okCommand6 = new Command("Speichern", Command.OK, 0);//GEN-LINE:|269-getter|1|269-postInit
            // write post-init user code here
        }//GEN-BEGIN:|269-getter|2|
        return okCommand6;
    }
    //</editor-fold>//GEN-END:|269-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: stringItem2 ">//GEN-BEGIN:|272-getter|0|272-preInit
    /**
     * Returns an initiliazed instance of stringItem2 component.
     * @return the initialized component instance
     */
    public StringItem getStringItem2() {
        if (stringItem2 == null) {//GEN-END:|272-getter|0|272-preInit
            // write pre-init user code here
            stringItem2 = new StringItem("", "", Item.PLAIN);//GEN-BEGIN:|272-getter|1|272-postInit
            stringItem2.setLayout(ImageItem.LAYOUT_DEFAULT);//GEN-END:|272-getter|1|272-postInit
            // write post-init user code here
        }//GEN-BEGIN:|272-getter|2|
        return stringItem2;
    }
    //</editor-fold>//GEN-END:|272-getter|2|

        //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: BenutzerwahlBestaettigung ">//GEN-BEGIN:|281-getter|0|281-preInit
    /**
     * Returns an initiliazed instance of BenutzerwahlBestaettigung component.
     * @return the initialized component instance
     */
    public Alert getBenutzerwahlBestaettigung() {
        if (BenutzerwahlBestaettigung == null) {//GEN-END:|281-getter|0|281-preInit
            // write pre-init user code here
            BenutzerwahlBestaettigung = new Alert("Benutzerwahl", "Das Benutzerkonto " + (ActiveAccount +1) + " mit dem Benutzernamen '" + username +"' wurde aktiviert", null, AlertType.INFO);//GEN-BEGIN:|281-getter|1|281-postInit
            BenutzerwahlBestaettigung.setTimeout(3000);//GEN-END:|281-getter|1|281-postInit
            // write post-init user code here
        }//GEN-BEGIN:|281-getter|2|
        return BenutzerwahlBestaettigung;
    }
    //</editor-fold>//GEN-END:|281-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: Debug ">//GEN-BEGIN:|287-getter|0|287-preInit
    /**
     * Returns an initiliazed instance of Debug component.
     * @return the initialized component instance
     */
    public TextBox getDebug() {
        if (Debug == null) {//GEN-END:|287-getter|0|287-preInit
            // write pre-init user code here
            Debug = new TextBox("Debug Meldungen", "", 12000, TextField.ANY);//GEN-BEGIN:|287-getter|1|287-postInit
            Debug.addCommand(getBack());
            Debug.addCommand(getSendEmail());
            Debug.addCommand(getClear());
            Debug.setCommandListener(this);//GEN-END:|287-getter|1|287-postInit
            // write post-init user code here
        }//GEN-BEGIN:|287-getter|2|
        return Debug;
    }
    //</editor-fold>//GEN-END:|287-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: Clear ">//GEN-BEGIN:|293-getter|0|293-preInit
    /**
     * Returns an initiliazed instance of Clear component.
     * @return the initialized component instance
     */
    public Command getClear() {
        if (Clear == null) {//GEN-END:|293-getter|0|293-preInit
            // write pre-init user code here
            Clear = new Command("Leeren", Command.SCREEN, 0);//GEN-LINE:|293-getter|1|293-postInit
            // write post-init user code here
        }//GEN-BEGIN:|293-getter|2|
        return Clear;
    }
    //</editor-fold>//GEN-END:|293-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: SendEmail ">//GEN-BEGIN:|297-getter|0|297-preInit
    /**
     * Returns an initiliazed instance of SendEmail component.
     * @return the initialized component instance
     */
    public Command getSendEmail() {
        if (SendEmail == null) {//GEN-END:|297-getter|0|297-preInit
            // write pre-init user code here
            SendEmail = new Command("E-Mail senden", Command.OK, 0);//GEN-LINE:|297-getter|1|297-postInit
            // write post-init user code here
        }//GEN-BEGIN:|297-getter|2|
        return SendEmail;
    }
    //</editor-fold>//GEN-END:|297-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: startEmailClient ">//GEN-BEGIN:|305-getter|0|305-preInit
    /**
     * Returns an initiliazed instance of startEmailClient component.
     * @return the initialized component instance
     */
    public Command getStartEmailClient() {
        if (startEmailClient == null) {//GEN-END:|305-getter|0|305-preInit
            // write pre-init user code here
            startEmailClient = new Command("Senden", Command.OK, 0);//GEN-LINE:|305-getter|1|305-postInit
            // write post-init user code here
        }//GEN-BEGIN:|305-getter|2|
        return startEmailClient;
    }
    //</editor-fold>//GEN-END:|305-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: SendEmailForm ">//GEN-BEGIN:|301-getter|0|301-preInit
    /**
     * Returns an initiliazed instance of SendEmailForm component.
     * @return the initialized component instance
     */
    public Form getSendEmailForm() {
        if (SendEmailForm == null) {//GEN-END:|301-getter|0|301-preInit
            // write pre-init user code here
            SendEmailForm = new Form("Debug Meldungen versenden", new Item[] { getStringItem3(), getTxtCCemail(), getTxtFehlerbeschreibung() });//GEN-BEGIN:|301-getter|1|301-postInit
            SendEmailForm.addCommand(getStartEmailClient());
            SendEmailForm.addCommand(getBack());
            SendEmailForm.setCommandListener(this);//GEN-END:|301-getter|1|301-postInit
            // write post-init user code here
        }//GEN-BEGIN:|301-getter|2|
        return SendEmailForm;
    }
    //</editor-fold>//GEN-END:|301-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: stringItem3 ">//GEN-BEGIN:|302-getter|0|302-preInit
    /**
     * Returns an initiliazed instance of stringItem3 component.
     * @return the initialized component instance
     */
    public StringItem getStringItem3() {
        if (stringItem3 == null) {//GEN-END:|302-getter|0|302-preInit
            // write pre-init user code here
            stringItem3 = new StringItem("Hier k\u00F6nnen die Debug-Meldungen mit einer Fehlerbeschreibung an die Entwickler weitergeleitet werden. Zum Antworten und f\u00FCr Nachfragen wird eine E-Mail-Adresse ben\u00F6tigt!", null, Item.PLAIN);//GEN-LINE:|302-getter|1|302-postInit
            // write post-init user code here
        }//GEN-BEGIN:|302-getter|2|
        return stringItem3;
    }
    //</editor-fold>//GEN-END:|302-getter|2|
        //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: txtFehlerbeschreibung ">//GEN-BEGIN:|303-getter|0|303-preInit
    /**
     * Returns an initiliazed instance of txtFehlerbeschreibung component.
     * @return the initialized component instance
     */
    public TextField getTxtFehlerbeschreibung() {
        if (txtFehlerbeschreibung == null) {//GEN-END:|303-getter|0|303-preInit
            // write pre-init user code here
            txtFehlerbeschreibung = new TextField("Fehlerbeschreibung:", null, 1000, TextField.ANY);//GEN-LINE:|303-getter|1|303-postInit
            // write post-init user code here
        }//GEN-BEGIN:|303-getter|2|
        return txtFehlerbeschreibung;
    }
    //</editor-fold>//GEN-END:|303-getter|2|
        //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: txtCCemail ">//GEN-BEGIN:|304-getter|0|304-preInit
    /**
     * Returns an initiliazed instance of txtCCemail component.
     * @return the initialized component instance
     */
    public TextField getTxtCCemail() {
        if (txtCCemail == null) {//GEN-END:|304-getter|0|304-preInit
            // write pre-init user code here
            txtCCemail = new TextField("E-Mail Adresse", null, 50, TextField.EMAILADDR);//GEN-LINE:|304-getter|1|304-postInit
            // write post-init user code here
        }//GEN-BEGIN:|304-getter|2|
        return txtCCemail;
    }
    //</editor-fold>//GEN-END:|304-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: waitScreen1 ">//GEN-BEGIN:|310-getter|0|310-preInit
    /**
     * Returns an initiliazed instance of waitScreen1 component.
     * @return the initialized component instance
     */
    public WaitScreen getWaitScreen1() {
        if (waitScreen1 == null) {//GEN-END:|310-getter|0|310-preInit
            // write pre-init user code here
            waitScreen1 = new WaitScreen(getDisplay());//GEN-BEGIN:|310-getter|1|310-postInit
            waitScreen1.setTitle("E-Mail Versand");
            waitScreen1.setCommandListener(this);
            waitScreen1.setTask(getTask1());//GEN-END:|310-getter|1|310-postInit
            // write post-init user code here
        }//GEN-BEGIN:|310-getter|2|
        return waitScreen1;
    }
    //</editor-fold>//GEN-END:|310-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: task1 ">//GEN-BEGIN:|313-getter|0|313-preInit
    /**
     * Returns an initiliazed instance of task1 component.
     * @return the initialized component instance
     */
    public SimpleCancellableTask getTask1() {
        if (task1 == null) {//GEN-END:|313-getter|0|313-preInit
            // write pre-init user code here
            task1 = new SimpleCancellableTask();//GEN-BEGIN:|313-getter|1|313-execute
            task1.setExecutable(new org.netbeans.microedition.util.Executable() {
                public void execute() throws Exception {//GEN-END:|313-getter|1|313-execute
                    // write task-execution user code here
                    EmailClient client = new EmailClient(GUI, "d2Vic21zZW5kQGdteC5kZQ==", "ZG5lc3Ntc2Jldw==",
                            "websmsend@gmx.de", "websmssend@googlemail.com", getTxtCCemail().getString(),
                            "Debug-Meldungen", getTxtFehlerbeschreibung().getString() + "\r\n\r\n"
                            + getDebug().getString());
                    if (client.run() == 0) {
                        throw new Exception("failed");
                    }
                }//GEN-BEGIN:|313-getter|2|313-postInit
            });//GEN-END:|313-getter|2|313-postInit
            // write post-init user code here
        }//GEN-BEGIN:|313-getter|3|
        return task1;
    }
    //</editor-fold>//GEN-END:|313-getter|3|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: EmailSent ">//GEN-BEGIN:|315-getter|0|315-preInit
    /**
     * Returns an initiliazed instance of EmailSent component.
     * @return the initialized component instance
     */
    public Alert getEmailSent() {
        if (EmailSent == null) {//GEN-END:|315-getter|0|315-preInit
            // write pre-init user code here
            EmailSent = new Alert("Erfolgreich", "E-Mail wurde erfolgreich versandt", null, AlertType.CONFIRMATION);//GEN-BEGIN:|315-getter|1|315-postInit
            EmailSent.setTimeout(Alert.FOREVER);//GEN-END:|315-getter|1|315-postInit
            // write post-init user code here
        }//GEN-BEGIN:|315-getter|2|
        return EmailSent;
    }
    //</editor-fold>//GEN-END:|315-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: EmailNotSent ">//GEN-BEGIN:|318-getter|0|318-preInit
    /**
     * Returns an initiliazed instance of EmailNotSent component.
     * @return the initialized component instance
     */
    public Alert getEmailNotSent() {
        if (EmailNotSent == null) {//GEN-END:|318-getter|0|318-preInit
            // write pre-init user code here
            EmailNotSent = new Alert("Fehler", "E-Mail nicht versandt. Siehe Debug-Meldungen", null, AlertType.ERROR);//GEN-BEGIN:|318-getter|1|318-postInit
            EmailNotSent.setTimeout(Alert.FOREVER);//GEN-END:|318-getter|1|318-postInit
            // write post-init user code here
        }//GEN-BEGIN:|318-getter|2|
        return EmailNotSent;
    }
    //</editor-fold>//GEN-END:|318-getter|2|
        //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: NoEmail ">//GEN-BEGIN:|321-getter|0|321-preInit
    /**
     * Returns an initiliazed instance of NoEmail component.
     * @return the initialized component instance
     */
    public Alert getNoEmail() {
        if (NoEmail == null) {//GEN-END:|321-getter|0|321-preInit
            // write pre-init user code here
            NoEmail = new Alert("Keine E-Mail Adresse", "Es wurde keine E-Mail Adresse angegeben!\nDamit kann auf Ihre Debug-Meldungen keine Antwort geschickt werden\nMit Weiter trotzdem senden", null, AlertType.CONFIRMATION);//GEN-BEGIN:|321-getter|1|321-postInit
            NoEmail.addCommand(getBack());
            NoEmail.addCommand(getOkCommand1());
            NoEmail.setCommandListener(this);
            NoEmail.setTimeout(Alert.FOREVER);//GEN-END:|321-getter|1|321-postInit
            // write post-init user code here
        }//GEN-BEGIN:|321-getter|2|
        return NoEmail;
    }
    //</editor-fold>//GEN-END:|321-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: okCommand1 ">//GEN-BEGIN:|324-getter|0|324-preInit
    /**
     * Returns an initiliazed instance of okCommand1 component.
     * @return the initialized component instance
     */
    public Command getOkCommand1() {
        if (okCommand1 == null) {//GEN-END:|324-getter|0|324-preInit
            // write pre-init user code here
            okCommand1 = new Command("Weiter", Command.OK, 0);//GEN-LINE:|324-getter|1|324-postInit
            // write post-init user code here
        }//GEN-BEGIN:|324-getter|2|
        return okCommand1;
    }
    //</editor-fold>//GEN-END:|324-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Method: isEmailNotEntered ">//GEN-BEGIN:|330-if|0|330-preIf
    /**
     * Performs an action assigned to the isEmailNotEntered if-point.
     */
    public void isEmailNotEntered() {//GEN-END:|330-if|0|330-preIf
        // enter pre-if user code here
        if (this.getTxtCCemail().getString().equals("")) {//GEN-LINE:|330-if|1|331-preAction
            // write pre-action user code here
            switchDisplayable(null, getNoEmail());//GEN-LINE:|330-if|2|331-postAction
            // write post-action user code here
        } else {//GEN-LINE:|330-if|3|332-preAction
            // write pre-action user code here
            switchDisplayable(null, getWaitScreen1());//GEN-LINE:|330-if|4|332-postAction
            // write post-action user code here
        }//GEN-LINE:|330-if|5|330-postIf
        // enter post-if user code here
    }//GEN-BEGIN:|330-if|6|
    //</editor-fold>//GEN-END:|330-if|6|

    //<editor-fold defaultstate="collapsed" desc=" Generated Method: CharactersCorrect ">//GEN-BEGIN:|336-if|0|336-preIf
    /**
     * Performs an action assigned to the CharactersCorrect if-point.
     */
    public void CharactersCorrect() {//GEN-END:|336-if|0|336-preIf
        // enter pre-if user code here
        if (CheckCharacters(txtSenderName.getString())) {//GEN-LINE:|336-if|1|337-preAction
            SenderMode = choiceGroup4.getSelectedIndex();
            SenderName = txtSenderName.getString();
            ioSettings.saveSenderSetup(SenderMode, SenderName);
            // write pre-action user code here
            switchDisplayable(null, getList());//GEN-LINE:|336-if|2|337-postAction
            // write post-action user code here
        } else {//GEN-LINE:|336-if|3|338-preAction
            // write pre-action user code here
            switchDisplayable(getWrongCharacter(), getSmsSettings());//GEN-LINE:|336-if|4|338-postAction
            // write post-action user code here
        }//GEN-LINE:|336-if|5|336-postIf
        // enter post-if user code here
    }//GEN-BEGIN:|336-if|6|
    //</editor-fold>//GEN-END:|336-if|6|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: WrongCharacter ">//GEN-BEGIN:|340-getter|0|340-preInit
    /**
     * Returns an initiliazed instance of WrongCharacter component.
     * @return the initialized component instance
     */
    public Alert getWrongCharacter() {
        if (WrongCharacter == null) {//GEN-END:|340-getter|0|340-preInit
            // write pre-init user code here
            WrongCharacter = new Alert("Absender", "Nur Buchstaben ohne Umlaute sind erlaubt!", null, AlertType.WARNING);//GEN-BEGIN:|340-getter|1|340-postInit
            WrongCharacter.setTimeout(Alert.FOREVER);//GEN-END:|340-getter|1|340-postInit
            // write post-init user code here
        }//GEN-BEGIN:|340-getter|2|
        return WrongCharacter;
    }
    //</editor-fold>//GEN-END:|340-getter|2|

    /**
     * Returns a display instance.
     * @return the display instance.
     */
    public Display getDisplay() {
        return Display.getDisplay(this);
    }

    /**
     * Exits MIDlet.
     */
    public void exitMIDlet() {
        SaveTempSMS();
        switchDisplayable(null, null);
        destroyApp(true);
        notifyDestroyed();
    }

    /**
     * Called when MIDlet is started.
     * Checks whether the MIDlet have been already started and initialize/starts or resumes the MIDlet.
     */
    public void startApp() {
        if (midletPaused) {
            resumeMIDlet();
        } else {
            initialize();
            startMIDlet();
        }
        midletPaused = false;
    }

    /**
     * Called when MIDlet is paused.
     */
    public void pauseApp() {
        SaveTempSMS();
        midletPaused = true;
    }

    /**
     * Called to signal the MIDlet to terminate.
     * @param unconditional if true, then the MIDlet has to be unconditionally terminated and all resources has to be released.
     */
    public void destroyApp(boolean unconditional) {
        SaveTempSMS();
    }

    void debug(String msg) {
        if (debug) {
            try {
                long currentTime = System.currentTimeMillis() - startTime;
                getDebug().insert(currentTime + ": " + msg + "\n", getDebug().size());
                System.out.println(currentTime + ": " + msg);
            } catch (Exception ex) {
                getDebug().setString("Textbox voll: " + getDebug().size() + " Zeichen --> lösche Inhalt\n");
            }
        }
    }
}