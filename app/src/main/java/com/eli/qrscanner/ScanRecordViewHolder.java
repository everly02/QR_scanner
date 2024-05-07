package com.eli.qrscanner;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ScanRecordViewHolder extends RecyclerView.ViewHolder {

    public TextView tvValue, tvCategory;
    public ImageView ivFavorite;

    public ScanRecordViewHolder(@NonNull View itemView) {
        super(itemView);
        tvValue = itemView.findViewById(R.id.tv_value);
        tvCategory = itemView.findViewById(R.id.tv_category);
        ivFavorite = itemView.findViewById(R.id.iv_favorite);
    }
}
