package labelingStudy.nctu.minuku.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import labelingStudy.nctu.minuku.model.DataRecord.AppUsageDataRecord;

/**
 * Created by tingwei on 2018/9/10.
 */
@Dao
public interface AppUsageDataRecordDAO {
    @Query("SELECT * FROM AppUsageDataRecord")
    List<AppUsageDataRecord> getAll();

    @Insert
    void insertAll(AppUsageDataRecord appUsageDataRecord);
}
