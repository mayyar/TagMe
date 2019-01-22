package labelingStudy.nctu.minuku.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import labelingStudy.nctu.minuku.model.DataRecord.ActivityRecognitionDataRecord;

/**
 * Created by tingwei on 2018/9/10.
 */
@Dao
public interface ActivityRecognitionDataRecordDAO {
    @Query("SELECT * FROM ActivityRecognitionDataRecord")
    List<ActivityRecognitionDataRecord> getAll();

    @Insert
    void insertAll(ActivityRecognitionDataRecord activityRecognitionDataRecord);
}
