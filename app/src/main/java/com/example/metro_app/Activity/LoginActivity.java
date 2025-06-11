package com.example.metro_app.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.metro_app.Activity.Admin.AdHomeActivity;
import com.example.metro_app.Activity.User.HomeActivity;
import com.example.metro_app.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    private String CCCD;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final int RC_SIGN_IN = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // Ensure this is set in strings.xml
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        Button googleSignInButton = findViewById(R.id.Google);

        googleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInWithGoogle();
            }
        });
    }

    private void signInWithGoogle() {
        mGoogleSignInClient.signOut().addOnCompleteListener(task -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    firebaseAuthWithGoogle(account.getIdToken());
                }
            } catch (ApiException e) {
                Toast.makeText(this, "Google Sign-In failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                String uid = user.getUid();
                                db.collection("Account").document(uid)
                                        .get()
                                        .addOnSuccessListener(documentSnapshot -> {
                                            if (documentSnapshot.exists()) {
                                                String role = documentSnapshot.getString("Role");
                                                if ("Admin".equals(role)) {
                                                    saveUserInfo(user);
                                                    startActivity(new Intent(LoginActivity.this, AdHomeActivity.class));
                                                } else {
                                                    saveUserInfo(user);
                                                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                                    intent.putExtra("UUID", uid); // Truyền UUID sang HomeActivity
                                                    startActivity(intent);
                                                }
                                                Toast.makeText(LoginActivity.this, "Google Sign-In successful!", Toast.LENGTH_SHORT).show();
                                                finish();
                                            } else {
                                                Map<String, Object> data = new HashMap<>();
                                                data.put("Name", user.getDisplayName());
                                                data.put("Email", user.getEmail());
                                                data.put("Role", "User"); // mặc định là user
                                                data.put("CCCD", "");
                                                data.put("avatarUrl", user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "");

// Thêm dòng sau để lưu thời gian đăng nhập đầu tiên
                                                data.put("firstTimeLogin", com.google.firebase.Timestamp.now());

                                                db.collection("Account").document(uid).set(data)
                                                        .addOnSuccessListener(aVoid -> {
                                                            saveUserInfo(user);
                                                            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                                            intent.putExtra("UUID", uid);
                                                            startActivity(intent);
                                                            finish();
                                                        });

                                            }
                                        });
                            }
                        } else {
                            Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void saveUserInfo(FirebaseUser user) {
        if (user != null) {
            SharedPreferences prefs = getSharedPreferences("UserInfo", MODE_PRIVATE);
            db.collection("Account").document(user.getUid().toString()).get().addOnSuccessListener(documentSnapshot -> {
                if(documentSnapshot.exists()){
                    CCCD =documentSnapshot.getString("CCCD");
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("UserID",user.getUid());
                    editor.putString("phoneNumber", user.getPhoneNumber());
                    editor.putString("name", user.getDisplayName());
                    editor.putString("email", user.getEmail());
                    editor.putString("photo", user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "");
                    editor.putString("CCCD",CCCD);
                    editor.apply();
                }
            });

        }
    }
}