package com.female.safetyapp.femalesafetyapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class InputCodeActivity extends AppCompatActivity {

    Button btnVerify;
    EditText edtVerifyCode;
    private String verificationId;

    private NumberInputFragment numberInputFragment;
    private CodeInputFragment codeInputFragment = null;

    private static final String TAG = "PhoneAuthActivity";

    // [START declare_auth]
    private FirebaseAuth mAuth;
    // [END declare_auth]

    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    String phoneNumber;
    ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_code);

        btnVerify = findViewById(R.id.button_verify);
        edtVerifyCode = findViewById(R.id.edittext_secret_code);
        progressBar = findViewById(R.id.progress_auth);
        mAuth = FirebaseAuth.getInstance();
        phoneNumber = getIntent().getStringExtra("phoneNum");
        sendVerificationCode(phoneNumber);
        btnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(edtVerifyCode.getText().toString())) {
                    //if the OTP text field is empty display a message to user to enter OTP
                    Toast.makeText(InputCodeActivity.this, "Please enter OTP", Toast.LENGTH_SHORT).show();
                } else {
                    //if OTP field is not empty calling method to verify the OTP.
                    verifyCode(edtVerifyCode.getText().toString());
                }
            }
        });


    }

    private void signInWithCredential(PhoneAuthCredential credential) {
        //inside this method we are checking if the code entered is correct or not.
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //if the code is correct and the task is succesful we are sending our user to new activity.

                            Intent intent = new Intent(InputCodeActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finishAffinity();

                        } else {
                            //if the code is not correct then we are displaying an error message to the user.
                            Toast.makeText(InputCodeActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }


    private void sendVerificationCode(String number) {
        //this method is used for getting OTP on user phone number.

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                number,//first parameter is user's mobile number
                60,//second parameter is time limit for OTP verification which is 60 seconds in our case.
                TimeUnit.SECONDS,// third parameter is for initializing units for time period which is in seconds in our case.
                TaskExecutors.MAIN_THREAD,//this task will be excuted on Main thread.
                mCallBack//we are calling callback method when we recieve OTP for auto verification of user.
        );

    }

    //callback method is called on Phone auth provider.
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks
            //initializing our callbacks for on verification callback method.
            mCallBack = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        //below method is used when OTP is sent from Firebase
        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            //when we recieve the OTP it contains a unique id wich we are storing in our string which we have already created.
            verificationId = s;
        }

        //this method is called when user recieve OTP from Firebase.
        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            //below line is used for getting OTP code which is sent in phone auth credentials.
            final String code = phoneAuthCredential.getSmsCode();
            //checking if the code is null or not.
//            if (code != null) {
//                verifyCode(code);
//                edtVerifyCode.setText(code);
            progressBar.setVisibility(View.GONE);
//            }
             signInWithCredential(phoneAuthCredential);
//            if (code != null) {
//                //if the code is not null then we are setting that code to our OTP edittext field.
//                edtVerifyCode.setText(code);
//                //after setting this code to OTP edittext field we are calling our verifycode method.
//                verifyCode(code);
//
//            }

        }

        //thid method is called when firebase doesnot sends our OTP code due to any error or issue.
        @Override
        public void onVerificationFailed(FirebaseException e) {
            //displaying error message with firebase exception.
            Toast.makeText(InputCodeActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    };

    //below method is use to verify code from Firebase.
    private void verifyCode(String code) {
        //below line is used for getting getting credentials from our verification id and code.
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        //after getting credential we are calling sign in method.
        signInWithCredential(credential);
    }


}


//