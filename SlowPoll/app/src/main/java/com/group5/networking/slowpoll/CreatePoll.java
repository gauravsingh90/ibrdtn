package com.group5.networking.slowpoll;

import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

/**
 * Uses FirebaseDatabase references to push poll to database.
 * Replace writeNewPoll with relevant poll queue/broadcast method when integrating DTN
 */

public class CreatePoll extends Fragment implements View.OnClickListener {

    public EditText title, optionOne, optionTwo, incentive;
    public PollController controller;
    public Button button;
    public FirebaseDatabase database;
    DatabaseReference ref;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    //replace functionality in this method so that poll is queued/broadcasted rather than pushed to firebase
    //no further changes need to be made to this class
    private void writeNewPoll(String title, String optionOne, String optionTwo, String incentive) {
        Poll poll = new Poll(title, optionOne, optionTwo, incentive);
        String key = ref.child("polls").push().getKey();
        ref.child("polls").child(key).setValue(poll);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        database = FirebaseDatabase.getInstance();
        ref = database.getReference();

        getActivity().setTitle("Create a Poll");
        title = (EditText) getView().findViewById(R.id.pollTitle);
        optionOne = (EditText) getView().findViewById(R.id.optionOne);
        optionTwo = (EditText) getView().findViewById(R.id.optionTwo);
        incentive = (EditText) getView().findViewById(R.id.incentive);
        button = (Button) getView().findViewById(R.id.buttonCreatePoll);
        //textView = (TextView) getView().findViewById(R.id.listAllPolls);
        controller = new PollController(getContext());

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //one line length validation
                if((title.getText().toString().length() > 0 && title.getText().toString().length() < 140) && (optionOne.getText().toString().length() > 0 && optionOne.getText().toString().length() < 50) && (optionTwo.getText().toString().length() > 0 && optionTwo.getText().toString().length() < 50) && ( incentive.getText().toString().length() < 140)) {
                    try {
                        writeNewPoll(title.getText().toString(), optionOne.getText().toString(), optionTwo.getText().toString(), incentive.getText().toString());
                        Toast.makeText(getActivity(), "Poll Created", Toast.LENGTH_SHORT).show();
                        Fragment fragment = new BrowsePolls();
                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                        ft.replace(R.id.content_main, fragment);
                        ft.commit();
                    } catch (SQLiteException e) {
                        Toast.makeText(getActivity(), "Unable to create poll at this time. Please try again later.", Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    Toast.makeText(getActivity(), "Error: Either missing a required field, or input is too long. Please make sure your poll title & incentive are less than 140 characters and each option is less than 50 characters.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        //button = (Button) inflater.inflate(R.layout.createpoll, container, false).findViewById(R.id.buttonCreatePoll);
        //button.setOnClickListener(this);
        return inflater.inflate(R.layout.createpoll, container, false);

    }

    @Override
    public void onClick(View v) {

    }
}
