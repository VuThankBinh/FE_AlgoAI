package com.example.nckh;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class code extends AppCompatActivity implements SocketManager.SocketListener {
    private EditText etCode, etInput;
    private TextView tvKetQua, tvDeBai;
    private Spinner spinnerLanguage;
    private Button btnRun, btnSubmit;
    private ImageButton btnToggleDeBai, btnToggleInput;
    private SocketManager socketManager;
    private boolean isDeBaiVisible = false;
    private boolean isInputVisible = false;
    private boolean isWaitingForInput = false;

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
        btnToggleInput = findViewById(R.id.btnToggleInput);

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

        // Xử lý sự kiện toggle input
        btnToggleInput.setOnClickListener(v -> {
            isInputVisible = !isInputVisible;
            etInput.setVisibility(isInputVisible ? View.VISIBLE : View.GONE);
            btnToggleInput.setImageResource(isInputVisible ? 
                R.drawable.ic_expand_less : R.drawable.ic_expand_more);
        });

        // Xử lý sự kiện khi nhập input
        etInput.setOnEditorActionListener((v, actionId, event) -> {
            if (isWaitingForInput) {
                String input = etInput.getText().toString();
                if (!input.isEmpty()) {
                    socketManager.sendInput(input);
                    etInput.setText(""); // Xóa input sau khi gửi
                    isWaitingForInput = false;
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
            String language = spinnerLanguage.getSelectedItem().toString();
            String input = etInput.getText().toString();

            if (code.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập code", Toast.LENGTH_SHORT).show();
                return;
            }

            // Xóa kết quả cũ
            tvKetQua.setText("");
            
            // Hiển thị input nếu chưa hiển thị
            if (!isInputVisible) {
                isInputVisible = true;
                etInput.setVisibility(View.VISIBLE);
                btnToggleInput.setImageResource(R.drawable.ic_expand_less);
            }

            // Gửi code và input lên server
            socketManager.executeCode(code, language, input);
        });

        // Xử lý sự kiện nút Submit
        btnSubmit.setOnClickListener(v -> {
            // Xử lý nộp bài
            Toast.makeText(this, "Đã nộp bài", Toast.LENGTH_SHORT).show();
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
                isWaitingForInput = true;
                // Hiển thị input nếu chưa hiển thị
                if (!isInputVisible) {
                    isInputVisible = true;
                    etInput.setVisibility(View.VISIBLE);
                    btnToggleInput.setImageResource(R.drawable.ic_expand_less);
                }
                // Focus vào input
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
            Toast.makeText(this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            tvKetQua.append("\nLỗi: " + error);
        });
    }
}