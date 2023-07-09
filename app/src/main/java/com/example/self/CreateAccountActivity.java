package com.example.self;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import Util.JournalAPI;

public class CreateAccountActivity extends AppCompatActivity {

    private Button btnCreateAccount;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;
    private EditText etEmailAccount;
    private EditText etPasswordAccount;
    private EditText etUsernameAccount;
    private ProgressBar progressBar;

    //Firestore Connection;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private CollectionReference collectionReference = db.collection("Users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        Objects.requireNonNull(getSupportActionBar()).setElevation(0);
        //Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();

        btnCreateAccount = findViewById(R.id.btnCreateAcc);
        progressBar = findViewById(R.id.createAccountProgressBar);
        etEmailAccount = findViewById(R.id.etEmailAccount);
        etPasswordAccount = findViewById(R.id.etPasswordAccount);
        etUsernameAccount = findViewById(R.id.etUsernameAccount);

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                currentUser = firebaseAuth.getCurrentUser();
                if (currentUser != null)
                {
                    //user is already logged in

                }
                else
                {
                    //no user yet
                }
            }
        };

        btnCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!TextUtils.isEmpty(etEmailAccount.getText().toString()) && !TextUtils.isEmpty(etPasswordAccount.getText().toString()) &&
                        !TextUtils.isEmpty(etUsernameAccount.getText().toString())) {
                    String email = etEmailAccount.getText().toString().trim();
                    String password = etPasswordAccount.getText().toString().trim();
                    String username = etUsernameAccount.getText().toString().trim();
                    createUserEmailAccount(email, password, username);
                }
                else
                {
                    Toast.makeText(CreateAccountActivity.this, "Empty fields not allowed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void createUserEmailAccount(String email, String password, String username)
    {
        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(username))
        {
            progressBar.setVisibility(View.VISIBLE);
            firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful())
                    {
                        //we take user to addJournal Activity
                        currentUser = firebaseAuth.getCurrentUser();
                        assert currentUser != null;
                        String currentUserID = currentUser.getUid();
                        
                        //Create a user MAP so we can create a user in Collection
                        Map<String, String> userObj = new HashMap<>();
                        userObj.put("User ID", currentUserID);
                        userObj.put("User Name", username);

                        //Save to firestore database
                        collectionReference.add(userObj)
                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {

                                        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if (Objects.requireNonNull(task.getResult()).exists())
                                                {
                                                    progressBar.setVisibility(View.INVISIBLE);
                                                    String name = task.getResult().getString("username");

                                                    JournalAPI journalAPI = JournalAPI.getInstance();
                                                    journalAPI.setUserName(name);
                                                    journalAPI.setUserID(currentUserID);

                                                    //Moving to the Feed
                                                    Intent intent = new Intent(CreateAccountActivity.this, PostJournalActivity.class);
                                                    intent.putExtra("username", name);
                                                    intent.putExtra("userID", currentUserID);
                                                    startActivity(intent);
                                                }
                                                else
                                                {
                                                    progressBar.setVisibility(View.INVISIBLE);
                                                }
                                            }
                                        });
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                    }
                                });

                    }
                    else
                    {
                        //something went wrong
                    }
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        //Check if user is signed in
        currentUser = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);

    }
}