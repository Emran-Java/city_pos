package acquire.database.repository;


import android.text.TextUtils;

import androidx.room.Entity;
import androidx.sqlite.db.SimpleSQLiteQuery;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import acquire.base.utils.DateUtils;
import acquire.base.utils.LoggerUtils;
import acquire.database.AcquireDatabase;
import acquire.database.bean.TransactionSummary;
import acquire.database.constant.DatabaseContent;
import acquire.database.dao.RecordDao;
import acquire.database.model.Record;
import acquire.sdk.emv.constant.EmvTransType;

/**
 * access the transaction record table according to business needs
 *
 * @author Janson
 * @date 2021/1/5 17:18
 */
public class RecordRepository {
    /**
     * Transaction record table. see {@link Record}'s {@link Entity}
     */
    private final static String TABLE_NAME = "T_RECORD";

    private final RecordDao recordDao;

    public RecordRepository() {
        recordDao = AcquireDatabase.getInstance().recordDao();
    }


    public boolean add(Record record) {
        if (record == null) {
            LoggerUtils.e("Add record failed: Record is null");
            return false;
        }
        if (record.getTraceNo() == null) {
            LoggerUtils.e("Add record failed: TraceNo is null");
            return false;
        }
        // determine if it is a duplicate trace
        if (findByTrace(record.getTraceNo()) != null) {
            LoggerUtils.e("Add record failed: Duplicate trace,trace is " + record.getTraceNo());
            return false;
        }

        String recentlyAddedRecordTranType = record.getTransType();
        if (PreStepVoidAbleRecordData.oldTransactionType != null && PreStepVoidAbleRecordData.oldTransactionType.equalsIgnoreCase("AuthComplete")
                && record.getTransType().equalsIgnoreCase("VoidSale")) {
            record.setTransType("VoidAuthComplete");
        }
        else if (PreStepVoidAbleRecordData.oldTransactionType != null && PreStepVoidAbleRecordData.oldTransactionType.equalsIgnoreCase("TipSale")
                && record.getTransType().equalsIgnoreCase("VoidSale")) {
            record.setTransType("VoidTipSale");
        }


        switch (record.getTransType()) {
            case DatabaseContent.TRANS_TIP_SALE: {
                record.setDisplayTitle(DatabaseContent.TRANS_DISPLAY_TIP_SALE);
                break;
            }
            case DatabaseContent.TRANS_VOID_TIP_ADJUST: {
                record.setDisplayTitle(DatabaseContent.TRANS_DISPLAY_VOID_TIP_ADJUST);
                break;
            }
            case DatabaseContent.TRANS_SALE: {
                record.setDisplayTitle(DatabaseContent.REPORT_DISPLAY_TEXT_SALE);
                break;
            }
            case DatabaseContent.TRANS_VOID_SALE: {
                record.setDisplayTitle(DatabaseContent.REPORT_DISPLAY_TEXT_VOID);
                break;
            }
            case DatabaseContent.TRANS_PRE_AUTH: {
                record.setDisplayTitle(DatabaseContent.REPORT_DISPLAY_TEXT_PRE_AUTH);
                break;
            }
            case DatabaseContent.TRANS_VOID_PRE_AUTH: {
                record.setDisplayTitle(DatabaseContent.REPORT_DISPLAY_TEXT_VOID_PRE_AUTH);
                break;
            }
            case DatabaseContent.TRANS_AUTH_COMPLETE: {
                record.setDisplayTitle(DatabaseContent.REPORT_DISPLAY_TEXT_PRE_AUTH_COMPLETE);
                break;
            }
            case DatabaseContent.TRANS_VOID_AUTH_COMPLETE: {
                record.setDisplayTitle(DatabaseContent.REPORT_DISPLAY_TEXT_VOID_PRE_AUTH_COMPLETE);
                break;
            }
            case DatabaseContent.TRANS_INSTALLMENT: {
                record.setDisplayTitle(DatabaseContent.REPORT_DISPLAY_TEXT_INSTALLMENT);
                break;
            }
            case DatabaseContent.TRANS_VOID_INSTALLMENT: {
                record.setDisplayTitle(DatabaseContent.REPORT_DISPLAY_TEXT_VOID_INSTALLMENT);
                break;
            }
        }

            //Date: 20260316: we delete Sale record after Void performing
            boolean rtnBool = recordDao.insert(record) > 0;

            //Take sale recorde delete attempt
            if (rtnBool) {
                try {
                    Record tmpSale = findByApproval(record.getAuthCode());
                    if (PreStepVoidAbleRecordData.oldTransactionType.equals("AuthComplete")
                            || PreStepVoidAbleRecordData.oldTransactionType.equals("PreAuth")
                            || PreStepVoidAbleRecordData.oldTransactionType.equals(DatabaseContent.TRANS_TIP_SALE)
                            || PreStepVoidAbleRecordData.oldTransactionType.equals(DatabaseContent.TRANS_INSTALLMENT)//PayFlex
                    ) {
                    /*if(record.getTransType().equalsIgnoreCase("VoidAuthComplete")){
                        record.setTransType("");
                    }*/
                        tmpSale = findByTrace(PreStepVoidAbleRecordData.oltTransactionInvId);
                    }


                    LoggerUtils.i("old search auth: " + tmpSale.getAuthCode() + ", " + tmpSale.getId() + ", Status: " + tmpSale.getStatus() + ", TraceNo: " + tmpSale.getTraceNo());
                    LoggerUtils.i("new TraceNo: " + record.getTraceNo());
                    if (tmpSale != null && (
                            recentlyAddedRecordTranType.equalsIgnoreCase("VoidSale")
                                    || recentlyAddedRecordTranType.equalsIgnoreCase("VoidPreAuth")
//                                    || recentlyAddedRecordTranType.equalsIgnoreCase(DatabaseContent.TRANS_VOID_TIP_ADJUST)
                                    || recentlyAddedRecordTranType.equalsIgnoreCase(DatabaseContent.TRANS_TIP_SALE)
                                    || recentlyAddedRecordTranType.equalsIgnoreCase("AuthComplete")
                                    || recentlyAddedRecordTranType.equalsIgnoreCase(DatabaseContent.TRANS_VOID_INSTALLMENT)

                    )
                    ) {
                        if (deleteById(tmpSale.getId())) {
                            LoggerUtils.i("Sale Record deleted success because Void performed: " + record.getTraceNo());
                        } else {
                            LoggerUtils.e("Sale Record not delete " + record.getTraceNo());
                        }
                    }
                    //clear contain
                    PreStepVoidAbleRecordData.oldTransactionType = null;
                    PreStepVoidAbleRecordData.oltTransactionInvId = null;
                } catch (Exception ex) {
                    LoggerUtils.e("Sale Record not delete, Excp: " + ex.getMessage());
                }
            }
            return rtnBool;
//        return recordDao.insert(record) > 0;
        }


        public boolean deleteById ( long id){
            int rtnInt = recordDao.deleteById(id);
            return rtnInt >= 0;
        }


        public boolean deleteByTrace (String traceNo){
            Record record = findByTrace(traceNo);
            if (record == null) {
                LoggerUtils.e("Delete record failed: No such record with trace[" + traceNo + "]");
                return true;
            }
            return deleteById(record.getId());
        }


        public boolean deleteAll () {
            return recordDao.deleteAll() >= 0;
        }


        public boolean deleteByMidTid (String mid, String tid){
            return recordDao.deleteAll(mid, tid) >= 0;
        }

        public boolean deleteBtacSettleByMidTid (String mid, String tid){
            return recordDao.deleteBtacSettleByMidTid(mid, tid) >= 0;
        }


        public Record findByTrace (String trace){
            if (trace == null) {
                return null;
            }
            List<Record> list = recordDao.findByTraceNo(trace);
            return !list.isEmpty() ? list.get(0) : null;
        }

        public Record findByApproval (String apvCode){
            if (apvCode == null) {
                return null;
            }
            List<Record> list = recordDao.findSaleByApprovalCode(apvCode);
            return !list.isEmpty() ? list.get(0) : null;
        }


        public Record findByReferNum (String referNum){
            if (referNum == null) {
                return null;
            }
            List<Record> list = recordDao.findByReferNum(referNum);
            return !list.isEmpty() ? list.get(0) : null;
        }


        public Record findByAuthCode (String transType, String authCode){
            if (authCode == null) {
                return null;
            }
            List<Record> records = recordDao.findByAuthCode(authCode);
            for (Record record : records) {
                if (record.getTransType().equals(transType)) {
                    return record;
                }
            }
            return null;
        }


        public Record findByIndex ( int index){
            List<Record> records = recordDao.findByRange(index, 1);
            if (records == null || records.isEmpty()) {
                return null;
            }
            return records.get(0);
        }


        public Record findByIndex (String mid, String tid,int index){
            if (mid == null || tid == null) {
                return null;
            }
            List<Record> records = recordDao.findByRange(mid, tid, index, 1);
            if (records == null || records.isEmpty()) {
                return null;
            }
            return records.get(0);
        }

        public Record findByIndexBracSettle (String mid, String tid,int index){
            if (mid == null || tid == null) {
                return null;
            }
            List<Record> records = recordDao.findByIndexBracSettle(mid, tid, index, 1);
            if (records == null || records.isEmpty()) {
                return null;
            }
            return records.get(0);
        }


        public Record findByOutOrderNo (String outOrderNo){
            if (outOrderNo == null) {
                return null;
            }
            List<Record> list = recordDao.findByOutOrderNo(outOrderNo);
            return !list.isEmpty() ? list.get(0) : null;
        }


        public List<Record> findByTransType (String oldTransType){
            return recordDao.findByTransType(oldTransType);
        }


        public Record findByQrOrder (String qrOrder){
            if (qrOrder == null) {
                return null;
            }
            List<Record> list = recordDao.findByOrder(qrOrder);
            return !list.isEmpty() ? list.get(0) : null;
        }


        public Record findLastBracTran () {
            List<Record> records = recordDao.findByRangeDescBtacTran(0, 1);
            if (records == null || records.isEmpty()) {
                return null;
            }
            return records.get(0);
        }

        public Record findLast () {
            List<Record> records = recordDao.findByRangeDesc(0, 1);
            if (records == null || records.isEmpty()) {
                return null;
            }
            return records.get(0);
        }

        public int getCount () {
            return recordDao.getCount();
        }

        public int getBracSettleCountEmi() {
            return recordDao.getBracSettleCountEmi();
        }

        public int getBracSettleCount () {
            return recordDao.getBracSettleCount();
        }


        public int getCountByMidTid (String mid, String tid){
            return recordDao.getCountByMidTid(mid, tid);
        }

        public int getCountByMidTidBracSettle (String mid, String tid){
            return recordDao.getCountByMidTidBracSettle(mid, tid);
        }


        public boolean update (Record record){
            if (record == null) {
                LoggerUtils.e("Update record failed: Params is null.");
                return false;
            }
            if (record.getId() <= 0) {
                Record tmp = findByTrace(record.getTraceNo());
                if (tmp == null) {
                    LoggerUtils.e("Update record failed: No such record with trace " + record.getTraceNo());
                    return false;
                }
                record.setId(tmp.getId());
            }
            return recordDao.update(record) >= 0;
        }


        public int updateIsOnUs () {
            return recordDao.updateIsOnUs();
        }

        public List<Record> findAll () {
            return recordDao.findAll();
        }

        public List<Record> findSchemeWiseOnOffUs () {
            return recordDao.findSchemeWiseOnOffUs();
        }

        public List<Record> findSchemeWiseOnOffUs2 () {
            return recordDao.findSchemeWiseOnOffUs2();
        }
        public List<Record> findSettleSchemeWiseOnOffUs2 () {
            return recordDao.findSettleSchemeWiseOnOffUs2();
        }

        public List<Record> findEmiSchemeWiseOnOffUs () {
            return recordDao.findEmiSchemeWiseOnOffUs();
        }

        public List<Record> findEmiSchemeWiseOnOffUs2 () {
            return recordDao.findEmiSchemeWiseOnOffUs2();
        }

        public List<Record> findAllPreAuth () {
            return recordDao.findAllPreAuth();
        }

        public List<Record> findAllVoidPreAuth () {
            return recordDao.findAllVoidPreAuth();
        }

        public List<Record> findAllPayFlex() {
            return recordDao.findEmiSchemeWiseOnOffUs();
        }

        public List<Record> findAllSaleAndVoid () {
            return recordDao.findAllSaleAndVoid();
        }

        public List<Record> findAllSalePreAuthAndVoid () {
            return recordDao.findAllSalePreAuthAndVoid();
        }

        public List<Record> findAllSalePreAuthComplete () {
            return recordDao.findAllSalePreAuthComplete();
        }

        public List<Record> findAllReport () {
            return recordDao.findAllReport();
        }

        public List<Record> findAllVoidReport () {
            return recordDao.findAllVoidReport();
        }

        public List<Record> findByMidTid (String mid, String tid){
            if (mid == null || tid == null) {
                return null;
            }
            return recordDao.findByMidTid(mid, tid);
        }

        public List<Record> findByAuthCode (String authCode){
            if (authCode == null) {
                return null;
            }
            return recordDao.findByAuthCode(authCode);
        }


        public int getCountByFilter (RecordRepository.RecordFilter filter){

            String where = getPageSqlWhere(filter);
            //sql:create
            String sql;
            if (TextUtils.isEmpty(where)) {
                sql = String.format(Locale.getDefault(), "select count(*) from %s", TABLE_NAME);
            } else {
                sql = String.format(Locale.getDefault(), "select count(*) from %s where %s", TABLE_NAME, where);
            }
            LoggerUtils.d("sql: " + sql);
            return recordDao.getCountBySql(new SimpleSQLiteQuery(sql));
        }

        public List<Record> findByPageDesc (RecordFilter filter,int pageNoCur, int pageSize){
            //sql: where
            String where = getPageSqlWhere(filter);
            //sql:create
            String sql;
            int firstIndex = pageNoCur * pageSize;
            if (TextUtils.isEmpty(where)) {
                sql = String.format(Locale.US, "select * from %s order by ID desc limit %d,%d", TABLE_NAME, firstIndex, pageSize);
            } else {
                sql = String.format(Locale.US, "select * from %s where %s order by ID desc limit %d,%d ", TABLE_NAME, where, firstIndex, pageSize);
            }
            LoggerUtils.d("sql: " + sql);
            return recordDao.findBySql(new SimpleSQLiteQuery(sql));
        }

        public List<TransactionSummary> getTransactionSummary (String mid, String tid){
            return recordDao.getTransactionSummary(mid, tid);
        }

        public List<TransactionSummary> getBracTransactionSummary (String mid, String tid){
            return recordDao.getBracTransactionSummary(mid, tid);
        }

        /**
         * Get paging SQL condition statement
         */
        private String getPageSqlWhere (RecordFilter filter){
            String and = " and ";
            StringBuilder builder = new StringBuilder();

            //sql: where
            if (filter != null) {
                if (filter.transStatuses != null && filter.transStatuses.length > 0) {
                    StringBuilder temp = new StringBuilder();
                    for (int i = 0; i < filter.transStatuses.length; i++) {
                        temp.append(filter.transStatuses[i]);
                        if (i != filter.transStatuses.length - 1) {
                            temp.append(",");
                        }
                    }
                    String format = String.format(Locale.getDefault(), "STATUS in (%s)", temp);
                    builder.append(format);
                    builder.append(and);
                }
                if (filter.transTypes != null && filter.transTypes.length > 0) {
                    StringBuilder temp = new StringBuilder();
                    for (int i = 0; i < filter.transTypes.length; i++) {
                        temp.append("'")
                                .append(filter.transTypes[i])
                                .append("'");
                        if (i != filter.transTypes.length - 1) {
                            temp.append(",");
                        }
                    }
                    String format = String.format(Locale.getDefault(), "TRANS_TYPE in (%s)", temp);
                    builder.append(format);
                    builder.append(and);
                }
                /*
                 *sql: start date and end date
                 */
                if (filter.from != null) {
                    String from = DateUtils.formatTime(filter.from, DateUtils.YYYYMMDDHHMMSS);
                    String format = String.format(Locale.getDefault(), "((DATE || TIME) >= '%s')", from);
                    builder.append(format);
                    builder.append(and);
                }
                if (filter.to != null) {
                    String to = DateUtils.formatTime(filter.to, DateUtils.YYYYMMDDHHMMSS);
                    String format = String.format(Locale.getDefault(), "((DATE || TIME) <= '%s')", to);
                    builder.append(format);
                    builder.append(and);
                }
            }

            String where = builder.toString();
            if (where.endsWith(and)) {
                where = where.substring(0, where.length() - and.length());
            }
            return where;
        }

        public static class RecordFilter {
            private int[] transStatuses;
            private String[] transTypes;
            private Date from;
            private Date to;

            public int[] getTransStatuses() {
                return transStatuses;
            }

            public void setTransStatuses(int[] transStatuses) {
                this.transStatuses = transStatuses;
            }


            public String[] getTransTypes() {
                return transTypes;
            }

            public void setTransTypes(String[] transTypes) {
                this.transTypes = transTypes;
            }

            public Date getFrom() {
                return from;
            }

            public void setFrom(Date from) {
                this.from = from;
            }

            public Date getTo() {
                return to;
            }

            public void setTo(Date to) {
                this.to = to;
            }
        }

    }
