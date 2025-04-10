package com.example.nckh;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class code extends AppCompatActivity implements SocketManager.SocketListener {
    private EditText etCode, etInput;
    private TextView tvKetQua, tvDeBai;
    private Spinner spinnerLanguage;
    private Button btnRun, btnSubmit;
    private ImageButton btnToggleDeBai;
    private SocketManager socketManager;
    private boolean isDeBaiVisible = false;
    private boolean isExecuting = false;
    private boolean daLamCode = false;
    private String codeCu = "";
    private int diemCu = 0;
    private Integer idBaiHoc = 0;
    private String mucDoHienTai = "co_ban";
    private OkHttpClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_code);

        // Ánh xạ view
        etCode = findViewById(R.id.etCode);
        etInput = findViewById(R.id.input);
        tvKetQua = findViewById(R.id.etKetQua);
        tvDeBai = findViewById(R.id.tvDeBai);
        spinnerLanguage = findViewById(R.id.spinnerLanguage);
        btnRun = findViewById(R.id.btnRun);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnToggleDeBai = findViewById(R.id.btnToggleDeBai);

        // Khởi tạo client
        client = new OkHttpClient();

        // Lấy id bài học từ intent
        Intent intent = getIntent();
        idBaiHoc = intent.getIntExtra("baiHocId", 0);

        // Kiểm tra bài code đã làm
        kiemTraDaLamCode();

        // Thiết lập Spinner ngôn ngữ
        String[] languages = {"Python", "Java", "SQL", "C#", "JavaScript"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, languages);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLanguage.setAdapter(adapter);

        // Xử lý sự kiện toggle đề bài
        btnToggleDeBai.setOnClickListener(v -> {
            isDeBaiVisible = !isDeBaiVisible;
            tvDeBai.setVisibility(isDeBaiVisible ? View.VISIBLE : View.GONE);
            btnToggleDeBai.setImageResource(isDeBaiVisible ? 
                R.drawable.ic_expand_less : R.drawable.ic_expand_more);
        });

        // Vô hiệu hóa input ban đầu
        etInput.setEnabled(false);

        // Xử lý sự kiện khi nhấn Enter trên input
        etInput.setOnEditorActionListener((v, actionId, event) -> {
            if ((actionId == EditorInfo.IME_ACTION_DONE || 
                (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) 
                && isExecuting && etInput.isEnabled()) {
                
                String input = etInput.getText().toString().trim();
                if (!input.isEmpty()) {
                    socketManager.sendInput(input);
                    tvKetQua.append(input + "\n");
                    etInput.setText("");
                    etInput.setEnabled(false);
                }
                return true;
            }
            return false;
        });

        // Khởi tạo SocketManager
        socketManager = SocketManager.getInstance();
        socketManager.setListener(this);

        // Xử lý sự kiện nút Run
        btnRun.setOnClickListener(v -> {
            String code = etCode.getText().toString();
            String language = spinnerLanguage.getSelectedItem().toString().toLowerCase();
            
            if (code.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập code", Toast.LENGTH_SHORT).show();
                return;
            }

            // Xóa kết quả cũ
            tvKetQua.setText("");
            isExecuting = true;
            etInput.setEnabled(false);
            
            // Gửi code lên server
            socketManager.executeCode(code, language, "");
        });

        // Xử lý sự kiện nút Submit
        btnSubmit.setOnClickListener(v -> {
            String code = etCode.getText().toString();
            if (code.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập code", Toast.LENGTH_SHORT).show();
                return;
            }
        });
    }






    private void capNhatTienDo() {
        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String userId = sharedPreferences.getString("id", "");

        String url = ApiConfig.getFullUrl(ApiConfig.CAP_NHAT_TIEN_DO_ENDPOINT)
                + "?idNguoiDung=" + userId
                + "&idBaiHoc=" + idBaiHoc;

        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(new byte[0], null))
                .build();

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
                        Toast.makeText(code.this, "Cập nhật tiến độ thành công!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        System.out.println("err update tiendo2: " + response.toString());
                    }
                });
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        socketManager.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        socketManager.disconnect();
    }

    @Override
    public void onOutput(String output) {
        runOnUiThread(() -> {
            // Kiểm tra nếu output chứa prompt nhập input
            if (output.contains("input(") || output.contains("Nhập")) {
                isExecuting = true;
                etInput.setEnabled(true);
                etInput.requestFocus();
            }
            
            // Thêm output vào TextView
            tvKetQua.append(output);
            
            // Cuộn xuống dòng cuối cùng
            tvKetQua.post(() -> {
                int scrollAmount = tvKetQua.getLayout().getLineTop(tvKetQua.getLineCount()) - tvKetQua.getHeight();
                if (scrollAmount > 0) {
                    tvKetQua.scrollTo(0, scrollAmount);
                }
            });
        });
    }

    @Override
    public void onError(String error) {
        runOnUiThread(() -> {
            isExecuting = false;
            etInput.setEnabled(false);
            Toast.makeText(this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            tvKetQua.append("\nLỗi: " + error);
        });
    }
}