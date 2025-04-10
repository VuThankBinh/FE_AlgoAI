package com.example.nckh.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.nckh.ApiConfig;
import com.example.nckh.R;
import com.example.nckh.model.BaiHoc;
import com.squareup.picasso.Picasso;

import java.util.List;

public class BaiHocAdapterNgang extends RecyclerView.Adapter<BaiHocAdapterNgang.BaiHocViewHolder> {

    private List<BaiHoc> baiHocList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(BaiHoc baiHoc);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public BaiHocAdapterNgang(List<BaiHoc> baiHocList) {
        this.baiHocList = baiHocList;
    }

    @NonNull
    @Override
    public BaiHocViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_baihoc_ngang, parent, false);
        return new BaiHocViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BaiHocViewHolder holder, int position) {
        BaiHoc baiHoc = baiHocList.get(position);
        holder.bind(baiHoc);
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(baiHoc);
            }
        });
    }

    @Override
    public int getItemCount() {
        return baiHocList.size();
    }

    public static class BaiHocViewHolder extends RecyclerView.ViewHolder {
        ImageView imgBaiHoc;
        TextView tvTieuDe, tvTrangThai, tvDiem1, tvDiem2, noidung1, noidung2;
        LinearLayout allinner_baihocgannhat;

        public BaiHocViewHolder(@NonNull View itemView) {
            super(itemView);
            imgBaiHoc = itemView.findViewById(R.id.img_baihoc);
            tvTieuDe = itemView.findViewById(R.id.titlegannhat);
            tvTrangThai = itemView.findViewById(R.id.tinhtrangbaigannhat);
            allinner_baihocgannhat = itemView.findViewById(R.id.allinner_baihocgannhat);
            tvDiem1 = itemView.findViewById(R.id.diem1);
            tvDiem2 = itemView.findViewById(R.id.diem2);
            noidung1 = itemView.findViewById(R.id.noidung1);
            noidung2 = itemView.findViewById(R.id.noidung2);
        }

        public void bind(BaiHoc baiHoc) {
            tvTieuDe.setText(baiHoc.getTieuDe());
            if(baiHoc.getTrangThai().equals("chua_học"))
                tvTrangThai.setText("Trạng thái: Chưa học");
            if(baiHoc.getTrangThai().equals("dang_hoc"))
                tvTrangThai.setText("Trạng thái: Đang học");
            if(baiHoc.getTrangThai().equals("da_hoc"))
                tvTrangThai.setText("Trạng thái: Hoàn thành");

            if(baiHoc.isDaLamQuiz()){
                String mucdo="";
                if(baiHoc.getMucDoQuiz().equals("co_ban")) mucdo="Cơ bản";
                else if(baiHoc.getMucDoQuiz().equals("trung_binh")) mucdo="Trung bình";
                else if(baiHoc.getMucDoQuiz().equals("nang_cao")) mucdo="Nâng cao";
                noidung1.setText("Trắc nghiệm: "+baiHoc.getDiemQuiz() +" điểm ");
                tvDiem1.setText("Mức độ: "+mucdo);
            }
            else {
                noidung1.setText("Trắc nghiệm: Chưa làm");
                tvDiem1.setText("");
            }
            if(baiHoc.isDaLamCode()){
                String mucdo="";
                if(baiHoc.getMucDoCode().equals("co_ban")) mucdo="Cơ bản";
                else if(baiHoc.getMucDoCode().equals("trung_binh")) mucdo="Trung bình";
                else if(baiHoc.getMucDoCode().equals("nang_cao")) mucdo="Nâng cao";
                noidung2.setText("Code: "+baiHoc.getDiemCode() +" điểm ");
                tvDiem2.setText("Mức độ: "+mucdo);
            }
            else {
                noidung2.setText("Code: Chưa làm");
                tvDiem2.setText("");
            }
            if (baiHoc.getAnhBaiHoc() != null && !baiHoc.getAnhBaiHoc().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(ApiConfig.getFullUrl(ApiConfig.get_imagge_ENDPOINT + baiHoc.getAnhBaiHoc()))
                        .placeholder(R.drawable.user)
                        .error(R.drawable.user)
                        .into(imgBaiHoc);
            } else {
                imgBaiHoc.setImageResource(R.drawable.user);
            }
        }
    }
}