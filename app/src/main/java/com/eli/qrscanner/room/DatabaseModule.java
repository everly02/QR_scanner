package com.eli.qrscanner.room;

import android.content.Context;

import androidx.room.Room;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class DatabaseModule {
    @Provides
    @Singleton
    public static ScanRecordDatabase provideDatabase(@ApplicationContext Context context) {
        return Room.databaseBuilder(context, ScanRecordDatabase.class, "scan_record_db").build();
    }

    @Provides
    public static ScanRecordDao provideScanRecordDao(ScanRecordDatabase database) {
        return database.scanRecordDao();
    }
}
