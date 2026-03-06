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
    private final ConflictMarkerRenderer conflictRenderer = new ConflictMarkerRenderer();

    public TextThreeWayMerger.MergeTextResult merge(String baseText, String localText, String newText) {
        try {
            Document baseDoc = parse(baseText);
            Document localDoc = parse(localText);
            Document newDoc = parse(newText);

            Element baseRoot = baseDoc.getDocumentElement();
            Element localRoot = localDoc.getDocumentElement();
            Element newRoot = newDoc.getDocumentElement();

            List<ConflictBlock> conflicts = new ArrayList<>();
            String namespace = choose(baseRoot.getAttribute("namespace"), localRoot.getAttribute("namespace"),
                    newRoot.getAttribute("namespace"), conflicts, "xml-namespace-conflict");

            Map<String, Element> baseNodes = childElements(baseRoot);
            Map<String, Element> localNodes = childElements(localRoot);
            Map<String, Element> newNodes = childElements(newRoot);
            List<String> renderedNodes = new ArrayList<>();

            List<String> order = new ArrayList<>(newNodes.keySet());
            for (String key : localNodes.keySet()) {
                if (!order.contains(key) && !baseNodes.containsKey(key)) {
                    order.add(key);
                }
            }

            for (String key : order) {
                String mergedNode = mergeNode(baseNodes.get(key), localNodes.get(key), newNodes.get(key), key,
                        conflicts);
                if (mergedNode != null && !mergedNode.isBlank()) {
                    renderedNodes.add(mergedNode);
                }
            }

            return new TextThreeWayMerger.MergeTextResult(buildMapperXml(namespace, renderedNodes), conflicts);
        } catch (Exception e) {
            return textMerger.merge(baseText, localText, newText);
        }
    }

    private String mergeNode(Element baseNode, Element localNode, Element newNode, String key, List<ConflictBlock> conflicts)
            throws Exception {
        String base = serialize(baseNode);
        String local = serialize(localNode);
        String newer = serialize(newNode);

        if (equalsText(local, base) && !equalsText(newer, base)) {
            return newer;
        }
        if (!equalsText(local, base) && equalsText(newer, base)) {
            return local;
        }
        if (equalsText(local, newer)) {
            return local;
        }
        if (baseNode == null) {
            if (localNode == null) {
                return newer;
            }
            if (newNode == null) {
                return local;
            }
            ConflictBlock conflict = buildConflictBlock(key, base, local, newer, "XML 节点在 Local 与 New 中同时新增且内容不同");
            conflicts.add(conflict);
            return renderConflict(conflict);
        }
        if (localNode == null || newNode == null) {
            ConflictBlock conflict = buildConflictBlock(key, base, local, newer, "XML 节点出现删除/修改冲突");
            conflicts.add(conflict);
            return renderConflict(conflict);
        }

        TextThreeWayMerger.MergeTextResult mergedText = textMerger.merge(base, local, newer);
        if (mergedText.hasConflict()) {
            ConflictBlock conflict = buildConflictBlock(key, base, local, newer, "XML 节点内部内容冲突");
            conflicts.add(conflict);
            return renderConflict(conflict);
        }
        return mergedText.getMergedText();
    }

    private String choose(String base, String local, String newer, List<ConflictBlock> conflicts, String conflictMessage) {
        if (equalsText(local, base) && !equalsText(newer, base)) {
            return newer;
        }
        if (!equalsText(local, base) && equalsText(newer, base)) {
            return local;
        }
        if (equalsText(local, newer)) {
            return local;
        }
        conflicts.add(new ConflictBlock("mapper:namespace", base, local, newer, "", CodegenFileType.MAPPER_XML,
                "Mapper namespace 冲突: " + conflictMessage));
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

    private boolean equalsText(String left, String right) {
        return normalize(left).equals(normalize(right));
    }

    private String normalize(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\r\n", "\n").replace('\r', '\n').trim();
    }

    private ConflictBlock buildConflictBlock(String key, String base, String local, String newer, String reason) {
        return new ConflictBlock(key, base, local, newer, "", CodegenFileType.MAPPER_XML, reason);
    }

    private String renderConflict(ConflictBlock block) {
        return conflictRenderer.render(block, "    ");
    }

    private String buildMapperXml(String namespace, List<String> nodes) {
        String lineSeparator = System.lineSeparator();
        StringBuilder builder = new StringBuilder();
        builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>").append(lineSeparator);
        builder.append("<mapper namespace=\"").append(namespace == null ? "" : namespace).append("\">")
                .append(lineSeparator);
        for (String node : nodes) {
            String normalized = node.replace("\r\n", "\n").replace('\r', '\n').trim();
            if (normalized.isEmpty()) {
                continue;
            }
            String[] lines = normalized.split("\n", -1);
            for (String line : lines) {
                builder.append("    ").append(line).append(lineSeparator);
            }
            builder.append(lineSeparator);
        }
        builder.append("</mapper>").append(lineSeparator);
        return builder.toString();
    }
}
