/*
 * Copyright 2011 Matthew Precious
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mattprecious.prioritysms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.PhoneLookup;
import android.telephony.SmsMessage;

public class SMSReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Bundle bundle = intent.getExtras();
        SmsMessage[] msgs = null;
        if (bundle != null) {
            Object[] pdus = (Object[]) bundle.get("pdus");
            msgs = new SmsMessage[pdus.length];
            for (int i = 0; i < msgs.length; i++) {
                msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                String sender = msgs[i].getOriginatingAddress();
                String message = msgs[i].getMessageBody();

                SharedPreferences settings = context.getSharedPreferences(context.getPackageName() + "_preferences", 0);

                boolean enabled         = settings.getBoolean("enabled", false);
                boolean filterKeyword   = settings.getBoolean("filter_keyword", false);
                boolean filterContact   = settings.getBoolean("filter_contact", false);

                String keyword          = settings.getString("keyword", "");
                String contactLookupKey = settings.getString("contact", "");
                
                // return if we aren't filtering by anything
                if (!filterKeyword && !filterContact) {
                    return;
                }
                
                boolean keywordCondition = false;
                boolean contactCondition = false;

                // if we're filtering by keyword,
                // check if the keyword is set, and
                // check if the message contains the keyword
                if (filterKeyword) {
                    keywordCondition = !keyword.equals("") && (message.toLowerCase().indexOf(keyword.toLowerCase()) != -1);
                } else {
                    keywordCondition = true;
                }

                // if we're filtering by contact,
                // look up the contact id of our filtered contact, and
                // look up the contact id of the sender, and
                // check if they're the same ID
                if (filterContact && !contactLookupKey.equals("")) {
                    Uri contactUri = Uri.withAppendedPath(Contacts.CONTENT_LOOKUP_URI, contactLookupKey);

                    String[] columns = new String[] { Contacts._ID };
                    Cursor c = context.getContentResolver().query(contactUri, columns, null, null, null);

                    if (c.moveToFirst()) {
                        String contactId = c.getString(c.getColumnIndex(Contacts._ID));
                        
                        Uri phoneUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(sender));
                        
                        String[] columns2 = new String[] { PhoneLookup._ID };
                        Cursor c2 = context.getContentResolver().query(phoneUri, columns2, null, null, null);
                        
                        if (c2.moveToFirst()) {
                            String thisContactId = c2.getString(c.getColumnIndex(PhoneLookup._ID));
                            
                            contactCondition = thisContactId.equals(contactId);
                        }
                        
                        c2.close();
                    }
                    
                    c.close();
                } else {
                    contactCondition = true;
                }

                // if we're enabled and all of our conditions are met, open the
                // notification
                if (enabled && keywordCondition && contactCondition) {
                    Intent newIntent = new Intent(context, Notification.class);

                    newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    newIntent.putExtra("sender", sender);
                    newIntent.putExtra("message", message);

                    context.startActivity(newIntent);

                }
            }
        }

    }
}
