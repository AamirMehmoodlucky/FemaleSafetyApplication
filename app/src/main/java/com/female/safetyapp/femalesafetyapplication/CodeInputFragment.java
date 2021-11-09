package com.female.safetyapp.femalesafetyapplication;


import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;


/**
 * A simple {@link Fragment} subclass.
 */
public class CodeInputFragment extends Fragment implements View.OnClickListener {

    EditText codeEditText;
    String secretCode;

    @Override
    public void onClick(View view) {
        listener.viewListener(view.getId());
    }

    interface CodeInputFragListener {
        void viewListener(long id);
    }

    private CodeInputFragListener listener;

    public CodeInputFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_code_input, container, false);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        listener = (CodeInputFragListener) context;

    }

    @Override
    public void onStart() {
        super.onStart();
        View view = getView();
        if (view != null) {
            codeEditText = view.findViewById(R.id.edittext_secret_code);
            if (secretCode !=null) {
                codeEditText.setText(secretCode);
            }
            Button verifyButton = view.findViewById(R.id.button_verify);
            verifyButton.setOnClickListener(this);
        }
    }

    void setSecretCode(String secretCode) { this.secretCode = secretCode; }
    String getSecretCode() { return secretCode; }

}
