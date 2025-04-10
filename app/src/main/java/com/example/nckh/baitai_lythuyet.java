package com.example.nckh;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nckh.adapter.ChatAdapter;
import com.example.nckh.model.ChatMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;

public class baitai_lythuyet extends AppCompatActivity {
    private TextView tvTieuDe, tvNoiDung;
    private Button btnMinhHoa;
    private WebView webView;
    private Intent intent;
    private String idBaiHoc, tieuDe, noiDung, linkYoutube, linkMoTa, anhBaiHoc;
    private ImageView aiBot;
    private boolean isChatExpanded = false;
    private Dialog chatDialog;
    private RecyclerView rvChatMessages;
    private EditText etMessage;
    private Button btnSend;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> messages;
    private OkHttpClient client;
    private String sessionId = "1s";
    private LinearLayout layoutSuggestedQuestions;
    private boolean isFirstChat = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_baitai_lythuyet);
        
        // Khởi tạo OkHttpClient
        client = new OkHttpClient();
        
        // Khởi tạo các view
        webView = findViewById(R.id.thumbnailImage);
        btnMinhHoa = findViewById(R.id.btnMinhHoa);
        tvTieuDe = findViewById(R.id.tvTieuDe);
        tvNoiDung = findViewById(R.id.tvNoiDung);
        aiBot = findViewById(R.id.aiBot);

        // Khởi tạo danh sách tin nhắn
        messages = new ArrayList<>();
        chatAdapter = new ChatAdapter(messages);

        // Cấu hình WebView
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebChromeClient(new WebChromeClient());

        // Nhận dữ liệu từ Intent
        intent = getIntent();
        getBaiHoc();

        // Xử lý sự kiện click nút minh họa
        btnMinhHoa.setOnClickListener(v -> showMinhHoa(baitai_lythuyet.this, linkMoTa));

        // Xử lý sự kiện click vào AI bot
        aiBot.setOnClickListener(v -> toggleChatWindow());
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
        chatDialog.setContentView(R.layout.dialog_chat);
        
        // Cấu hình dialog full màn
        Window window = chatDialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.setGravity(Gravity.CENTER);
        }

        // Khởi tạo các view trong dialog
        rvChatMessages = chatDialog.findViewById(R.id.rvChatMessages);
        etMessage = chatDialog.findViewById(R.id.etMessage);
        btnSend = chatDialog.findViewById(R.id.btnSend);
        ImageView btnClose = chatDialog.findViewById(R.id.btnCloseChat);
        layoutSuggestedQuestions = chatDialog.findViewById(R.id.layoutSuggestedQuestions);

        // Cấu hình RecyclerView
        rvChatMessages.setLayoutManager(new LinearLayoutManager(this));
        rvChatMessages.setAdapter(chatAdapter);

        // Xử lý sự kiện đóng dialog
        btnClose.setOnClickListener(v -> toggleChatWindow());

        // Xử lý sự kiện các câu hỏi gợi ý
        Button btnQuestion1 = chatDialog.findViewById(R.id.btnQuestion1);
        Button btnQuestion2 = chatDialog.findViewById(R.id.btnQuestion2);
        Button btnQuestion3 = chatDialog.findViewById(R.id.btnQuestion3);

        View.OnClickListener questionClickListener = v -> {
            Button clickedButton = (Button) v;
            String question = clickedButton.getText().toString() +"về bài học:" + tieuDe;
            sendMessageToAPI(question);
            layoutSuggestedQuestions.setVisibility(View.GONE);
            isFirstChat = false;
        };

        btnQuestion1.setOnClickListener(questionClickListener);
        btnQuestion2.setOnClickListener(questionClickListener);
        btnQuestion3.setOnClickListener(questionClickListener);

        // Xử lý sự kiện gửi tin nhắn
        btnSend.setOnClickListener(v -> {
            String message = etMessage.getText().toString().trim();
            if (!message.isEmpty()) {
                // Thêm tin nhắn của người dùng
                chatAdapter.addMessage(new ChatMessage(message, true));
                etMessage.setText("");

                // Gọi API chat
                sendMessageToAPI(message);

                // Ẩn layout câu hỏi gợi ý nếu là lần chat đầu tiên
                if (isFirstChat) {
                    layoutSuggestedQuestions.setVisibility(View.GONE);
                    isFirstChat = false;
                }
            }
        });
    }

    private void sendMessageToAPI(String message) {
        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        sessionId = sharedPreferences.getString("sessionId", "");
        String url = ApiConfig.getFullUrl(ApiConfig.POST_CHAT_ENDPOINT) + message + "&sessionId=" + sessionId;

        RequestBody body = RequestBody.create("", null);

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        System.out.println(url);

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(baitai_lythuyet.this, "Lỗi kết nối: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String responseBody = response.body().string();
                    final String[] reply = {""};
                    runOnUiThread(() -> {
                        try {
                            JSONObject responseJson = new JSONObject(responseBody);
                            reply[0] = responseJson.getString("content");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        chatAdapter.addMessage(new ChatMessage(reply[0], false));
                        rvChatMessages.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
                    });
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(baitai_lythuyet.this, "Lỗi phản hồi từ server", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    public void showMinhHoa(Context context, String webUrl) {
        Intent intent = new Intent(context, webview.class);
        intent.putExtra("url", webUrl);
        startActivity(intent);
    }

    public void getBaiHoc() {
        if (intent != null) {
            idBaiHoc = intent.getStringExtra("id_bai_hoc");
            tieuDe = intent.getStringExtra("tieu_de");
            noiDung = intent.getStringExtra("noi_dung");
            linkYoutube = intent.getStringExtra("link_youtube");
            linkMoTa = intent.getStringExtra("link_mo_ta");
            anhBaiHoc = intent.getStringExtra("anh_bai_hoc");

            if(linkMoTa.equals("null") || linkMoTa.equals("") || linkMoTa==null){
                btnMinhHoa.setVisibility(View.GONE);
            } else {
                btnMinhHoa.setVisibility(View.VISIBLE);
            }

            // Hiển thị dữ liệu
            tvTieuDe.setText(tieuDe);
            tvNoiDung.setText(noiDung);

            String html = "<iframe width=\"100%\" height=\"100%\" " +
                    "src="+linkYoutube+"?rel=0&autoplay=1&modestbranding=1\" " +
                    "frameborder=\"0\" allowfullscreen></iframe>";

            webView.loadData(html, "text/html", "utf-8");
        }

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.setWebChromeClient(new WebChromeClient() {
            private View mCustomView;
            private CustomViewCallback mCustomViewCallback;
            private FrameLayout mFullscreenContainer;
            private WebChromeClient.CustomViewCallback mCallback;
            private int mOriginalSystemUiVisibility;

            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                if (mCustomView != null) {
                    callback.onCustomViewHidden();
                    return;
                }

                mCustomView = view;
                mOriginalSystemUiVisibility = getWindow().getDecorView().getSystemUiVisibility();
                mCustomViewCallback = callback;

                FrameLayout decor = (FrameLayout) getWindow().getDecorView();
                decor.addView(mCustomView, new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));

                getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_FULLSCREEN |
                                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            }

            @Override
            public void onHideCustomView() {
                FrameLayout decor = (FrameLayout) getWindow().getDecorView();
                decor.removeView(mCustomView);
                mCustomView = null;

                getWindow().getDecorView().setSystemUiVisibility(mOriginalSystemUiVisibility);
                mCustomViewCallback.onCustomViewHidden();
            }
        });
    }
}