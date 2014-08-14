package io.supermonkey.crawler.device.android;

import io.supermonkey.crawler.hierarchy.Node;
import io.supermonkey.crawler.hierarchy.Element;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Erik Nijkamp (erik.nijkamp@gmail.com)
 * @since 30.05.14
 */
public class UiDumpXmlParser {

	public Node parse(String xml) {

		Document doc = parseXml(xml);
		org.w3c.dom.Node sourceNode = doc.getDocumentElement();
		Node targetNode = transform(sourceNode);

		return targetNode;
	}

	private static Node transform(org.w3c.dom.Node sourceNode) {
		NamedNodeMap sourceAttributes = sourceNode.getAttributes();
		NodeList sourceChilds = sourceNode.getChildNodes();

		Map<String, String> attributes = toAttributes(sourceAttributes);
		Element.Type type = toType(attributes.get("class"));
		boolean isScrollable = "true".equals(attributes.get("scrollable"));
		boolean isClickable = "true".equals(attributes.get("clickable"));

		Element targetElement = new Element(attributes, type, isScrollable, isClickable);
		List<Node> targetChilds = new ArrayList<>(sourceChilds.getLength());
		for(int i = 0; i < sourceChilds.getLength(); i++) {
			org.w3c.dom.Node sourceChild = sourceChilds.item(i);
			Node targetChild = transform(sourceChild);
			targetChilds.add(targetChild);
		}

		return new Node(targetElement, targetChilds);
	}

	private static Element.Type toType(String widgetClass) {
		if("android.widget.TextView".equals(widgetClass)) {
			return Element.Type.TEXT_INPUT_FIELD;
		}

		if("android.widget.Button".equals(widgetClass)) {
			return Element.Type.BUTTON;
		}

		return Element.Type.UNKNOWN;
	}

	private static Map<String, String> toAttributes(NamedNodeMap source) {
		Map<String, String> target = new HashMap<>();
		for(int i = 0; i < source.getLength(); i++) {
			org.w3c.dom.Node item = source.item(i);
			String name = item.getNodeName();
			String value = item.getNodeValue();
			target.put(name, value);
		}
		return target;
	}

	private static String toNodeString(NamedNodeMap attributes) {
		String string = "<node ";
		for(int i = 0; i < attributes.getLength(); i++) {
			string += attributes.item(i) + " ";
		}
		string += ">";

		return string;
	}

	private static Document parseXml(String xml) {
		try {
			return createDocumentBuilder().parse(new InputSource(new ByteArrayInputStream(xml.getBytes("utf-8"))));
		} catch (SAXException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static DocumentBuilder createDocumentBuilder() {
		try {
			DocumentBuilderFactory domFactory =	DocumentBuilderFactory.newInstance();
			domFactory.setNamespaceAware(true);
			return domFactory.newDocumentBuilder();
		} catch(ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
	}

}
