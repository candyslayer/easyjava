package com.easyjava.codegen;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class MapperXmlMerger {

    private final TextThreeWayMerger textMerger = new TextThreeWayMerger();

    public TextThreeWayMerger.MergeTextResult merge(String baseText, String localText, String newText) {
        try {
            Document baseDoc = parse(baseText);
            Document localDoc = parse(localText);
            Document newDoc = parse(newText);

            Element baseRoot = baseDoc.getDocumentElement();
            Element localRoot = localDoc.getDocumentElement();
            Element newRoot = newDoc.getDocumentElement();

            List<String> conflicts = new ArrayList<>();
            String namespace = choose(baseRoot.getAttribute("namespace"), localRoot.getAttribute("namespace"),
                    newRoot.getAttribute("namespace"), conflicts, "xml-namespace-conflict");

            Document resultDoc = parse(newText);
            Element resultRoot = resultDoc.getDocumentElement();
            resultRoot.setAttribute("namespace", namespace);
            clearChildren(resultRoot);

            Map<String, Element> baseNodes = childElements(baseRoot);
            Map<String, Element> localNodes = childElements(localRoot);
            Map<String, Element> newNodes = childElements(newRoot);

            List<String> order = new ArrayList<>(newNodes.keySet());
            for (String key : localNodes.keySet()) {
                if (!order.contains(key) && !baseNodes.containsKey(key)) {
                    order.add(key);
                }
            }

            for (String key : order) {
                Element mergedNode = mergeNode(baseNodes.get(key), localNodes.get(key), newNodes.get(key), key,
                        conflicts, resultDoc);
                if (mergedNode != null) {
                    resultRoot.appendChild(mergedNode);
                }
            }

            return new TextThreeWayMerger.MergeTextResult(toString(resultDoc), conflicts);
        } catch (Exception e) {
            return textMerger.merge(baseText, localText, newText);
        }
    }

    private Element mergeNode(Element baseNode, Element localNode, Element newNode, String key, List<String> conflicts,
            Document resultDoc) throws Exception {
        String base = serialize(baseNode);
        String local = serialize(localNode);
        String newer = serialize(newNode);

        if (equalsText(local, base) && !equalsText(newer, base)) {
            return importNode(resultDoc, newNode);
        }
        if (!equalsText(local, base) && equalsText(newer, base)) {
            return importNode(resultDoc, localNode);
        }
        if (equalsText(local, newer)) {
            return importNode(resultDoc, localNode);
        }
        if (baseNode == null) {
            if (localNode == null) {
                return importNode(resultDoc, newNode);
            }
            if (newNode == null) {
                return importNode(resultDoc, localNode);
            }
            conflicts.add("xml-node-conflict:" + key);
            return importNode(resultDoc, localNode);
        }
        if (localNode == null || newNode == null) {
            conflicts.add("xml-node-delete-conflict:" + key);
            return importNode(resultDoc, localNode != null ? localNode : newNode);
        }

        TextThreeWayMerger.MergeTextResult mergedText = textMerger.merge(base, local, newer);
        if (mergedText.hasConflict()) {
            conflicts.addAll(mergedText.getConflicts());
            return importNode(resultDoc, localNode);
        }
        Document mergedDoc = parse("<mapper>" + mergedText.getMergedText() + "</mapper>");
        Node node = mergedDoc.getDocumentElement().getFirstChild();
        while (node != null && node.getNodeType() != Node.ELEMENT_NODE) {
            node = node.getNextSibling();
        }
        return node == null ? null : (Element) resultDoc.importNode(node, true);
    }

    private String choose(String base, String local, String newer, List<String> conflicts, String conflictMessage) {
        if (equalsText(local, base) && !equalsText(newer, base)) {
            return newer;
        }
        if (!equalsText(local, base) && equalsText(newer, base)) {
            return local;
        }
        if (equalsText(local, newer)) {
            return local;
        }
        conflicts.add(conflictMessage);
        return local;
    }

    private Map<String, Element> childElements(Element root) {
        Map<String, Element> map = new LinkedHashMap<>();
        NodeList children = root.getChildNodes();
        int anonymousIndex = 0;
        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            Element element = (Element) node;
            String id = element.getAttribute("id");
            String key = element.getTagName() + ":" + (id == null || id.isEmpty() ? "_anon_" + anonymousIndex++ : id);
            map.put(key, element);
        }
        return map;
    }

    private void clearChildren(Element root) {
        while (root.hasChildNodes()) {
            root.removeChild(root.getFirstChild());
        }
    }

    private Document parse(String xml) throws Exception {
        String source = xml;
        if (source == null || source.isBlank()) {
            source = "<mapper namespace=\"\"></mapper>";
        }
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        factory.setIgnoringComments(false);
        return factory.newDocumentBuilder().parse(new InputSource(new StringReader(source)));
    }

    private Element importNode(Document resultDoc, Element source) {
        return source == null ? null : (Element) resultDoc.importNode(source, true);
    }

    private String serialize(Element element) throws Exception {
        if (element == null) {
            return "";
        }
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(element), new StreamResult(writer));
        return writer.toString();
    }

    private String toString(Document document) throws Exception {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(document), new StreamResult(writer));
        return writer.toString();
    }

    private boolean equalsText(String left, String right) {
        return normalize(left).equals(normalize(right));
    }

    private String normalize(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\r\n", "\n").replace('\r', '\n').trim();
    }
}
