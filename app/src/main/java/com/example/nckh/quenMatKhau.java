package com.example.nckh;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class quenMatKhau extends AppCompatActivity {
    private TextInputEditText matKhauEditText, xacNhanMatKhauEditText;
    private Button sendInstructionsButton;
    private String email;
    private OkHttpClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quen_mat_khau);

        // Khởi tạo OkHttpClient
        client = new OkHttpClient();

        // Lấy email từ Intent
        Intent intent = getIntent();
        email = intent.getStringExtra("email");
        email="nguyendinhvuongabc1@gmail.com";
        System.out.println("email: "+email);


        // Ánh xạ các view
        matKhauEditText = findViewById(R.id.matKhauEditText);
        xacNhanMatKhauEditText = findViewById(R.id.xacNhanMatKhauEditText);
        sendInstructionsButton = findViewById(R.id.sendInstructionsButton);

        // Xử lý sự kiện đặt lại mật khẩu
        sendInstructionsButton.setOnClickListener(v -> resetPassword());
    }

    private void resetPassword() {
        String matKhauMoi = matKhauEditText.getText().toString().trim();
        String xacNhanMatKhauMoi = xacNhanMatKhauEditText.getText().toString().trim();

        // Kiểm tra mật khẩu trống
        if (matKhauMoi.isEmpty() || xacNhanMatKhauMoi.isEmpty()) {
            showCustomToast("Vui lòng nhập đầy đủ thông tin", R.drawable.baseline_error_24);
            return;
        }

        // Kiểm tra mật khẩu khớp
        if (!matKhauMoi.equals(xacNhanMatKhauMoi)) {
            showCustomToast("Mật khẩu không khớp", R.drawable.baseline_error_24);
            return;
        }

        // Kiểm tra độ dài mật khẩu
        if (matKhauMoi.length() < 6) {
            showCustomToast("Mật khẩu phải có ít nhất 6 ký tự", R.drawable.baseline_error_24);
            return;
        }

        // Tạo JSON object
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("email", email);
            jsonObject.put("matKhauMoi", matKhauMoi);
            jsonObject.put("xacNhanMatKhauMoi", xacNhanMatKhauMoi);
            jsonObject.put("otp", "string");
        } catch (Exception e) {
            e.printStackTrace();
            showCustomToast("Lỗi tạo dữ liệu", R.drawable.baseline_error_24);
            return;
        }

        // Tạo request body
        RequestBody body = RequestBody.create(
            MediaType.parse("application/json"),
            jsonObject.toString()
        );

        // Tạo request
        Request request = new Request.Builder()
            .url(ApiConfig.getFullUrl(ApiConfig.RESET_PASSWORD_ENDPOINT))
            .post(body)
            .build();

        // Chạy request trong background thread
        new Thread(() -> {
            try {
                Response response = client.newCall(request).execute();
                String responseBody = response.body().string();
                JSONObject jsonResponse = new JSONObject(responseBody);
                
                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        showCustomToast("Đặt lại mật khẩu thành công", R.drawable.baseline_check_24);
                        // Chuyển về màn hình đăng nhập
                        startActivity(new Intent(quenMatKhau.this, MainActivity.class));
                        finish();
                    } else {
                        String message = jsonResponse.optString("message", "Đặt lại mật khẩu thất bại");
                        showCustomToast(message, R.drawable.baseline_error_24);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    System.out.println("claude err : " + e.toString());
                    showCustomToast("Lỗi: " + e.getMessage(), R.drawable.baseline_error_24);
                });
            }
        }).start();
    }

    private void showCustomToast(String message, int iconResId) {
        LayoutInflater inflater = LayoutInflater.from(this);
        LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.custom_toast, findViewById(R.id.toast_container));

        TextView toastText = layout.findViewById(R.id.toast_text);
        ImageView toastIcon = layout.findViewById(R.id.toast_icon);

        toastText.setText(message);
        toastIcon.setImageResource(iconResId);

        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.BOTTOM, 0, 100);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);

        // Load animations
        Animation animIn = AnimationUtils.loadAnimation(this, R.anim.toast_in);
        Animation animOut = AnimationUtils.loadAnimation(this, R.anim.toast_out);

        // Apply enter animation when setting the view
        layout.startAnimation(animIn);

        toast.show();

        // Apply exit animation after the duration
        layout.postDelayed(() -> layout.startAnimation(animOut), 2000);
    }
}