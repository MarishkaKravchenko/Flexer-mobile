package flexer.com.flexer_mobile;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class BarcodeScannerActivity extends AppCompatActivity {

    private Camera mCamera;
    private CameraPreview mPreview;
    private Handler autoFocusHandler;

    private Button scanButton;
    private ImageScanner scanner;

    private boolean barcodeScanned = false;
    private boolean previewing = true;

    private ProgressDialog pDialog;

    private RequestQueue requestQueue;
   /* private static final String URL = "http://private-4e85ed-mokky.apiary-mock.com/users";*/
    private static String URL;
    private JSONObject obj;

    SharedPreferences sPref;

    private String user;
    private String pass;

    static {
        System.loadLibrary("iconv");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.code_scanner);

        pDialog = new ProgressDialog(BarcodeScannerActivity.this, R.style.AppTheme_Dark_Dialog);
        pDialog.setMessage("Loading...");
        pDialog.setCancelable(false);

        requestQueue = Volley.newRequestQueue(this);

        sPref = getPreferences(MODE_PRIVATE);
        user = sPref.getString("user", "");
        pass = sPref.getString("pass", "");

        initControls();
    }

    private void initControls() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        autoFocusHandler = new Handler();
        mCamera = getCameraInstance();

        // Instance barcode scanner
        scanner = new ImageScanner();
        scanner.setConfig(0, Config.X_DENSITY, 3);
        scanner.setConfig(0, Config.Y_DENSITY, 3);

        mPreview = new CameraPreview(BarcodeScannerActivity.this, mCamera, previewCb,
                autoFocusCB);
        FrameLayout preview = (FrameLayout) findViewById(R.id.cameraPreview);
        preview.addView(mPreview);

        scanButton = (Button) findViewById(R.id.ScanButton);

        scanButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (barcodeScanned) {
                    barcodeScanned = false;
                    mCamera.setPreviewCallback(previewCb);
                    mCamera.startPreview();
                    previewing = true;
                    mCamera.autoFocus(autoFocusCB);
                }
            }
        });
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            releaseCamera();
        }
        return super.onKeyDown(keyCode, event);
    }


    /**
     * A safe way to get an instance of the Camera object.
     */
    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open();
        } catch (Exception e) {
        }
        return c;
    }

    private void releaseCamera() {
        if (mCamera != null) {
            previewing = false;
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
    }

    private Runnable doAutoFocus = new Runnable() {
        public void run() {
            if (previewing)
                mCamera.autoFocus(autoFocusCB);
        }
    };

    Camera.PreviewCallback previewCb = new Camera.PreviewCallback() {
        public void onPreviewFrame(byte[] data, Camera camera) {
            Camera.Parameters parameters = camera.getParameters();
            Camera.Size size = parameters.getPreviewSize();

            Image barcode = new Image(size.width, size.height, "Y800");
            barcode.setData(data);

            int result = scanner.scanImage(barcode);

            if (result != 0) {
                previewing = false;
                mCamera.setPreviewCallback(null);
                mCamera.stopPreview();

                SymbolSet syms = scanner.getResults();
                for (Symbol sym : syms) {

                    Log.i("<<<<<<Asset Code>>>>> ",
                            "<<<<Bar Code>>> " + sym.getData());

                    String scanResult = sym.getData().trim();
                    makeJsonObjReq(scanResult);

                  /*  Toast.makeText(BarcodeScannerActivity.this, scanResult,
                            Toast.LENGTH_SHORT).show();*/

                    barcodeScanned = true;

                    break;
                }
            }
        }
    };

    // Mimic continuous auto-focusing
    Camera.AutoFocusCallback autoFocusCB = new Camera.AutoFocusCallback() {
        public void onAutoFocus(boolean success, Camera camera) {
            autoFocusHandler.postDelayed(doAutoFocus, 1000);
        }
    };

    private void showProgressDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideProgressDialog() {
        if (pDialog.isShowing())
            pDialog.hide();
    }

    private void makeJsonObjReq(String result) {
        showProgressDialog();
        try {
            JSONObject obj = new JSONObject(result);
            Integer count = obj.getInt("count");
            count++;
            obj.put("count", count);
            URL = "http://192.168.0.101/employee-api/cards/" + obj.getString("cardId");
        } catch (JSONException e) {
            e.printStackTrace();
        }



        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.PUT,
                URL,
                obj,
                listener,
                errorListener){

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return createBasicAuthHeader(user, pass);
            }
        };

        requestQueue.add(request);

    };

    Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {

        @Override
        public void onResponse(JSONObject response) {
            try {
                if (response.names().get(0).equals("success")) {
                    Toast.makeText(getApplicationContext(), "Welcome", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(getApplicationContext(), ResultActivity.class);
                    intent.putExtra("message", response.toString());
                    startActivity(intent);
                    hideProgressDialog();
                } else {
                    Toast.makeText(getApplicationContext(), "Error" + response.getString("error"), Toast.LENGTH_LONG).show();
                    hideProgressDialog();
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
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                hideProgressDialog();
            }
        }
    };

    Map<String, String> createBasicAuthHeader(String username, String password) {
        Map<String, String> headerMap = new HashMap<String, String>();
        String credentials = username + ":" + password;
        String base64EncodedCredentials =
                Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
        headerMap.put("Authorization", "Basic " + base64EncodedCredentials);

        return headerMap;
    }
}