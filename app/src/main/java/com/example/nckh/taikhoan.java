package com.example.nckh;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class taikhoan extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;
    LinearLayout in4tk,doipass,logout;
    private TextView tvGreeting, getmail, tvStatus;
    private ImageView avt;;
    private SharedPreferences sharedPreferences;
    private OkHttpClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_taikhoan);
        sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String user_id= sharedPreferences.getString("id", "");
// Xử lý Bottom Navigation
        client = new OkHttpClient();
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        in4tk=findViewById(R.id.in4tk);
        doipass=findViewById(R.id.doipass);
        logout=findViewById(R.id.logout);
        avt=findViewById(R.id.atv);
        tvGreeting=findViewById(R.id.tvGreeting);
        tvStatus=findViewById(R.id.tvStatus);
        fetchUserInfo();
        in4tk.setOnClickListener(v -> {
            Intent intent = new Intent(taikhoan.this, thongtintaikhoan.class);
            intent.putExtra("email", tvStatus.getText().toString());
            startActivity(intent);
        });
        doipass.setOnClickListener(v -> {
            Intent intent = new Intent(taikhoan.this, doimatkhau.class);
            intent.putExtra("email", tvStatus.getText().toString());
            startActivity(intent);
        });
        logout.setOnClickListener(v -> {
            Intent intent = new Intent(taikhoan.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();
            startActivity(intent);
            finish();
        });
        bottomNavigationView.setSelectedItemId(R.id.navigation_profile);
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.navigation_home) {
                    Intent intent = new Intent(taikhoan.this, home.class);
                    startActivity(intent);
//                    finish();
                    return true;
                } else if (itemId == R.id.navigation_practice) {
                    Intent intent = new Intent(taikhoan.this, lythuyet.class);
                    startActivity(intent);
//                    finish();
                    return true;
                } else if (itemId == R.id.navigation_code) {
                    Intent intent = new Intent(taikhoan.this, thuchanhlist.class);
                    startActivity(intent);
//                    finish();
                    return true;
                } else if (itemId == R.id.navigation_profile) {
                    return true;
                }
                return false;
            }
        });

    }
    private void fetchUserInfo() {
        // Lấy id user từ SharedPreferences
        String userId = sharedPreferences.getString("id", "");
        System.out.println("userId:"+userId);
        if (userId.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
            return;
        }

        Request request = new Request.Builder()
                .url(ApiConfig.getFullUrl(ApiConfig.GET_USER_INFO_ENDPOINT+userId))
                .build();

        System.out.println(request.url().toString());
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(taikhoan.this, "Lỗi kết nối: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseData = response.body().string();
                        JSONObject jsonObject = new JSONObject(responseData);

                        String name = jsonObject.getString("tenDangNhap");
                        String email = jsonObject.getString("email");
                        String avatar = jsonObject.getString("tenDangNhap");
                        System.out.println("name: "+name);

                        runOnUiThread(() -> {
                            tvGreeting.setText(name);
                            tvStatus.setText(email);

                            if (avatar != null && !avatar.isEmpty()) {
                                Glide.with(taikhoan.this)
                                        .load(ApiConfig.getFullUrl(ApiConfig.get_imagge_ENDPOINT + avatar))
                                        .placeholder(R.drawable.user)
                                        .error(R.drawable.user)
                                        .circleCrop() // hoặc .circleCrop() nếu muốn avatar tròn
                                        .into(avt);
                            } else {
                                avt.setImageResource(R.drawable.user);
                            }
                        });
                    } catch (JSONException e) {
                        runOnUiThread(() -> {
                            Toast.makeText(taikhoan.this, "Lỗi xử lý dữ liệu", Toast.LENGTH_SHORT).show();
                        });
                    }
                } else {
                    runOnUiThread(() -> {
                        System.out.println("errr: "+response.toString());
                    });
                }
            }
        });
    }
}