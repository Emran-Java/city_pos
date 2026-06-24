package acquire.database.repository;

import java.util.List;

import acquire.base.utils.LoggerUtils;
import acquire.database.AcquireDatabase;
import acquire.database.dao.ReversalDataDao;
import acquire.database.model.ReversalData;

/**
 * access the reversal table according to business needs
 *
 * @author Janson
 * @date 2021/1/5 17:15
 */
public class ReversalDataRepository {
    private final ReversalDataDao mReversalRecordDao;

    public ReversalDataRepository() {
        mReversalRecordDao = AcquireDatabase.getInstance().reverseRecordDao();
    }

    
    public ReversalData getReverseRecord() {
        List<ReversalData> reversalData = mReversalRecordDao.findAll();
        if (reversalData == null || reversalData.isEmpty()){
            return null;
        }
        //there should be only one reversal data in the reversal table
        return reversalData.get(0);
    }

    
    public boolean add(ReversalData reversalData)  {
        if (getReverseRecord() != null) {
            LoggerUtils.e("Reversal data already exists, cannot add");
            return false;
        }
        return mReversalRecordDao.insert(reversalData) > 0;
    }

    
    public boolean deleteAllReversalData() {
        return mReversalRecordDao.deleteAll() >= 0;
    }

    
    public boolean updateField55(String field55) {
        ReversalData rw = getReverseRecord() ;
        if (rw == null) {
            LoggerUtils.e("Update Field55: No reversal data");
            return true;
        }
        rw.setField55(field55);
        return mReversalRecordDao.update(rw) >= 0;
    }


}
