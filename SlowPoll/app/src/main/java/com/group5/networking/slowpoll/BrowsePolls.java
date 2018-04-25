package com.group5.networking.slowpoll;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

/**
 * Created by Joe on 4/9/2018.
 */

public class BrowsePolls extends Fragment{

    DatabaseReference dref;
    ListView listview;
    ArrayList<String> list=new ArrayList<String>();
    ArrayList<String> optOneList = new ArrayList<String>();
    ArrayList<String> optTwoList = new ArrayList<String>();
    ArrayList<String> listKeys = new ArrayList<String>();
    ArrayList<String> incentiveList = new ArrayList<String>();
    public ArrayList<Integer> responseOneList = new ArrayList<Integer>();
    public ArrayList<Integer> responseTwoList = new ArrayList<Integer>();
    public PollController controller;
    Button refreshButton;

    private boolean NotPresentInLocalDatabase(String key) {
        SQLiteDatabase db = controller.getReadableDatabase();
        String selection = DatabaseContract.PollEntry._ID + " = ?";
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
           // System.out.println("KEY CURSOR IS AT: " + cursor.getString(0));
            if(cursor.getString(0).equals(key)){
                return false;
            }
        }
        //System.out.println("Key is not present in Local Database");
        return true;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Browse Local Polls");
        controller = new PollController(getContext());
        refreshButton = (Button) getActivity().findViewById(R.id.refreshPolls);
        listview=(ListView) getActivity().findViewById(R.id.pollsListView);
        final PollAdapter adapter = new PollAdapter(list, getContext(), optOneList, optTwoList, listKeys, incentiveList, responseOneList, responseTwoList);
        listview.setAdapter(adapter);
        listview.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listview.setDivider(null);
        listview.setDividerHeight(0);

        dref= FirebaseDatabase.getInstance().getReference().child("polls");
        dref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                //if poll is not in archived polls sqlite
                if(NotPresentInLocalDatabase(dataSnapshot.getKey())) {
                    list.add(dataSnapshot.child("title").getValue(String.class));
                    optOneList.add(dataSnapshot.child("optionOne").getValue(String.class));
                    optTwoList.add(dataSnapshot.child("optionTwo").getValue(String.class));
                    listKeys.add(dataSnapshot.getKey());
                    incentiveList.add(dataSnapshot.child("incentive").getValue(String.class));
                    responseOneList.add(dataSnapshot.child("responseOne").getValue(Integer.class));
                    responseTwoList.add(dataSnapshot.child("responseTwo").getValue(Integer.class));
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
                    Toast.makeText(getContext(), "Looks like there are no local polls at this time. Please check back later!", Toast.LENGTH_LONG).show();
                }
            }
        }, 2000);

        refreshButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Fragment fragment = new BrowsePolls();
                if(fragment != null){
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.replace(R.id.content_main, fragment);
                    ft.commit();
                }
            }
        });
    }



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.browsepolls,container,false);
    }


}
