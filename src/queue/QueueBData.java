package queue;

import jsonform.DownInfoForm;

import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by lynch on 2018/7/4. <br>
 **/
public class QueueBData {
    private Queue<DownInfoForm> queueBData = new ArrayBlockingQueue<>(2048);

    private ArrayList<DownInfoForm> list = new ArrayList<DownInfoForm>();

    public void push(DownInfoForm downInfoForm){
        queueBData.add(downInfoForm);
        list.add(downInfoForm);
    }
    public DownInfoForm poll()
    {

        DownInfoForm down = list.get(0);
        list.remove(0);
        return down;
    }

    public DownInfoForm pull() {
        return queueBData.poll();
    }

    public boolean isEmpty()
    {
        return queueBData.isEmpty();
    }

    public ArrayList<DownInfoForm> getList() {
        return list;
    }
    public void setList(ArrayList<DownInfoForm> list) {
        this.list = list;
    }

}
