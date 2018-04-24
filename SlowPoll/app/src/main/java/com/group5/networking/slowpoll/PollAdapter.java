package com.group5.networking.slowpoll;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Joe on 4/24/2018.
 */

public class PollAdapter  extends BaseAdapter implements ListAdapter {
    private ArrayList<String> list = new ArrayList<String>();
    private ArrayList<String> keysList = new ArrayList<String>();
    private ArrayList<String> optOneList = new ArrayList<String>();
    private ArrayList<String> optTwoList = new ArrayList<String>();
    private Context context;



    public PollAdapter(ArrayList<String> list, Context context, ArrayList<String> optOneList, ArrayList<String> optTwoList, ArrayList<String> keysList) {
        this.list = list;
        this.context = context;
        this.optOneList = optOneList;
        this.optTwoList = optTwoList;
        this.keysList = keysList;
    }

    public void incrementResponseValue(String title, String option){

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
        TextView listItemText = (TextView) view.findViewById(R.id.list_item_string);
        listItemText.setText(list.get(position));

        //Handle buttons and add onClickListeners
        Button optTwoBtn = (Button) view.findViewById(R.id.optionTwoBtn);
        optTwoBtn.setText(optTwoList.get(position));
        Button optOneBtn = (Button) view.findViewById(R.id.optionOneBtn);
        optOneBtn.setText(optOneList.get(position));

        optOneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                incrementResponseValue(list.get(position), optOneList.get(position));
                notifyDataSetChanged();
            }
        });
        optTwoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                incrementResponseValue(list.get(position), optTwoList.get(position));
                notifyDataSetChanged();
            }
        });


        return view;
    }
}
