package labelingStudy.nctu.minuku.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import labelingStudy.nctu.minuku.model.DataRecord.BatteryDataRecord;

/**
 * Created by tingwei on 2018/9/10.
 */
@Dao
public interface BatteryDataRecordDAO {
    @Query("SELECT * FROM BatteryDataRecord")
    List<BatteryDataRecord> getAll();

    @Query("SELECT * FROM BatteryDataRecord WHERE creationTime BETWEEN :start AND :end")
    List<BatteryDataRecord> getRecordBetweenTimes(long start, long end);

    @Insert
    void insertAll(BatteryDataRecord batteryDataRecord);
}
