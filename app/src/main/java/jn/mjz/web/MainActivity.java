package jn.mjz.web;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.LinkedList;

import jn.mjz.web.DateBase.Adapter.HistoryListViewAdapter;
import jn.mjz.web.DateBase.Adapter.ListViewAdapter;
import jn.mjz.web.DateBase.Collection;
import jn.mjz.web.DateBase.History;
import jn.mjz.web.DateBase.HistoryDataBaseHelper;
import jn.mjz.web.Util.GlobalUtil;


public class MainActivity extends AppCompatActivity {
    private long exitTime = 0, historyPopTime = 0;
    private WebView mWv = null;
    private ProgressBar mPb = null;
    private String stringUrl = "", stringEt = "", stringName = "";
    private EditText mEt = null;
    private TextView mTvMyCollections, mTvNoHistories;
    private DrawerLayout mDl = null;
    private Button mBtnLeft = null, mBtnPop = null, mBtnCollect = null, mBtnClear = null;
    private ListView mLv = null;
    private ListView mLvHistory = null;
    private PopupMenu mPm = null;
    private PopupWindow mPwHistory = null;
    private InputMethodManager mInputMethodManager = null;
    private String[] names = {""};
    private String[] urls;
    private String[] historyName;
    private CollectDialog collectDialog;
    private int selectCollectionNum = 0, selectHistoryNum = 0;
    private TextView mTvNoCollectionOne, mTvNoCollectionTwo;
    private View popUpWindowView;
    private float x1 = 0, x2 = 0;
    private SwipeRefreshLayout mSRL = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GlobalUtil.getInstance().setContext(MainActivity.this);
        if (TextUtils.isEmpty(GlobalUtil.getInstance().sharedPreferences.getString("homeUrl", null))) {
            GlobalUtil.getInstance().editor.putString("homeUrl", getString(R.string.defHomeUrl));
        }
        GlobalUtil.getInstance().editor.apply();
        initViews();
        mWv.requestFocus();
        initWebView();
        setListeners();
        getCollections();
    }

    //按钮点击事件
    class OnClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_main_open_left_view:
                    mDl.openDrawer(Gravity.START);
                    break;
                case R.id.btn_main_collect:
                    initCollectDialog();
                    String url = mWv.getUrl();

                    final Collection collection = GlobalUtil.getInstance().collectionDataBaseHelper.haveCollection(url);
                    if (collection != null) {
                        collectDialog.setTitleString("编辑").setConfirmString("完成").setCancelString("取消收藏").setStringName(collection.getName()).show();
                        collectDialog.hideUrl();
                        final Collection newCollection = new Collection();

                        newCollection.setUuid(collection.getUuid());
                        newCollection.setUrl(collection.getUrl());
                        newCollection.setTime(collection.getTime());
                        newCollection.setDate(collection.getDate());

                        collectDialog.setIOnConfirmClickListener(new CollectDialog.IOnConfirmClickListener() {
                            @Override
                            public void onConfirmClick() {
                                newCollection.setName(collectDialog.getStringName());
                                GlobalUtil.getInstance().collectionDataBaseHelper.editCollection(collection.getUuid(), newCollection);
                                getCollections();
                                collectDialog.dismiss();
                            }
                        }).setIOnCancelClickListener(new CollectDialog.IOnCancelClickListener() {
                            @Override
                            public void onCancelClick() {
                                GlobalUtil.getInstance().collectionDataBaseHelper.removeCollection(collection.getUuid());
                                Toast.makeText(MainActivity.this, "取消收藏成功", Toast.LENGTH_SHORT).show();
                                collectDialog.dismiss();
                                getCollections();
                            }
                        });
                    } else {
                        collectDialog.show();
                    }
                    break;
                case R.id.btn_layout_clear_edit_text:
                    mEt.setText("");
                    break;
                case R.id.btn_main_menu:
                    if (mPm == null) {
                        mPm = new PopupMenu(MainActivity.this, mBtnPop);
                        mPm.getMenuInflater().inflate(R.menu.menu_pop_menu, mPm.getMenu());
                        mPm.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                switch (item.getItemId()) {
                                    case R.id.item_menu_pop_up_refresh:
                                        mWv.reload();
                                        break;
                                    case R.id.item_menu_pop_up_forward:
                                        if (mWv.canGoForward()) {
                                            mWv.goForward();
                                        } else {
                                            Toast.makeText(MainActivity.this, "不能再前进啦", Toast.LENGTH_SHORT).show();
                                        }
                                        break;
                                    case R.id.item_menu_pop_up_back:
                                        if (mWv.canGoBack()) {
                                            mWv.goBack();
                                        } else {
                                            Toast.makeText(MainActivity.this, "不能再后退啦", Toast.LENGTH_SHORT).show();
                                        }
                                        break;
                                    case R.id.item_menu_pop_up_exit:
                                        finish();
                                        break;
                                    case R.id.item_menu_pop_up_home:
                                        mWv.loadUrl(GlobalUtil.getInstance().sharedPreferences.getString("homeUrl", getString(R.string.defHomeUrl)));
                                        break;
                                    case R.id.item_menu_pop_up_share:
                                        shareWeb();
                                        break;
                                    case R.id.item_menu_pop_up_open_with_other:
                                        Uri uri = Uri.parse(mWv.getUrl());
                                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                        startActivity(intent);
                                        break;
                                    case R.id.item_menu_pop_up_setting:
                                        Intent intent1 = new Intent(MainActivity.this, SettingActivity.class);
                                        startActivity(intent1);
                                        break;
                                }
                                return true;
                            }
                        });
                        mPm.show();
                    } else {
                        mPm.show();
                    }
                    break;
                case R.id.tv_main_my_collections:
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("来自菜鸡开发者:").setMessage(R.string.tips).setNegativeButton("知道了", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    }).setCancelable(false).show();
                    break;
            }
        }
    }

    //文编编辑框监听事件
    class OnEditorActionListener implements EditText.OnEditorActionListener {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

            if (actionId == EditorInfo.IME_ACTION_SEND
                    || actionId == EditorInfo.IME_ACTION_DONE
                    || (event != null && KeyEvent.KEYCODE_ENTER == event.getKeyCode() && KeyEvent.ACTION_DOWN == event.getAction())) {
                //处理软键盘回车事件
                stringEt = mEt.getText().toString();
                if (!stringEt.equals("")) {
                    if (stringEt.startsWith("http:") || stringEt.startsWith("https:")) {
                        mWv.loadUrl(stringEt);
                    } else {
                        addHistory();
                        mWv.loadUrl("https://m.baidu.com/s?ie=utf-8&f=8&rsv_bp=1&tn=baidu&wd=" + stringEt + "&oq=abc&rsv_pq=bafe4d2100096a4c&rsv_t=13241Mith9YYNFtBBsI086dqYMbFG33cx7iHB1k7rNEc8NdAfFaAJL08pss&rqlang=cn&rsv_enter=0");
                        mEt.setText("");
                        mEt.setHint("正在搜索“" + stringEt + "”请稍等...");
                    }
                    //按下回车后关闭软键盘
                    if (mInputMethodManager == null) {
                        mInputMethodManager = (InputMethodManager) MainActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                    }
                    if (mInputMethodManager.isActive()) {
                        mInputMethodManager.hideSoftInputFromWindow(mEt.getWindowToken(), 0);
                    }
                } else {
                    Toast.makeText(MainActivity.this, "输入不能为空!", Toast.LENGTH_SHORT).show();
                }
            }
            return true;
        }

    }

    //初始化view
    private void initViews() {
        mTvNoCollectionOne = findViewById(R.id.text_view_no_collection_one);
        mTvNoCollectionTwo = findViewById(R.id.text_view_no_collection_two);
        mTvNoHistories = findViewById(R.id.text_view_no_history);
        mWv = findViewById(R.id.wv_main);
        mEt = findViewById(R.id.et_main_search);
        mBtnLeft = findViewById(R.id.btn_main_open_left_view);
        mBtnPop = findViewById(R.id.btn_main_menu);
        mBtnCollect = findViewById(R.id.btn_main_collect);
        mLv = findViewById(R.id.lv_main);
        mLvHistory = findViewById(R.id.list_view_history);
        mDl = findViewById(R.id.dl_main);
        mDl.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        mPb = findViewById(R.id.pb_main_progress);
        mEt.setImeOptions(EditorInfo.IME_ACTION_SEND);
        mBtnClear = findViewById(R.id.btn_layout_clear_edit_text);
        mTvMyCollections = findViewById(R.id.tv_main_my_collections);
        mSRL = findViewById(R.id.SwipeRefresh);
        // 使通知栏透明化
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
    }

    //读取保存的数据并赋给两个字符串数组并且刷新收藏ListView
    public void getCollections() {

        if (GlobalUtil.getInstance().collectionDataBaseHelper.haveCollection(mWv.getUrl()) != null) {
            mBtnCollect.setForeground(getDrawable(R.drawable.ic_collect));
        } else {
            mBtnCollect.setForeground(getDrawable(R.drawable.ic_no_collect));
        }

        int max = GlobalUtil.getInstance().collectionDataBaseHelper.getCount();
        if (max > 0) {
            //有Item时隐藏
            mLv.setVisibility(View.VISIBLE);
            mTvNoCollectionOne.setText("");
            mTvNoCollectionTwo.setText("");
            LinkedList<Collection> collections = GlobalUtil.getInstance().collectionDataBaseHelper.readCollections();
            names = new String[max];
            urls = new String[max];
            for (int i = 0; i < max; i++) {
                names[i] = collections.get(i).getName();
                urls[i] = collections.get(i).getUrl();
            }

            mLv.setAdapter(new ListViewAdapter(MainActivity.this, names, new ListViewAdapter.IOnItemClickListener() {
                @Override
                public void onItemClick(int index) {
                    mWv.loadUrl(urls[index]);
                    mDl.closeDrawer(Gravity.START);
                }
            }, new ListViewAdapter.IOnItemLongClickListener() {
                @Override
                public void onItemLongClick(int index) {
                    selectCollectionNum = index;
                    initDialog();
                }
            }));
        } else {
            mLv.setVisibility(View.INVISIBLE);
            mTvNoCollectionOne.setText("还没有收藏记录");
            mTvNoCollectionTwo.setText("试试点击右上角的五角星");
        }
    }

    //读取保存的数据并赋给两个字符串数组并且刷新历史记录ListView
    public void getHistories() {
        int max = GlobalUtil.getInstance().historyDataBaseHelper.getCount();
        TextView mTvClearHistory = popUpWindowView.findViewById(R.id.text_view_clear_all);
        TextView mTvNoHistory = popUpWindowView.findViewById(R.id.text_view_no_history);
        mLvHistory = popUpWindowView.findViewById(R.id.list_view_history);
        if (max > 0) {
            //有Item时隐藏
            mTvNoHistory.setVisibility(View.GONE);
            mLvHistory.setVisibility(View.VISIBLE);
            LinkedList<History> histories = GlobalUtil.getInstance().historyDataBaseHelper.readHistory();
            historyName = new String[max];
            for (int i = 0; i < max; i++) {
                historyName[i] = histories.get(i).getName();
            }

            mTvClearHistory.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //清空表
                    final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this)
                            .setTitle("确定清空所有历史记录?")
                            .setPositiveButton("清空", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    mPwHistory.dismiss();
                                    GlobalUtil.getInstance().historyDataBaseHelper.getWritableDatabase().delete(HistoryDataBaseHelper.TABLE_NAME, null, null);
                                }
                            })
                            .setNeutralButton("我再想想", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });
                    builder.show();
                }
            });
            mLvHistory.setAdapter(new HistoryListViewAdapter(MainActivity.this, historyName, new HistoryListViewAdapter.IOnItemClickListener() {
                @Override
                public void onItemClick(int index) {
                    //百度搜索
                    //To-Do:
                    selectHistoryNum = index;
                    mEt.setText(GlobalUtil.getInstance().historyDataBaseHelper.haveHistory(historyName[index]).getName());
                    mPwHistory.dismiss();
                }
            }, new HistoryListViewAdapter.IOnItemLongClickListener() {
                @Override
                public void onItemLongClick(int index) {
                    selectHistoryNum = index;
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("确定删除 " + historyName[index])
                            .setNeutralButton("我再想想", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .setPositiveButton("删除", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    GlobalUtil.getInstance().historyDataBaseHelper.removeHistory(GlobalUtil.getInstance().historyDataBaseHelper.haveHistory(historyName[selectHistoryNum]).getUuid());
                                    Toast.makeText(MainActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
                                    mPwHistory.dismiss();
                                    initHistoryPopUpWindow();
                                }
                            }).show();

                    //删除对话框
                    //To-Do:
                }
            }));
        } else {
            mLvHistory.setVisibility(View.GONE);
            mTvClearHistory.setVisibility(View.GONE);
        }
    }

    //初始化收藏dialog
    void initCollectDialog() {
        stringName = mWv.getTitle();
        stringUrl = mWv.getUrl();
        if (stringName == null) {
            stringName = "正在获取网页标题";
        }
        collectDialog = new CollectDialog(MainActivity.this, new CollectDialog.IOnCancelClickListener() {
            @Override
            public void onCancelClick() {
                collectDialog.dismiss();
            }
        }, new MyOnConfirmClick()).setStringName(stringName).setStringUrl(stringUrl);
    }

    //初始化名且显示编辑dialog
    void initDialog() {
        AlertDialog.Builder builder;
        final Collection selectedCollection = GlobalUtil.getInstance().collectionDataBaseHelper.readCollections().get(selectCollectionNum);

        builder = new AlertDialog.Builder(MainActivity.this).setItems(new CharSequence[]{"编辑", "设为主页", "取消收藏"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int which) {
                //编辑
                if (which == 0) {
                    initCollectDialog();
                    collectDialog.setIOnConfirmClickListener(new CollectDialog.IOnConfirmClickListener() {
                        @Override
                        public void onConfirmClick() {
                            String url = collectDialog.getStringUrl();
                            final Collection collection = GlobalUtil.getInstance().collectionDataBaseHelper.haveCollection(url);
                            Collection newCollection = new Collection();
                            if (collection != null) {
                                newCollection.setUuid(collection.getUuid());
                                newCollection.setUrl(collection.getUrl());
                                newCollection.setTime(collection.getTime());
                                newCollection.setDate(collection.getDate());
                                newCollection.setName(collectDialog.getStringName());
                                GlobalUtil.getInstance().collectionDataBaseHelper.editCollection(collection.getUuid(), newCollection);
                                GlobalUtil.getInstance().collectionDataBaseHelper.removeCollection(selectedCollection.getUuid());
                            } else {
                                newCollection.setName(collectDialog.getStringName());
                                newCollection.setUrl(collectDialog.getStringUrl());
                                GlobalUtil.getInstance().collectionDataBaseHelper.addCollection(newCollection);
                            }
                            getCollections();
                            collectDialog.dismiss();
                        }
                    }).setStringName(selectedCollection.getName()).setStringUrl(selectedCollection.getUrl()).setTitleString("编辑").setConfirmString("完成").show();
                } else if (which == 2) {
                    GlobalUtil.getInstance().collectionDataBaseHelper.removeCollection(selectedCollection.getUuid());
                    getCollections();
                    Toast.makeText(MainActivity.this, "取消收藏成功", Toast.LENGTH_SHORT).show();
                } else {
                    GlobalUtil.getInstance().editor.putString("homeUrl", selectedCollection.getUrl());
                    GlobalUtil.getInstance().editor.apply();
                    Toast.makeText(MainActivity.this, "设置成功", Toast.LENGTH_SHORT).show();
                }
            }
        }).setTitle(selectedCollection.getName());
        builder.show();
    }

    //初始化历史记录PopUpWindow
    void initHistoryPopUpWindow() {
        mPwHistory = new PopupWindow(MainActivity.this);
        popUpWindowView = LayoutInflater.from(MainActivity.this).inflate(R.layout.layout_pop_up_history, null);
        getHistories();
        if (GlobalUtil.getInstance().historyDataBaseHelper.getCount() >= 9) {
            mPwHistory.setHeight(660);
        }
        mPwHistory.setContentView(popUpWindowView);
        mPwHistory.setWidth(mEt.getWidth());
        mPwHistory.setOutsideTouchable(true);
        mPwHistory.showAsDropDown(mEt, 0, 30, Gravity.CENTER);
    }

    //保存到数据库Collection
    private void addCollection() {
        Collection collection = new Collection();
        collection.setName(collectDialog.getStringName());
        collection.setUrl(collectDialog.getStringUrl());
        GlobalUtil.getInstance().collectionDataBaseHelper.addCollection(collection);
        Toast.makeText(MainActivity.this, "收藏成功", Toast.LENGTH_SHORT).show();
        getCollections();
    }

    //保存到数据库History
    private void addHistory() {
        History history = new History();
        history.setName(mEt.getText().toString());
        GlobalUtil.getInstance().historyDataBaseHelper.addHistory(history);
    }

    //返回键后退网页和返回键退出
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (!mDl.isDrawerOpen(Gravity.START)) {
                    if (mWv.canGoBack()) {
                        mWv.goBack();
                    } else {
                        if ((System.currentTimeMillis() - exitTime) > 2000) {
                            Toast.makeText(MainActivity.this, "再按一次退出", Toast.LENGTH_SHORT).show();
                            exitTime = System.currentTimeMillis();
                        } else {
                            finish();
                        }
                    }
                } else {
                    mDl.closeDrawer(Gravity.START);
                }
                break;
        }
        return true;
    }

    //设置监听事件
    private void setListeners() {
        OnClick onClick = new OnClick();
        OnEditorActionListener onEditorActionListener = new OnEditorActionListener();
        mBtnLeft.setOnClickListener(onClick);
        mBtnCollect.setOnClickListener(onClick);
        mBtnPop.setOnClickListener(onClick);
        mEt.setOnEditorActionListener(onEditorActionListener);
        mBtnClear.setOnClickListener(onClick);
        mTvMyCollections.setOnClickListener(onClick);
        mEt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    historyPopTime = System.currentTimeMillis();
                }
                if (event.getAction() == MotionEvent.ACTION_UP && (System.currentTimeMillis() - historyPopTime) < 500) {
                    initHistoryPopUpWindow();
                }
                return false;
            }
        });
        mWv.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        x1 = event.getX();
                        break;
                    case MotionEvent.ACTION_UP:
                        x2 = event.getX();
                        scrollBackOrForward(x1, x2);
                        break;
                }
                return false;
            }
        });
        mDl.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View view, float v) {

            }

            @Override
            public void onDrawerOpened(@NonNull View view) {
                mDl.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            }

            @Override
            public void onDrawerClosed(@NonNull View view) {
                mDl.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            }

            @Override
            public void onDrawerStateChanged(int i) {

            }
        });
        mSRL.setColorSchemeResources(
                android.R.color.holo_blue_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_green_dark,
                android.R.color.holo_red_dark);
        mSRL.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mWv.reload();
            }
        });
    }

    //分享网页
    private void shareWeb() {
        Intent textIntent = new Intent(Intent.ACTION_SEND);
        textIntent.setType("text/plain");
        textIntent.putExtra(Intent.EXTRA_TEXT, mWv.getTitle() + "\n" + mWv.getUrl());
        startActivity(Intent.createChooser(textIntent, mWv.getTitle()));
    }

    //初始化WebView
    private void initWebView() {
        WebSettings webSettings = mWv.getSettings();
        //设置可访问文件
        webSettings.setAllowContentAccess(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setAppCacheEnabled(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setDomStorageEnabled(true);//开启DOM缓存
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSettings.setBlockNetworkImage(false);
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);//允许混合http和https
        webSettings.setLoadsImagesAutomatically(true);//自动加载图片
        webSettings.setDefaultTextEncodingName("utf-8");//设置默认的文本编码
        webSettings.setJavaScriptEnabled(true);//支持js
        webSettings.setSupportZoom(true);//支持缩放
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        mWv.setWebViewClient(new MyWebViewClient());//不跳转到其他浏览器
        mWv.setWebChromeClient(new MyWebChromeClient());
        mWv.loadUrl(GlobalUtil.getInstance().sharedPreferences.getString("homeUrl", getString(R.string.defHomeUrl)));//加载主页
    }

    class MyWebChromeClient extends WebChromeClient {
        //进度条
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            if (newProgress < 100) {
                mPb.setProgress(newProgress);
            } else {
                mPb.setProgress(0);
                mSRL.setRefreshing(false);
            }
        }

        //接收到标题
        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
            if (GlobalUtil.getInstance().collectionDataBaseHelper.haveCollection(view.getUrl()) != null) {
                mBtnCollect.setForeground(getDrawable(R.drawable.ic_collect));
            } else {
                mBtnCollect.setForeground(getDrawable(R.drawable.ic_no_collect));
            }
            stringName = title;
            if (collectDialog != null) {
                if (collectDialog.isShowing()) {
                    Toast.makeText(MainActivity.this, "成功获取网页标题：" + title, Toast.LENGTH_LONG).show();
                    collectDialog.dismiss();
                    initCollectDialog();
                    collectDialog.show();
                }
            }
        }
    }

    class MyWebViewClient extends WebViewClient {

        //不跳转到其他浏览器
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            String url = request.getUrl().toString();
            if (url.startsWith("http:") || url.startsWith("https:")) {
                view.loadUrl(url);
            } else {//非http或者https的网络请求拦截，用action_view启动。可能报错。 
                try {
                    Uri uri = Uri.parse(url);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                    if (url.startsWith("alipay")) {
                        Toast.makeText(MainActivity.this, "请确认是否安装支付宝", Toast.LENGTH_SHORT).show();
                    } else if (url.startsWith("mqqwpa")) {
                        Toast.makeText(MainActivity.this, "请确认是否安装QQ", Toast.LENGTH_SHORT).show();
                    } else if (url.startsWith("weixin")) {
                        Toast.makeText(MainActivity.this, "请确认是否安装微信", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            return true;
        }

        //接收到错误信息
        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
            Log.e("onReceivedError: ", error.getDescription().toString() + "\nerrorCode:" + error.getErrorCode());
            if (error.getErrorCode() == -2) {
                Toast.makeText(MainActivity.this, "呀,没网了!\n玩玩小游戏吧", Toast.LENGTH_SHORT).show();
                mWv.loadUrl("file:///android_asset/2048/index.html");
            }
        }

        //页面开始加载
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            stringUrl = url;
        }

        //页面加载完成
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            stringName = view.getTitle();
            mEt.setHint("请输入想要百度的内容");
        }
    }

    private class MyOnConfirmClick implements CollectDialog.IOnConfirmClickListener {
        @Override
        public void onConfirmClick() {
            stringName = collectDialog.getStringName();
            stringUrl = collectDialog.getStringUrl();
            if (!TextUtils.isEmpty(stringName) && !TextUtils.isEmpty(stringUrl)) {
                //添加到收藏夹
                addCollection();
                collectDialog.dismiss();
            } else if (TextUtils.isEmpty(stringName) && TextUtils.isEmpty(stringUrl)) {
                Toast.makeText(MainActivity.this, "请输入名称和网址", Toast.LENGTH_SHORT).show();
            } else if (TextUtils.isEmpty(stringName)) {
                Toast.makeText(MainActivity.this, "请输入名称", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "请输入网址", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void scrollBackOrForward(float x1, float x2) {
        Display display = getWindowManager().getDefaultDisplay();
        Point outSize = new Point();
        display.getSize(outSize);
        int x = outSize.x;
        int y = outSize.y;
        if (x2 - x1 >= 300) {
            if (x1 <= 70) {
                if (mWv.canGoBack()) {
                    mWv.goBack();
                } else {
                    if ((System.currentTimeMillis() - exitTime) > 2000) {
                        Toast.makeText(MainActivity.this, "再滑一次退出", Toast.LENGTH_SHORT).show();
                        exitTime = System.currentTimeMillis();
                    } else {
                        finish();
                    }
                }
            }
        } else if (x1 - x2 >= 300) {
            if (x1 >= x - 70) {
                if (mWv.canGoForward()) {
                    mWv.goForward();
                } else {
                    Toast.makeText(MainActivity.this, "不能再前进了", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
