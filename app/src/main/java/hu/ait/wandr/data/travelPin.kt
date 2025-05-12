package hu.ait.wandr.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class TravelPin(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val latitude: Double,
    val longitude: Double,
    val note: String,
    var rating: Int,
    val photoUri: String?
)