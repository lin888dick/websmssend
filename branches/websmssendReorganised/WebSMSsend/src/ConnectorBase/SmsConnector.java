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

protected static final String REMAINING_SMS_FIELD = "RemainingSMS";
protected static final String MAX_FREE_SMS = "MaxFreeSMS";

protected IGui gui_ = null;
protected int remsms_ = 0;
protected int maxfreesms_ = 0;
protected String password_;
protected String username_;

    public void Initialize(String userName, String passWord, IGui Gui) {
        password_ = passWord;
        username_ = userName;
        gui_ = Gui;
    }

    public int RemainingSMS() { 
        try {
            String remsms = gui_.GetItem(REMAINING_SMS_FIELD);
            remsms_ = Integer.parseInt(remsms);
        } catch (Exception ex) {
            remsms_=0;
        }
        return remsms_;
    }

    public int MaxFreeSMS() {       
        try {
            String maxfreesms = gui_.GetItem(MAX_FREE_SMS);
            maxfreesms_ = Integer.parseInt(maxfreesms);
        } catch (Exception ex) {
            maxfreesms_ = 0;
        }
        return maxfreesms_;
    }
}
