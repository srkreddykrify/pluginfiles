package com.tsongkha.spinnerdatepicker;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class SpinnerDatePickerDialogBuilder {

    private Context context;
    private DatePickerDialog.OnDateSetListener callBack;
    private boolean isDayShown = true;
    private boolean isTitleShown = true;
    private int theme = -1;                 //default theme
    private int spinnerTheme = -1;          //default theme
    private Calendar defaultDate = new GregorianCalendar(1980, 0, 1);
    private Calendar minDate = new GregorianCalendar(1900, 0, 1);
    private Calendar maxDate = new GregorianCalendar(2100, 0, 1);


    public SpinnerDatePickerDialogBuilder context(Context context) {
        this.context = context;
        return this;
    }

    public SpinnerDatePickerDialogBuilder callback(DatePickerDialog.OnDateSetListener callBack) {
        this.callBack = callBack;
        return this;
    }

    public SpinnerDatePickerDialogBuilder dialogTheme(int theme) {
        this.theme = theme;
        return this;
    }

    public SpinnerDatePickerDialogBuilder spinnerTheme(int spinnerTheme) {
        this.spinnerTheme = spinnerTheme;
        return this;
    }

    public SpinnerDatePickerDialogBuilder defaultDate(int year, int monthIndexedFromZero, int day) {
        this.defaultDate = new GregorianCalendar(year, monthIndexedFromZero, day);
        return this;
    }

    public SpinnerDatePickerDialogBuilder minDate(int year, int monthIndexedFromZero, int day) {
        this.minDate = new GregorianCalendar(year, monthIndexedFromZero, day);
        return this;
    }

    public SpinnerDatePickerDialogBuilder maxDate(int year, int monthIndexedFromZero, int day) {
        this.maxDate = new GregorianCalendar(year, monthIndexedFromZero, day);
        return this;
    }

    public SpinnerDatePickerDialogBuilder showDaySpinner(boolean showDaySpinner) {
        this.isDayShown = showDaySpinner;
        return this;
    }

    public SpinnerDatePickerDialogBuilder showTitle(boolean showTitle) {
        this.isTitleShown = showTitle;
        return this;
    }



    public DatePickerDialog build() {
        if (context == null) throw new IllegalArgumentException("Context must not be null");
        if (maxDate.getTime().getTime() <= minDate.getTime().getTime()) throw new IllegalArgumentException("Max date is not after Min date");

//        return new DatePickerDialog(context, theme, spinnerTheme, callBack, defaultDate, minDate, maxDate, isDayShown, isTitleShown);
//        return new DatePickerDialog(context, theme, spinnerTheme, callBack, defaultDate, minDate, maxDate, isDayShown, isTitleShown);

        DatePickerDialog datePickerDialog = new DatePickerDialog(context, theme, spinnerTheme, callBack, defaultDate, minDate, maxDate, isDayShown, isTitleShown);
//
//        colorAlertDialogTitle(datePickerDialog,      Color.parseColor("#760000"));

//        int textViewId = datePickerDialog.getContext().getResources().getIdentifier("android:id/alertTitle", null, null);
//        TextView tv = (TextView) datePickerDialog.findViewById(textViewId);
//        tv.setTextColor(context.getResources().getColor(R.color.red));
        return  datePickerDialog;
    }
    public  void colorAlertDialogTitle(AlertDialog dialog, int color) {
        int dividerId = dialog.getContext().getResources().getIdentifier("android:id/titleDivider", null, null);
        if (dividerId != 0) {
            View divider = dialog.findViewById(dividerId);
            divider.setBackgroundColor(color);
        }

        int textViewId = dialog.getContext().getResources().getIdentifier("android:id/alertTitle", null, null);
        if (textViewId != 0) {
            TextView tv = (TextView) dialog.findViewById(textViewId);
            tv.setTextColor(color);
        }

        int iconId = dialog.getContext().getResources().getIdentifier("android:id/icon", null, null);
        if (iconId != 0) {
            ImageView icon = (ImageView) dialog.findViewById(iconId);
            icon.setColorFilter(color);
        }
    }


}