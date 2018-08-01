package queue;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import jsonform.UpInfoForm;

public class QueueUp {
	private Queue<UpInfoForm> upQueue = new ArrayBlockingQueue<>(2048);
	
	public void push(UpInfoForm upInfoForm){
		upQueue.add(upInfoForm);
	}
	
	public UpInfoForm pull() {
		return upQueue.poll();
	}
}
