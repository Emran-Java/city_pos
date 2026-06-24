package acquire.app.dev_rnd;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.zztl.pos.city.R;
import acquire.base.utils.network.NetworkUtils;

public class TestActivity extends AppCompatActivity {

    private TextView mTvDisplayMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_test);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Window window = getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.status_bar_color));

        View decor = getWindow().getDecorView();
        decor.setSystemUiVisibility(0);

        window.setNavigationBarColor(
                ContextCompat.getColor(this, R.color.nav_bar_color)
        );


        String opName = NetworkUtils.getOperatorName(this);
        Log.d("devTest", "opName: "+opName);

        Button btnClickAction = (Button)findViewById(R.id.btnTestTransection);
        Button btnClickSale = (Button)findViewById(R.id.btnSaleTransection);
        mTvDisplayMessage = (TextView) findViewById(R.id.tvDisplayMessage);

        btnClickAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                testTransaction(BankSocketClient.TEST_TRANSECTION);
            }
        });

        btnClickSale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                testTransaction(BankSocketClient.TEST_SALE_TRANSECTION);
            }
        });


    }

    private void testTransaction(String transectionFor) {
        showMessage("");
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler uiHandler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                byte[] resp = BankSocketClient.sendTestTransaction(transectionFor);
                boolean approved = TestTxnResponseParser.isApproved(resp);

                uiHandler.post(() -> {
                    if (approved) {
                        showMessage("TEST TRANSACTION APPROVED");
                    } else {
                        showMessage("TEST TRANSACTION FAILED");
                    }
                });

            } catch (SocketTimeoutException e) {
                uiHandler.post(() -> showMessage("TIME OUT"));
            } catch (Exception e) {
                uiHandler.post(() -> showMessage("ERROR: " + e.getMessage()));
            }
        });
    }

    private void showMessage(String message){
        Toast.makeText(this, ""+message, Toast.LENGTH_LONG).show();
        Log.d("devTest", "message: "+message);
        mTvDisplayMessage.setText(message);
    }
}