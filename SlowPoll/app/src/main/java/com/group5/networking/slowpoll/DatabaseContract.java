package com.group5.networking.slowpoll;

import android.provider.BaseColumns;

/**
 * Created by Joe on 4/23/2018.
 */

public final class DatabaseContract {
    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private DatabaseContract() {}

    /* Inner class that defines the table contents */
    public static class CredentialEntry implements BaseColumns {
        public static final String TABLE_NAME = "credential";
        public static final String COLUMN_NAME_TITLE = "title";
    }
    public static class PollEntry implements BaseColumns{
        public static final String TABLE_NAME = "poll";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_OPTIONONE = "optionOne";
        public static final String COLUMN_NAME_OPTIONTWO = "optionTwo";
        public static final String COLUMN_NAME_RESPONSEONE = "responseOne";
        public static final String COLUMN_NAME_RESPONSETWO = "responseTwo";
        public static final String COLUMN_NAME_INCENTIVE = "incentive";
        public static final String COLUMN_NAME_ANSWER = "answer";
    }
}