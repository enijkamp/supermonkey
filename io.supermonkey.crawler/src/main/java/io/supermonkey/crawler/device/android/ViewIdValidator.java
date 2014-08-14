package io.supermonkey.crawler.device.android;

import io.supermonkey.crawler.device.App;
import io.supermonkey.crawler.device.View;
import io.supermonkey.crawler.hierarchy.Validators;

/**
 * @author Erik Nijkamp (erik.nijkamp@gmail.com)
 * @since 04.06.14
 */
public class ViewIdValidator implements Validators.Validator2<View.Id, App.Handle> {

	@Override
	public boolean isValid(View.Id viewId, App.Handle appHandle) {
		AndroidDevice.AppHandle androidAppHandle = (AndroidDevice.AppHandle) appHandle;
		AndroidDevice.ViewId androidViewId = (AndroidDevice.ViewId) viewId;

		String appPackage = androidAppHandle.packageHandle.packageName;
		String currentPackage = androidViewId.packageName;

		return appPackage.equals(currentPackage);
	}

}
