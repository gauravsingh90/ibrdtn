package com.group5.networking.slowpoll;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Joe on 4/20/2018.
 */

public class Credential extends Fragment implements View.OnClickListener{

    public EditText code;
    public CredentialController controller;
    public Button button;

    public static void hideKeyboardFrom(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void insert_entry(String title){
        SQLiteDatabase db = controller.getWritableDatabase();
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.CredentialEntry.COLUMN_NAME_TITLE, title);
        // Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert(DatabaseContract.CredentialEntry.TABLE_NAME, null, values);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getActivity().setTitle("Credentials");
        code = (EditText) getView().findViewById(R.id.credentialCode);
        button = (Button) getView().findViewById(R.id.submitCredential);
        controller = new CredentialController(getContext());
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    //would obviously remove below if SlowPoll were a real thing
                    if(code.getText().toString().equals("NETWORKING")) {
                        insert_entry(code.getText().toString());
                        Toast.makeText(getActivity(), "Successful. Please restart the app to unlock features.", Toast.LENGTH_SHORT).show();
                        //Fragment fragment = new BrowsePolls();
                        //FragmentTransaction ft = getFragmentManager().beginTransaction();
                        //ft.replace(R.id.content_main, fragment);
                        //ft.commit();
                        Intent mStartActivity = new Intent(getContext(), MainActivity.class);
                        int mPendingIntentId = 123456;
                        PendingIntent mPendingIntent = PendingIntent.getActivity(getContext(), mPendingIntentId,    mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
                        AlarmManager mgr = (AlarmManager)getContext().getSystemService(Context.ALARM_SERVICE);
                        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
                        System.exit(0);
                    }
                    else{
                        Toast.makeText(getActivity(), "Invalid credentials.", Toast.LENGTH_SHORT).show();
                    }
                    code.setText("");
                    hideKeyboardFrom(getContext(),getView());
                }catch(SQLiteException e){
                    Toast.makeText(getActivity(), "Error testing credential. Please try again later.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.credential,container,false);
    }

    @Override
    public void onClick(View v) {

    }
}
