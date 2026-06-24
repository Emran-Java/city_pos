package acquire.sdk.printer;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;

import com.newland.nsdk.core.api.common.ModuleType;
import com.newland.nsdk.core.api.common.exception.NSDKException;
import com.newland.nsdk.core.api.internal.printer.Printer;
import com.newland.nsdk.core.api.internal.printer.PrinterStatus;
import com.newland.nsdk.core.internal.NSDKModuleManagerImpl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import acquire.base.utils.LoggerUtils;
import acquire.sdk.R;

/**
 * Printer
 *
 * @author Janson
 * @date 2021/10/9 11:28
 */
public class BPrinter implements IPrinter {
    private final Printer mPrinter;

    public BPrinter() {
        mPrinter = (Printer) NSDKModuleManagerImpl.getInstance().getModule(ModuleType.PRINTER);
    }


    /**
     * get the printer status
     *
     * @return OK = 0
     * ERR = -1
     * ERR_BUSY = 8
     * ERR_NOPAPER = 2
     * ERR_OVERHEAT = 4
     * ERR_VOLERR = 112
     * ERR_CUTERR = 512
     * ERR_PPSERR = 2048
     * ERR_IOCTL = -5
     * ERR_PARA = -6
     * ERR_PATH = -7
     * ERR_DECODE_IMAGE = -8
     * ERR_MACLLOC = -9
     * ERR_TIMEOUT = -10
     */
    public int getStatus() {
        try {
            PrinterStatus status = mPrinter.getStatus();
            return status.getCode();
        } catch (Exception e) {
            LoggerUtils.e("Printer.getStatus() failed",e);
            return 0xff;
        }
    }

    public boolean outOfPaper(){
        try {
            PrinterStatus status = mPrinter.getStatus();
            return status == PrinterStatus.NO_PAPER;
        } catch (NSDKException e) {
            LoggerUtils.e("Printer.getStatus() failed",e);
            return false;
        }
    }

    /**
     * get status description
     *
     * @param status printer status
     * @return status description
     */
    public String getStatusDescription(int status) {
        Context context = NSDKModuleManagerImpl.getInstance().getContext();
        if (context == null) {
            return "";
        }
        if (status == PrinterStatus.OK.getCode()) {
            return context.getString(R.string.sdk_helper_printer_status_normal);
        } else if (status == PrinterStatus.BUSY.getCode()) {
            return context.getString(R.string.sdk_helper_printer_status_busy);
        } else if (status == PrinterStatus.NO_PAPER.getCode()) {
            return context.getString(R.string.sdk_helper_printer_status_outof_paper);
        } else if (status == PrinterStatus.OVERHEAT.getCode()) {
            return context.getString(R.string.sdk_helper_printer_status_hot);
        }else if (status == 2048) {
            return context.getString(R.string.sdk_helper_printer_status_paper_slot);
        } else {
            return context.getString(R.string.sdk_helper_printer_status_exception);
        }
    }

    @Override
    public void print(@NonNull Bitmap receipt,boolean endFeed, final PrintCallback callback) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        //compress bitmap.
        receipt.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] receiptBytes = baos.toByteArray();
        try {
            baos.close();
        } catch (IOException e) {
            LoggerUtils.e("close ByteArrayOutputStream failed!",e);
        }
        try {
            LoggerUtils.i("[NSDK Printer]--start to print.");
            mPrinter.printImage(receiptBytes, 0, receipt.getWidth(), receipt.getHeight(), status -> {
                if (status == PrinterStatus.OK.getCode()) {
                    if (endFeed){
                        try {
                            mPrinter.feedPaper();
                        } catch (NSDKException e) {
                            LoggerUtils.e("Auto Feed failed.",e);
                        }
                    }
                    LoggerUtils.i("[NSDK Printer]--print completion.");
                    if (callback != null) {
                        callback.onFinish();
                    } else {
                        LoggerUtils.d("[NSDK Printer]--No callback");
                    }
                } else {
                    String statusMsg = getStatusDescription(status);
                    LoggerUtils.e("[NSDK Printer]--print failed,because of "+statusMsg+", status is "+ status);
                    if (callback != null) {
                        if (status == PrinterStatus.NO_PAPER.getCode()){
                            callback.onOutOfPaper();
                        }else{
                            callback.onError(statusMsg);
                        }
                    } else {
                        LoggerUtils.d("[NSDK Printer]--No callback");
                    }
                }
            });
        } catch (NSDKException e) {
            LoggerUtils.e("printImage failed!",e);
            if (callback != null) {
                callback.onError(e.getMessage());
            }
        }
    }


    @Override
    public void cutPaper(){
        try {
            mPrinter.cutPaper();
        } catch (NSDKException e) {
            LoggerUtils.e("Printer.cutPaper failed!",e);
        }
    }
    public void setGray(@IntRange(from = 1,to = 10) int gray){
        try {
            mPrinter.setGray(gray);
        } catch (NSDKException e) {
            LoggerUtils.e("Printer.setGray failed!",e);
        }
    }

}
