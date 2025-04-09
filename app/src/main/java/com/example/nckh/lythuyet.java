package com.example.nckh;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nckh.adapter.BaiHocAdapter;
import com.example.nckh.adapter.BaiHocAdapterNgang;
import com.example.nckh.model.BaiHoc;
import com.google.android.material.bottomnavigation.BottomNavigationView;

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

public class lythuyet extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private Spinner spinnerSubjects;
    private RecyclerView recyclerViewLessonsCompleted;
    private RecyclerView recyclerViewLessonsLearning;
    private RecyclerView recyclerViewLessons;
    private BaiHocAdapterNgang adapterCompleted;
    private BaiHocAdapterNgang adapterLearning;
    private BaiHocAdapter adapterNotStarted;
    private List<BaiHoc> listCompleted = new ArrayList<>();
    private List<BaiHoc> listLearning = new ArrayList<>();
    private List<BaiHoc> listNotStarted = new ArrayList<>();
    private OkHttpClient client = new OkHttpClient();
    private SharedPreferences sharedPreferences;
    private Integer id_bai = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lythuyet);

        // Khởi tạo SharedPreferences
        sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);

        // Khởi tạo các view
        spinnerSubjects = findViewById(R.id.spinnerSubjects);
        recyclerViewLessonsCompleted = findViewById(R.id.recyclerViewLessonsCompleted);
        recyclerViewLessonsLearning = findViewById(R.id.recyclerViewLessonsLearning);
        recyclerViewLessons = findViewById(R.id.recyclerViewLessons);

        // Khởi tạo adapter
        adapterCompleted = new BaiHocAdapterNgang(listCompleted);
        adapterLearning = new BaiHocAdapterNgang(listLearning);
        adapterNotStarted = new BaiHocAdapter(listNotStarted);

        // Cấu hình RecyclerView cho bài học đã hoàn thành và đang học (ngang)
        setupRecyclerViewHorizontal(recyclerViewLessonsCompleted, adapterCompleted);
        setupRecyclerViewHorizontal(recyclerViewLessonsLearning, adapterLearning);
        
        // Cấu hình RecyclerView cho bài học chưa học (dọc)
        setupRecyclerViewVertical(recyclerViewLessons, adapterNotStarted);

        // Xử lý Spinner môn học
        String[] subjects = {"Thuật toán", "SQL server", "Lập trình OOP"};
        Integer[] subjectsIds = {12521068, 12521069, 12521070};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, subjects);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSubjects.setAdapter(adapter);

        spinnerSubjects.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                id_bai = subjectsIds[position];
                fetchBaiHoc("da_hoc"); // Bài học đã hoàn thành
                fetchBaiHoc("dang_hoc"); // Bài học đang học
                fetchBaiHoc("chua_hoc"); // Bài học chưa học
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Không cần xử lý
            }
        });

        // Xử lý Bottom Navigation
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        // Thiết lập item được chọn là Practice
        bottomNavigationView.setSelectedItemId(R.id.navigation_practice);
        
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.navigation_home) {
                    Intent intent = new Intent(lythuyet.this, home.class);
                    startActivity(intent);
                    finish();
                    return true;
                } else if (itemId == R.id.navigation_practice) {
                    return true;
                } else if (itemId == R.id.navigation_code) {
                    Intent intent = new Intent(lythuyet.this, thuchanhlist.class);
                    startActivity(intent);
                    finish();
                    return true;
                } else if (itemId == R.id.navigation_profile) {
                    Intent intent = new Intent(lythuyet.this, taikhoan.class);
                    startActivity(intent);
                    finish();
                    return true;
                }
                return false;
            }
        });
    }

    private void setupRecyclerViewHorizontal(RecyclerView recyclerView, BaiHocAdapterNgang adapter) {
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setAdapter(adapter);
    }

    private void setupRecyclerViewVertical(RecyclerView recyclerView, BaiHocAdapter adapter) {
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setAdapter(adapter);
    }

    private void fetchBaiHoc(String trangThai) {
        String userId = sharedPreferences.getString("id", "");
        if (userId.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
            return;
        }

        Request request = new Request.Builder()
                .url(ApiConfig.getFullUrl(ApiConfig.GET_BAI_HOC_ENDPOINT + "idNguoiDung=" + userId + "&idKhoaHoc=" + id_bai + "&trangThai=" + trangThai))
                .build();

        System.out.println("URL API: " + request.url().toString());

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(lythuyet.this, "Lỗi kết nối: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseData = response.body().string();
                        System.out.println("Response data: " + responseData);
                        JSONArray jsonArray = new JSONArray(responseData);
                        List<BaiHoc> list = new ArrayList<>();

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            BaiHoc baiHoc = new BaiHoc(
                                    jsonObject.optString("id", null),
                                    jsonObject.optString("tieuDe", null),
                                    jsonObject.optString("noiDung", null),
                                    jsonObject.optString("linkYoutube", null),
                                    jsonObject.optString("linkMoTa", null),
                                    jsonObject.optString("trangThai", null),
                                    jsonObject.optString("anhBaiHoc", null),
                                    jsonObject.optBoolean("daLamQuiz", false),
                                    jsonObject.has("diemQuiz") && !jsonObject.isNull("diemQuiz") ? jsonObject.optInt("diemQuiz", 0) : null,
                                    jsonObject.optString("mucDoQuiz", null),
                                    jsonObject.optBoolean("daLamCode", false),
                                    jsonObject.optString("diemCode", null),
                                    jsonObject.optString("mucDoCode", null)
                            );

                            list.add(baiHoc);
                        }

                        runOnUiThread(() -> {
                            switch (trangThai) {
                                case "da_hoc":
                                    listCompleted.clear();
                                    listCompleted.addAll(list);
                                    adapterCompleted.notifyDataSetChanged();
                                    break;
                                case "dang_hoc":
                                    listLearning.clear();
                                    listLearning.addAll(list);
                                    adapterLearning.notifyDataSetChanged();
                                    break;
                                case "chua_hoc":
                                    listNotStarted.clear();
                                    listNotStarted.addAll(list);
                                    adapterNotStarted.notifyDataSetChanged();
                                    break;
                            }
                        });
                    } catch (JSONException e) {
                        runOnUiThread(() -> {
                            Toast.makeText(lythuyet.this, "Lỗi xử lý dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    }
                } else {
                    runOnUiThread(() -> {
                        System.out.println("errr: " + response.toString());
                    });
                }
            }
        });
    }
}