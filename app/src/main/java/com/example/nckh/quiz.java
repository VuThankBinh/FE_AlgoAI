package com.example.nckh;

import android.os.Bundle;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class quiz extends AppCompatActivity {
    private Spinner spinnerDeThi;
    private LinearLayout questionContainer;
    private Button btnXemKetQua;
    private List<CauHoi> danhSachCauHoi;
    private List<View> cauHoiViews;
    private String mucDoHienTai = "co_ban";
    private OkHttpClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        // Ánh xạ view
        spinnerDeThi = findViewById(R.id.spinnerDeThi);
        questionContainer = findViewById(R.id.questionContainer);
        btnXemKetQua = findViewById(R.id.btnXemKetQua);
        client = new OkHttpClient();

        // Khởi tạo danh sách câu hỏi
        danhSachCauHoi = new ArrayList<>();
        cauHoiViews = new ArrayList<>();

        // Thiết lập Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.muc_do, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDeThi.setAdapter(adapter);

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
                loadCauHoi();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });

        // Xử lý sự kiện nút xem kết quả
        btnXemKetQua.setOnClickListener(v -> tinhDiem());
    }

    private void loadCauHoi() {
        Request request = new Request.Builder()
                .url(ApiConfig.getFullUrl(ApiConfig.GET_CAU_HOI_ENDPOINT + "?mucDo=" + mucDoHienTai +"&idBaiHoc=2"))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(quiz.this, "Lỗi kết nối: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
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
                                    jsonObject.getString("mucDo")
                            );
                            danhSachCauHoi.add(cauHoi);
                        }

                        runOnUiThread(() -> hienThiCauHoi());
                    } catch (JSONException e) {
                        runOnUiThread(() ->
                                Toast.makeText(quiz.this, "Lỗi xử lý dữ liệu", Toast.LENGTH_SHORT).show()
                        );
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
        int diem = 0;
        int tongCauHoi = cauHoiViews.size();

        for (int i = 0; i < tongCauHoi; i++) {
            View view = cauHoiViews.get(i);
            RadioGroup radioGroup = view.findViewById(R.id.radioGroup);
            int selectedId = radioGroup.getCheckedRadioButtonId();

            if (selectedId != -1) {
                RadioButton radioButton = view.findViewById(selectedId);
                int selectedIndex = radioGroup.indexOfChild(radioButton);
                String selectedAnswer = String.valueOf((char) ('A' + selectedIndex));
                
                // Kiểm tra đáp án
                if (selectedAnswer.equals(danhSachCauHoi.get(i).getDapAnDung())) {
                    diem++;
                }
            }
        }

        // Hiển thị kết quả
        String ketQua = "Bạn đạt " + diem + "/" + tongCauHoi + " điểm";
        Toast.makeText(this, ketQua, Toast.LENGTH_LONG).show();
    }
}