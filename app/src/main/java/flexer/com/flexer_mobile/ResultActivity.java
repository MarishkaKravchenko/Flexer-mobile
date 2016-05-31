package flexer.com.flexer_mobile;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

import butterknife.ButterKnife;
import butterknife.InjectView;
import flexer.com.flexer_mobile.utils.OkHttpHandler;
import flexer.com.flexer_mobile.utils.User;
import flexer.com.flexer_mobile.utils.UserLocalStore;

public class ResultActivity extends AppCompatActivity {


    @InjectView(R.id.pb) ProgressBar progressBar;
    @InjectView(R.id.tv_progress) TextView tv_progress;
    @InjectView(R.id.btn_bonus) Button btn_getBonus;

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

        ButterKnife.inject(this);

        Intent intent = getIntent();
        String message = intent.getStringExtra("message");

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
            if(response!=null) {
                startActivity(new Intent(getApplicationContext(), QrActivity.class));
            }else{
                Toast.makeText(getApplicationContext(), "Something went wrong..", Toast.LENGTH_SHORT).show();
            }
        } catch (InterruptedException | ExecutionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        hideProgressDialog();
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
