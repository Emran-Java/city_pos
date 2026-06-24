package acquire.core.report_data_factiry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import acquire.base.utils.LoggerUtils;
import acquire.core.constant.OnUsBinMap;
import acquire.core.model.CardSchemeReportModel;
import acquire.database.model.Record;

public class ReportDataFactory {

    private static ReportDataFactory reportDataFactory;

    public static ReportDataFactory getInstance(){
        if(reportDataFactory==null) reportDataFactory = new ReportDataFactory();
        return reportDataFactory;
    }

    private ReportDataFactory(){}

    public List<CardSchemeReportModel> generatePreAuthSchemeReport(List<Record> records) {

        Map<String, CardSchemeReportModel> map = new HashMap<>();

        String grandTotalTitle = "GRAND TOTAL";
        long grandTotalSaleAmount = 0;
        long grandTotalVoidSaleAmount = 0;
        int grandTotalSaleCount = 0;
        int grandTotalVoidSaleCount = 0;
        int grandTotalCount = 0;
        long grandTotalAmount = 0;

        for (Record record : records) {
            String scheme = record.getCardScheme();
            boolean isOnUs = false;
            try {
                String ben = record.getCardNo().substring(0, 6);
                String cardTitle = OnUsBinMap.REPORT_CARD_ONUS_MAP.get(ben).getCardTitle();
                if (cardTitle != null && !cardTitle.isEmpty()) {
                    isOnUs = true;
                }
            } catch (Exception ex) {

            }

            if (isOnUs) scheme = scheme + " ONUS";
            else scheme = scheme + " OFFUS";

            if (scheme != null) {
                if (!map.containsKey(scheme)) {
                    CardSchemeReportModel m = new CardSchemeReportModel();
                    m.setScheme(scheme);
                    map.put(scheme, m);
                }
                CardSchemeReportModel model = map.get(scheme);

                if(model!=null) {
                    model.setTransType(record.getTransType());
                    if ("PreAuth".equalsIgnoreCase(record.getTransType())) {
                        ++grandTotalSaleCount;
                        ++grandTotalCount;
                        model.setTranSaleTitle("PRE-AUTH");
                        model.setSaleCount(model.getSaleCount() + 1);
                        grandTotalSaleAmount += record.getAmount();
                        model.setSaleAmount(model.getSaleAmount() + record.getAmount());
                    }
                    if ("VoidPreAuth".equalsIgnoreCase(record.getTransType())) {
                        ++grandTotalVoidSaleCount;
                        ++grandTotalCount;
                        model.setTranVoidTitle("VOID");
                        grandTotalVoidSaleAmount += record.getAmount();
                        model.setVoidCount(model.getVoidCount() + 1);
                        model.setVoidAmount(model.getVoidAmount() + record.getAmount());
                    }
                }
                else{
                    LoggerUtils.i("newCall CardSchemeReportModel is null in generatePreAuthSchemeReport() function");
                }
            }
        }

        CardSchemeReportModel grandTotalObject = new CardSchemeReportModel();
        grandTotalObject.setScheme(grandTotalTitle);
        grandTotalObject.setTranSaleTitle("PRE-AUTH");
        grandTotalObject.setTranVoidTitle("VOID");

        grandTotalObject.setSaleCount(grandTotalSaleCount);
        grandTotalObject.setSaleAmount(grandTotalSaleAmount);

        grandTotalObject.setVoidCount(grandTotalVoidSaleCount);
        grandTotalObject.setVoidAmount(grandTotalVoidSaleAmount);

        //grandTotalAmount = grandTotalSaleAmount - grandTotalVoidSaleAmount;
        grandTotalObject.setVoidAmount(grandTotalVoidSaleAmount);
        map.put(grandTotalTitle, grandTotalObject);

        return new ArrayList<>(map.values());
    }

    public List<CardSchemeReportModel> generateSchemeReportWithPreAuthComp(List<Record> records) {

        Map<String, CardSchemeReportModel> map = new HashMap<>();

        String grandTotalTitle = "GRAND TOTAL";
        long grandTotalSaleAmount = 0;
        long grandTotalVoidSaleAmount = 0;
        int grandTotalSaleCount = 0;
        int grandTotalVoidSaleCount = 0;
        int grandTotalCount = 0;
        long grandTotalAmount = 0;

        for (Record record : records) {
            String scheme = record.getCardScheme();
            boolean isOnUs = false;
            try {
                String ben = record.getCardNo().substring(0, 6);
                String cardTitle = OnUsBinMap.REPORT_CARD_ONUS_MAP.get(ben).getCardTitle();
                if (cardTitle != null && !cardTitle.isEmpty()) {
                    isOnUs = true;
                }
            } catch (Exception ex) {

            }

            if (isOnUs) scheme = scheme + " ONUS";
            else scheme = scheme + " OFFUS";
            if (scheme != null) {
                if (!map.containsKey(scheme)) {
                    CardSchemeReportModel m = new CardSchemeReportModel();
                    m.setScheme(scheme);
                    map.put(scheme, m);
                }
                CardSchemeReportModel model = map.get(scheme);

                String saleOrPreAuthTitle = "SALE";

                if("AuthComplete".equalsIgnoreCase(record.getTransType())){
                    saleOrPreAuthTitle = "PRE-AUTH";
                }

                if ("SALE".equalsIgnoreCase(record.getTransType()) || "AuthComplete".equalsIgnoreCase(record.getTransType())) {
                    ++grandTotalSaleCount;
                    ++grandTotalCount;
                    model.setTranSaleTitle(saleOrPreAuthTitle);
                    model.setSaleCount(model.getSaleCount() + 1);
                    grandTotalSaleAmount += record.getAmount();
                    model.setSaleAmount(model.getSaleAmount() + record.getAmount());
                }


                if ("VoidSale".equalsIgnoreCase(record.getTransType())
                        || "VOID SALE".equalsIgnoreCase(record.getTransType())) {
                    ++grandTotalVoidSaleCount;
                    ++grandTotalCount;
                    model.setTranVoidTitle("VOID");
                    grandTotalVoidSaleAmount += record.getAmount();
                    model.setVoidCount(model.getVoidCount() + 1);
                    model.setVoidAmount(model.getVoidAmount() + record.getAmount());
                }
            }
        }

        CardSchemeReportModel grandTotalObject = new CardSchemeReportModel();
        grandTotalObject.setScheme(grandTotalTitle);
        grandTotalObject.setTranSaleTitle("SALE");
        grandTotalObject.setTranVoidTitle("VOID");


        grandTotalObject.setSaleCount(grandTotalSaleCount);
        grandTotalObject.setSaleAmount(grandTotalSaleAmount);

        grandTotalObject.setVoidCount(grandTotalVoidSaleCount);
        grandTotalObject.setVoidAmount(grandTotalVoidSaleAmount);

        //grandTotalAmount = grandTotalSaleAmount - grandTotalVoidSaleAmount;
        grandTotalObject.setVoidAmount(grandTotalVoidSaleAmount);
        map.put(grandTotalTitle, grandTotalObject);

        return new ArrayList<>(map.values());
    }

    public List<CardSchemeReportModel> generateSchemeReport(List<Record> records) {

        Map<String, CardSchemeReportModel> map = new HashMap<>();

        String grandTotalTitle = "GRAND TOTAL";
        long grandTotalSaleAmount = 0;
        long grandTotalVoidSaleAmount = 0;
        int grandTotalSaleCount = 0;
        int grandTotalVoidSaleCount = 0;
        int grandTotalCount = 0;
        long grandTotalAmount = 0;

        for (Record record : records) {
            String scheme = record.getCardScheme();
            boolean isOnUs = false;
            try {
                String ben = record.getCardNo().substring(0, 6);
                String cardTitle = OnUsBinMap.REPORT_CARD_ONUS_MAP.get(ben).getCardTitle();
                if (cardTitle != null && !cardTitle.isEmpty()) {
                    isOnUs = true;
                }
            } catch (Exception ex) {

            }

            if (isOnUs) scheme = scheme + " ONUS";
            else scheme = scheme + " OFFUS";


            if (scheme != null) {
                if (!map.containsKey(scheme)) {
                    CardSchemeReportModel m = new CardSchemeReportModel();
                    m.setScheme(scheme);
                    map.put(scheme, m);
                }
                CardSchemeReportModel model = map.get(scheme);
                model.setTransType(record.getTransType());

                if ("SALE".equalsIgnoreCase(record.getTransType())) {
                    ++grandTotalSaleCount;
                    ++grandTotalCount;
                    model.setTranSaleTitle("SALE");
                    model.setSaleCount(model.getSaleCount() + 1);
                    grandTotalSaleAmount += record.getAmount();
                    model.setSaleAmount(model.getSaleAmount() + record.getAmount());
                }
                if ("VoidSale".equalsIgnoreCase(record.getTransType())
                        || "VOID SALE".equalsIgnoreCase(record.getTransType())) {
                    ++grandTotalVoidSaleCount;
                    ++grandTotalCount;
                    model.setTranVoidTitle("VOID");
                    grandTotalVoidSaleAmount += record.getAmount();
                    model.setVoidCount(model.getVoidCount() + 1);
                    model.setVoidAmount(model.getVoidAmount() + record.getAmount());
                }

                if ("AuthComplete".equalsIgnoreCase(record.getTransType())) {
                    ++grandTotalVoidSaleCount;
                    ++grandTotalCount;
                    model.setTranPreAuthCmpltTitle("SALE COMPLETE");
                    grandTotalSaleAmount += record.getAmount();
                    model.setVoidCount(model.getPreAuthCount() + 1);
                    model.setVoidAmount(model.getPreAuthAmount() + record.getAmount());
                }

                if ("VoidPreAuth".equalsIgnoreCase(record.getTransType())) {
                    ++grandTotalVoidSaleCount;
                    ++grandTotalCount;
                    model.setTranPreAuthVoidCmpltTitle("VOID SALE COMPLETE");
                    grandTotalVoidSaleAmount += record.getAmount();
                    model.setVoidCount(model.getVoidPreAuthCount() + 1);
                    model.setVoidAmount(model.getVoidPreAuthAmount() + record.getAmount());
                }

            }
        }

        CardSchemeReportModel grandTotalObject = new CardSchemeReportModel();
        grandTotalObject.setScheme(grandTotalTitle);
        grandTotalObject.setTranSaleTitle("SALE");
        grandTotalObject.setTranVoidTitle("VOID");


        grandTotalObject.setSaleCount(grandTotalSaleCount);
        grandTotalObject.setSaleAmount(grandTotalSaleAmount);

        grandTotalObject.setVoidCount(grandTotalVoidSaleCount);
        grandTotalObject.setVoidAmount(grandTotalVoidSaleAmount);

        //grandTotalAmount = grandTotalSaleAmount - grandTotalVoidSaleAmount;
        grandTotalObject.setVoidAmount(grandTotalVoidSaleAmount);
        map.put(grandTotalTitle, grandTotalObject);

        return new ArrayList<>(map.values());
    }

    public List<CardSchemeReportModel> moveGrandTotalToBottom(List<CardSchemeReportModel> schemeList) {

        CardSchemeReportModel grandTotalObj = null;

        for (CardSchemeReportModel model : schemeList) {
            if ("GRAND TOTAL".equalsIgnoreCase(model.getScheme())) {
                grandTotalObj = model;
                break;
            }
        }

        if (grandTotalObj != null) {
            schemeList.remove(grandTotalObj);
            schemeList.add(grandTotalObj);
        }
        return schemeList;
    }

}
