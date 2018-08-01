package queue;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import jsonform.DownInfoForm;

public class QueueDown {
	private Queue<DownInfoForm> downQueue = new ArrayBlockingQueue<>(2048);
	
	public void push(DownInfoForm downInfoForm){
		downQueue.add(downInfoForm);
	}
	
	public DownInfoForm pull() {
		return downQueue.poll();
	}
}
