package com.advanpro.fwtools.fwdebug;

import android.app.Activity;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.HandlerThread;
import android.os.Process;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import com.advanpro.fwtools.R;
import com.advanpro.fwtools.ble.BleDataParser;
import com.advanpro.fwtools.ble.BleDevice;
import com.advanpro.fwtools.common.view.TitleBar;

import java.util.HashMap;
import java.util.List;

/**
 * Created by AdvanPro on 2016/6/15.
 */
public class FwDebugActivity extends Activity
        implements ListView.OnItemClickListener, FwDataAsyncHandler.CompleteListener {

    private HandlerThread  mHandlerThread;
    private FwDataAsyncHandler mTaskHandler;
    private BleDevice  mDevice;

    private TitleBar mTitleBar;
    private ListView mListView;
    private ImageView mProgressView;
    private int mlistType;
    private int mDayn;
    private IFwdebugListAdapter mCurrentAdapter;
    private HashMap<String, IFwdebugListAdapter> mAdapterMap = new HashMap<String, IFwdebugListAdapter>();

    private final String[] mMainSelcetItem = {"运动概况", "运动详情", "存储变量"};
    public static final int LIST_TYPE_MAIN = 0;
    public static final int LIST_TYPE_SUMMARY = 1;
    public static final int LIST_TYPE_DETAIL = 2;
    public static final int LIST_TYPE_KEYVALUE = 3;
    public static final int LIST_TYPE_DETAIL_DAYN = 4;
    public static final int LIST_TYPE_FOOTPRESS = 5;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDevice = (BleDevice)getIntent().getParcelableExtra("device");
        setContentView(R.layout.activity_fwdebug);

        mTitleBar = (TitleBar)findViewById(R.id.title_bar);
        mProgressView = (ImageView)findViewById(R.id.fwdebug_progress);
        mListView = (ListView)findViewById(R.id.fwdbug_listview);
        setTitle("调试固件");
        initListView(LIST_TYPE_MAIN, null);
        startAsyncLooper();
        BleDataParser.getParser(mDevice).enterFwdebugMode(mTaskHandler);
    }

    private void startAsyncLooper() {
        mHandlerThread = new HandlerThread("FwDebugActivity", Process.THREAD_PRIORITY_BACKGROUND);
        mHandlerThread.start();
        mTaskHandler = new FwDataAsyncHandler(mHandlerThread.getLooper(), mDevice);
    }

    private void initListView(int type, List data) {
        switch(type) {
        case LIST_TYPE_MAIN:
            mCurrentAdapter = mAdapterMap.get("main");
            if(mCurrentAdapter == null) {
                mCurrentAdapter = new DebugSelectAdapter(this, mMainSelcetItem);
                mAdapterMap.put("main", mCurrentAdapter);
            }
            break;
        case LIST_TYPE_SUMMARY:
            mCurrentAdapter = mAdapterMap.get("summary");
            if(mCurrentAdapter == null) {
                mCurrentAdapter = new SumInfoAdapter(this);
                mAdapterMap.put("summary", mCurrentAdapter);
            }
            break;
        case LIST_TYPE_DETAIL:
            mCurrentAdapter = mAdapterMap.get("detail");
            if(mCurrentAdapter == null) {
                mCurrentAdapter = new DetailSelectAdapter(this);
                mAdapterMap.put("detail", mCurrentAdapter);
            }
            break;
        case LIST_TYPE_KEYVALUE:
            mCurrentAdapter = mAdapterMap.get("keyvalue");
            if(mCurrentAdapter == null) {
                mCurrentAdapter = new KeyValueAdapter(this);
                mAdapterMap.put("keyvalue", mCurrentAdapter);
            }
            break;
        case LIST_TYPE_DETAIL_DAYN:
            mCurrentAdapter = mAdapterMap.get("dayn");
            if(mCurrentAdapter == null) {
                mCurrentAdapter = new DetailInfoAdapter(this);
                mAdapterMap.put("dayn", mCurrentAdapter);
            }
            break;
        case LIST_TYPE_FOOTPRESS:
                mCurrentAdapter = mAdapterMap.get("footpress");
                if(mCurrentAdapter == null) {
                    mCurrentAdapter = new FootpressAdapter(this);
                    mAdapterMap.put("footpress", mCurrentAdapter);
                }
                break;
        default:
            return;
        }
        if(data != null) {
            mCurrentAdapter.updateData(data);
        }
        mListView.setAdapter(mCurrentAdapter);
        if(type == LIST_TYPE_MAIN) {
            mListView.setOnItemClickListener(this);
        } else {
            mListView.setOnItemClickListener(null);
        }
        mlistType = type;
    }

    @Override
    protected void onDestroy() {
        mHandlerThread.quit();
        BleDataParser.getParser(mDevice).exitFwdebugMode();
        super.onDestroy();
    }

    private void showProgess(boolean show) {
        if(show) {
            mProgressView.setVisibility(View.VISIBLE);
            ((AnimationDrawable) mProgressView.getBackground()).start();
        } else {
            ((AnimationDrawable) mProgressView.getBackground()).stop();
            mProgressView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
        mListView.setVisibility(View.GONE);
        showProgess(true);
        mTaskHandler.startParseMessage(pos + 1, 0, 0, this);
        setTitle(mMainSelcetItem[pos]);
    }

    public void setTitle(String title) {
        mTitleBar.setTitle(title);
    }

    public void loadDayDetail(int part) {
        mListView.setVisibility(View.GONE);
        showProgess(true);
        mDayn = part&0x0f;
        mTaskHandler.startParseMessage(LIST_TYPE_DETAIL_DAYN, part, 0, this);
    }

    public void loadDayDetail(int part, FwDataAsyncHandler.CompleteListener listener) {
        mTaskHandler.startParseMessage(LIST_TYPE_DETAIL_DAYN, part, 0, listener);
    }

    public void loadFootPress(int addr, int len) {
        mListView.setVisibility(View.GONE);
        showProgess(true);
        mTaskHandler.startParseMessage(LIST_TYPE_FOOTPRESS, addr, len, this);
    }

    @Override
    public void onLoadComplete(final int type, int errorNo, final Object data) {
        if(errorNo == 0) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showProgess(false);
                    initListView(type, (List)data);
                    mListView.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        if(mlistType == LIST_TYPE_MAIN) {
            super.onBackPressed();
        } else if(mlistType == LIST_TYPE_DETAIL_DAYN){
            setTitle("运动详情");
            initListView(LIST_TYPE_DETAIL, null);
        } else if(mlistType == LIST_TYPE_FOOTPRESS) {
            //setTitle("第几天");
            initListView(LIST_TYPE_DETAIL_DAYN, null);
        } else {
            setTitle("调试固件");
            initListView(LIST_TYPE_MAIN, null);
        }
    }
}
