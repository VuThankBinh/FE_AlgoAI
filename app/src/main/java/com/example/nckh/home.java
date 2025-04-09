package com.example.nckh;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nckh.adapter.BaiHocAdapter;
import com.example.nckh.model.BaiHoc;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class home extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private Button btnhoctiep;
    private Spinner spinnerSubjects;
    private TextView tvGreeting, getmail, tvStatus;
    private ImageView avt;
    private OkHttpClient client = new OkHttpClient();
    private SharedPreferences sharedPreferences;
    private RecyclerView recyclerViewLessons;
    private LinearLayout liner_lythuyet, liner_code;
    private BaiHocAdapter baiHocAdapter;
    private List<BaiHoc> baiHocList = new ArrayList<>();
    private Integer id_bai=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        // Khởi tạo SharedPreferences
        sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);

        // Khởi tạo các view
        tvGreeting = findViewById(R.id.tvGreeting);
        getmail = findViewById(R.id.getmail);
        tvStatus = findViewById(R.id.tvStatus);
        avt = findViewById(R.id.avt);
        recyclerViewLessons = findViewById(R.id.recyclerViewLessons);
        liner_code=findViewById(R.id.line_code);
        liner_lythuyet=findViewById(R.id.line_lythuyet);
        liner_lythuyet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(home.this, lythuyet.class);
                startActivity(intent);
            }
        });
        liner_code.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(home.this, thuchanhlist.class);
                startActivity(intent);
            }
        });
        // Cấu hình RecyclerView
        recyclerViewLessons.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewLessons.setNestedScrollingEnabled(false);
        recyclerViewLessons.setHasFixedSize(false);
        baiHocAdapter = new BaiHocAdapter(baiHocList);
        recyclerViewLessons.setAdapter(baiHocAdapter);

        // Gọi API lấy thông tin người dùng
        fetchUserInfo();

        // Xử lý Spinner môn học
        spinnerSubjects = findViewById(R.id.spinnerSubjects);
        String[] subjects = {"Thuật toán", "SQL server", "Lập trình OOP"};
        Integer[] subjectsIds = {12521068, 12521069, 12521070};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, subjects);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSubjects.setAdapter(adapter);

        spinnerSubjects.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedSubject = subjects[position];
                Integer subjectsId = subjectsIds[position];
                tvStatus.setText("Bạn đang học " + selectedSubject);
                id_bai=subjectsId;
                fetchBaiHocGanNhat();
                fetchDanhSachBaiHoc();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Không cần xử lý
            }
        });

        btnhoctiep=findViewById(R.id.btnhoctiep);
        btnhoctiep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(home.this, "adu", Toast.LENGTH_SHORT).show();
            }
        });

        // Xử lý Bottom Navigation
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.navigation_home);
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.navigation_home) {
                    return true;
                } else if (itemId == R.id.navigation_practice) {
                    Intent intent = new Intent(home.this, lythuyet.class);
                    startActivity(intent);
//                    finish();
                    return true;
                } else if (itemId == R.id.navigation_code) {
                    Intent intent = new Intent(home.this, thuchanhlist.class);
                    startActivity(intent);
//                    finish();
                    return true;
                } else if (itemId == R.id.navigation_profile) {
                    Intent intent = new Intent(home.this, taikhoan.class);
                    startActivity(intent);
//                    finish();
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
                    Toast.makeText(home.this, "Lỗi kết nối: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseData = response.body().string();
                        JSONObject jsonObject = new JSONObject(responseData);
                        sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("name", jsonObject.getString("tenDangNhap"));
                        editor.apply();
                        String name = jsonObject.getString("tenDangNhap");
                        String email = jsonObject.getString("email");
                        String avatar = jsonObject.getString("anhDaiDien");
                        System.out.println("name: "+name);

                        runOnUiThread(() -> {
                            tvGreeting.setText("Xin chào, " + name);
                            getmail.setText(email);

                            if (avatar != null && !avatar.isEmpty()) {
                                Picasso.get()
                                        .load(ApiConfig.getFullUrl(ApiConfig.get_imagge_ENDPOINT+avatar))
                                        .placeholder(R.drawable.user) // trong khi tải
                                        .error(R.drawable.user)       // lỗi thì dùng ảnh mặc định
                                        .into(avt);
                            } else {
                                avt.setImageResource(R.drawable.user);
                            }
                        });
                    } catch (JSONException e) {
                        runOnUiThread(() -> {
                            Toast.makeText(home.this, "Lỗi xử lý dữ liệu", Toast.LENGTH_SHORT).show();
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

    private void fetchBaiHocGanNhat() {
        // Lấy id user từ SharedPreferences
        String userId = sharedPreferences.getString("id", "");
        System.out.println("userId:"+userId);
        if (userId.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
            return;
        }

        Request request = new Request.Builder()
                .url(ApiConfig.getFullUrl(ApiConfig.get_bai_hoc_gan_nhat_ENDPOINT+userId + "&idKhoaHoc="+id_bai))
                .build();

        System.out.println(request.url().toString());
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(home.this, "Lỗi kết nối: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseData = response.body().string();
                        System.out.println("responseData:"+responseData);
                        if(responseData.isEmpty())
                            System.out.println(21123);
                        JSONObject jsonObject1 = new JSONObject(responseData);
                        String jsonObject2 = jsonObject1.getString("data");
                        JSONObject jsonObject = new JSONObject(jsonObject2);

                        String id_baihoc=jsonObject.getString("id");
                        String title = jsonObject.getString("tieuDe");
                        String noidung = jsonObject.getString("noiDung");
                        String linkYoutube=jsonObject.getString("linkYoutube");
                        String linkMoTa=jsonObject.getString("linkMoTa");
                        String trangThai=jsonObject.getString("trangThai");
                        String anhBaiHoc=jsonObject.getString("anhBaiHoc");

                        runOnUiThread(() -> {
                            TextView baihocgannhat= findViewById(R.id.baihocgannhat);
                            baihocgannhat.setVisibility(VISIBLE);
                            LinearLayout linner_baihocgannhat= findViewById(R.id.linner_baihocgannhat);
                            linner_baihocgannhat.setVisibility(VISIBLE);
                            ImageView img_baihoc =findViewById(R.id.img_baihoc);
                            TextView titlegannhat=findViewById(R.id.titlegannhat);
                            titlegannhat.setText(title);
                            TextView tinhtrangbaigannhat=findViewById(R.id.tinhtrangbaigannhat);
                            String trangthai="";
                            if(trangThai.equals("dang_hoc")) {
                                trangthai = "Đang học";
                                btnhoctiep.setVisibility(VISIBLE);
                            }
                            else if(trangThai.equals("chua_hoc"))
                                trangthai="Chưa học";
                            else if(trangThai.equals("da_hoc")){
                                btnhoctiep.setVisibility(GONE);
                                trangthai="Đã học";
                            }
                            tinhtrangbaigannhat.setText("Trạng thái: "+trangthai);


                            if (anhBaiHoc != null && !anhBaiHoc.isEmpty()) {
                                Picasso.get()
                                        .load(ApiConfig.get_imagge_ENDPOINT+anhBaiHoc)
                                        .placeholder(R.drawable.user) // trong khi tải
                                        .error(R.drawable.user)       // lỗi thì dùng ảnh mặc định
                                        .into(img_baihoc);
                            } else {
                                img_baihoc.setImageResource(R.drawable.user);
                            }
                        });
                    } catch (JSONException e) {
                        runOnUiThread(() -> {
                            Toast.makeText(home.this, "Lỗi xử lý dữ liệu", Toast.LENGTH_SHORT).show();
                            System.out.println("err:"+e.toString());
                        });
                    }
                }
                if(!response.isSuccessful()){
                    runOnUiThread(() -> {
//                        Toast.makeText(home.this, "Lỗi server: " + response.code(), Toast.LENGTH_SHORT).show();
                        TextView baihocgannhat= findViewById(R.id.baihocgannhat);
                        baihocgannhat.setVisibility(GONE);
                        LinearLayout linner_baihocgannhat= findViewById(R.id.linner_baihocgannhat);
                        linner_baihocgannhat.setVisibility(GONE);

                    });
                }
                else {
                    runOnUiThread(() -> {
//                        Toast.makeText(home.this, "Lỗi server: " + response.code(), Toast.LENGTH_SHORT).show();
                        System.out.println("errr: "+response.toString());
                    });
                }
            }
        });
    }

    private void fetchDanhSachBaiHoc() {
        String userId = sharedPreferences.getString("id", "");
        if (userId.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
            return;
        }

        Request request = new Request.Builder()
                .url(ApiConfig.getFullUrl(ApiConfig.GET_BAI_HOC_ENDPOINT + "idNguoiDung=" + userId + "&idKhoaHoc=" + id_bai + "&trangThai=chua_hoc"))
                .build();

        System.out.println("URL API: " + request.url().toString());

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(home.this, "Lỗi kết nối: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseData = response.body().string();
                        System.out.println("Response data: " + responseData);
                        
                        // Kiểm tra nếu response là mảng rỗng
                        if (responseData.isEmpty() || responseData.equals("[]")) {
                            runOnUiThread(() -> {
                                Toast.makeText(home.this, "Không có bài học nào", Toast.LENGTH_SHORT).show();
                                baiHocList.clear();
                                baiHocAdapter.notifyDataSetChanged();
                            });
                            return;
                        }

                        JSONArray jsonArray = new JSONArray(responseData);
                        System.out.println("Số lượng bài học: " + jsonArray.length());
                        
                        baiHocList.clear();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            BaiHoc baiHoc = new BaiHoc(
                                    jsonObject.getString("id"),
                                    jsonObject.getString("tieuDe"),
                                    jsonObject.getString("noiDung"),
                                    jsonObject.getString("linkYoutube"),
                                    jsonObject.getString("linkMoTa"),
                                    jsonObject.getString("trangThai"),
                                    jsonObject.getString("anhBaiHoc")
                            );
                            System.out.println("Thêm bài học: " + baiHoc.getTieuDe());
                            baiHocList.add(baiHoc);
                        }

                        runOnUiThread(() -> {
                            System.out.println("Số lượng bài học trong list: " + baiHocList.size());

                            baiHocAdapter.notifyDataSetChanged();
                        });
                    } catch (JSONException e) {
                        runOnUiThread(() -> {
                            Toast.makeText(home.this, "Lỗi xử lý dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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