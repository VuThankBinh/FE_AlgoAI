package com.example.nckh;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nckh.model.CauHoi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class quiz extends AppCompatActivity {
    private Spinner spinnerDeThi;
    private LinearLayout questionContainer;
    private Button btnXemKetQua;
    private List<CauHoi> danhSachCauHoi;
    private List<View> cauHoiViews;
    private String mucDoHienTai = "co_ban";
    private OkHttpClient client;
    private Integer idBaiHoc = 0;
    private Integer idKhoaHoc = 0;
    private String tenBai = "";
    private TextView tvKetQua;
    private boolean daNopBai = false;
    private int diem = 0;
    private boolean daLamQuiz = false;
    private String dapAnCu = "";
    private int diemCu = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        // Ánh xạ view
        spinnerDeThi = findViewById(R.id.spinnerDeThi);
        questionContainer = findViewById(R.id.questionContainer);
        btnXemKetQua = findViewById(R.id.btnXemKetQua);
        tvKetQua = findViewById(R.id.tvKetQua);
        client = new OkHttpClient();

        // Kiểm tra xem đã làm quiz này chưa
        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String userId = sharedPreferences.getString("id", "");
        kiemTraDaLamQuiz(userId);

        // Khởi tạo danh sách câu hỏi
        danhSachCauHoi = new ArrayList<>();
        cauHoiViews = new ArrayList<>();

        // Thiết lập Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.muc_do, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDeThi.setAdapter(adapter);
        Intent intent = getIntent();
        idBaiHoc = intent.getIntExtra("baiHocId", 0);
        idKhoaHoc = intent.getIntExtra("idKhoaHoc", 0);
        tenBai = intent.getStringExtra("nameBai");
        System.out.println(tenBai);
        System.out.println(idBaiHoc);
        System.out.println(idKhoaHoc);
        TextView tenBaiHoc = findViewById(R.id.tenBaiHoc);
        tenBaiHoc.setText(tenBai);

        loadCauHoi();
        // Xử lý sự kiện chọn mức độ
        spinnerDeThi.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        mucDoHienTai = "co_ban";
                        break;
                    case 1:
                        mucDoHienTai = "trung_binh";
                        break;
                    case 2:
                        mucDoHienTai = "nang_cao";
                        break;
                }
                // Kiểm tra kết quả cũ trước khi load câu hỏi mới
                kiemTraKetQuaCu();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });

        // Xử lý sự kiện nút xem kết quả/làm lại
        btnXemKetQua.setOnClickListener(v -> {
            if (!daNopBai) {
                tinhDiem();
            } else {
                // Làm lại bài
                resetQuiz();
            }
        });
    }

    private void kiemTraDaLamQuiz(String userId) {
        String url = ApiConfig.getFullUrl(ApiConfig.Check_TienDo_quiz_ENDPOINT) + "?idNguoiDung=" + userId
                + "&idBaiHoc=" + idBaiHoc;

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    System.out.println("err kiem tra da lam quiz: " + e.toString());
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseData = response.body().string();
                        JSONObject jsonObject = new JSONObject(responseData);
                        daLamQuiz = jsonObject.getBoolean("data");
                        System.out.println("da lam quiz: " + daLamQuiz);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void kiemTraKetQuaCu() {
        // Ẩn điểm cũ trước khi kiểm tra
        tvKetQua.setVisibility(View.GONE);

        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String userId = sharedPreferences.getString("id", "");

        String url = ApiConfig.getFullUrl(ApiConfig.get_quiz_completed_ENDPOINT)
                + "?idNguoiDung=" + userId
                + "&idBaiHoc=" + idBaiHoc
                + "&mucDo=" + mucDoHienTai;

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    System.out.println("err kiem tra ket qua cu: " + e.toString());
                    // Nếu không lấy được kết quả cũ thì load câu hỏi mới
                    loadCauHoi();
                    // Reset trạng thái nút
                    btnXemKetQua.setText("Xem kết quả");
                    btnXemKetQua.setVisibility(View.VISIBLE);
                    daNopBai = false;
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseData = response.body().string();
                        JSONArray jsonArray = new JSONArray(responseData);

                        if (jsonArray.length() > 0) {
                            JSONObject jsonObject = jsonArray.getJSONObject(0);
                            diemCu = jsonObject.getInt("diem");
                            dapAnCu = jsonObject.getString("dapAnNguoiDung");

                            runOnUiThread(() -> {
                                hienThiKetQuaCu();
                            });
                        } else {
                            // Nếu không có kết quả cũ thì load câu hỏi mới
                            runOnUiThread(() -> {
                                loadCauHoi();
                                // Reset trạng thái nút
                                btnXemKetQua.setText("Xem kết quả");
                                btnXemKetQua.setVisibility(View.VISIBLE);
                                daNopBai = false;
                            });
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        runOnUiThread(() -> {
                            loadCauHoi();
                            // Reset trạng thái nút
                            btnXemKetQua.setText("Xem kết quả");
                            btnXemKetQua.setVisibility(View.VISIBLE);
                            daNopBai = false;
                        });
                    }
                } else {
                    runOnUiThread(() -> {
                        loadCauHoi();
                        // Reset trạng thái nút
                        btnXemKetQua.setText("Xem kết quả");
                        btnXemKetQua.setVisibility(View.VISIBLE);
                        daNopBai = false;
                    });
                }
            }
        });
    }

    private void hienThiKetQuaCu() {
        // Hiển thị điểm cũ
        String ketQua = "Điểm cũ: " + diemCu + "/10";
        tvKetQua.setText(ketQua);
        tvKetQua.setVisibility(View.VISIBLE);

        // Vô hiệu hóa các RadioGroup
        for (View view : cauHoiViews) {
            RadioGroup radioGroup = view.findViewById(R.id.radioGroup);
            radioGroup.setEnabled(false);
            // Vô hiệu hóa từng RadioButton
            for (int i = 0; i < radioGroup.getChildCount(); i++) {
                radioGroup.getChildAt(i).setEnabled(false);
            }
        }

        // Hiển thị đáp án cũ
        String[] dapAns = dapAnCu.split("-");
        for (int i = 0; i < cauHoiViews.size() && i < dapAns.length; i++) {
            View view = cauHoiViews.get(i);
            RadioGroup radioGroup = view.findViewById(R.id.radioGroup);
            int selectedIndex = dapAns[i].charAt(0) - 'A';
            if (selectedIndex >= 0 && selectedIndex < radioGroup.getChildCount()) {
                RadioButton radioButton = (RadioButton) radioGroup.getChildAt(selectedIndex);
                radioButton.setChecked(true);
            }
        }

        // Thay đổi nút
        btnXemKetQua.setText("Làm lại");
        btnXemKetQua.setVisibility(View.VISIBLE);
        daNopBai = true;
    }

    private void loadCauHoi() {
        Request request = new Request.Builder()
                .url(ApiConfig.getFullUrl(
                        ApiConfig.GET_CAU_HOI_ENDPOINT + "?mucDo=" + mucDoHienTai + "&idBaiHoc=" + idBaiHoc))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(
                        () -> Toast.makeText(quiz.this, "Lỗi kết nối: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseData = response.body().string();
                        JSONArray jsonArray = new JSONArray(responseData);
                        danhSachCauHoi.clear();

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            CauHoi cauHoi = new CauHoi(
                                    jsonObject.getInt("id"),
                                    jsonObject.getInt("idBaiHoc"),
                                    jsonObject.getString("cauHoi"),
                                    jsonObject.getString("luaChonA"),
                                    jsonObject.getString("luaChonB"),
                                    jsonObject.getString("luaChonC"),
                                    jsonObject.getString("luaChonD"),
                                    jsonObject.getString("dapAnDung"),
                                    jsonObject.getString("mucDo"));
                            danhSachCauHoi.add(cauHoi);
                        }

                        runOnUiThread(() -> hienThiCauHoi());
                    } catch (JSONException e) {
                        runOnUiThread(() -> Toast.makeText(quiz.this, "Lỗi xử lý dữ liệu", Toast.LENGTH_SHORT).show());
                    }
                }
            }
        });
    }

    private void hienThiCauHoi() {
        questionContainer.removeAllViews();
        cauHoiViews.clear();

        // Hiển thị 10 câu hỏi ngẫu nhiên
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            CauHoi cauHoi = danhSachCauHoi.get(i);

            View view = getLayoutInflater().inflate(R.layout.item_cauhoi, questionContainer, false);
            TextView tvCauHoi = view.findViewById(R.id.tvCauHoi);
            RadioGroup radioGroup = view.findViewById(R.id.radioGroup);

            tvCauHoi.setText("Câu " + (i + 1) + ": " + cauHoi.getCauHoi());

            RadioButton radioButton1 = view.findViewById(R.id.radioButton1);
            RadioButton radioButton2 = view.findViewById(R.id.radioButton2);
            RadioButton radioButton3 = view.findViewById(R.id.radioButton3);
            RadioButton radioButton4 = view.findViewById(R.id.radioButton4);

            radioButton1.setText("A. " + cauHoi.getLuaChonA());
            radioButton2.setText("B. " + cauHoi.getLuaChonB());
            radioButton3.setText("C. " + cauHoi.getLuaChonC());
            radioButton4.setText("D. " + cauHoi.getLuaChonD());

            questionContainer.addView(view);
            cauHoiViews.add(view);
        }
    }

    private void tinhDiem() {
        diem = 0;
        int tongCauHoi = cauHoiViews.size();
        StringBuilder dapAn = new StringBuilder();
        boolean daChonHet = true;

        for (int i = 0; i < tongCauHoi; i++) {
            View view = cauHoiViews.get(i);
            RadioGroup radioGroup = view.findViewById(R.id.radioGroup);
            int selectedId = radioGroup.getCheckedRadioButtonId();

            if (selectedId != -1) {
                RadioButton radioButton = view.findViewById(selectedId);
                int selectedIndex = radioGroup.indexOfChild(radioButton);
                String selectedAnswer = String.valueOf((char) ('A' + selectedIndex));

                if (i > 0) {
                    dapAn.append("-");
                }
                dapAn.append(selectedAnswer);

                if (selectedAnswer.equals(danhSachCauHoi.get(i).getDapAnDung())) {
                    diem++;
                }
            } else {
                if (i > 0) {
                    dapAn.append("-");
                }
                dapAn.append("-");
                daChonHet = false;
            }
        }

        if (!daChonHet) {
            Toast.makeText(this, "Vui lòng chọn đáp án cho tất cả các câu hỏi!", Toast.LENGTH_LONG).show();
            return;
        }

        // Vô hiệu hóa tất cả RadioGroup và RadioButton ngay sau khi tính điểm
        for (View view : cauHoiViews) {
            RadioGroup radioGroup = view.findViewById(R.id.radioGroup);
            radioGroup.setEnabled(false);
            // Vô hiệu hóa từng RadioButton
            for (int i = 0; i < radioGroup.getChildCount(); i++) {
                radioGroup.getChildAt(i).setEnabled(false);
            }
        }

        // Hiển thị kết quả
        String ketQua = "Bạn đạt " + diem + "/" + tongCauHoi + " điểm";
        tvKetQua.setText(ketQua);
        tvKetQua.setVisibility(View.VISIBLE);

        // Thay đổi nút
        if (diem < 10) {
            btnXemKetQua.setText("Làm lại");
        } else {
            btnXemKetQua.setVisibility(View.GONE);
        }

        daNopBai = true;

        // Gọi API nộp bài
        nopBai(diem, dapAn.toString());
    }

    private void resetQuiz() {
        // Reset trạng thái
        daNopBai = false;
        diem = 0;
        tvKetQua.setVisibility(View.GONE); // Ẩn điểm cũ
        btnXemKetQua.setText("Xem kết quả");
        btnXemKetQua.setVisibility(View.VISIBLE);

        // Bật lại các RadioGroup
        for (View view : cauHoiViews) {
            RadioGroup radioGroup = view.findViewById(R.id.radioGroup);
            radioGroup.clearCheck();
            radioGroup.setEnabled(true);
            // Bật lại từng RadioButton
            for (int i = 0; i < radioGroup.getChildCount(); i++) {
                radioGroup.getChildAt(i).setEnabled(true);
            }
        }

        // Load lại câu hỏi
        loadCauHoi();
    }

    private void nopBai(int diem, String dapAn) {
        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String userId = sharedPreferences.getString("id", "");

        // Kiểm tra đã làm bài chưa
        String url = ApiConfig.getFullUrl("/api/nop-bai/kiem-tra/quiz/theo-muc-do")
                + "?idNguoiDung=" + userId
                + "&idBaiHoc=" + idBaiHoc
                + "&mucDo=" + mucDoHienTai;

        Request checkRequest = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(checkRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    System.out.println("err kiem tra da lam: " + e.toString());
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseData = response.body().string();
                        boolean daLam = Boolean.parseBoolean(responseData);

                        // Nộp bài
                        try {
                            JSONObject nopBaiJson = new JSONObject();
                            nopBaiJson.put("idNguoiDung", Integer.parseInt(userId));
                            nopBaiJson.put("idBaiHoc", idBaiHoc);
                            nopBaiJson.put("dapAn", dapAn);
                            nopBaiJson.put("mucDo", mucDoHienTai);
                            nopBaiJson.put("diem", diem);
                            nopBaiJson.put("ngayNop", new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new java.util.Date()));

                            RequestBody body = RequestBody.create(nopBaiJson.toString(), MediaType.parse("application/json"));

                            Request.Builder requestBuilder = new Request.Builder();
                            if (daLam) {
                                requestBuilder = new Request.Builder()
                                        .url(ApiConfig.getFullUrl(ApiConfig.NOP_BAI_QUIZ_ENDPOINT) + "?idNguoiDung=" + userId + "&idBaiHoc=" + idBaiHoc);
                                requestBuilder.put(body);
                                requestBuilder.header("Content-Type", "application/json");
                                System.out.println("da lam roi");
                                System.out.println(ApiConfig.getFullUrl(ApiConfig.NOP_BAI_QUIZ_ENDPOINT) + "?idNguoiDung=" + userId + "&idBaiHoc=" + idBaiHoc);
                                System.out.println(nopBaiJson);
                            } else {
                                requestBuilder = new Request.Builder()
                                        .url(ApiConfig.getFullUrl(ApiConfig.NOP_BAI_QUIZ_ENDPOINT));
                                requestBuilder.post(body);
                                requestBuilder.header("Content-Type", "application/json");
                            }

                            Request request = requestBuilder.build();

                            client.newCall(request).enqueue(new Callback() {
                                @Override
                                public void onFailure(Call call, IOException e) {
                                    runOnUiThread(() -> {
                                        System.out.println("err nop: " + e.toString());
                                    });
                                }

                                @Override
                                public void onResponse(Call call, Response response) throws IOException {
                                    runOnUiThread(() -> {
                                        if (response.isSuccessful()) {
                                            Toast.makeText(quiz.this, "Nộp bài thành công!", Toast.LENGTH_SHORT).show();
                                            // Chỉ cập nhật tiến độ nếu là lần đầu làm quiz
                                            if (!daLam) {
                                                capNhatTienDo();
                                            } else {
                                                finish();
                                            }
                                        } else {
                                            System.out.println("err nop: " + response.toString());
                                        }
                                    });
                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                            runOnUiThread(() -> {
                                Toast.makeText(quiz.this, "Lỗi tạo dữ liệu nộp bài", Toast.LENGTH_SHORT).show();
                            });
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        runOnUiThread(() -> {
                            Toast.makeText(quiz.this, "Lỗi đọc kết quả", Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            }
        });
    }

    private void capNhatTienDo() {
        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String userId = sharedPreferences.getString("id", "");
        RequestBody body = RequestBody.create(new byte[0], null);
        ;

// Hoặc nếu server yêu cầu MediaType cụ thể (ví dụ: JSON)
// MediaType JSON = MediaType.parse("application/json; charset=utf-8");
// RequestBody body = RequestBody.create("{}", JSON);

// Tạo URL có query params
        String url = ApiConfig.getFullUrl(ApiConfig.CAP_NHAT_TIEN_DO_ENDPOINT)
                + "?idNguoiDung=" + userId
                + "&idBaiHoc=" + idBaiHoc;

// Tạo request
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

// Gửi request
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    System.out.println("err update tiendo: " + e.toString());
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        Toast.makeText(quiz.this, "Cập nhật tiến độ thành công!", Toast.LENGTH_SHORT).show();
                    } else {
                        System.out.println("err update tiendo2: " + response.toString());
                    }
                });
            }
        });

    }
}
