package acquire.core.report_data_factiry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import acquire.core.constant.CoreContent;
import acquire.core.constant.TransType;
import acquire.core.model.GroupByTranType;
import acquire.core.model.SchemeGroup;
import acquire.database.model.Record;
import acquire.database.repository.RecordRepository;

public class CardDataFactory {

    public static List<SchemeGroup> getSettleEmiTypeWizeTotal(){
        RecordRepository recordRepository = new RecordRepository();
        List<SchemeGroup> schemeGroups = new ArrayList<>();
        schemeGroups.add(getGrandTotal(recordRepository.findEmiSchemeWiseOnOffUs()));
        return schemeGroups;
    }

    public static List<SchemeGroup> getSettleTypeWiseTotal(){
        RecordRepository recordRepository = new RecordRepository();
        List<SchemeGroup> schemeGroups = new ArrayList<>();
        schemeGroups.add(getGrandTotal(recordRepository.findSchemeWiseOnOffUs()));
        return schemeGroups;
    }

    public static List<SchemeGroup> getEmiSchemeGroups() {
        RecordRepository recordRepository = new RecordRepository();
        return getSchemeGroups(recordRepository.findEmiSchemeWiseOnOffUs2());
    }
    public static List<SchemeGroup> getSettleSchemeGroups() {
        RecordRepository recordRepository = new RecordRepository();
        return getSchemeGroups(recordRepository.findSettleSchemeWiseOnOffUs2());
    }

    public static List<SchemeGroup> getSchemeGroups() {
        RecordRepository recordRepository = new RecordRepository();
        return getSchemeGroups(recordRepository.findSchemeWiseOnOffUs2());
    }

    public static List<SchemeGroup> getSchemeAllVoidGroups() {
        RecordRepository recordRepository = new RecordRepository();
        return getSchemeGroups(recordRepository.findAllVoidReport());
    }

    public static List<SchemeGroup> getSchemeGroups(List<Record> allRecords) {
       /* RecordRepository recordRepository = new RecordRepository();

        List<Record> allRecords = recordRepository.findSchemeWiseOnOffUs();
        */
        List<SchemeGroup> schemeGroups = new ArrayList<>();
        Map<String, List<Record>> recordGroupedByScheme = new HashMap<>();

        for (Record record : allRecords) {
            String suffix = record.isOnUs() ? " " + CoreContent.REPORT_DISPLAY_ONUS : " " + CoreContent.REPORT_DISPLAY_OFFUS;
            String schemeTitle = (record.getCardScheme() != null ? record.getCardScheme() : CoreContent.REPORT_DISPLAY_UNKNOWN) + suffix;

            if (!recordGroupedByScheme.containsKey(schemeTitle)) {
                recordGroupedByScheme.put(schemeTitle, new ArrayList<>());
            }
            Objects.requireNonNull(recordGroupedByScheme.get(schemeTitle)).add(record);
        }


        for (Map.Entry<String, List<Record>> entry : recordGroupedByScheme.entrySet()) {
            SchemeGroup group = new SchemeGroup();
            group.setSchemeTitle(entry.getKey());

            List<Record> recordsUnderScheme = entry.getValue();
            List<GroupByTranType> groupByTranTypeList = new ArrayList<>();

            Map<String, List<Record>> typeMap = new HashMap<>();
            for (Record r : recordsUnderScheme) {
                if (!typeMap.containsKey(r.getTransType())) {
                    typeMap.put(r.getTransType(), new ArrayList<>());
                }
                Objects.requireNonNull(typeMap.get(r.getTransType())).add(r);
            }

            long totalSchemeAmount = 0;
            long totalVoidSchemeAmount = 0;
            int totalSchemeCount = 0;
            int totalVoidSchemeCount = 0;


            for (Map.Entry<String, List<Record>> typeEntry : typeMap.entrySet()) {
                List<Record> typeRecords = typeEntry.getValue();
                Record firstRecord = typeRecords.get(0);

                GroupByTranType gType = new GroupByTranType();
                gType.setTranType(typeEntry.getKey());
                gType.setTitle(firstRecord.getDisplayTitle()); // table - displayTitle
                gType.setCount(typeRecords.size());

                long sumAmount = 0;
                long sumVoidAmount = 0;
                int sumVoidCount = 0;
                long sumTip = 0;

                for (Record record : typeRecords) {

                    long voidAmount = (record.getAmount() + record.getTipAmount()); // amount + tipAmount
                    sumAmount += (record.getAmount() );//+ record.getTipAmount()); // amount + tipAmount
                    sumTip += record.getTipAmount();

                    if (record.getTransType() != null && record.getTransType().toLowerCase().contains("void")) {
                        sumVoidAmount += voidAmount;
                        ++sumVoidCount;
                    }
                }

                gType.setAmount(sumAmount);
                gType.setTipAmount(sumTip);

                String tType = typeEntry.getKey();
                boolean isSpec = tType.equals(TransType.TRANS_SALE) || tType.equals(TransType.TRANS_AUTH_COMPLETE) || tType.equals(TransType.TRANS_PRE_AUTH) || tType.equals(TransType.TRANS_TIP_SALE);

                gType.setCalculate(isSpec);
                gType.setShowTip(isSpec);
                gType.setCountable(true); // default true
                gType.setSubtract(false); // default false

                groupByTranTypeList.add(gType);

                totalSchemeAmount += sumAmount;
                totalVoidSchemeAmount += sumVoidAmount;
                totalSchemeCount += typeRecords.size();
                totalVoidSchemeCount += sumVoidCount;

            }

            group.setGroupByTranType(groupByTranTypeList);
            group.setTotalSchemeWiseTranCount((totalSchemeCount - totalVoidSchemeCount));
            group.setTotalSchemeWiseTranAmount(totalSchemeAmount - totalVoidSchemeAmount);

            schemeGroups.add(group);
        }
        schemeGroups.add(getGrandTotal(allRecords));
        return schemeGroups;
    }

    public static SchemeGroup getGrandTotal(List<Record> recordList) {
        //RecordRepository recordRepository = new RecordRepository();
        List<GroupByTranType> groupedTranTypeList = getGroupedTranTypeList(recordList);
        List<SchemeGroup> rtnData = new ArrayList<>();

        SchemeGroup schemeGroup = new SchemeGroup();

        long grandTotalAmount = 0;
        int grandTotalCount = 0;
        long grandSumTip = 0;

        for(GroupByTranType groupByTranType : groupedTranTypeList){
            if(groupByTranType.getTranType()!=null && !groupByTranType.getTranType().toLowerCase().contains("void")){
                grandTotalAmount+=groupByTranType.getAmount();
                grandSumTip+=groupByTranType.getTipAmount();
                grandTotalCount+=groupByTranType.getCount();
            }
        }

        schemeGroup.setSchemeTitle("Grand Total");
        schemeGroup.setTotalSchemeWiseTranAmount(grandTotalAmount);
        schemeGroup.setTotalSchemeWiseTranCount(grandTotalCount);
        schemeGroup.setTotalSchemeWiseTranTipAmount(grandSumTip);
        schemeGroup.setGroupByTranType(groupedTranTypeList);
        rtnData.add(schemeGroup);
        return schemeGroup;
    }

    // Convert List<Record> -> List<GroupByTranType>
    private static  List<GroupByTranType> getGroupedTranTypeList(List<Record> recordList) {
      //  RecordRepository recordDao = new RecordRepository();

//        List<Record> recordList = recordDao.findSchemeWiseOnOffUs();
        List<GroupByTranType> result = new ArrayList<>();

        if (recordList == null || recordList.isEmpty()) {
            return result;
        }

        Map<String, GroupByTranType> map = new LinkedHashMap<>();

        for (Record record : recordList) {
            if (record == null) {
                continue;
            }

            String tranType = record.getTransType();
            if (tranType == null) {
                tranType = "";
            }

            GroupByTranType group = map.get(tranType);

            if (group == null) {
                group = new GroupByTranType();

                group.setTranType(tranType);

                String title = record.getDisplayTitle();
                group.setTitle(title != null ? title : "");

                group.setCount(0);
                group.setAmount(0L);
                group.setTipAmount(0L);

                // Optional flags
                group.setSubtract(false);
                group.setCountable(true);
                group.setShowTip(true);
                group.setCalculate(true);

                map.put(tranType, group);
            }

            // Count
            group.setCount(group.getCount() + 1);

            // Sum Amount
            group.setAmount(group.getAmount() + record.getAmount());

            // Sum Tip Amount
            long oldTip = 0;
            try {
                //long tip = group.getTipAmount();
                oldTip = group.getTipAmount();
                /*if (tip != null && !tip.isEmpty()) {
                    oldTip = Long.parseLong(tip);
                }*/
            } catch (Exception ignored) {
            }

            long totalTip = oldTip + record.getTipAmount();
            group.setTipAmount(totalTip);
        }

        result.addAll(map.values());

        return result;
    }

    /*
    public static List<SchemeGroup> getGrandTotal(List<SchemeGroup> schemeGroups) {
        List<SchemeGroup> data = new ArrayList<>();

        SchemeGroup schemeGroupObj = new SchemeGroup();
        schemeGroupObj.setSchemeTitle("Grand Total:");
        Map<String, List<GroupByTranType>> stringListMap = new HashMap<>();

        for (SchemeGroup r : schemeGroups) {
            for (GroupByTranType groupByTranType : r.getGroupByTranType()) {
                stringListMap.put(groupByTranType.getTitle(), new ArrayList<>());
            }
        }

        for (SchemeGroup r : schemeGroups) {
            for (GroupByTranType groupByTranType : r.getGroupByTranType()) {
                stringListMap.get(groupByTranType.getTitle()).addAll(r.getGroupByTranType());
            }
        }

        List<GroupByTranType> groupByTranTypes = new ArrayList<>();
        for (List<GroupByTranType> r : stringListMap) {

            for (GroupByTranType groupByTranType : r.getGroupByTranType()) {

                List<GroupByTranType> groupByTranTypes3 = stringListMap.get(groupByTranType.getTitle());
                long amount = 0;
                int count = groupByTranTypes3.size();

                for (GroupByTranType groupByTranType1 : groupByTranTypes3) {
                    amount += groupByTranType1.getAmount();
                }
                GroupByTranType groupByTranType1 = new GroupByTranType();
                groupByTranType1.setAmount(amount);
                groupByTranType1.setCount(count);
                groupByTranType1.setTranType(groupByTranType.getTranType());
                groupByTranType1.setTitle(groupByTranType.getTitle());
                groupByTranTypes.add(groupByTranType1);
            }
        }

                */
/*stringListMap.put(groupByTranType.getTitle(), new ArrayList<>());

                if (!typeMap.containsKey(r.getTransType())) {
                    typeMap.put(r.getTransType(), new ArrayList<>());
                }
                Objects.requireNonNull(typeMap.get(r.getTransType())).add(r);

            //}
            if (!typeMap.containsKey(r.getTransType())) {
                typeMap.put(r.getTransType(), new ArrayList<>());
            }*//*

        //Objects.requireNonNull(typeMap.get(r.getTransType())).add(r);


        for (SchemeGroup schemeGroup : schemeGroups) {

        }

        return null;
    }
*/

}
