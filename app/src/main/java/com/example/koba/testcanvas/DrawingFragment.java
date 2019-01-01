package com.example.koba.testcanvas;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.koba.testcanvas.shape.ShapeManager;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static android.content.Context.CLIPBOARD_SERVICE;

public class DrawingFragment extends Fragment {
    private View view;
    private DrawingView drawingView;
    private ShapeManager shapeManager;
    /**
     * 操作モードのリスト
     */
    private final List<State> stateList = Arrays.asList(State.DRAWING, State.TRANSFER, State.COPY);
    /**
     * ファイル保存先ディレクトリ名
     */
    private File saveDir;
    /**
     * ファイル名の共通部分
     */
    private static final String SAVE_BASE_NAME = "canvas";
    /**
     * 内部データ保存用ファイル名
     */
    private static final String INNER_SAVE_BASE_NAME = "innerdata.dat";
    /**
     * 現在の操作モード
     */
    private State state = State.DRAWING;
    /**
     * パーミッション許可確認用
     */
    private static final int REQUEST_PERMISSION = 1;
    /**
     * Intent受け取り用 保存確認
     */
    private static final int REQUEST_CODE_SAVE_DIALOG = 0;
    /**
     * Intent受け取り用 上書き確認
     */
    private static final int REQUEST_CODE_REWRITE_DIALOG = 1;
    /**
     * Intent受け取り用 ダイアログから文字列取得
     */
    private static final int REQUEST_CODE_SET_STRING = 2;
    /**
     * Intent受け取り用 ファイルパス取得
     */
    private static final int REQUEST_CODE_SELECT_LOAD_FILE = 3;

    private static final String BUNDLE_KEY_STATE = "DrawingFragmentState";
    private static final String BUNDLE_KEY_SHAPETYPE = "DrawingFragmentShapeType";

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        shapeManager.saveInstanceState(outState);
        outState.putSerializable(BUNDLE_KEY_STATE, state);
        Spinner spinner = view.findViewById(R.id.spinnerShapeType);
        outState.putInt(BUNDLE_KEY_SHAPETYPE, spinner.getSelectedItemPosition());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        setHasOptionsMenu(true);

        shapeManager = new ShapeManager();
        final Fragment self = this;
        shapeManager.setOnSetTextListener(new ShapeManager.OnSetTextListener() {
            @Override
            public void onSetText() {
                // ダイアログ生成後、TextDialogFragmentからインテントを受け取って、
                // onActivityResult()でテキストを設定する
                final TextDialogFragment df = TextDialogFragment.newInstance(self,
                        REQUEST_CODE_SET_STRING);
                final FragmentManager fm = getFragmentManager();
                if (fm != null)
                    df.show(fm, "textDialog");
            }
        });

        // 起動時、設定されている場合、内部データを読み込む
        final Context context = Objects.requireNonNull(getContext());
        if (savedInstanceState == null && SettingManager.getStartActionLoad(context))
            restoreInnerData();

        //final String outputDir = Environment.getExternalStorageDirectory().getPath();
        final String outputDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath();
        saveDir = new File(outputDir, getResources().getString(R.string.app_name));

        view = inflater.inflate(R.layout.drawing, container, false);
        return view;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        drawingView = view.findViewById(R.id.DrawingView);

        int shapeTypePosition = 0;  // 図形選択位置

        // 前回の状態がある場合、設定する
        if (savedInstanceState != null) {
            shapeManager.restoreInstanceState(savedInstanceState);
            state = State.class.cast(savedInstanceState.getSerializable(BUNDLE_KEY_STATE));
            shapeTypePosition = savedInstanceState.getInt(BUNDLE_KEY_SHAPETYPE);
        }

        // 各種イベントの設定

        // 画面レイアウト変更時
        drawingView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                shapeManager.setSize(drawingView.getWidth(), drawingView.getHeight());
            }
        });

        final Button buttonUndo = view.findViewById(R.id.buttonUndo);
        buttonUndo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (shapeManager.undo())
                    drawingView.invalidate();
            }
        });

        final Button buttonRedo = view.findViewById(R.id.buttonRedo);
        buttonRedo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (shapeManager.redo())
                    drawingView.invalidate();
            }
        });

        // 操作モード選択用スピナー
        final List<String> stateNameList = new ArrayList<>();
        for (State st : stateList)
            stateNameList.add(getResources().getString(st.getId()));
        final Spinner spinnerState = view.findViewById(R.id.spinnerState);
        final ArrayAdapter<String> adapterState = new ArrayAdapter<>(
                spinnerState.getContext(), R.layout.spinner_item, stateNameList);
        adapterState.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerState.setAdapter(adapterState);
        spinnerState.setSelection(0, false);
        spinnerState.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                shapeManager.fix();
                setState(stateList.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        setState(state);

        // 図形選択用スピナー
        final List<Integer> shapeIdList = shapeManager.getShapeIdList();
        final List<String> shapeNameList = new ArrayList<>();
        for (int id : shapeIdList)
            shapeNameList.add(getResources().getString(id));
        final Spinner spinnerShapeType = view.findViewById(R.id.spinnerShapeType);
        final ArrayAdapter<String> adapterShapeType = new ArrayAdapter<>(
                spinnerShapeType.getContext(), R.layout.spinner_item, shapeNameList);
        adapterShapeType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerShapeType.setAdapter(adapterShapeType);
        spinnerShapeType.setSelection(shapeTypePosition, false);
        shapeManager.setShape(shapeTypePosition);
        spinnerShapeType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                shapeManager.setShape(position);
                // 描画する図形を変更した場合、強制的に描画モードに移行する
                if (state != State.DRAWING)
                    setState(State.DRAWING);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        spinnerShapeType.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // スピナーにタッチした時点で図形を確定させる
                shapeManager.fix();
                return false;  // イベント続行
            }
        });

        // タッチ関係時
        drawingView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final float x = event.getX();
                final float y = event.getY();

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:  // タッチした
                        touchDown(v, x, y);
                        break;
                    case MotionEvent.ACTION_MOVE:  // タッチ後に移動した
                        touchMove(v, x, y);
                        break;
                    case MotionEvent.ACTION_UP:  // タッチ後に指をはなした
                        touchUp(v, x, y);
                        break;
                    default:
                        return false;
                }

                return true;
            }
        });

        // onDraw()時
        drawingView.setOnDrawListener(new DrawingView.OnDrawListener() {
            @Override
            public void onDraw(Canvas canvas) {
                // 使用不可能なボタンは無効にする
                buttonUndo.setEnabled(shapeManager.canUndo());
                buttonRedo.setEnabled(shapeManager.canRedo());

                final Context context = Objects.requireNonNull(getContext());
                // 戻るした図形の表示
                if (SettingManager.getShapeAppearanceUndo(context))
                    shapeManager.drawUndo(canvas);
                // 移動時の対象図形の強調
                if (state == State.TRANSFER && SettingManager.getShapeAppearanceTransfer(context))
                    shapeManager.drawShapesLastHighlight(canvas);
                else
                    shapeManager.drawShapes(canvas);
            }
        });
    }

    private void touchDown(View v, float x, float y) {
        switch (state) {
            case DRAWING:
                shapeManager.start(x, y);
                break;
            case TRANSFER:
                shapeManager.preTransfer(x, y);
                break;
            case COPY:
                shapeManager.copy(x, y);
                shapeManager.preTransfer(x, y);
                break;
        }
        v.invalidate();  // 再描画
    }

    private void touchMove(View v, float x, float y) {
        switch (state) {
            case DRAWING:
                shapeManager.move(x, y);
                break;
            case TRANSFER:
                shapeManager.transfer(x, y);
                break;
            case COPY:
                setState(State.TRANSFER);  // 複製後は移動する
                break;
        }
        v.invalidate();
    }

    private void touchUp(View v, float x, float y) {
        switch (state) {
            case DRAWING:
                // 何もしない
                break;
            case TRANSFER:
                // 何もしない
                break;
            case COPY:
                setState(State.TRANSFER);  // 複製後は移動する
                break;
        }
    }

    /**
     * 状態の変更 合わせて画面表示の変更
     * @param newState 変更する状態
     */
    private void setState(State newState) {
        final int stateIndex = stateList.indexOf(newState);
        if (stateIndex < 0)
            return;
        state = newState;

        final Spinner spinner = view.findViewById(R.id.spinnerState);
        spinner.setSelection(stateIndex);

        drawingView.invalidate();
    }

    @Override
    public void onStop() {
        super.onStop();

        // 終了時のみ保存する
        // 内部状態の保存自体は設定に関係なく行う
        final Activity activity = Objects.requireNonNull(getActivity());
        if (activity.isFinishing())
            saveInnerData();
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        inflater.inflate(R.menu.menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_save:
                saveDialog();
                return true;
            case R.id.menu_copy_path:
                copySavePath();
                return true;
            case R.id.menu_clear:
                clearAll();
                return true;
            case R.id.menu_setting:
                setting();
                return true;
            case R.id.menu_load:
                selectLoadFile();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_SAVE_DIALOG:
                resultSaveDialog(resultCode, data);
                break;
            case REQUEST_CODE_REWRITE_DIALOG:
                resultRewriteDialog(resultCode, data);
                break;
            case REQUEST_CODE_SET_STRING:
                shapeManager.setText(data.getStringExtra(Intent.EXTRA_TEXT));
                drawingView.invalidate();
                break;
            case REQUEST_CODE_SELECT_LOAD_FILE:
                if (data != null) {
                    final Uri uri = data.getData();
                    loadSvg(uri);
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 保存確認ダイアログ結果処理
     * @param resultCode 結果
     * @param data 保存先パス
     */
    private void resultSaveDialog(int resultCode, Intent data) {
        switch (resultCode) {
            case Activity.RESULT_OK:
                final String savePathName = data.getStringExtra(Intent.EXTRA_TEXT);
                final File savePath = new File(savePathName);
                save(savePath);
                break;
            case Activity.RESULT_CANCELED:
                // 何もしない
                break;
        }
    }

    /**
     * 上書き確認ダイアログ結果処理
     * @param resultCode 結果
     * @param data 保存先パス
     */
    private void resultRewriteDialog(int resultCode, Intent data) {
        switch (resultCode) {
            case Activity.RESULT_OK:
                final String savePathName = data.getStringExtra(Intent.EXTRA_TEXT);
                final File savePath = new File(savePathName);
                writeTo(savePath);
                break;
            case Activity.RESULT_CANCELED:
                // 何もしない
                break;
        }
    }

    /**
     * 簡易版メッセージ表示 （とりあえずtoast）
     * @param msg 表示するメッセージ
     */
    private void show(String msg) {
        Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
    }

    /**
     * 現在の絵を保存する （保存確認ダイアログあり）
     */
    private void saveDialog() {
        if (!hasPermissionSTORAGE()) {
            requestPermissionSTORAGE();
            return;
        }

        // ダイアログでファイル保存するか確認する
        final String savePathName = getSavePath().getAbsolutePath();
        final FileDialogFragment df = FileDialogFragment.newInstance(this,
                REQUEST_CODE_SAVE_DIALOG, savePathName,
                "ファイル保存確認", savePathName + "として保存しますか");
        final FragmentManager fm = getFragmentManager();
        if (fm != null)
            df.show(fm, "saveDialog");
    }

    /**
     * 現在の絵を保存する （上書き確認ダイアログあり）
     */
    private void save(File savePath) {
        if (!saveDir.exists()) {
            if(!saveDir.mkdir()) {
                show("出力先ディレクトリの作成に失敗しました");
                return;
            }
        }

        // ファイルが存在する場合、ダイアログで上書き確認する
        if (savePath.exists()) {
            final String savePathName = savePath.getAbsolutePath();
            final FileDialogFragment df = FileDialogFragment.newInstance(this,
                    REQUEST_CODE_REWRITE_DIALOG, savePathName,
                    "上書き確認", savePath + "を上書きしますか");
            final FragmentManager fm = getFragmentManager();
            if (fm != null)
                df.show(fm, "saveDialog");
            return;
        }

        writeTo(savePath);
    }

    /**
     * 保存するファイルのフルパスを返す
     * @return フルパス
     */
    private File getSavePath() {
        // ファイル名の重複のチェックはしていない
        final StringBuilder filename = new StringBuilder(SAVE_BASE_NAME);
        final Calendar cal = Calendar.getInstance();
        final SimpleDateFormat f = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault());
        filename.append("_").append(f.format(cal.getTime())).append(".svg");
        return new File(saveDir, filename.toString());
    }

    /**
     * 絵をファイルに書き込む
     * @param outputPath 出力先
     */
    private void writeTo(File outputPath) {
        boolean wrote = false;
        try (final FileOutputStream stream = new FileOutputStream(outputPath, false);
             final OutputStreamWriter ow = new OutputStreamWriter(stream, "UTF-8");
             final BufferedWriter writer = new BufferedWriter(ow)) {
            wrote = shapeManager.writeTo(writer);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (wrote)
            show(String.format("保存しました %s", outputPath.getName()));
        else
            show("ファイル書き込みに失敗しました");
    }

    /**
     * 内部データを内部領域に保存する
     */
    private void saveInnerData() {
        final File innerDataFile = getInnerDataFile();
        try (final FileOutputStream fs = new FileOutputStream(innerDataFile, false);
             final BufferedOutputStream buffer = new BufferedOutputStream(fs);
             final ObjectOutputStream stream = new ObjectOutputStream(buffer)) {
            shapeManager.saveInnerData(stream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 内部データを内部領域から取得する
     */
    private void restoreInnerData() {
        final File innerDataFile = getInnerDataFile();
        if (!innerDataFile.exists())
            return;
        try (final FileInputStream fs = new FileInputStream(innerDataFile);
             final BufferedInputStream buffer = new BufferedInputStream(fs);
             final ObjectInputStream stream = new ObjectInputStream(buffer)) {
            shapeManager.restoreInnerData(stream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return 内部データ保存用ファイルパスを返す
     */
    @NonNull
    private File getInnerDataFile() {
        // 内部領域のキャッシュ領域を利用する
        final Context context = Objects.requireNonNull(getContext());
        return new File(context.getCacheDir(), INNER_SAVE_BASE_NAME);
    }

    /**
     * 書き込み許可を持っているか
     * @return 書き込み許可がある場合、真
     */
    private boolean hasPermissionSTORAGE() {
        final int permission = ContextCompat.checkSelfPermission(Objects.requireNonNull(getActivity()),
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return permission == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * 外部ストレージ書き込み許可をユーザに求める
     */
    private void requestPermissionSTORAGE() {
        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                REQUEST_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    saveDialog();
                } else {
                    // パーミッションが得られなかった場合、メッセージ表示
                    if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        // 拒否された場合のメッセージ（権限についての補足）
                        show("画像ファイル保存のために権限が必要です");
                    } else {
                        // 永続的に拒否された場合のメッセージ（状況説明のみ）
                        show("ストレージにアクセスできません");
                    }
                }
                break;
        }
    }

    /**
     * クリップボードに画像の保存先をコピーする
     */
    private void copySavePath() {
        final ClipData.Item item = new ClipData.Item(saveDir.getPath());
        final String[] mimeType = new String[1];
        mimeType[0] = ClipDescription.MIMETYPE_TEXT_PLAIN;
        final ClipData cd = new ClipData(new ClipDescription("text_data", mimeType), item);
        final ClipboardManager cm = (ClipboardManager) Objects.requireNonNull(getActivity()).getSystemService(CLIPBOARD_SERVICE);
        cm.setPrimaryClip(cd);
        show(saveDir.getPath());
    }

    private void clearAll() {
        // 全ての操作を元に戻す
        while (shapeManager.canUndo())
            shapeManager.undo();

        drawingView.invalidate();
    }

    /**
     * 設定画面の表示
     */
    private void setting() {
        Intent intent = new Intent(getActivity(), SettingActivity.class);
        startActivity(intent);
    }

    /**
     * 読み込むSVGファイルの選択
     */
    private void selectLoadFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        startActivityForResult(intent, REQUEST_CODE_SELECT_LOAD_FILE);
    }

    /**
     * SVGファイルの読み込み
     */
    private void loadSvg(Uri uri) {
        boolean read = false;
        Activity activity = Objects.requireNonNull(getActivity());
        try (final InputStream is = activity.getContentResolver().openInputStream(uri);
             final BufferedInputStream stream = new BufferedInputStream(is)) {
            read = shapeManager.read(stream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (read) {
            show("ファイルを読み込みました");
            drawingView.invalidate();
        } else {
            show("ファイル読み込みに失敗しました");
        }
    }

    private enum State {
        /**
         * 図形描画モード
         */
        DRAWING(R.string.button_state_draw),
        /**
         * 図形移動モード
         */
        TRANSFER(R.string.button_state_transfer),
        /**
         * 図形複製モード
         */
        COPY(R.string.button_state_copy);

        private final int id;

        State(int id) {
            this.id = id;
        }

        /**
         * 対応する R.stringのIDを返す
         * @return id
         */
        int getId() {
            return id;
        }
    }
}
