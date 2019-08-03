package com.example.koba.testcanvas;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.InputFilter;
import android.text.InputType;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.widget.EditText;

import java.util.Objects;

/**
 * テキスト取得用ダイアログ
 * <div>
 *     呼び出し側のFragmentにintentを投げることで結果を通知している <br>
 *     呼び出し側はonActivityResult()を実装して受け取ること
 * </div>
 */
public class TextDialogFragment extends DialogFragment {
    private static final String KEY_BR = "br";
    private static final String KEY_INIT_TEXT = "initText";
    private static final String KEY_TITLE = "Title";
    private static final String KEY_INPUT_TEXT = "inputText";
    private EditText editText;

    static TextDialogFragment newInstance(Fragment target, int requestCode, String title, String initText) {
        final TextDialogFragment frag = new TextDialogFragment();
        frag.setTargetFragment(target, requestCode);
        final Bundle args = new Bundle();
        final String br_ = System.getProperty("line.separator");  // 改行文字
        args.putString(KEY_BR, br_);
        args.putString(KEY_TITLE, title);
        args.putString(KEY_INIT_TEXT, initText);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (editText != null) {
            // 入力中の文字列を保存する
            final String inputText = editText.getText().toString();
            outState.putString(KEY_INPUT_TEXT, inputText);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Activity activity = Objects.requireNonNull(getActivity());
        final Bundle args = Objects.requireNonNull(getArguments());
        final String br = Objects.requireNonNull(args.getString(KEY_BR));
        final String title = Objects.requireNonNull(args.getString(KEY_TITLE));
        final String initText = Objects.requireNonNull(args.getString(KEY_INIT_TEXT));
        final Fragment target = getTargetFragment();

        editText = new EditText(activity);
        editText.setInputType(InputType.TYPE_CLASS_TEXT);
        if (savedInstanceState != null) {
            final String inputText = savedInstanceState.getString(KEY_INPUT_TEXT);
            if (inputText != null)
                editText.setText(inputText);
        } else {
            editText.setText(initText);
        }
        final InputFilter[] filters = {new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                // 改行があると、SVGとCanvasで扱いが違うため空白に置換する
                final String text = source.toString();
                // 改行無しの場合、デフォルトの動作
                if (!text.contains(br))
                    return null;
                return text.replace(br, " ");
            }}};
        editText.setFilters(filters);
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(title);
        builder.setView(editText);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (target == null)
                    return;
                final Intent data = new Intent();
                // 入力された文字列を投げる
                final SpannableStringBuilder ssb = (SpannableStringBuilder)editText.getText();
                data.putExtra(Intent.EXTRA_TEXT, ssb.toString());
                target.onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, data);
            }
        });
        builder.setNegativeButton("Cancel", null);
        return builder.create();
    }
}
