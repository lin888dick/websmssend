/*
 *
 *
Copyright 2009 Max Hänze --- maximum.blogsite.org
Copyright 2010 Christian Morlok --- cmorlok.de
modifiziert 2010 von RedRocket ---
modifiziert 2011 von schirinowski@gmail.com
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

import GMX.GMX;
import O2.O2;
import ConnectorBase.ISmsConnector;
import ConnectorBase.Properties;
import ConnectorBase.SmsData;
import java.io.IOException;
import java.io.PrintStream;
import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;
import org.netbeans.microedition.lcdui.WaitScreen;
import org.netbeans.microedition.util.SimpleCancellableTask;

public class webSMSsend extends MIDlet implements CommandListener, IGui {

    public static final String MESSAGE_TEXTFIELD_LABEL = "0 (0 SMS)";
    //FOR DEBUG ONLY!! SMS will not be sent if true
    public static boolean simulation = false;
    public static final int SENDERMODE_STANDARD = 0;
    public static final int SENDERMODE_TEXT = 1;
    /** if true SMS-Connector should resume sending a SMS after use approval */
    boolean resumeMode=false;
    /** if true there are no free sms available and user is asked what to do */
    boolean noMoreFreeSMS=false;
    String recvNB;
    String text;
    String username;
    String password;
    int provider; //0 = O2, 1 = GMX
    boolean saveEachCharacter;
    boolean debug;
    PrintStream debugPrintStream;
    private long startTime = 0;
    int ActiveAccount; //0=Account1 1=Account2 etc.
    String SenderName; //Name bei Text als Absender
    int SenderMode; //0=Phonenumber, 1=Text( SenderName )
    private boolean midletPaused = false;
    /** Determines the app's automatic update behaviour */
    boolean autoUpdate = true;
    /** Saves the update status, whether there is one available */
    private boolean updateAvailable = false;
    /** Stores the update's versio number */
    private String currentVersion = "0.0.0";
    private webSMSsend GUI;
    private ISmsConnector SmsConnector;
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
    private Command LastSMS;
    private Command okCommand1;
    private Command switchAccount;
    private Command cancelCommand;
    private Command installUpdateCmd;
    private Command cancelUpdateCmd;
    private Command okCommandResumeSendSms;
    private Command backToPropertiesCmd;
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
    private StringItem StatusLabel;
    private ChoiceGroup choiceGroup4;
    private TextField txtSenderName;
    private Form setup;
    private ChoiceGroup choiceGroup2;
    private TextField textField4;
    private TextField textField5;
    private Form loginScreen;
    private TextField textField7;
    private TextField textField6;
    private List chooseAccount;
    private Alert BenutzerwahlBestaetigung;
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
    private Alert noMoreFreeSmsScreen;
    private WaitScreen updateWaitScreen;
    private Alert installUpdateQuery;
    private Alert errorAlert;
    private SimpleCancellableTask task;
    private Font font;
    private Image image1;
    private SimpleCancellableTask task1;
    private Font smallFont;
    private SimpleCancellableTask checkForUpdateTask;
    private Image updateIcon;
    //</editor-fold>//GEN-END:|fields|0|

    /**
     * webSMSsend's constructor.
     */
    public webSMSsend() throws IOException {
        GUI = this;
//#if Test
//#         simulation = true;
//#else
    simulation = false;
//#endif
    }

    private boolean checkCharacters(String text) {
        boolean correctcharacters = true;
        char[] checkname = text.toCharArray();
        for (int i = 0; i < checkname.length; i++) {
            if ((checkname[i] >= 'a' && checkname[i] <= 'z') || (checkname[i] >= 'A' && checkname[i] <= 'Z')) {
            } else {
                correctcharacters = false;
                break;
            }
        }
        return correctcharacters;
    }

    private String getVersion() {
        return getAppProperty("MIDlet-Version");
    }

    private void checkForUpdate() throws Exception {
        debug("Pr\u00FCfe auf Update");
        // Check for update and store the most current version number
        currentVersion = Update.getCurrentVersion();
        if (Update.isUpdateAvailable(getVersion())) {
            debug("Update ist verf\u00FCgbar."
                    + "\nInstallierte Version: " + getVersion()
                    + "\nVerf\u00FCgbare Version: " + currentVersion
                    + "\nURL: " + Update.getBuildUrl());
            updateAvailable = true;
        } else {
            debug("Kein Update verf\u00FCgbar."
                    + "\nInstallierte Version: " + getVersion()
                    + "\nVerf\u00FCgbare Version: " + currentVersion
                    + "\nURL: " + Update.getBuildUrl());
        }
    }

    private void installUpdate() {
        getUpdateWaitScreen().setTitle("Installiere Update");
        updateWaitScreen.setTask(null);
        debug("Installiere Update");
        switchDisplayable(null, updateWaitScreen);
        try {
            platformRequest(Update.getBuildUrl());
        } catch (Exception ex) {
            debug("Update fehlgeschlagen: " + ex.getMessage());
            getErrorAlert().setString(ex.getMessage());
            switchDisplayable(getErrorAlert(), getMainMenu());
            errorAlert = null;
        }
    }

    private String getPasswordFieldLabel() {
        return SmsConnector.getPasswordFieldLabel();
    }

    private String getRemSMSText() {
            return SmsConnector.getRemSmsText()
                    + "\nBenutzerkonto " + (ActiveAccount + 1);
    }

    public void setWaitScreenText(String Text) {
        if (waitScreen != null) {
            waitScreen.setText(Text);
            debug(Text);
        }
    }

    public int sendSMS(String smsRecv, String smsText) throws Exception {
        if (resumeMode) {
            resumeMode=false;
            if (SmsConnector.hasProperty(Properties.CAN_ABORT_SEND_PROCESS_WHEN_NO_FREE_SMS_AVAILABLE)){
                if (SmsConnector.resumeSending()== false){
                    sendSMS(smsRecv, smsText);
                }
            }else{
                sendSMS(smsRecv, smsText);
            }
        } else {
            String sendername = "";

            if (SenderMode == 1) { //SenderMode = 1 TEXT as Sender
                sendername = SenderName;
            }

            int retValue = SmsConnector.send(new SmsData(username, password, this, smsRecv, smsText, sendername, simulation));

            if (retValue == SmsConnector.NO_MORE_FREE_SMS) { //Send process aborted having no more free sms
                noMoreFreeSMS = true;
                throw new Exception("No more Free-SMS");
            }
        }
        return 0;
    }

    private void syncSettings() {
        username = ioSettings.getUsername();
        password = ioSettings.getPassword();
        provider = ioSettings.getSetup();
        saveEachCharacter = ioSettings.getSaveEachCharacter().equals("true");
        ActiveAccount = Integer.parseInt(ioSettings.getActiveAccount());
        SenderMode = ioSettings.getSenderMode();
        SenderName = ioSettings.getSenderName();
        debug = ioSettings.getDebug().equals("true");
        autoUpdate = ioSettings.getAutoUpdate().equals("true");

        if (simulation) {
            debug = true;
        }

        if (provider == 0) { //set correct SMS-Provider
            SmsConnector = new O2();
        } else {
            SmsConnector = new GMX();
        }

        if (stringItem1 != null)
            stringItem1.setText(getRemSMSText());

        debug("ActiveAccount: " + ActiveAccount);
    }

    private void saveTempSMS() {
        if (getMainMenu() != null) {
            ioSettings.saveTempSMS(textField.getString(), textField3.getString());
        }
    }

    private void retrieveTempSMS() {
        textField3.setString("" + ioSettings.getTempSMStext());
        textField.setString("" + ioSettings.getTempSMSto());
        textField3.notifyStateChanged();
        getDisplay().setCurrentItem(textField);
    }

    private void clearSMSInput() {
        textField3.setString("");
        textField3.setLabel(MESSAGE_TEXTFIELD_LABEL);
        textField.setString("");
        getDisplay().setCurrentItem(textField);
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
                syncSettings();
                textField3.notifyStateChanged();
            }
//GEN-LINE:|3-startMIDlet|1|3-postAction
        debugSystemProperties();
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
        if (displayable == Debug) {//GEN-BEGIN:|7-commandAction|1|294-preAction
            if (command == Clear) {//GEN-END:|7-commandAction|1|294-preAction
                    // write pre-action user code here
                getDebug().setString(getDebug().size() +" Zeichen gel\u00F6scht");//GEN-LINE:|7-commandAction|2|294-postAction
                    // write post-action user code here
            } else if (command == SendEmail) {//GEN-LINE:|7-commandAction|3|298-preAction
                    // write pre-action user code here
                switchDisplayable(null, getSendEmailForm());//GEN-LINE:|7-commandAction|4|298-postAction
                    // write post-action user code here
            } else if (command == back) {//GEN-LINE:|7-commandAction|5|291-preAction
                    // write pre-action user code here
                switchDisplayable(null, getList());//GEN-LINE:|7-commandAction|6|291-postAction
                    // write post-action user code here
            }//GEN-BEGIN:|7-commandAction|7|348-preAction
        } else if (displayable == MainMenu) {
            if (command == LastSMS) {//GEN-END:|7-commandAction|7|348-preAction
                // write pre-action user code here
                textField.setString("" + ioSettings.getLastSMSto());//GEN-BEGIN:|7-commandAction|8|348-postAction
                textField3.setString("" + ioSettings.getLastSMStext());
                textField3.notifyStateChanged();//GEN-END:|7-commandAction|8|348-postAction
                // write post-action user code here
            } else if (command == eingabeLeeren) {//GEN-LINE:|7-commandAction|9|245-preAction
                    // write pre-action user code here
                clearSMSInput();//GEN-LINE:|7-commandAction|10|245-postAction
                    // write post-action user code here
            } else if (command == exitCommand) {//GEN-LINE:|7-commandAction|11|19-preAction
                    // write pre-action user code here
                exitMIDlet();//GEN-LINE:|7-commandAction|12|19-postAction
                    // write post-action user code here
            } else if (command == goToSettings) {//GEN-LINE:|7-commandAction|13|82-preAction
                    // write pre-action user code here
                switchDisplayable(null, getList());//GEN-LINE:|7-commandAction|14|82-postAction

            } else if (command == writeSMS) {//GEN-LINE:|7-commandAction|15|29-preAction
                    recvNB = textField.getString();
                    text = textField3.getString();
                    debug("Senden pressed");
                    resumeMode = false;
                    noMoreFreeSMS = false;
                    if (!password.equals("")) {
                        switchDisplayable(null, getWaitScreen());//GEN-LINE:|7-commandAction|16|29-postAction
                    } else {
                        switchDisplayable(null, getLoginScreen());
                        textField6.setString(username);
                    }

            }//GEN-BEGIN:|7-commandAction|17|323-preAction
        } else if (displayable == NoEmail) {
            if (command == back) {//GEN-END:|7-commandAction|17|323-preAction
                    // write pre-action user code here
                switchDisplayable(null, getSendEmailForm());//GEN-LINE:|7-commandAction|18|323-postAction
                    // write post-action user code here
            } else if (command == okCommand1) {//GEN-LINE:|7-commandAction|19|325-preAction
                    // write pre-action user code here
                switchDisplayable(null, getWaitScreen1());//GEN-LINE:|7-commandAction|20|325-postAction
                    // write post-action user code here
            }//GEN-BEGIN:|7-commandAction|21|307-preAction
        } else if (displayable == SendEmailForm) {
            if (command == back) {//GEN-END:|7-commandAction|21|307-preAction
                    // write pre-action user code here
                switchDisplayable(null, getDebug());//GEN-LINE:|7-commandAction|22|307-postAction
                    // write post-action user code here
            } else if (command == startEmailClient) {//GEN-LINE:|7-commandAction|23|306-preAction
                    // write pre-action user code here
                isEmailNotEntered();//GEN-LINE:|7-commandAction|24|306-postAction
                    // write post-action user code here
            }//GEN-BEGIN:|7-commandAction|25|249-preAction
        } else if (displayable == chooseAccount) {
            if (command == List.SELECT_COMMAND) {//GEN-END:|7-commandAction|25|249-preAction
                    // write pre-action user code here
                chooseAccountAction();//GEN-LINE:|7-commandAction|26|249-postAction
                    // write post-action user code here
                    switchDisplayable(getBenutzerwahlBestaetigung(), getList());
                    BenutzerwahlBestaetigung = null;
            } else if (command == back) {//GEN-LINE:|7-commandAction|27|255-preAction
                    // write pre-action user code here
                switchToPreviousDisplayable();//GEN-LINE:|7-commandAction|28|255-postAction
                // write post-action user code here
            } else if (command == okCommand5) {//GEN-LINE:|7-commandAction|29|254-preAction
                debug("resumeMode: "+ resumeMode);
                chooseAccountAction();

                if (resumeMode) {
                    waitScreen = null;
                    switchDisplayable(getBenutzerwahlBestaetigung(), getWaitScreen());
                } else {
                    // write pre-action user code here
                    switchDisplayable(getBenutzerwahlBestaetigung(), getList());//GEN-LINE:|7-commandAction|30|254-postAction
                    // write post-action user code here
                }
                BenutzerwahlBestaetigung = null;
            }//GEN-BEGIN:|7-commandAction|31|408-preAction
        } else if (displayable == installUpdateQuery) {
            if (command == cancelUpdateCmd) {//GEN-END:|7-commandAction|31|408-preAction
                // write pre-action user code here
                switchDisplayable(null, getMainMenu());//GEN-LINE:|7-commandAction|32|408-postAction
                // write post-action user code here
            } else if (command == installUpdateCmd) {//GEN-LINE:|7-commandAction|33|410-preAction
                // write pre-action user code here
                installUpdate();//GEN-LINE:|7-commandAction|34|410-postAction
                // write post-action user code here
            }//GEN-BEGIN:|7-commandAction|35|166-preAction
        } else if (displayable == list) {
            if (command == List.SELECT_COMMAND) {//GEN-END:|7-commandAction|35|166-preAction
                    // write pre-action user code here
                listAction();//GEN-LINE:|7-commandAction|36|166-postAction
                    // write post-action user code here
            } else if (command == back) {//GEN-LINE:|7-commandAction|37|184-preAction
                    // write pre-action user code here
                switchDisplayable(null, getMainMenu());//GEN-LINE:|7-commandAction|38|184-postAction

            } else if (command == nextSettings) {//GEN-LINE:|7-commandAction|39|173-preAction
                    // write pre-action user code here
                listAction();//GEN-LINE:|7-commandAction|40|173-postAction
                    // write post-action user code here
            }//GEN-BEGIN:|7-commandAction|41|238-preAction
        } else if (displayable == loginScreen) {
            if (command == back) {//GEN-END:|7-commandAction|41|238-preAction


                switchToPreviousDisplayable();//GEN-LINE:|7-commandAction|42|238-postAction
                    // write post-action user code here
            } else if (command == loginScreenSend) {//GEN-LINE:|7-commandAction|43|241-preAction
                    username = textField6.getString();
                    password = textField7.getString();
                    switchDisplayable(null, getWaitScreen());//GEN-LINE:|7-commandAction|44|241-postAction
                    // write post-action user code here
            }//GEN-BEGIN:|7-commandAction|45|65-preAction
        } else if (displayable == loginSettings) {
            if (command == back) {//GEN-END:|7-commandAction|45|65-preAction
                    // write pre-action user code here
                switchToPreviousDisplayable();//GEN-LINE:|7-commandAction|46|65-postAction
                    // write post-action user code here
            } else if (command == okCommand) {//GEN-LINE:|7-commandAction|47|85-preAction

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
                    syncSettings();
                    switchToPreviousDisplayable();//GEN-LINE:|7-commandAction|48|85-postAction
                    // write post-action user code here
            }//GEN-BEGIN:|7-commandAction|49|383-preAction
        } else if (displayable == noMoreFreeSmsScreen) {
            if (command == cancelCommand) {//GEN-END:|7-commandAction|49|383-preAction
                // write pre-action user code here
                switchDisplayable(null, getMainMenu());//GEN-LINE:|7-commandAction|50|383-postAction
                // write post-action user code here
            } else if (command == okCommandResumeSendSms) {//GEN-LINE:|7-commandAction|51|385-preAction
                resumeMode = true;
                // write pre-action user code here
                switchDisplayable(null, getWaitScreen());//GEN-LINE:|7-commandAction|52|385-postAction
                // write post-action user code here
            } else if (command == switchAccount) {//GEN-LINE:|7-commandAction|53|381-preAction
                resumeMode = true;
                // write pre-action user code here
                switchDisplayable(null, getChooseAccount());//GEN-LINE:|7-commandAction|54|381-postAction
                // write post-action user code here
            }//GEN-BEGIN:|7-commandAction|55|112-preAction
        } else if (displayable == notSend) {
            if (command == okCommand3) {//GEN-END:|7-commandAction|55|112-preAction
                    // write pre-action user code here
                switchDisplayable(null, getMainMenu());//GEN-LINE:|7-commandAction|56|112-postAction
                    // write post-action user code here
            }//GEN-BEGIN:|7-commandAction|57|187-preAction
        } else if (displayable == optimSettings) {
            if (command == back) {//GEN-END:|7-commandAction|57|187-preAction
                    // write pre-action user code here
                switchToPreviousDisplayable();//GEN-LINE:|7-commandAction|58|187-postAction
                    // write post-action user code here
            } else if (command == okCommand) {//GEN-LINE:|7-commandAction|59|208-preAction
                    if (choiceGroup.isSelected(0)) {
                        ioSettings.saveSaveEachCharacter("true");
                        saveEachCharacter = true;
                    } else {
                        ioSettings.saveSaveEachCharacter("false");
                        saveEachCharacter = false;
                    }
                    if (choiceGroup.isSelected(1)) {
                        ioSettings.saveDebug("true");
                        debug = true;
                    } else {
                        ioSettings.saveDebug("false");
                        debug = false;
                    }
                    if (choiceGroup.isSelected(2)) {
                        ioSettings.saveAutoUpdate("true");
                        autoUpdate = true;
                    } else {
                        ioSettings.saveAutoUpdate("false");
                        autoUpdate = false;
                    }
                    syncSettings();
                    switchDisplayable(null, getList());//GEN-LINE:|7-commandAction|60|208-postAction
                    // write post-action user code here
            }//GEN-BEGIN:|7-commandAction|61|189-preAction
        } else if (displayable == providerSettings) {
            if (command == back) {//GEN-END:|7-commandAction|61|189-preAction
                    // write pre-action user code here
                switchToPreviousDisplayable();//GEN-LINE:|7-commandAction|62|189-postAction
                    // write post-action user code here
            } else if (command == okCommand) {//GEN-LINE:|7-commandAction|63|232-preAction
                if (choiceGroup3.getSelectedIndex() != -1) {
                    provider = choiceGroup3.getSelectedIndex();
                    ioSettings.saveSetup("" + provider);
                    // Set appropriate password field label
                    if (getLoginSettings() != null) {
                        textField2.setLabel(getPasswordFieldLabel());
                    }
                }
                syncSettings();
                switchToPreviousDisplayable();//GEN-LINE:|7-commandAction|64|232-postAction
            }//GEN-BEGIN:|7-commandAction|65|227-preAction
        } else if (displayable == setup) {
            if (command == okCommand) {//GEN-END:|7-commandAction|65|227-preAction
                    username = textField5.getString();
                    password = textField4.getString();
                    provider = choiceGroup2.getSelectedIndex();
                    debug("Provider: " + provider);
                    debug = false;

                    if (provider == -1) {
                        provider = 0;
                    }
                    ioSettings.saveSaveEachCharacter("true");
                    ioSettings.saveDebug("false");
                    ioSettings.saveSetup("" + (provider));
                    ioSettings.saveToRMS(username, password);
                    switchDisplayable(null, getMainMenu());//GEN-LINE:|7-commandAction|66|227-postAction
                    syncSettings();
                    // write post-action user code here
            }//GEN-BEGIN:|7-commandAction|67|107-preAction
        } else if (displayable == smsSend) {
            if (command == okCommand2) {//GEN-END:|7-commandAction|67|107-preAction
                    // write pre-action user code here
                switchDisplayable(null, getMainMenu());
                isAutomaticUpdateWished();//GEN-LINE:|7-commandAction|68|107-postAction
//                stringItem1.setText(getRemSMSText());
            }//GEN-BEGIN:|7-commandAction|69|183-preAction
        } else if (displayable == smsSettings) {
            if (command == back) {//GEN-END:|7-commandAction|69|183-preAction
                    // write pre-action user code here
                switchToPreviousDisplayable();//GEN-LINE:|7-commandAction|70|183-postAction
                    // write post-action user code here
            } else if (command == okCommand6) {//GEN-LINE:|7-commandAction|71|270-preAction
                    if (txtSenderName.getString().length() < 5 && choiceGroup4.getSelectedIndex() != 0) {
                        //Fehlermeldung "Name zu kurz" falls Text als Absender gewählt
                        StatusLabel.setText("Der Absender muss mindestens 5 Buchstaben lang sein");
                        StatusLabel.setLabel("Achtung!");
                    } else {
                        // write pre-action user code here
                        charactersCorrect();//GEN-LINE:|7-commandAction|72|270-postAction
                        // write post-action user code here
                    }
            }//GEN-BEGIN:|7-commandAction|73|402-preAction
        } else if (displayable == updateWaitScreen) {
            if (command == WaitScreen.FAILURE_COMMAND) {//GEN-END:|7-commandAction|73|402-preAction
                getErrorAlert().setString("Update fehlgeschlagen:\n"
                        + getCheckForUpdateTask().getFailureMessage());
                getErrorAlert().setType(null);
                debug("Update fehlgeschlagen: "
                        + getCheckForUpdateTask().getFailureMessage());
                switchDisplayable(getErrorAlert(), getList());//GEN-LINE:|7-commandAction|74|402-postAction
                errorAlert = null;
                // Reset update process
                updateWaitScreen = null;
            } else if (command == WaitScreen.SUCCESS_COMMAND) {//GEN-LINE:|7-commandAction|75|401-preAction
                if (getUpdateWaitScreen().getTask() != getCheckForUpdateTask()) {
                    getErrorAlert().setTitle("Update fortsetzen");
                    getErrorAlert().setString("Das System wurde angewiesen, "
                            + "die aktuellste Version zu installieren.");
                    debug("Das System wurde angewiesen, "
                            + "die aktuellste Version zu installieren.");
                    getErrorAlert().setType(null);
                    switchDisplayable(getErrorAlert(), getMainMenu());
                    // Reset update process
                    updateWaitScreen = null;
                } else {
                    isUpdateAvailable();//GEN-LINE:|7-commandAction|76|401-postAction
                }
            } else if (command == backToPropertiesCmd) {//GEN-LINE:|7-commandAction|77|426-preAction
                // write pre-action user code here
                switchDisplayable(null, getList());//GEN-LINE:|7-commandAction|78|426-postAction
                // write post-action user code here
            }//GEN-BEGIN:|7-commandAction|79|51-preAction
        } else if (displayable == waitScreen) {
            if (command == WaitScreen.FAILURE_COMMAND) {//GEN-END:|7-commandAction|79|51-preAction
                    getNotSend().setString("SMS nicht gesendet!");
                    noMoreFreeSmsAvailable();//GEN-LINE:|7-commandAction|80|51-postAction

            } else if (command == WaitScreen.SUCCESS_COMMAND) {//GEN-LINE:|7-commandAction|81|50-preAction
                    //Save LastSMS
                    ioSettings.saveLastSMS(textField.getString(), textField3.getString());
                    clearSMSInput();
                    getSmsSend().setString("SMS gesendet\n" + getRemSMSText());
                    getStringItem1().setText(getRemSMSText());
                    switchDisplayable(null, getSmsSend());//GEN-LINE:|7-commandAction|82|50-postAction

            } else if (command == exitCommand1) {//GEN-LINE:|7-commandAction|83|99-preAction
                    // write pre-action user code here
                switchDisplayable(null, getMainMenu());//GEN-LINE:|7-commandAction|84|99-postAction
                    // write post-action user code here
                waitScreen = null;
            }//GEN-BEGIN:|7-commandAction|85|312-preAction
        } else if (displayable == waitScreen1) {
            if (command == WaitScreen.FAILURE_COMMAND) {//GEN-END:|7-commandAction|85|312-preAction
                    // write pre-action user code here
                switchDisplayable(getEmailNotSent(), getSendEmailForm());//GEN-LINE:|7-commandAction|86|312-postAction
                    // write post-action user code here
            } else if (command == WaitScreen.SUCCESS_COMMAND) {//GEN-LINE:|7-commandAction|87|311-preAction
                    // write pre-action user code here
                switchDisplayable(getEmailSent(), getDebug());//GEN-LINE:|7-commandAction|88|311-postAction
                    // write post-action user code here
            }//GEN-BEGIN:|7-commandAction|89|7-postCommandAction
        }//GEN-END:|7-commandAction|89|7-postCommandAction
            // write post-action user code here
    }//GEN-BEGIN:|7-commandAction|90|
    //</editor-fold>//GEN-END:|7-commandAction|90|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: exitCommand ">//GEN-BEGIN:|18-getter|0|18-preInit
    /**
     * Returns an initiliazed instance of exitCommand component.
     * @return the initialized component instance
     */
    public Command getExitCommand() {
        if (exitCommand == null) {//GEN-END:|18-getter|0|18-preInit
                // write pre-init user code here
            exitCommand = new Command("Beenden", Command.EXIT, 0);//GEN-LINE:|18-getter|1|18-postInit
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
            MainMenu.addCommand(getLastSMS());
            MainMenu.setCommandListener(this);//GEN-END:|14-getter|1|14-postInit
            // write post-init user code here
            ItemStateListener listener = new ItemStateListener() {

                public void itemStateChanged(Item item) {
                    if (item == textField3) {
                        StringBuffer smsInputLabel = new StringBuffer().append(textField3.getString().length())
                                .append(" (").append(SmsConnector.countSms(textField3.getString()))
                                .append(" SMS)");
                        if (textField3.size() > SmsConnector.getMaxSMSLength()) {
                            smsInputLabel.append("\nSMS ist zu lang! Max. ")
                                    .append(SmsConnector.getMaxSMSLength())
                                    .append(" Zeichen!");
                        }
                        textField3.setLabel(smsInputLabel.toString());
                        if (saveEachCharacter) {
                            saveTempSMS();
                        }
                    }
                }
            };
            MainMenu.setItemStateListener(listener);
            retrieveTempSMS();
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
            goToSettings = new Command("Einstellungen", Command.CANCEL, 2);//GEN-LINE:|24-getter|1|24-postInit
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
            textField1 = new TextField("Benutzername:", " ", 32, TextField.ANY);//GEN-BEGIN:|61-getter|1|61-postInit
            textField1.setLayout(ImageItem.LAYOUT_LEFT | Item.LAYOUT_TOP | Item.LAYOUT_VCENTER | Item.LAYOUT_VSHRINK | Item.LAYOUT_EXPAND);//GEN-END:|61-getter|1|61-postInit
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
            textField2 = new TextField(getPasswordFieldLabel(), " ", 32, TextField.ANY | TextField.PASSWORD);//GEN-BEGIN:|62-getter|1|62-postInit
            textField2.setLayout(ImageItem.LAYOUT_LEFT | Item.LAYOUT_TOP | Item.LAYOUT_VCENTER | Item.LAYOUT_VSHRINK | Item.LAYOUT_EXPAND);//GEN-END:|62-getter|1|62-postInit
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
            notSend.addCommand(getOkCommand3());
            notSend.setCommandListener(this);
            notSend.setTimeout(Alert.FOREVER);//GEN-END:|90-getter|1|90-postInit
                // write post-init user code here
            try {
                AlertType.WARNING.playSound(Display.getDisplay(this));
                Thread.sleep(500);
                AlertType.WARNING.playSound(Display.getDisplay(this));
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
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
            exitCommand1 = new Command("Abbrechen", Command.EXIT, 0);//GEN-LINE:|98-getter|1|98-postInit
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
            exitCommand2 = new Command("Beenden", Command.EXIT, 0);//GEN-LINE:|103-getter|1|103-postInit
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
            okCommand2 = new Command("OK", Command.OK, 0);//GEN-LINE:|106-getter|1|106-postInit
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
            exitCommand3 = new Command("Beenden", Command.EXIT, 0);//GEN-LINE:|109-getter|1|109-postInit
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
            okCommand3 = new Command("OK", Command.OK, 0);//GEN-LINE:|111-getter|1|111-postInit
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
            About = new Alert("About", "© 2009 Max H\u00E4nze\n© 2010 Christian Morlok\n© 2010-2011 RedRocket\n© 2011 Schirinowski\nLizenz: GNU GPL 3.0\nVersion: "+getVersion(), null, null);//GEN-BEGIN:|116-getter|1|116-postInit
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
                    if (autoUpdate && (!Update.alreadyCheckedForUpdate() || updateAvailable)) {
                        setWaitScreenText("Pr\u00FCfe auf Update...");
                        checkForUpdate();
                    }
                    else {
                        updateAvailable = false;
                    }
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
            textField = new TextField("Empf\u00E4nger (Handynummer):", null, 32, TextField.PHONENUMBER);//GEN-BEGIN:|156-getter|1|156-postInit
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
            nextSettings = new Command("OK", Command.OK, 0);//GEN-LINE:|172-getter|1|172-postInit
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
            list.append("Auf Update pr\u00FCfen", null);
            list.append("About", null);
            list.addCommand(getNextSettings());
            list.addCommand(getBack());
            list.setCommandListener(this);
            list.setSelectedFlags(new boolean[] { false, false, false, false, false, false, false, false });//GEN-END:|165-getter|1|165-postInit
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
                chooseAccount.setSelectedIndex(ActiveAccount, true);
            } else if (__selectedString.equals("Zugangsdaten")) {//GEN-LINE:|165-action|3|168-preAction
                // write pre-action user code here
                switchDisplayable(null, getLoginSettings());//GEN-LINE:|165-action|4|168-postAction
                textField1.setString(username);
                textField2.setString(password);
            } else if (__selectedString.equals("SMS Eigenschaften")) {//GEN-LINE:|165-action|5|169-preAction
                // write pre-action user code here
                switchDisplayable(null, getSmsSettings());//GEN-LINE:|165-action|6|169-postAction
                // write post-action user code here
                if (SmsConnector.hasProperty(Properties.CAN_SEND_NAME_AS_SENDER)) {
                    choiceGroup4.setSelectedIndex(SenderMode, true);
                    txtSenderName.setString(SenderName);
                    StatusLabel.setText("");
                    StatusLabel.setLabel("");
                }
                else{
                    StatusLabel.setText("nicht unterst\u00FCtzt!");
                }
                //hier muss die der richtige Eintrag markiert werden und der Absender eingetragen werden
            } else if (__selectedString.equals("Optimierung")) {//GEN-LINE:|165-action|7|170-preAction
                // write pre-action user code here
                switchDisplayable(null, getOptimSettings());//GEN-LINE:|165-action|8|170-postAction

                boolean[] selected = {saveEachCharacter, debug, autoUpdate};
                choiceGroup.setSelectedFlags(selected);
            } else if (__selectedString.equals("SMS-Anbieter")) {//GEN-LINE:|165-action|9|171-preAction
                // write pre-action user code here
                switchDisplayable(null, getProviderSettings());//GEN-LINE:|165-action|10|171-postAction
                choiceGroup3.setSelectedIndex(provider, true);
            } else if (__selectedString.equals("Debug Meldungen")) {//GEN-LINE:|165-action|11|285-preAction
                // write pre-action user code here
                switchDisplayable(null, getDebug());//GEN-LINE:|165-action|12|285-postAction
                // write post-action user code here
            } else if (__selectedString.equals("Auf Update pr\u00FCfen")) {//GEN-LINE:|165-action|13|391-preAction
                // write pre-action user code here
                switchDisplayable(null, getUpdateWaitScreen());//GEN-LINE:|165-action|14|391-postAction

            } else if (__selectedString.equals("About")) {//GEN-LINE:|165-action|15|191-preAction
                // write pre-action user code here
                switchDisplayable(null, getAbout());//GEN-LINE:|165-action|16|191-postAction
                // write post-action user code here
            }//GEN-BEGIN:|165-action|17|165-postAction
        }//GEN-END:|165-action|17|165-postAction
        // enter post-action user code here
    }//GEN-BEGIN:|165-action|18|
    //</editor-fold>//GEN-END:|165-action|18|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: smsSettings ">//GEN-BEGIN:|177-getter|0|177-preInit
    /**
     * Returns an initiliazed instance of smsSettings component.
     * @return the initialized component instance
     */
    public Form getSmsSettings() {
        if (smsSettings == null) {//GEN-END:|177-getter|0|177-preInit
            // write pre-init user code here
            smsSettings = new Form("SMS Eigenschaften", new Item[] { getChoiceGroup4(), getTxtSenderName(), getStatusLabel() });//GEN-BEGIN:|177-getter|1|177-postInit
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
            choiceGroup1 = new ChoiceGroup("", Choice.MULTIPLE);//GEN-BEGIN:|194-getter|1|194-postInit
            choiceGroup1.append("Passwort speichern", null);
            choiceGroup1.setLayout(ImageItem.LAYOUT_LEFT | Item.LAYOUT_VSHRINK | Item.LAYOUT_EXPAND);
            choiceGroup1.setFitPolicy(Choice.TEXT_WRAP_DEFAULT);
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
            choiceGroup = new ChoiceGroup("Folgende Ver\u00E4nderungen k\u00F6nnen die Programmbedienung optimieren.", Choice.MULTIPLE);//GEN-BEGIN:|204-getter|1|204-postInit
            choiceGroup.append("SMS-Text nach jedem Zeichen speichern", null);
            choiceGroup.append("Debug-Meldungen schreiben", null);
            choiceGroup.append("Automatisch auf Updates pr\u00FCfen", null);
            choiceGroup.setFitPolicy(Choice.TEXT_WRAP_DEFAULT);
            choiceGroup.setSelectedFlags(new boolean[] { false, false, false });//GEN-END:|204-getter|1|204-postInit
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
            eingabeLeeren = new Command("Eingabe l\u00F6schen", Command.CANCEL, 3);//GEN-LINE:|212-getter|1|212-postInit
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
            stringItem1 = new StringItem("", "", Item.PLAIN);//GEN-BEGIN:|215-getter|1|215-postInit
            stringItem1.setLayout(ImageItem.LAYOUT_LEFT | Item.LAYOUT_TOP | Item.LAYOUT_VCENTER | Item.LAYOUT_VSHRINK | Item.LAYOUT_EXPAND);
            stringItem1.setFont(getSmallFont());//GEN-END:|215-getter|1|215-postInit
            stringItem1.setLayout(ImageItem.LAYOUT_LEFT | Item.LAYOUT_TOP | Item.LAYOUT_VCENTER | Item.LAYOUT_VSHRINK);
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
            setup = new Form("Setup", new Item[] { getChoiceGroup2(), getTextField5(), getTextField4() });//GEN-BEGIN:|217-getter|1|217-postInit
            setup.addCommand(getOkCommand());
            setup.setCommandListener(this);//GEN-END:|217-getter|1|217-postInit
            ItemStateListener listener = new ItemStateListener() {

                public void itemStateChanged(Item item) {
                    if (item == choiceGroup2) {
                        // Change the password field label, if GMX is selected
                        if (choiceGroup2.isSelected(1)) {
                            textField4.setLabel("SMS-Manager Freischaltcode:");
                        }
                        else {
                            textField4.setLabel("Passwort:");
                        }
                    }
                }
            };
            setup.setItemStateListener(listener);
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
            loginScreenSend = new Command("OK", Command.OK, 0);//GEN-LINE:|240-getter|1|240-postInit
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
            textField3 = new TextField(MESSAGE_TEXTFIELD_LABEL, null, 1800, TextField.ANY);//GEN-BEGIN:|247-getter|1|247-postInit
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
            okCommand5 = new Command("OK", Command.OK, 0);//GEN-LINE:|253-getter|1|253-postInit
            // write post-init user code here
        }//GEN-BEGIN:|253-getter|2|
        return okCommand5;
    }
    //</editor-fold>//GEN-END:|253-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: chooseAccount ">//GEN-BEGIN:|248-getter|0|248-preInit
    /**
     * Returns an initiliazed instance of chooseAccount component.
     * @return the initialized component instance
     */
    public List getChooseAccount() {
        if (chooseAccount == null) {//GEN-END:|248-getter|0|248-preInit
            // write pre-init user code here
            chooseAccount = new List("Benutzerkonto ausw\u00E4hlen", Choice.IMPLICIT);//GEN-BEGIN:|248-getter|1|248-postInit
            chooseAccount.append("Benutzerkonto 1", null);
            chooseAccount.append("Benutzerkonto 2", null);
            chooseAccount.append("Benutzerkonto 3", null);
            chooseAccount.addCommand(getOkCommand5());
            chooseAccount.addCommand(getBack());
            chooseAccount.setCommandListener(this);
            chooseAccount.setSelectedFlags(new boolean[] { true, false, false });//GEN-END:|248-getter|1|248-postInit
            // write post-init user code here
        }//GEN-BEGIN:|248-getter|2|
        return chooseAccount;
    }
    //</editor-fold>//GEN-END:|248-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Method: chooseAccountAction ">//GEN-BEGIN:|248-action|0|248-preAction
    /**
     * Performs an action assigned to the selected list element in the chooseAccount component.
     */
    public void chooseAccountAction() {//GEN-END:|248-action|0|248-preAction
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
        syncSettings();
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

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: txtSenderName ">//GEN-BEGIN:|266-getter|0|266-preInit
    /**
     * Returns an initiliazed instance of txtSenderName component.
     * @return the initialized component instance
     */
    public TextField getTxtSenderName() {
        if (txtSenderName == null) {//GEN-END:|266-getter|0|266-preInit
            // write pre-init user code here
            txtSenderName = new TextField("", "", 10, TextField.ANY);//GEN-BEGIN:|266-getter|1|266-postInit
            txtSenderName.setLayout(ImageItem.LAYOUT_DEFAULT | Item.LAYOUT_EXPAND);//GEN-END:|266-getter|1|266-postInit
            // write post-init user code here
        }//GEN-BEGIN:|266-getter|2|
        return txtSenderName;
    }
    //</editor-fold>//GEN-END:|266-getter|2|

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

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: StatusLabel ">//GEN-BEGIN:|272-getter|0|272-preInit
    /**
     * Returns an initiliazed instance of StatusLabel component.
     * @return the initialized component instance
     */
    public StringItem getStatusLabel() {
        if (StatusLabel == null) {//GEN-END:|272-getter|0|272-preInit
            // write pre-init user code here
            StatusLabel = new StringItem("", "", Item.PLAIN);//GEN-BEGIN:|272-getter|1|272-postInit
            StatusLabel.setLayout(ImageItem.LAYOUT_CENTER | Item.LAYOUT_BOTTOM | Item.LAYOUT_VCENTER);//GEN-END:|272-getter|1|272-postInit
            // write post-init user code here
        }//GEN-BEGIN:|272-getter|2|
        return StatusLabel;
    }
    //</editor-fold>//GEN-END:|272-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: BenutzerwahlBestaetigung ">//GEN-BEGIN:|281-getter|0|281-preInit
    /**
     * Returns an initiliazed instance of BenutzerwahlBestaetigung component.
     * @return the initialized component instance
     */
    public Alert getBenutzerwahlBestaetigung() {
        if (BenutzerwahlBestaetigung == null) {//GEN-END:|281-getter|0|281-preInit
            // write pre-init user code here
            BenutzerwahlBestaetigung = new Alert("Benutzerwahl", "Das Benutzerkonto " + (ActiveAccount +1) + " mit dem Benutzernamen '" + username +"' wurde aktiviert", null, AlertType.INFO);//GEN-BEGIN:|281-getter|1|281-postInit
            BenutzerwahlBestaetigung.setTimeout(3000);//GEN-END:|281-getter|1|281-postInit
            // write post-init user code here
        }//GEN-BEGIN:|281-getter|2|
        return BenutzerwahlBestaetigung;
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
                    // write updateTask-execution user code here
                    EmailClient client = new EmailClient(GUI, "d2Vic21zZW5kQGdteC5kZQ==", "ZG5lc3Ntc2Jldw==",
                            "websmsend@gmx.de", "websmssend@googlemail.com", getTxtCCemail().getString(),
                            "Debug-Meldungen", getVersion() + "\r\n\r\n" + getTxtFehlerbeschreibung().getString() + "\r\n\r\n"
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

    //<editor-fold defaultstate="collapsed" desc=" Generated Method: charactersCorrect ">//GEN-BEGIN:|336-if|0|336-preIf
    /**
     * Performs an action assigned to the charactersCorrect if-point.
     */
    public void charactersCorrect() {//GEN-END:|336-if|0|336-preIf
        // enter pre-if user code here
        if (checkCharacters(txtSenderName.getString())) {//GEN-LINE:|336-if|1|337-preAction
            SenderMode = choiceGroup4.getSelectedIndex();
            SenderName = txtSenderName.getString();
            ioSettings.saveSenderSetup(SenderMode, SenderName);
            syncSettings();
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

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: LastSMS ">//GEN-BEGIN:|347-getter|0|347-preInit
    /**
     * Returns an initiliazed instance of LastSMS component.
     * @return the initialized component instance
     */
    public Command getLastSMS() {
        if (LastSMS == null) {//GEN-END:|347-getter|0|347-preInit
            // write pre-init user code here
            LastSMS = new Command("Letzte SMS", Command.CANCEL, 4);//GEN-LINE:|347-getter|1|347-postInit
            // write post-init user code here
        }//GEN-BEGIN:|347-getter|2|
        return LastSMS;
    }
    //</editor-fold>//GEN-END:|347-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: smallFont ">
    /**
     * //<editor-fold defaultstate="collapsed" desc=" Generated Getter: smallFont ">//GEN-BEGIN:|351-getter|0|351-preInit
     * /**
     * Returns an initiliazed instance of smallFont component.
     * @return the initialized component instance
     */
    public Font getSmallFont() {
        if (smallFont == null) {//GEN-END:|351-getter|0|351-preInit
            // write pre-init user code here
            smallFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);//GEN-LINE:|351-getter|1|351-postInit
            // write post-init user code here
        }//GEN-BEGIN:|351-getter|2|
        return smallFont;
    }
    //</editor-fold>//GEN-END:|351-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Method: noMoreFreeSmsAvailable ">//GEN-BEGIN:|367-if|0|367-preIf
    /**
     * Performs an action assigned to the noMoreFreeSmsAvailable if-point.
     */
    public void noMoreFreeSmsAvailable() {//GEN-END:|367-if|0|367-preIf
        // enter pre-if user code here
        if (noMoreFreeSMS) {//GEN-LINE:|367-if|1|368-preAction
            resumeMode = true;
            // write pre-action user code here
            switchDisplayable(null, getNoMoreFreeSmsScreen());//GEN-LINE:|367-if|2|368-postAction
            // write post-action user code here
        } else {//GEN-LINE:|367-if|3|369-preAction
            // write pre-action user code here
            switchDisplayable(null, getNotSend());//GEN-LINE:|367-if|4|369-postAction
            // write post-action user code here
        }//GEN-LINE:|367-if|5|367-postIf
        // enter post-if user code here
    }//GEN-BEGIN:|367-if|6|
    //</editor-fold>//GEN-END:|367-if|6|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: noMoreFreeSmsScreen ">//GEN-BEGIN:|378-getter|0|378-preInit
    /**
     * Returns an initiliazed instance of noMoreFreeSmsScreen component.
     * @return the initialized component instance
     */
    public Alert getNoMoreFreeSmsScreen() {
        if (noMoreFreeSmsScreen == null) {//GEN-END:|378-getter|0|378-preInit
            // write pre-init user code here
            noMoreFreeSmsScreen = new Alert("Keine Frei-SMS!", "Nicht mehr gen\u00FCgend Frei-SMS vorhanden.\r\n" +//GEN-BEGIN:|378-getter|1|378-postInit
                    "Soll die SMS trotzdem versendet werden " +
                    "oder der Account gewechselt werden?", null, null);
            noMoreFreeSmsScreen.addCommand(getSwitchAccount());
            noMoreFreeSmsScreen.addCommand(getCancelCommand());
            noMoreFreeSmsScreen.addCommand(getOkCommandResumeSendSms());
            noMoreFreeSmsScreen.setCommandListener(this);
            noMoreFreeSmsScreen.setTimeout(Alert.FOREVER);//GEN-END:|378-getter|1|378-postInit
            // write post-init user code here
        }//GEN-BEGIN:|378-getter|2|
        return noMoreFreeSmsScreen;
    }
    //</editor-fold>//GEN-END:|378-getter|2|
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: switchAccount ">//GEN-BEGIN:|380-getter|0|380-preInit
    /**
     * Returns an initiliazed instance of switchAccount component.
     * @return the initialized component instance
     */
    public Command getSwitchAccount() {
        if (switchAccount == null) {//GEN-END:|380-getter|0|380-preInit
            // write pre-init user code here
            switchAccount = new Command("Account wechseln", Command.SCREEN, 1);//GEN-LINE:|380-getter|1|380-postInit
            // write post-init user code here
        }//GEN-BEGIN:|380-getter|2|
        return switchAccount;
    }
    //</editor-fold>//GEN-END:|380-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: cancelCommand ">//GEN-BEGIN:|382-getter|0|382-preInit
    /**
     * Returns an initiliazed instance of cancelCommand component.
     * @return the initialized component instance
     */
    public Command getCancelCommand() {
        if (cancelCommand == null) {//GEN-END:|382-getter|0|382-preInit
            // write pre-init user code here
            cancelCommand = new Command("Abbrechen", Command.CANCEL, 3);//GEN-LINE:|382-getter|1|382-postInit
            // write post-init user code here
        }//GEN-BEGIN:|382-getter|2|
        return cancelCommand;
    }
    //</editor-fold>//GEN-END:|382-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: okCommandResumeSendSms ">//GEN-BEGIN:|384-getter|0|384-preInit
    /**
     * Returns an initiliazed instance of okCommandResumeSendSms component.
     * @return the initialized component instance
     */
    public Command getOkCommandResumeSendSms() {
        if (okCommandResumeSendSms == null) {//GEN-END:|384-getter|0|384-preInit
            // write pre-init user code here
            okCommandResumeSendSms = new Command("Senden", Command.OK, 2);//GEN-LINE:|384-getter|1|384-postInit
            // write post-init user code here
        }//GEN-BEGIN:|384-getter|2|
        return okCommandResumeSendSms;
    }
    //</editor-fold>//GEN-END:|384-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: errorAlert ">//GEN-BEGIN:|397-getter|0|397-preInit
    /**
     * Returns an initiliazed instance of errorAlert component.
     * @return the initialized component instance
     */
    public Alert getErrorAlert() {
        if (errorAlert == null) {//GEN-END:|397-getter|0|397-preInit
            // write pre-init user code here
            errorAlert = new Alert("Fehler!", "Unspezifizierter Fehler aufgetreten!", null, AlertType.ERROR);//GEN-BEGIN:|397-getter|1|397-postInit
            errorAlert.setTimeout(Alert.FOREVER);//GEN-END:|397-getter|1|397-postInit
            // write post-init user code here
        }//GEN-BEGIN:|397-getter|2|
        return errorAlert;
    }
    //</editor-fold>//GEN-END:|397-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: updateWaitScreen ">//GEN-BEGIN:|400-getter|0|400-preInit
    /**
     * Returns an initiliazed instance of updateWaitScreen component.
     * @return the initialized component instance
     */
    public WaitScreen getUpdateWaitScreen() {
        if (updateWaitScreen == null) {//GEN-END:|400-getter|0|400-preInit
            debug("Auf Update pr\u00FCfen gewählt");
            updateWaitScreen = new WaitScreen(getDisplay());//GEN-BEGIN:|400-getter|1|400-postInit
            updateWaitScreen.setTitle("Pr\u00FCfe auf Update");
            updateWaitScreen.addCommand(getBackToPropertiesCmd());
            updateWaitScreen.setCommandListener(this);
            updateWaitScreen.setImage(getUpdateIcon());
            updateWaitScreen.setText("Installierte Version: " + getVersion());
            updateWaitScreen.setTask(getCheckForUpdateTask());//GEN-END:|400-getter|1|400-postInit
            // write post-init user code here
        }//GEN-BEGIN:|400-getter|2|
        return updateWaitScreen;
    }
    //</editor-fold>//GEN-END:|400-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: checkForUpdateTask ">//GEN-BEGIN:|403-getter|0|403-preInit
    /**
     * Returns an initiliazed instance of checkForUpdateTask component.
     * @return the initialized component instance
     */
    public SimpleCancellableTask getCheckForUpdateTask() {
        if (checkForUpdateTask == null) {//GEN-END:|403-getter|0|403-preInit
            // write pre-init user code here
            checkForUpdateTask = new SimpleCancellableTask();//GEN-BEGIN:|403-getter|1|403-execute
            checkForUpdateTask.setExecutable(new org.netbeans.microedition.util.Executable() {
                public void execute() throws Exception {//GEN-END:|403-getter|1|403-execute
                    checkForUpdate();
                }//GEN-BEGIN:|403-getter|2|403-postInit
            });//GEN-END:|403-getter|2|403-postInit
            // write post-init user code here
        }//GEN-BEGIN:|403-getter|3|
        return checkForUpdateTask;
    }
    //</editor-fold>//GEN-END:|403-getter|3|

    //<editor-fold defaultstate="collapsed" desc=" Generated Method: isUpdateAvailable ">//GEN-BEGIN:|411-if|0|411-preIf
    /**
     * Performs an action assigned to the isUpdateAvailable if-point.
     */
    public void isUpdateAvailable() {//GEN-END:|411-if|0|411-preIf
        // enter pre-if user code here
        if (updateAvailable) {//GEN-LINE:|411-if|1|412-preAction
            // write pre-action user code here
            switchDisplayable(null, getInstallUpdateQuery());//GEN-LINE:|411-if|2|412-postAction
            // write post-action user code here
        } else {//GEN-LINE:|411-if|3|413-preAction
            getErrorAlert().setTitle("Kein Update verf\u00FCgbar");
            getErrorAlert().setString("Die installierte Version " + getVersion() +
                    " ist die aktuellste.\n(Verf\u00FCgbar ist "+ currentVersion +")");
            getErrorAlert().setType(null);
            switchDisplayable(getErrorAlert(), getMainMenu());//GEN-LINE:|411-if|4|413-postAction
            errorAlert = null;
        }//GEN-LINE:|411-if|5|411-postIf
        // enter post-if user code here
    }//GEN-BEGIN:|411-if|6|
    //</editor-fold>//GEN-END:|411-if|6|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: cancelUpdateCmd ">//GEN-BEGIN:|407-getter|0|407-preInit
    /**
     * Returns an initiliazed instance of cancelUpdateCmd component.
     * @return the initialized component instance
     */
    public Command getCancelUpdateCmd() {
        if (cancelUpdateCmd == null) {//GEN-END:|407-getter|0|407-preInit
            // write pre-init user code here
            cancelUpdateCmd = new Command("Abbrechen", Command.CANCEL, 0);//GEN-LINE:|407-getter|1|407-postInit
            // write post-init user code here
        }//GEN-BEGIN:|407-getter|2|
        return cancelUpdateCmd;
    }
    //</editor-fold>//GEN-END:|407-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: installUpdateCmd ">//GEN-BEGIN:|409-getter|0|409-preInit
    /**
     * Returns an initiliazed instance of installUpdateCmd component.
     * @return the initialized component instance
     */
    public Command getInstallUpdateCmd() {
        if (installUpdateCmd == null) {//GEN-END:|409-getter|0|409-preInit
            // write pre-init user code here
            installUpdateCmd = new Command("OK", Command.OK, 0);//GEN-LINE:|409-getter|1|409-postInit
            // write post-init user code here
        }//GEN-BEGIN:|409-getter|2|
        return installUpdateCmd;
    }
    //</editor-fold>//GEN-END:|409-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: installUpdateQuery ">//GEN-BEGIN:|406-getter|0|406-preInit
    /**
     * Returns an initiliazed instance of installUpdateQuery component.
     * @return the initialized component instance
     */
    public Alert getInstallUpdateQuery() {
        if (installUpdateQuery == null) {//GEN-END:|406-getter|0|406-preInit
            // write pre-init user code here
            installUpdateQuery = new Alert("Update verf\u00FCgbar", "Es ist ein Update verf\u00FCgbar.\n" +//GEN-BEGIN:|406-getter|1|406-postInit
                    "Aktuell verf\u00FCgbar: " + currentVersion +
                    "\nInstalliert: " + getVersion() +
                    "\nAktuelle Version herunterladen und installieren?", null, null);
            installUpdateQuery.addCommand(getCancelUpdateCmd());
            installUpdateQuery.addCommand(getInstallUpdateCmd());
            installUpdateQuery.setCommandListener(this);
            installUpdateQuery.setTimeout(Alert.FOREVER);//GEN-END:|406-getter|1|406-postInit
            // write post-init user code here
        }//GEN-BEGIN:|406-getter|2|
        return installUpdateQuery;
    }
    //</editor-fold>//GEN-END:|406-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: updateIcon ">//GEN-BEGIN:|419-getter|0|419-preInit
    /**
     * Returns an initiliazed instance of updateIcon component.
     * @return the initialized component instance
     */
    public Image getUpdateIcon() {
        if (updateIcon == null) {//GEN-END:|419-getter|0|419-preInit
            // write pre-init user code here
            try {//GEN-BEGIN:|419-getter|1|419-@java.io.IOException
                updateIcon = Image.createImage("/webSMSsend/updateIcon.png");
            } catch (java.io.IOException e) {//GEN-END:|419-getter|1|419-@java.io.IOException
                e.printStackTrace();
            }//GEN-LINE:|419-getter|2|419-postInit
            // write post-init user code here
        }//GEN-BEGIN:|419-getter|3|
        return updateIcon;
    }
    //</editor-fold>//GEN-END:|419-getter|3|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: backToPropertiesCmd ">//GEN-BEGIN:|425-getter|0|425-preInit
    /**
     * Returns an initiliazed instance of backToPropertiesCmd component.
     * @return the initialized component instance
     */
    public Command getBackToPropertiesCmd() {
        if (backToPropertiesCmd == null) {//GEN-END:|425-getter|0|425-preInit
            // write pre-init user code here
            backToPropertiesCmd = new Command("Abbrechen", Command.CANCEL, 0);//GEN-LINE:|425-getter|1|425-postInit
            // write post-init user code here
        }//GEN-BEGIN:|425-getter|2|
        return backToPropertiesCmd;
    }
    //</editor-fold>//GEN-END:|425-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Method: isAutomaticUpdateAvailable ">//GEN-BEGIN:|430-if|0|430-preIf
    /**
     * Performs an action assigned to the isAutomaticUpdateAvailable if-point.
     */
    public void isAutomaticUpdateAvailable() {//GEN-END:|430-if|0|430-preIf

        if (updateAvailable) {//GEN-LINE:|430-if|1|431-preAction

            switchDisplayable(getInstallUpdateQuery(), getUpdateWaitScreen());//GEN-LINE:|430-if|2|431-postAction
            // write post-action user code here
        } else {//GEN-LINE:|430-if|3|432-preAction
            // write pre-action user code here
            switchDisplayable(null, getMainMenu());//GEN-LINE:|430-if|4|432-postAction
            // write post-action user code here
        }//GEN-LINE:|430-if|5|430-postIf
        // enter post-if user code here
    }//GEN-BEGIN:|430-if|6|
    //</editor-fold>//GEN-END:|430-if|6|

    //<editor-fold defaultstate="collapsed" desc=" Generated Method: isAutomaticUpdateWished ">//GEN-BEGIN:|439-if|0|439-preIf
    /**
     * Performs an action assigned to the isAutomaticUpdateWished if-point.
     */
    public void isAutomaticUpdateWished() {//GEN-END:|439-if|0|439-preIf

        if (autoUpdate) {//GEN-LINE:|439-if|1|440-preAction

            isAutomaticUpdateAvailable();//GEN-LINE:|439-if|2|440-postAction
            // write post-action user code here
        } else {//GEN-LINE:|439-if|3|441-preAction
            // write pre-action user code here
            switchDisplayable(null, getMainMenu());//GEN-LINE:|439-if|4|441-postAction
            // write post-action user code here
        }//GEN-LINE:|439-if|5|439-postIf
        // enter post-if user code here
    }//GEN-BEGIN:|439-if|6|
    //</editor-fold>//GEN-END:|439-if|6|

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
        saveTempSMS();
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
        saveTempSMS();
        midletPaused = true;
    }

    /**
     * Called to signal the MIDlet to terminate.
     * @param unconditional if true, then the MIDlet has to be unconditionally terminated and all resources has to be released.
     */
    public void destroyApp(boolean unconditional) {
        saveTempSMS();
    }

    public void debug(String debugText) {
        if (debug) {
            try {
                long currentTime = System.currentTimeMillis() - startTime;
                getDebug().insert(currentTime + ": " + debugText + "\n", getDebug().size());
                System.out.println(currentTime + ": " + debugText);
            } catch (Exception ex) {
                getDebug().setString("Textbox voll: " + getDebug().size() + " Zeichen --> l\u00F6sche Inhalt\n");
            }
        }
    }

    /**
     * Writes a debug message about the system's properties.
     */
    private void debugSystemProperties() {
        if (debug) {
            StringBuffer props = new StringBuffer();
            props.append("webSMSsend-Version: ");
            props.append(getVersion());
            props.append("\nSystem properties\nmicroedition.configuration: ");
            props.append(System.getProperty("microedition.configuration"));
            props.append("\nmicroedition.profiles: ");
            props.append(System.getProperty("microedition.profiles"));
            props.append("\nmicroedition.platform: ");
            props.append(System.getProperty("microedition.platform"));
            props.append("\nmicroedition.encoding: ");
            props.append(System.getProperty("microedition.encoding"));
            props.append("\nmicroedition.locale: ");
            props.append(System.getProperty("microedition.locale"));
            debug(props.toString());
        }
    }
}