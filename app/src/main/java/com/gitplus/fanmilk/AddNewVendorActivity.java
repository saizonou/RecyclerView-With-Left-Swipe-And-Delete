package com.gitplus.fanmilk;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.asksira.bsimagepicker.BSImagePicker;
import com.bumptech.glide.Glide;
import com.gitplus.fanmilk.app.AppController;
import com.gitplus.fanmilk.helper.Constant;
import com.gitplus.fanmilk.helper.FileUtil;
import com.gitplus.fanmilk.helper.SessionManager;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.mikhaellopez.circularimageview.CircularImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import id.zelory.compressor.Compressor;

public class AddNewVendorActivity extends AppCompatActivity implements BSImagePicker.OnMultiImageSelectedListener {

    private EditText editTextName, editTextAddress, editTextPhone2, editTextEmail, editTextPhone;
    private TextInputLayout inputLayoutName, inputLayoutAddress, inputLayoutPhone;
    private String name, address, phone2, email, phone, agent_code, imageString;
    //Custom progressBar
    private KProgressHUD hud;
    //The image Picker
    private BSImagePicker pickerDialog;
    //CircularView
    private CircularImageView profileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_vendor);

        SessionManager sessionManager = new SessionManager(this);
        HashMap<String,String> user = sessionManager.readUserDetails();
        agent_code = user.get(sessionManager.AGENT_CODE);
        initView();
        hud = KProgressHUD.create(this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setCancellable(false)
                .setLabel("Loading...")
                .setDimAmount(0.5f);
        findViewById(R.id.submit).setOnClickListener(v -> getData());

    }

    /**
     * Initialize the views
     */
    private void initView() {
        editTextName = findViewById(R.id.name);
        editTextAddress = findViewById(R.id.address);
        editTextPhone2 = findViewById(R.id.phone2);
        editTextEmail = findViewById(R.id.email);
        editTextPhone = findViewById(R.id.phone);

        inputLayoutName = findViewById(R.id.inputName);
        inputLayoutAddress = findViewById(R.id.inputAddress);
        inputLayoutPhone = findViewById(R.id.inputPhone);

        profileImage = findViewById(R.id.img_profile);

        findViewById(R.id.img_plus).setOnClickListener(v -> {
            pickerDialog = new BSImagePicker.Builder("com.gitplus.fanmilk.fileprovider")
                    .setMaximumDisplayingImages(Integer.MAX_VALUE)
                    .isMultiSelect()
                    .setMinimumMultiSelectCount(1)
                    .setMaximumMultiSelectCount(1)
                    .build();
            pickerDialog.show(AddNewVendorActivity.this.getSupportFragmentManager(), "picker");
        });

        findViewById(R.id.submit).setOnClickListener(v -> sendData());
    }

    /**
     * Get the data from editext
     */
    private void getData(){
        //inputLayoutName.setEnabled(false);
        //inputLayoutAddress.setEnabled(false);
        //inputLayoutPhone.setEnabled(false);

        name = editTextName.getText().toString().trim();
        address = editTextAddress.getText().toString().trim();
        phone2 = editTextPhone2.getText().toString().trim();
        email = editTextEmail.getText().toString().trim();
        phone = editTextPhone.getText().toString().trim();

        boolean isError = false;

        if (name.isEmpty()){
            inputLayoutName.setError("Name is required");
            isError = true;
        }

        if (address.isEmpty()){
            inputLayoutAddress.setError("Address is required");
            isError = true;
        }

        if (phone.isEmpty()){
            inputLayoutPhone.setError("Phone is required");
            isError = true;
        }

        if (imageString == null) {
            Toast.makeText(this, "Vondor image required", Toast.LENGTH_SHORT).show();
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
        StringRequest strReq = new StringRequest(Request.Method.POST, Constant.ADD_VENDOR, response -> {

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
                params.put("vendor_name", name);
                params.put("vendor_image", imageString);
                params.put("vendor_address", address);
                if (email != null) {
                    params.put("phone_extra", phone2);
                }
                if (email != null) {
                    params.put("email", email);
                }
                params.put("phone_main", phone);
                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    /**
     * Compress the user image
     * @param actualImage the image to be compressed
     * @return return the compressed image in a bitmap format
     */
    public Bitmap customCompressImage(File actualImage) {
        Bitmap bitmap = null;
        if (actualImage == null) {
            Toast.makeText(this, "Please choose an image", Toast.LENGTH_SHORT).show();
        } else {
            // Compress image in main thread using custom Compressor
            try {
                bitmap = new Compressor(this)
                        .setMaxWidth(640)
                        .setMaxHeight(480)
                        .setQuality(75)
                        .setCompressFormat(Bitmap.CompressFormat.WEBP)
                        .setDestinationDirectoryPath(Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_PICTURES).getAbsolutePath())
                        .compressToBitmap(actualImage);

            } catch (IOException e) {
                e.printStackTrace();
                e.getMessage();
            }
        }
        return bitmap;
    }

    /**
     * Convert the bitmap to string
     * @param bitmap the bitmap of the image to be converted
     * @return return the image in a string
     */
    private String imageToString(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream);
        byte[] imageBytes = outputStream.toByteArray();

        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    @Override
    public void onMultiImageSelected(List<Uri> uriList) {

        Glide.with(AddNewVendorActivity.this)
                .load(uriList.get(0))
                .into(profileImage);

        try {
            File actualImage = FileUtil.from(this, uriList.get(0));
            Bitmap bitmap = customCompressImage(actualImage);
            imageString = imageToString(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
            finish();
        });

        alertDialog.setCancelable(false);
        alertDialog.show();
    }
}
