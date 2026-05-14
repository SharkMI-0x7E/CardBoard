package io.papermc.paper.util.concurrent;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ThreadPoolExecutor.AbortPolicy;
import java.util.concurrent.atomic.AtomicInteger;

public final class ScalingThreadPool {
   private ScalingThreadPool() {
   }

   public static RejectedExecutionHandler defaultReEnqueuePolicy() {
      return reEnqueuePolicy(new AbortPolicy());
   }

   public static RejectedExecutionHandler reEnqueuePolicy(RejectedExecutionHandler original) {
      return new ScalingThreadPool.ReEnqueuePolicy(original);
   }

   public static <E> BlockingQueue<E> createUnboundedQueue() {
      return new ScalingThreadPool.Queue<>();
   }

   public static <E> BlockingQueue<E> createQueue(int capacity) {
      return new ScalingThreadPool.Queue<>(capacity);
   }

   private static final class Queue<E> extends LinkedBlockingQueue<E> {
      private final AtomicInteger idleThreads = new AtomicInteger(0);

      private Queue() {
      }

      private Queue(int capacity) {
         super(capacity);
      }

      @Override
      public boolean offer(E e) {
         return this.idleThreads.get() > 0 && super.offer(e);
      }

      @Override
      public E take() throws InterruptedException {
         this.idleThreads.incrementAndGet();

         Object var1;
         try {
            var1 = super.take();
         } finally {
            this.idleThreads.decrementAndGet();
         }

         return (E)var1;
      }

      @Override
      public E poll(long timeout, TimeUnit unit) throws InterruptedException {
         this.idleThreads.incrementAndGet();

         Object var4;
         try {
            var4 = super.poll(timeout, unit);
         } finally {
            this.idleThreads.decrementAndGet();
         }

         return (E)var4;
      }

      @Override
      public boolean add(E e) {
         return super.offer(e);
      }
   }

   private record ReEnqueuePolicy(RejectedExecutionHandler originalHandler) implements RejectedExecutionHandler {
      @Override
      public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
         if (!executor.getQueue().add(r)) {
            this.originalHandler.rejectedExecution(r, executor);
         }
      }
   }
}
