package com.example.nckh.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nckh.ApiConfig;
import com.example.nckh.R;
import com.example.nckh.model.BaiHoc;
import com.squareup.picasso.Picasso;

import java.util.List;

public class BaiHocAdapterNgang extends RecyclerView.Adapter<BaiHocAdapterNgang.BaiHocViewHolder> {

    private List<BaiHoc> baiHocList;

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
        if(baiHoc.isDaLamQuiz()){
            String mucdo="";
            if(baiHoc.getMucDoQuiz().equals("co_ban")) mucdo="Cơ bản";
            else if(baiHoc.getMucDoQuiz().equals("trung_binh")) mucdo="Trung bình";
            else if(baiHoc.getMucDoQuiz().equals("nang_cao")) mucdo="Nâng cao";
            holder.noidung1.setText("Trắc nghiệm: "+baiHoc.getDiemQuiz() +" điểm ");
            holder.tvDiem1.setText("Mức độ: "+mucdo);
        }
        else {
            holder.noidung1.setText("Trắc nghiệm: Chưa làm");
            holder.tvDiem1.setText("");
        }
        if(baiHoc.isDaLamCode()){

            String mucdo="";
            if(baiHoc.getMucDoCode().equals("co_ban")) mucdo="Cơ bản";
            else if(baiHoc.getMucDoCode().equals("trung_binh")) mucdo="Trung bình";
            else if(baiHoc.getMucDoCode().equals("nang_cao")) mucdo="Nâng cao";
            holder.noidung2.setText("Code: "+baiHoc.getDiemCode() +" điểm ");
            holder.tvDiem2.setText("Mức độ: "+mucdo);
        }
        else {
            holder.noidung2.setText("Code: Chưa làm");
            holder.tvDiem2.setText("");
        }
        if (baiHoc.getAnhBaiHoc() != null && !baiHoc.getAnhBaiHoc().isEmpty()) {
            Picasso.get()
                    .load(ApiConfig.get_imagge_ENDPOINT + baiHoc.getAnhBaiHoc())
                    .placeholder(R.drawable.user)
                    .error(R.drawable.user)
                    .into(holder.imgBaiHoc);
        } else {
            holder.imgBaiHoc.setImageResource(R.drawable.user);
        }
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
    }
}