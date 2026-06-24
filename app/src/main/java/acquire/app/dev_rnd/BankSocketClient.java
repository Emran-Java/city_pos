package acquire.app.dev_rnd;

import android.os.Build;
import android.util.Log;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.HexFormat;

public class BankSocketClient {
    // failed to connect to /10.0.1.21 (port 4123)
    private static final String HOST = "10.0.1.21";
    private static final int PORT = 4123;//3050;//1250;
    private static final int TIMEOUT = 5000; // 15 sec

    public static final String TEST_TRANSECTION = "TestTran";
    public static final String TEST_SALE_TRANSECTION = "TestSaleTran";

    public static byte[] sendTestTransaction(String transectionFor) throws Exception {

        Socket socket = new Socket();
        socket.connect(new InetSocketAddress(HOST, PORT), TIMEOUT);
        socket.setSoTimeout(TIMEOUT);

        OutputStream os = socket.getOutputStream();
        InputStream is = socket.getInputStream();


        byte[] request = null;
        switch (transectionFor) {
            case TEST_TRANSECTION: {
                request = TestTransactionBuilder.buildTestTxn();
                break;
            }
            case TEST_SALE_TRANSECTION: {
                request = TestSaleIsoBuilder.buildTestSaleRequest();
                break;
            }
            default: {
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            String hexString = HexFormat.of().formatHex(request);
            Log.d("devTest", transectionFor+" - request Log: " + hexString);

        } else {
            String hexString = bytesToHex(request);
            Log.d("devTest", transectionFor+" - request Log: " + hexString);
        }

        Log.d("devTest", transectionFor+" - request Log: " + request);
        os.write(request);
        os.flush();

        byte[] buffer = new byte[1024];
        int len = is.read(buffer);

        if (len <= 0) {
            throw new SocketTimeoutException("No response from host");
        }

        byte[] response = Arrays.copyOf(buffer, len);

        socket.close();
        Log.d("devTest", transectionFor+" - response Log: " + response);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            String hexString = HexFormat.of().formatHex(response);
            Log.d("devTest", transectionFor+" - response Log: " + hexString);

        } else {
            String hexString = bytesToHex(response);
            Log.d("devTest", transectionFor+" - response Log: " + hexString);
        }

        return response;
    }

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2]; // 2 hex chars per byte
        for (int j = 0; j < bytes.length; j++) {
            // Mask the byte with 0xFF to handle signed bytes correctly and
            // convert to an unsigned integer (0-255 range)
            int v = bytes[j] & 0xFF;

            // Get the high 4 bits (first hex digit) and map to character
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];

            // Get the low 4 bits (second hex digit) and map to character
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

}
