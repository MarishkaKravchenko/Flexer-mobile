package flexer.com.flexer_mobile;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import android.content.Intent;
import android.widget.Button;
import android.widget.ImageButton;

import flexer.com.flexer_mobile.utils.UserLocalStore;

public class QrActivity extends AppCompatActivity implements View.OnClickListener {

    private Button scannerButton;
    private ImageButton logoutButton;

    UserLocalStore userLocalStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr);

        scannerButton = (Button) findViewById(R.id.btn_scannerButton);
        logoutButton = (ImageButton) findViewById(R.id.btn_logout);
        scannerButton.setOnClickListener(this);
        logoutButton.setOnClickListener(this);
    }
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_scannerButton:
                    startActivity(new Intent(getApplicationContext(), BarcodeScannerActivity.class));

                    break;
                case R.id.btn_logout:
                    userLocalStore.clearUserData();
                    userLocalStore.setUserLoggedIn(false);
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    break;
            }
        }
}