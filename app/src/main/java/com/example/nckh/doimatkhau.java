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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class doimatkhau extends AppCompatActivity {
    private TextInputEditText matKhauEditText, matKhauNewEditText, xacNhanMatKhauEditText;
    private TextInputLayout matKhauLayout, matKhauNewLayout, xacNhanMatKhauLayout;
    private Button btnSave;
    String email="";
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doimatkhau);

        // Khởi tạo SharedPreferences
        sharedPreferences = getSharedPreferences("user_data", MODE_PRIVATE);
        Intent intent =getIntent();
        email=intent.getStringExtra("email");
        // Ánh xạ các view
        matKhauEditText = findViewById(R.id.matKhauEditText);
        matKhauNewEditText = findViewById(R.id.matKhauNewEditText);
        xacNhanMatKhauEditText = findViewById(R.id.xacNhanMatKhauEditText);
        matKhauLayout = findViewById(R.id.matKhauLayout);
        matKhauNewLayout = findViewById(R.id.matKhauNewLayout);
        xacNhanMatKhauLayout = findViewById(R.id.xacNhanMatKhauLayout);
        btnSave = findViewById(R.id.btnSave);

        // Xử lý sự kiện click nút Lưu
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInput()) {
                    changePassword();
                }
            }
        });
    }

    private boolean validateInput() {
        boolean isValid = true;
        String matKhau = matKhauEditText.getText().toString().trim();
        String matKhauMoi = matKhauNewEditText.getText().toString().trim();
        String xacNhanMatKhau = xacNhanMatKhauEditText.getText().toString().trim();

        // Kiểm tra mật khẩu cũ
        if (matKhau.isEmpty()) {
            matKhauLayout.setError("Vui lòng nhập mật khẩu cũ");
            isValid = false;
        } else {
            matKhauLayout.setError(null);
        }

        // Kiểm tra mật khẩu mới
        if (matKhauMoi.isEmpty()) {
            matKhauNewLayout.setError("Vui lòng nhập mật khẩu mới");
            isValid = false;
        } else if (matKhauMoi.length() < 6) {
            matKhauNewLayout.setError("Mật khẩu phải có ít nhất 6 ký tự");
            isValid = false;
        } else if (!matKhauMoi.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$")) {
            matKhauNewLayout.setError("Mật khẩu phải có ít nhất 1 chữ thường, 1 chữ hoa và 1 số");
            isValid = false;
        } else {
            matKhauNewLayout.setError(null);
        }

        // Kiểm tra xác nhận mật khẩu
        if (xacNhanMatKhau.isEmpty()) {
            xacNhanMatKhauLayout.setError("Vui lòng xác nhận mật khẩu");
            isValid = false;
        } else if (!xacNhanMatKhau.equals(matKhauMoi)) {
            xacNhanMatKhauLayout.setError("Mật khẩu xác nhận không khớp");
            isValid = false;
        } else {
            xacNhanMatKhauLayout.setError(null);
        }

        return isValid;
    }

    private void changePassword() {
        String matKhau = matKhauEditText.getText().toString().trim();
        String matKhauMoi = matKhauNewEditText.getText().toString().trim();
        String xacNhanMatKhau = xacNhanMatKhauEditText.getText().toString().trim();
        OkHttpClient client=new OkHttpClient();
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("email", email);
            jsonObject.put("matKhauHienTai", matKhau);
            jsonObject.put("matKhauMoi", matKhauMoi);
            jsonObject.put("xacNhanMatKhauMoi", xacNhanMatKhau);
        } catch (Exception e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(
                MediaType.parse("application/json"),
                jsonObject.toString()
        );

        Request request = new Request.Builder()
                .url(ApiConfig.getFullUrl(ApiConfig.POST_Doi_Pass_ENDPOINT))
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

                    runOnUiThread(() -> {
                        if (response.isSuccessful()) {
                            showCustomToast(message, R.drawable.baseline_check_24);
                            Intent intent = new Intent(doimatkhau.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.clear();
                            editor.apply();
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
