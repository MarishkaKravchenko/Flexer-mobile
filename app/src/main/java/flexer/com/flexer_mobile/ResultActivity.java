package flexer.com.flexer_mobile;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Credentials;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class ResultActivity extends AppCompatActivity {

    TextView progressView;
    ProgressBar progressBar;
    Button btnBonus;

    private int progressActual;
    private int progressRequired;
    private int ProgressStatus = 0;

    private ProgressDialog pDialog;
    private String URL;
    private int cardId;

    UserLocalStore userLocalStore;


    private Handler handler = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        Intent intent = getIntent();
        String message = intent.getStringExtra("message");

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressView = (TextView) findViewById(R.id.progressView);
        btnBonus = (Button) findViewById(R.id.btn_bonus);
        btnBonus.setEnabled(false);

        pDialog = new ProgressDialog(ResultActivity.this, R.style.AppTheme_Dark_Dialog);
        pDialog.setMessage("Please wait...");
        pDialog.setCancelable(false);

        userLocalStore = new UserLocalStore(this);

        try {
            JSONObject res = new JSONObject(message);
            progressActual = res.getInt("actual");
            progressRequired = res.getInt("needed");
            cardId = res.getInt("cardId");
            if(progressActual == progressRequired){
                btnBonus.setEnabled(true);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        progressBar.setMax(progressRequired);

        new Thread(new Runnable() {
            public void run() {
                while (ProgressStatus < progressActual) {
                    ProgressStatus += 1;
                    handler.post(new Runnable() {
                        public void run() {
                            progressBar.setProgress(ProgressStatus);
                            progressView.setText(ProgressStatus + "/" + progressBar.getMax());
                        }
                    });
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public void getBonus(View v){

        showProgressDialog();

        URL = "http://192.168.0.101/employee-api/cards/" + cardId;

        User user = userLocalStore.getLoggedInUser();
        OkHttpHandler handler = new OkHttpHandler(user.username, user.password);
        String response = null;
        try {
            response = handler.execute(URL, "PUT", "{\"count\":0}").get();
            Log.e("response ", "onResponse(): " + response);
            hideProgressDialog();
            Intent intent = new Intent(ResultActivity.this, QrActivity.class);
            startActivity(intent);
        } catch (InterruptedException | ExecutionException e) {
            // TODO Auto-generated catch block
            hideProgressDialog();
            e.printStackTrace();
        }

//        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
//        RequestBody body = RequestBody.create(JSON, "\"count\":0");
//
//        OkHttpClient client = new OkHttpClient();
//        User user = userLocalStore.getLoggedInUser();
//        String credential = Credentials.basic(user.username, user.password);
//
//        Request request = new Request.Builder()
//                .url(URL)
//                .put(body)
//                .addHeader("Authorization", credential)
//                .build();
//        Call call = client.newCall(request);
//
//        call.enqueue(new Callback() {
//            @Override
//            public void onFailure(Request request, IOException e) {
//
//                Log.e("HttpService", "onFailure() Request was: " + request);
//                hideProgressDialog();
//                e.printStackTrace();
//            }
//
//            @Override
//            public void onResponse(Response r) throws IOException {
//
//                String response = r.body().string();
//                Log.e("response ", "onResponse(): " + response);
//            }
//        });
    }



    public void onBackPressed() {
        Intent intent = new Intent(ResultActivity.this, QrActivity.class);
        startActivity(intent);
        finish();
    }
    private void showProgressDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideProgressDialog() {
        if (pDialog.isShowing())
            pDialog.hide();
    }
}
