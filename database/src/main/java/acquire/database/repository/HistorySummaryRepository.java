package acquire.database.repository;

import java.util.ArrayList;
import java.util.List;

import acquire.database.AcquireDatabase;
import acquire.database.bean.TransactionSummary;
import acquire.database.dao.HistoryDao;
import acquire.database.model.HistorySummary;
import acquire.database.model.Merchant;

/**
 * access the history record table according to business needs
 *
 * @author Janson
 * @date 2023/12/12 16:33
 */
public class HistorySummaryRepository {
    private final HistoryDao historyDao;

    public HistorySummaryRepository() {
        historyDao= AcquireDatabase.getInstance().historyDao();
    }


    public void addTransactionSummaries(Merchant merchant,List<TransactionSummary> transactionSummaries) {
        if (transactionSummaries == null || transactionSummaries.isEmpty()){
            HistorySummary historySummary = new HistorySummary();
            historySummary.setMid(merchant.getMid());
            historySummary.setTid(merchant.getTid());
            historySummary.setBatchNo(merchant.getBatchNo());
            historySummary.setSettleDate(merchant.getSettleDate());
            historySummary.setSettleTime(merchant.getSettleTime());
            historySummary.setSettleEqual(merchant.isSettleEqual());
            historySummary.setAmount(0);
            historySummary.setCount(0);
            historySummary.setTxnType(null);
            historyDao.insert(historySummary);
            return;
        }
        for (TransactionSummary transactionSummary : transactionSummaries) {
            HistorySummary historySummary = new HistorySummary();
            historySummary.setMid(merchant.getMid());
            historySummary.setTid(merchant.getTid());
            historySummary.setBatchNo(merchant.getBatchNo());
            historySummary.setSettleDate(merchant.getSettleDate());
            historySummary.setSettleTime(merchant.getSettleTime());
            historySummary.setSettleEqual(merchant.isSettleEqual());
            historySummary.setAmount(transactionSummary.getAmount());
            historySummary.setCount(transactionSummary.getCount());
            historySummary.setTxnType(transactionSummary.getTransType());
            historyDao.insert(historySummary);
        }
    }



    public List<TransactionSummary> getTransactionSummaries(Merchant merchant){
        List<HistorySummary> historySummaries = historyDao.getHistorySummary(merchant.getMid(),merchant.getTid());
        if (historySummaries == null){
            return new ArrayList<>();
        }
        List<TransactionSummary> summaries = new ArrayList<>();
        for (HistorySummary historySummary : historySummaries) {
            TransactionSummary transactionSummary = new TransactionSummary();
            transactionSummary.setTransType(historySummary.getTxnType());
            transactionSummary.setAmount(historySummary.getAmount());
            transactionSummary.setCount(historySummary.getCount());
            summaries.add(transactionSummary);
        }
        return summaries;
    }
    public String getSettleDate(Merchant merchant){
        List<HistorySummary> historySummaries = historyDao.getHistorySummary(merchant.getMid(),merchant.getTid());
        if (historySummaries == null){
            return null;
        }
        return historySummaries.get(0).getSettleDate();
    }
    public String getSettleTime(Merchant merchant){
        List<HistorySummary> historySummaries = historyDao.getHistorySummary(merchant.getMid(),merchant.getTid());
        if (historySummaries == null){
            return null;
        }
        return historySummaries.get(0).getSettleTime();
    }
    public boolean isSettleEqual(Merchant merchant){
        List<HistorySummary> historySummaries = historyDao.getHistorySummary(merchant.getMid(),merchant.getTid());
        if (historySummaries == null){
            return true;
        }
        return historySummaries.get(0).isSettleEqual();
    }
    public void clear(Merchant merchant){
        historyDao.delete(merchant.getMid(),merchant.getTid());
    }

    public int getCount() {
        return historyDao.getCount();
    }


}
