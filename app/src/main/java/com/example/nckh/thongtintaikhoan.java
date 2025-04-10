package com.example.nckh;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.squareup.picasso.Picasso;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class thongtintaikhoan extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 2;
    private static final int REQUEST_PERMISSIONS = 3;
    private ImageView imgAvatar;
    private TextView txtChangeAvatar;
    private EditText edtHoTen, edtEmail, edtSDT;
    private Spinner spinnerTinhThanh, spinnerNgheNghiep;
    private Button btnSave;
    private SharedPreferences sharedPreferences;
    private String email;
    private Uri selectedImageUri;
    private String currentPhotoPath;
    OkHttpClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thongtintaikhoan);

        // Khởi tạo SharedPreferences
        sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        Intent intent = getIntent();
        email = intent.getStringExtra("email");
        client = new OkHttpClient();
        // Ánh xạ các view
        imgAvatar = findViewById(R.id.imgAvatar);
        txtChangeAvatar = findViewById(R.id.txtChangeAvatar);
        edtHoTen = findViewById(R.id.edtHoTen);
        edtEmail = findViewById(R.id.edtEmail);
        edtSDT = findViewById(R.id.edtSDT);
        spinnerTinhThanh = findViewById(R.id.spinnerTinhThanh);
        spinnerNgheNghiep = findViewById(R.id.spinnerNgheNghiep);
        btnSave = findViewById(R.id.btnSave);

        // Load thông tin người dùng
        loadUserInfo();

        // Xử lý sự kiện click nút Lưu
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUserInfo();
            }
        });

        // Xử lý sự kiện click thay đổi ảnh đại diện
        txtChangeAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAndRequestPermissions();
            }
        });
    }

    private void checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            
            ActivityCompat.requestPermissions(this,
                new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                },
                REQUEST_PERMISSIONS);
        } else {
            showImagePickerOptions();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showImagePickerOptions();
            } else {
                Toast.makeText(this, "Cần cấp quyền để sử dụng tính năng này", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showImagePickerOptions() {
        String[] options = {"Chọn ảnh từ thư viện", "Chụp ảnh mới"};
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Chọn ảnh đại diện");
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                openGallery();
            } else {
                dispatchTakePictureIntent();
            }
        });
        builder.show();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(this, "Lỗi tạo file ảnh", Toast.LENGTH_SHORT).show();
            }
            if (photoFile != null) {
                selectedImageUri = FileProvider.getUriForFile(this,
                        "com.example.nckh.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, selectedImageUri);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE_REQUEST && data != null && data.getData() != null) {
                selectedImageUri = data.getData();
                imgAvatar.setImageURI(selectedImageUri);
                uploadImage(selectedImageUri);
            } else if (requestCode == REQUEST_IMAGE_CAPTURE) {
                imgAvatar.setImageURI(selectedImageUri);
                uploadImage(selectedImageUri);
            }
        }
    }

    private void uploadImage(Uri imageUri) {
        try {
            // Mở InputStream từ Uri
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            if (inputStream == null) {
                Toast.makeText(this, "Không thể mở ảnh", Toast.LENGTH_SHORT).show();
                return;
            }

            // Đọc byte[] từ InputStream
            byte[] fileBytes = IOUtils.toByteArray(inputStream);
            inputStream.close();

            // Tạo tên file ngẫu nhiên
            String fileName = "image_" + System.currentTimeMillis() + ".png";

            // Tạo request body cho file
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/png"), fileBytes);

            // Tạo MultipartBody
            MultipartBody.Builder builder = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", fileName, requestFile);

            RequestBody requestBody = builder.build();

            // Gửi request bằng OkHttp
            Request request = new Request.Builder()
                    .url("http://192.168.1.10:8080/api/upload")
                    .addHeader("accept", "*/*")
                    .post(requestBody)
                    .build();

            System.out.println("Upload URL: " + request.url().toString());
            System.out.println("fileName: "+fileName);
            // Lưu URL ảnh vào SharedPreferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("avatar", fileName);
            editor.apply();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> {
                        Toast.makeText(thongtintaikhoan.this, "Lỗi upload ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        System.out.println("Upload error: " + e.getMessage());
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body().string();
                    System.out.println("Upload response: " + responseBody);

                    if (response.isSuccessful()) {
                        try {
                            JSONObject jsonResponse = new JSONObject(responseBody);


                            runOnUiThread(() -> {
                                Toast.makeText(thongtintaikhoan.this, "Upload ảnh thành công", Toast.LENGTH_SHORT).show();
                                Picasso.get()
                                        .load(ApiConfig.getFullUrl(ApiConfig.get_imagge_ENDPOINT + fileName))
                                        .placeholder(R.drawable.user)
                                        .error(R.drawable.user)
                                        .into(imgAvatar);
                            });
                        } catch (JSONException e) {
                            runOnUiThread(() -> {
                                Toast.makeText(thongtintaikhoan.this, "Lỗi xử lý phản hồi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                        }
                    } else {
                        runOnUiThread(() -> {
                            System.out.println("err upload: "+ response.toString());
                            System.out.println("Response body: " + responseBody);
                        });
                    }
                }
            });

        } catch (Exception e) {
            Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            System.out.println("Error in uploadImage: " + e.getMessage());
        }
    }

    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        android.database.Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor == null) return null;
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String path = cursor.getString(column_index);
        cursor.close();
        return path;
    }

    private void loadUserInfo() {
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
                    Toast.makeText(thongtintaikhoan.this, "Lỗi kết nối: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                        String avatar = jsonObject.getString("anhDaiDien");
                        System.out.println("name: "+name);
                        sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
                        String phone=sharedPreferences.getString("phone","xxxxxxxxxx");

                        runOnUiThread(() -> {
                            edtHoTen.setText(name);
                            edtEmail.setText(email);
                            edtSDT.setText(phone);

                            if (avatar != null && !avatar.isEmpty()) {
                                Glide.with(thongtintaikhoan.this)
                                        .load(ApiConfig.getFullUrl(ApiConfig.get_imagge_ENDPOINT + avatar))
                                        .placeholder(R.drawable.user)
                                        .error(R.drawable.user)
                                        .circleCrop() // hoặc .circleCrop() nếu muốn avatar tròn
                                        .into(imgAvatar);
                            } else {
                                imgAvatar.setImageResource(R.drawable.user);
                            }
                        });
                    } catch (JSONException e) {
                        runOnUiThread(() -> {
                            System.out.println("error data: "+e.toString());
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

    private void updateUserInfo() {
        String tenDangNhap = edtHoTen.getText().toString().trim();
        String sdt = edtSDT.getText().toString().trim();
        String avatarFileName = sharedPreferences.getString("avatar", "");
        
        System.out.println("in4: name: "+tenDangNhap +" sdt: "+sdt+" avatar: "+avatarFileName);
        
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("email", email);
            jsonObject.put("tenDangNhap", tenDangNhap);
            jsonObject.put("anhDaiDien", avatarFileName);
            jsonObject.put("sdt", sdt);
        } catch (Exception e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(
                MediaType.parse("application/json"),
                jsonObject.toString()
        );

        System.out.println("Update user request body: " + jsonObject.toString());

        Request request = new Request.Builder()
                .url(ApiConfig.getFullUrl(ApiConfig.POST_UPDATE_USER_ENDPOINT))
                .put(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(thongtintaikhoan.this, "Lỗi kết nối: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String responseBody = response.body().string();
                    System.out.println("Update user response: " + responseBody);

                    if (response.isSuccessful()) {
                        String message = "Cập nhật thành công";
                        
                        runOnUiThread(() -> {
                            Toast.makeText(thongtintaikhoan.this, message, Toast.LENGTH_SHORT).show();
                            // Cập nhật thông tin trong SharedPreferences
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("name", tenDangNhap);
                            editor.putString("phone", sdt);
                            editor.putString("avatar", avatarFileName);
                            editor.apply();
                            finish();
                        });
                    } else {
                        String errorMessage = "Có lỗi xảy ra";
                        runOnUiThread(() ->
                                Toast.makeText(thongtintaikhoan.this, "Lỗi: " + errorMessage, Toast.LENGTH_SHORT).show()
                        );
                    }
                } catch (Exception e) {
                    runOnUiThread(() ->
                            Toast.makeText(thongtintaikhoan.this, "Lỗi xử lý dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }
} 
