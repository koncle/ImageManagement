package com.koncle.imagemanagement.bean;

import android.os.Parcel;

import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;

/**
 * Created by Koncle on 2018/1/21.
 */

public class MySearchSuggestion implements SearchSuggestion {
    public int getType() {
        return type;
    }

    private int type;
    private String suggestion;

    public static int TYPE_EVENT = 1;

    public static int TYPE_TAG = 0;

    @Override
    public boolean equals(Object obj) {
        return obj instanceof SearchSuggestion && suggestion.equals(((SearchSuggestion) obj).getBody());
    }

    public MySearchSuggestion(String suggestion, int type) {
        this.suggestion = suggestion;
        this.type = type;
    }

    @Override
    public String getBody() {
        return suggestion;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.suggestion);
    }

    public MySearchSuggestion() {
    }

    protected MySearchSuggestion(Parcel in) {
        this.suggestion = in.readString();
    }

    public static final Creator<MySearchSuggestion> CREATOR = new Creator<MySearchSuggestion>() {
        @Override
        public MySearchSuggestion createFromParcel(Parcel source) {
            return new MySearchSuggestion(source);
        }

        @Override
        public MySearchSuggestion[] newArray(int size) {
            return new MySearchSuggestion[size];
        }
    };
}
