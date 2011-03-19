/*
 *
 *
    Copyright 2011 schirinowski@gmail.com
    This file is part of WebSMSsend.
    The following code is based on parts of the LessIsMore package by fkoester.
    For details on LessIsMore see <http://wiki.evolvis.org/lessismore/index.php/LessIsMore>
    and <https://evolvis.org/scm/viewvc.php/lessismore/LessIsMore-MIDlet-Prototype/trunk/src/org/evolvis/lessismore/>

    WebSMSsend is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    WebSMSsend is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with WebSMSsend.  If not, see <http://www.gnu.org/licenses/>.

 *
 *
 */

package GMX;

import ConnectorBase.SmsConnector;
import ConnectorBase.Properties;
import ConnectorBase.SmsData;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

/**
 * This class implements the {@link ConnectorBase.ISmsConnector communication
 * interface} to the GMX SMS service.
 * 
 * @author Copyright 2011 schirinowski@gmail.com
 * @version $Revision$
 */
public class GMX extends SmsConnector {
    /**
     * Stores the SMS server's URL
     * ({@code http://app3.wr-gmbh.de/WRServer/WRServer.dll/WR}) to connect to.
     * Default GMX SMS server is app5, according to some internet forum entries,
     * the default recovery procedure is app{0,...,4,6,7} if app5 is not
     * available. As app5 has a questionable performance in terms of the delay
     * between receiving the SMS to be sent and the time it is actually sent,
     * app5 is not the default server of webSMSsend
     */
    private static final String WEBSERVICE_URL = "http://app3.wr-gmbh.de/WRServer/WRServer.dll/WR";

    /**
     * Maximum SMS length according to http://www.gmx.net/gmx-sms, which is 760.
     */
    private static final int MAX_SMS_LENGTH = 760;

    /**
     * GMX internal customer representation. Its value is received from the GMX
     * server after the user has logged into it.
     */
    String customerID;
    /**
     * Mobile phone number stored in GMX database belonging to the user logged
     * in. Its value is received from the GMX server after the user has logged
     * into it.
     */
    String senderPhoneNumber;
    /** Stores if the send process was canceled previously due to no free SMS */
    boolean resumingSendProcess = false;
    /** Stores the SMS to be sent using a different account */
    SmsData resumeSMS = null;

    /**
     * The constructor initializes the connector's properties. Until now, the
     * GMX connector has the {@link Properties#CAN_SIMULATE_SEND_PROCESS
     * CAN_SIMULATE_SEND_PROCESS} as well as the {@link Properties#CAN_ABORT_SEND_PROCESS_WHEN_NO_FREE_SMS_AVAILABLE
     * CAN_ABORT_SEND_PROCESS_WHEN_NO_FREE_SMS_AVAILABLE} property.
     */
    public GMX(){
        specs.AddProperty(new int[]{Properties.CAN_SIMULATE_SEND_PROCESS,
            Properties.CAN_ABORT_SEND_PROCESS_WHEN_NO_FREE_SMS_AVAILABLE});
    }

    /**
     * Returns the current connector's name.
     * @return A {@link String} containing the connector's name ({@code GMX}).
     */
    public String getName() {
        return "GMX";
    }

    /**
     * Returns the label which describes the password's meaning which must be
     * entered for this connector. This varies according to the used 
     * {@link SmsConnector}.
     * @return a {@link String} containing the password's meaning. In
     *         the case of GMX, it is the ({@code SMS-Manager Freischaltcode}).
     */
    public String getPasswordFieldLabel() {
        return "SMS-Manager Freischaltcode:";
    }

    /**
     * Returns the description how many free SMS are left with respect to the
     * active account.
     * @return a {@link String} showing the number of left GMX free SMS
     *         and the maximum number of free SMS in the current month.
     */
    public String getRemSmsText() {
        StringBuffer text = new StringBuffer("Verbleibende Frei-SMS: ");

        if (getRemainingSMS() == -1 ) {
            text.append("?");
        } else {
            text.append(remsms);
        }

        if (this.getMaxFreeSMS() == -1 ) {
            text.append("/?");
        } else {
            text.append("/").append(maxfreesms);
        }
        return text.toString();
    }

    /**
     * Returns the maximum number of chars an SMS may contain.
     * @return an integer representing the maximum number of chars an SMS may
     *         contain when using GMX.
     */
    public int getMaxSMSLength() {
        return MAX_SMS_LENGTH;
    }

    /**
     * This method computes the number of SMS the provided text will be
     * resulting in.
     * @param smsText the SMS text to be sent.
     * @return the number of SMS the sent text will result in.
     */
    public int countSms(String smsText) {
        // Taken from the web SMS manager:
        // (^ { } \ [ ~ ] | € are special characters and count twice; if they are
        //  at the last position of a sms part, they have to be moved into the
        //  next SMS, since we don't want such a character to be split over 2
        //  message parts)

        // Count the special characters
        char[] specialChars = "^{}\\[~]|€".toCharArray();
        int addChars = 0;
        for (int i = 0; i < specialChars.length; i++) {
            int start = smsText.indexOf(specialChars[i]);
            while (start != -1) {
                addChars++;
                start = smsText.indexOf(specialChars[i], ++start);
            }
        }

        int completeLength = smsText.length() + addChars;
        if (completeLength > 160) {
            return (int) Math.floor((completeLength + 151) / 152);
        } else if (completeLength == 0) {
            return 0;
        } else {
            return 1;
        }
    }

    public boolean resumeSending() throws Exception {
        gui.debug("Wiederaufnahme des Sendeprozesses");
        resumingSendProcess = true;
        send(resumeSMS);
        return true;
    }

    /**
     * This method handles the SMS sending process and transmits the SMS to be
     * sent to the {@link #WEBSERVICE_URL GMX SMS server} for further
     * processing. It first logs the user in by which it also receives the
     * number of remaining free SMS. Afterwards, the SMS to be sent is fit into
     * the protocol message and transmitted to the GMX server which confirms the
     * SMS transmission.
     * @param sms contains all relevant information which are necessary for
     *        sending an SMS, e. g. the SMS' text, recipient's phone number,
     *        etc.
     * @throws Exception
     */
     public int send(SmsData sms) throws Exception {
        try {
            gui = sms.getGui();
            gui.debug("Starte " + getClass().getName() + ".send()"
                    + (sms.isSimulation() ? " SIMULATION!" : ""));
            long totaltime = System.currentTimeMillis();

            if (sms.getSmsRecv().equals("")) {
                throw new Exception("Kein Empf\u00E4nger angegeben");
            }

            if (sms.getSmsText().equals("")) {
                throw new Exception("Kein SMS-Text angegeben");
            }

            if (sms.getSmsText().length() > getMaxSMSLength()) {
                throw new Exception("SMS-Text zu lang (max. 760 Zeichen)");
            }

            String smsRecv = checkRecv(sms.getSmsRecv());

            //#if Test
//#             // Output only on developer site, message contains sensitive data
//#             gui.debug("Empf\u00E4nger-Handynummer: " + smsRecv);
            //#else
            gui.debug("Empf\u00E4nger-Handynummer: " + smsRecv.substring(0, 3) + "*******");
            //#endif
            gui.debug("Webservice-URL: " + WEBSERVICE_URL);
            
            Hashtable params = new Hashtable();
            params.put("email_address", sms.getUsername());
            params.put("password", sms.getPassword());

            gui.setWaitScreenText("Login...");
            Hashtable result = sendPackage("GET_CUSTOMER", "1.10", params, true);

            if(result == null)
                throw new Exception("Login-Fehler");

            Object returnCode = result.get("rslt");

            if(returnCode == null)
                throw new RuntimeException("Konnte Server-Antwort nicht lesen");

            if(returnCode.equals("0")) {
                gui.setWaitScreenText("Login erfolgreich");
            }
            else if(returnCode.equals("25")) {
                throw new Exception("E-Mail/Freischaltcode falsch");
            }
            else {
                gui.setWaitScreenText("Unbekannte Serverantwort: " + returnCode);
            }

            int appliedForDebit = Integer.parseInt(result.get("gmx_ls_flag").toString());
            if(appliedForDebit != 10) {
                throw new Exception("Nicht zum Lastschriftverfahren bei GMX angemeldet");
            }

            int senderPhoneNumberConfirmed = Integer.parseInt(result.get("cell_phone_confirmed").toString());
            if(senderPhoneNumberConfirmed != 1) {
                throw new Exception("Handynummer im GMX SMS-Manager nicht best\u00E4tigt");
            }
            
            Object customerIDObj = result.get("customer_id");
            Object senderPhoneNumberObj = result.get("cell_phone");
            // Maximum number of free SMS a user can send
            String freeMaxMonth = result.get("free_max_month").toString();
            // Number of free SMS remaining the current month
            String freeRemainingMonth = result.get("free_rem_month").toString();
            // Maximum number of free SMS a user can send the current day
            String freeMaxDay = result.get("free_max_day").toString();
            // Number of free SMS remaining the current day
            String freeRemainingDay = result.get("free_rem_day").toString();

            gui.setWaitScreenText("Senden wird vorbereitet...");
            //#if Test
//#             // Output only on developer site, message contains sensitive data
//#             gui.debug("Kundennummer: " + customerIDObj);
//#             gui.debug("Absender-Handynummer: " + senderPhoneNumberObj);
            //#endif
            
            if(senderPhoneNumberObj == null || freeMaxMonth == null || freeRemainingMonth == null)
                throw new Exception("Fehler beim Lesen der Serverantwort");

            maxfreesms = Integer.parseInt(freeMaxMonth);
            int remSMS = Integer.parseInt(freeRemainingMonth);
            int maxDay = Integer.parseInt(freeMaxDay);
            int remSMStoday = Integer.parseInt(freeRemainingDay);

            // Counting amount of used SMS
            int SMSneeded = countSms(sms.getSmsText());
            gui.debug("Die SMS ist Zeichen lang: " + sms.getSmsText().length());
            gui.debug("Anzahl SMS: " + SMSneeded);
            remSMS -= SMSneeded;
            remSMStoday -= SMSneeded;

            // Determine if the user has free SMS left in this month and if
            // there is any limitation of free SMS for one day
            boolean freeSmsLeft = maxfreesms > 0 && remSMS >= 0;
            boolean unlimitedDailyFreeSms = maxDay == 0 || (maxDay > 0 && remSMStoday >= 0);
            // Cancel send process if no free SMS would be available and the send
            // process was not canceled before
            if ((!freeSmsLeft || !unlimitedDailyFreeSms)
                    && !resumingSendProcess) {
                resumeSMS = sms;
                return NO_MORE_FREE_SMS;
            } else {
                resumingSendProcess = false;
            }

            senderPhoneNumber = senderPhoneNumberObj.toString();
            customerID = customerIDObj.toString();

            if(customerID == null)
                throw new Exception("Keine Kundennummer empfangen");

            if(senderPhoneNumber == null)
                throw new Exception("Keine Absender-Handynummer empfangen");

            params.put("customer_id", customerID);
            params.put("receivers", "\\<TBL ROWS=\"1\" COLS=\"3\"\\>receiver_id\\\\;receiver_name\\\\;receiver_number\\\\;1\\\\;Bla\\\\;" + smsRecv + "\\\\;\\</TBL\\>");
            params.put("sms_text", maskSpecialChars(sms.getSmsText()));
            params.put("send_option", "sms");
            params.put("sms_sender", senderPhoneNumber);

            if (!sms.isSimulation()) {
                gui.setWaitScreenText("SMS wird gesendet...");
                result = sendPackage("SEND_SMS", "1.01", params, false);

                Object sendReturnCode = result.get("rslt");

                if(sendReturnCode == null)
                    throw new RuntimeException("Konnte Server-Antwort nicht lesen");

                if(sendReturnCode.equals("0")) {
                    gui.setWaitScreenText("SMS wurde gesendet");
                }
                else {
                    gui.setWaitScreenText("Unbekannte Serverantwort: " + returnCode);
                }

                // Determine remaining SMS this month
                try {
                    remSMS = Integer.parseInt(result.get("free_rem_month").toString());
                } catch (NumberFormatException ne) {
                    gui.debug("Konnte verbleibende SMS nicht bestimmen: "
                            + ne.getMessage()
                            + "\nNutze alte Methode");
                    // Check if there are free SMS left
                    if (remSMS - SMSneeded > 0) {
                        remSMS = remSMS - SMSneeded;
                    }
                    else {
                        remSMS = 0;
                    }
                }
            }
            
            // Get remaining free SMS this month
            remsms = remSMS;
            // Determine maximum possible free SMS this month
            freeMaxMonth = result.get("free_max_month").toString();
            maxfreesms = Integer.parseInt(freeMaxMonth.toString());

            saveItem(REMAINING_SMS_FIELD, remsms+"");
            saveItem(MAX_FREE_SMS, maxfreesms+"");

            gui.debug("Fertig mit " + getClass().getName()
                    + ".Send(), Dauer: "
                    + (System.currentTimeMillis() - totaltime) + " ms");
            return SMS_SENT;
        } catch (OutOfMemoryError ex) {
            gui.setWaitScreenText("Systemspeicher voll. " + ex.getMessage());
            Thread.sleep(3000);
            throw ex;
        } catch (UnsupportedEncodingException ex) {
            gui.setWaitScreenText("Zeichenkodierung nicht unterst\u00FCzt: " + ex.getMessage());
            Thread.sleep(3000);
            throw ex;
        } catch (Exception ex) {
            gui.setWaitScreenText("SMS nicht gesendet: " + ex.getMessage());
            Thread.sleep(3000);
            throw ex;
        } catch (Throwable e) {
            gui.setWaitScreenText("Unklarer Fehler: " + e.toString());
            Thread.sleep(10000);
            throw new Exception("Fehler!");
        }
    }

    /**
     * Handles the actual communication between the connector and the
     * {@link #WEBSERVICE_URL GMX server}. It first creates the protocol message
     * by invoking {@link #createRequest(String method, String version,
     * Hashtable params, boolean gmxFlag) createRequest()}, transmits it to the
     * server via HTTP and returns the server's response as a key value paired
     * collection after invoking {@link #parseResponse(java.lang.String)
     * parseRepsonse()}.
     * @param method determines the sort of request transmitted to the server.
     *        Valid arguments are:
     *        <ul>
     *        <li>{@code GET_CUSTOMER}: Logs the user in.<ul>
     *            <li>Necessary {@code version} parameter value: {@code 1.10}</li>
     *            <li>Obligatory key value pairs contained in {@code params} needed
     *            for this request: tbd</li>
     *            <li>{@code gmxFlag}: Set {@code true}.</li>
     *            <li>Received key value pairs: tbd</li></ul>
     *        </li>
     *        <li>{@code SEND_SMS}: Sends the SMS in the end.<ul>
     *            <li>Necessary {@code version} parameter value: {@code 1.01}</li>
     *            <li>Obligatory key value pairs contained in {@code params} needed
     *            for this request: tbd</li>
     *            <li>{@code gmxFlag}: Set {@code false}.</li>
     *            <li>Received key value pairs: tbd</li></ul>
     *        </li>
     *        </ul>
     * @param version obligatory parameter for the specified request. Value
     *        depends on the selected {@code method}.
     * @param params obligatory key value pair collection containing all
     *        parameters send to the server. Contents depend on the selected
     *        {@code method}.
     * @param gmxFlag defines whether the current request is GMX specific. Value
     *        depends on selected {@code method}.
     * @throws Exception
     * @return a collection with all key value pairs inlcuded in the server's
     *         response.
     */
    private Hashtable sendPackage(String method, String version, Hashtable params, boolean gmxFlag) throws Exception {
        HttpConnection connection = (HttpConnection) Connector.open(WEBSERVICE_URL, Connector.READ_WRITE);

        if (connection == null) {
            throw new RuntimeException("Konnte keine Verbindung zum Server aufbauen");
        }
        
        connection.setRequestMethod(HttpConnection.POST);
        connection.setRequestProperty("Accept", "*/*");
        connection.setRequestProperty("Content-Type", "text/plain");
        connection.setRequestProperty("Content-Encoding", "wr-cs");
        connection.setRequestProperty("User-Agent", "Mozilla/3.0 (compatible)");

        //#if Test
//#         // Output only on developer site, message contains sensitive data
//#         gui.debug("Erstelle Serveranfrage mit folgenden Parametern: " + params);
        //#endif
        
        String request = createRequest(method, version, params, gmxFlag);
        byte[] reqEnc = encodeCP1252(request.toCharArray());

        //#if Test
//#         // Output as is only on developer site, message contains sensitive data
//#         gui.debug("Serveranfrage: " + new String(decodeCP1252(reqEnc)));
        //#else
        gui.debug("Serveranfrage: " + anonymizeProtocolMsg(new String(decodeCP1252(reqEnc))));
        //#endif

        connection.setRequestProperty("Content-Length", "" + reqEnc.length);

        OutputStream os = connection.openOutputStream();
        os.write(reqEnc, 0, reqEnc.length);
        os.close();
        gui.debug("Bytes \u00FCbertragen: " + connection.getRequestProperty("Content-Length"));

        int responseCode = connection.getResponseCode();
        if (responseCode != HttpConnection.HTTP_OK) {
            throw new IOException("HTTP Antwort-Code: " + responseCode
                    + ", Grund: " + connection.getResponseMessage());
        }

        InputStream is = connection.openInputStream();

        int length = (int) connection.getLength();
        gui.debug("Empfange " + length + " Bytes...");

        byte[] buffer = new byte[length];
        is.read(buffer, 0, length);
        is.close();

        String asString = new String(decodeCP1252(buffer));

        //#if Test
//#         // Output as is only during development, message contains sensitive data
//#         gui.debug("Serverantwort: " + asString);
        //#else
        gui.debug("Serverantwort: " + anonymizeProtocolMsg(asString));
        //#endif

        if (asString.indexOf("<WR TYPE=\"RSPNS\"") < 0) {
            throw new RuntimeException("Keine g\u00FCltige Serverantwort empfangen");
        }

        String line = asString.substring(asString.indexOf("<WR TYPE=\"RSPNS\""), asString.indexOf("</WR>") + 5);

        return parseResponse(line);
    }

    private byte[] encodeCP1252(char[] string) {
        byte[] isoString = new byte[0];

        Hashtable unicodeToCP1252 = new Hashtable();
        unicodeToCP1252.put("\u20AC", new Byte((byte) 0x80)); //EURO SIGN
        unicodeToCP1252.put("\u201A", new Byte((byte) 0x82)); //SINGLE LOW-9 QUOTATION MARK
        unicodeToCP1252.put("\u0192", new Byte((byte) 0x83)); //LATIN SMALL LETTER F WITH HOOK
        unicodeToCP1252.put("\u201E", new Byte((byte) 0x84)); //DOUBLE LOW-9 QUOTATION MARK
        unicodeToCP1252.put("\u2026", new Byte((byte) 0x85)); //HORIZONTAL ELLIPSIS
        unicodeToCP1252.put("\u2020", new Byte((byte) 0x86)); //DAGGER
        unicodeToCP1252.put("\u2021", new Byte((byte) 0x87)); //DOUBLE DAGGER
        unicodeToCP1252.put("\u02C6", new Byte((byte) 0x88)); //MODIFIER LETTER CIRCUMFLEX ACCENT
        unicodeToCP1252.put("\u2030", new Byte((byte) 0x89)); //PER MILLE SIGN
        unicodeToCP1252.put("\u0160", new Byte((byte) 0x8A)); //LATIN CAPITAL LETTER S WITH CARON
        unicodeToCP1252.put("\u2039", new Byte((byte) 0x8B)); //SINGLE LEFT-POINTING ANGLE QUOTATION MARK
        unicodeToCP1252.put("\u0152", new Byte((byte) 0x8C)); //LATIN CAPITAL LIGATURE OE
        unicodeToCP1252.put("\u017D", new Byte((byte) 0x8E)); //LATIN CAPITAL LETTER Z WITH CARON
        unicodeToCP1252.put("\u2018", new Byte((byte) 0x91)); //LEFT SINGLE QUOTATION MARK
        unicodeToCP1252.put("\u2019", new Byte((byte) 0x92)); //RIGHT SINGLE QUOTATION MARK
        unicodeToCP1252.put("\u201C", new Byte((byte) 0x93)); //LEFT DOUBLE QUOTATION MARK
        unicodeToCP1252.put("\u201D", new Byte((byte) 0x94)); //RIGHT DOUBLE QUOTATION MARK
        unicodeToCP1252.put("\u2022", new Byte((byte) 0x95)); //BULLET
        unicodeToCP1252.put("\u2013", new Byte((byte) 0x96)); //EN DASH
        unicodeToCP1252.put("\u2014", new Byte((byte) 0x97)); //EM DASH
        unicodeToCP1252.put("\u02DC", new Byte((byte) 0x98)); //SMALL TILDE
        unicodeToCP1252.put("\u2122", new Byte((byte) 0x99)); //TRADE MARK SIGN
        unicodeToCP1252.put("\u0161", new Byte((byte) 0x9A)); //LATIN SMALL LETTER S WITH CARON
        unicodeToCP1252.put("\u203A", new Byte((byte) 0x9B)); //SINGLE RIGHT-POINTING ANGLE QUOTATION MARK
        unicodeToCP1252.put("\u0153", new Byte((byte) 0x9C)); //LATIN SMALL LIGATURE OE
        unicodeToCP1252.put("\u017E", new Byte((byte) 0x9E)); //LATIN SMALL LETTER Z WITH CARON
        unicodeToCP1252.put("\u0178", new Byte((byte) 0x9F)); //LATIN CAPITAL LETTER Y WITH DIAERESIS

        for (int i = 0; i < string.length; i++) {
            byte[] extended = new byte[isoString.length + 1];
            System.arraycopy(isoString, 0, extended, 0, isoString.length);
            isoString = extended;

            // Transform Unicode character to unsigned byte
            if (unicodeToCP1252.containsKey("" + string[i])) {
                Byte subst = (Byte) unicodeToCP1252.get("" + string[i]);
                isoString[isoString.length - 1] = (byte) (subst.byteValue() & 0xFF);
            } else {
                isoString[isoString.length - 1] = (byte) (string[i] & 0xFF);
            }
        }
        return isoString;
    }

    private char[] decodeCP1252(byte[] bytes) {
        char[] utfString = new char[0];

        Hashtable cp1252toUnicode = new Hashtable();
        cp1252toUnicode.put(new Byte((byte) 0x80), "\u20AC"); // EURO SIGN
        cp1252toUnicode.put(new Byte((byte) 0x81), "\u003F"); // UNDEFINED
        cp1252toUnicode.put(new Byte((byte) 0x82), "\u201A"); // SINGLE LOW-9 QUOTATION MARK
        cp1252toUnicode.put(new Byte((byte) 0x83), "\u0192"); // LATIN SMALL LETTER F WITH HOOK
        cp1252toUnicode.put(new Byte((byte) 0x84), "\u201E"); // DOUBLE LOW-9 QUOTATION MARK
        cp1252toUnicode.put(new Byte((byte) 0x85), "\u2026"); // HORIZONTAL ELLIPSIS
        cp1252toUnicode.put(new Byte((byte) 0x86), "\u2020"); // DAGGER
        cp1252toUnicode.put(new Byte((byte) 0x87), "\u2021"); // DOUBLE DAGGER
        cp1252toUnicode.put(new Byte((byte) 0x88), "\u02C6"); // MODIFIER LETTER CIRCUMFLEX ACCENT
        cp1252toUnicode.put(new Byte((byte) 0x89), "\u2030"); // PER MILLE SIGN
        cp1252toUnicode.put(new Byte((byte) 0x8A), "\u0160"); // LATIN CAPITAL LETTER S WITH CARON
        cp1252toUnicode.put(new Byte((byte) 0x8B), "\u2039"); // SINGLE LEFT-POINTING ANGLE QUOTATION MARK
        cp1252toUnicode.put(new Byte((byte) 0x8C), "\u0152"); // LATIN CAPITAL LIGATURE OE
        cp1252toUnicode.put(new Byte((byte) 0x8D), "\u003F"); // UNDEFINED
        cp1252toUnicode.put(new Byte((byte) 0x8E), "\u017D"); // LATIN CAPITAL LETTER Z WITH CARON
        cp1252toUnicode.put(new Byte((byte) 0x8F), "\u003F"); // UNDEFINED
        cp1252toUnicode.put(new Byte((byte) 0x90), "\u003F"); // UNDEFINED
        cp1252toUnicode.put(new Byte((byte) 0x91), "\u2018"); // LEFT SINGLE QUOTATION MARK
        cp1252toUnicode.put(new Byte((byte) 0x92), "\u2019"); // RIGHT SINGLE QUOTATION MARK
        cp1252toUnicode.put(new Byte((byte) 0x93), "\u201C"); // LEFT DOUBLE QUOTATION MARK
        cp1252toUnicode.put(new Byte((byte) 0x94), "\u201D"); // RIGHT DOUBLE QUOTATION MARK
        cp1252toUnicode.put(new Byte((byte) 0x95), "\u2022"); // BULLET
        cp1252toUnicode.put(new Byte((byte) 0x96), "\u2013"); // EN DASH
        cp1252toUnicode.put(new Byte((byte) 0x97), "\u2014"); // EM DASH
        cp1252toUnicode.put(new Byte((byte) 0x98), "\u02DC"); // SMALL TILDE
        cp1252toUnicode.put(new Byte((byte) 0x99), "\u2122"); // TRADE MARK SIGN
        cp1252toUnicode.put(new Byte((byte) 0x9A), "\u0161"); // LATIN SMALL LETTER S WITH CARON
        cp1252toUnicode.put(new Byte((byte) 0x9B), "\u203A"); // SINGLE RIGHT-POINTING ANGLE QUOTATION MARK
        cp1252toUnicode.put(new Byte((byte) 0x9C), "\u0153"); // LATIN SMALL LIGATURE OE
        cp1252toUnicode.put(new Byte((byte) 0x9D), "\u003F"); // UNDEFINED
        cp1252toUnicode.put(new Byte((byte) 0x9E), "\u017E"); // LATIN SMALL LETTER Z WITH CARON
        cp1252toUnicode.put(new Byte((byte) 0x9F), "\u0178"); // LATIN CAPITAL LETTER Y WITH DIAERESIS

        for (int i = 0; i < bytes.length; i++) {
            char[] extended = new char[utfString.length + 1];
            System.arraycopy(utfString, 0, extended, 0, utfString.length);
            utfString = extended;

            Byte b = new Byte(bytes[i]);
            if (cp1252toUnicode.containsKey(b)) {
                String subst = (String) cp1252toUnicode.get(b);
                utfString[utfString.length - 1] = subst.charAt(0);
            } else {
                // Create the Unicode char for further processing
                // from the unsigned byte value
                utfString[utfString.length - 1] = (char) (bytes[i] & 0xFF);
            }
        }
        return utfString;
    }

    /**
     * Creates an XML-style protocol message used for the communication between
     * connector and server. Takes all parameters and wraps it into a message
     * sent to the server.
     * @param method cf. {@link #sendPackage(java.lang.String, java.lang.String,
     * java.util.Hashtable, boolean) sendPackage()}.
     * @param version cf. {@link #sendPackage(java.lang.String, java.lang.String,
     * java.util.Hashtable, boolean sendPackage()) sendPackage()}.
     * @param params cf. {@link #sendPackage(java.lang.String, java.lang.String,
     * java.util.Hashtable, boolean) sendPackage()}.
     * @param gmxFlag cf. {@link #sendPackage(java.lang.String, java.lang.String,
     * java.util.Hashtable, boolean) sendPackage()}.
     * @return the request message formatted according to the protocol used for
     * communication between connector and server.
     */
    private String createRequest(String method, String version, Hashtable params, boolean gmxFlag) {
        StringBuffer request = new StringBuffer();
        request.append("<WR TYPE=\"RQST\" NAME=\"")
                .append(method)
                .append("\" VER=\"")
                .append(version)
                .append("\" PROGVER=\"1.13.04\">")
                .append(gmxFlag ? "gmx=1\\p" : "");

        Enumeration paramKeys = params.keys();
        while (paramKeys.hasMoreElements()) {
            Object key = paramKeys.nextElement();
            Object value = params.get(key);
            request.append(key).append("=").append(value).append("\\p");
        }

        request.append("</WR>");
        return request.toString();
    }

    /**
     * Takes a server's response as parameter and splits it into key value pairs
     * which are returned as a {@link Hashtable}.
     * @param response server's response on a request sent using {@link
     *        #sendPackage(java.lang.String, java.lang.String,
     *        java.util.Hashtable, boolean) sendPackage()}
     * @return a key value paired collection created from the server's response.
     */
    private Hashtable parseResponse(String response) {
        Hashtable result = new Hashtable();
        String dataString = response.substring(response.indexOf('>') + 1, response.indexOf("</WR>"));

        while (dataString.length() > 0) {
            int posKeyEnd = dataString.indexOf("=");
            int posValueEnd = dataString.indexOf("\\p");

            String key = dataString.substring(0, posKeyEnd);
            String value = dataString.substring(posKeyEnd + 1, posValueEnd);

            result.put(key, value);

            dataString = dataString.substring(posValueEnd + 2);
        }
        //#if Test
//#         // Output only during development, message contains sensitive data
//#         gui.debug("Serverantwort geparsed: " + result);
        //#endif
        return result;
    }

    private String maskSpecialChars(String text) {
        StringBuffer escaped = new StringBuffer(text);
        char[] toEscape = "\\<>".toCharArray();

        for(int i = 0; i < toEscape.length; i++) {
            for (int j = 0; j < escaped.length(); j++) {
                 if (escaped.charAt(j) == toEscape[i]) {
                     escaped.insert(j++, '\\');
                 }
            }
        }
        return escaped.toString();
    }

    private String anonymizeProtocolMsg(String message) {
        String[] keywords = {"customer_id",
                            "reseller_id",
                            "register_date",
                            "email_address",
                            "password",
                            "first_name",
                            "last_name",
                            "cell_phone",
                            "sms_sender",
                            "receivers",
                            "sms_text"};

        StringBuffer msg = new StringBuffer(message);

        String key = new String();
        for (int i = 0; i < keywords.length; i++) {
            key = keywords[i];
   
            int start = message.indexOf(key);
            if (start < 0) {
                continue;
            }
            
            int caret = start + key.length() + 1;
            int stop = message.indexOf("\\p", start);

            while (caret < stop) {
                msg.setCharAt(caret, '*');
                caret++;
            }
        }
        return msg.toString();
    }
}