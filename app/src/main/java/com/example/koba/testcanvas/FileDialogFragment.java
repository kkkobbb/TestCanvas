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

import java.util.Objects;

/**
 * ファイル関係確認用ダイアログ
 * <div>
 *     呼び出し側のFragmentにintentを投げることで結果を通知している <br>
 *     呼び出し側はonActivityResult()を実装して受け取ること
 * </div>
 */
public class FileDialogFragment extends DialogFragment {
    private static final String KEY_SAVE_PATH = "savePath";
    private static final String KEY_TITLE = "title";
    private static final String KEY_MESSAGE = "msg";

    static FileDialogFragment newInstance(Fragment target, int requestCode, String savePathName, String title, String msg) {
        final FileDialogFragment frag = new FileDialogFragment();
        frag.setTargetFragment(target, requestCode);
        final Bundle args = new Bundle();
        args.putString(KEY_SAVE_PATH, savePathName);
        args.putString(KEY_TITLE, title);
        args.putString(KEY_MESSAGE, msg);
        frag.setArguments(args);
        return frag;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Bundle args = Objects.requireNonNull(getArguments());
        final String savePathName = args.getString(KEY_SAVE_PATH);
        final String title = args.getString(KEY_TITLE);
        final String msg = args.getString(KEY_MESSAGE);
        final Fragment target = getTargetFragment();

        // Use the Builder class for convenient dialog construction
        final AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
        builder.setTitle(title);
        builder.setMessage(msg);
        builder.setPositiveButton("はい", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (target == null)
                    return;
                final Intent data = new Intent();
                // 生成時に渡されたファイルパスを投げる
                data.putExtra(Intent.EXTRA_TEXT, savePathName);
                target.onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, data);
            }
        });
        builder.setNegativeButton("いいえ", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (target == null)
                    return;
                target.onActivityResult(getTargetRequestCode(), Activity.RESULT_CANCELED, null);
            }
        });

        // Create the AlertDialog object and return it
        return builder.create();
    }
}
