package acquire.core.tools;

import android.util.ArrayMap;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import acquire.base.BaseApplication;
import acquire.base.constants.BasePrefKey;
import acquire.base.utils.LoggerUtils;
import acquire.base.utils.ParamsUtils;
import acquire.base.utils.file.FileUtils;
import acquire.core.R;
import acquire.core.constant.FileConst;
import acquire.core.constant.FileDir;
import acquire.core.constant.ParamsConst;
import acquire.core.model.ConnectivityModel;
import acquire.database.model.Merchant;
import acquire.database.repository.MerchantRepository;

/**
 * Import default params
 *
 * @author Janson
 * @date 2021/3/15 9:25
 */
public class AppParamsImporter {

    /**
     * load defaultparams.ini from assets.
     */
    public static void initDefaultAppParams(String opName) {

        InputStream inputStream = null;
        try {
            //The file downloaded from the PC downloader will be placed in FileDir.SHARE_PATH

            File importFile = new File(FileDir.SHARE_PATH + FileConst.PARAMS);
            File importExtranalFile = new File("/storage/emulated/0/custom_ucb/UCBLPARA.ini");

            if (importExtranalFile.exists()) {
                importFile = importExtranalFile;
                ParamsUtils.setBoolean(ParamsConst.PARAMS_KEY_IS_PRESENT_INI_FILE, true);
            } else {
                ParamsUtils.setBoolean(ParamsConst.PARAMS_KEY_IS_PRESENT_INI_FILE, false);
                initDefaultMerchants();
            }

            if (importFile.exists()) {
                LoggerUtils.e("There is an external defaultparams.properties !");
                inputStream = new FileInputStream(importFile);
                if (inputStream.available() < 0) {
                    LoggerUtils.e("External defaultparams.properties is empty!!!");
                    inputStream = null;
                }
            }
            if (inputStream == null) {
                inputStream = BaseApplication.getAppContext().getAssets().open(FileConst.PARAMS);
                //file:///android_asset/ic_quick_item.png
            }
            //import file data
            importAppParams(inputStream, opName);

            if (importFile.exists()) {
                //Brac TODO, currently commit belo line for developingTime. It should open before release
                importFile.delete(); // this line responsible for remove *.ini file from memory
            }
        } catch (Exception e) {
            LoggerUtils.e("initDefaultAppParams failed!", e);
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e1) {
                    LoggerUtils.e("initDefaultAppParams inputStream close failed!", e1);
                }
            }
        }
    }

    /**
     * import params
     *
     * @param inputStream params data stream.  File format reference core>src>main>asset>defaultparams.ini
     */
    public static void importAppParams(InputStream inputStream, String opName) throws IOException {
        ArrayList<String> paramsGroupList = new ArrayList<>(
                Arrays.asList(
                       /* "[BASE]",
                        "[TRANS]",
                        "[COMM]",
                        "[LINE]",
                        "[DIAL]",*/
                        "[GPRS1]",
                        "[GPRS2]",
                        "[GPRS3]",
                        "[GPRS4]",
                        "[CDMA]",
                        "[WIFI]"/*,
                        "[KEY]"*/
                )
        );
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        Map<String, String> map = new ArrayMap<>();
        String line;
        LoggerUtils.d("Import params->**Start** parsing:");
        boolean isIpPoGetActive = false;

        ArrayList<ConnectivityModel> connectivityModels = new ArrayList<>();
        ConnectivityModel tmpObj = new ConnectivityModel();
        while ((line = bufferedReader.readLine()) != null) {

            line = line.trim();
            if (!isIpPoGetActive && line.matches(".*=.*")) {
                int i = line.indexOf('=');
                //e.g. BASE_TRACE_NO=000001 , key = BASE_TRACE_NO, value = 000001
                String key = line.substring(0, i).trim();
                String value = line.substring(i + 1).trim();
                map.put(key, value);
                LoggerUtils.d("Import params->key:" + key + ", value:" + value);
            } else if (line.equals("[KEY]")) {
                LoggerUtils.d("Import params->Atart Key input key:");
                isIpPoGetActive = false;
            } else if (isIpPoGetActive || paramsGroupList.contains(line)) {
              /*int index = sectionList.indexOf(line);
                String value = sectionList.get(sectionList.indexOf(line));*/
                if (isIpPoGetActive /*&& (!line.startsWith("[GPRS") || !line.startsWith("[CDMA") || !line.startsWith("[WIFI"))*/) {
                    if ((line.startsWith("[GPRS") || line.startsWith("[CDMA") || line.startsWith("[WIFI")) && tmpObj.getMode() != null && !tmpObj.getMode().isEmpty() && !tmpObj.getOperatorCode().equals(line)) {
                        connectivityModels.add(tmpObj);
                        tmpObj = new ConnectivityModel();
                        tmpObj.setOperatorCode(line);
                    } else if (line.matches(".*=.*")) {
                        int i = line.indexOf('=');
                        //e.g. BASE_TRACE_NO=000001 , key = BASE_TRACE_NO, value = 000001
                        String key = line.substring(0, i).trim();
                        String value = line.substring(i + 1).trim();
                        if (key.equals("OPERATORNAME") || key.equals("SSID")) {
                            tmpObj.setOperatorName(value);
                        } else if (key.equals("SERVERIP1")) {
                            tmpObj.setServiceIp1(value);
                        } else if (key.equals("SERVERIP2")) {
                            tmpObj.setServiceIp2(value);
                        } else if (key.equals("PORT1") || key.equals("PORT")) {
                            tmpObj.setPort1(value);
                        } else if (key.equals("PORT2")) {
                            tmpObj.setPort2(value);
                        } else if (key.equals("APN")) {
                            tmpObj.setApn(value);
                        } else if (key.equals("USERNAME")) {
                            tmpObj.setUserName(value);
                        } else if (key.equals("PWD")) {
                            tmpObj.setUserPwd(value);
                        } else if (key.equals("MODE")) {
                            tmpObj.setMode(value);
                        } else if (key.equals("USSD")) {
                            tmpObj.setUssd(value);
                        } else if (key.equals("LOCALIP")) {
                            tmpObj.setLocalIp(value);
                        } else if (key.equals("MASK")) {
                            tmpObj.setMask(value);
                        } else if (key.equals("GATEWAY")) {
                            tmpObj.setGateway(value);
                        }
                    }

                }
                /* else if (isIpPoGetActive && (!line.startsWith("[GPRS") || !line.startsWith("[CDMA") || !line.startsWith("[WIFI"))) {
                    connectivityModels.add(tmpObj);
                    LoggerUtils.d("Import params-> tmpObj:" + tmpObj.toString());

                    tmpObj = new ConnectivityModel();
                    tmpObj.setOperatorCode(line);
                }*/
                else {
                    isIpPoGetActive = true;
                    tmpObj.setOperatorCode(line);
                }

            } else {
                LoggerUtils.d("Import params->non parsing string:" + line);
            }
        }//end while


       /* Gson gson = new Gson();
        String json = gson.toJson(connectivityModels);
        LoggerUtils.d("Import params->**Converted Host info model to string** ");
        LoggerUtils.d("Import params->** data: " + json);
        map.put("HOST_SERVERS", json);
        LoggerUtils.d("Import params->** data save in Map: ");*/


        LoggerUtils.d("Import params->**END** parsing:");
        ParamsUtils.save(map);
        LoggerUtils.d("Import params->** data save in prefarance: ");
        bufferedReader.close();

        initBracDefaultMerchants(opName, connectivityModels);

    }


    public static void initBracDefaultMerchants(String opName, ArrayList<ConnectivityModel> getConnectivityList) {
        try {
            //m = merchant
            /*String hostServers = ParamsUtils.getString("HOST_SERVERS", "");

            if (hostServers.isEmpty()) {
                LoggerUtils.d("Import params->** data save in prefarance: ");
                return;
            }
           Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<ConnectivityModel>>() {
            }.getType();
            ArrayList<ConnectivityModel> getConnectivityList = gson.fromJson(hostServers, type);
            if (getConnectivityList == null || getConnectivityList.size() <= 0) {
                return;
            }*/

            ConnectivityModel mobileOperatorModel = getConnectivityList.stream()
                    .filter(model -> opName.equalsIgnoreCase(model.getOperatorName()))
                    .findFirst()
                    .orElse(null);

            if (mobileOperatorModel != null) {
                ParamsUtils.setBoolean(ParamsConst.PARAMS_KEY_IS_OPERATOR_AVAILABLE, true);
                //We separate selected Operator. It insert in first row in Merchant database
                getConnectivityList.removeIf(
                        model -> opName.equalsIgnoreCase(model.getOperatorName())
                );
            } else {
                ParamsUtils.setBoolean(ParamsConst.PARAMS_KEY_IS_OPERATOR_AVAILABLE, false);
                LoggerUtils.d("Import params->*** No default Connectivity Operator *** ");

                return;
            }

            String mTimeOut = ParamsUtils.getString(ParamsConst.PARAMS_KEY_TIMEOUT, "5"); // default 5 second
            String mId = ParamsUtils.getString(ParamsConst.PARAMS_KEY_MERCHANT_ID, "");
            String mName = ParamsUtils.getString(ParamsConst.PARAMS_KEY_MERCHANT_NAME_NEW, "");
            String mPosTerminalId = ParamsUtils.getString(ParamsConst.PARAMS_KEY_POS_ID, "");
            String mType = ParamsUtils.getString(ParamsConst.PARAMS_KEY_TYPE, "");
            //Merchant Address
            String mAddress1 = ParamsUtils.getString(ParamsConst.PARAMS_KEY_MERCHANT_ADDR1, "");
            String mAddress2 = ParamsUtils.getString(ParamsConst.PARAMS_KEY_MERCHANT_ADDR2, "");
            String mAddress3 = ParamsUtils.getString(ParamsConst.PARAMS_KEY_MERCHANT_ADDR3, "");
            String mAddress4 = ParamsUtils.getString(ParamsConst.PARAMS_KEY_MERCHANT_ADDR4, "");
            String mAddress5 = ParamsUtils.getString(ParamsConst.PARAMS_KEY_MERCHANT_ADDR5, "");

            String mBatchNo = ParamsUtils.getString(ParamsConst.PARAMS_KEY_BATCH_NO, "");
            String mTpdu = ParamsUtils.getString(ParamsConst.PARAMS_KEY_TPDU, "");
            String mNii = ParamsUtils.getString(ParamsConst.PARAMS_KEY_NII, "");
            String mMasterKekKeyIndex = ParamsUtils.getString(ParamsConst.PARAMS_KEY_MASTER_KEY_INDEX, "1");
            String mTpkIndex = ParamsUtils.getString(ParamsConst.PARAMS_KEY_TPK_KEY_INDEX, "1");
            String mAlgorithmType = ParamsUtils.getString(ParamsConst.PARAMS_KEY_ALGORITHM_TYPE, "1");

            //EMI -
            String mEmiId = ParamsUtils.getString(BasePrefKey.PREF_KEY_PAYPLEX_EMI_EMIMERCHANTID, "");
            String mEmiPosTerminalId = ParamsUtils.getString(BasePrefKey.PREF_KEY_PAYPLEX_EMI_EMIPOSID, "");
            String mEmiName = ParamsUtils.getString(BasePrefKey.PREF_KEY_PAYPLEX_EMI_EMIMERCHANTNAME, "");
            String mEmiBatchNo = ParamsUtils.getString(BasePrefKey.PREF_KEY_PAYPLEX_EMI_EMIBATCHNO, "000001");
            String mEmiTenure = ParamsUtils.getString(BasePrefKey.PREF_KEY_PAYPLEX_EMI_EMITENURE, "");
            String mEmiVendor = ParamsUtils.getString(BasePrefKey.PREF_KEY_PAYPLEX_EMI_VENDOR, "");
            String mEmiProductId = ParamsUtils.getString(BasePrefKey.PREF_KEY_PAYPLEX_EMI_PRODUCTID, "");
            String mEmiMerchantType = ParamsUtils.getString(BasePrefKey.PREF_KEY_PAYPLEX_EMI_EMITYPE, "");
            String mEmiMasterKekKeyIndex = ParamsUtils.getString(BasePrefKey.PREF_KEY_PAYPLEX_EMI_EMIMASTERKEYINDEX, "2");
            String mEmiTpkIndex = ParamsUtils.getString(BasePrefKey.PREF_KEY_PAYPLEX_EMI_EMITPKINDEX, "2");
            //----------------

            Merchant mchEmi = new Merchant();
            mchEmi.setMid(mEmiId);
            mchEmi.setEmiBatchNo(mEmiBatchNo);

            mchEmi.setMerchantName(mEmiName);
            mchEmi.setType(mEmiMerchantType);
            mchEmi.setTid(mEmiPosTerminalId);
            mchEmi.setBatchNo(mEmiBatchNo);
            mchEmi.setCommTimeout(Integer.parseInt(mTimeOut));

            mchEmi.setTpdu(mTpdu);
            mchEmi.setNii(mNii);
//            mchEmi.setMasterKeyIndex(Integer.parseInt(mEmiMasterKekKeyIndex));
            mchEmi.setMasterKeyIndex(2);
            mchEmi.setAlgorithm(Integer.parseInt(mAlgorithmType));

            mchEmi.setIp(mobileOperatorModel.getServiceIp1());
            //--------------------------------

            Merchant mch = new Merchant();
            mch.setMid(mId);
            mch.setMerchantName(mName);
            mch.setType(mType);
            mch.setTid(mPosTerminalId);
            mch.setBatchNo(mBatchNo);
            mch.setCommTimeout(Integer.parseInt(mTimeOut));

            mch.setTpdu(mTpdu);
            mch.setNii(mNii);
            mch.setMasterKeyIndex(Integer.parseInt(mMasterKekKeyIndex));
            mch.setAlgorithm(Integer.parseInt(mAlgorithmType));

            mch.setIp(mobileOperatorModel.getServiceIp1());
            try {
                mch.setPort(Integer.parseInt(mobileOperatorModel.getPort1()));
                mchEmi.setPort(Integer.parseInt(mobileOperatorModel.getPort1()));
            } catch (Exception ex) {
                mch.setPort(0);
                mchEmi.setPort(0);
            }

            //insert default
            AppParamsImporter.importBracMerchants(mch);

            for (int i = 0; i < getConnectivityList.size(); i++) {
                mch.setIp(getConnectivityList.get(i).getServiceIp1());
                try {
                    mch.setPort(Integer.parseInt(getConnectivityList.get(i).getPort1()));
                } catch (Exception ex) {
                    mch.setPort(0);
                }
                mch.setType(""); //set empty merchant type accept first one, because first one is Enable SIM wise binding
                AppParamsImporter.importBracMerchantsAdd(mch);
            }
            AppParamsImporter.importBracMerchantsAdd(mchEmi);


        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * load defaultmerchants.ini from assets.
     */
    public static void initDefaultMerchants() {
        InputStream inputStream = null;
        try {
            //The file downloaded from the PC downloader will be placed in FileDir.SHARE_PATH
            File importFile = new File(FileDir.SHARE_PATH + FileConst.MERCHANTS);
            if (importFile.exists()) {
                LoggerUtils.e("There is an external merchants.xml !");
                inputStream = new FileInputStream(importFile);
                if (inputStream.available() < 0) {
                    LoggerUtils.e("External merchants.xml is empty!!!");
                    inputStream = null;
                }
            }
            if (inputStream == null) {
                inputStream = BaseApplication.getAppContext().getAssets().open(FileConst.MERCHANTS);
            }
            //import merchant file data
            importMerchants(inputStream);
            if (importFile.exists()) {
                importFile.delete();
            }
        } catch (Exception e) {
            LoggerUtils.e("initDefaultMerchants failed!", e);
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e1) {
                    LoggerUtils.e("initDefaultMerchants inputStream close failed!", e1);
                }
            }
        }
    }

    public static void importBracMerchants(Merchant mch) throws IOException {
        MerchantRepository merchantRepository = new MerchantRepository();
        merchantRepository.deleteAll();

        LoggerUtils.d("Import Brac merchant for save in Database->" + mch.toString());
        merchantRepository.add(mch);
    }

    public static void importBracMerchantsAdd(Merchant mch) throws IOException {
        MerchantRepository merchantRepository = new MerchantRepository();
        // merchantRepository.deleteAll();
        LoggerUtils.d("Import Brac merchant for save in merchant database Database->" + mch.toString());
        merchantRepository.addBracMerchants(mch);
    }


    /**
     * import merchant
     *
     * @param inputStream merchant data stream. File format reference core>src>main>asset>defaultmerchants.ini
     */
    public static void importMerchants(InputStream inputStream) throws IOException {
        try {
            XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
            parser.setInput(inputStream, StandardCharsets.UTF_8.name());
            List<Merchant> merchants = new ArrayList<>();
            String groupTag = "Merchant";
            int eventType = parser.getEventType();
            Merchant targetMerchant = null;
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String tagName = parser.getName();
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        if (groupTag.equals(tagName)) {
                            targetMerchant = new Merchant();
                        } else if (targetMerchant != null) {
                            String value = parser.nextText();
                            for (Field field : Merchant.class.getDeclaredFields()) {
                                field.setAccessible(true);
                                if (tagName.equals(field.getName())) {
                                    try {
                                        field.set(targetMerchant, DataConverter.stringToObject(value, field.getType()));
                                    } catch (Exception e) {
                                        LoggerUtils.e("importMerchants " + field + " set  Merchant failed!", e);
                                    }
                                }
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if (groupTag.equals(tagName) && targetMerchant != null) {
                            merchants.add(targetMerchant);
                        }
                        break;
                    default:
                        break;
                }
                eventType = parser.next();
            }
            if (!merchants.isEmpty()) {
                MerchantRepository merchantRepository = new MerchantRepository();
                merchantRepository.deleteAll();
                for (Merchant mch : merchants) {
                    LoggerUtils.d("Import merchant->" + mch.toString());
                    merchantRepository.add(mch);
                }
            }
        } catch (Exception e) {
            LoggerUtils.e("importMerchants failed!", e);
        }
    }

    public static void downloadFromPc() throws Exception {
        File downloadDir = new File(FileDir.PC_DOWNLOAD);
        FileUtils.delete(downloadDir, true);
        File prefFile = new File(downloadDir, FileConst.PARAMS);
        File merchantFile = new File(downloadDir, FileConst.MERCHANTS);
        if (!prefFile.exists() && !merchantFile.exists()) {
            throw new Exception(BaseApplication.getAppString(R.string.core_paramfile_no_exist));
        }
        if (prefFile.exists()) {
            try (FileInputStream inputStream = new FileInputStream(prefFile)) {
                if (inputStream.available() > 0) {
                    importAppParams(inputStream, null);
                } else {
                    throw new Exception(BaseApplication.getAppString(R.string.core_paramfile_parse_failed_format, FileConst.PARAMS));
                }
            } catch (IOException e) {
                LoggerUtils.e(e.getMessage());
                throw new Exception(BaseApplication.getAppString(R.string.core_paramfile_parse_failed_format, FileConst.PARAMS));
            }
        }
        if (merchantFile.exists()) {
            try (FileInputStream inputStream = new FileInputStream(merchantFile)) {
                if (inputStream.available() > 0) {
                    importMerchants(inputStream);
                } else {
                    throw new Exception(BaseApplication.getAppString(R.string.core_paramfile_parse_failed_format, FileConst.MERCHANTS));
                }
            } catch (IOException e) {
                LoggerUtils.e(e.getMessage());
                throw new Exception(BaseApplication.getAppString(R.string.core_paramfile_parse_failed_format, FileConst.MERCHANTS));
            }
        }
    }

}
