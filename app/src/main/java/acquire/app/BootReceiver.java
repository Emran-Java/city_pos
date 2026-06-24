package acquire.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // ডিভাইস পুরোপুরি অন হয়েছে কিনা চেক করা
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()) ||
                "android.intent.action.QUICKBOOT_POWERON".equals(intent.getAction())) {

            // MainActivity ওপেন করার জন্য একটি ইন্টেন্ট তৈরি
            Intent i = new Intent(context, MainActivity.class);

            // ব্যাকগ্রাউন্ড থেকে অ্যাক্টিভিটি চালু করার জন্য এই ফ্ল্যাগটি দেওয়া বাধ্যতামূলক
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            // অ্যাপটি স্বয়ংক্রিয়ভাবে ওপেন করা হলো
            context.startActivity(i);
        }
    }
}