package com.female.safetyapp.femalesafetyapplication;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.telephony.PhoneNumberFormattingTextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;


public class NewContactFragment extends Fragment implements View.OnClickListener {

    private OnFragmentInteractionListener mListener;
    private  Context context;

    public NewContactFragment(Context context) {
        this.context = context;
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_new_contact, container, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        View view = getView();
        if (view != null) {
            view.findViewById(R.id.button_save).setOnClickListener(this);
            view.findViewById(R.id.button_add_from_phonebook).setOnClickListener(this);
            EditText phoneNumber = view.findViewById(R.id.edittext_contact_phoneNumber);
            phoneNumber.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View view) {
        mListener.onNewContactFragInteraction(view.getId());
    }

    public interface OnFragmentInteractionListener {
        void onNewContactFragInteraction(long viewId);
    }

    void setNameField(String contactName){
        EditText editTextContactName = getView().findViewById(R.id.edittext_contact_name);
        editTextContactName.setText(contactName);

    }
    void setNumberField(String contactNumber){
        EditText editTextContactNumber = getView().findViewById(R.id.edittext_contact_phoneNumber);
        editTextContactNumber.setText(contactNumber);

    }

    private Contact getContactFromFields() {
        EditText editTextContactName = getView().findViewById(R.id.edittext_contact_name);
        EditText editTextContactNumber = getView().findViewById(R.id.edittext_contact_phoneNumber);
        String contactName = editTextContactName.getText().toString().trim();
        String contactNumber = editTextContactNumber.getText().toString().trim();

        if (contactName.equalsIgnoreCase("")) {
            editTextContactName.setError("Name Required");
            return null;
        }
        if (contactNumber.equalsIgnoreCase("")) {
            editTextContactNumber.setError("Number Required");
            return null;
        }

        return new Contact(contactName, contactNumber);
    }

    boolean saveContact() {
        Contact newContact = getContactFromFields();
        if (newContact == null) {
            return false;
        }
        String key;
        if (newContact.getContactNumber().length()>10)
        key = new StringBuffer(newContact.getContactNumber())
                .reverse()
                .toString()
                .replace(" ","")
                .replace("(","")
                .replace(")","")
                .replace("-","")
                .substring(0,10);
        else key = newContact.getContactNumber();
        Log.d("", "------------------------------saveContact: ------------------------------"+ Arrays.toString(newContact.getContactNumber().replaceAll("[^a-zA-Z ]", "").toLowerCase().split("\\s+")));
        FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("contacts")
                .child(key).setValue(newContact);
        SQLiteOpenHelper databaseHelper = new FemaleSafetyAppDatabaseHelper(context);
        SQLiteDatabase sqLiteDatabase = databaseHelper.getWritableDatabase();
        FemaleSafetyAppDatabaseHelper.insertData(sqLiteDatabase,
                newContact.getContactName(),
                newContact.getContactNumber());
        MainFragment.contactsArray.add(newContact);
        //sqLiteDatabase.close();
        return true;
    }

}
