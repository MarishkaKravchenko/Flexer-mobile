package flexer.com.flexer_mobile;

import android.app.ProgressDialog;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.InjectView;
import flexer.com.flexer_mobile.utils.OkHttpHandler;
import flexer.com.flexer_mobile.utils.User;
import flexer.com.flexer_mobile.utils.UserLocalStore;


import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {


    private String URL = "http://private-4e85ed-mokky.apiary-mock.com/users2";
    /*private String URL = "http://192.168.0.101/auth/login";*/

    UserLocalStore userLocalStore;

    @InjectView(R.id.input_user) EditText userText;
    @InjectView(R.id.input_password) EditText passwordText;
    @InjectView(R.id.btn_login) Button loginButton;
    @InjectView(R.id.ch_remember_me) CheckBox ch_remember_me;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        userLocalStore = new UserLocalStore(this);

        loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String username = userText.getText().toString();
                String password = passwordText.getText().toString();
                User user = new User(username, password);
                auth(user);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (authenticate()) {
            displayUserDetails();
            startActivity(new Intent(getApplicationContext(), QrActivity.class));
        }
    }

    private boolean authenticate() {
        return userLocalStore.getLoggedInUser() != null;
    }

    private void displayUserDetails() {
        User user = userLocalStore.getLoggedInUser();
        userText.setText(user.username);
        passwordText.setText(user.password);
    }

    public void auth(User user) {

        if (!validate(user)) {
            onLoginFailed();
            return;
        }

        loginButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");
        progressDialog.show();

        OkHttpHandler handler = new OkHttpHandler(user.username, user.password);
        String result = null;
        try {
            result = handler.execute(URL, "GET").get();
            Log.v("RESPONSE>>>>>>>>>>>>",result);
            if(result!=null) {
                onLoginSuccess(user);
            }
            else{
                onLoginFailed();
            }
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

        } catch (ExecutionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        progressDialog.dismiss();
    }

    @Override
    public void onBackPressed() {
        // disable going back to the MainActivity
        moveTaskToBack(true);
    }

    public void onLoginSuccess(User user) {
        loginButton.setEnabled(true);
        if(ch_remember_me.isChecked()) {
            userLocalStore.storeUserData(user);
            userLocalStore.setUserLoggedIn(true);
        }
        startActivity(new Intent(getApplicationContext(), QrActivity.class));
        finish();
    }

    public void onLoginFailed() {
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();
        loginButton.setEnabled(true);
    }

    public boolean validate(User user) {
        boolean valid = true;

        if (user.username.isEmpty()) {
            userText.setError("enter your name");
            valid = false;
        } else {
            userText.setError(null);
        }

        if (user.password.isEmpty() || user.password.length() < 4 || user.password.length() > 20) {
            passwordText.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            passwordText.setError(null);
        }
        return valid;
    }
}
