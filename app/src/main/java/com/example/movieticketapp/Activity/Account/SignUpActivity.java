package com.example.movieticketapp.Activity.Account;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.movieticketapp.Firebase.FirebaseRequest;
import com.example.movieticketapp.Model.Users;
import com.example.movieticketapp.NetworkChangeListener;
import com.example.movieticketapp.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class SignUpActivity extends AppCompatActivity {

    NetworkChangeListener networkChangeListener = new NetworkChangeListener();
    StorageReference storageReference = FirebaseStorage.getInstance().getReference();

    // UI
    android.widget.EditText fullNameET;
    android.widget.EditText emailET;
    android.widget.EditText passwordET;
    android.widget.EditText confirmPasswordET;
    TextInputLayout nameLayout, emailLayout, passwordLayout, confirmPasswordLayout;
    Uri avatarUri = null;
    String avatarUrl;
    String fullname;

    @Override
    protected void onStart() {
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeListener, filter);
        super.onStart();
    }

    @Override
    protected void onStop() {
        unregisterReceiver(networkChangeListener);
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up_screen);

        ImageView imageView = findViewById(R.id.addimage);
        ImageView imageAvatar = findViewById(R.id.avatarprofile);
        fullNameET = findViewById(R.id.fullname);
        emailET = findViewById(R.id.emailaddress);
        passwordET = findViewById(R.id.password);
        confirmPasswordET = findViewById(R.id.confirmpassword);
        nameLayout = findViewById(R.id.nameLayout);
        emailLayout = findViewById(R.id.emailLayout);
        passwordLayout = findViewById(R.id.layoutPassword);
        confirmPasswordLayout = findViewById(R.id.layoutConfirmPassword);

        // 🖼 chọn ảnh đại diện
        ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
                registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                    if (uri != null) {
                        imageAvatar.setImageURI(uri);
                        avatarUri = uri;
                    } else {
                        Log.d("PhotoPicker", "No media selected");
                    }
                });
        imageView.setOnClickListener(view -> pickMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build()));

        // 🔙 nút back
        Button backBt = findViewById(R.id.backbutton);
        backBt.setOnClickListener(view -> finish());

        LinearLayout layoutElement = findViewById(R.id.SignUpLayout);
        layoutElement.setOnClickListener(v -> {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        });

        // 📝 clear error khi nhập lại
        fullNameET.addTextChangedListener(clearError(nameLayout));
        emailET.addTextChangedListener(clearError(emailLayout));
        passwordET.addTextChangedListener(clearError(passwordLayout));
        confirmPasswordET.addTextChangedListener(clearError(confirmPasswordLayout));

        Button signUpBt = findViewById(R.id.SignUpBtn);
        signUpBt.setOnClickListener(view -> {
            boolean error = false;
            if (fullNameET.length() == 0) {
                nameLayout.setError("Full Name is not empty!!!");
                error = true;
            }
            if (emailET.length() == 0) {
                emailLayout.setError("Email is not empty!!!");
                error = true;
            }
            if (passwordET.length() == 0) {
                passwordLayout.setError("Password is not empty!!!");
                error = true;
            } else if (passwordET.length() < 6) {
                passwordLayout.setError("Password should be at least 6 characters!!!");
                error = true;
            }
            if (!confirmPasswordET.getText().toString().equals(passwordET.getText().toString())) {
                confirmPasswordLayout.setError("Password and confirmation passwords are not equals !!!");
                error = true;
            }

            if (!error) {
                fullname = fullNameET.getText().toString();
                Calendar calFordData = Calendar.getInstance();
                SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
                String saveCurrentData = currentDate.format(calFordData.getTime());

                SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
                String saveCurrentTime = currentTime.format(calFordData.getTime());

                String postRandomName = saveCurrentData + saveCurrentTime;

                if (avatarUri != null) {
                    StorageReference fileRef = storageReference.child(postRandomName + "as.jpg");
                    UploadTask uploadTask = fileRef.putFile(avatarUri);
                    Task<Uri> urlTask = uploadTask.continueWithTask(task -> {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        return fileRef.getDownloadUrl();
                    }).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            avatarUrl = task.getResult().toString();
                            CreateUser(emailET.getText().toString(), passwordET.getText().toString(), fullNameET.getText().toString(), avatarUrl);
                        } else {
                            Toast.makeText(getApplicationContext(), "ERROR!!!", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    CreateUser(emailET.getText().toString(), passwordET.getText().toString(), fullNameET.getText().toString(), null);
                }
            }
        });
    }

    private TextWatcher clearError(TextInputLayout layout) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                layout.setError(null);
            }

            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {}
        };
    }

    // ✅ Hàm tạo tài khoản có check admin
    void CreateUser(String email, String password, String Name, @Nullable String url) {
        String urlAvatar;
        if (url == null) {
            urlAvatar = "https://firebasestorage.googleapis.com/v0/b/movie-ticket-app-0.appspot.com/o/avatar.png?alt=media&token=23a1d250-ca27-414b-a46b-bbef69dac7da";
        } else {
            urlAvatar = url;
        }

        FirebaseRequest.mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = FirebaseRequest.mAuth.getCurrentUser();
                        UpdatePhotho(urlAvatar);

                        // ✅ Nếu email là admin -> set quyền admin
                        String accountType = email.equals("admin@gmail.com") ? "admin" : "user";

                        Users u = new Users(
                                user.getUid(),
                                Name,
                                email,
                                0,
                                accountType,
                                urlAvatar,
                                new ArrayList<>(),
                                new ArrayList<>()
                        );

                        FirebaseRequest.database.collection("Users")
                                .document(user.getUid())
                                .set(u.toJson())
                                .addOnSuccessListener(aVoid -> Log.d(TAG, "Admin account created"))
                                .addOnFailureListener(e -> Log.w(TAG, "Error writing document", e));

                        Users.currentUser = u;
                        Intent i = new Intent(getApplicationContext(), ConfirmationProfileActivity.class);
                        startActivity(i);
                    } else {
                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                        Toast.makeText(SignUpActivity.this, "The email had used.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void UpdatePhotho(String urlAvatar) {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setPhotoUri(Uri.parse(urlAvatar)).setDisplayName(fullname)
                .build();

        FirebaseRequest.mAuth.getCurrentUser().updateProfile(profileUpdates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Profile updated");
                    }
                });
    }
}
