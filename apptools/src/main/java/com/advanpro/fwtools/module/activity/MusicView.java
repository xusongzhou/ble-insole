package com.advanpro.fwtools.module.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.*;
import android.widget.*;

import com.advanpro.fwtools.R;
import com.advanpro.fwtools.common.base.BaseHolder;
import com.advanpro.fwtools.common.base.BaseListAdapter;
import com.advanpro.fwtools.common.util.FileUtils;
import com.advanpro.fwtools.common.util.UiUtils;
import com.advanpro.fwtools.db.Dao;
import com.advanpro.fwtools.db.Song;
import com.advanpro.fwtools.entity.SimpleObserver;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by zengfs on 2016/1/16.
 * 伴跑音乐播放控件
 */
public class MusicView implements View.OnClickListener, MediaPlayer.OnCompletionListener, AdapterView.OnItemClickListener {
    private static final int[] modeResIds = {R.drawable.all_cycle, R.drawable.random, R.drawable.single_cycle};
    private static final String DEFALUT_SONG_ASSETS_DIR = "songs";
    private static final int MODE_ALL_CYCLE = 0;
    private static final int MODE_RANDOM = 1;
    private static final int MODE_SINGLE_CYCLE = 2;
    private static final int STATE_STOP = 0;
    private static final int STATE_PAUSE = 1;
    private static final int STATE_PLAYING = 2;
    private Context context;
    public View rootView;
    private ImageButton btnPreTrack;
    private ImageButton btnNextTrack;
    private ImageView ivPlayMode;
    private FrameLayout flPlaylist;
    private FrameLayout flPlayMode;
    private ImageButton btnPlay;
    private TextView tvMusicName;
    private int currentMode;
    private int currentState;
    private MediaPlayer mediaPlayer;
    private boolean isInitialized;
    private String[] defaultSongs;
    private long songlistId = -1;//默认播放列表为-1，其他为歌单在数据库的id
    //播放列表全部音乐，如果是自定义的音乐song里不包含扩展名，默认的音乐信息包含
    private List<Song> songs = new ArrayList<>();
    private int currentSongIndex;
    private boolean isPlayingCustomSongs;//是否正在播放默认列表音乐
    private Random random;
    private TelephonyManager tm;//电话管理
    private boolean isPauseByPhoneIn;
    private SimpleObserver simpleObserver;
    //----------播放列表对话框------------
    private Dialog playlistDialog;
    private TextView tvSongsNum;
    private PlaylistAdapter playlistAdapter;
    private ListView lvSongs;

    public MusicView(ViewGroup container) {
        this.context = container.getContext();
        assignViews(container);
        initViews();
        init();
    }

    private void init() {
        try {
            //获取APP自带音乐文件
            defaultSongs = context.getAssets().list(DEFALUT_SONG_ASSETS_DIR);
        } catch (IOException e) {
            e.printStackTrace();
        }
        random = new Random();
        //监听来电状态，来电时暂停音乐，挂断继续播放
        tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        tm.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        simpleObserver = new SimpleObserver(ObservableMgr.getSonglistObservable()) {
            @Override
            public void update(Observable observable, Object data) {
                loadPlaylist();//重新加载播放列表
                updatePlaylist();//刷新播放列表
            }
        };
    }

    private void assignViews(ViewGroup container) {
        rootView = View.inflate(context, R.layout.view_music, container);
        btnPreTrack = (ImageButton) rootView.findViewById(R.id.btn_pre_track);
        btnNextTrack = (ImageButton) rootView.findViewById(R.id.btn_next_track);
        btnPlay = (ImageButton) rootView.findViewById(R.id.btn_play);
        flPlaylist = (FrameLayout) rootView.findViewById(R.id.fl_playlist);
        flPlayMode = (FrameLayout) rootView.findViewById(R.id.fl_play_mode);
        ivPlayMode = (ImageView) rootView.findViewById(R.id.iv_play_mode);
        tvMusicName = (TextView) rootView.findViewById(R.id.tv_music_name);
    }

    private void initViews() {
        initPlaylistDialog();
        flPlaylist.setOnClickListener(this);
        flPlayMode.setOnClickListener(this);
        btnPlay.setOnClickListener(this);
        btnPreTrack.setOnClickListener(this);
        btnNextTrack.setOnClickListener(this);
        tvMusicName.setText(R.string.loading_playlist);
    }

    private void initPlaylistDialog() {
        playlistDialog = new Dialog(context, R.style.DialogStyle);
        playlistDialog.setContentView(getPlaylistDialogView());
        Window window = playlistDialog.getWindow();
        window.setWindowAnimations(R.style.DialogAnimation);
        window.getDecorView().setPadding(0, 0, 0, 0);
        window.setBackgroundDrawable(new ColorDrawable(UiUtils.getColor(R.color.playlist_bg)));
        WindowManager.LayoutParams params = window.getAttributes();
        window.setGravity(Gravity.BOTTOM);
        params.width = -1;
        params.height = (int) (UiUtils.getScreenHeight() * 0.8);
        params.alpha = 0.9f;
        window.setAttributes(params);
    }

    private View getPlaylistDialogView() {
        View view = View.inflate(context, R.layout.dialog_songlist, null);
        tvSongsNum = (TextView) view.findViewById(R.id.tv_num);
        lvSongs = (ListView) view.findViewById(R.id.lv);
        TextView tvClose = (TextView) view.findViewById(R.id.tv_close);
        view.findViewById(R.id.tv_random).setOnClickListener(this);
        tvClose.setOnClickListener(this);
        playlistAdapter = new PlaylistAdapter(songs);
        lvSongs.setAdapter(playlistAdapter);
        lvSongs.setOnItemClickListener(this);
        return view;
    }

    public void initialize() {
        //初始化播放器，设置完成监听
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnCompletionListener(this);
        }
        loadPlaylist();
        updatePlaylist();
        isInitialized = true;
    }

    //加载播放列表
    private void loadPlaylist() {
        //记录当前正在播放的歌曲路径
        String currentPlayPath = songs.size() > 0 ? songs.get(currentSongIndex).path : null;
        songs.clear();
        long runSonglistId = Dao.INSTANCE.queryRunSonglistId();
        File file;
        //文件存在则添加到播放列表，否则从数据库删除
        List<Song> list = Dao.INSTANCE.querySongs(runSonglistId);
        for (int i = 0; i < list.size(); i++) {
            Song song = list.get(i);
            //路径一致说明是一首歌，更新歌单后，更新歌曲索引
            if (song.path.equals(currentPlayPath)) currentSongIndex = i;
            file = new File(song.path);
            if (file.exists()) songs.add(song);
            else Dao.INSTANCE.deleteSong(song.id);
        }
        //如果自定义播放列表为空，使用默认歌曲播放
        if (songs.size() > 0) {
            isPlayingCustomSongs = true;
            switchSonglist(runSonglistId);
            tvMusicName.setText(songs.get(currentSongIndex).title);
        } else {
            isPlayingCustomSongs = false;
            for (String fileName : defaultSongs) {
                songs.add(new Song(null, fileName, null, null, 0, 0, 0));
            }
            switchSonglist(-1);
            tvMusicName.setText(FileUtils.deleteSuffix(songs.get(currentSongIndex).title));
        }
    }

    //如果当前播放的歌单Id和数据库中的伴跑歌单不一样，从头开始播放
    private void switchSonglist(long selectedSonglistId) {
        if (songlistId != selectedSonglistId) {
            currentSongIndex = 0;
            if (currentState == STATE_PLAYING) play();
        }
        songlistId = selectedSonglistId;
    }

    private void updatePlaylist() {
        playlistAdapter.notifyDataSetChanged();
        if (currentSongIndex >= songs.size()) currentSongIndex = 0;
        if (songs.size() > 0) {
            tvMusicName.setText(isPlayingCustomSongs ? songs.get(currentSongIndex).title
                    : FileUtils.deleteSuffix(songs.get(currentSongIndex).title));
        }
        else tvMusicName.setText(R.string.playlist_is_null);
        tvSongsNum.setText(context.getString(R.string.brackets_songs_number).replace("?", songs.size() + ""));
        scrollToCurrentItem();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        currentSongIndex = position;
        play();
    }

    private class PlaylistAdapter extends BaseListAdapter<Song> {

        public PlaylistAdapter(List<Song> data) {
            super(data);
        }

        @Override
        protected BaseHolder<Song> getHolder() {
            return new BaseHolder<Song>() {

                private TextView tv;
                private ImageView ivDelete;
                private View indicator;

                @Override
                protected void setData(final Song song, final int position) {
                    tv.setText(isPlayingCustomSongs ? song.title : FileUtils.deleteSuffix(song.title));
                    if (position == currentSongIndex) {
                        tv.setTextColor(UiUtils.getColor(R.color.playing_item));
                        indicator.setVisibility(View.VISIBLE);
                    } else {
                        tv.setTextColor(UiUtils.getColor(R.color.content_text));
                        indicator.setVisibility(View.INVISIBLE);
                    }
                    ivDelete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            getData().remove(song);
                            //如果删除的曲目当前选中，选中下一首，删除的item在选中item之前，index减1
                            if (getData().size() <= 0) {
                                stop();
                            } else if (position == currentSongIndex) {
                                if (currentState == STATE_PLAYING) play();
                            } else if (position < currentSongIndex) {
                                currentSongIndex --;
                            }
                            updatePlaylist();
                        }
                    });
                }

                @Override
                protected View createConvertView() {
                    View convertView = View.inflate(context, R.layout.item_song, null);
                    tv = (TextView) convertView.findViewById(R.id.tv_title);
                    ivDelete = (ImageView) convertView.findViewById(R.id.iv_delete);
                    indicator = convertView.findViewById(R.id.indicator);
                    return convertView;
                }
            };
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.fl_play_mode:
                switchMode();
                break;
            case R.id.fl_playlist:
                playlistDialog.show();
                break;
            case R.id.btn_play:
                if (isInitialized) switchState();
                break;
            case R.id.btn_next_track:
                if (isInitialized) next();
                break;
            case R.id.btn_pre_track:
                if (isInitialized) previous();
                break;
            case R.id.tv_close:
                playlistDialog.dismiss();
                break;
            case R.id.tv_random://随机打乱当前播放列表
                Song current = songs.get(currentSongIndex);
                Collections.shuffle(songs);
                currentSongIndex = songs.indexOf(current);
                playlistAdapter.notifyDataSetChanged();//更新播放列表指示位置
                scrollToCurrentItem();
                break;
        }
    }

    //滚动到当前播放位置
    private void scrollToCurrentItem() {
        int firstVisible = lvSongs.getFirstVisiblePosition();
        int lastVisible = lvSongs.getLastVisiblePosition();
        if (currentSongIndex >= lastVisible || currentSongIndex <= firstVisible) {
            lvSongs.setSelection(currentSongIndex);
        }
    }

    /*
     * 切换播放状态
     */
    private void switchState() {
        switch(currentState) {
            case STATE_STOP:
                play();
                break;
            case STATE_PAUSE:
                resume();
                break;
            case STATE_PLAYING:
                pause();
                break;
        }
    }

    /*
     * 切换播放模式
     */
    private void switchMode() {
        if (++currentMode > 2) currentMode = 0;
        ivPlayMode.setBackgroundResource(modeResIds[currentMode]);
    }

    private PhoneStateListener phoneStateListener = new PhoneStateListener() {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            switch(state) {
                case TelephonyManager.CALL_STATE_RINGING:
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    //如果正在播放音乐，暂停
                    if (currentState == STATE_PLAYING) {
                        isPauseByPhoneIn = true;
                        pause();
                    }
                    break;
                case TelephonyManager.CALL_STATE_IDLE:
                    //如果之前因为电话进来暂停，继续播放
                    if (isPauseByPhoneIn) {
                        isPauseByPhoneIn = false;
                        resume();
                    }
                    break;
            }
        }
    };

    //上一首
    private void previous() {
        if (--currentSongIndex < 0) currentSongIndex = songs.size() - 1;
        play();
    }

    //下一首
    private void next() {
        if (++currentSongIndex >= songs.size()) currentSongIndex = 0;
        play();
    }

    //暂停播放
    private void pause() {
        mediaPlayer.pause();
        btnPlay.setBackgroundResource(R.drawable.play);
        currentState = STATE_PAUSE;
    }

    //继续播放
    private void resume() {
        mediaPlayer.start();
        btnPlay.setBackgroundResource(R.drawable.pause);
        currentState = STATE_PLAYING;
    }

    //停止播放
    private void stop() {
        mediaPlayer.stop();
        btnPlay.setBackgroundResource(R.drawable.play);
        currentState = STATE_STOP;
    }

    //开始播放音乐
    private void play() {
        if (songs.size() > 0) {
            try {
                mediaPlayer.reset();
                Song song = songs.get(currentSongIndex);
                if (isPlayingCustomSongs) {
                    mediaPlayer.setDataSource(song.path);
                    tvMusicName.setText(song.title);
                } else {
                    String file = DEFALUT_SONG_ASSETS_DIR + "/" + song.title;
                    AssetFileDescriptor fileDescriptor = context.getAssets().openFd(file);
                    mediaPlayer.setDataSource(fileDescriptor.getFileDescriptor(), fileDescriptor.getStartOffset(),
                            fileDescriptor.getLength());
                    tvMusicName.setText(FileUtils.deleteSuffix(song.title));
                }
                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        resume();
                        playlistAdapter.notifyDataSetChanged();//更新播放列表指示位置
                        scrollToCurrentItem();
                    }
                });
                mediaPlayer.prepareAsync();
            } catch (IOException e) {
                e.printStackTrace();
                next();
            }
        }
    }

    /**
     * 释放资源，停止电话状态监听
     */
    public void destroy() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        tm.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);//停止电话状态监听
        ObservableMgr.getSonglistObservable().deleteObserver(simpleObserver);//将观察者移除
        phoneStateListener = null;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        switch(currentMode) {
            case MODE_ALL_CYCLE:
                next();
                break;
            case MODE_SINGLE_CYCLE:
                play();
                break;
            case MODE_RANDOM:
                currentSongIndex = random.nextInt(songs.size());
                play();
                break;
        }
    }
}
