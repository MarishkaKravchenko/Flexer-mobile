package flexer.com.flexer_mobile;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

import flexer.com.flexer_mobile.utils.OkHttpHandler;
import flexer.com.flexer_mobile.utils.User;
import flexer.com.flexer_mobile.utils.UserLocalStore;

public class ResultActivity extends AppCompatActivity {

    TextView tv_progress;
    ProgressBar progressBar;
    Button btn_getBonus;

    private int progressActual;
    private int progressRequired;
    private int ProgressStatus = 0;

    private ProgressDialog pDialog;
    private int cardId;

    UserLocalStore userLocalStore;


    private Handler handler = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        Intent intent = getIntent();
        String message = intent.getStringExtra("message");

        progressBar = (ProgressBar) findViewById(R.id.pb);
        tv_progress = (TextView) findViewById(R.id.tv_progress);
        btn_getBonus = (Button) findViewById(R.id.btn_bonus);
        btn_getBonus.setEnabled(false);

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
                btn_getBonus.setEnabled(true);
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
                            tv_progress.setText(ProgressStatus + "/" + progressBar.getMax());
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

        String URL = "http://192.168.0.101/employee-api/cards/" + cardId;

        User user = userLocalStore.getLoggedInUser();
        OkHttpHandler handler = new OkHttpHandler(user.username, user.password);
        String response = null;
        try {
            response = handler.execute(URL, "PUT", "{\"count\":0}").get();
            Log.e("response ", "onResponse(): " + response);
            hideProgressDialog();
            startActivity(new Intent(getApplicationContext(), QrActivity.class));
        } catch (InterruptedException | ExecutionException e) {
            // TODO Auto-generated catch block
            hideProgressDialog();
            e.printStackTrace();
        }
    }

    public void onBackPressed() {
        startActivity(new Intent(getApplicationContext(), QrActivity.class));
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
