package com.advanpro.fwtools.common.manager;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public enum  ThreadMgr {
	INSTANCE;
	private ThreadPoolProxy largePool;
	private ThreadPoolProxy smallPool;
	
	/**
	 * 线程池最小线程数：5，最大线程数10，线程池维护线程的空闲时间为5秒，缓冲队列深度10
	 * @return 线程池对象
	 */
	public synchronized ThreadPoolProxy getLPool() {
        if(largePool == null) {
            largePool = new ThreadPoolProxy(5, 10, 5000L, 10);
        }		
		return largePool;
	}

	/**
	 * 线程池最小线程数：3，最大线程数6，线程池维护线程的空闲时间为5秒，缓冲队列深度10
	 * @return 线程池对象
	 */
	public synchronized ThreadPoolProxy getSPool() {
        if(smallPool == null) {
            smallPool = new ThreadPoolProxy(3, 6, 5000L, 10);
        }		
		return smallPool;
	}



	/**
	 * 线程池代理对象
	 */
	public class ThreadPoolProxy {
		// 线程池对象
		private ThreadPoolExecutor pool;
		//线程池维护线程的最少数量
		private int corePoolSize;
		//线程池维护线程的最大数量
		private int maximumPoolSize;
		//线程池维护线程所允许的空闲时间
		private long keepAliveTime;
		//线程池所使用的缓冲队列深度
		private int queueDeep;		

		public ThreadPoolProxy(int corePoolSize, int maximumPoolSize,
				long keepAliveTime, int queueDeep) {
			this.corePoolSize = corePoolSize;
			this.maximumPoolSize = maximumPoolSize;
			this.keepAliveTime = keepAliveTime;
			this.queueDeep = queueDeep;
		}

		/**
		 * 执行任务
		 * @param task 要执行的任务
		 */
		public void execute(Runnable task) {
			if (pool == null) {
				pool = new ThreadPoolExecutor(corePoolSize, maximumPoolSize,
						keepAliveTime, TimeUnit.MILLISECONDS,
						new LinkedBlockingDeque<Runnable>(queueDeep));
			}
			pool.execute(task);
		}
		
		/**
		 * 取消任务
		 * @param task 要取消的任务
		 */
		public void cancel(Runnable task) {
			//线程池不为空，不是停止状态，不是关闭状态
			if (pool != null && !pool.isShutdown() && !pool.isTerminated()) {
				pool.remove(task);
			}
		}
	}
}
