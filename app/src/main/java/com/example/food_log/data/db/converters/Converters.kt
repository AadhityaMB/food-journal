package com.example.food_log.data.db.converters

import androidx.room.TypeConverter
import com.example.food_log.data.model.enums.DiningMode
import com.example.food_log.data.model.enums.Platform
import com.example.food_log.data.model.enums.PhotoType
import com.example.food_log.data.model.enums.WouldOrderAgain

class Converters {

    // --- DiningMode ---
    @TypeConverter
    fun fromDiningMode(value: DiningMode?): String? = value?.name

    @TypeConverter
    fun toDiningMode(value: String?): DiningMode? =
        value?.let { DiningMode.valueOf(it) }

    // --- Platform ---
    @TypeConverter
    fun fromPlatform(value: Platform?): String? = value?.name

    @TypeConverter
    fun toPlatform(value: String?): Platform? =
        value?.let { Platform.valueOf(it) }

    // --- WouldOrderAgain ---
    @TypeConverter
    fun fromWouldOrderAgain(value: WouldOrderAgain?): String? = value?.name

    @TypeConverter
    fun toWouldOrderAgain(value: String?): WouldOrderAgain? =
        value?.let { WouldOrderAgain.valueOf(it) }

    // --- PhotoType ---
    @TypeConverter
    fun fromPhotoType(value: PhotoType?): String? = value?.name

    @TypeConverter
    fun toPhotoType(value: String?): PhotoType? =
        value?.let { PhotoType.valueOf(it) }
}
