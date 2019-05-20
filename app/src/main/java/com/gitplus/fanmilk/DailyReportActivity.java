package com.gitplus.fanmilk;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.gitplus.fanmilk.app.AppController;
import com.gitplus.fanmilk.helper.Constant;
import com.gitplus.fanmilk.helper.SessionManager;
import com.gitplus.fanmilk.model.Vendor;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.mikhaellopez.circularimageview.CircularImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class DailyReportActivity extends AppCompatActivity {

    private CircularImageView vendor_image_view;
    private TextView vendor_name_textview;
    private String vendor_code, vendor_image, vendor_name, date, mois, amount, agent_code;
    private Calendar calendar;
    private int year, month, day, position;
    private EditText editTextDate, editTextAmount;
    //Custom progressBar
    private KProgressHUD hud;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_report);
        position = getIntent().getIntExtra("position",-1);
        initViews();

        SessionManager sessionManager = new SessionManager(this);
        HashMap<String,String> user = sessionManager.readUserDetails();
        agent_code = user.get(sessionManager.AGENT_CODE);

        vendor_code = getIntent().getStringExtra("vendor_code");
        vendor_image = getIntent().getStringExtra("vendor_image");
        vendor_name = getIntent().getStringExtra("vendor_name");

        Glide.with(this)
                .load(vendor_image)
                .into(vendor_image_view);

        vendor_name_textview.setText(vendor_name);

        editTextDate.setOnTouchListener((v, event) -> {
            setDate(v);
            return true;
        });
        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);

        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        showDate(year, month, day);

    }

    private void initViews() {
        vendor_image_view = findViewById(R.id.img_profile);
        vendor_name_textview = findViewById(R.id.vendor_name);
        editTextDate = findViewById(R.id.date);
        editTextAmount = findViewById(R.id.amout);
        hud = KProgressHUD.create(this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setCancellable(false)
                .setLabel("Loading...")
                .setDimAmount(0.5f);
        findViewById(R.id.submit).setOnClickListener(v -> getData());
    }

    /**
     * Get the data from editext
     */
    private void getData(){
        amount = editTextAmount.getText().toString().trim();
        boolean isError = false;

        if (amount.isEmpty()){
            Toast.makeText(this, "The amount is required", Toast.LENGTH_SHORT).show();
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
        StringRequest strReq = new StringRequest(Request.Method.POST, Constant.SALE_REPORT, response -> {

            try {
                JSONObject jObj = new JSONObject(response);
                boolean error = jObj.getBoolean("error");
                String errorMsg = jObj.getString("error_msg");
                // Check for error node in json
                if (!error) {
                    openDialogAfterPostSuccess(errorMsg);
                    hud.dismiss();

                } else {
                    // Error in login. Get the error message
                    Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_LONG).show();
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
                params.put("vendor_code", vendor_code);
                params.put("amount", amount);
                params.put("date", date);
                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    @SuppressWarnings("deprecation")
    public void setDate(View view) {
        showDialog(999);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        // TODO Auto-generated method stub
        if (id == 999) {
            return new DatePickerDialog(this,
                    myDateListener, year, month, day);
        }
        return null;
    }

    /**
     * Listen to the date selected
     */
    private DatePickerDialog.OnDateSetListener myDateListener = (arg0, arg1, arg2, arg3) -> {
        // TODO Auto-generated method stub
        // arg1 = year
        // arg2 = month
        // arg3 = day
        showDate(arg1, arg2, arg3);
    };

    /**
     * Display the date in the format year/month/day
     * @param year the current year
     * @param month the current month
     * @param day the current day
     */
    private void showDate(int year, int month, int day) {
        switch (month) {
            case 0:
                mois = "Jan";
                break;
            case 1:
                mois = "Feb";
                break;
            case 2:
                mois = "Mar";
                break;
            case 3:
                mois = "Apr";
                break;
            case 4:
                mois = "May";
                break;
            case 5:
                mois = "Jun";
                break;
            case 6:
                mois = "Jul";
                break;
            case 7:
                mois = "Aug";
                break;
            case 8:
                mois = "Sep";
                break;
            case 9:
                mois = "Oct";
                break;
            case 10:
                mois = "Nov";
                break;
            case 11:
                mois = "Dec";
                break;
        }
        editTextDate.setText(new StringBuilder().append(day).append("/")
                .append(mois).append("/").append(year));
        date = String.valueOf(new StringBuilder().append(year).append("-").append(month + 1).append("-").append(day));
    }

    /**
     * Open the post success dialog
     * @param message the message to be displayed
     */
    private void openDialogAfterPostSuccess(String message) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.post_success, null);
        dialogBuilder.setView(dialogView);

        AlertDialog alertDialog = dialogBuilder.create();

        TextView messageTxt = dialogView.findViewById(R.id.message);
        messageTxt.setText(message);
        TextView btnContinue = dialogView.findViewById(R.id.afterPost);
        btnContinue.setOnClickListener(v -> {
            alertDialog.dismiss();
            Intent intent = new Intent(DailyReportActivity.this, Vendor.class);
            intent.putExtra("amount", amount);
            intent.putExtra("position", position);
            setResult(1,intent);
            finish();
        });

        alertDialog.setCancelable(false);
        alertDialog.show();
    }
}
