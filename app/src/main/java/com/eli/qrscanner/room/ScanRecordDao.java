package com.eli.qrscanner.room;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ScanRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(ScanRecord scanRecord);

    @Query("SELECT * FROM scan_records")
    List<ScanRecord> getAll();

    @Query("SELECT * FROM scan_records WHERE id = :id")
    ScanRecord getById(int id);

    @Query("SELECT * FROM scan_records WHERE category = :category")
    List<ScanRecord> getByCategory(String category);

    @Query("UPDATE scan_records SET isFavorite = :isFavorite WHERE id = :id")
    void updateFavorite(int id, boolean isFavorite);

    @Update
    void update(ScanRecord scanRecord);
}