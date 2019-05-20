package com.gitplus.fanmilk;

import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.gitplus.fanmilk.adpter.VendorAdapter;
import com.gitplus.fanmilk.app.AppController;
import com.gitplus.fanmilk.helper.Constant;
import com.gitplus.fanmilk.helper.SessionManager;
import com.gitplus.fanmilk.model.Vendor;
import com.kaopiz.kprogresshud.KProgressHUD;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VendorActivity extends AppCompatActivity implements VendorAdapter.IProcessFilter {

    private List<Vendor> vendorList = new ArrayList<>();
    private SessionManager sessionManager;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayout linearLayoutError;
    private TextView textViewError, textViewRetry;
    private ImageView imageViewNoInternet;
    private String agent_code, date;
    private static final int NETWORK_ERROR = 1;
    private static final int NO_DATA = 2;
    private VendorAdapter vendorAdapter;
    private boolean isAddvendor = false;
    private int position;
    //Custom progressBar
    private KProgressHUD hud;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vendor);
        initView();
        sessionManager = new SessionManager(this);
        HashMap<String, String> user = sessionManager.readUserDetails();
        agent_code = user.get(sessionManager.AGENT_CODE);
        vendorAdapter = new VendorAdapter(VendorActivity.this, vendorList, this);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(vendorAdapter);

        fetchData();
    }


    /**
     * Initialise view
     */
    private void initView() {
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        linearLayoutError = findViewById(R.id.error_layout);
        textViewError = findViewById(R.id.error_message);
        imageViewNoInternet = findViewById(R.id.error_image);

        findViewById(R.id.add_vendor).setOnClickListener(v -> {
            startActivity(new Intent(VendorActivity.this, AddNewVendorActivity.class));
            isAddvendor = true;
        });

        textViewRetry = findViewById(R.id.retry);
        textViewRetry.setOnClickListener(v -> {
            fetchData();
            linearLayoutError.setVisibility(View.GONE);
        });
        hud = KProgressHUD.create(this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setCancellable(false)
                .setLabel("Loading...")
                .setDimAmount(0.5f);
    }

    /**
     * Fetch the data from de server
     */
    private void fetchData() {
        if (vendorList.size() > 0) {
            vendorList.clear();
            vendorAdapter.notifyDataSetChanged();
        }
        textViewRetry.setVisibility(View.GONE);
        swipeRefreshLayout.setRefreshing(true);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constant.FETCH_VENDOR, response -> {

            try {
                // stopping swipe refresh
                swipeRefreshLayout.setRefreshing(false);

                JSONObject jsonResponse = new JSONObject(response);
                JSONArray jsonArrayVendor = jsonResponse.optJSONArray("json");
                if (jsonArrayVendor == null || jsonArrayVendor.isNull(0)) {
                    updateUI(NO_DATA, "No data found");
                } else {
                    for (int i = 0; i < jsonArrayVendor.length(); i++) {
                        JSONObject jsonChildNode = jsonArrayVendor.getJSONObject(i);

                        String vendor_name = jsonChildNode.optString("vendor_name");
                        String vendor_code = jsonChildNode.optString("vendor_code");
                        String vendor_image = jsonChildNode.optString("vendor_image");
                        String vendor_phone = jsonChildNode.optString("phone_main");
                        String amount = jsonChildNode.optString("amount");

                        Vendor vendor = new Vendor(vendor_code, vendor_name, vendor_phone, vendor_image, amount);
                        vendorList.add(vendor);
                    }
                    vendorAdapter.notifyDataSetChanged();
                }


            } catch (JSONException e) {
                Toast.makeText(this, R.string.json_erro, Toast.LENGTH_SHORT).show();
              /*  String error_message = "LoaderActivity Json error: "+ e.getMessage();
                Intent intentLike = new Intent(VendorActivity.this, ErrorService.class);
                intentLike.putExtra("error_message",error_message);
                startService(intentLike);*/
            }

        }, error -> {
            updateUI(NETWORK_ERROR, getResources().getString(R.string.no_internet));

        }) {
            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<>();
                params.put("agent_code", agent_code);
                if (date != null) {
                    params.put("date", date);
                }
                return params;
            }
        };

        RequestQueue requestQueue;
        requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    /**
     * Delete vendor
     */
    private void deleteVendor(String vendor_code) {
        // Tag used to cancel the request
        String tag_string_req = "req_login";
        hud.show();
        StringRequest strReq = new StringRequest(Request.Method.POST, Constant.DELETE_VENDOR, response -> {

            try {
                JSONObject jObj = new JSONObject(response);
                boolean error = jObj.getBoolean("error");
                String errorMsg = jObj.getString("error_msg");
                // Check for error node in json
                if (!error) {
                    Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_LONG).show();
                    vendorList.remove(position);
                    vendorAdapter.notifyItemRemoved(position);
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
                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    private void updateUI(int ACTION, String message) {
        switch (ACTION) {
            case NETWORK_ERROR:
                swipeRefreshLayout.setRefreshing(false);
                linearLayoutError.setVisibility(View.VISIBLE);
                imageViewNoInternet.setVisibility(View.VISIBLE);
                textViewRetry.setVisibility(View.VISIBLE);
                textViewError.setText(message);
                break;
            case NO_DATA:
                linearLayoutError.setVisibility(View.VISIBLE);
                imageViewNoInternet.setVisibility(View.GONE);
                textViewRetry.setVisibility(View.GONE);
                textViewError.setText(message);
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_log_out) {
            if (sessionManager.isLoggedIn()) {
                sessionManager.setLogin(false);
                sessionManager.deleteUserDetails();
                Intent intent = new Intent(VendorActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isAddvendor) {
            fetchData();
            isAddvendor = false;
        }
    }

    @Override
    public void onProcessFilter(String vendor_code, int position) {
        deleteVendor(vendor_code);
        this.position = position;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == 1) {
            if (data != null) {
                String amount = data.getStringExtra("amount");
                int position = data.getIntExtra("position",-1);

                if (amount != null && position >= 0){
                    amount = "₦ "+amount;
                    Vendor vendor = vendorList.get(position);

                    vendorList.set(position,new Vendor(vendor.getVendor_code(),vendor.getVendor_name(),vendor.getPhone_main(), vendor.getImage(),amount));
                    vendorAdapter.notifyDataSetChanged();
                }

            }
        }
    }
}
