package hu.ait.wandr.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TravelPinDao {
    @Query("SELECT * FROM TravelPin")
    fun getAllAsFlow(): Flow<List<TravelPin>>

    @Query("SELECT * FROM TravelPin ORDER BY rating DESC")
    fun getAllRankedAsFlow(): Flow<List<TravelPin>>

    @Query("SELECT * FROM TravelPin WHERE id = :id")
    suspend fun getById(id: Int): TravelPin?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(pin: TravelPin): Long

    @Update
    suspend fun update(pin: TravelPin)

    @Delete
    suspend fun delete(pin: TravelPin)

    @Query("SELECT * FROM TravelPin ORDER BY RANDOM() LIMIT 2")
    suspend fun getRandomPairForComparison(): List<TravelPin>

    @Query("DELETE FROM TravelPin")
    suspend fun deleteAllPins()
}