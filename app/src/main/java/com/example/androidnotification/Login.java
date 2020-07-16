package com.example.androidnotification;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;


public class Login extends AppCompatActivity {

    private static final String TAG = "TAG";
    EditText edUserName, edPassword;
    CardView cardLogin;
    ImageView imageView;
    TextView tvSignUp;

    //FirebaseAuth
    FirebaseAuth mAuth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    ProgressDialog progressDialog;

    SignInButton btnGoogle;

    private static final int RC_SIGN_IN = 1;
    private GoogleSignInClient mGoogleSignInClient;

    private static final String EMAIL = "email";

    CallbackManager callbackManager;
    LoginButton loginButton;

    String fbEmail, fbFirstName, fbLastName, fbUid;

    CardView btnFacebook;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //get progressDialog
        progressDialog = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();

        edUserName = findViewById(R.id.edUserName);
        edPassword = findViewById(R.id.edPassword);
        cardLogin = findViewById(R.id.cardLogin);
        imageView = findViewById(R.id.imageView);
        btnGoogle = findViewById(R.id.btnGoogle);
        tvSignUp = findViewById(R.id.tvSignUp);
        btnFacebook = findViewById(R.id.btnFacebook);


        cardLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                progressDialog.setMessage("Processing");
                progressDialog.setCancelable(false);
                progressDialog.show();

                if (edUserName.getText().toString().isEmpty()) {

                    progressDialog.dismiss();
                    edUserName.setError("Required Field...");
                    edUserName.requestFocus();
                    return;
                }

                if (edPassword.getText().toString().isEmpty()) {

                    progressDialog.dismiss();
                    edPassword.setError("Required Field...");
                    edUserName.requestFocus();
                    return;

                }

                mAuth.signInWithEmailAndPassword(edUserName.getText().toString().trim().toLowerCase(), edPassword.getText().toString().trim())
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                if (task.isSuccessful()) {

                                    progressDialog.dismiss();
                                    Toast.makeText(Login.this, "Login Successful", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(Login.this, CollectingData.class));
                                    finish();

                                } else {

                                    progressDialog.dismiss();
                                    Toast.makeText(Login.this, "Login Successful plz check your email or password ", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                //Toast.makeText(Login.this, String.valueOf(edUserName.getText().toString().toLowerCase()), Toast.LENGTH_SHORT).show();
            }
        });

        tvSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(Login.this, SignUp.class));
            }
        });


        // **********************Google Sign-in Code**************************************** //

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        btnGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                signIn();
            }
        });

        //printKeyHash
        //printKeyHash();


        //Custom Button of facebook login
        callbackManager = CallbackManager.Factory.create();
        btnFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                LoginManager.getInstance().logInWithReadPermissions(Login.this, Arrays.asList("email", "public_profile"));
                LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {

                        handleFacebookAccessToken(loginResult.getAccessToken());
                    }

                    @Override
                    public void onCancel() {
                        // App code
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        // App code

                        Toast.makeText(Login.this, "Not Login", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });


    }


    private void handleFacebookAccessToken(AccessToken token) {

        //Toast.makeText(this, String.valueOf(token), Toast.LENGTH_SHORT).show();

        Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            Toast.makeText(Login.this, "signInWithCredential:success", Toast.LENGTH_SHORT).show();
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(Login.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        }

                    }
                });
    }

    //This method to getting facebook data in the from of object
    private void getFaceBookData(JSONObject object) {

        try {

            fbEmail = object.getString("email");
            fbFirstName = object.getString("first_name");
            fbLastName = object.getString("last_name");
            fbUid = object.getString("id");


            Toast.makeText(this, fbEmail, Toast.LENGTH_SHORT).show();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //This method to create the key Hash for the facebook
    private void printKeyHash() {

        try {

            PackageInfo info = getPackageManager().getPackageInfo("com.example.androidnotification", PackageManager.GET_SIGNATURES);

            for (Signature signature : info.signatures) {

                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());

                Log.d(TAG, "printKeyHash: " + Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }

        } catch (PackageManager.NameNotFoundException e) {

            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private void signIn() {

        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();

        AlertDialog.Builder builder = new AlertDialog.Builder(Login.this);

        builder.setCancelable(false);
        builder.setTitle("Exit");
        builder.setMessage("Are you sure you want to exit?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                //super.onBackPressed();
                finish();
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                dialogInterface.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {

            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);

            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                //Log.w(TAG, "Google sign in failed", e);
                Toast.makeText(this, "Google sign in failed = " + e, Toast.LENGTH_SHORT).show();
                // ...
            }
        }


    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.

        if (mAuth.getCurrentUser() != null) {

            startActivity(new Intent(Login.this, CollectingData.class));
            finish();
            //updateUI(mAuth.getCurrentUser());
        }

    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            //Log.d(TAG, "signInWithCredential:success");
                            Toast.makeText(Login.this, "signInWithCredential:success", Toast.LENGTH_SHORT).show();
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);

                        } else {
                            // If sign in fails, display a message to the user.
                            //Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(Login.this, "signInWithCredential:failure", Toast.LENGTH_SHORT).show();
                            // Snackbar.make(findViewById(R.id.), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                            //updateUI(null);
                        }

                        // ...
                    }
                });

    }

    //Update user information in the database
    private void updateUI(FirebaseUser user) {

        HashMap<String, Object> data = new HashMap<>();
        data.put("FName", user.getDisplayName());
        data.put("LName", user.getDisplayName());
        data.put("email", user.getEmail());
        data.put("password", "No need this");
        data.put("uid", mAuth.getCurrentUser().getUid());
        data.put("token", user.getIdToken(true).toString());

        db.collection("users").document(mAuth.getCurrentUser().getUid())
                .set(data).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                finish();
                startActivity(new Intent(Login.this, CollectingData.class));
            }
        });

    }


}
