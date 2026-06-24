package acquire.core.tools;

import android.content.Context;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;

import acquire.base.utils.LoggerUtils;
import acquire.base.utils.ParamsUtils;
import acquire.core.constant.FileConst;
import acquire.core.model.CardBinModel;
import acquire.core.model.PayFlexResponse;
import acquire.core.model.PreAuthResponse;
import acquire.core.model.PrintReceiptResponse;
import acquire.core.model.RecordResponse;
import acquire.core.model.FeatureSubMenuModel;
import acquire.core.model.ReportsResponse;
import acquire.database.model.Record;

public class LoadMenuData {

    public static ArrayList<Record> loadDummyRecords(Context context) {

        ArrayList<Record> reportList = new ArrayList<>();

       // String json = ParamsUtils.getString(FileConst.REPORT_MENU_PREF_KEY, null);//JsonUtils.loadJSONFromAsset(getContext(), null);
        String json = JsonUtils.loadJSONFromAsset(context, FileConst.MENU_FILE_RECORDS);

        if(json==null){
            LoggerUtils.i("newCall get ReportMenu: is null");
            return reportList;
        }

        LoggerUtils.d("newCall get ReportMenu: "+json);
        Gson gson = new Gson();
        RecordResponse response =
                gson.fromJson(json, RecordResponse.class);

        try{
            reportList = new ArrayList<>(response.getRecords());
        }catch (Exception ex){
            LoggerUtils.d("newCall get loadDummyRecords(): "+ex.getMessage());
        }

        return reportList;
    }

    public static ArrayList<FeatureSubMenuModel> loadPrintReceiptMenuItems() {

        ArrayList<FeatureSubMenuModel> printReceiptMenuList = new ArrayList<>();

        String json = ParamsUtils.getString(FileConst.MENU_KEY_PRINT_RECEIPT, null);//JsonUtils.loadJSONFromAsset(getContext(), null);

        if(json==null){
            LoggerUtils.i("newCall get printReceiptMenuList: is null");
            return printReceiptMenuList;
        }

        LoggerUtils.d("newCall get printReceiptMenuList: "+json);
        Gson gson = new Gson();
        PrintReceiptResponse response =
                gson.fromJson(json, PrintReceiptResponse.class);

        printReceiptMenuList = new ArrayList<>(response.getPayFlaxMenu());
        printReceiptMenuList.removeIf(printReceiptItem -> !printReceiptItem.isShow()); //This will directly remove all items where isShow == false.
        return printReceiptMenuList;
    }

    public static ArrayList<FeatureSubMenuModel> loadPayFlexMenuItems() {

        ArrayList<FeatureSubMenuModel> payFlexMenuList = new ArrayList<>();

        String json = ParamsUtils.getString(FileConst.MENU_KEY_PAY_FLEX, null);//JsonUtils.loadJSONFromAsset(getContext(), null);

        if(json==null){
            LoggerUtils.i("newCall get PayFlexMenu: is null");
            return payFlexMenuList;
        }

        LoggerUtils.d("newCall get PayFlexMenu: "+json);
        Gson gson = new Gson();
        PayFlexResponse response =
                gson.fromJson(json, PayFlexResponse.class);

        payFlexMenuList = new ArrayList<>(response.getPayFlaxMenu());
        payFlexMenuList.removeIf(payFlexItem -> !payFlexItem.isShow()); //This will directly remove all items where isShow == false.
        return payFlexMenuList;
    }

    public static ArrayList<FeatureSubMenuModel> loadReportMenuItems() {

        ArrayList<FeatureSubMenuModel> reportList = new ArrayList<>();

        String json = ParamsUtils.getString(FileConst.MENU_PREF_KEY_REPORT, null);//JsonUtils.loadJSONFromAsset(getContext(), null);

        if(json==null){
            LoggerUtils.i("newCall get ReportMenu: is null");
            return reportList;
        }

        LoggerUtils.d("newCall get ReportMenu: "+json);
        Gson gson = new Gson();
        ReportsResponse response =
                gson.fromJson(json, ReportsResponse.class);

        reportList = new ArrayList<>(response.getReports());
        reportList.removeIf(report -> !report.isShow()); //This will directly remove all items where isShow == false.
        return reportList;
    }

    public static ArrayList<FeatureSubMenuModel> loadPreAuthMenuItems() {

        ArrayList<FeatureSubMenuModel> dataList = new ArrayList<>();

        String json = ParamsUtils.getString(FileConst.MENU_PREF_KEY_PRE_AUTH, null);//JsonUtils.loadJSONFromAsset(getContext(), null);

        if(json==null){
            LoggerUtils.i("newCall get PreAuthMenu: is null");
            return dataList;
        }

        LoggerUtils.d("newCall get PreAuthMenu: "+json);
        Gson gson = new Gson();
        PreAuthResponse response =
                gson.fromJson(json, PreAuthResponse.class);

        dataList = new ArrayList<>(response.getPreAuthMenu());
        dataList.removeIf(dataItem -> !dataItem.isShow()); //This will directly remove all items where isShow == false.
        return dataList;
    }


    public static ArrayList<CardBinModel> parseCardBins(String data){

        ArrayList<CardBinModel> list = new ArrayList<>();
        String[] records = data.split(",");
        for(String record : records){
            String[] fields = record.split(":");
            if(fields.length == 4){
                int length = Integer.parseInt(fields[0]);
                String startBin = fields[1];
                String endBin = fields[2];
                String title = fields[3];

                CardBinModel model =
                        new CardBinModel(length,startBin,endBin,title);
                list.add(model);
            }
        }

        return list;
    }

    /**
     * Parse INI bean range into the CardBinModel type Map collection
     *
     * */
    public static HashMap<String,CardBinModel> parseCardBinsMap(String data){
        HashMap<String,CardBinModel> cardRangeMap = new HashMap<>();
        //ArrayList<CardBinModel> list = new ArrayList<>();
        String[] records = data.split(",");
        for(String record : records){
            String[] fields = record.split(":");
            if(fields.length == 4) {
                int length = Integer.parseInt(fields[0]);
                String startBin = fields[1];
                String binKey = startBin.substring(0, 6);
                String endBin = fields[2];
                String title = fields[3];

                CardBinModel model =
                        new CardBinModel(length,startBin,endBin,title);
                cardRangeMap.put(binKey,model);
                //list.add(model);
            }
        }
        return cardRangeMap;
    }
}
