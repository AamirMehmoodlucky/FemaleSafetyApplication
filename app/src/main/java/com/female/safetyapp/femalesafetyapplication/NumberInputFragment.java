package com.female.safetyapp.femalesafetyapplication;


import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.telephony.PhoneNumberFormattingTextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;


/**
 * A simple {@link Fragment} subclass.
 */
public class NumberInputFragment extends Fragment implements View.OnClickListener {

    EditText phoneNumberEditText;

    public NumberInputFragment() {
        // Required empty public constructor
    }

    @Override
    public void onClick(View view) {
        if (listener != null) {
            listener.viewClicked(view.getId());
        }
    }

    interface NumberInputFragListener {
        void viewClicked(long id);
    }

    private NumberInputFragListener listener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_number_input, container, false);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.listener = (NumberInputFragListener) context;
    }

    @Override
    public void onStart() {
        super.onStart();
        View view = getView();
        if (view != null) {
            phoneNumberEditText = view.findViewById(R.id.edittext_registration_phone_number);
            phoneNumberEditText.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
            Button registerButton = view.findViewById(R.id.button_register);
            registerButton.setOnClickListener(this);
        }
    }

    String getPhoneNumber() {
        String phoneNumber = phoneNumberEditText.getText().toString().trim();
        if (phoneNumber.equalsIgnoreCase("")) {
            phoneNumberEditText.setError("Number Required");
            return null;
        } else if (phoneNumber.length() < 10) {
            phoneNumberEditText.setError("Phone Number invalid");
            return null;
        }
        if (phoneNumber.startsWith("0")) {
            phoneNumber = "+92" + phoneNumber.substring(1);
            Log.d("", "-----------------getPhoneNumber: "+phoneNumber);
        }
        return phoneNumber;
    }
}
