package acquire.base.utils.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

import acquire.base.utils.LoggerUtils;

/**
 * Socket Test
 *
 * @author Janson
 * @date 2025/3/17 11:23
 */
public class SocketTest {
    public static void test()  {
        String host = "test.rebex.net";
        int port = 21;
        LoggerUtils.e("SocketTest start!");
        try (Socket socket = new Socket(host, port);){
            InputStream inputStream = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String response = reader.readLine();
            LoggerUtils.e("SocketTest result:"+ response);
        } catch (IOException e) {
            LoggerUtils.e("SocketTest failed!",e);
        }
    }
} 
