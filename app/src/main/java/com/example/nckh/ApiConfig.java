package com.example.nckh;

public class ApiConfig {
    // Base URL của API
    public static final String BASE_URL = "http://192.168.1.10"; // Thay thế bằng URL thực tế của API

    // Các endpoint
    public static final String REGISTER_ENDPOINT = "/api/user/register";
    public static final String SEND_OTP_ENDPOINT = "/api/otp/send";
    public static final String SEND_OTP_Reset_ENDPOINT = "/api/otp/sendOTPreset";

    public static final String VERIFY_OTP_ENDPOINT = "/api/otp/verify";
    public static final String LOGIN_ENDPOINT = "/api/auth/login";
    public static final String VALIDATE_TOKEN_ENDPOINT = "/api/sessions/validate";

    public static final String RESET_PASSWORD_ENDPOINT = "/api/user/dat-lai-mat-khau";

    public static final String GET_USER_INFO_ENDPOINT = "/api/user/";

    public  static final String get_bai_hoc_gan_nhat_ENDPOINT = "/api/tien-do/bai-hoc-gan-nhat?idNguoiDung=";

    public  static final String GET_BAI_HOC_ENDPOINT = "/api/tien-do/bai-hoc?";

    public static final String get_imagge_ENDPOINT = "/uploads/images/";
    public static final String POST_Doi_Pass_ENDPOINT = "/api/user/doi-mat-khau";

    public static final String POST_UPDATE_USER_ENDPOINT = "/api/user/cap-nhat-thong-tin";

    public static final String POST_UPLOAD_FILE_ENDPOINT = "/api/upload/file";

    public static final String GET_CAU_HOI_ENDPOINT = "/api/bai-tap-quiz/theo-muc-do";

    public static final String POST_CHAT_ENDPOINT = "/api/chat/send?message=";

    public static  String getSocketUrl (){return BASE_URL +":5000";  }
    // Phương thức để lấy URL đầy đủ
    public static String getFullUrl(String endpoint) {
        return BASE_URL +":8080"+ endpoint;
    }
} 