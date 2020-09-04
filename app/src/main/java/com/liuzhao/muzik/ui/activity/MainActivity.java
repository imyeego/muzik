package com.liuzhao.muzik.ui.activity;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Xml;
import android.view.Choreographer;
import android.view.FrameStats;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.imyeego.promise.Promise;
import com.liuzhao.ioc_annotations.BindView;
import com.liuzhao.ioc_annotations.OnClick;
import com.liuzhao.ioc_api.ViewFinder;
import com.liuzhao.muzik.R;
import com.liuzhao.muzik.annotation.SingleClick;
import com.liuzhao.muzik.app.App;
import com.liuzhao.muzik.common.OkWebSocket;
import com.liuzhao.muzik.common.OkioSocket;
import com.liuzhao.muzik.common.download.DownloadManager;
import com.liuzhao.muzik.database.TestDao;
import com.liuzhao.muzik.model.bean.MovieEntity;
import com.liuzhao.muzik.model.bean.NewsEntity;
import com.liuzhao.muzik.model.bean.User;
import com.liuzhao.muzik.model.event.FirstEvent;
import com.liuzhao.muzik.model.event.SecondEvent;
import com.liuzhao.muzik.presenter.NewsContract;
import com.liuzhao.muzik.presenter.NewsPresenter;
import com.liuzhao.muzik.ui.base.BaseActivity;
import com.liuzhao.muzik.utils.Counter;
import com.liuzhao.muzik.utils.FileUtil;
import com.liuzhao.okevent.OkEvent;
import com.liuzhao.okevent.Subscribe;
import com.liuzhao.okevent.ThreadMode;

import java.io.File;
import java.nio.channels.AsynchronousFileChannel;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends BaseActivity<NewsPresenter> implements NewsContract.View
        , DownloadManager.ObserverProgress, EasyPermissions.PermissionCallbacks, OkWebSocket.WebSocketCallback {

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
    @BindView(R.id.cl_hello)
    ConstraintLayout clHello;
    @BindView(R.id.cl_stop)
    ConstraintLayout clStop;
    @BindView(R.id.rv)
    RecyclerView rvTimeline;
    @BindView(R.id.bn_save)
    Button btnSave;
    @BindView(R.id.btn_send)
    Button btnSend;
    @BindView(R.id.ed_text)
    EditText editText;

    private TimeLineAdapter timeLineAdapter;
    List<TimeLine> timeLines = new ArrayList<>();
    CenterLayoutManager layoutManager1;
    private DownloadManager manager;
    private Counter counter;
    private int width;
    private int i = 0;
    String str = "[{\"SN\":\"54:E1:AD:F1:8E:D5\",\"aim\":\"download_sfrz\",\"version\":\"0.1\"}]";
    String str1 = "[{\"SN\":\"54:E1:AD:F1:8E:D5\",\"aim\":\"upload_sfrz_result\",\"version\":\"0.1\"},{\"zjhm\":\"610124198810271029\",\"rzsj\":\"2019-07-08 08:25:25\",\"rzfs\":\"12\",\"rzjg\":\"1\",\"devSN\":\"\",\"zwppl\":\"\", \"rlppl\":\"0.36\",\"rlzp\":\"\",\"zwtp\":\"\",\"sfzzp\":\"\"}]";

    private OkioSocket socket;
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    private ScheduledExecutorService scheduledService = Executors.newScheduledThreadPool(5);
    private ExecutorService service = Executors.newCachedThreadPool();
    private TestDao testDao;
    private Object object = new Object();
    private boolean isPause;
    private OkWebSocket okWebsocket;
    

    @Override
    protected void initView() {
        OkEvent.getInstance().register(this);
        counter = Counter.create();
        ViewFinder.inject(this);
        testDao = App.getContext().getAppDatabase().testDao();
//        manager = DownloadManager.getInstance();
//        manager.setObserverProgress(this);
//        openWifi(context);
//        socket = new OkioSocket("172.16.41.42", 8889);

        btnHello.setText("START");

        clHello.post(() -> {
            width = clHello.getMeasuredWidth();
        });

//        Intent intent = new Intent(this, NetworkService.class);
//        startService(intent);
        layoutManager1 = new CenterLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        timeLineAdapter = new TimeLineAdapter(R.layout.item_timeline, timeLines);
        rvTimeline.setLayoutManager(layoutManager1);
        rvTimeline.addItemDecoration(new DividerItemDecoration());
//        new LinearSnapHelper().attachToRecyclerView(rvTimeline);

        rvTimeline.setAdapter(timeLineAdapter);
        loadProgress();
    }

    @OnClick(R.id.bn_hello)
    @SingleClick
    void onStart(View view){
//        Map<String, Object> map = new HashMap<>();
//        map.put("username", "liuzhao");
//        presenter.onLogin(map);
//        manager.start();
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
//        int size = testDao.getAll().size();
        Toast.makeText(context, "hello", Toast.LENGTH_SHORT).show();
//        service.execute(() -> {
//            synchronized (object) {
//                while (true) {
//                    try {
//                        if (isPause) object.wait();
//                        Thread.sleep(1000);
//                        Log.e(TAG, "扫描中...");
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//
//        });

        okWebsocket = OkWebSocket.instance()
                .url("ws://172.16.41.35:8080/server/websocket")
                .callback(this)
                .connect();


    }

    @OnClick(R.id.btn_send)
    void send(View v) {
        String message = editText.getText().toString();
        if (TextUtils.isEmpty(message) && okWebsocket == null) return;
        okWebsocket.send(message);
        editText.setText("");
    }

    @Override
    public void onMessage(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.bn_save)
    public void onSave(View v) {

        Promise.of(() -> {
            int result = -1;
            synchronized (timeLines) {
                for (int i = 0; i < timeLines.size(); i++) {
                    if (timeLines.get(i).current) result = i;
                }
                return result;
            }

        }).ui(result -> {
//            if (result < 2) rvTimeline.smoothScrollToPosition(0);
//            else rvTimeline.smoothScrollToPosition(result + 1);
            layoutManager1.smoothScrollToPosition(rvTimeline, new RecyclerView.State(), result);
        }).make();

    }

    private void startScan() {
        synchronized (object) {
            isPause = false;
            object.notify();
        }
    }

    private void pauseScan() {
        isPause = true;
    }

    class TimeLineAdapter extends BaseQuickAdapter<TimeLine, BaseViewHolder> {

        public TimeLineAdapter(int layoutResId, @Nullable List<TimeLine> data) {
            super(layoutResId, data);
        }

        @Override
        protected void convert(BaseViewHolder helper, TimeLine item) {
            if (item != null) {
            }
            helper.setText(R.id.tv_content, "" + item.content);
        }
    }

    class DividerItemDecoration extends RecyclerView.ItemDecoration {
        private final static int MARGIN = 35;
        private int top = 40, left = MARGIN, right = MARGIN;
        private final static int radius = 6;
        private final static int SPACE_WIDTH = radius + 5;
        private Paint paint, paint1, paint2;
        private PathEffect pathEffect;

        public DividerItemDecoration() {
            pathEffect = new DashPathEffect(new float[]{5, 3}, 0);
            paint = new Paint();
            paint.setColor(Color.BLUE);
            paint.setAntiAlias(true);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(1);
            paint.setPathEffect(pathEffect);


            paint2 = new Paint();
            paint2.setColor(Color.BLUE);
            paint2.setAntiAlias(true);
            paint2.setStrokeWidth(1);
            paint2.setStyle(Paint.Style.FILL_AND_STROKE);

            paint1 = new Paint();
            paint1.setColor(Color.BLUE);
            paint1.setAntiAlias(true);
            paint1.setTextSize(15);
        }

        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);
            int index = parent.getChildAdapterPosition(view);
            List<TimeLine> list = ((TimeLineAdapter) parent.getAdapter()).getData();

            if (index == 0) outRect.set(10, top, right, 10);
            else if (index == list.size() - 1) outRect.set(left, top, 10, 10);
            else outRect.set(left, top, right, 10);

        }


        @Override
        public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            super.onDraw(c, parent, state);
            int childCount = parent.getChildCount();
            TimeLineAdapter adapter = (TimeLineAdapter) parent.getAdapter();
            int index;
            List<TimeLine> list = adapter.getData();
            for (int i = 0; list.size() > 0 && i < childCount; i++) {
                View view = parent.getChildAt(i);
                index = parent.getChildAdapterPosition(view);
                TimeLine timeLine = list.get(index);
                int leftOfView = view.getLeft();
                int rightOfView = view.getRight();
                float y = view.getY() - 10;
                float x = (view.getLeft() + view.getRight()) >> 1;
                c.drawCircle(x, y, radius, paint2);

                float startX = leftOfView - left;
                float endX = rightOfView + right;
                if (index != 0) {
                    Path path = new Path();
                    path.moveTo(startX, y);
                    path.lineTo(x - SPACE_WIDTH, y);
                    c.drawPath(path, timeLine.current ? paint2 : paint);
                }
                if (index != list.size() - 1) {
                    Path path = new Path();
                    path.moveTo(x + SPACE_WIDTH, y);
                    path.lineTo(endX, y);
                    c.drawPath(path, timeLine.current ? paint2 : paint);
                }
                float textWidth = paint1.measureText(timeLine.time);
                float textX = x - (textWidth / 2);
                c.drawText(timeLine.time, textX, y - 15, paint1);
            }

        }
    }

    static class TimeLine {
        String content;
        String time;
        boolean current;

        public TimeLine(String content, String time, boolean current) {
            this.content = content;
            this.time = time;
            this.current = current;
        }
    }

    public class CenterLayoutManager extends LinearLayoutManager {

        public CenterLayoutManager(Context context, int orientation, boolean reverseLayout) {
            super(context, orientation, reverseLayout);
        }

        @Override
        public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
            RecyclerView.SmoothScroller smoothScroller = new CenterSmoothScroller(recyclerView.getContext());
            smoothScroller.setTargetPosition(position);
            startSmoothScroll(smoothScroller);
        }

        private class CenterSmoothScroller extends LinearSmoothScroller {
            CenterSmoothScroller(Context context) {
                super(context);
            }

            @Override
            public int calculateDtToFit(int viewStart, int viewEnd, int boxStart, int boxEnd, int snapPreference) {
                return (boxStart + (boxEnd - boxStart) / 2) - (viewStart + (viewEnd - viewStart) / 2);
            }

            @Override
            protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                return 0.2f;
            }
        }

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
        Map<String, Object> map = new HashMap<>();
//        map.put("username", "jyd");
//        map.put("password", "jyd");
//        map.put("harddata", "BD8A11910051");
//        map.put("taskcode", "jyd");

        map.put("schoolcode", "86.63.27.003");
        map.put("taskcode", "1");

//        Http.instance().postMap("login/appDoLogin", map, new PostCallback<UserBean>() {
//            @Override
//            public void success(UserBean userBean) {
//                Toast.makeText(context, userBean.getAccount(), Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void fail(String err) {
//                Toast.makeText(context, err, Toast.LENGTH_SHORT).show();
//            }
//        });
//        Http.instance().download("dataApi/androidDB", map, new DownloadListener() {
//            @Override
//            public void onStart() {
//                Toast.makeText(context, "开始下载", Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void onProgress(int progress) {
//                progressBar.setProgress(progress);
//                tvHello.setText("" + progress + "%");
//            }
//
//            @Override
//            public void onFinish() {
//                Toast.makeText(context, "下载完成", Toast.LENGTH_SHORT).show();
//
//            }
//
//            @Override
//            public void onFail(Throwable t) {
//                Toast.makeText(context, t.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });
//        pauseScan();
        okWebsocket.disconnect();

    }

    @OnClick(R.id.bn_playlist)
    void onToPlaylist(View view){
//        Intent intent = new Intent();
//        intent.setClass(this, PlaylistActivity.class);
//        startActivity(intent);
//        if (!EasyPermissions.hasPermissions(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)){
//            EasyPermissions.requestPermissions(this, "请求存储权限", WRITE_STORAGE_CODE
//                    , Manifest.permission.WRITE_EXTERNAL_STORAGE);
//            return;
//        }
//        File file = new File(Constants.DATA_PATH);
//        Http.instance().upload("upload", file, new UploadListener<BaseResult>() {
//            @Override
//            public void onStart() {
//                Toast.makeText(context, "开始上传", Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void onProgress(int progress) {
//                progressBar.setProgress(progress);
//                tvHello.setText("" + progress + "%");
//            }
//
//            @Override
//            public void onFinish(BaseResult baseResult) {
//                Toast.makeText(context, "下载完成:" + baseResult.getCode(), Toast.LENGTH_SHORT).show();
//
//            }
//
//            @Override
//            public void onFail(Throwable t) {
//                Toast.makeText(context, t.getMessage(), Toast.LENGTH_SHORT).show();
//
//            }
//        });
//        startScan();
        loadProgress();

    }

    private void loadProgress() {
        timeLines.clear();
        int length = (int) (Math.random() * 10) + 1;
        Promise.of(() -> {
            for (int j = 0; j < length; j++) {
                TimeLine timeLine = new TimeLine("进程" + j, String.format("%d:00", j), j < 5);
                timeLines.add(timeLine);
            }
            return 1;
        }).ui(i -> {
            timeLineAdapter.notifyDataSetChanged();
        }).make();
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
