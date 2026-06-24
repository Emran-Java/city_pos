package acquire.base.utils.iso8583;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Xml format file node
 */
public class XmlNode {

    private String name ;
    private Map<String, String> attrs ;
    private String text ;
    private List<XmlNode> children ;
    private int level;
    private XmlNode parent;

    /**
     * Get node data array by node name
     *
     * @param name node name
     * @return node data array
     */
    public XmlNode[] getChildren(String name) {
        List<XmlNode> list = new ArrayList<>();

        if (children == null) {
            return  list.toArray(new XmlNode[0]);
        }

        for (XmlNode node : children) {
            if (name.equals(node.name)) {
                list.add(node);
            }
        }

        return  list.toArray(new XmlNode[0]);
    }

    /**
     * Get a node data by node name
     *
     * @param name node name
     * @return node data；
     */
    public XmlNode getChild(String name) {
        if (children == null){
            return null;
        }
        for (XmlNode node : children) {
            if (name.equals(node.name)) {
                return node;
            }
        }

        return null;
    }

    /**
     * Parse xml
     *
     * @param ins xml file {@link InputStream}
     */
    public void decodeXml(InputStream ins) {
        try {
            XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
            parser.setInput(ins, StandardCharsets.UTF_8.name());

            int eventType = parser.getEventType();

            int attrsCount ;
            XmlNode parentNode = null;
            XmlNode currentNode = this;

            while (XmlPullParser.END_DOCUMENT != eventType) {
                switch (eventType) {
                    //case XmlPullParser.START_DOCUMENT:
                    case XmlPullParser.START_TAG:
                        if (null == currentNode) {
                            currentNode = new XmlNode();
                        }
                        currentNode.name = parser.getName();

                        attrsCount = parser.getAttributeCount();
                        if (attrsCount > 0) {
                            currentNode.attrs = new HashMap<>();
                            for (int index = 0; index < attrsCount; index++) {
                                currentNode.attrs.put(parser.getAttributeName(index),
                                        parser.getAttributeValue(index));
                            }
                        }

                        if (parentNode != null) {
                            parentNode.children.add(currentNode);
                            currentNode.level = parentNode.level + 1;
                            currentNode.parent = parentNode;
                        }

                        try {
                            currentNode.text = parser.nextText();
                            currentNode = null;
                        } catch (Exception e) {
                            currentNode.children = new ArrayList<>();
                            parentNode = currentNode;
                            currentNode = null;
                            continue;
                        }

                        break;

                    case XmlPullParser.END_TAG:
                        if (currentNode != null) {
                            //never do this
                            parentNode = currentNode.parent;
                            currentNode = null;
                        } else if (parentNode != null) {
                            parentNode = parentNode.parent;
                        }
                        break;
                    default:
                        break;
                    //case XmlPullParser.END_DOCUMENT:

                    //break;
                }
                //int i = XmlPullParser.ENTITY_REF;
                eventType = parser.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (ins != null) {
                    ins.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * Parse xml by file
     *
     * @param xmlFile file path
     */
    public void decodeXml(String xmlFile) {
        try {
            decodeXml(new FileInputStream(xmlFile));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public Map<String, String> getAttrs() {
        return attrs;
    }

    @Override
    public String toString() {
        return "XmlNode{" +
                "name='" + name + '\'' +
                ", attrs=" + attrs +
                ", text='" + text + '\'' +
                ", children=" + children +
                ", level=" + level +
                ", parent=" + parent +
                '}';
    }
}
