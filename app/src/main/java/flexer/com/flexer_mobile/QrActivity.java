package flexer.com.flexer_mobile;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import android.content.Intent;
import android.widget.Button;
import android.widget.ImageButton;

import butterknife.ButterKnife;
import butterknife.InjectView;
import flexer.com.flexer_mobile.utils.UserLocalStore;

public class QrActivity extends AppCompatActivity implements View.OnClickListener {

    UserLocalStore userLocalStore;

    @InjectView(R.id.btn_scannerButton) Button scannerButton;
    @InjectView(R.id.btn_logout) ImageButton logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr);
        ButterKnife.inject(this);

        scannerButton.setOnClickListener(this);
        logoutButton.setOnClickListener(this);

        userLocalStore = new UserLocalStore(this);
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