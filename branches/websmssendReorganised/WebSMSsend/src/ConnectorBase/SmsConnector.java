/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ConnectorBase;

import webSMSsend.IGui;

/**
 *
 * @author Copyright 2011 redrocketracoon@googlemail.com
 */
public abstract class SmsConnector implements ISmsConnector {

protected IGui gui_ = null;
protected int remsms_ = 0;
protected String password_;
protected String username_;

    public void Initialize(String userName, String passWord, IGui Gui) {
        password_ = passWord;
        username_ = userName;
        gui_ = Gui;
    }

    public int RemainingSMS() {
        return remsms_;
    }

    public abstract String PasswordFieldLabel();

    public abstract void Send(String smsRecv, String smsText) throws Exception;

    public abstract void Send(String smsRecv, String smsText, String senderName, boolean simulation) throws Exception ;

    public abstract int CountSms(String smsText);

}
