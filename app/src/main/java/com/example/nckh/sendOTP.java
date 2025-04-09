package com.example.nckh;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class sendOTP extends AppCompatActivity {
    private String email;
    private EditText[] otpEditTexts;
    private TextView resendOTP;
    private Button verifyButton;
    private CountDownTimer countDownTimer;
    private OkHttpClient client;
    private TextView emailTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_otp);

        // Khởi tạo OkHttpClient
        client = new OkHttpClient();

        // Lấy email từ Intent
        Intent intent = getIntent();
        email = intent.getStringExtra("email");

        // Ánh xạ các view
        emailTextView = findViewById(R.id.emailSendOTP);
        emailTextView.setText(email);

        resendOTP = findViewById(R.id.sendOTP);
        verifyButton = findViewById(R.id.btnVerifyOTP);

        // Khởi tạo mảng EditText cho OTP
        otpEditTexts = new EditText[]{
            findViewById(R.id.otpDigit1),
            findViewById(R.id.otpDigit2),
            findViewById(R.id.otpDigit3),
            findViewById(R.id.otpDigit4),
            findViewById(R.id.otpDigit5),
            findViewById(R.id.otpDigit6)
        };

        // Thiết lập TextWatcher cho các EditText
        setupOTPEditTexts();

        // Bắt đầu đếm ngược
        startCountdown();

        // Xử lý sự kiện gửi lại OTP
        resendOTP.setOnClickListener(v -> {
            if (resendOTP.isEnabled()) {
                resendOTP();
            }
        });

        // Xử lý sự kiện xác thực OTP
        verifyButton.setOnClickListener(v -> verifyOTP());
    }

    private void setupOTPEditTexts() {
        for (int i = 0; i < otpEditTexts.length; i++) {
            final int currentIndex = i;
            otpEditTexts[i].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 1 && currentIndex < otpEditTexts.length - 1) {
                        otpEditTexts[currentIndex + 1].requestFocus();
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });

            // Xử lý sự kiện xóa ký tự
            otpEditTexts[i].setOnKeyListener((v, keyCode, event) -> {
                if (keyCode == android.view.KeyEvent.KEYCODE_DEL && event.getAction() == android.view.KeyEvent.ACTION_DOWN) {
                    if (otpEditTexts[currentIndex].getText().length() == 0 && currentIndex > 0) {
                        // Nếu ô hiện tại trống và không phải ô đầu tiên
                        otpEditTexts[currentIndex - 1].requestFocus();
                        otpEditTexts[currentIndex - 1].setText("");
                        return true;
                    }
                }
                return false;
            });

            // Xử lý sự kiện paste
            otpEditTexts[i].setOnLongClickListener(v -> {
                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                if (clipboard.hasPrimaryClip()) {
                    android.content.ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
                    String pastedText = item.getText().toString();
                    if (pastedText != null && pastedText.matches("[0-9]+")) {
                        // Nếu dữ liệu paste là số
                        int startIndex = currentIndex;
                        int endIndex = Math.min(startIndex + pastedText.length(), otpEditTexts.length);
                        
                        for (int j = startIndex; j < endIndex; j++) {
                            otpEditTexts[j].setText(String.valueOf(pastedText.charAt(j - startIndex)));
                        }
                        
                        if (endIndex < otpEditTexts.length) {
                            otpEditTexts[endIndex].requestFocus();
                        }
                        return true;
                    }
                }
                return false;
            });
        }
    }

    private void startCountdown() {
        resendOTP.setEnabled(false);
        countDownTimer = new CountDownTimer(5 * 60 * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long minutes = millisUntilFinished / 60000;
                long seconds = (millisUntilFinished % 60000) / 1000;
                resendOTP.setText(String.format("Gửi lại sau %02d:%02d", minutes, seconds));
            }

            @Override
            public void onFinish() {
                resendOTP.setEnabled(true);
                resendOTP.setText("Gửi lại mã OTP");
            }
        }.start();
    }

    private void resendOTP() {
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
                            Intent intent = new Intent(sendOTP.this, quenMatKhau.class);
                            intent.putExtra("email",email);
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

    private void verifyOTP() {
        StringBuilder otpBuilder = new StringBuilder();
        for (EditText editText : otpEditTexts) {
            otpBuilder.append(editText.getText().toString());
        }
        String otp = otpBuilder.toString();

        if (otp.length() != 6) {
            showCustomToast("Vui lòng nhập đủ 6 chữ số", R.drawable.baseline_error_24);
            return;
        }

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("email", email);
            jsonObject.put("otp", otp);
        } catch (Exception e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(
            MediaType.parse("application/json"),
            jsonObject.toString()
        );

        Request request = new Request.Builder()
            .url(ApiConfig.getFullUrl(ApiConfig.VERIFY_OTP_ENDPOINT))
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
                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        showCustomToast("Xác thực thành công", R.drawable.baseline_check_24);
                        // Chuyển đến màn hình đặt lại mật khẩu
                        Intent intent = new Intent(sendOTP.this, quenMatKhau.class);
                        intent.putExtra("email", email);
                        System.out.println("email: "+email);
                        startActivity(intent);
                        finish();
                    } else {
                        showCustomToast("Mã OTP không đúng", R.drawable.baseline_error_24);
                    }
                });
            }
        });
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
    private boolean isValidEmail(String email) {
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        return email.matches(emailPattern);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}