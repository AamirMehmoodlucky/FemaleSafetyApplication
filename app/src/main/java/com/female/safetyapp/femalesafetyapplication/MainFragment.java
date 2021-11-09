package com.female.safetyapp.femalesafetyapplication;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;


public class MainFragment extends Fragment implements View.OnClickListener {

    private OnFragmentInteractionListener mListener;

    private Context context;

    ListView contactsList;
    static ArrayList<Contact> contactsArray;
    static ContactsAdapter contactsAdapter;

    public MainFragment() {

    }

    MainFragment(Context context) {
        this.context = context;

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        contactsArray = new ArrayList<>();
        try {
            SQLiteOpenHelper databaseHelper = new FemaleSafetyAppDatabaseHelper(context);
            SQLiteDatabase sqLiteDatabase = databaseHelper.getReadableDatabase();
            if (contactsArray.isEmpty()) {
                Cursor cursor = sqLiteDatabase.query("CONTACTS",
                        new String[]{"CONTACT_NAME", "CONTACT_NUMBER"},
                        null,
                        null,
                        null,
                        null,
                        null);
                cursor.moveToFirst();
                for (int i=0; i < cursor.getCount(); i++){
                    contactsArray.add(new Contact(cursor.getString(0), cursor.getString(1)));
                    cursor.moveToNext();
                }
                cursor.close();
            }

            //sqLiteDatabase.close();
        } catch (SQLException e) {
            Toast.makeText(context, "Database unavailable: - "+e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        contactsList = view.findViewById(R.id.list_contacts);
        contactsList.setEmptyView(view.findViewById(R.id.when_empty_list));
        contactsAdapter = new ContactsAdapter(context, contactsArray);
        contactsList.setAdapter(contactsAdapter);
        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        contactsAdapter.notifyDataSetChanged();
    }

    @Override
    public void onStart() {
        super.onStart();
        View view = getView();
        if (view != null) {
            final ImageButton deleteButton = view.findViewById(R.id.button_delete_contact);
            deleteButton.setOnClickListener(this);
            view.findViewById(R.id.button_add_contact).setOnClickListener(this);
            view.findViewById(R.id.empty_list_add_button).setOnClickListener(this);
            contactsList.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);

        }
    }

    void deleteContact() {

        SQLiteOpenHelper databaseHelper = new FemaleSafetyAppDatabaseHelper(context);
        SQLiteDatabase sqLiteDatabase = databaseHelper.getWritableDatabase();
        SparseBooleanArray checked = contactsList.getCheckedItemPositions();
        int totalCount = contactsList.getCount();
        for (int i=totalCount; i>-1; i--) {
            if (checked.get(i)) {
                Log.d("","-----------------"+contactsArray.size()+"-----------------");
                Contact contact = contactsArray.get(i);
                deleteContactFromFirebase(contact);
                FemaleSafetyAppDatabaseHelper.deleteData(sqLiteDatabase,
                        contact.getContactName(), contact.getContactNumber());
                //item on position i remains checked so we uncheck position before removing item
                contactsList.setItemChecked(i,false);
                contactsArray.remove(i);
            }
        }
        //sqLiteDatabase.close();
        contactsAdapter.notifyDataSetChanged();
    }

    private void deleteContactFromFirebase(Contact contact) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference contactRef = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(currentUserId)
                .child("contacts")
                .child(getContactKey(contact));
        contactRef.removeValue();
    }

    private String getContactKey(Contact contact) {
        if (contact.getContactNumber().length()>10)
            return new StringBuffer(contact.getContactNumber())
                .reverse()
                .toString()
                .replace(" ", "")
                .replace("(","")
                .replace(")","")
                .replace("-","")
                .substring(0, 10);
        else return contact.getContactNumber();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View view) {
        mListener.onMainFragInteraction(view.getId());
       // Toast.makeText(getContext(), "clicked", Toast.LENGTH_SHORT).show();
    }


    public interface OnFragmentInteractionListener {
        void onMainFragInteraction(long viewId);
    }
}
