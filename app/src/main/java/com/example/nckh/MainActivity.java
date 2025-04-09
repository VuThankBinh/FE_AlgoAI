package com.example.nckh;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private TextView registerLink, forgotPasswordTextView;
    private CheckBox showPasswordCheckBox;
    private OkHttpClient client;
    private SharedPreferences sharedPreferences;
    private ImageView loadingImageView;
    private LinearLayout loadingLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Khởi tạo SharedPreferences
        sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
//        clearLoginInfo();
        // Ánh xạ loading views
        loadingLayout = findViewById(R.id.loadingLayout);
        loadingImageView = findViewById(R.id.loadingImageView);

        // Khởi tạo OkHttpClient
        client = new OkHttpClient();

        // Ánh xạ các view
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.registerButton);
        registerLink = findViewById(R.id.registerLink);
        forgotPasswordTextView = findViewById(R.id.forgotPasswordTextView);
        showPasswordCheckBox = findViewById(R.id.showPasswordCheckBox);

        // Xử lý hiển thị mật khẩu
        showPasswordCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int inputType = isChecked ? 
                android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD : 
                android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD;
            passwordEditText.setInputType(inputType);
        });

        // Xử lý đăng nhập
        loginButton.setOnClickListener(v -> login());

        // Xử lý chuyển đến màn hình đăng ký
        registerLink.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, dangky.class));
        });
        // Xử lý quên mật khẩu
        forgotPasswordTextView.setOnClickListener(v -> {
        String email= emailEditText.getText().toString().trim();
            if(email.isEmpty()){
                showCustomToast("Bạn phải nhập email", R.drawable.baseline_error_24);
                return;
            }
            if (!isValidEmail(email)) {
                showCustomToast("Email không hợp lệ", R.drawable.baseline_error_24);
                return;
            }
            sendOTP();


        });

        // Kiểm tra đăng nhập tự động
        if (isLoggedIn()) {
            if (isTokenValid()) {
                showLoading();
                checkTokenAndAutoLogin();
            } else {
                clearLoginInfo();
                showCustomToast("Phiên đăng nhập đã hết hạn", R.drawable.baseline_error_24);
            }
        }
    }
    private void sendOTP() {
        String email = emailEditText.getText().toString().trim();

        if (email.isEmpty()) {
            showCustomToast("Vui lòng nhập email", R.drawable.baseline_error_24);
            return;
        }

        if (!isValidEmail(email)) {
            showCustomToast("Email không hợp lệ", R.drawable.baseline_error_24);
            return;
        }

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("email", email);
        } catch (Exception e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(
                MediaType.parse("application/json"),
                jsonObject.toString()
        );

        Request request = new Request.Builder()
                .url(ApiConfig.getFullUrl(ApiConfig.SEND_OTP_Reset_ENDPOINT))
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        showCustomToast("Lỗi kết nối: " + e.getMessage(), R.drawable.baseline_error_24)
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String responseBody = response.body().string();
                    JSONObject jsonResponse = new JSONObject(responseBody);

                    String status = jsonResponse.getString("status");
                    String message = jsonResponse.getString("message");
                    String data = jsonResponse.getString("data");

                    runOnUiThread(() -> {
                        if (response.isSuccessful()) {
                            showCustomToast(message, R.drawable.baseline_check_24);
                            Intent intent = new Intent(MainActivity.this, sendOTP.class);
                            intent.putExtra("email",email);
                            startActivity(intent);

                        } else {
                            showCustomToast("Lỗi: " + message, R.drawable.baseline_error_24);
                        }
                    });
                } catch (Exception e) {
                    runOnUiThread(() ->
                            showCustomToast("Lỗi xử lý dữ liệu: " + e.getMessage(), R.drawable.baseline_error_24)
                    );
                }
            }
        });
    }
    private boolean isValidEmail(String email) {
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        return email.matches(emailPattern);
    }

    private boolean isTokenValid() {
        long expirationTime = sharedPreferences.getLong("tokenExpiration", 0);
        return expirationTime > System.currentTimeMillis();
    }

    private void showLoading() {
        loadingLayout.setVisibility(View.VISIBLE);
        Animation rotateAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate_animation);
        loadingImageView.startAnimation(rotateAnimation);
    }

    private void hideLoading() {
        loadingLayout.setVisibility(View.GONE);
        loadingImageView.clearAnimation();
    }

    private void checkTokenAndAutoLogin() {
        showLoading();
        String token = sharedPreferences.getString("token", "");
        String sessionId = sharedPreferences.getString("sessionId", "");

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("token", token);
            jsonObject.put("sessionId", sessionId);
        } catch (Exception e) {
            e.printStackTrace();
            hideLoading();
            return;
        }

        RequestBody body = RequestBody.create(
            MediaType.parse("application/json"), 
            jsonObject.toString()
        );

        Request request = new Request.Builder()
            .url(ApiConfig.getFullUrl(ApiConfig.VALIDATE_TOKEN_ENDPOINT))
            .post(body)
            .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    hideLoading();
                    showCustomToast("Lỗi kết nối: " + e.getMessage(), R.drawable.baseline_error_24);
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                runOnUiThread(() -> {
                    hideLoading();
                    if (response.isSuccessful()) {
                        // Token còn hợp lệ, chuyển đến màn hình chính
                        startActivity(new Intent(MainActivity.this, home.class));
                        finish();
                    } else {
                        // Token hết hạn, xóa thông tin đăng nhập
                        clearLoginInfo();
                        showCustomToast("Phiên đăng nhập đã hết hạn", R.drawable.baseline_error_24);
                    }
                });
            }
        });
    }

    private void clearLoginInfo() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    private void login() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showCustomToast("Vui lòng điền đầy đủ thông tin", R.drawable.baseline_error_24);
            return;
        }

        // Kiểm tra định dạng email
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        if (!email.matches(emailPattern)) {
            showCustomToast("Email không đúng định dạng", R.drawable.baseline_error_24);
            return;
        }

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("email", email);
            jsonObject.put("matKhau", password);
        } catch (Exception e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(
            MediaType.parse("application/json"), 
            jsonObject.toString()
        );

        Request request = new Request.Builder()
            .url(ApiConfig.getFullUrl(ApiConfig.LOGIN_ENDPOINT))
            .post(body)
            .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        showCustomToast("Lỗi kết nối: " + e.getMessage(), R.drawable.baseline_error_24)
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String responseBody = response.body().string();
                    JSONObject jsonResponse = new JSONObject(responseBody);
                    
                    String status = jsonResponse.getString("status");
                    String message = jsonResponse.getString("message");
                    JSONObject data = jsonResponse.getJSONObject("data");

                    runOnUiThread(() -> {
                        if (response.isSuccessful()) {
                            // Lưu thông tin đăng nhập
                            saveLoginInfo(data);
                            showCustomToast(message, R.drawable.baseline_check_24);
                            // Chuyển đến màn hình chính
                            startActivity(new Intent(MainActivity.this, home.class));
                            finish();
                        } else {
                            showCustomToast("Lỗi: " + message, R.drawable.baseline_error_24);
                        }
                    });
                } catch (Exception e) {
                    runOnUiThread(() ->
                            showCustomToast("Lỗi xử lý dữ liệu: " + e.getMessage(), R.drawable.baseline_error_24)
                    );
                }
            }
        });
    }

    private void saveLoginInfo(JSONObject data) {
        try {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("token", data.getString("token"));
            editor.putString("sessionId", data.getString("sessionId"));
            editor.putString("id", data.getString("id"));
            
            // Lưu thời gian hết hạn của token (ví dụ: 24 giờ)
            long expirationTime = System.currentTimeMillis() + (24 * 60 * 60 * 1000);
            editor.putLong("tokenExpiration", expirationTime);
            
            editor.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isLoggedIn() {
        return sharedPreferences.contains("token") && sharedPreferences.contains("sessionId");
    }

    private void showCustomToast(String message, int iconResId) {
        LayoutInflater inflater = LayoutInflater.from(this);
        LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.custom_toast, findViewById(R.id.toast_container));

        TextView toastText = layout.findViewById(R.id.toast_text);
        ImageView toastIcon = layout.findViewById(R.id.toast_icon);

        toastText.setText(message);
        toastIcon.setImageResource(iconResId);

        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.BOTTOM, 0, 100); // Adjust position as needed
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);

        // Load animations
        Animation animIn = AnimationUtils.loadAnimation(this, R.anim.toast_in);
        Animation animOut = AnimationUtils.loadAnimation(this, R.anim.toast_out);

        // Apply enter animation when setting the view
        layout.startAnimation(animIn);

        toast.show();

        // Apply exit animation after the duration
        layout.postDelayed(() -> layout.startAnimation(animOut), 2000); // 2000ms = 2 seconds (adjust as needed, should be longer than toast duration)
    }
}