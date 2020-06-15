package com.mds.eventifind;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {

    private SignInButton signInButton;
    private static GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    private int RC_SIGN_IN = 1;
    private Button btnBait;
    private MediaPlayer s1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        signInButton = findViewById(R.id.sign_in_button);
        mAuth = FirebaseAuth.getInstance();
        // seteaza optiunile de signin
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail().build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        // onClickListener pentru buton si daca e apasat apeleaza functia de sign-in
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                singInWithGoogle();
            }
        });

        btnBait = findViewById(R.id.btnBait);
        btnBait.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImageView imageview =(ImageView) findViewById(R.id.imageView3);
                imageview.setVisibility(View.VISIBLE);

                if(s1 != null) {
                    if (s1.isPlaying() == true) {
                            s1.release();
                            s1 = MediaPlayer.create(LoginActivity.this, R.raw.jebaitedsong);
                            imageview.setVisibility(View.INVISIBLE);
                    } else {
                        s1 = MediaPlayer.create(LoginActivity.this, R.raw.jebaitedsong);
                        s1.start();
                        Toast.makeText(LoginActivity.this, "Nu merge!!!", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    s1 = MediaPlayer.create(LoginActivity.this, R.raw.jebaitedsong);
                    s1.start();

                    Toast.makeText(LoginActivity.this, "Nu merge!!!", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        // la start daca contul nu e null, inseamna ca a fost logat deja si intra direct in MainActivity
        //GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null)
            gotoMain();
    }

    private void gotoMain() {
        Intent intent = new Intent(this, MainActivity.class);
        //intent.putExtra("account", account);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void singInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_SIGN_IN){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }

    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            assert account != null;
            firebaseAuthWithGoogle(account);
        } catch (ApiException e) {
            Log.e("Err","signInResult:failed code=" + e.getStatusCode());
        }
    }

    private void firebaseAuthWithGoogle(final GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success
                            FirebaseUser user = mAuth.getCurrentUser();
                            FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid()).child("admin").setValue(false);
                            FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid()).child("email").setValue(user.getEmail());
                            gotoMain();
                        } else {
                            // If sign in fails
                            Log.e("err", "signInWithCredential:failure", task.getException());
                        }
                    }
                });

    }
    static void signOut() {
        mGoogleSignInClient.signOut();
        FirebaseAuth.getInstance().signOut();
    }

    // go home on back pressed
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

}
