package acquire.database.repository;


import java.util.List;

import acquire.base.utils.LoggerUtils;
import acquire.database.AcquireDatabase;
import acquire.database.dao.MerchantDao;
import acquire.database.model.Merchant;

/**
 *  access the merchant table according to business needs.
 *
 * @author Janson
 * @date 2021/1/5 17:17
 */
public class MerchantRepository {
    private final MerchantDao merchantDao;

    public MerchantRepository() {
        merchantDao = AcquireDatabase.getInstance().merchantDao();
    }

    
    public boolean add(Merchant merchant) {
        if (merchant == null) {
            LoggerUtils.e("Add Merchant Error：Merchant is null!");
            return false;
        }
        if (findByMidTid(merchant.getMid(),merchant.getTid()) != null) {
            LoggerUtils.e(" Redundant merchant[mid = "+merchant.getMid()+",tid = "+merchant.getTid()+"].");
            return false;
        }
        return merchantDao.insert(merchant) > 0;
    }

    public boolean addBracMerchants(Merchant merchant) {
        if (merchant == null) {
            LoggerUtils.e("Add Merchant Error：Merchant is null!");
            return false;
        }
        if (findByMidTidType(merchant.getMid(),merchant.getTid(), merchant.getType()) != null) {
            LoggerUtils.e(" Redundant merchant[mid = "+merchant.getMid()+",tid = "+merchant.getTid()+"].");
            return false;
        }
        return merchantDao.insert(merchant) > 0;
    }


    
    public List<Merchant> findAll() {
        return merchantDao.findAll();
    }

    public List<Merchant> findAllByMType() {
        return merchantDao.findAllByMType();
    }

    public List<Merchant> findDefaultMerchant() {
        return merchantDao.findAllByMType("DEFAULT");
    }

    public List<Merchant> findEmiMerchant() {
        return merchantDao.findAllByMType("EMI");
    }
    
    public Merchant findByMidTid(String mid, String tid) {
        if (mid == null) {
            LoggerUtils.e("Mid is null!");
            return null;
        }
        if (tid == null) {
            LoggerUtils.e("Tid is null!");
            return null;
        }
        List<Merchant> merchants = merchantDao.findByMidTid(mid,tid);
        return !merchants.isEmpty() ? merchants.get(0) : null;
    }

    public Merchant findByMidTidType(String mid, String tid, String type) {
        if (mid == null) {
            LoggerUtils.e("Mid is null!");
            return null;
        }
        if (tid == null) {
            LoggerUtils.e("Tid is null!");
            return null;
        }
        List<Merchant> merchants = merchantDao.findByMidTidType(mid,tid,type);
        return !merchants.isEmpty() ? merchants.get(0) : null;
    }

    
    public Merchant findByType(String type) {
        if (type == null) {
            LoggerUtils.e("type is null!");
            return null;
        }
        List<Merchant> merchants = merchantDao.findByType(type);
        return !merchants.isEmpty() ? merchants.get(0) : null;
    }

    
    public boolean deleteAll() {
        return merchantDao.deleteAll() >= 0;
    }

    
    public boolean deleteByMidTid(String mid, String tid) {
        Merchant merchant = findByMidTid(mid,tid);
        if (merchant == null) {
            LoggerUtils.e(" No such merchant[mid = "+mid+", tid = "+tid+"].");
            return true;
        }
        return merchantDao.delete(merchant.getId())>=0;
    }

    
    public boolean update(Merchant merchant) {
        if (merchant == null) {
            LoggerUtils.e("Merchant is null");
            return false;
        }
        if (merchant.getId() <= 0) {
            Merchant tmp = findByMidTid(merchant.getMid(),merchant.getTid());
            if (tmp == null) {
                LoggerUtils.e("Merchant is not exist[mid = "+merchant.getMid()+",tid = "+merchant.getTid()+"]." );
                return false;
            }
            merchant.setId(tmp.getId());
        }
        return merchantDao.update(merchant) >= 0;
    }

    
    public void clearHalt() {
        List<Merchant> merchants = merchantDao.findAll();
        for (Merchant merchant : merchants) {
            merchant.setSettleStep(0);
            merchantDao.update(merchant);
        }
    }

    
    public void clearHalt(List<Merchant> merchants) {
        for (Merchant merchant : merchants) {
            merchant.setSettleStep(0);
            merchantDao.update(merchant);
        }
    }
}
