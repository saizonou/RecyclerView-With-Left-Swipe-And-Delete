package com.gitplus.fanmilk;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.gitplus.fanmilk.app.AppController;
import com.gitplus.fanmilk.helper.Constant;
import com.gitplus.fanmilk.helper.SessionManager;
import com.kaopiz.kprogresshud.KProgressHUD;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private EditText editTextAgentCode, editTextPassword;
    //The session to retrieve user preferences
    private SessionManager sessionManager;
    //Custom progressBar
    private KProgressHUD hud;
    //Agent code and password variable
    private String agent_code, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        sessionManager = new SessionManager(this);
        if (sessionManager.isLoggedIn()){
            Intent intent = new Intent(MainActivity.this, VendorActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
        initView();

        hud = KProgressHUD.create(this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setCancellable(false)
                .setLabel("Loading...")
                .setDimAmount(0.5f);

        findViewById(R.id.submit).setOnClickListener(v -> getData());
    }

    /**
     * Initilize the views
     */
    private void initView() {
        editTextAgentCode = findViewById(R.id.agent_code);
        editTextPassword = findViewById(R.id.password);
    }

    /**
     * Get the data from editext
     */
    private void getData(){
        agent_code = editTextAgentCode.getText().toString();
        password = editTextPassword.getText().toString();

        boolean isError = false;

        if (agent_code.isEmpty()){
            Toast.makeText(this, "Agent code is required", Toast.LENGTH_SHORT).show();
            isError = true;
        }

        if (password.isEmpty()){
            Toast.makeText(this, "Password is required", Toast.LENGTH_SHORT).show();
            isError = true;
        }

        if (!isError) {
            sendData();
        }
    }

    /**
     * Send data to the server
     */
    private void sendData() {
        // Tag used to cancel the request
        String tag_string_req = "req_login";
        hud.show();
        StringRequest strReq = new StringRequest(Request.Method.POST, Constant.LOGIN, response -> {

            try {
                JSONObject jObj = new JSONObject(response);
                boolean error = jObj.getBoolean("error");
                String errorMsg = jObj.getString("error_msg");
                Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_LONG).show();

                // Check for error node in json
                if (!error) {
                    String name = jObj.getString("agent_name").trim();
                    String code = jObj.getString("agent_code").trim();
                    sessionManager.saveUserDetails(name,code);
                    sessionManager.setLogin(true);

                    Intent intent = new Intent(MainActivity.this, VendorActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();

                    hud.dismiss();

                } else {
                    // Error in login. Get the error message
                    hud.dismiss();
                }
            } catch (JSONException e) {
                // JSON error
                Toast.makeText(this, R.string.json_erro, Toast.LENGTH_SHORT).show();
                /*String error_message = "MainActivity sendRegistrationToServerAuth Json error: "+ e.getMessage();
                Intent intentLike = new Intent(MainActivity.this, ErrorService.class);
                intentLike.putExtra("error_message",error_message);
                startService(intentLike);*/
                hud.dismiss();
            }

        }, error -> {
            hud.dismiss();

            Toast.makeText(this, R.string.net_error, Toast.LENGTH_SHORT).show();

        }) {
            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<>();
                params.put("agent_code", agent_code);
                params.put("password", password);
                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

}
