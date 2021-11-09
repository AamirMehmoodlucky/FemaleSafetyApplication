package com.female.safetyapp.femalesafetyapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
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

public class UserRegistration extends AppCompatActivity {



    Button btnRegister;
    EditText edtPhoneNumber;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_registration);


        btnRegister=findViewById(R.id.button_register);
        edtPhoneNumber=findViewById(R.id.edittext_registration_phone_number);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(edtPhoneNumber.getText().toString().length()>9){
                    Intent intent=new Intent(UserRegistration.this,InputCodeActivity.class);
                    intent.putExtra("phoneNum",edtPhoneNumber.getText().toString());
                    startActivity(intent);
                }
            }
        });

    }

//
//    @Override
//    public void viewClicked(long id) {
//        if (id == R.id.button_register) {
//            String phoneNumber = numberInputFragment.getPhoneNumber();
//            if (phoneNumber == null) return;
//            sendVerificationCode(phoneNumber);
//        }
//    }
//
//    public void sendVerificationCode(String phoneNumber) {
//        try {
//            Toast.makeText(this, "Please Wait...", Toast.LENGTH_SHORT).show();
//            PhoneAuthOptions options =
//                    PhoneAuthOptions.newBuilder(mAuth)
//                            .setPhoneNumber(phoneNumber)       // Phone number to verify
//                            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
//                            .setActivity(this)                 // Activity (for callback binding)
//                            .setCallbacks(mCallback)          // OnVerificationStateChangedCallbacks
//                            .build();
//            PhoneAuthProvider.verifyPhoneNumber(options);
//        } catch (Exception e) {
//            Log.e("failed", e.getMessage().toString());
//        }
//
//    }
//
//    private final PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallback = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
//
//        @Override
//        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
//            super.onCodeSent(s, forceResendingToken);
//            verificationId = s;
//        }
//
//        @Override
//        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
//            try {
//                codeInputFragment = new CodeInputFragment();
//                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//                transaction.replace(R.id.user_reg_frag_container, codeInputFragment);
//                transaction.addToBackStack(null);
//                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
//                transaction.commit();
//                String code = phoneAuthCredential.getSmsCode();
//                 if (code != null) {
//                    codeInputFragment.setSecretCode(code);
//                    verifyCode(code);
//                }
//            } catch (Exception e) {
//                Log.e("authFailed", e.getMessage());
//            }
//
//        }
//
//        @Override
//        public void onVerificationFailed(@NonNull FirebaseException e) {
//            Toast.makeText(UserRegistration.this, "Failed! Please try again later.", Toast.LENGTH_LONG).show();
//            Log.e("phoneAuth", e.toString());
//        }
//    };
//
//    private void verifyCode(String secretCode) {
//        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, secretCode);
//
//        signInWithCredentials(credential);
//    }
//
//    private void signInWithCredentials(PhoneAuthCredential credential) {
//        mAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
//            @Override
//            public void onComplete(@NonNull Task<AuthResult> task) {
//                if (task.isSuccessful()) {
//                    openMainActivity();
//                } else {
//                    Toast.makeText(UserRegistration.this, "Sign In Failed" + task.getException().getMessage(), Toast.LENGTH_LONG).show();
//                }
//            }
//        });
//    }
//
//    @Override
//    public void viewListener(long id) {
//        if (id == R.id.button_verify) {
//            String secretCode = codeInputFragment.getSecretCode();
//            verifyCode(secretCode);
//        }
//    }
//
//    @Override
//    protected void onStart() {
//        super.onStart();
//        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
//            openMainActivity();
//        }
//    }
//
//    private void openMainActivity() {
//        Intent mainActivityIntent = new Intent(UserRegistration.this, MainActivity.class);
//        mainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        startActivity(mainActivityIntent);
//    }
}
