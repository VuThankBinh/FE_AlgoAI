package com.example.nckh;

public class ApiConfig {
    // Base URL của API
    public static final String BASE_URL = "http://192.168.250.139"; // Thay thế bằng URL thực tế của API

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
    public static final String POST_UPLOAD_FILE_ENDPOINT = "/api/upload";

    public static final String GET_CAU_HOI_ENDPOINT = "/api/bai-tap-quiz/theo-muc-do";

    public static final String POST_CHAT_ENDPOINT = "/api/chat/send?message=";

    public static final String NOP_BAI_QUIZ_ENDPOINT = "/api/nop-bai/quiz";

    public static final String NOP_BAI_CODE_ENDPOINT = "/api/nop-bai/code";
    public static final String CAP_NHAT_TIEN_DO_ENDPOINT = "/api/tien-do/cap-nhat";

    public static final String Check_TienDo_quiz_ENDPOINT = "/api/nop-bai/kiem-tra-da-lam-quiz";

    public  static final  String get_quiz_completed_ENDPOINT = "/api/nop-bai/bai-tap-quiz-theo-muc-do-co-de-bai";

    public static final String Check_TienDo_code_ENDPOINT = "/api/nop-bai/kiem-tra-da-lam-code";

    public static final String get_code_completed_ENDPOINT  = "/api/nop-bai/bai-tap-code-theo-muc-do-co-de-bai";

    public static final String GET_code_excercise_ENDPOINT = "/api/bai-tap-code/theo-muc-do";
    public static final String Post_cham_diem_code_ENDPOINT = "/api/code-grading/grade";
    public static final String Post_save_phan_hoi_AI_ENDPOINT = "/api/phan-hoi-ai/luu";
    public static final String get_code_BY_LEVEL_ENDPOINT = "/api/nop-bai/kiem-tra/code/theo-muc-do";
    public static final String NOP_BAI_CODE_AGAINT_ENDPOINT = "/api/nop-bai/code";
    public static final String Get_phan_hoi_AI_ENDPOINT = "/api/phan-hoi-ai/";

    public static final String excute_jdoode_ENDPOINT = "/api/code/execute";


    public static  String getSocketUrl (){return BASE_URL +":5000";  }
    // Phương thức để lấy URL đầy đủ
    public static String getFullUrl(String endpoint) {
        return BASE_URL +":8080"+ endpoint;
    }
} 