package com.inquid.develop.inquidstoriesandroid;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

public class StoriesActivity extends Activity {
    private static final Integer[] XMEN = {R.drawable.story_1, R.drawable.story_3, R.drawable.story_4, R.drawable.story_5, R.drawable.story_6};
    private ArrayList<Integer> XMENArray = new ArrayList<>();
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private StorageReference mStorageRef;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final String LOG_TAG = "Barcode Scanner API";
    private static final int PHOTO_REQUEST = 10;
    private Uri imageuri;
    private static final int REQUEST_WRITE_PERMISSION = 20;
    private static final String SAVED_INSTANCE_URI = "uri";
    private static final String SAVED_INSTANCE_RESULT = "result";
    ProgressDialog progressDialog;
    ImageView imageView;
    private String userName = "gogl92";
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stories);
        imageView = findViewById(R.id.imageview);
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    userId = user.getUid();
                    Log.d("STORIES_ACTIVITY", "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d("STORIES_ACTIVITY", "onAuthStateChanged:signed_out");
                }
            }
        };
        mStorageRef = FirebaseStorage.getInstance().getReference();
        progressDialog = new ProgressDialog(StoriesActivity.this);
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    public void viewStory(View view) {
        startActivity(new Intent(StoriesActivity.this, MainActivity.class));
    }

    public void uploadStory(View view) {
        mAuth.signInWithEmailAndPassword("luisarmando1234@gmail.com", "123456")
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d("STORIES_ACTIVITY", "signInWithEmail:onComplete:" + task.isSuccessful());
                        if (task.isSuccessful()) {
                            ActivityCompat.requestPermissions(StoriesActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_PERMISSION);
                        }
                        if (!task.isSuccessful()) {
                            Log.w("STORIES_ACTIVITY", "signInWithEmail:failed", task.getException());
                            Toast.makeText(StoriesActivity.this, R.string.auth_failed,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {

            case REQUEST_WRITE_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    takePicture();
                } else {
                    Toast.makeText(StoriesActivity.this, "Permission Denied!" + requestCode, Toast.LENGTH_SHORT).show();
                }


        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PHOTO_REQUEST && resultCode == RESULT_OK) {
            launchMediaScanIntent();
            try {
                Scanner scanner = new Scanner();
                final Bitmap bitmap = scanner.decodeBitmapUri(StoriesActivity.this, imageuri);
                //progressDialog.setTitle("Uploading..");
                //progressDialog.show();
                StorageReference filepath = mStorageRef.child("users").child(userId).child("stories").child(imageuri.getLastPathSegment());
                filepath.putFile(imageuri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        imageView.setImageBitmap(bitmap);
                        Toast.makeText(StoriesActivity.this, "uploaded", Toast.LENGTH_LONG).show();
                        //progressDialog.dismiss();
                    }
                });
            } catch (Exception e) {
                Toast.makeText(this, "Failed to load Story", Toast.LENGTH_SHORT)
                        .show();
                Log.e(LOG_TAG, e.toString());
            }
        }
    }

    public void takePicture() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Date date = new Date();
        File photo = new File(Environment.getExternalStorageDirectory(), "story" + "_" + userId + "_" + date.getTime() + ".jpg");
        imageuri = Uri.fromFile(photo);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageuri);
        startActivityForResult(intent, PHOTO_REQUEST);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (imageuri != null) {
            outState.putString(SAVED_INSTANCE_URI, imageuri.toString());
        }
        super.onSaveInstanceState(outState);
    }

    private void launchMediaScanIntent() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(imageuri);
        this.sendBroadcast(mediaScanIntent);
    }
}
