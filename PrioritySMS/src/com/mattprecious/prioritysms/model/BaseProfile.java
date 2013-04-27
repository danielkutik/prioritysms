
package com.mattprecious.prioritysms.model;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.common.collect.Sets;

import java.util.HashSet;
import java.util.Set;

public abstract class BaseProfile implements Parcelable {
    private int id;
    private String name;
    private boolean enabled;
    private Uri ringtone;
    private int volume;
    private boolean overrideSilent;
    private boolean vibrate;

    private Set<String> contacts;

    protected BaseProfile() {
        id = -1;
        contacts = new HashSet<String>();
    }

    public int getId() {
        return id;
    }

    public void setId(int rowId) {
        this.id = rowId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Uri getRingtone() {
        return ringtone;
    }

    public void setRingtone(Uri ringtone) {
        this.ringtone = ringtone;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public boolean isOverrideSilent() {
        return overrideSilent;
    }

    public void setOverrideSilent(boolean overrideSilent) {
        this.overrideSilent = overrideSilent;
    }

    public boolean isVibrate() {
        return vibrate;
    }

    public void setVibrate(boolean vibrate) {
        this.vibrate = vibrate;
    }

    public Set<String> getContacts() {
        return contacts;
    }

    public void setContacts(Set<String> contacts) {
        this.contacts = contacts;
    }

    public void addContact(String lookupKey) {
        contacts.add(lookupKey);
    }

    public void removeContact(String lookupKey) {
        contacts.remove(lookupKey);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeByte((byte) (enabled ? 1 : 0));
        dest.writeString((ringtone == null) ? null : ringtone.toString());
        dest.writeInt(volume);
        dest.writeByte((byte) (overrideSilent ? 1 : 0));
        dest.writeByte((byte) (vibrate ? 1 : 0));
        dest.writeStringArray(contacts.toArray(new String[0]));
    }

    public BaseProfile(Parcel in) {
        id = in.readInt();
        name = in.readString();
        enabled = in.readByte() == 1;

        String ringtoneStr = in.readString();
        ringtone = (ringtoneStr == null) ? null : Uri.parse(ringtoneStr);

        volume = in.readInt();
        overrideSilent = in.readByte() == 1;
        vibrate = in.readByte() == 1;

        String[] contactsArr = in.createStringArray();
        if (contactsArr == null) {
            contacts = Sets.newHashSet();
        } else {
            contacts = Sets.newHashSet(contactsArr);
        }
    }

    @Override
    public String toString() {
        return "BaseProfile [id=" + id + ", name=" + name + ", enabled=" + enabled + ", ringtone="
                + ringtone + ", volume=" + volume + ", overrideSilent=" + overrideSilent
                + ", vibrate=" + vibrate + ", contacts=" + contacts + "]";
    }

}
