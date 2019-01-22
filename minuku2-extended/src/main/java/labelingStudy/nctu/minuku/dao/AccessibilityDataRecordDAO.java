package labelingStudy.nctu.minuku.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import labelingStudy.nctu.minuku.model.DataRecord.AccessibilityDataRecord;

@Dao
public interface AccessibilityDataRecordDAO {
    @Query("SELECT * FROM AccessibilityDataRecord")
    List<AccessibilityDataRecord> getAll();

    @Insert
    void insertAll(AccessibilityDataRecord accessibilityDataRecord);
}