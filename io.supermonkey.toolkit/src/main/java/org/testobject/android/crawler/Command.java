package org.testobject.android.crawler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.Callable;

/**
 * @author Erik Nijkamp
 * @since 28.05.14
 */
public interface Command extends Callable<Command.Result> {

	class Result {

		public enum Status {
			SUCCESS(0),
			FAILURE(1);

			private final int code;

			Status(int code) {
				this.code = code;
			}

			public int code() {
				return code;
			}
		}

		private final JSONObject json;

		public Result(final Status status, final String val) {
			json = new JSONObject();
			try {
				json.put("status", status.code());
				json.put("value", val);
			} catch (JSONException e) {
				throw new RuntimeException(e);
			}
		}

		public static Result failure(String val) {
			return new Result(Status.FAILURE, val);
		}

		public static Result success(String val) {
			return new Result(Status.SUCCESS, val);
		}

		public static Result success(JSONObject val) {
			return new Result(Status.SUCCESS, val.toString());
		}

		@Override
		public String toString() {
			return json.toString();
		}
	}

}
