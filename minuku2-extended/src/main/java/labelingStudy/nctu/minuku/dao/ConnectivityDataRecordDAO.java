package labelingStudy.nctu.minuku.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import labelingStudy.nctu.minuku.model.DataRecord.ConnectivityDataRecord;

/**
 * Created by tingwei on 2018/9/10.
 */
@Dao
public interface ConnectivityDataRecordDAO {
    @Query("SELECT * FROM ConnectivityDataRecord")
    List<ConnectivityDataRecord> getAll();

    @Query("SELECT * FROM ConnectivityDataRecord WHERE creationTime BETWEEN :start AND :end")
    List<ConnectivityDataRecord> getRecordBetweenTimes(long start, long end);

    @Insert
    void insertAll(ConnectivityDataRecord connectivityDataRecord);
}
