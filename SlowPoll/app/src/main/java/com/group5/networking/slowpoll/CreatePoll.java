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

import org.w3c.dom.Text;

/**
 * Created by Joe on 4/9/2018.
 */

public class CreatePoll extends Fragment implements View.OnClickListener {

    public EditText title, optionOne, optionTwo;
    public DB_Controller controller;
    public Button button;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getActivity().setTitle("Create a Poll");
        title = (EditText) getView().findViewById(R.id.pollTitle);
        optionOne = (EditText) getView().findViewById(R.id.optionOne);
        optionTwo = (EditText) getView().findViewById(R.id.optionTwo);
        button = (Button) getView().findViewById(R.id.buttonCreatePoll);
        //textView = (TextView) getView().findViewById(R.id.listAllPolls);
        controller = new DB_Controller(getActivity(), "",null, 1);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    controller.insert_poll(title.getText().toString(),optionOne.getText().toString(),optionTwo.getText().toString());
                    //Log.w("testing", "we made it fam");
                    Toast.makeText(getActivity(), "Poll Created", Toast.LENGTH_SHORT).show();
                    Fragment fragment = new BrowsePolls();
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.replace(R.id.content_main, fragment);
                    ft.commit();
                }catch(SQLiteException e){
                    Toast.makeText(getActivity(), "ALREADY EXISTS", Toast.LENGTH_SHORT).show();
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
