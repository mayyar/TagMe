package labelingStudy.nctu.minuku.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import labelingStudy.nctu.minuku.model.DataRecord.TelephonyDataRecord;

/**
 * Created by tingwei on 2018/9/10.
 */
@Dao
public interface TelephonyDataRecordDAO {
    @Query("SELECT * FROM TelephonyDataRecord")
    List<TelephonyDataRecord> getAll();

    @Query("SELECT * FROM TelephonyDataRecord WHERE creationTime BETWEEN :start AND :end")
    List<TelephonyDataRecord> getRecordBetweenTimes(long start, long end);

    @Insert
    void insertAll(TelephonyDataRecord telephonyDataRecord);
}
