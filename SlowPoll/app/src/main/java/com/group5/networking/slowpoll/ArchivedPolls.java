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

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

import java.util.ArrayList;

/**
 * Created by Joe on 4/9/2018.
 */

public class ArchivedPolls extends Fragment implements View.OnClickListener {

    DatabaseReference dref;
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
    Button clearPollsButton;
    SQLiteDatabase db;
    String selection;

    private boolean PresentInLocalDatabase(String key) {
        db = controller.getReadableDatabase();
        selection = DatabaseContract.PollEntry._ID + " = ?";
        String[] selectionArgs = { key };
        Cursor cursor = db.query(
                DatabaseContract.PollEntry.TABLE_NAME,   // The table to query
                null,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                null               // The sort order
        );
        while(cursor.moveToNext()) {
            /*System.out.println("0: " + cursor.getString(0)); //prints out key
            System.out.println("1: " + cursor.getString(1)); //prints out title
            System.out.println("2: " + cursor.getString(2)); //prints out optionOne
            System.out.println("3: " + cursor.getString(3)); //prints out optionTwo
            System.out.println("4: " + cursor.getString(4)); //prints out # responses to optionOne
            System.out.println("5: " + cursor.getString(5)); //prints out # responses to optionTwo
            System.out.println("6: " + cursor.getString(6)); //prints out incentive
            System.out.println("7: " + cursor.getString(7));*/ //prints out selected answer by user
            if(cursor.getString(0).equals(key)){
                return true;
            }
        }
        //System.out.println("Key is not present in Local Database");
        return false;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Archived Polls");
        clearPollsButton = (Button) getView().findViewById(R.id.deleteAllPolls);
        listview=(ListView) getActivity().findViewById(R.id.archivedPollsListView);
        controller = new PollController(getContext());
        final ArchivedPollAdapter adapter = new ArchivedPollAdapter(list, getContext(), optOneList, optTwoList, listKeys, incentiveList, responseOneList, responseTwoList, answeredList);
        listview.setAdapter(adapter);
        listview.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        controller = new PollController(getContext());
        dref= FirebaseDatabase.getInstance().getReference().child("polls");
        dref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if(PresentInLocalDatabase(dataSnapshot.getKey())) {
                    db = controller.getReadableDatabase();
                    selection = DatabaseContract.PollEntry._ID + " = ?";
                    String[] selectionArgs = { dataSnapshot.getKey() };
                    Cursor cursor = db.query(
                            DatabaseContract.PollEntry.TABLE_NAME,   // The table to query
                            null,             // The array of columns to return (pass null to get all)
                            selection,              // The columns for the WHERE clause
                            selectionArgs,          // The values for the WHERE clause
                            null,                   // don't group the rows
                            null,                   // don't filter by row groups
                            null               // The sort order
                    );
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
                }
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                String key = dataSnapshot.getKey();
                int index = listKeys.indexOf(key);

                if (index != -1) {
                    list.remove(index);
                    listKeys.remove(index);
                    optOneList.remove(index);
                    optTwoList.remove(index);
                    incentiveList.remove(index);
                    responseOneList.remove(index);
                    responseTwoList.remove(index);
                    adapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(list.size() == 0){
                    Toast.makeText(getContext(), "Looks like there are no archived polls. Please check back later!", Toast.LENGTH_LONG).show();
                }
            }
        }, 1000);

        clearPollsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    Toast.makeText(getActivity(), "Records Cleared", Toast.LENGTH_SHORT).show();
                    Fragment fragment = new ArchivedPolls();
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.replace(R.id.content_main, fragment);
                    ft.commit();
                }catch(SQLiteException e){
                    Toast.makeText(getActivity(), "Error Clearing Records", Toast.LENGTH_SHORT).show();
                }
            }
        });
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
