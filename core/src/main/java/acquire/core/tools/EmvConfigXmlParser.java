package acquire.core.tools;

import android.content.Context;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import acquire.base.utils.BytesUtils;
import acquire.base.utils.LoggerUtils;
import acquire.base.utils.TlvUtils;
import acquire.sdk.emv.IEmvParamLoader;
import acquire.sdk.emv.bean.CapkBean;
import acquire.sdk.emv.bean.EmvCustomTagBean;
import acquire.sdk.emv.bean.EmvDrlTagBean;

/**
 * Emv aid or capk configuration xml file parse
 *
 * @author Janson
 * @date 2021/11/4 9:45
 */
public class EmvConfigXmlParser {
    /**
     * load evm configuration xml.
     *
     * @param xmlAssetFile xml file
     * @param loader       EMV AID or Capk loader
     * @return xml elements
     */
    public static boolean parseXml(Context context, String xmlAssetFile, IEmvParamLoader loader) {
        try (InputStream is = context.getAssets().open(xmlAssetFile)) {
            return parseXml(is, loader);
        }catch (IOException e){
            LoggerUtils.e("parseXml failed!",e);
            return false;
        }
    }

    /**
     * load evm configuration xml.
     *
     * @param is     xml file input stream
     * @param loader EMV AID or Capk loader
     * @return xml elements
     */
    public static boolean parseXml(InputStream is, IEmvParamLoader loader) {
        loader.clearCtAid();
        loader.clearClessAid();
        loader.clearCapk();
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            Document dom = factory.newDocumentBuilder().parse(is);
            Element root = dom.getDocumentElement();
            //start to parse xml nodes
            //NODE: config
            NodeList configs = root.getChildNodes();
            for (int i = 0; i < configs.getLength(); i++) {
                Node config = configs.item(i);
                if (!(config instanceof Element)) {
                    continue;
                }
                String configName = ((Element) config).getAttribute("name");
                //NODE: entry
                NodeList entries = config.getChildNodes();
                for (int j = 0; j < entries.getLength(); j++) {
                    Node entry = entries.item(j);
                    if (!(entry instanceof Element)) {
                        continue;
                    }
                    String entryName = ((Element) entry).getAttribute("name");
                    //NODE: item
                    NodeList items = entry.getChildNodes();
                    if ("CONTACT".equals(configName) || "CONTACTLESS".equals(configName)) {
                        //Load AID
                        TlvUtils.PackTlv pack = TlvUtils.newPackTlv();
                        for (int k = 0; k < items.getLength(); k++) {
                            Node item = items.item(k);
                            if (!(item instanceof Element)) {
                                continue;
                            }
                            String itemNode = item.getNodeName();
                            if (itemNode == null) {
                                continue;
                            }
                            switch (itemNode) {
                                case "item": {
                                    //aid standard tag
                                    String tag = ((Element) item).getAttribute("tag");
                                    String value = ((Element) item).getAttribute("value");
                                    int nTag = Integer.parseInt(tag, 16);
                                    byte[] bValue = BytesUtils.hexToBytes(value);
                                    pack.append(nTag, bValue);
                                }
                                break;
                                case "DRL": {
                                    //DRL tags
                                    NodeList nodeList = item.getChildNodes();
                                    EmvDrlTagBean emvDrlTagBean = new EmvDrlTagBean();
                                    for (int m = 0; m < nodeList.getLength(); m++) {
                                        Node cNode = nodeList.item(m);
                                        if (!(cNode instanceof Element)) {
                                            continue;
                                        }

                                        String tag = ((Element) cNode).getAttributeNode("key").getValue();
                                        String value = ((Element) cNode).getAttributeNode("value").getValue();
                                        if (tag != null) {
                                            switch (tag.toLowerCase()) {
                                                case "drl exist":
                                                    //AMEX
                                                    emvDrlTagBean.setDrlExist(Integer.parseInt(value, 16));
                                                    break;
                                                case "drl id":
                                                    //AMEX
                                                    emvDrlTagBean.setDrlId(Integer.parseInt(value, 16));
                                                    break;
                                                case "limist exist":
                                                    //AMEX
                                                    emvDrlTagBean.setLimitExist(Integer.parseInt(value, 16));
                                                    break;
                                                case "clss transaction limit":
                                                    emvDrlTagBean.setClTransLimit(BytesUtils.hexToBytes(value));
                                                    break;
                                                case "clss offline limit":
                                                    emvDrlTagBean.setClOfflineLimit(BytesUtils.hexToBytes(value));
                                                    break;
                                                case "cvm limit":
                                                    emvDrlTagBean.setCvmLimit(BytesUtils.hexToBytes(value));
                                                    break;
                                                case "app id":
                                                    //VISA Paywave
                                                    emvDrlTagBean.setAppId(BytesUtils.hexToBytes(value));
                                                    break;
                                                case "cllimitexist":
                                                    //VISA Paywave
                                                    emvDrlTagBean.setClLimitExist(Integer.parseInt(value, 16));
                                                    break;
                                                default:
                                                    break;
                                            }
                                        }
                                    }
                                    if (((Element) entry).getAttribute("name").contains("AMEX")) {
                                        //AMEX
                                        emvDrlTagBean.setAmex(true);
                                        pack.append(0XDF53, emvDrlTagBean.toByteArray());
                                    } else {
                                        //VISA
                                        pack.append(0XDF3F, emvDrlTagBean.toByteArray());
                                    }
                                }
                                break;
                                case "CUSTOM": {
                                    //CUSTOM node is used to let the EMV kernel to fetch tags outside of the standard
                                    NodeList nodeList = item.getChildNodes();
                                    EmvCustomTagBean emvCustomTagBean = new EmvCustomTagBean();
                                    for (int m = 0; m < nodeList.getLength(); m++) {
                                        Node cNode = nodeList.item(m);
                                        if (!(cNode instanceof Element)) {
                                            continue;
                                        }
                                        String tag = ((Element) cNode).getAttributeNode("key").getValue();
                                        String value = ((Element) cNode).getAttributeNode("value").getValue();
                                        if (tag != null) {
                                            switch (tag.toLowerCase()) {
                                                case "tag name":
                                                    emvCustomTagBean.setTag(Integer.parseInt(value, 16));
                                                    break;
                                                case "template1":
                                                    emvCustomTagBean.setTemplate1(Integer.parseInt(value, 16));
                                                    break;
                                                case "template2":
                                                    emvCustomTagBean.setTemplate2(Integer.parseInt(value, 16));
                                                    break;
                                                case "source":
                                                    emvCustomTagBean.setSource(Integer.parseInt(value, 16));
                                                    break;
                                                case "format":
                                                    emvCustomTagBean.setFormat(Integer.parseInt(value, 16));
                                                    break;
                                                case "min lenth":
                                                    emvCustomTagBean.setMinLen(Integer.parseInt(value, 16));
                                                    break;
                                                case "max lenth":
                                                    emvCustomTagBean.setMaxLen(Integer.parseInt(value, 16));
                                                    break;
                                                default:
                                                    break;
                                            }
                                        }
                                    }

                                    pack.append(0X1F811F, emvCustomTagBean.toByteArray());
                                }
                                break;
                                default:
                                    LoggerUtils.e("unknow node: " + itemNode);
                                    break;
                            }
                        }
                        byte[] aidTlvs = pack.pack();
                        String strTlv = BytesUtils.bcdToString(aidTlvs);
                        boolean isTerminalEntry = "Terminal Configuration".equals(entryName);
                        if ("CONTACT".equals(configName)) {
                            boolean success = loader.loadCtAid(strTlv, isTerminalEntry);
                            if (!success) {
                                return false;
                            }
                        } else {
                            boolean success = loader.loadClessAid(strTlv, isTerminalEntry);
                            if (!success) {
                                return false;
                            }
                        }

                    } else if ("PublicKeys".equals(configName)) {
                        //Load capk
                        CapkBean capkBean = new CapkBean();
                        for (int k = 0; k < items.getLength(); k++) {
                            Node item = items.item(k);
                            if (!(item instanceof Element)) {
                                continue;
                            }
                            String key = ((Element) item).getAttribute("key");
                            String value = ((Element) item).getAttribute("value");
                            switch (key) {
                                case "RID":
                                    capkBean.setRid(value);
                                    break;
                                case "Index":
                                    capkBean.setIndex(Integer.parseInt(value, 16));
                                    break;
                                case "Hash":
                                    capkBean.setHash(value);
                                    break;
                                case "Exponent":
                                    capkBean.setExponent(value);
                                    break;
                                case "Modulus":
                                    capkBean.setModulus(value);
                                    break;
                                case "Hash Algorithm":
                                    capkBean.setHashAlgorithm(Integer.parseInt(value));
                                    break;
                                case "Sign Algorithm":
                                    capkBean.setAlgorithmIndicator(Integer.parseInt(value));
                                    break;
                                default:
                                    LoggerUtils.e("unknown key: " + key);
                                    break;
                            }
                        }
                        boolean success = loader.loadCapk(capkBean);
                        if (!success) {
                            return false;
                        }
                    }
                }
            }
            return true;
        } catch (ParserConfigurationException | SAXException | IOException e) {
            LoggerUtils.e("parsed xml  failed.", e);
            return false;
        }
    }
}

