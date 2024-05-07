package com.eli.qrscanner;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.eli.qrscanner.room.ScanRecord;

import java.util.ArrayList;
import java.util.List;

public class ScanRecordAdapter extends RecyclerView.Adapter<ScanRecordAdapter.ScanRecordViewHolder> {

    private List<ScanRecord> scanRecords = new ArrayList<>();

    @NonNull
    @Override
    public ScanRecordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_scan_record, parent, false);
        return new ScanRecordViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ScanRecordViewHolder holder, int position) {
        ScanRecord record = scanRecords.get(position);
        holder.tvValue.setText(record.getValue());
        holder.tvCategory.setText(record.getCategory());
        holder.ivFavorite.setImageResource(
                record.isFavorite() ? android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off);
    }

    @Override
    public int getItemCount() {
        return scanRecords.size();
    }

    public void setScanRecords(List<ScanRecord> scanRecords) {
        this.scanRecords = scanRecords;
        notifyDataSetChanged();
    }

    public static class ScanRecordViewHolder extends RecyclerView.ViewHolder {

        public TextView tvValue, tvCategory;
        public ImageView ivFavorite;

        public ScanRecordViewHolder(@NonNull View itemView) {
            super(itemView);
            tvValue = itemView.findViewById(R.id.tv_value);
            tvCategory = itemView.findViewById(R.id.tv_category);
            ivFavorite = itemView.findViewById(R.id.iv_favorite);
        }
    }
}
