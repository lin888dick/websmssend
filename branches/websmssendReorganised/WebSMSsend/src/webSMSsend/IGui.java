/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package webSMSsend;

/**
 *
 * @author Copyright 2011 redrocketracoon@googlemail.com
 */
public interface IGui {
public void Debug(String debugText);
public void SetWaitScreenText(String Text);
public void setRemSMS(int remSMS, int maxFreeSMS);
public int CountSMS(String SmsText);
public void SaveItem(String sender,String itemName, String content);
public String GetItem(String sender, String itemName);
}
