package io.supermonkey.crawler.device.android;

import io.supermonkey.crawler.exceptions.SelectorTranslateException;
import io.supermonkey.crawler.hierarchy.Selector;
import io.supermonkey.crawler.hierarchy.Validators;

import static io.supermonkey.crawler.device.android.JsonSelectorTranslator.Selectors.*;
import static io.supermonkey.crawler.device.android.JsonSelectorTranslator.Attributes.*;

import java.util.Map;

/**
 * @author Erik Nijkamp (erik.nijkamp@gmail.com)
 * @since 31.05.14
 */
public class JsonSelectorTranslator implements Validators.Validator1<Selector> {

	public interface Selectors {
		// static final int SELECTOR_NIL = 0; // nothing.
		/** text(String text) */
		int SELECTOR_TEXT                 = 1;
		/** textStartsWith(String text) */
		int SELECTOR_START_TEXT           = 2;
		/** textContains(String text) */
		int SELECTOR_CONTAINS_TEXT        = 3;
		/** className(String className), className(Class<T> type) */
		int SELECTOR_CLASS                = 4;
		/** description(String desc) */
		int SELECTOR_DESCRIPTION          = 5;
		/** descriptionStartsWith(String desc) */
		int SELECTOR_START_DESCRIPTION    = 6;
		/** descriptionContains(String desc) */
		int SELECTOR_CONTAINS_DESCRIPTION = 7;
		/** index(final int index) */
		int SELECTOR_INDEX                = 8;
		/** instance(final int instance) */
		int SELECTOR_INSTANCE             = 9;
		/** enabled(boolean val) */
		int SELECTOR_ENABLED              = 10;
		/** focused(boolean val) */
		int SELECTOR_FOCUSED              = 11;
		/** focusable(boolean val) */
		int SELECTOR_FOCUSABLE            = 12;
		/** scrollable(boolean val) */
		int SELECTOR_SCROLLABLE           = 13;
		/** clickable(boolean val) */
		int SELECTOR_CLICKABLE            = 14;
		/** checked(boolean val) */
		int SELECTOR_CHECKED              = 15;
		/** selected(boolean val) */
		int SELECTOR_SELECTED             = 16;
		/** packageName(String name) */
		int SELECTOR_PACKAGE_NAME         = 18;
		/** longClickable(boolean val) */
		int SELECTOR_LONG_CLICKABLE       = 24;
		/** textMatches(String regex) */
		int SELECTOR_TEXT_REGEX           = 25;
		/** classNameMatches(String regex) */
		int SELECTOR_CLASS_REGEX          = 26;
		/** descriptionMatches(String regex) */
		int SELECTOR_DESCRIPTION_REGEX    = 27;
		/** packageNameMatches(String regex) */
		int SELECTOR_PACKAGE_NAME_REGEX   = 28;
		/** resourceId(String id) */
		int SELECTOR_RESOURCE_ID          = 29;
		/** checkable(boolean val) */
		int SELECTOR_CHECKABLE            = 30;
		/** resourceIdMatches(String regex) */
		int SELECTOR_RESOURCE_ID_REGEX    = 31;
	}

	public interface Attributes {
		String RESOURCE_ID = "resource-id";
		String PACKAGE = "package";
		String CLASS = "class";
		String TEXT = "text";
		String CONTENT_DESC = "content-desc";
		String INDEX = "index";
	}

	private static class Pair {
		public final int selector;
		public final String value;

		public Pair(int selector, String value) {
			this.selector = selector;
			this.value = value;
		}

		@Override
		public String toString() {
			return "[[" + selector + ",\"" + value + "\"]]";
		}
	}

	private final static String CANNOT_TRANSLATE = null;

	@Override
	public boolean isValid(Selector selector) {
		return doTranslate(selector) != CANNOT_TRANSLATE;
	}

	public String translate(Selector selector) {
		if(!isValid(selector)) {
			throw new SelectorTranslateException();
		}

		return doTranslate(selector);
	}

	private static String doTranslate(Selector selector) {
		Map<String, String> attributes = selector.getPath().getLast().getAttributes();
		Pair resourceId = pair(SELECTOR_RESOURCE_ID, attributes.get(RESOURCE_ID));
		Pair packageName = pair(SELECTOR_PACKAGE_NAME, attributes.get(PACKAGE));
		Pair className = pair(SELECTOR_CLASS, attributes.get(CLASS));
		Pair text = pair(SELECTOR_TEXT, attributes.get(TEXT));
		Pair contentDesc = pair(SELECTOR_CONTAINS_DESCRIPTION, attributes.get(CONTENT_DESC));
		Pair index = pair(SELECTOR_INDEX, attributes.get(INDEX));

		if(has(resourceId)) {
			return selector(resourceId, index);
		}

		if(has(text)) {
			return selector(text, packageName, className, index);
		}

		if(has(contentDesc)) {
			return selector(contentDesc, packageName, className, index);
		}

		// TODO build selector xpath (en)

		return CANNOT_TRANSLATE;
	}

	private static String selector(Pair ... pairs) {
		String result = "";
		for(int i = 0; i < pairs.length - 1; i++) {
			result += pairs[i].toString() + ",";
		}
		result += pairs[pairs.length - 1];

		return "[" + result + "]";
	}

	private static Pair pair(int selector, String value) {
		return new Pair(selector,  value);
	}

	private static boolean has(Pair pair) {
		return (pair.value != null) && ("".equals(pair.value) == false);
	}

}
