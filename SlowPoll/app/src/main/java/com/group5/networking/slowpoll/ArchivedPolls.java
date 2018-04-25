package com.group5.networking.slowpoll;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;

/**
 * Created by Joe on 4/9/2018.
 * Entire class works offline using local storage - no Firebase connections initialized
 */

public class ArchivedPolls extends Fragment implements View.OnClickListener {

    ListView listview;
    ArrayList<String> list=new ArrayList<String>();
    ArrayList<String> optOneList = new ArrayList<String>();
    ArrayList<String> optTwoList = new ArrayList<String>();
    ArrayList<String> listKeys = new ArrayList<String>();
    ArrayList<String> incentiveList = new ArrayList<String>();
    public ArrayList<Integer> responseOneList = new ArrayList<Integer>();
    public ArrayList<Integer> answeredList = new ArrayList<Integer>();
    public ArrayList<Integer> responseTwoList = new ArrayList<Integer>();
    public PollController controller;
    SQLiteDatabase db;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Archived Polls");
        listview=(ListView) getActivity().findViewById(R.id.archivedPollsListView);
        controller = new PollController(getContext());
        final ArchivedPollAdapter adapter = new ArchivedPollAdapter(list, getContext(), optOneList, optTwoList, listKeys, incentiveList, responseOneList, responseTwoList, answeredList);
        listview.setAdapter(adapter);
        listview.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        //reference to local sqlite database
        controller = new PollController(getContext());

        db = controller.getReadableDatabase();
        Cursor cursor = db.query(
                DatabaseContract.PollEntry.TABLE_NAME,   // The table to query
                null,             // The array of columns to return (pass null to get all)
                null,              // The columns for the WHERE clause
                null,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                null               // The sort order
        );
        //loop through all saved polls, push values to relevant arraylists and update adapter
        while(cursor.moveToNext()) {
            list.add(cursor.getString(1));
            optOneList.add(cursor.getString(2));
            optTwoList.add(cursor.getString(3));
            listKeys.add(cursor.getString(0));
            incentiveList.add(cursor.getString(6));
            responseOneList.add(cursor.getInt(4));
            responseTwoList.add(cursor.getInt(5));
            answeredList.add(cursor.getInt(7));
        }
        // answeredList.add();
        adapter.notifyDataSetChanged();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(list.size() == 0){
                    Toast.makeText(getContext(), "Looks like there are no archived polls. Please check back later!", Toast.LENGTH_LONG).show();
                }
            }
        }, 1000);


    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.archivedpolls,container, false);
    }


    @Override
    public void onClick(View v) {
    }
}
