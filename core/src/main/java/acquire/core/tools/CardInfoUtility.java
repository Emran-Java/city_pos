package acquire.core.tools;

import java.util.HashMap;
import java.util.Map;

public class CardInfoUtility {

    private static CardInfoUtility instance;

    private CardInfoUtility() {
        // private constructor
    }

    public static String formatAmount(long input) {
        double value = input / 100.0;
        return String.format("%.2f", value);
    }

    public static CardInfoUtility getInstance() {
        if (instance == null) {
            instance = new CardInfoUtility();
        }
        return instance;
    }


    public String maskCard(String card){

        if(card == null || card.length() < 10) return card;

        return card.substring(0,6)
                +"******"+
                card.substring(card.length()-4);
    }


    public String formatDate(String d){

        if(d == null || d.length() < 8) return d;

        return d.substring(6,8)+"/"+
                d.substring(4,6)+"/"+
                d.substring(0,4);
    }


    public String formatTime(String t){

        if(t == null || t.length() < 6) return t;

        return t.substring(0,2)+":"+
                t.substring(2,4)+":"+
                t.substring(4,6);
    }


    public String formatExp(String e){

        if(e == null || e.length() < 4) return e;

        return e.substring(2,4)+"/"+e.substring(0,2);
    }


    public String getEntryMode(int mode){

        switch(mode){

            case 1: return "NFC";
            case 2: return "CHIP(PIN)";
            case 3: return "MAG";
        }

        return "";
    }

    public static String getCardholderName(String field55Hex, String track1Data) {
        // Step 1: Try from Field 55 (Tag 5F20)
        if (field55Hex != null && !field55Hex.isEmpty()) {
            Map<String, String> tlvMap = parseTLV(field55Hex);
            String hexName = tlvMap.get("5F20");

            if (hexName != null && !hexName.isEmpty()) {
                return hexToAscii(hexName).trim();
            }
        }

        // Step 2: Fallback → Track 1
        if (track1Data != null && !track1Data.isEmpty()) {
            String name = parseTrack1Name(track1Data);
            if (name != null && !name.isEmpty()) {
                return name.trim();
            }
        }

        return null;
    }

    public static String parseTrack1Name(String track1) {
        try {
            if (track1.contains("^")) {
                String[] parts = track1.split("\\^");
                if (parts.length > 1) {
                    String name = parts[1]; // DOE/JOHN

                    // Optional: format change → JOHN DOE
                    if (name.contains("/")) {
                        String[] nameParts = name.split("/");
                        if (nameParts.length == 2) {
                            return nameParts[1] + " " + nameParts[0];
                        }
                    }
                    return name;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Map<String, String> parseTLV(String tlv) {
        Map<String, String> map = new HashMap<>();
        int i = 0;

        while (i < tlv.length()) {
            String tag = tlv.substring(i, i + 2);
            i += 2;

            // Handle 2-byte tag (like 5F20)
            if ((Integer.parseInt(tag, 16) & 0x1F) == 0x1F) {
                tag += tlv.substring(i, i + 2);
                i += 2;
            }

            int length = Integer.parseInt(tlv.substring(i, i + 2), 16);
            i += 2;

            String value = tlv.substring(i, i + (length * 2));
            i += (length * 2);

            map.put(tag, value);
        }

        return map;
    }

    public static String hexToAscii(String hex) {
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < hex.length(); i += 2) {
            String str = hex.substring(i, i + 2);
            output.append((char) Integer.parseInt(str, 16));
        }
        return output.toString();
    }


}