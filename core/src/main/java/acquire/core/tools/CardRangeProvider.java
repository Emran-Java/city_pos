package acquire.core.tools;

import android.text.TextUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import acquire.base.BaseApplication;
import acquire.base.utils.LoggerUtils;
import acquire.base.utils.StringUtils;
import acquire.core.constant.CardSchemes;
import acquire.core.constant.FileConst;

/**
 * Card bin utils
 *
 * @author Janson
 * @date 2022/7/19 17:28
 */
public class CardRangeProvider {
    private final static List<String[]> CARD_RANGES = new ArrayList<>();

    /**
     * Init
     */
    private static void init() {
        //load card bin file
        try (InputStream is = BaseApplication.getAppContext().getAssets().open(FileConst.CARD_RANGE)) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dombuild = factory.newDocumentBuilder();
            Document dom = dombuild.parse(is);
            Element root = dom.getDocumentElement();
            //Node: card scheme
            NodeList nodes = root.getChildNodes();
            for (int i = 0; i < nodes.getLength(); i++) {
                Node item = nodes.item(i);
                if (!(item instanceof Element)) {
                    continue;
                }
                String scheme = ((Element) item).getAttribute("scheme");
                //must match CardSchemes.class
                if (!validScheme(scheme)){
                    LoggerUtils.e("invalid CardRange scheme: "+scheme);
                    continue;
                }
                NodeList cardNodes = item.getChildNodes();
                for (int j = 0; j < cardNodes.getLength(); j++) {
                    Node cardNode = cardNodes.item(j);
                    if (!(cardNode instanceof Element)) {
                        continue;
                    }
                    String start = ((Element) cardNode).getAttribute("start").trim();
                    String end = ((Element) cardNode).getAttribute("end").trim();
                    CARD_RANGES.add(new String[]{start, end, scheme});
                }
            }
        } catch (IOException | SAXException | ParserConfigurationException e) {
            LoggerUtils.e("CardRangeProvider init failed!",e);
        }
    }

    private static boolean validScheme(String scheme){
        if (TextUtils.isEmpty(scheme)){
            return false;
        }
        for (Field field : CardSchemes.class.getDeclaredFields()) {
            int modifiers = field.getModifiers();
            if ((Modifier.FINAL & modifiers) != 0 && (Modifier.STATIC & modifiers) != 0&& (Modifier.PUBLIC & modifiers) != 0) {
                try {
                    if (scheme.equals(field.get(CardSchemes.class))){
                        return true;
                    }
                } catch (IllegalAccessException e) {
                    LoggerUtils.e("CardRangeProvider validScheme failed!",e);
                }
            }
        }
        return false;
    }

    /**
     * get card scheme
     *
     * @param pan card number
     * @return card scheme {@link CardSchemes}
     */
    public static String getCardScheme(String pan) {
        if (CARD_RANGES.isEmpty()) {
            init();
        }
        for (String[] cardBin : CARD_RANGES) {
            String start = cardBin[0];
            String end = cardBin[1];
            if (belong(start, end, pan)) {
                return cardBin[2];
            }
        }
        return null;
    }

    private static boolean belong(String start, String end, String pan) {
        if (TextUtils.isEmpty(end)) {
            return pan.startsWith(start);
        } else {
            try {
                int maxLen = Math.max(start.length(), end.length());
                start = StringUtils.fill(start, "0", maxLen, false);
                end = StringUtils.fill(end, "0", maxLen, false);
                BigInteger nStart=new BigInteger(start);
                BigInteger nEnd=new BigInteger(end);
                BigInteger nPan;
                if (pan.length() > maxLen) {
                    nPan = new BigInteger(pan.substring(0, maxLen));
                } else {
                    nPan = new BigInteger(StringUtils.fill(pan, "0", maxLen, false));
                }
                if (nPan.compareTo(nStart) > 0 && nPan.compareTo(nEnd) < 0) {
                    return true;
                }
            } catch (NumberFormatException e) {
                LoggerUtils.e("CardRangeProvider belong failed!",e);
            }
            return false;
        }

    }

}
