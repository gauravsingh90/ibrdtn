package com.group5.networking.slowpoll;

import android.database.sqlite.SQLiteException;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

/**
 * Created by Joe on 4/9/2018.
 */

public class ArchivedPolls extends Fragment implements View.OnClickListener {
    public PollController controller;
    public TextView textView;
    public Button clearPollsButton;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Archived Polls");
        textView = (TextView) getView().findViewById(R.id.listAllPolls);
        clearPollsButton = (Button) getView().findViewById(R.id.deleteAllPolls);
        controller = new PollController(getContext());

        clearPollsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    //Log.w("testing", "we made it fam");
                    //controller.delete_all_polls();
                    Toast.makeText(getActivity(), "Records Deleted", Toast.LENGTH_SHORT).show();
                    Fragment fragment = new ArchivedPolls();
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.replace(R.id.content_main, fragment);
                    ft.commit();
                }catch(SQLiteException e){
                    Toast.makeText(getActivity(), "error", Toast.LENGTH_SHORT).show();
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
