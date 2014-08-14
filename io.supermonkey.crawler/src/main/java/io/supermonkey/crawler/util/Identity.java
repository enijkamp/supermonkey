package io.supermonkey.crawler.util;

import com.google.common.base.Objects;
import com.google.common.collect.ComparisonChain;

/**
 * @author Erik Nijkamp (erik.nijkamp@gmail.com)
 * @since 11.06.14
 */
public interface Identity {

	class With1<T1 extends Comparable<T1>> implements Comparable<With1<T1>> {
		private final T1 id1;

		public With1(T1 id1) {
			this.id1 = id1;
		}

		public T1 getId1() {
			return id1;
		}

		@Override
		public boolean equals(Object o) {
			if(o instanceof With1 == false) {
				return false;
			}
			With1<T1> another = (With1<T1>) o;
			return Objects.equal(this.id1, another.id1);
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(this.id1);
		}

		@Override
		public int compareTo(With1<T1> another) {
			return this.getId1().compareTo(another.getId1());
		}

		@Override
		public String toString() {
			return id1.toString();
		}
	}

	class With2<T1 extends Comparable, T2 extends Comparable> implements Comparable<With2<T1, T2>> {
		private final T1 id1;
		private final T2 id2;

		public With2(T1 id1, T2 id2) {
			this.id1 = id1;
			this.id2 = id2;
		}

		public T1 getId1() {
			return id1;
		}

		public T2 getId2() {
			return id2;
		}

		@Override
		public boolean equals(Object o) {
			if(o instanceof With3 == false) {
				return false;
			}
			With2<T1, T2> another = (With2<T1, T2>) o;
			return Objects.equal(this.id1, another.id1) && Objects.equal(this.id2, another.id2);
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(this.id1, this.id2);
		}

		@Override
		public int compareTo(With2<T1, T2> another) {
			return ComparisonChain.start()
					.compare(this.id1, another.id1)
					.compare(this.id2, another.id2)
					.result();
		}

		@Override
		public String toString() {
			return id1.toString() + "/" + id2.toString();
		}
	}

	class With3<T1 extends Comparable, T2 extends Comparable, T3 extends Comparable> implements Comparable<With3<T1, T2, T3>> {
		private final T1 id1;
		private final T2 id2;
		private final T3 id3;

		public With3(T1 id1, T2 id2, T3 id3) {
			this.id1 = id1;
			this.id2 = id2;
			this.id3 = id3;
		}

		public T1 getId1() {
			return id1;
		}

		public T2 getId2() {
			return id2;
		}

		public T3 getId3() {
			return id3;
		}

		@Override
		public boolean equals(Object o) {
			if(o instanceof With3 == false) {
				return false;
			}
			With3<T1, T2, T3> another = (With3<T1, T2, T3>) o;
			return Objects.equal(this.id1, another.id1) && Objects.equal(this.id2, another.id2) && Objects.equal(this.id3, another.id3);
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(this.id1, this.id2, this.id3);
		}

		@Override
		public int compareTo(With3<T1, T2, T3> another) {
			return ComparisonChain.start()
					.compare(this.id1, another.id1)
					.compare(this.id2, another.id2)
					.compare(this.id3, another.id3)
					.result();
		}

		@Override
		public String toString() {
			return id1.toString() + "/" + id2.toString() + "/" + id3.toString();
		}
	}

}
