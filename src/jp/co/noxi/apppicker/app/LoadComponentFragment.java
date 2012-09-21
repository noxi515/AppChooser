package jp.co.noxi.apppicker.app;

import jp.co.noxi.apppicker.content.ComponentHolder;
import jp.co.noxi.apppicker.content.ComponentLoader;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

public class LoadComponentFragment extends Fragment
implements ComponentLoaderHolder, LoaderManager.LoaderCallbacks<ComponentHolder> {

	private ComponentHolder mComponents;

	public LoadComponentFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public Loader<ComponentHolder> onCreateLoader(int id, Bundle args) {
		final Activity activity = getActivity();
		final Intent intent = ((ChooserController) activity).getChooserIntent();
		return new ComponentLoader(activity, intent);
	}

	@Override
	public void onLoadFinished(Loader<ComponentHolder> loader, ComponentHolder components) {
		mComponents = components;

		final ChooserController controller = (ChooserController) getActivity();
		if (controller != null) {
			new Handler().post(new Runnable() {
				@Override
				public void run() {
					if (controller.isChooserDialogShowing()) {
						controller.getChooserDialog().onComponentLoaded(mComponents);
					} else {
						controller.onComponentLoaded(mComponents);
					}
				}
			});
		}
	}

	@Override
	public void onLoaderReset(Loader<ComponentHolder> components) {
	}

	@Override
	public void getComponents(ChooserDialog chooserDialog) {
		if (mComponents != null) {
			chooserDialog.onComponentLoaded(mComponents);
		} else {
			getLoaderManager().restartLoader(0, null, this);
		}
	}

}
