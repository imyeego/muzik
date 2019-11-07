package com.liuzhao.muzik.ui.activity;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.FloatEvaluator;
import android.animation.IntEvaluator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.MessageQueue;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewGroupCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewAnimationUtils;
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
import com.imyeego.promise.Promise;
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import okio.Buffer;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;
import okio.Source;
import pub.devrel.easypermissions.EasyPermissions;
import rx.Observable;

public class MainActivity extends BaseActivity<NewsPresenter> implements NewsContract.View
        , DownloadManager.ObserverProgress, EasyPermissions.PermissionCallbacks {

    private static String TAG = "MainActivity";
    private static final int WRITE_STORAGE_CODE = 455;
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
    @BindView(R.id.cl_hello)
    ConstraintLayout clHello;
    @BindView(R.id.cl_stop)
    ConstraintLayout clStop;
    private DownloadManager manager;
    private Counter counter;
    private int width;
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
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    private ScheduledExecutorService scheduledService = Executors.newScheduledThreadPool(5);
    private ScheduledFuture future;
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
//        openWifi(context);
        socket = new OkioSocket("172.16.41.42", 8889);
        btnHello.setText("START");
//        loadData();
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
        clHello.post(() -> {
            width = clHello.getMeasuredWidth();
        });

        saveFirstData();
//        Intent intent = new Intent(this, NetworkService.class);
//        startService(intent);
    }

    @OnClick(R.id.bn_hello)
    @SingleClick
    void onStart(View view){
//        Map<String, Object> map = new HashMap<>();
//        map.put("username", "liuzhao");
//        presenter.onLogin(map);
        manager.start();
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
//        NioSocket nioSocket = NioSocket.instance()
//                .setHostAndPort("192.168.137.1", 8889)
//                .build();



//        Toast.makeText(context, "Tinker修复成功...", Toast.LENGTH_SHORT).show();

//        ObjectAnimator animator = ObjectAnimator.ofObject(clStop, "translationX", new FloatEvaluator(), width, 0);
//        ObjectAnimator animator1 = ObjectAnimator.ofObject(clHello, "translationX", new FloatEvaluator(), 0, -width);
//        AnimatorSet animatorSet = new AnimatorSet();
//        animatorSet.play(animator).with(animator1);
//        animatorSet.setDuration(1000);
//        animatorSet.addListener(new Animator.AnimatorListener() {
//            @Override
//            public void onAnimationStart(Animator animation) {
//                clStop.setVisibility(View.VISIBLE);
//
//            }
//
//            @Override
//            public void onAnimationEnd(Animator animation) {
//                clStop.setVisibility(View.GONE);
//
//            }
//
//            @Override
//            public void onAnimationCancel(Animator animation) {
//
//            }
//
//            @Override
//            public void onAnimationRepeat(Animator animation) {
//
//            }
//        });
//        animatorSet.start();


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
        if (future != null) {
            future.cancel(false);
        }

//        socket.send(str1, new OkioSocket.Callback() {
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

//        Toast.makeText(context, "Tinker修复成功...", Toast.LENGTH_SHORT).show();

    }

    @OnClick(R.id.bn_playlist)
    void onToPlaylist(View view){
//        Intent intent = new Intent();
//        intent.setClass(this, PlaylistActivity.class);
//        startActivity(intent);
        if (!EasyPermissions.hasPermissions(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            EasyPermissions.requestPermissions(this, "请求存储权限", WRITE_STORAGE_CODE
                    , Manifest.permission.WRITE_EXTERNAL_STORAGE);
            return;
        }
        future = scheduledService.scheduleWithFixedDelay(load, 1, 1, TimeUnit.SECONDS);
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
        student.setUploaded("");
        student.setTime(dateFormat.format(new Date()));
        try {
            dbManager.save(student);
            long count = dbManager.selector(Student.class).where("upload", "=", "").count();
            Log.e(TAG, "before:" + count);
        } catch (DbException e) {
            e.printStackTrace();
        }

        try {
            Student s = dbManager.selector(Student.class).where("upload", "=", "").findFirst();
            s.setUploaded("1");
            s.setTime(dateFormat.format(new Date()));
            dbManager.saveOrUpdate(s);
//            GsonBuilder builder = new GsonBuilder();
//            builder.excludeFieldsWithoutExposeAnnotation();
//            builder.serializeNulls();
//            String json = builder.create().toJson(s);
            long count = dbManager.selector(Student.class).where("upload", "=", "").count();
            Log.e(TAG, "after:" + count);
//            Toast.makeText(context, "" + count, Toast.LENGTH_SHORT).show();

        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        switch (requestCode) {
            case WRITE_STORAGE_CODE:
                future = scheduledService.scheduleWithFixedDelay(load, 1, 1, TimeUnit.SECONDS);
                break;
            default:
                break;
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        switch (requestCode) {
            case WRITE_STORAGE_CODE:
                new AlertDialog.Builder(this)
                        .setTitle("权限申请")
                        .setMessage("当前App需要存储权限，请在应用的权限管理中授予。")
                        .setPositiveButton("确定", (dialog, which) -> {
                            Intent intent = new Intent();
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                            intent.setData(Uri.fromParts("package", context.getPackageName(), null));
                            startActivity(intent);
                        })
                        .setNegativeButton("取消", (dialog, which) -> {
                            Toast.makeText(MainActivity.this, "您取消了权限授予", Toast.LENGTH_SHORT).show();
                        }).create().show();
                break;
            default:
                break;
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

    private Runnable load = () -> {
        saveFirstData();
    };
}
