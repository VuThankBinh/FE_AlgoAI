package com.example.nckh;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class dangky extends AppCompatActivity {
    private TextInputEditText tenDangNhapEditText, emailEditText, matKhauEditText, xacNhanMatKhauEditText, otpEditText;
    private TextView sendOTPTextView, loginLink;
    private Button registerButton;
    private OkHttpClient client;
    private CountDownTimer countDownTimer;
    private boolean isTimerRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dangky);

        // Khởi tạo OkHttpClient
        client = new OkHttpClient();

        // Ánh xạ các view
        tenDangNhapEditText = findViewById(R.id.tenDangNhapEditText);
        emailEditText = findViewById(R.id.emailEditText);
        matKhauEditText = findViewById(R.id.matKhauEditText);
        xacNhanMatKhauEditText = findViewById(R.id.xacNhanMatKhauEditText);
        otpEditText = findViewById(R.id.otpEditText);
        sendOTPTextView = findViewById(R.id.sendOTP);
        registerButton = findViewById(R.id.registerButton);
        loginLink=findViewById(R.id.LoginLink);

        // Xử lý sự kiện gửi OTP
        sendOTPTextView.setOnClickListener(v -> {
            if (!isTimerRunning) {
                sendOTP();
            }
        });

        // Xử lý sự kiện đăng ký
        registerButton.setOnClickListener(v -> register());

        // Xử lý chuyển đến màn hình đăng ký
        loginLink.setOnClickListener(v -> { this.finish();});
    }

    private void startCountdown() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        isTimerRunning = true;
        sendOTPTextView.setEnabled(false);

        countDownTimer = new CountDownTimer(300000, 1000) { // 5 phút = 300000 milliseconds
            @Override
            public void onTick(long millisUntilFinished) {
                long minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished);
                long seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60;
                String timeLeft = String.format("%02d:%02d", minutes, seconds);
                runOnUiThread(() -> sendOTPTextView.setText("Gửi lại sau " + timeLeft));
            }

            @Override
            public void onFinish() {
                isTimerRunning = false;
                runOnUiThread(() -> {
                    sendOTPTextView.setText("Gửi mã OTP");
                    sendOTPTextView.setEnabled(true);
                });
            }
        }.start();
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
            .url(ApiConfig.getFullUrl(ApiConfig.SEND_OTP_ENDPOINT))
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
                            startCountdown();
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

    private void register() {
        String username = tenDangNhapEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = matKhauEditText.getText().toString().trim();
        String confirmPassword = xacNhanMatKhauEditText.getText().toString().trim();
        String otp = otpEditText.getText().toString().trim();

        // Kiểm tra dữ liệu đầu vào
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || otp.isEmpty()) {
            showCustomToast("Vui lòng điền đầy đủ thông tin", R.drawable.baseline_error_24);
            return;
        }

        if (!isValidEmail(email)) {
            showCustomToast("Email không hợp lệ", R.drawable.baseline_error_24);
            return;
        }

        if (!password.equals(confirmPassword)) {
            showCustomToast("Mật khẩu không khớp", R.drawable.baseline_error_24);
            return;
        }

        if (password.length() < 6) {
            showCustomToast("Mật khẩu phải có ít nhất 6 ký tự", R.drawable.baseline_error_24);
            return;
        }

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("tenDangNhap", username);
            jsonObject.put("email", email);
            jsonObject.put("matKhau", password);
            jsonObject.put("xacNhanMatKhau", confirmPassword);
            jsonObject.put("otp", otp);
        } catch (Exception e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(
            MediaType.parse("application/json"), 
            jsonObject.toString()
        );

        Request request = new Request.Builder()
            .url(ApiConfig.getFullUrl(ApiConfig.REGISTER_ENDPOINT))
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
                            // Chuyển đến màn hình đăng nhập hoặc màn hình chính
                            Intent intent = new Intent(dangky.this, MainActivity.class);
                            startActivity(intent);
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

    private boolean isValidEmail(String email) {
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        return email.matches(emailPattern);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}