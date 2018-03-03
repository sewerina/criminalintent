package com.example.criminalintent;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class Crime {

    private UUID mId;
    private String mTitle;
    private Date mDate;
    private boolean mSolved;
    private String mSuspect;
    private String mSuspectId;

    public Crime() {
        this(UUID.randomUUID());
//        mId = UUID.randomUUID();
//        mDate = new Date();
    }

    public Crime(UUID id) {
        mId = id;
        mDate = new Date();
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        mTitle = df.format(mDate);
    }

    public UUID getId() {
        return mId;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public Date getDate() {
        return mDate;
    }

    public void setDate(Date date) {
        mDate = date;
    }

    public boolean isSolved() {
        return mSolved;
    }

    public void setSolved(boolean solved) {
        mSolved = solved;
    }

    public String getSuspect() {
        return mSuspect;
    }

    public void setSuspect(String suspect) {
        this.mSuspect = suspect;
    }

    public String getSuspectId() {
        return mSuspectId;
    }

    public void setSuspectId(String suspectId) {
        this.mSuspectId = suspectId;
    }

}
