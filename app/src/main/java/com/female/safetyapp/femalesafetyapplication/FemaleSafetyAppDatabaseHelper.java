package com.female.safetyapp.femalesafetyapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class FemaleSafetyAppDatabaseHelper extends SQLiteOpenHelper {


    private static final String DB_NAME = "FemaleSafetyAppDatabase";
    private static final String Db_Table="CONTACTS";
    private static final int DB_VERSION = 1;
    private DatabaseReference firebaseDatabaseRef;
    //
    private Context context;

    //
    FemaleSafetyAppDatabaseHelper(@Nullable Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(final SQLiteDatabase sqLiteDatabase) {
        ProgressBar progressBar = new ProgressBar(context);
        progressBar.setHorizontalScrollBarEnabled(true);
        progressBar.setVisibility(View.VISIBLE);

        firebaseDatabaseRef = FirebaseDatabase.getInstance().getReference();
        //        updateMyDatabase (sqLiteDatabase, 0, DB_VERSION);
        sqLiteDatabase.execSQL("CREATE TABLE CONTACTS (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "CONTACT_NAME TEXT," +
                "CONTACT_NUMBER INTEGER);");
        Log.d("", "------------------------------onCreate: Creating Database SQL------------------------------");
        DatabaseReference currentUserRef = firebaseDatabaseRef.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        currentUserRef.child("contacts").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d("", "onDataChange: Fetching from Firebase");
                for (DataSnapshot contactsDataSnapshot : dataSnapshot.getChildren()) {
                    Contact contact = contactsDataSnapshot.getValue(Contact.class);
                    MainFragment.contactsArray.add(contact);
                    insertData(sqLiteDatabase, contact.getContactName(), contact.getContactNumber());
                    Log.d("", "onDataChange: " + contact.getContactName() + " " + contact.getContactNumber());
                }
                MainFragment.contactsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        //        updateMyDatabase(sqLiteDatabase, oldVersion, newVersion);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + Db_Table);

        // Create tables again
        onCreate(sqLiteDatabase);
    }


    static void insertData(SQLiteDatabase sqLiteDatabase, String contactName, String contactNumber) {
        try {
            ContentValues contact = new ContentValues();
            contact.put("CONTACT_NAME", contactName);
            contact.put("CONTACT_NUMBER", contactNumber);
            sqLiteDatabase.insert("CONTACTS", null, contact);
        } catch (Exception e) {

        }

    }

    static void deleteData(SQLiteDatabase database, String contactName, String contactNumber) {
        try {
            database.delete("CONTACTS", "CONTACT_NAME = ? AND CONTACT_NUMBER = ? ", new String[]{contactName, contactNumber});
        } catch (Exception e) {

        }
    }
}