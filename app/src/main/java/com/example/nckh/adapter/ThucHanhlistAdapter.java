package com.example.nckh.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nckh.ApiConfig;
import com.example.nckh.R;
import com.example.nckh.code;
import com.example.nckh.model.BaiHoc;
import com.example.nckh.quiz;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ThucHanhlistAdapter extends RecyclerView.Adapter<ThucHanhlistAdapter.ThucHanhlistViewHolder> {

    private List<BaiHoc> baiHocList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onQuizClick(BaiHoc baiHoc);
        void onCodeClick(BaiHoc baiHoc);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public ThucHanhlistAdapter(List<BaiHoc> baiHocList) {
        this.baiHocList = baiHocList;
    }

    @NonNull
    @Override
    public ThucHanhlistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_baihoc_thuchanh, parent, false);
        return new ThucHanhlistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ThucHanhlistViewHolder holder, int position) {
        BaiHoc baiHoc = baiHocList.get(position);
        holder.tvTieuDe.setText(baiHoc.getTieuDe());

        String trangThai = "";
        if (baiHoc.getTrangThai().equals("dang_hoc")) {
            trangThai = "Đang học";
        } else if (baiHoc.getTrangThai().equals("chua_hoc")) {
            trangThai = "Chưa học";
        } else if (baiHoc.getTrangThai().equals("da_hoc")) {
            trangThai = "Đã học";
        }
        holder.tvTrangThai.setText("Trạng thái: " + trangThai);

        // Hiển thị thông tin Quiz
        if (baiHoc.isDaLamQuiz()) {
            holder.layoutQuizInfo.setVisibility(View.VISIBLE);
            holder.tvQuizScore.setText("Điểm Quiz: " + baiHoc.getDiemQuiz());
            String mucDoQuiz = "";
            if (baiHoc.getMucDoQuiz().equals("co_ban")) {
                mucDoQuiz = "Cơ bản";
            } else if (baiHoc.getMucDoQuiz().equals("trung_binh")) {
                mucDoQuiz = "Trung bình";
            } else if (baiHoc.getMucDoQuiz().equals("nang_cao")) {
                mucDoQuiz = "Nâng cao";
            }
            holder.tvQuizLevel.setText("Mức độ: " + mucDoQuiz);
        } else {
            holder.layoutQuizInfo.setVisibility(View.GONE);
        }

        // Hiển thị thông tin Code
        if (baiHoc.isDaLamCode()) {
            holder.layoutCodeInfo.setVisibility(View.VISIBLE);
            holder.tvCodeScore.setText("Điểm Code: " + baiHoc.getDiemCode());
            String mucDoCode = "";
            if (baiHoc.getMucDoCode().equals("co_ban")) {
                mucDoCode = "Cơ bản";
            } else if (baiHoc.getMucDoCode().equals("trung_binh")) {
                mucDoCode = "Trung bình";
            } else if (baiHoc.getMucDoCode().equals("kho")) {
                mucDoCode = "Khó";
            }
            holder.tvCodeLevel.setText("Mức độ: " + mucDoCode);
        } else {
            holder.layoutCodeInfo.setVisibility(View.GONE);
        }

        if (baiHoc.getAnhBaiHoc() != null && !baiHoc.getAnhBaiHoc().isEmpty()) {
            Picasso.get()
                    .load(ApiConfig.getFullUrl(ApiConfig.get_imagge_ENDPOINT + baiHoc.getAnhBaiHoc()))
                    .placeholder(R.drawable.user)
                    .error(R.drawable.user)
                    .into(holder.imgBaiHoc);
        } else {
            holder.imgBaiHoc.setImageResource(R.drawable.user);
        }

        // Xử lý sự kiện click cho nút Quiz
        holder.quiz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Quiz clicked for lesson: " + baiHoc.getTieuDe());
                System.out.println("Quiz clicked for lesson id: " + baiHoc.getId());
                if (listener != null) {
                    listener.onQuizClick(baiHoc);
                } else {
                    System.out.println("Listener is null");
                }
            }
        });

        // Xử lý sự kiện click cho nút Code
        holder.code.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Code clicked for lesson: " + baiHoc.getTieuDe());
                if (listener != null) {
                    listener.onCodeClick(baiHoc);
                } else {
                    System.out.println("Listener is null");
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return baiHocList.size();
    }

    public static class ThucHanhlistViewHolder extends RecyclerView.ViewHolder {
        ImageView imgBaiHoc;
        TextView tvTieuDe, tvTrangThai;
        LinearLayout allinner_baihocgannhat;
        Button quiz, code;
        
        // Thêm các view mới
        LinearLayout layoutQuizInfo, layoutCodeInfo;
        TextView tvQuizScore, tvQuizLevel, tvCodeScore, tvCodeLevel;

        public ThucHanhlistViewHolder(@NonNull View itemView) {
            super(itemView);
            imgBaiHoc = itemView.findViewById(R.id.img_baihoc);
            tvTieuDe = itemView.findViewById(R.id.titlegannhat);
            tvTrangThai = itemView.findViewById(R.id.tinhtrangbaigannhat);
            allinner_baihocgannhat = itemView.findViewById(R.id.allinner_baihocgannhat);
            quiz = itemView.findViewById(R.id.btn_tracnghiem);
            code = itemView.findViewById(R.id.btn_code);
            
            // Khởi tạo các view mới
            layoutQuizInfo = itemView.findViewById(R.id.layoutQuizInfo);
            layoutCodeInfo = itemView.findViewById(R.id.layoutCodeInfo);
            tvQuizScore = itemView.findViewById(R.id.tvQuizScore);
            tvQuizLevel = itemView.findViewById(R.id.tvQuizLevel);
            tvCodeScore = itemView.findViewById(R.id.tvCodeScore);
            tvCodeLevel = itemView.findViewById(R.id.tvCodeLevel);
        }
    }
}