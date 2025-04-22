package com.example.nckh;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nckh.adapter.ChatAdapter;
import com.example.nckh.adapter.ChatAdapter2;
import com.example.nckh.model.ChatMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
    private Spinner spinnerLanguage, spinnerMucDo;
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
    private Integer idKhoaHoc = 0;
    private String tenBai = "";
    private OkHttpClient client;
    private ImageView aiBot;
    private Dialog chatDialog;
    private boolean isChatExpanded = false;
    private RecyclerView rvChatMessages;
    private EditText etMessage;
    private Button btnSend;
    private LinearLayout layoutSuggestedQuestions;
    private ChatAdapter2 chatAdapter;
    private boolean isFirstChat = true;
    private String tieuDe = "";
    private List<ChatMessage> messages;
    private Button btnXemPhanHoi;
    private int NopBaiId = 0;
    private LinearLayout questionContainer;

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
        spinnerMucDo = findViewById(R.id.spinnerMucDo);
        btnRun = findViewById(R.id.btnRun);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnToggleDeBai = findViewById(R.id.btnToggleDeBai);
        aiBot = findViewById(R.id.aiBot);
        btnXemPhanHoi = findViewById(R.id.btnXemPhanHoi);
        // Khởi tạo client
        client = new OkHttpClient();

        // Khởi tạo danh sách tin nhắn
        messages = new ArrayList<>();
        chatAdapter = new ChatAdapter2(messages);

        // Lấy id bài học từ intent
        Intent intent = getIntent();
        idBaiHoc = intent.getIntExtra("baiHocId", 0);
        idKhoaHoc = intent.getIntExtra("idKhoaHoc", 0);
        tenBai = intent.getStringExtra("nameBai");
        tieuDe = tenBai;

        // Thiết lập sự kiện click cho nút AI
        aiBot.setOnClickListener(v -> toggleChatWindow());

        // Thiết lập sự kiện click cho nút xem phản hồi
        btnXemPhanHoi.setOnClickListener(v -> layPhanHoiAI(NopBaiId));

        // Thiết lập Spinner ngôn ngữ
        String[] languages = {"Python", "Java", "SQL", "C#", "JavaScript"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, languages);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLanguage.setAdapter(adapter);

        // Thiết lập Spinner mức độ
        ArrayAdapter<CharSequence> adapterMucDo = ArrayAdapter.createFromResource(this,
                R.array.muc_do, android.R.layout.simple_spinner_item);
        adapterMucDo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMucDo.setAdapter(adapterMucDo);
        spinnerMucDo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
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
                // Kiểm tra bài đã làm chưa
                kiemTraKetQuaCu();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

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

            // Nếu là SQL thì gửi request trực tiếp đến API
            if (language.equals("sql")) {
                executeSQLCode(code);
                return;
            }

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

            if (btnSubmit.getText().toString().equals("Làm lại")) {
                // Reset bài tập
                etCode.setText("");
                etCode.setEnabled(true);
                tvKetQua.setText("");
                tvKetQua.setVisibility(View.GONE);
                btnSubmit.setText("Nộp bài");
                layBaiTapCode();
            } else {
                // Nộp bài
                nopbai();
            }
        });
    }

    private void toggleChatWindow() {
        if (chatDialog == null) {
            createChatDialog();
        }

        if (isChatExpanded) {
            chatDialog.dismiss();
        } else {
            chatDialog.show();
        }
        isChatExpanded = !isChatExpanded;
    }

    private void createChatDialog() {
        chatDialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        chatDialog.setContentView(R.layout.dialog_chat2);

        // Cấu hình dialog full màn
        Window window = chatDialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.setGravity(Gravity.CENTER);
        }

        // Khởi tạo các view trong dialog
        rvChatMessages = chatDialog.findViewById(R.id.rvChatMessages);
        questionContainer = chatDialog.findViewById(R.id.questionContainer);
        ImageView btnClose = chatDialog.findViewById(R.id.btnCloseChat);

        // Cấu hình RecyclerView
        rvChatMessages.setLayoutManager(new LinearLayoutManager(this));
        rvChatMessages.setAdapter(chatAdapter);

        // Xử lý sự kiện đóng dialog
        btnClose.setOnClickListener(v -> toggleChatWindow());
        Button btnQuestion1 = chatDialog.findViewById(R.id.btnQuestion1);
        Button btnQuestion2 = chatDialog.findViewById(R.id.btnQuestion2);
        Button btnQuestion3 = chatDialog.findViewById(R.id.btnQuestion3);
        Button btnQuestion4 = chatDialog.findViewById(R.id.btnQuestion4);
        btnQuestion4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messages.add(new ChatMessage(btnQuestion4.getText().toString(), true));
                chatAdapter.notifyItemInserted(messages.size() - 1);
                rvChatMessages.scrollToPosition(messages.size() - 1);
                requestFixBug();
            }
        });
        btnQuestion3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                messages.add(new ChatMessage(btnQuestion3.getText().toString(), true));
                chatAdapter.notifyItemInserted(messages.size() - 1);
                rvChatMessages.scrollToPosition(messages.size() - 1);
                requestTestCases();
            }
        });
        btnQuestion2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messages.add(new ChatMessage(btnQuestion2.getText().toString(), true));
                chatAdapter.notifyItemInserted(messages.size() - 1);
                rvChatMessages.scrollToPosition(messages.size() - 1);
                requestFakeCode();
            }
        });
        btnQuestion1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messages.add(new ChatMessage(btnQuestion1.getText().toString(), true));
                chatAdapter.notifyItemInserted(messages.size() - 1);
                rvChatMessages.scrollToPosition(messages.size() - 1);
                requestGrade();
            }
        });

    }
    private void requestFixBug() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("code", etCode.getText().toString());
            jsonObject.put("errorMessage", tvKetQua.getText().toString());
            jsonObject.put("sessionId", "1s");

            RequestBody body = RequestBody.create(jsonObject.toString(), MediaType.parse("application/json"));
            Request request = new Request.Builder()
                    .url(ApiConfig.getFullUrl("/api/code-chat/debug"))
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> {
                        messages.add(new ChatMessage("Xin lỗi, không thể sửa lỗi code lúc này.", false));
                        chatAdapter.notifyItemInserted(messages.size() - 1);
                        rvChatMessages.scrollToPosition(messages.size() - 1);
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String responseData = response.body().string();
                        runOnUiThread(() -> {
                            messages.add(new ChatMessage(responseData, false));
                            chatAdapter.notifyItemInserted(messages.size() - 1);
                            rvChatMessages.scrollToPosition(messages.size() - 1);
                        });
                    } else {
                        runOnUiThread(() -> {
                            messages.add(new ChatMessage("Xin lỗi, có lỗi xảy ra khi xử lý sửa lỗi code.", false));
                            chatAdapter.notifyItemInserted(messages.size() - 1);
                            rvChatMessages.scrollToPosition(messages.size() - 1);
                        });
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
            runOnUiThread(() -> {
                messages.add(new ChatMessage("Xin lỗi, có lỗi xảy ra khi tạo yêu cầu.", false));
                chatAdapter.notifyItemInserted(messages.size() - 1);
                rvChatMessages.scrollToPosition(messages.size() - 1);
            });
        }
    }

    private void requestGrade() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("question", "các bước làm thuật toán " + tvDeBai.getText().toString());
            jsonObject.put("sessionId", "1s");
            jsonObject.put("exerciseId", idBaiHoc);

            RequestBody body = RequestBody.create(jsonObject.toString(), MediaType.parse("application/json"));
            Request request = new Request.Builder()
                    .url(ApiConfig.getFullUrl("/api/code-chat/algorithm"))
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> {
                        messages.add(new ChatMessage("Xin lỗi, không thể lấy các bước thuật toán lúc này.", false));
                        chatAdapter.notifyItemInserted(messages.size() - 1);
                        rvChatMessages.scrollToPosition(messages.size() - 1);
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String responseData = response.body().string();
                        runOnUiThread(() -> {
                            messages.add(new ChatMessage(responseData, false));
                            chatAdapter.notifyItemInserted(messages.size() - 1);
                            rvChatMessages.scrollToPosition(messages.size() - 1);
                        });
                    } else {
                        runOnUiThread(() -> {
                            messages.add(new ChatMessage("Xin lỗi, có lỗi xảy ra khi xử lý các bước thuật toán.", false));
                            chatAdapter.notifyItemInserted(messages.size() - 1);
                            rvChatMessages.scrollToPosition(messages.size() - 1);
                        });
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
            runOnUiThread(() -> {
                messages.add(new ChatMessage("Xin lỗi, có lỗi xảy ra khi tạo yêu cầu.", false));
                chatAdapter.notifyItemInserted(messages.size() - 1);
                rvChatMessages.scrollToPosition(messages.size() - 1);
            });
        }
    }

    private void requestFakeCode() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("question", "mã giả thuật toán " + tvDeBai.getText().toString());
            jsonObject.put("sessionId", "1s");
            jsonObject.put("exerciseId", idBaiHoc);

            RequestBody body = RequestBody.create(jsonObject.toString(), MediaType.parse("application/json"));
            Request request = new Request.Builder()
                    .url(ApiConfig.getFullUrl("/api/code-chat/guide"))
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> {
                        messages.add(new ChatMessage("Xin lỗi, không thể lấy mã giả thuật toán lúc này.", false));
                        chatAdapter.notifyItemInserted(messages.size() - 1);
                        rvChatMessages.scrollToPosition(messages.size() - 1);
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String responseData = response.body().string();
                        runOnUiThread(() -> {
                            messages.add(new ChatMessage(responseData, false));
                            chatAdapter.notifyItemInserted(messages.size() - 1);
                            rvChatMessages.scrollToPosition(messages.size() - 1);
                        });
                    } else {
                        runOnUiThread(() -> {
                            messages.add(new ChatMessage("Xin lỗi, có lỗi xảy ra khi xử lý mã giả thuật toán.", false));
                            chatAdapter.notifyItemInserted(messages.size() - 1);
                            rvChatMessages.scrollToPosition(messages.size() - 1);
                        });
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
            runOnUiThread(() -> {
                messages.add(new ChatMessage("Xin lỗi, có lỗi xảy ra khi tạo yêu cầu.", false));
                chatAdapter.notifyItemInserted(messages.size() - 1);
                rvChatMessages.scrollToPosition(messages.size() - 1);
            });
        }
    }

    private void requestTestCases() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("exerciseDescription", tvDeBai.getText().toString());
            jsonObject.put("programmingLanguage", spinnerLanguage.getSelectedItem().toString().toLowerCase());
            if (mucDoHienTai.equals("co_ban")) {
                jsonObject.put("difficultyLevel", "Easy");
            } else if (mucDoHienTai.equals("trung_binh")) {
                jsonObject.put("difficultyLevel", "Medium");
            } else {
                jsonObject.put("difficultyLevel", "Hard");
            }
            jsonObject.put("constraints", ""); // Có thể thêm constraints nếu cần

            RequestBody body = RequestBody.create(jsonObject.toString(), MediaType.parse("application/json"));
            Request request = new Request.Builder()
                    .url(ApiConfig.getFullUrl("/api/test-case-suggestion/suggest"))
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> {
                        messages.add(new ChatMessage("Xin lỗi, không thể tạo test case lúc này.", false));
                        chatAdapter.notifyItemInserted(messages.size() - 1);
                        rvChatMessages.scrollToPosition(messages.size() - 1);
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        try {
                            String responseData = response.body().string();
                            JSONObject jsonResponse = new JSONObject(responseData);
                            String testCases = jsonResponse.getString("testCases");

                            runOnUiThread(() -> {
                                messages.add(new ChatMessage(testCases, false));
                                chatAdapter.notifyItemInserted(messages.size() - 1);
                                rvChatMessages.scrollToPosition(messages.size() - 1);
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                            runOnUiThread(() -> {
                                messages.add(new ChatMessage("Xin lỗi, có lỗi xảy ra khi xử lý test case.", false));
                                chatAdapter.notifyItemInserted(messages.size() - 1);
                                rvChatMessages.scrollToPosition(messages.size() - 1);
                            });
                        }
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void sendMessageToAPI(String message) {
        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String userId = sharedPreferences.getString("id", "");

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("message", message);
            jsonObject.put("userId", userId);
            jsonObject.put("baiHocId", idBaiHoc);
            jsonObject.put("mucDo", mucDoHienTai);

            RequestBody body = RequestBody.create(jsonObject.toString(), MediaType.parse("application/json"));

            Request request = new Request.Builder()
                    .url(ApiConfig.getFullUrl(ApiConfig.POST_CHAT_ENDPOINT))
                    .post(body)
                    .header("Content-Type", "application/json")
                    .build();

            System.out.println("url chat: " + ApiConfig.getFullUrl(ApiConfig.POST_CHAT_ENDPOINT));
            System.out.println("body chat: " + jsonObject.toString());

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> {
                        messages.add(new ChatMessage("Xin lỗi, đã có lỗi xảy ra. Vui lòng thử lại sau.", false));
                        chatAdapter.notifyItemInserted(messages.size() - 1);
                        rvChatMessages.scrollToPosition(messages.size() - 1);
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        try {
                            String responseData = response.body().string();
                            JSONObject jsonResponse = new JSONObject(responseData);
                            String reply = jsonResponse.getString("content");

                            runOnUiThread(() -> {
                                messages.add(new ChatMessage(reply, false));
                                chatAdapter.notifyItemInserted(messages.size() - 1);
                                rvChatMessages.scrollToPosition(messages.size() - 1);
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                            runOnUiThread(() -> {
                                messages.add(new ChatMessage("Xin lỗi, đã có lỗi xảy ra. Vui lòng thử lại sau.", false));
                                chatAdapter.notifyItemInserted(messages.size() - 1);
                                rvChatMessages.scrollToPosition(messages.size() - 1);
                            });
                        }
                    } else {
                        runOnUiThread(() -> {
                            messages.add(new ChatMessage("Xin lỗi, đã có lỗi xảy ra. Vui lòng thử lại sau.", false));
                            chatAdapter.notifyItemInserted(messages.size() - 1);
                            rvChatMessages.scrollToPosition(messages.size() - 1);
                        });
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
            messages.add(new ChatMessage("Xin lỗi, đã có lỗi xảy ra. Vui lòng thử lại sau.", false));
            chatAdapter.notifyItemInserted(messages.size() - 1);
            rvChatMessages.scrollToPosition(messages.size() - 1);
        }
    }

    private void nopbai() {
        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String userId = sharedPreferences.getString("id", "");

        try {
            // Tạo JSON cho chấm điểm
            JSONObject gradingJson = new JSONObject();
            gradingJson.put("code", etCode.getText().toString());
            gradingJson.put("exerciseDescription", tvDeBai.getText().toString());
            gradingJson.put("programmingLanguage", spinnerLanguage.getSelectedItem().toString().toLowerCase());
            if (mucDoHienTai.equals("co_ban")) {
                gradingJson.put("difficultyLevel", "Easy");
            } else if (mucDoHienTai.equals("trung_binh")) {
                gradingJson.put("difficultyLevel", "Medium");
            } else if (mucDoHienTai.equals("nang_cao")) {
                gradingJson.put("difficultyLevel", "Hard");
            }
            gradingJson.put("expectedOutput", ""); // Cần lấy từ API
            gradingJson.put("constraints", ""); // Cần lấy từ API

            RequestBody gradingBody = RequestBody.create(gradingJson.toString(), MediaType.parse("application/json"));

            Request gradingRequest = new Request.Builder()
                    .url(ApiConfig.getFullUrl(ApiConfig.Post_cham_diem_code_ENDPOINT))
                    .post(gradingBody)
                    .build();

            client.newCall(gradingRequest).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> {
                        System.out.println("err cham diem: " + e.toString());
                        Toast.makeText(code.this, "Lỗi chấm điểm", Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        try {
                            String responseData = response.body().string();
                            JSONObject jsonObject = new JSONObject(responseData);
                            Double diem = jsonObject.getDouble("totalScore") / 10;
                            String noiDung = jsonObject.getString("feedback");
                            String goiYCaiThien = jsonObject.getString("suggestions");
                            System.out.println("response: " + responseData);
                            // Lưu phản hồi AI
                            nopBaiCode(diem, noiDung, goiYCaiThien);


                            // Nộp bài
                        } catch (JSONException e) {
                            e.printStackTrace();
                            runOnUiThread(() -> {
                                Toast.makeText(code.this, "Lỗi xử lý kết quả chấm điểm", Toast.LENGTH_SHORT).show();
                            });
                        }
                    } else {
                        runOnUiThread(() -> {
                            System.out.println("err cham diem: " + response.toString());
                            Toast.makeText(code.this, "Lỗi chấm điểm", Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi tạo dữ liệu chấm điểm", Toast.LENGTH_SHORT).show();
        }
    }

    private void nopBaiCode(Double diem, String nodungphanhoi, String goiYCaiThien) {
        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String userId = sharedPreferences.getString("id", "");

        // Kiểm tra đã làm bài chưa
        String url = ApiConfig.getFullUrl("/api/nop-bai/kiem-tra/code/theo-muc-do")
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
                            nopBaiJson.put("dapAn", etCode.getText().toString());
                            nopBaiJson.put("mucDo", mucDoHienTai);
                            nopBaiJson.put("diem", diem);
                            nopBaiJson.put("ngayNop", new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new java.util.Date()));

                            System.out.println("post nop bai: " + nopBaiJson.toString());
                            RequestBody body = RequestBody.create(nopBaiJson.toString(), MediaType.parse("application/json"));
                            
                            Request.Builder requestBuilder;
                            if (daLam) {
                                System.out.println("da lam code");
                                // Nếu đã làm thì dùng PUT
                                String putUrl = ApiConfig.getFullUrl(ApiConfig.NOP_BAI_CODE_ENDPOINT)
                                    + "?idNguoiDung=" + userId
                                    + "&idBaiHoc=" + idBaiHoc;
                                
                                requestBuilder = new Request.Builder()
                                    .url(putUrl)
                                    .put(body)
                                    .header("Content-Type", "application/json");
                                
                                System.out.println("URL PUT: " + putUrl);
                            } else {
                                // Nếu chưa làm thì dùng POST
                                requestBuilder = new Request.Builder()
                                    .url(ApiConfig.getFullUrl(ApiConfig.NOP_BAI_CODE_ENDPOINT))
                                    .post(body)
                                    .header("Content-Type", "application/json");
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
                                            try {
                                                String responseData = response.body().string();
                                                JSONObject jsonObject = new JSONObject(responseData);
                                                String resDa = jsonObject.getString("data");
                                                JSONObject data2 = new JSONObject(resDa);
                                                int NopBaiId = data2.getInt("id");

                                                System.out.println("response: " + responseData);
                                                
                                                // Lưu phản hồi AI
                                                luuPhanHoiAI(nodungphanhoi, goiYCaiThien, diem, NopBaiId, daLam);

                                                // Vô hiệu hóa code editor
                                                etCode.setEnabled(false);
                                                
                                                // Thay đổi nút Run thành nút Làm lại
                                                btnRun.setText("Làm lại");
                                                btnRun.setOnClickListener(v2 -> {
                                                    // Reset trạng thái
                                                    etCode.setEnabled(true);
                                                    etCode.setText("");
                                                    tvKetQua.setText("");
                                                    tvKetQua.setVisibility(View.GONE);
                                                    btnRun.setText("Run");
                                                    btnRun.setOnClickListener(v3 -> {
                                                        String code = etCode.getText().toString();
                                                        String language = spinnerLanguage.getSelectedItem().toString().toLowerCase();
                                                        
                                                        if (code.isEmpty()) {
                                                            Toast.makeText(code.this, "Vui lòng nhập code", Toast.LENGTH_SHORT).show();
                                                            return;
                                                        }

                                                        // Xóa kết quả cũ
                                                        tvKetQua.setText("");
                                                        isExecuting = true;
                                                        etInput.setEnabled(false);
                                                        
                                                        // Gửi code lên server
                                                        socketManager.executeCode(code, language, "");
                                                    });
                                                    
                                                    // Load lại bài tập
                                                    loadBaiTap();
                                                });

                                                Toast.makeText(code.this, "Nộp bài thành công!", Toast.LENGTH_SHORT).show();
                                                kiemTraDaLamCode(userId);

                                                if (!daLamCode) {
                                                    capNhatTienDo();
                                                }
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                                Toast.makeText(code.this, "Lỗi xử lý kết quả nộp bài", Toast.LENGTH_SHORT).show();
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                                Toast.makeText(code.this, "Lỗi đọc kết quả nộp bài", Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            Log.e("RESPONSE_FAIL", "Response not successful: " + response.code());
                                        }
                                    });
                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(code.this, "Lỗi tạo dữ liệu nộp bài", Toast.LENGTH_SHORT).show();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        runOnUiThread(() -> {
                            Toast.makeText(code.this, "Lỗi đọc kết quả", Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            }
        });
    }

    private void luuPhanHoiAI(String noiDung, String goiYCaiThien, Double diem, int NopbaiId, boolean daLam) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("idNopBai", NopbaiId);
            jsonObject.put("noiDung", noiDung);
            jsonObject.put("goiYCaiThien", goiYCaiThien);
            jsonObject.put("diem", diem);

            System.out.println("post save phan hoi: " + jsonObject.toString());
            RequestBody body = RequestBody.create(jsonObject.toString(), MediaType.parse("application/json"));

            Request.Builder requestBuilder;
            if (daLam) {
                // Nếu đã có phản hồi thì dùng PUT
                String putUrl = ApiConfig.getFullUrl(ApiConfig.Post_save_phan_hoi_AI_ENDPOINT)
                        + "?idNopBai=" + NopbaiId;

                requestBuilder = new Request.Builder()
                        .url(putUrl)
                        .put(body)
                        .header("Content-Type", "application/json");

                System.out.println("URL PUT phan hoi: " + putUrl);
            } else {
                // Nếu chưa có phản hồi thì dùng POST
                requestBuilder = new Request.Builder()
                        .url(ApiConfig.getFullUrl(ApiConfig.Post_save_phan_hoi_AI_ENDPOINT))
                        .post(body)
                        .header("Content-Type", "application/json");
            }

            Request request = requestBuilder.build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> {
                        System.out.println("err luu phan hoi AI: " + e.toString());
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        runOnUiThread(() -> {
                            System.out.println("err luu phan hoi AI: " + response.toString());
                        });
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void kiemTraDaLamCode(String userId) {
        String url = ApiConfig.getFullUrl(ApiConfig.Check_TienDo_code_ENDPOINT) + "?idNguoiDung=" + userId + "&idBaiHoc=" + idBaiHoc;

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    System.out.println("err kiem tra da lam code: " + e.toString());
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseData = response.body().string();
                        JSONObject jsonObject = new JSONObject(responseData);
                        daLamCode = jsonObject.getBoolean("data");
                        System.out.println("da lam Code: " + daLamCode);

                        // Nếu đã làm code, lấy thông tin bài đã làm
                        if (daLamCode) {
                            layBaiCodeDaLam(userId);
                        } else {
                            // Nếu chưa làm, lấy bài tập mới
                            layBaiTapCode();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void layBaiTapCode() {
        String url = ApiConfig.getFullUrl(ApiConfig.GET_code_excercise_ENDPOINT)
                + "?mucDo=" + mucDoHienTai
                + "&idBaiHoc=" + idBaiHoc;
        System.out.println("layBaiTapCode url: " + url);

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    System.out.println("err lay bai tap code: " + e.toString());
                    Toast.makeText(code.this, "Lỗi tải bài tập", Toast.LENGTH_SHORT).show();
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
                            String deBai = jsonObject.getString("deBai")
                                    + "\n " + "Đầu vào: " + jsonObject.getString("dauVaoMau")
                                    + "\n" + "Đầu ra: " + jsonObject.getString("dauRaMau");

                            runOnUiThread(() -> {
                                tvDeBai.setText(deBai);
                                tvDeBai.setVisibility(View.VISIBLE);
                                isDeBaiVisible = true;
                            });
                        } else {
                            runOnUiThread(() -> {
                                Toast.makeText(code.this, "Không tìm thấy bài tập", Toast.LENGTH_SHORT).show();
                            });
                        }
                        System.out.println("response: " + responseData);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        runOnUiThread(() -> {
                            Toast.makeText(code.this, "Lỗi xử lý dữ liệu", Toast.LENGTH_SHORT).show();
                        });
                    }
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(code.this, "Lỗi tải bài tập", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void layBaiCodeDaLam(String userId) {
        String url = ApiConfig.getFullUrl(ApiConfig.get_code_completed_ENDPOINT)
                + "?idNguoiDung=" + userId
                + "&idBaiHoc=" + idBaiHoc
                + "&mucDo=" + mucDoHienTai;
        System.out.println("layBaiCodeDaLam url: " + url);
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    System.out.println("err lay bai code da lam: " + e.toString());
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
                            codeCu = jsonObject.getString("dapAnNguoiDung");
                            diemCu = jsonObject.getInt("diem");
                            int NopBaiId = jsonObject.getInt("idNopBai");
                            String deBai = jsonObject.getString("deBai")
                                    + "\n " + "Đầu vào: " + jsonObject.getString("dauVaoMau")
                                    + "\n" + "Đầu ra: " + jsonObject.getString("dauRaMau");

                            runOnUiThread(() -> {
                                hienThiBaiCodeDaLam(deBai);
                                // Lấy phản hồi AI
                                layPhanHoiAI(NopBaiId);
                            });
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void hienThiBaiCodeDaLam(String deBai) {
        // Hiển thị đề bài
        tvDeBai.setText(deBai);
        tvDeBai.setVisibility(View.VISIBLE);
        isDeBaiVisible = true;

        // Hiển thị code cũ
        etCode.setText(codeCu);
        etCode.setEnabled(false);

        // Hiển thị điểm cũ
        String ketQua = "Điểm cũ: " + diemCu + "/10";
        tvKetQua.setText(ketQua);
        tvKetQua.setVisibility(View.VISIBLE);

        // Thay đổi nút
        btnSubmit.setText("Làm lại");
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
        // Kiểm tra bài code đã làm khi vào activity
        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String userId = sharedPreferences.getString("id", "");
        kiemTraDaLamCode(userId);
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
                // Hiển thị prompt cho người dùng
                tvKetQua.append(output);
                return;
            }

            // Thêm output vào TextView
            if (tvKetQua.getVisibility() != View.VISIBLE) {
                tvKetQua.setVisibility(View.VISIBLE);
            }
            
            // Kiểm tra xem output có phải là giá trị input không
            if (!output.equals(etInput.getText().toString().trim())) {
                tvKetQua.append(output);
            }

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
            
            // Đảm bảo TextView được hiển thị
            tvKetQua.setVisibility(View.VISIBLE);
            
            // Xóa nội dung cũ nếu cần
            if (tvKetQua.getText().toString().isEmpty()) {
                tvKetQua.setText("Lỗi: " + error);
            } else {
                tvKetQua.append("\nLỗi: " + error);
            }
            
            // Log lỗi để debug
            System.out.println("Lỗi: " + error);
            
            // Hiển thị Toast với thông báo lỗi ngắn gọn
            String shortError = error.split("\n")[0];
            Toast.makeText(this, "Lỗi: " + shortError, Toast.LENGTH_SHORT).show();
        });
    }

    private void kiemTraKetQuaCu() {
        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String userId = sharedPreferences.getString("id", "");

        String url = ApiConfig.getFullUrl(ApiConfig.get_code_completed_ENDPOINT)
                + "?idNguoiDung=" + userId
                + "&idBaiHoc=" + idBaiHoc
                + "&mucDo=" + mucDoHienTai;

        System.out.println("kiemTraKetQuaCu url: " + url);
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    System.out.println("err kiem tra ket qua cu: " + e.toString());
                    Toast.makeText(code.this, "Lỗi kiểm tra bài làm", Toast.LENGTH_SHORT).show();
                    // Nếu không kiểm tra được thì load bài mới
                    layBaiTapCode();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseData = response.body().string();
                        JSONArray jsonArray = new JSONArray(responseData);

                        if (jsonArray.length() > 0) {
                            // Đã có bài làm
                            JSONObject jsonObject = jsonArray.getJSONObject(0);
                            diemCu = jsonObject.getInt("diem");
                            codeCu = jsonObject.getString("dapAnNguoiDung");
                            NopBaiId = jsonObject.getInt("idNopBai");
                            String deBai = jsonObject.getString("deBai")
                                    + "\n " + "Đầu vào: " + jsonObject.getString("dauVaoMau")
                                    + "\n" + "Đầu ra: " + jsonObject.getString("dauRaMau");

                            runOnUiThread(() -> {
                                // Hiển thị đề bài
                                tvDeBai.setText(deBai);
                                tvDeBai.setVisibility(View.VISIBLE);
                                isDeBaiVisible = true;
                                // Hiển thị kết quả cũ
                                hienThiKetQuaCu();
                            });
                        } else {
                            // Chưa có bài làm
                            runOnUiThread(() -> {
                                // Reset trạng thái
                                resetCode();
                                // Load bài tập mới
                                layBaiTapCode();
                            });
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        runOnUiThread(() -> {
                            Toast.makeText(code.this, "Lỗi xử lý dữ liệu", Toast.LENGTH_SHORT).show();
                            // Nếu xử lý lỗi thì load bài mới
                            layBaiTapCode();
                        });
                    }
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(code.this, "Lỗi kiểm tra bài làm", Toast.LENGTH_SHORT).show();
                        // Nếu lỗi thì load bài mới
                        layBaiTapCode();
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

        // Hiển thị code cũ
        etCode.setText(codeCu);
        etCode.setEnabled(false);
        etCode.setFocusable(false);
        etCode.setClickable(true);
        etCode.setLongClickable(true);
        etCode.setTextIsSelectable(true);

        // Thay đổi nút
        btnSubmit.setText("Làm lại");
        btnSubmit.setVisibility(View.VISIBLE);
        daLamCode = true;

        // Hiển thị nút xem phản hồi AI
        btnXemPhanHoi.setVisibility(View.VISIBLE);
    }

    private void resetCode() {
        // Reset trạng thái
        daLamCode = false;
        diemCu = 0;
        tvKetQua.setVisibility(View.GONE);
        btnSubmit.setText("Xem kết quả");
        btnSubmit.setVisibility(View.VISIBLE);
        btnXemPhanHoi.setVisibility(View.GONE);

        // Bật lại code editor
        etCode.setEnabled(true);
        etCode.setFocusableInTouchMode(true);
        etCode.setClickable(true);
        etCode.setLongClickable(true);
        etCode.setTextIsSelectable(true);
        etCode.setText("");

        // Reset input
        etInput.setText("");
        etInput.setEnabled(false);
        isExecuting = false;
    }

    private void loadBaiTap() {
        // Kiểm tra kết quả cũ trước khi load bài mới
        kiemTraKetQuaCu();
    }

    private void hienThiDialogPhanHoiAI(String noiDung, String goiYCaiThien, Double diem) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        btnXemPhanHoi.setVisibility(View.VISIBLE);
        builder.setTitle("Phản hồi từ AI");

        // Tạo layout cho dialog
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        // Thêm điểm
        TextView tvDiem = new TextView(this);
        tvDiem.setText("Điểm: " + diem + "/10");
        tvDiem.setTextSize(18);
        tvDiem.setTypeface(null, Typeface.BOLD);
        layout.addView(tvDiem);

        // Thêm nội dung phản hồi
        TextView tvNoiDung = new TextView(this);
        tvNoiDung.setText("\nNội dung phản hồi:\n" + noiDung);
        tvNoiDung.setTextSize(16);
        layout.addView(tvNoiDung);

        // Thêm gợi ý cải thiện
        TextView tvGoiY = new TextView(this);
        tvGoiY.setText("\nGợi ý cải thiện:\n" + goiYCaiThien);
        tvGoiY.setTextSize(16);
        layout.addView(tvGoiY);

        builder.setView(layout);
        builder.setPositiveButton("Đóng", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void layPhanHoiAI(int NopBaiId) {
        String url = ApiConfig.getFullUrl(ApiConfig.Get_phan_hoi_AI_ENDPOINT) + NopBaiId;

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    System.out.println("err lay phan hoi AI: " + e.toString());
                    Toast.makeText(code.this, "Lỗi lấy phản hồi AI", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseData = response.body().string();
                        JSONObject jsonObject = new JSONObject(responseData);
                        String data2= jsonObject.getString("data");
                        JSONObject jsonObject2 = new JSONObject(data2);
                        System.out.println("get url ai: "+url);
                        String noiDung = jsonObject2.getString("noiDung");
                        String goiYCaiThien = jsonObject2.getString("goiYCaiThien");
                        String dataDiem= jsonObject2.getString("nopBai");
                        JSONObject jsonObject3 = new JSONObject(dataDiem);
                        Double diem = jsonObject3.getDouble("diem");

                        runOnUiThread(() -> {
                            hienThiDialogPhanHoiAI(noiDung, goiYCaiThien, diem);
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                        runOnUiThread(() -> {
                            Toast.makeText(code.this, "Lỗi xử lý phản hồi AI", Toast.LENGTH_SHORT).show();
                        });
                    }
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(code.this, "Không tìm thấy phản hồi AI", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void executeSQLCode(String code) {
        try {
            JSONObject jsonObject = new JSONObject();
            // Chuyển đổi ký tự xuống dòng thành dấu cách
            String cleanSQL = code.replace("\n", " ");
            
            jsonObject.put("versionIndex", "3");
            jsonObject.put("stdin", "");
            jsonObject.put("clientId", "37b74c2b9f31a362ad8ecb4ecbb22441");
            jsonObject.put("language", "sql");
            jsonObject.put("clientSecret", "c01683242339952959b606fa035fbaeb59b6894fb11bf3dfa8415994c0b5ba0c");
            jsonObject.put("script", cleanSQL);

            System.out.println("executeSQLCode: " + jsonObject.toString());
            RequestBody body = RequestBody.create(jsonObject.toString(), MediaType.parse("application/json"));
            Request request = new Request.Builder()
                    .url(ApiConfig.getFullUrl("/api/code/execute"))
                    .post(body)
                    .build();

            new Thread(() -> {
                try {
                    Response response = client.newCall(request).execute();
                    String responseData = response.body().string();

                    // Parse JSON và lấy output
                    JSONObject jsonResponse = new JSONObject(responseData);
                    JSONObject data = jsonResponse.optJSONObject("data");
                    String output = data != null ? data.optString("output") : "Không có output";

                    runOnUiThread(() -> {
                        tvKetQua.setVisibility(View.VISIBLE);
                        if (response.isSuccessful()) {
                            tvKetQua.setText(output);  // In ra output
                        } else {
                            tvKetQua.setText(output);
                        }
                    });
                } catch (IOException | JSONException e) {
                    runOnUiThread(() -> {
                        tvKetQua.setVisibility(View.VISIBLE);
                        tvKetQua.setText("Lỗi: Không thể kết nối đến server");
                        Toast.makeText(code.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                    });
                }
            }).start();
        } catch (JSONException e) {
            e.printStackTrace();
            tvKetQua.setVisibility(View.VISIBLE);
            tvKetQua.setText("Lỗi: Không thể tạo request");
        }
    }
}