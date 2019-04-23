/**
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.reactcommunity.rndatetimepicker;

import android.app.DatePickerDialog.OnDateSetListener;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import android.widget.DatePicker;

import com.facebook.react.bridge.*;
import com.facebook.react.common.annotations.VisibleForTesting;
import com.facebook.react.module.annotations.ReactModule;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * {@link NativeModule} that allows JS to show a native date picker dialog and get called back when
 * the user selects a date.
 */
@ReactModule(name = RNDatePickerDialogModule.FRAGMENT_TAG)
public class RNDatePickerDialogModule extends ReactContextBaseJavaModule {

  @VisibleForTesting
  public static final String FRAGMENT_TAG = "RNDatePickerAndroid";

  public RNDatePickerDialogModule(ReactApplicationContext reactContext) {
    super(reactContext);
  }

  @Override
  public @Nonnull String getName() {
    return RNDatePickerDialogModule.FRAGMENT_TAG;
  }

  private class DatePickerDialogListener implements OnDateSetListener, OnDismissListener {

    private final Promise mPromise;
    private boolean mPromiseResolved = false;

    public DatePickerDialogListener(final Promise promise) {
      mPromise = promise;
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
      if (!mPromiseResolved && getReactApplicationContext().hasActiveCatalystInstance()) {
        WritableMap result = new WritableNativeMap();
        result.putString("action", RNConstants.ACTION_DATE_SET);
        result.putInt("year", year);
        result.putInt("month", month);
        result.putInt("day", day);
        mPromise.resolve(result);
        mPromiseResolved = true;
      }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
      if (!mPromiseResolved && getReactApplicationContext().hasActiveCatalystInstance()) {
        WritableMap result = new WritableNativeMap();
        result.putString("action", RNConstants.ACTION_DISMISSED);
        mPromise.resolve(result);
        mPromiseResolved = true;
      }
    }
  }

  /**
   * Show a date picker dialog.
   *
   * @param options a map containing options. Available keys are:
   *
   * <ul>
   *   <li>{@code date} (timestamp in milliseconds) the date to show by default</li>
   *   <li>
   *     {@code minimumDate} (timestamp in milliseconds) the minimum date the user should be allowed
   *     to select
   *   </li>
   *   <li>
   *     {@code maximumDate} (timestamp in milliseconds) the maximum date the user should be allowed
   *     to select
   *    </li>
   *   <li>
   *      {@code display} To set the date picker display to 'calendar/spinner/default'
   *   </li>
   * </ul>
   *
   * @param promise This will be invoked with parameters action, year,
   *                month (0-11), day, where action is {@code dateSetAction} or
   *                {@code dismissedAction}, depending on what the user did. If the action is
   *                dismiss, year, month and date are undefined.
   */
  @ReactMethod
  public void open(@Nullable final ReadableMap options, Promise promise) {
    FragmentActivity activity = (FragmentActivity) getCurrentActivity();
    if (activity == null) {
      promise.reject(
          RNConstants.ERROR_NO_ACTIVITY,
          "Tried to open a DatePicker dialog while not attached to an Activity");
      return;
    }

    FragmentManager fragmentManager = activity.getSupportFragmentManager();
    final RNDatePickerDialogFragment oldFragment = (RNDatePickerDialogFragment) fragmentManager.findFragmentByTag(FRAGMENT_TAG);

    if (oldFragment != null && options != null) {
      UiThreadUtil.runOnUiThread(new Runnable() {
        @Override
        public void run() {
          oldFragment.update(createFragmentArguments(options));
        }
      });

      return;
    }

    RNDatePickerDialogFragment fragment = new RNDatePickerDialogFragment();

    if (options != null) {
      fragment.setArguments(createFragmentArguments(options));
    }

    final DatePickerDialogListener listener = new DatePickerDialogListener(promise);
    fragment.setOnDismissListener(listener);
    fragment.setOnDateSetListener(listener);
    fragment.show(fragmentManager, FRAGMENT_TAG);
  }

  private Bundle createFragmentArguments(ReadableMap options) {
    final Bundle args = new Bundle();
    if (options.hasKey(RNConstants.ARG_VALUE) && !options.isNull(RNConstants.ARG_VALUE)) {
      args.putLong(RNConstants.ARG_VALUE, (long) options.getDouble(RNConstants.ARG_VALUE));
    }
    if (options.hasKey(RNConstants.ARG_MINDATE) && !options.isNull(RNConstants.ARG_MINDATE)) {
      args.putLong(RNConstants.ARG_MINDATE, (long) options.getDouble(RNConstants.ARG_MINDATE));
    }
    if (options.hasKey(RNConstants.ARG_MAXDATE) && !options.isNull(RNConstants.ARG_MAXDATE)) {
      args.putLong(RNConstants.ARG_MAXDATE, (long) options.getDouble(RNConstants.ARG_MAXDATE));
    }
    if (options.hasKey(RNConstants.ARG_DISPLAY) && !options.isNull(RNConstants.ARG_DISPLAY)) {
      args.putString(RNConstants.ARG_DISPLAY, options.getString(RNConstants.ARG_DISPLAY));
    }
    return args;
  }
}
