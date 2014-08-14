package io.supermonkey.crawler.hierarchy;

/**
 * @author Erik Nijkamp (erik.nijkamp@gmail.com)
 * @since 04.06.14
 */
public interface Validators {

	interface Validator1<T1> {

		boolean isValid(T1 a1);

	}

	interface Validator2<T1, T2> {

		boolean isValid(T1 a1, T2 a2);

	}
}
