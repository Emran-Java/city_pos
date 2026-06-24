package acquire.core.trans.pack.iso;

import android.util.Log;

import androidx.annotation.NonNull;

import java.util.Arrays;

import acquire.base.BaseApplication;
import acquire.base.utils.BytesUtils;
import acquire.base.utils.LoggerUtils;
import acquire.core.R;

/**
 * Pack head and tpdu
 *
 * @author Janson
 * @date 2019/11/7 16:48
 */
public class PacketHead {
    /**
     * data length bytes
     */
    private final static int LEN_BYTE_COUNT = 2;

    @NonNull
    static byte[] packHeadTpduBitmap(String tpdu,String mti, String bitMap, byte[] request) throws Exception {

        String stringRequest = LoggerUtils.bytesToHex(request);
        Log.d("NewCall", "request: "+stringRequest);


        //add bitmap
        byte[] bBitmap = BytesUtils.hexToBytes(bitMap);
        if (bBitmap == null) {
            throw new Exception(BaseApplication.getAppString(R.string.core_comm_pack_tpdu_error));
        }

        String sBitmap = LoggerUtils.bytesToHex(bBitmap);
        LoggerUtils.d("Add request bitmap: " + sBitmap);
        request = BytesUtils.merge(bBitmap, request);
        stringRequest = LoggerUtils.bytesToHex(request);
        Log.d("NewCall", "update with bBitmap: "+stringRequest);

        //add MTI
        byte[] bMti = BytesUtils.hexToBytes(mti);
        if (bMti == null) {
            throw new Exception(BaseApplication.getAppString(R.string.core_comm_pack_tpdu_error));
        }
        LoggerUtils.d("Add request bMti: " + bMti);
        request = BytesUtils.merge(bMti, request);
        stringRequest = LoggerUtils.bytesToHex(request);
        Log.d("NewCall", "update with bBitmap: "+stringRequest);
        Log.d("NewCall", "update with bTpdu: "+LoggerUtils.bytesToHex(request));


        //add tpdu
        byte[] bTpdu = BytesUtils.hexToBytes(tpdu);
        if (bTpdu == null) {
            throw new Exception(BaseApplication.getAppString(R.string.core_comm_pack_tpdu_error));
        }
        LoggerUtils.d("Add request tpdu: " + tpdu);
        request = BytesUtils.merge(bTpdu, request);
        stringRequest = LoggerUtils.bytesToHex(request);
        Log.d("NewCall", "update with bBitmap: "+stringRequest);
        Log.d("NewCall", "update with bTpdu: "+LoggerUtils.bytesToHex(request));



        //add length bytes
        byte[] bLen = BytesUtils.intToBytes(request.length, LEN_BYTE_COUNT);
        LoggerUtils.d("Add request length bytes: " + BytesUtils.bcdToString(bLen)+" => "+request.length);

        byte[] withLangth = BytesUtils.merge(bLen, request);
       String replay = LoggerUtils.bytesToHex(withLangth);
        Log.d("NewCall", "update with bTpdu: "+replay);

        return withLangth;
    }

    /**
     * Add: length bytes + head + tdpu
     *
     * @param request request data
     * @return the data after adding
     */
    @NonNull
    static byte[] packHeadTpdu(String tpdu,byte[] request) throws Exception {
        String sRequest = LoggerUtils.bytesToHex(request);
        Log.d("NewCall", "request: "+sRequest);

        //add tpdu
        byte[] bTpdu = BytesUtils.hexToBytes(tpdu);
        if (bTpdu == null) {
            throw new Exception(BaseApplication.getAppString(R.string.core_comm_pack_tpdu_error));
        }

        LoggerUtils.d("Add request tpdu: " + tpdu);
        request = BytesUtils.merge(bTpdu, request);

        sRequest = LoggerUtils.bytesToHex(request);
        Log.d("NewCall", "update with bTpdu: "+sRequest);

        //add length bytes
        byte[] bLen = BytesUtils.intToBytes(request.length, LEN_BYTE_COUNT);
        LoggerUtils.d("Add request length bytes: " + BytesUtils.bcdToString(bLen)+" => "+request.length);

        byte[] withLangth = BytesUtils.merge(bLen, request);

        sRequest = LoggerUtils.bytesToHex(withLangth);
        Log.d("NewCall", "Add request length bytes: "+sRequest);

        return withLangth;
    }

    /**
     * Delete: length bytes + head + tdpu
     *
     * @param responseData reponse data
     * @return the data after deleting
     */
    @NonNull
    static byte[] unpackHeadTpdu(String tpdu,@NonNull byte[] responseData) throws Exception {
        if (responseData.length < LEN_BYTE_COUNT) {
            LoggerUtils.e("Response length is shorter than " + LEN_BYTE_COUNT);
            throw new Exception(BaseApplication.getAppString(R.string.core_comm_unpack_length_error));
        }
        //delete length bytes
        byte[] bLen = Arrays.copyOfRange(responseData, 0, LEN_BYTE_COUNT);
        int len = BytesUtils.bytesToInt(bLen);
        if (len == 0 || len != responseData.length - LEN_BYTE_COUNT) {
            LoggerUtils.e("Response length is wrong.");
            throw new Exception(BaseApplication.getAppString(R.string.core_comm_unpack_length_error));
        }
        LoggerUtils.d("Delete response length: " + BytesUtils.bcdToString(bLen)+" => "+len);
        responseData = Arrays.copyOfRange(responseData, LEN_BYTE_COUNT, responseData.length);

        //delete tpdu
        int tpduLen = tpdu.length() / 2;
        if (responseData.length < tpduLen) {
            throw new Exception(BaseApplication.getAppString(R.string.core_comm_unpack_tpdu_error));
        }
        LoggerUtils.d("Delete response tpdu: " + BytesUtils.bcdToString(Arrays.copyOfRange(responseData, 0, tpduLen)));
        responseData = Arrays.copyOfRange(responseData, tpduLen, responseData.length);
        if (responseData.length == 0) {
            throw new Exception(BaseApplication.getAppString(R.string.core_comm_unpack_response_format_error));
        }
        return responseData;
    }
}
