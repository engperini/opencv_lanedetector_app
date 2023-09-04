package com.perinidev.roadhelper;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;

public class TextFormatter {
    public static SpannableString formatText(String nome1, String nome2, String nome3, double valor1, double valor2, double valor3, int backgroundColor, int textColor) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(nome1).append(": ").append(String.format("%.2f", valor1)).append(" m\n");
        builder.append(nome2).append(": ").append(String.format("%.2f", valor2)).append(" m\n");
        builder.append(nome3).append(": ").append(String.format("%.2f", valor3)).append(" m\n");

        SpannableString spannableString = new SpannableString(builder);
        BackgroundColorSpan backgroundSpan = new BackgroundColorSpan(backgroundColor);
        ForegroundColorSpan foregroundSpan = new ForegroundColorSpan(textColor);

        spannableString.setSpan(backgroundSpan, 0, spannableString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(foregroundSpan, 0, spannableString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        return spannableString;
    }
}
