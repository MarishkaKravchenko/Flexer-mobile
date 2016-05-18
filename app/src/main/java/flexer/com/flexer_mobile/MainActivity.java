package flexer.com.flexer_mobile;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.InjectView;


import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "AuthActivity";

    private RequestQueue requestQueue;
    /*private static final String URL = "http://private-4e85ed-mokky.apiary-mock.com/users";*/
    private static final String URL = "http://192.168.0.101/auth/login";
    private String user;
    private String pass;

    SharedPreferences sPref;

    @InjectView(R.id.input_user) EditText _userText;
    @InjectView(R.id.input_password) EditText _passwordText;
    @InjectView(R.id.btn_login) Button _loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        requestQueue = Volley.newRequestQueue(this);



        _loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                auth();
            }
        });
    }

    public void auth() {
        Log.d(TAG, "Auth");

        if (!validate()) {
            onLoginFailed();
            return;
        }

        user = _userText.getText().toString();
        pass = _passwordText.getText().toString();

        sPref = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putString("user", user);
        ed.putString("pass", pass);
        ed.commit();

        _loginButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");
        progressDialog.show();

        new android.os.Handler().postDelayed(
                new Runnable() {
                    Map<String, String> createBasicAuthHeader(String username, String password) {
                        Map<String, String> headerMap = new HashMap<String, String>();
                        String credentials = username + ":" + password;
                        String base64EncodedCredentials =
                                Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
                        headerMap.put("Authorization", "Basic " + base64EncodedCredentials);

                        return headerMap;
                    }

                    Response.Listener<String> listener = new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                if (jsonObject.names().get(0).equals("success")) {
                                    Toast.makeText(getApplicationContext(), "Welcome", Toast.LENGTH_LONG).show();
                                    startActivity(new Intent(getApplicationContext(), QrActivity.class));
                                } else {
                                    Toast.makeText(getApplicationContext(), "Error" + jsonObject.getString("error"), Toast.LENGTH_LONG).show();
                                    onLoginFailed();
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    };

                    Response.ErrorListener errorListener = new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            if (error.networkResponse != null) {
                                Toast.makeText(getApplicationContext(), "Error Response code: " + error.networkResponse.statusCode, Toast.LENGTH_LONG).show();
                            }
                        }
                    };

                    public void run() {
                        StringRequest request = new StringRequest(
                                Request.Method.POST,
                                URL,
                                listener,
                                errorListener) {

                            @Override
                            public Map<String, String> getHeaders() throws AuthFailureError {
                                return createBasicAuthHeader(user, pass);
                            }
                        };

                        requestQueue.add(request);
                        onLoginSuccess();
                        progressDialog.dismiss();
                    }
                }, 3000);
    }

    @Override
    public void onBackPressed() {
        // disable going back to the MainActivity
        moveTaskToBack(true);
    }

    public void onLoginSuccess() {
        _loginButton.setEnabled(true);
        finish();
    }

    public void onLoginFailed() {
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();

        _loginButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String user = _userText.getText().toString();
        String password = _passwordText.getText().toString();

        if (user.isEmpty()) {
            _userText.setError("enter your name");
            valid = false;
        } else {
            _userText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 20) {
            _passwordText.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        return valid;
    }
}
