package com.group5.networking.slowpoll;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

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

    private int selectedPosition = 0;
    private Boolean itemSelected = false;


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getActivity().setTitle("Browse Local Polls");

        listview=(ListView) getActivity().findViewById(R.id.pollsListView);
        //final ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),android.R.layout.simple_dropdown_item_1line,list);
        final PollAdapter adapter = new PollAdapter(list, getContext(), optOneList, optTwoList, listKeys);
        listview.setAdapter(adapter);
        listview.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listview.setOnItemClickListener(
            new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    selectedPosition = position;
                    itemSelected = true;
                    Fragment fragment = new PollDetail();
                    if(fragment != null){
                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                        ft.replace(R.id.content_main, fragment);
                        ft.commit();
                    }
                }
            }
        );
        dref= FirebaseDatabase.getInstance().getReference().child("polls");
        dref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                list.add(dataSnapshot.child("title").getValue(String.class));
                optOneList.add(dataSnapshot.child("optionOne").getValue(String.class));
                optTwoList.add(dataSnapshot.child("optionTwo").getValue(String.class));
                listKeys.add(dataSnapshot.getKey());
                adapter.notifyDataSetChanged();
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
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.browsepolls,container,false);
    }


}
