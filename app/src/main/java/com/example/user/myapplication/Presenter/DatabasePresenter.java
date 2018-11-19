package com.example.user.myapplication.Presenter;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import com.example.user.myapplication.Presenter.Interface.IDatabasePresenter;
import com.example.user.myapplication.View.IDatabaseView;
import com.example.user.myapplication.model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;

import androidx.annotation.NonNull;

public class DatabasePresenter extends BasePresenter<IDatabaseView> implements IDatabasePresenter{

    private static DatabasePresenter instance = new DatabasePresenter();

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseUsers;
    private StorageReference storageRef;

    private User currentUser;

    private DatabasePresenter(){
        databaseUsers = FirebaseDatabase.getInstance().getReference();
        storageRef = FirebaseStorage.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();
    }

    public static DatabasePresenter getInstance(){
        return instance;
    }

    @Override
    public DatabaseReference getDatabaseUsers(){
        return databaseUsers;
    }
    @Override
    public FirebaseAuth getFirebaseAuth(){
        return firebaseAuth;
    }
    @Override
    public User getCurrentUser(){
        return currentUser;
    }

    @Override
    public void saveUserToDatabase(User userInfo){
        FirebaseUser user = firebaseAuth.getCurrentUser();
        Log.d("myLogs", "Save user" + user.getUid());
        Log.d("myLogs", "User" + userInfo.getEmail());
        databaseUsers.child(user.getUid()).setValue(userInfo);
        Log.d("myLogs", "Save user" + user.getEmail());
    }

    @Override
    public void uploadImageToFirebaseStorage(String img_path){
        ProgressDialog progressBar = view.getProgressDialog();
        //progressBar.setMessage("Uploading...");
        //progressBar.show();
        final FirebaseUser user = firebaseAuth.getCurrentUser();
        Uri file = Uri.fromFile(new File(img_path));
        String new_file_path =  String.format("profile_images/users/%s/profile_icon.jpg", user.getUid());
        StorageReference image_storage = storageRef.child(new_file_path);

        image_storage.putFile(file)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        view.onSuccessMessage("Upload image success");
                        //progressBar.dismiss();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        view.onErrorMessage("Upload image failed");
                        //progressBar.dismiss();
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        //progressBar.setMessage("Uploaded " + ((int) progress) + "%...");
                    }
                })
                .addOnPausedListener(new OnPausedListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onPaused(UploadTask.TaskSnapshot taskSnapshot) {
                        System.out.println("Upload is paused!");
                    }
                });
    }

    @Override
    public void downloadFromFirebaseStorage() {
        final ProgressDialog progressBar = view.getProgressDialog();
        final FirebaseUser user = firebaseAuth.getCurrentUser();
        String new_file_path =  String.format("profile_images/users/%s/profile_icon.jpg", user.getUid());
        StorageReference image_storage = storageRef.child(new_file_path);
        if (image_storage != null) {
            progressBar.setTitle("Downloading...");
            progressBar.setMessage(null);
            progressBar.show();
            try {
                final File localFile = File.createTempFile("images", ".jpg");

                image_storage.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        Bitmap bmp = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                        view.setProfileImg(bmp);
                        view.saveUser();
                        progressBar.dismiss();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        progressBar.dismiss();
                        view.onErrorMessage("Download failed. Check internet connection");
                    }
                }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                        progressBar.setMessage("Downloaded " + ((int) progress) + "%...");
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            view.onErrorMessage("Upload file before downloading");
        }
    }

    @Override
    public void loadUserInformationMenu(){
        final FirebaseUser user = firebaseAuth.getCurrentUser();
        databaseUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User userInfo = dataSnapshot.child(user.getUid()).getValue(User.class);
                currentUser = userInfo;
                if(userInfo != null) {
                    view.setUserInfo(userInfo);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                view.onErrorMessage("Load user information failed");
            }
        });
    }

}