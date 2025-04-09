package com.example.nckh;

public class ApiConfig {
    // Base URL của API
    public static final String BASE_URL = "http://192.168.1.10:8080"; // Thay thế bằng URL thực tế của API

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

    // Phương thức để lấy URL đầy đủ
    public static String getFullUrl(String endpoint) {
        return BASE_URL + endpoint;
    }
} 