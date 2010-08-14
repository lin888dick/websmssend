/*
 *
 *
    Copyright 2009 Max Hänze --- maximum.blogsite.org

    This file is part of WebSMSsend.

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

package webSMSsend;

import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;

public class ioSettings {

    protected static String getContentLoad(){
        return getData("optim",1);
    }

    protected static String getDebug(){
        return getData("debug",1);
    }

    protected static String getUsername(){
        return getData("LoginData",1);
    }
    protected static String getPassword(){
        return getData("LoginData",2);
    }
    protected static String getRemSMS(){
        return getData("remSMS",1);
    }
    protected static String getSetup(){
        return getData("setup",1);
    }
    protected static int saveToRMS(String username,String password){
        String[] data={username,password};
        return saveData("LoginData",data,2);
    }
    protected static int saveOptim(String contentLoad){
        String[] data={contentLoad};
        return saveData("optim",data,1);
    }

    protected static int saveRemSMS(String remSMS){
        String[] data={remSMS};
        return saveData("remSMS",data,1);
    }

    protected static int saveDebug(String debug){
        String[] data={debug};
        return saveData("debug",data,1);
    }

    protected static int saveSetup(String provider){
        String[] data={provider};
        return saveData("setup",data,1);
    }


    protected static String getData(String field, int nb){
        try {
            RecordStore rs = RecordStore.openRecordStore(field, true);

            if (rs.getNumRecords()>nb-1){
            String data = "";
                if (rs.getRecord(nb)!=null){
                    data=new String(rs.getRecord(nb));
                }

            rs.closeRecordStore();
            return data;
            }
            else{
                rs.closeRecordStore();
                return "";
            }

        } catch (RecordStoreException ex) {
            ex.printStackTrace();
            return "";
        }
    }
    protected static int saveData(String field, String[] content, int nb){

        try {
            RecordStore rs = RecordStore.openRecordStore(field, true);
            if (rs.getNumRecords()>nb-1){
                for (int i=1;i<=nb;i++){
                    rs.setRecord(i, content[i-1].getBytes(), 0, content[i-1].getBytes().length);

                }

            }
            else{
                for (int i=1;i<=nb;i++){
                rs.addRecord(content[i-1].getBytes(), 0, content[i-1].getBytes().length);
                }
                
            }
            rs.closeRecordStore();
            return 0;
        } catch (RecordStoreException ex) {
            ex.printStackTrace();
            return 1;
        }

    }
    
}
