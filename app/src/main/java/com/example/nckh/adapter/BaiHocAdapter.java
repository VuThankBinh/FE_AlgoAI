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

public class BaiHocAdapter extends RecyclerView.Adapter<BaiHocAdapter.BaiHocViewHolder> {

    private List<BaiHoc> baiHocList;

    public BaiHocAdapter(List<BaiHoc> baiHocList) {
        this.baiHocList = baiHocList;
    }

    @NonNull
    @Override
    public BaiHocViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_baihoc, parent, false);
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
        TextView tvTieuDe, tvTrangThai;
        LinearLayout allinner_baihocgannhat;

        public BaiHocViewHolder(@NonNull View itemView) {
            super(itemView);
            imgBaiHoc = itemView.findViewById(R.id.img_baihoc);
            tvTieuDe = itemView.findViewById(R.id.titlegannhat);
            tvTrangThai = itemView.findViewById(R.id.tinhtrangbaigannhat);
            allinner_baihocgannhat = itemView.findViewById(R.id.allinner_baihocgannhat);
        }
    }
} 