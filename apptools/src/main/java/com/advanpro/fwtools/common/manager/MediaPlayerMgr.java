package com.advanpro.fwtools.common.manager;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zengfs on 2016/3/14.
 * 播放器管理类，带电话状态监听
 */
public enum MediaPlayerMgr {
	INSTANCE;
	private MediaPlayer nextMediaPlayer; //负责一段音频播放结束后，播放下一段音频
	private MediaPlayer cachePlayer;     //负责setNextMediaPlayer的player缓存对象
	private List<MediaPlayer> playersCache = new ArrayList<>();
	private Map<MediaPlayer, Boolean> pauseState = new HashMap<>();
	private TelephonyManager manager;
	private MediaPlayer singlePlayer;

	private void initTelephonyManager(Context context) {
		if (manager == null) {
			manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
			manager.listen(new MyListener(), PhoneStateListener.LISTEN_CALL_STATE);
		}
	}

	/*
	 * 新开线程负责初始化负责播放剩余分段的player对象,避免UI线程做过多耗时操作
	 */
	private void initNextPlayer(final Context context, final List<Integer> idList) {
		ThreadMgr.INSTANCE.getLPool().execute(new Runnable() {
			@Override
			public void run() {
				for (int i = 1; i < idList.size(); i++) {
					nextMediaPlayer = MediaPlayer.create(context, idList.get(i));
					nextMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
					//设置下一个播放器
					cachePlayer.setNextMediaPlayer(nextMediaPlayer);
					cachePlayer = nextMediaPlayer;
					playersCache.add(nextMediaPlayer);
				}
			}
		});
	}
	
	public void play(Context context, int resId) {
		initTelephonyManager(context);		
        if (singlePlayer != null) {
            singlePlayer.reset();
        }
		singlePlayer = MediaPlayer.create(context, resId);	
		singlePlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		singlePlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				singlePlayer.release();
                singlePlayer = null;
			}
		});
        singlePlayer.start();
	}

	public void play(Context context, List<Integer> resIds) {		
		initTelephonyManager(context);
		for (MediaPlayer mediaPlayer : playersCache) {
			if (mediaPlayer.isPlaying()) mediaPlayer.stop();
			mediaPlayer.release();
		}
		playersCache.clear();
		MediaPlayer player = MediaPlayer.create(context, resIds.get(0));
		player.setAudioStreamType(AudioManager.STREAM_MUSIC);

		//设置cachePlayer为该player对象
		cachePlayer = player;
		initNextPlayer(context, resIds);

		//player对象初始化完成后，开启播放
		player.start();		
	}
	
	private class MyListener extends PhoneStateListener {
		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			switch (state) {
				case TelephonyManager.CALL_STATE_RINGING:
				case TelephonyManager.CALL_STATE_OFFHOOK:
					incomingPause(singlePlayer);
					for (MediaPlayer player : playersCache) {
						incomingPause(player);
					}
					break;
				case TelephonyManager.CALL_STATE_IDLE:
					incomingResume(singlePlayer);
					for (MediaPlayer player : playersCache) {
						incomingResume(player);
					}
					break;
			}
		}
	}
	
	private void incomingPause(MediaPlayer mp) {
		if (mp != null && mp.isPlaying()) {
			mp.pause();
			pauseState.put(mp, true);
		}
	}
	
	private void incomingResume(MediaPlayer mp) {
		if (mp != null && pauseState.get(mp) != null && pauseState.get(mp)) {
			mp.start();
			pauseState.remove(mp);
		}
	}
}
