/**
 * 唯有读书,不慵不扰
 * 
 */
package com.xiaoyu.core.cluster.fusion;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import com.xiaoyu.core.cluster.Strategy;
import com.xiaoyu.core.common.bean.BeaconPath;
import com.xiaoyu.core.common.constant.BeaconConstants;
import com.xiaoyu.core.common.exception.BizException;
import com.xiaoyu.core.common.exception.FusedException;
import com.xiaoyu.core.rpc.config.bean.Invocation;

/**
 * 熔断降级
 * 超时/故障/限流
 * 
 * @author hongyu
 * @date 2018-11
 * @description
 */
public class FuseStrategy implements Strategy {

    /**
     * service->策略发生累积次数
     */
    private static final Map<String, InvokerCounter> Counter_Map = new HashMap<>(32);

    /**
     * 级别 0-10
     */
    private static final int Grade_Threshold = 10;

    /**
     * service->降级计数 起始5,升级为0则正常调用,降级为10则再次尝试调用一次
     */
    private static final Map<String, InvokerCounter> Grade_Map = new HashMap<>(32);

    @Override
    public Object fuse(Invocation invocation, BeaconPath provider) throws Throwable {
        final BeaconPath consumer = invocation.getConsumer();
        // 已经熔断
        if (this.isFused(consumer)) {
            this.tryFuse(consumer, false);
            // 熔断后处理,如果使用者配置了熔断策略,则需要捕获此异常,并进行相应的后续处理
            throw new FusedException(
                    "Strongly advise to catch this exception and handle it for compensating business.");
        }
        Object response = null;
        try {
            response = invocation.invoke(provider);
        } catch (Throwable e) {
            // 业务类异常
            if (e instanceof BizException) {
                this.tryFuse(consumer, false);
                throw e;
            } else {
                throw e;
            }
        }
        this.tryFuse(consumer, true);
        return response;
    }

    /**
     * 是否已经发生了策略机制
     * 
     * @param consumer
     * @return
     */
    private boolean isFused(BeaconPath consumer) {
        InvokerCounter counter = null;
        long grade = 0;
        // 已经触发策略了 <=0代表恢复正常调用 >=Grade_Threshold代表下次重试
        if ((counter = Grade_Map.get(consumer.getService())) != null &&
                (grade = counter.getCount()) > 0 && grade < Grade_Threshold) {
            return true;
        }
        return false;
    }

    /**
     * 调用成功则升级,失败则降级
     * 
     * @param consumer
     * @param invokeSuccess
     */
    public void tryFuse(BeaconPath consumer, boolean invokeSuccess) {
        String service = consumer.getService();
        InvokerCounter gradeCounter = Grade_Map.get(service);
        String[] arr = consumer.getDowngrade().split(":");
        if (BeaconConstants.FUSE_QUERY.equals(arr[0])) {
            doQpsFuse(service, Long.valueOf(arr[1]));
            return;
        }
        // 正常成功
        if (invokeSuccess) {
            if (gradeCounter != null) {
                // 自动升级.发生策略后,降级到10级时,可能一段时间(30s)未发生调用,下次发生调用成功,
                // 我们认为她恢复的可能性增大,就不需要缓慢升级了
                if ((System.currentTimeMillis()) - gradeCounter.getStartTime() > 30_000) {
                    gradeCounter.refresh(0);
                }
                // 调用升级
                if (gradeCounter.decrement() <= 0) {
                    // 大部分情况下不会出现熔断情况,所以我们不需要保留,而直接删除
                    Grade_Map.remove(service);
                    Counter_Map.remove(service);
                }
            }
        } // 1.熔断(未失败) 2.调用失败
        else {
            // 自动降级.发生策略期间,可能一段时间(30s)未发生调用,这时候如果再次发生调用,但是策略还没降到10级
            // 我们认为下次可能就会成功,则自动降级到接近10级
            if (gradeCounter != null && (System.currentTimeMillis()) - gradeCounter.getStartTime() > 30_000) {
                gradeCounter.refresh(9);
            }
            // 调用降级
            doFuse(service, Long.valueOf(arr[1]));
        }
    }

    /**
     * 以阈值为分隔,高于则熔断
     * 
     * @param service
     * @param threshold
     */
    private void doFuse(String service, long threshold) {
        long count = 0;
        InvokerCounter counter = Counter_Map.get(service);
        if (counter != null) {
            count = counter.increment();
        } else {
            Counter_Map.putIfAbsent(service, (counter = new InvokerCounter()));
        }
        InvokerCounter gradeCounter = Grade_Map.get(service);
        // 超过次数,进行熔断
        if (count >= threshold) {
            counter.refresh(0);
            if (gradeCounter == null) {
                Grade_Map.put(service, new InvokerCounter(Grade_Threshold / 2));
            } else {
                // 降到最低级就会再重试一次
                gradeCounter.increment();
            }
        } else {
            if (gradeCounter != null) {
                gradeCounter.increment();
            }
        }
    }

    /**
     * 限流策略.以流量为分隔,高于流量则熔断,低于流量则恢复正常
     * 
     * @param service
     * @param threshold
     */
    private void doQpsFuse(String service, long threshold) {
        long qpms = 0;
        InvokerCounter qpsCounter = Counter_Map.get(service);
        if (qpsCounter != null) {
            qpms = qpsCounter.qpms();
        } else {
            qpsCounter = new InvokerCounter();
            Counter_Map.putIfAbsent(service, qpsCounter);
        }
        InvokerCounter gradeCounter = Grade_Map.get(service);
        // 超过限流
        long qps = qpms * 1000;
        if (qps >= threshold) {
            if (gradeCounter == null) {
                Grade_Map.put(service, new InvokerCounter(Grade_Threshold / 2));
            }
            // 重新计数
            qpsCounter.refresh(0);
        } else if (qps != -1 && qps < threshold) {
            if (gradeCounter != null) {
                // 流量平稳,恢复调用
                Grade_Map.remove(service);
            }
        }
    }

    /**
     * 1.累积计数,策略执行的次数,达到一定次数后触发熔断降级
     * 2.级数计数,0-10级.0代表正常调用 10代表下次重试
     */
    private class InvokerCounter {

        /**
         * 1.累积计数 2.级数计数
         */
        private final AtomicLong counter = new AtomicLong(0);
        /**
         * 1.记录QPS统计的开始时间 2.记录策略发生的时间
         */
        private long startTime;

        public long getStartTime() {
            return startTime;
        }

        public InvokerCounter() {
            startTime = System.currentTimeMillis();
        }

        public InvokerCounter(int count) {
            counter.set(count);
            startTime = System.currentTimeMillis();
        }

        public long increment() {
            return counter.incrementAndGet();
        }

        public void refresh(int count) {
            counter.set(count);
            startTime = System.currentTimeMillis();
        }

        public long getCount() {
            return counter.get();
        }

        public long decrement() {
            return counter.decrementAndGet();
        }

        public long qpms() {
            long count = counter.incrementAndGet();
            // 1seconds,间隔1s后统计下qps
            long subtract = 0;
            if ((subtract = System.currentTimeMillis() - startTime) > 1_000) {
                startTime = System.currentTimeMillis();
                return count / subtract;
            }
            return -1;
        }
    }

}
