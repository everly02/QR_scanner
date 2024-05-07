package com.eli.qrscanner.room;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Database(entities = {ScanRecord.class}, version = 1,exportSchema = false)
public abstract class ScanRecordDatabase extends RoomDatabase {
    public abstract ScanRecordDao scanRecordDao();
}
