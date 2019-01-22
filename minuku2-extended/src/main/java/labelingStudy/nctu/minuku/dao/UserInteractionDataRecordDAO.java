package labelingStudy.nctu.minuku.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import labelingStudy.nctu.minuku.model.DataRecord.UserInteractionDataRecord;

/**
 * Created by tingwei on 2018/9/10.
 */
@Dao
public interface UserInteractionDataRecordDAO {
    @Query("SELECT * FROM UserInteractionDataRecord")
    List<UserInteractionDataRecord> getAll();

    @Insert
    void insertAll(UserInteractionDataRecord userInteractionDataRecord);
}
