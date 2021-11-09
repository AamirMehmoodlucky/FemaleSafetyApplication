package com.female.safetyapp.femalesafetyapplication;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import android.widget.TextView;

import java.util.ArrayList;

public class ContactsAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<Contact> contactsArray;

    ContactsAdapter(Context context, ArrayList<Contact> contactsArray) {
        this.context = context;
        this.contactsArray = contactsArray;
    }

    @Override
    public int getCount() {
        return contactsArray.size();
    }

    @Override
    public Object getItem(int i) {
        return contactsArray.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    private class ViewHolder {
        private TextView name;
        private TextView number;
    }




    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (view == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            view = inflater.inflate(R.layout.layout_contacts, null);
            viewHolder = new ViewHolder();

            viewHolder.name = view.findViewById(R.id.layout_contact_name);
            viewHolder.number = view.findViewById(R.id.layout_contact_number);
            view.setTag(viewHolder);

        } else viewHolder = (ViewHolder) view.getTag();

        viewHolder.name.setText(contactsArray.get(i).getContactName());
        viewHolder.number.setText(contactsArray.get(i).getContactNumber());

        return view;
    }
}