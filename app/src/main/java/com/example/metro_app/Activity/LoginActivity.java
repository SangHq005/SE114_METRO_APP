//package com.example.metro_app.Activity;
//
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.os.Bundle;
//import android.text.InputType;
//import android.view.View;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.ImageView;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.example.metro_app.Activity.Admin.AdHomeActivity;
//import com.example.metro_app.Activity.User.InfoAcitivity;
//import com.example.metro_app.R;
//import com.google.android.gms.auth.api.signin.GoogleSignIn;
//import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
//import com.google.android.gms.auth.api.signin.GoogleSignInClient;
//import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
//import com.google.android.gms.common.api.ApiException;
//import com.google.android.gms.tasks.OnCompleteListener;
//import com.google.android.gms.tasks.Task;
//import com.google.firebase.auth.AuthCredential;
//import com.google.firebase.auth.AuthResult;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;
//import com.google.firebase.auth.GoogleAuthProvider;
//import com.google.firebase.firestore.FirebaseFirestore;
//import com.google.firebase.firestore.auth.User;
//
//import java.util.HashMap;
//import java.util.Map;
//
//public class LoginActivity extends AppCompatActivity {

//    private EditText passwordEditText;
//    private ImageView togglePassword;
//    private boolean isPasswordVisible = false;
//    private GoogleSignInClient mGoogleSignInClient;
//    private FirebaseAuth mAuth;
//    FirebaseFirestore db = FirebaseFirestore.getInstance();
//    private static final int RC_SIGN_IN = 9001;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_login);
//
//        mAuth = FirebaseAuth.getInstance();
//
//        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                .requestIdToken(getString(R.string.default_web_client_id)) // Ensure this is set in strings.xml
//                .requestEmail()
//                .build();
//
//        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
//
//        passwordEditText = findViewById(R.id.password);
//        togglePassword = findViewById(R.id.togglePasswordVisibility);
//        TextView signUpText = findViewById(R.id.SignUp);
//        Button googleSignInButton = findViewById(R.id.Google);
//
//        togglePassword.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (isPasswordVisible) {
//                    passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
//                    togglePassword.setImageResource(R.drawable.eyeoff);
//                } else {
//                    passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
//                    togglePassword.setImageResource(R.drawable.eyeopen);
//                }
//                isPasswordVisible = !isPasswordVisible;
//                passwordEditText.setSelection(passwordEditText.getText().length());
//            }
//        });
//
//        signUpText.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
//                startActivity(intent);
//            }
//        });
//
//        googleSignInButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                signInWithGoogle();
//            }
//        });
//    }
//
//    private void signInWithGoogle() {
//        mGoogleSignInClient.signOut().addOnCompleteListener(task -> {
//            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
//            startActivityForResult(signInIntent, RC_SIGN_IN);
//        });
//    }
//
//
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (requestCode == RC_SIGN_IN) {
//            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
//            try {
//                GoogleSignInAccount account = task.getResult(ApiException.class);
//                if (account != null) {
//                    firebaseAuthWithGoogle(account.getIdToken());
//                }
//            } catch (ApiException e) {
//                Toast.makeText(this, "Google Sign-In failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        }
//    }
//
//    private void firebaseAuthWithGoogle(String idToken) {
//        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
//        mAuth.signInWithCredential(credential)
//                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<AuthResult> task) {
//                        if (task.isSuccessful()) {
//                            FirebaseUser user = mAuth.getCurrentUser();
//                            if (user != null) {
//                                String uid = user.getUid();
//                                db.collection("Account").document(uid)
//                                        .get()
//                                        .addOnSuccessListener(documentSnapshot -> {
//                                            if (documentSnapshot.exists()) {
//                                                String role = documentSnapshot.getString("Role");
//                                                if ("Admin".equals(role)) {
//                                                    saveUserInfo(user);
//                                                    startActivity(new Intent(LoginActivity.this, AdHomeActivity.class));
//                                                } else {
//                                                    saveUserInfo(user);
//                                                    startActivity(new Intent(LoginActivity.this, InfoAcitivity.class));
//                                                }
//                                                Toast.makeText(LoginActivity.this, "Google Sign-In successful!", Toast.LENGTH_SHORT).show();
//                                                finish();
//                                            } else {
//                                                // Nếu lần đầu đăng nhập, tạo user mặc định
//                                                Map<String, Object> data = new HashMap<>();
//                                                data.put("Name",user.getDisplayName());
//                                                data.put("Email", user.getEmail());
//                                                data.put("Role", "User"); // mặc định là user
//                                                db.collection("Account").document(uid).set(data)
//                                                        .addOnSuccessListener(aVoid -> {
//                                                            saveUserInfo(user);
//                                                            startActivity(new Intent(LoginActivity.this, InfoAcitivity.class));
//                                                            finish();
//                                                        });
//                                            }
//                                        });
//                            }
//                        } else {
//                            Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                });
//    }
//
//    private void saveUserInfo(FirebaseUser user) {
//        if (user != null) {
//            SharedPreferences prefs = getSharedPreferences("UserInfo", MODE_PRIVATE);
//            SharedPreferences.Editor editor = prefs.edit();
//            editor.putString("phoneNumber",user.getPhoneNumber());
//            editor.putString("name", user.getDisplayName());
//            editor.putString("email", user.getEmail());
//            editor.putString("photo", user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "");
//            editor.apply();
//        }
//    }
//}

package com.example.metro_app.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.metro_app.R;

public class LoginActivity extends AppCompatActivity {

    private Button googleLoginBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }
}