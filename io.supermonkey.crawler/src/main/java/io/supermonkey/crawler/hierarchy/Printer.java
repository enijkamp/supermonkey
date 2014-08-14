package io.supermonkey.crawler.hierarchy;

import org.apache.commons.lang.StringUtils;

import java.io.PrintStream;
import java.util.Map;

/**
 * @author Erik Nijkamp (erik.nijkamp@gmail.com)
 * @since 30.05.14
 */
public class Printer {

	private final PrintStream out;

	public Printer(PrintStream out) {
		this.out = out;
	}

	public void print(Node node) {
		print(node, out, 0);
	}

	private void print(Node node, PrintStream out, int level) {
		String element = toString(node.getElement());
		out.println(indent(level) + element);
		for(Node child : node.getChilds()) {
			print(child, out, level + 1);
		}
	}

	private String indent(int level) {
		return StringUtils.repeat(" ", level * 2);
	}

	private String toString(Element element) {
		return element.getType().name().toLowerCase() + " {" + toString(element.getAttributes()) + "}";
	}

	private String toString(Map<String, String> map) {
		String result = " ";
		for(Map.Entry<String, String> entry : map.entrySet()) {
			result += entry.getKey() + "='" + entry.getValue() + "' ";
		}

		return result;
	}

}
