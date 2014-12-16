package com.isobar.jmann.acceleratedpickupdropoff;

/*
 * Copyright (c) 2014. McDonald's. All rights reserved.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 */
public class UIUtils {

    public static final String TAG = UIUtils.class.getSimpleName();
    private final static ThreadLocal<SimpleDateFormat> DATE_FORMAT_MONTH_DAY_YEAR = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("MM/dd/yy");
        }
    };
    private final static ThreadLocal<SimpleDateFormat> ISO8601_FORMAT = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        }
    };

    private final static ThreadLocal<SimpleDateFormat> DELIVERY_DATE_FORMATTER_HOURS =
            new ThreadLocal<SimpleDateFormat>() {
                @Override
                protected SimpleDateFormat initialValue() {
                    return new SimpleDateFormat("h:mm");
                }
            };

    private final static ThreadLocal<SimpleDateFormat> DELIVERY_DATE_FORMATTER_DAY_HOUR =
            new ThreadLocal<SimpleDateFormat>() {
                @Override
                protected SimpleDateFormat initialValue() {
                    return new SimpleDateFormat("MMM d, h:mm");
                }
            };

    private static ProgressDialog mProgressDialog = null;
    private static boolean mProgressDisplayed = false;

    public static int dpAsPixels(final Context context, final int dpValue) {
        if (context == null) {
            return 0;
        }
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int topOffset(Context context, View globalView) {

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity)context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int topOffset = displayMetrics.heightPixels - globalView.getMeasuredHeight();

        return topOffset;
    }

    public static int getDrawableIdByName(final Context context, final String pDrawableName) {
        return context.getResources().getIdentifier(pDrawableName, "drawable", "com.mcdonalds.app");
    }

    public static int getStringIdByName(final Context context, final String pStringName) {
        return context.getResources().getIdentifier(pStringName, "string", "com.mcdonalds.app");
    }

    public static String getStringByName(final Context context, final String key) {
        if (key != null) {
            // if our key starts with "raw:", we take whatever is after it literally
            if (key.startsWith("raw:")) {
                return key.substring(4);
            }

            // if our key is not "raw:", then we try to find the matching resource
            int resourceId = context.getResources().getIdentifier(key, "string", "com.mcdonalds.app");

            // and if we find it, get the localized version.  Otherwise we just spit the key back
            return resourceId > 0 ? context.getString(resourceId) : key;
        }

        // if key was null, we just return null
        return null;
    }


    public static String formatDeliveryTime(Context context, final Date deliveryDate) {
        if (deliveryDate != null) {

            // determine am/pm
            Calendar etaCalendar = Calendar.getInstance();
            etaCalendar.setTime(deliveryDate);
            String amPm = etaCalendar.get(Calendar.AM_PM) == Calendar.AM ? "am" : "pm";

            // If date is today, show only hours
            Calendar tomorrowStartCalendar = Calendar.getInstance();
            tomorrowStartCalendar.set(Calendar.HOUR, 0);
            tomorrowStartCalendar.set(Calendar.MINUTE, 0);
            tomorrowStartCalendar.set(Calendar.SECOND, 0);
            tomorrowStartCalendar.add(Calendar.DATE, 1);

            if (deliveryDate.before(tomorrowStartCalendar.getTime())) {

                return DELIVERY_DATE_FORMATTER_HOURS.get().format(deliveryDate) + amPm;
            } else {
                return DELIVERY_DATE_FORMATTER_DAY_HOUR.get().format(deliveryDate) + amPm;
            }

        } else {
            new RuntimeException("time not formattable");
        }

        return "unknown";
    }



    public static void startActivityIndicator(Context context, int msgResId) {
        if (context != null)
            startActivityIndicator(context, context.getString(msgResId));
    }

    public static void startActivityIndicator(final Context context, final String message) {
        if (context != null && !mProgressDisplayed) {
            mProgressDialog = new ProgressDialog(context, ProgressDialog.THEME_HOLO_LIGHT);
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setMessage(message);
            mProgressDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    stopActivityIndicator();
                    if (context instanceof Activity) {
                        ((Activity) context).onBackPressed();
                    }
                }
            });

            mProgressDialog.show();
            mProgressDisplayed = true;
        }
    }

    public static void stopActivityIndicator() {
        if (mProgressDisplayed) {
            mProgressDisplayed = false;
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
                mProgressDialog = null;
            }
        }
    }

    public static String formatDateMonthDayYear(Date date) {
        synchronized (DATE_FORMAT_MONTH_DAY_YEAR) {
            return DATE_FORMAT_MONTH_DAY_YEAR.get().format(date);
        }
    }

//    public static void setDefaultRefreshColors(SwipeRefreshLayout refreshLayout) {
//        refreshLayout.setColorScheme(R.color.mcd_red,
//                R.color.mcd_yellow,
//                R.color.mcd_red,
//                R.color.mcd_yellow);
//    }

    public static void dismissKeyboard(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static void showKeyboard(Context context, EditText editText, boolean shouldForce) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editText, shouldForce
                ? InputMethodManager.SHOW_FORCED
                : InputMethodManager.SHOW_IMPLICIT);
    }

    public static AlertDialog showGlobalAlertDialog(Context context, final String title, final String message, DialogInterface.OnClickListener onClick) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context, android.R.style.Theme_Holo_Light_Dialog);
        AlertDialog dialog = alertBuilder
                .setTitle(title)
                .setMessage(message)
                .setCancelable(true)
                .setNeutralButton("Okay", onClick)
                .create();
        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        return dialog;
    }

//    public static boolean isEditTextContainEmail(Context context, EditText argEditText) {
//
//        String email = argEditText.getText().toString();
//
//        return email.matches(context.getString(R.string.pattern_email_address));
//
//    }

    public static String getDayOfMonthSuffix(final int n) {
        if (n >= 11 && n <= 13) {
            return "th";
        }
        switch (n % 10) {
            case 1:
                return "st";
            case 2:
                return "nd";
            case 3:
                return "rd";
            default:
                return "th";
        }
    }

    public static Date getDateFromISO8601(String iso8601FormattedString) {
        try {
            return ISO8601_FORMAT.get().parse(iso8601FormattedString);
        } catch (ParseException e) {
            return null;
        }
    }

//    public static boolean passwordMeetsRequirements(Context context, EditText passwordEditText) {
//        String password = passwordEditText.getText().toString();
//        //String password = mPasswordConfirmEditText.getText().toString();
//        // requires 6-12 chars and at least one uppercase letter and at least one number
//        return (password.length() >= 6) && (password.length() <= 12)
//                && password.matches(context.getString(R.string.pattern_one_uppercase_letter))
//                && password.matches(context.getString(R.string.pattern_one_number))
//                && !password.matches(context.getString(R.string.pattern_one_space));
//    }

//    public static void showNoNetworkAlert(Context context) {
//        MCDAlertDialogBuilder.withContext(context)
//                .setMessage(R.string.trouble_connecting_message)
//                .setPositiveButton(R.string.ok, null)
//                .create()
//                .show();
//    }

    /**
     * Builder for a McDonald's specific Alert Dialog. This builder is a thin wrapper around Android's AlertDialog.Builder.
     */
    public static class MCDAlertDialogBuilder {
        private final Context mContext;
        AlertDialog.Builder mAlertBuilder;

        public MCDAlertDialogBuilder(Context context) {
            mContext = context;
            mAlertBuilder = new AlertDialog.Builder(context);
        }

        /**
         * Create a new MCDAlertDialogBuilder with {@link android.content.Context}
         *
         * @param context {@link android.content.Context} with which to create the builder
         * @return Returns an instance of the MCDAlertDialogBuilder to continue building upon
         */
        public static MCDAlertDialogBuilder withContext(Context context) {
            return new MCDAlertDialogBuilder(context);
        }

        /**
         * Sets the title of the Alert Dialog
         *
         * @param stringId Id of the string resource to set as the title of the AlertDialog
         * @return Returns an instance of the MCDAlertDialogBuilder to continue building upon
         */
        public MCDAlertDialogBuilder setTitle(int stringId) {
            mAlertBuilder.setTitle(mContext.getString(stringId));

            return this;
        }

        /**
         * Sets the title of the Alert Dialog
         *
         * @param string the string resource to set as the title of the AlertDialog
         * @return Returns an instance of the MCDAlertDialogBuilder to continue building upon
         */
        public MCDAlertDialogBuilder setTitle(String string) {
            mAlertBuilder.setTitle(string);

            return this;
        }

        /**
         * Sets the message of the Alert Dialog
         *
         * @param message Message to display on the dialog
         * @return Returns an instance of the MCDAlertDialogBuilder to continue building upon
         */
        public MCDAlertDialogBuilder setMessage(String message) {
            mAlertBuilder.setMessage(message);

            return this;
        }

        /**
         * Sets the message of the Alert Dialog
         *
         * @param stringId Id of the message to load from resources and set as the message on the AlertDialog
         * @return Returns an instance of the MCDAlertDialogBuilder to continue building upon
         */
        public MCDAlertDialogBuilder setMessage(int stringId) {
            mAlertBuilder.setMessage(mContext.getString(stringId));

            return this;
        }

        /**
         * Sets the positive button of the Alert Dialog
         *
         * @param stringId        Id of the string resource to set as the message of the AlertDialog's positive button
         * @param onClickListener Callback to be called when the button is clicked
         * @return Returns an instance of the MCDAlertDialogBuilder to continue building upon
         */
        public MCDAlertDialogBuilder setPositiveButton(int stringId, AlertDialog.OnClickListener onClickListener) {
            mAlertBuilder.setPositiveButton(mContext.getString(stringId), onClickListener);

            return this;
        }

        /**
         * Sets the positive button of the Alert Dialog
         *
         * @param string          The string resource to set as the message of the AlertDialog's positive button
         * @param onClickListener Callback to be called when the button is clicked
         * @return Returns an instance of the MCDAlertDialogBuilder to continue building upon
         */
        public MCDAlertDialogBuilder setPositiveButton(String string, AlertDialog.OnClickListener onClickListener) {
            mAlertBuilder.setPositiveButton(string, onClickListener);

            return this;
        }

        /**
         * Sets the negative button of the Alert Dialog
         *
         * @param stringId        Id of the string resource to set as the message of the AlertDialog's negative button
         * @param onClickListener Callback to be called when the button is clicked
         * @return Returns an instance of the MCDAlertDialogBuilder to continue building upon
         */
        public MCDAlertDialogBuilder setNegativeButton(int stringId, AlertDialog.OnClickListener onClickListener) {
            mAlertBuilder.setNegativeButton(stringId, onClickListener);

            return this;
        }

        /**
         * Sets the negative button of the Alert Dialog
         *
         * @param string          The string resource to set as the message of the AlertDialog's negative button
         * @param onClickListener Callback to be called when the button is clicked
         * @return Returns an instance of the MCDAlertDialogBuilder to continue building upon
         */
        public MCDAlertDialogBuilder setNegativeButton(String string, AlertDialog.OnClickListener onClickListener) {
            mAlertBuilder.setNegativeButton(string, onClickListener);

            return this;
        }

        /**
         * Sets the neutral button of the Alert Dialog
         *
         * @param stringId        Id of the string resource to set as the message of the AlertDialog's neutral button
         * @param onClickListener Callback to be called when the button is clicked
         * @return Returns an instance of the MCDAlertDialogBuilder to continue building upon
         */
        public MCDAlertDialogBuilder setNeutralButton(int stringId, AlertDialog.OnClickListener onClickListener) {
            mAlertBuilder.setNeutralButton(stringId, onClickListener);

            return this;
        }

        /**
         * Sets the neutral button of the Alert Dialog
         *
         * @param string          The string resource to set as the message of the AlertDialog's neutral button
         * @param onClickListener Callback to be called when the button is clicked
         * @return Returns an instance of the MCDAlertDialogBuilder to continue building upon
         */
        public MCDAlertDialogBuilder setNeutralButton(String string, AlertDialog.OnClickListener onClickListener) {
            mAlertBuilder.setNeutralButton(string, onClickListener);

            return this;
        }

        /**
         * Sets the flag on the AlertDialog if it is cancelable or not
         *
         * @param cancelable if the dialog is cancelable or not
         * @return Returns an instance of the MCDAlertDialogBuilder to continue building upon
         * @see android.app.AlertDialog.Builder#setCancelable(boolean)
         */
        public MCDAlertDialogBuilder setCancelable(boolean cancelable) {
            mAlertBuilder.setCancelable(cancelable);

            return this;
        }

        /**
         * Sets the cancel listener on the AlertDialog
         *
         * @param onCancelListener Callback to be called if the dialog is cancelled
         * @return Returns an instance of the MCDAlertDialogBuilder to continue building upon
         * @see android.app.AlertDialog.Builder#setOnCancelListener(android.content.DialogInterface.OnCancelListener)
         */
        public MCDAlertDialogBuilder setOnCancelListener(DialogInterface.OnCancelListener onCancelListener) {
            if (onCancelListener != null) {
                mAlertBuilder.setOnCancelListener(onCancelListener);
            }

            return this;
        }

        /**
         * Creates a {@link android.app.AlertDialog} to show
         *
         * @return {@link android.app.AlertDialog} to display to the user
         * @see android.app.AlertDialog.Builder#create()
         */
        public AlertDialog create() {
            return mAlertBuilder.create();
        }

        /**
         * Set a custom view to be the contents of the Dialog.
         *
         * @param view View to display on the alert dialog
         * @see android.app.AlertDialog.Builder#setView(android.view.View)
         */
        public MCDAlertDialogBuilder setView(View view) {
            mAlertBuilder.setView(view);
            return this;
        }
    }
}
