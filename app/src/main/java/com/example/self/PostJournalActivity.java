package com.example.self;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Date;
import java.util.Objects;

import Util.JournalAPI;
import model.Journal;

public class PostJournalActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int GALLERY_CODE = 1;
    private Button btnPostSaveJournal;
    private ProgressBar postProgressBar;
    private ImageView ivAddPhotoButton;
    private EditText etPostTitle;
    private EditText etPostDescription;
    private TextView tvCurrentUser;
    private ImageView imageView;

    private String currentUserID;
    private String currentUserName;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;

    //connection to firestore
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference storageReference;

    private CollectionReference collectionReference = db.collection("model");
    private Uri imageURI;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_journal);

        Objects.requireNonNull(getSupportActionBar()).setElevation(0);
        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();
        postProgressBar = findViewById(R.id.postProgressBar);
        btnPostSaveJournal = findViewById(R.id.btnPostSaveJournal);
        ivAddPhotoButton =findViewById(R.id.ivPostCameraBtn);
        etPostTitle = findViewById(R.id.etPostTitle);
        etPostDescription = findViewById(R.id.etPostDescription);
        tvCurrentUser = findViewById(R.id.tvPostUsername);
        imageView = findViewById(R.id.post_imageView);

        postProgressBar.setVisibility(View.INVISIBLE);

        btnPostSaveJournal.setOnClickListener(this);
        ivAddPhotoButton.setOnClickListener(this);

        if (JournalAPI.getInstance() != null)
        {
            currentUserID = JournalAPI.getInstance().getUserID();
            currentUserName = JournalAPI.getInstance().getUserName();
            tvCurrentUser.setText(currentUserName);
        }

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if (user != null)
                {

                }
                else
                {

                }
            }
        };

    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.btnPostSaveJournal:
                //save journal
                saveJournal();
                break;
            case R.id.ivPostCameraBtn:
                //get image from gallery/phone
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_CODE);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_CODE && resultCode == RESULT_OK)
        {
            if (data != null)
            {
                //get Actual path to the image
                imageURI = data.getData();
                //show image
                imageView.setImageURI(imageURI);
            }
        }
    }

    private void saveJournal() {
        String title = etPostTitle.getText().toString().trim();
        String thought = etPostDescription.getText().toString().trim();
        postProgressBar.setVisibility(View.VISIBLE);
        if (!TextUtils.isEmpty(title) && !TextUtils.isEmpty(thought) && imageURI != null)
        {
            StorageReference filePath = storageReference.child("Journal Images").child("My Image " + Timestamp.now().getSeconds());
            filePath.putFile(imageURI).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String imageUrl = uri.toString();
                            //TODO: create a journal object-model
                            Journal journal = new Journal();
                            journal.setTitle(title);
                            journal.setThought(thought);
                            journal.setImageUrl(imageUrl);
                            journal.setTimeAdded(new Timestamp(new Date()));
                            journal.setUserName(currentUserName);
                            journal.setUserID(currentUserID);

                            //TODO: invoke our journal collectionReference
                            collectionReference.add(journal).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    postProgressBar.setVisibility(View.INVISIBLE);
                                    startActivity(new Intent(PostJournalActivity.this, JournalListActivity.class));
                                    finish();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d("PostJounralActivity", "onFailure " + e.getMessage());
                                }
                            });
                            //TODO: save a journal instance
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    postProgressBar.setVisibility(View.INVISIBLE);
                }
            });
        }
        else
        {

        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        user = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (firebaseAuth != null)
        {
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }
}