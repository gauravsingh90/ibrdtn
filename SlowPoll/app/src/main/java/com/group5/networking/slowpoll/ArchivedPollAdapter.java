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


import org.w3c.dom.Text;

import java.util.ArrayList;

/**
 * Created by Joe on 4/24/2018.
 * Entire class works offline using local storage - no Firebase connections initialized
 */



public class ArchivedPollAdapter  extends BaseAdapter implements ListAdapter {
    public ArrayList<String> list = new ArrayList<String>();
    public ArrayList<String> keysList = new ArrayList<String>();
    public ArrayList<String> optOneList = new ArrayList<String>();
    public ArrayList<String> optTwoList = new ArrayList<String>();
    public ArrayList<String> incentiveList = new ArrayList<String>();
    public ArrayList<Integer> responseOneList = new ArrayList<Integer>();
    public ArrayList<Integer> responseTwoList = new ArrayList<Integer>();
    public ArrayList<Integer> answeredList = new ArrayList<Integer>();
    public PollController controller;
    public Context context;


    public ArchivedPollAdapter(ArrayList<String> list, Context context, ArrayList<String> optOneList, ArrayList<String> optTwoList, ArrayList<String> keysList,ArrayList<String> incentiveList, ArrayList<Integer> responseOneList, ArrayList<Integer> responseTwoList, ArrayList<Integer> answeredList) {
        this.list = list;
        this.context = context;
        this.optOneList = optOneList;
        this.optTwoList = optTwoList;
        this.keysList = keysList;
        this.incentiveList = incentiveList;
        this.responseOneList = responseOneList;
        this.responseTwoList = responseTwoList;
        this.answeredList = answeredList;
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
            view = inflater.inflate(R.layout.single_archived_poll_list, null);
        }

        //local database connection
        controller = new PollController(context);
        //Handle TextView and display string from your list
        TextView listItemText = (TextView) view.findViewById(R.id.list_item_string);
        listItemText.setText(list.get(position));

        //Buttons to display stats
        Button optTwoBtn = (Button) view.findViewById(R.id.optTwoArchived);
        optTwoBtn.setText(optTwoList.get(position) + " - " + calculatePercentage(responseTwoList.get(position), responseOneList.get(position))  + " (" + responseTwoList.get(position) + " responses)");
        optTwoBtn.setEnabled(false);
        Button optOneBtn = (Button) view.findViewById(R.id.optOneArchived);
        optOneBtn.setText(optOneList.get(position) + " - " + calculatePercentage(responseOneList.get(position), responseTwoList.get(position))  + " (" + responseOneList.get(position) + " responses)");
        optOneBtn.setEnabled(false);
        if(answeredList.get(position) == 1){
            optOneBtn.setBackgroundColor(Color.parseColor ("#b3e6c9"));
            optOneBtn.setTextColor(Color.BLACK);
            optTwoBtn.setTextColor(Color.BLACK);
        }
        else{
            optTwoBtn.setBackgroundColor(Color.parseColor ("#b3e6c9"));
            optTwoBtn.setTextColor(Color.BLACK);
            optOneBtn.setTextColor(Color.BLACK);

        }
        TextView listItemIncentive = (TextView) view.findViewById(R.id.list_item_incentive);
        listItemIncentive.setText("Incentive: " + incentiveList.get(position));
        return view;
    }

    private String calculatePercentage(Integer a, Integer b) {
        double temp = a + b;
        temp = a/temp;
        return Double.toString((Math.round(temp*10000.0)/100.0)) + "%";
    }
}
