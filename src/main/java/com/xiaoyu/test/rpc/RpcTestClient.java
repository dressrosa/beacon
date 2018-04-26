package com.xiaoyu.test.rpc;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

import com.xiaoyu.core.rpc.context.BeaconTestContext;
import com.xiaoyu.test.api.HelloServiceImpl;
import com.xiaoyu.test.api.IHelloService;

public class RpcTestClient {

    private static final AtomicInteger in = new AtomicInteger(0);
    private static final AtomicInteger count = new AtomicInteger(0);

    public static void main(String[] args) throws Exception {
        List<Thread> list = new ArrayList<>();
        long start = 0;
        try {

            BeaconTestContext.startClient();
            start = System.currentTimeMillis();
            CyclicBarrier ba = new CyclicBarrier(300);

            for (int i = 0; i < 900; i++) {
                Thread t = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            ba.await();
                        } catch (InterruptedException | BrokenBarrierException e) {
                            e.printStackTrace();
                        }
                        IHelloService service = (IHelloService) BeaconTestContext
                                .getBean(IHelloService.class);
                        int num = in.getAndIncrement();
                        String result = service.hello("tom" + num);
                        count.getAndIncrement();
                        System.out.println("答案:" + num + "->" + result + "->" + ("hello tom" + num).equals(result));

                    }
                });
                list.add(t);
            }

            for (Thread t : list) {
                t.start();
            }
        } finally {
            System.out.println("结束2!");
            for (;;) {
                if (count.get() == list.size()) {
                    break;
                }
            }
            System.out.println("总的耗时:" + (System.currentTimeMillis() - start));
            BeaconTestContext.stop();
        }
    }
}
