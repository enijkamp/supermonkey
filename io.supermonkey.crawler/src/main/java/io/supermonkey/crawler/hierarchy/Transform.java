package io.supermonkey.crawler.hierarchy;

import java.util.ArrayList;
import java.util.List;

/**
 * Transforms for immutables graphs.
 *
 * @author Erik Nijkamp (erik.nijkamp@gmail.com)
 * @since 18.06.14
 */
public class Transform {

	public interface Mutator {

		Element mutate(Element element);

	}

	public static Node transform(Node source, Mutator mutator) {

		Element targetElement = mutator.mutate(source.getElement());

		List<Node> targetChilds = new ArrayList<Node>(source.getChilds().size());
		for(Node sourceChild : source.getChilds()) {
			targetChilds.add(transform(sourceChild, mutator));
		}

		return new Node(targetElement, targetChilds);

	}

}
