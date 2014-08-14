package io.supermonkey.crawler.hierarchy;

import com.google.common.collect.Ordering;
import io.supermonkey.crawler.util.Identity;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Erik Nijkamp (erik.nijkamp@gmail.com)
 * @since 28.05.14
 */
public class Element {

	public static class Id extends Identity.With1<String> {
		public Id(String id) {
			super(id);
		}

		public static Id from(Map<String, String> attributes) {
			return new Id(toString(attributes));
		}

		private static String toString(Map<String, String> map) {
			String result = "{ ";
			List<String> sortedKeys = Ordering.natural().sortedCopy(map.keySet());
			Iterator<String> iter = sortedKeys.iterator();
			while(iter.hasNext()) {
				String key = iter.next();
				String value = map.get(key);
				if(iter.hasNext()) {
					result += key + "='" + value + "' ";
				} else {
					result += key + "='" + value + "' }";
				}
			}

			return result;
		}
	}

	public enum Type { BUTTON, TEXT_INPUT_FIELD, UNKNOWN }

	private final Id id;
	private final Map<String, String> attributes;
	private final Type type;
	private final boolean isScrollable;
	private final boolean isClickable;

	public Element(Map<String, String> attributes, Type type, boolean isScrollable, boolean isClickable) {
		this.id = Id.from(attributes);
		this.attributes = attributes;
		this.type = type;
		this.isScrollable = isScrollable;
		this.isClickable = isClickable;
	}

	public Id getId() {
		return id;
	}

	public Type getType() {
		return type;
	}

	public boolean isScrollable() {
		return isScrollable;
	}

	public boolean isClickable() {
		return isClickable;
	}

	public Map<String, String> getAttributes() {
		return attributes;
	}
}
