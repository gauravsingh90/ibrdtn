package com.group5.networking.slowpoll;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Created by Joe on 4/24/2018.
 */

public class PollAdapter  extends BaseAdapter implements ListAdapter {
    //PollAdapter class is used to display polls as a list in the BrowsePolls fragment
    //For the sake of demo, the PollAdapter holds ArrayList's of all poll's relevant data
    //If time was not a factor, we would change this to handle a single ArrayList of Polls.
    //We would also make the fields private.
    public ArrayList<String> list = new ArrayList<String>();
    public ArrayList<String> keysList = new ArrayList<String>();
    public ArrayList<String> optOneList = new ArrayList<String>();
    public ArrayList<String> optTwoList = new ArrayList<String>();
    public ArrayList<String> incentiveList = new ArrayList<String>();
    public ArrayList<Integer> responseOneList = new ArrayList<Integer>();
    public ArrayList<Integer> responseTwoList = new ArrayList<Integer>();
    public PollController controller;
    DatabaseReference dref;
    public Context context;


    //constructor to initialize arraylists
    public PollAdapter(ArrayList<String> list, Context context, ArrayList<String> optOneList, ArrayList<String> optTwoList, ArrayList<String> keysList,ArrayList<String> incentiveList, ArrayList<Integer> responseOneList, ArrayList<Integer> responseTwoList) {
        this.list = list;
        this.context = context;
        this.optOneList = optOneList;
        this.optTwoList = optTwoList;
        this.keysList = keysList;
        this.incentiveList = incentiveList;
        this.responseOneList = responseOneList;
        this.responseTwoList = responseTwoList;
    }

    //called on button click, saves the poll and user's response locally
    public void insert_entry(String id, String title, String optionOne, String optionTwo, int responseOne, int responseTwo, String incentive, int answer){

        SQLiteDatabase db = controller.getWritableDatabase();
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.PollEntry._ID, id);
        values.put(DatabaseContract.PollEntry.COLUMN_NAME_TITLE, title);
        values.put(DatabaseContract.PollEntry.COLUMN_NAME_OPTIONONE, optionOne);
        values.put(DatabaseContract.PollEntry.COLUMN_NAME_OPTIONTWO, optionTwo);
        values.put(DatabaseContract.PollEntry.COLUMN_NAME_RESPONSEONE, responseOne);
        values.put(DatabaseContract.PollEntry.COLUMN_NAME_RESPONSETWO, responseTwo);
        values.put(DatabaseContract.PollEntry.COLUMN_NAME_INCENTIVE, incentive);
        values.put(DatabaseContract.PollEntry.COLUMN_NAME_ANSWER, answer);
        // Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert(DatabaseContract.PollEntry.TABLE_NAME, null, values);
        System.out.println("INSERTED NEW POLL ENTRY. PRIMARY KEY: " + id);
    }


    public void incrementResponseValueOptionOne(String key, int position){
        dref= FirebaseDatabase.getInstance().getReference().child("polls").child(key);
        int val = responseOneList.get(position);
        val++;
        responseOneList.set(position, val);
        dref.child("responseOne").setValue(val);
        System.out.println("incremented responseOne value");
    }
    public void incrementResponseValueOptionTwo(String key, int position){
        dref= FirebaseDatabase.getInstance().getReference().child("polls").child(key);
        int val = responseTwoList.get(position);
        val++;
        responseTwoList.set(position, val);
        dref.child("responseTwo").setValue(val);
        System.out.println("incremented responseTwo value");
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int pos) {
        return list.get(pos);
    }

    @Override
    public long getItemId(int pos) {
       // return list.get(pos).getId();
        return 0;
        // /just return 0 if your list items do not have an Id variable.
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.single_poll_list, null);
        }

        //Handle TextView and display string from your list
        final TextView listItemText = (TextView) view.findViewById(R.id.list_item_string);
        listItemText.setText(list.get(position));

        //Handle buttons and add onClickListeners
       final Button optTwoBtn = (Button) view.findViewById(R.id.optionTwoBtn);
       optTwoBtn.setText(optTwoList.get(position));
       final Button optOneBtn = (Button) view.findViewById(R.id.optionOneBtn);
       optOneBtn.setText(optOneList.get(position));
        controller = new PollController(context);

        optOneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //disable buttons to avoid double clicking
                optOneBtn.setEnabled(false);
                optTwoBtn.setVisibility(View.GONE);
                //increment value in database
                incrementResponseValueOptionOne(keysList.get(position), position);
                //display incentive alert if present, otherwise toast saying thank you
                if(incentiveList.get(position) != null && !incentiveList.get(position).toString().trim().equals("") ){
                    Toast.makeText(v.getContext(), "Thank you for participating!\n" + incentiveList.get(position).toString(), Toast.LENGTH_LONG).show();
                }
                //make selected option background green
                optOneBtn.setBackgroundColor(Color.parseColor ("#b3e6c9"));
                optOneBtn.setTextColor(Color.BLACK);
                //save responded poll to archived poll sqlite
                insert_entry(keysList.get(position), list.get(position), optOneList.get(position), optTwoList.get(position), responseOneList.get(position), responseTwoList.get(position), incentiveList.get(position), 1);
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        optOneBtn.setVisibility(View.GONE);
                        listItemText.setVisibility(View.GONE);
                    }
                }, 4000);
                notifyDataSetChanged();
            }
        });
        optTwoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                optOneBtn.setVisibility(View.GONE);
                optTwoBtn.setEnabled(false);
                incrementResponseValueOptionTwo(keysList.get(position), position);
                //display incentive alert if present, otherwise toast saying thank you
                if(incentiveList.get(position) != null && !incentiveList.get(position).toString().trim().equals("") ){
                    Toast.makeText(v.getContext(), "Thank you for participating!\n" + incentiveList.get(position).toString(), Toast.LENGTH_LONG).show();
                }
                optTwoBtn.setBackgroundColor(Color.parseColor ("#b3e6c9"));
                optTwoBtn.setTextColor(Color.BLACK);
                insert_entry(keysList.get(position), list.get(position), optOneList.get(position), optTwoList.get(position), responseOneList.get(position), responseTwoList.get(position), incentiveList.get(position), 2);
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        optTwoBtn.setVisibility(View.GONE);
                        listItemText.setVisibility(View.GONE);
                    }
                }, 4000);
                notifyDataSetChanged();

            }
        });

        return view;
    }

}
