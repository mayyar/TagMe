package labelingStudy.nctu.minuku.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import labelingStudy.nctu.minuku.model.DataRecord.TransportationModeDataRecord;

/**
 * Created by tingwei on 2018/9/10.
 */
@Dao
public interface TransportationModeDataRecordDAO {
    @Query("SELECT * FROM TransportationModeDataRecord")
    List<TransportationModeDataRecord> getAll();

    @Query("SELECT * FROM TransportationModeDataRecord WHERE creationTime BETWEEN :start AND :end")
    List<TransportationModeDataRecord> getRecordBetweenTimes(long start, long end);

    @Insert
    void insertAll(TransportationModeDataRecord transportationModeDataRecord);
}
