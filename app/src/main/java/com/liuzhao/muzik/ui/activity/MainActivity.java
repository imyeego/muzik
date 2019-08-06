package com.liuzhao.muzik.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.bin.david.form.core.SmartTable;
import com.bin.david.form.core.TableConfig;
import com.bin.david.form.data.CellInfo;
import com.bin.david.form.data.column.Column;
import com.bin.david.form.data.format.bg.BaseBackgroundFormat;
import com.bin.david.form.data.format.bg.BaseCellBackgroundFormat;
import com.bin.david.form.data.format.bg.IBackgroundFormat;
import com.bin.david.form.data.format.bg.ICellBackgroundFormat;
import com.bin.david.form.data.style.FontStyle;
import com.bin.david.form.data.style.LineStyle;
import com.google.gson.GsonBuilder;
import com.liuzhao.ioc_annotations.BindView;
import com.liuzhao.ioc_annotations.OnClick;
import com.liuzhao.ioc_api.ViewFinder;
import com.liuzhao.muzik.R;
import com.liuzhao.muzik.annotation.SingleClick;
import com.liuzhao.muzik.app.Constants;
import com.liuzhao.muzik.common.OkioSocket;
import com.liuzhao.muzik.common.download.DownloadManager;
import com.liuzhao.muzik.common.nio.NioSocket;
import com.liuzhao.muzik.model.bean.Exam;
import com.liuzhao.muzik.model.bean.MovieEntity;
import com.liuzhao.muzik.model.bean.NewsEntity;
import com.liuzhao.muzik.model.bean.Student;
import com.liuzhao.muzik.model.bean.User;
import com.liuzhao.muzik.model.event.FirstEvent;
import com.liuzhao.muzik.model.event.SecondEvent;
import com.liuzhao.muzik.presenter.NewsContract;
import com.liuzhao.muzik.presenter.NewsPresenter;
import com.liuzhao.muzik.service.NetworkService;
import com.liuzhao.muzik.ui.base.BaseActivity;
import com.liuzhao.muzik.utils.Counter;
import com.liuzhao.okevent.OkEvent;
import com.liuzhao.okevent.Subscribe;
import com.liuzhao.okevent.ThreadMode;


import org.xutils.DbManager;
import org.xutils.ex.DbException;
import org.xutils.x;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okio.Buffer;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;
import okio.Source;

public class MainActivity extends BaseActivity<NewsPresenter> implements NewsContract.View, DownloadManager.ObserverProgress {

    private static String TAG = "MainActivity";
    @BindView(R.id.bn_hello)
    Button btnHello;
    @BindView(R.id.bn_stop)
    Button btnStop;

    @BindView(R.id.tv_hello)
    TextView tvHello;
    @BindView(R.id.progress)
    ProgressBar progressBar;

    @BindView(R.id.bn_playlist)
    Button bnPlaylist;
    @BindView(R.id.st_data)
    SmartTable<Exam> table;
    private DownloadManager manager;
    private Counter counter;
    private int i = 0;
    private String[] kms = new String[]{"文科综合", "理科综合", "英语"};
    private int[] kcCount = new int[]{4, 18, 32};
    private int[] bagCount = new int[]{4, 18, 32};
    private int[] sparedPaperCount = new int[]{1, 1, 1};
    private int[] sparedCardCount = new int[]{1, 1, 1};
    private int[] total = new int[]{6, 20, 24};
    String str = "[{\"SN\":\"54:E1:AD:F1:8E:D5\",\"aim\":\"download_sfrz\",\"version\":\"0.1\"}]";
    String str1 = "[{\"SN\":\"54:E1:AD:F1:8E:D5\",\"aim\":\"upload_sfrz_result\",\"version\":\"0.1\"},{\"zjhm\":\"610124198810271029\",\"rzsj\":\"2019-07-08 08:25:25\",\"rzfs\":\"12\",\"rzjg\":\"1\",\"devSN\":\"\",\"zwppl\":\"\", \"rlppl\":\"0.36\",\"rlzp\":\"\",\"zwtp\":\"\",\"sfzzp\":\"\"}]";

    private List<Exam> list = new ArrayList<>();
    private OkioSocket socket;
    private DbManager dbManager;
    private DbManager.DaoConfig config = new DbManager.DaoConfig()
            .setDbName("student.db")
            .setDbDir(new File(Constants.PATH))
            .setDbVersion(1)
            .setAllowTransaction(true)
            .setDbUpgradeListener((db, oldVersion, newVersion) -> {

            });

    @Override
    protected void initView() {
        OkEvent.getInstance().register(this);
        counter = Counter.create();
        ViewFinder.inject(this);
        manager = DownloadManager.getInstance();
        manager.setObserverProgress(this);
        openWifi(context);
        socket = new OkioSocket("172.16.41.42", 8889);
        btnHello.setText("START");
        loadData();
        table.getConfig().setShowXSequence(false);
        table.getConfig().setShowTableTitle(false);
        table.getConfig().setColumnTitleHorizontalPadding(0);
        table.getConfig().setShowYSequence(false);
        table.getConfig().setContentCellBackgroundFormat(new ICellBackgroundFormat<CellInfo>() {
            @Override
            public void drawBackground(Canvas canvas, Rect rect, CellInfo column, Paint paint) {
                paint.setColor(Color.parseColor("#ededed"));
                canvas.drawRect(rect, paint);
            }

            @Override
            public int getTextColor(CellInfo column) {
                return Color.parseColor("#191970");
            }
        });

        FontStyle fontStyle = new FontStyle(14, Color.parseColor("#000000"));
        table.getConfig().setColumnTitleStyle(fontStyle);

        LineStyle lineStyle = new LineStyle(2, Color.parseColor("#191970"));
        table.getConfig().setContentGridStyle(lineStyle);
        table.getConfig().setColumnTitleGridStyle(lineStyle);

        table.setData(list);
//        saveFirstData();
//        Intent intent = new Intent(this, NetworkService.class);
//        startService(intent);
    }

    @OnClick(R.id.bn_hello)
    @SingleClick
    void onStart(View view){
//        Map<String, Object> map = new HashMap<>();
//        map.put("username", "liuzhao");
//        presenter.onLogin(map);

//        socket.send(str, new OkioSocket.Callback() {
//            @Override
//            public void onSuccess(String response) {
//                Toast.makeText(context, "" + response.length(), Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void onFailure(String msg) {
//                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
//
//            }
//        });
        NioSocket nioSocket = NioSocket.instance()
                .setHostAndPort("192.168.137.1", 8889)
                .build();
//        Toast.makeText(context, "Tinker修复成功...", Toast.LENGTH_SHORT).show();


    }

    @OnClick(R.id.bn_stop)
    void onStop(View view){
//        if ((counter.getCount() & 1) == 0){
//            manager.pause();
//            btnStop.setText("restart");
//        }
//        else{
//            manager.reStart();
//            btnStop.setText("stop");
//        }
//        counter.count();

        socket.send(str1, new OkioSocket.Callback() {
            @Override
            public void onSuccess(String response) {
                Toast.makeText(context, "" + response.length(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(String msg) {
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();

            }
        });


    }

    @OnClick(R.id.bn_playlist)
    void onToPlaylist(View view){
        Intent intent = new Intent();
        intent.setClass(this, PlaylistActivity.class);
        startActivity(intent);
    }

    private void saveFirstData() {
        dbManager = x.getDb(config);

        Student student = new Student();
        student.setAge((short) 14);
        student.setName("刘钊");
        student.setGender((byte) 2);
        student.setClasses((byte) 8);
        student.setGrade((byte) 2);
        student.setSchool("北京101中学");
//        student.setUploaded("");
        try {
            dbManager.save(student);
        } catch (DbException e) {
            e.printStackTrace();
        }

        try {
            Student s = dbManager.selector(Student.class).where("name", "=", "刘钊").findFirst();
            GsonBuilder builder = new GsonBuilder();
            builder.excludeFieldsWithoutExposeAnnotation();
            builder.serializeNulls();
            String json = builder.create().toJson(s);
            Toast.makeText(context, json, Toast.LENGTH_SHORT).show();

        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    private void loadData() {
        list.clear();
        for (int j = 0; j < 3; j++) {
            Exam exam = new Exam();
            exam.setKm(kms[j]);
            exam.setKcCount(kcCount[j]);
            exam.setBagCount(bagCount[j]);
            exam.setSparedPaperCount(sparedPaperCount[j]);
            exam.setSparedCardCount(sparedCardCount[j]);
            exam.setTotal(total[j]);
            list.add(exam);
        }
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onText(FirstEvent event){
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Toast.makeText(getApplicationContext(), event.getMsg(), Toast.LENGTH_SHORT).show();
        Log.e(TAG, event.getMsg() + " " + Thread.currentThread().getName());
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onBar(SecondEvent event){
        Log.e(TAG, event.getMsg() + " " + Thread.currentThread().getName());
        Toast.makeText(getApplicationContext(), event.getMsg(), Toast.LENGTH_SHORT).show();

//        tvHello.setText(event.getMsg());
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected NewsPresenter getPresenter() {
        presenter = new NewsPresenter(context, this);
        return presenter;
    }

    @Override
    public void onLoadComplete(List<NewsEntity> list) {
        Log.e(TAG, "" + list.toString());
        tvHello.setText(list.get(0).toString());
    }

    @Override
    public void onLoadMovieComplete(List<MovieEntity> list) {
        Log.e(TAG, list.get(3).getTitle());
    }


    @Override
    public void progressChanged(long read, long contentLength, boolean done) {
        final int progress = (int)(100 * read / contentLength);
        progressBar.setProgress(progress);
        tvHello.setText(String.format("%d%%", progress));
        if (done) Toast.makeText(context, "下载完成!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLogin(User user) {
        Toast.makeText(context, user.getUsername(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onError() {

    }

    @Override
    protected void onDestroy() {
        presenter.unsubscribe();
        counter.clear();
        OkEvent.getInstance().unregister(this);
//        Intent intent = new Intent(this, NetworkService.class);
//        stopService(intent);
        super.onDestroy();
    }

    public static void openWifi(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null && !wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        Log.e("Wi-Fi 状态", "打开...");
    }

    public static boolean isFileExist(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return false;
        }
        File file = new File(filePath);
        return (file.exists() && file.isFile());
    }
}
