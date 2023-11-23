## Subscriptions with graphql-kotlin-ktor-server

A deadlock occurs when calling subscriptions in `graphql-kotlin-ktor-server` if a suspendable function is called (that actually suspends execution) 
if there aren't enough workers (`workerGroupSize` is set to `2` in `application.conf`).

Setting `workerGroupSize` to `3` fixes the problem on a local machine (since it has more than 2 CPUs), but we're running on AWS on instances with only 2 CPUs.

To reproduce:
1. Start the application with `./gradlew run`
2. Connect to the websocket at `http://localhost:8080/subscriptions`
3. Send `init` message: `{"type":"connection_init"}`
4. Send `subscribe` message: `{"type":"subscribe", "id":"12345", "payload": {"query": "subscription { random }" } }`
5. No messages are returned and subscription "hangs" and the worker thread (`eventLoopGroupProxy`) has state `WAITING`

### Full thread dump
```
"eventLoopGroupProxy-3-2 @raw-ws-handler#20@5243" daemon prio=5 tid=0x2a nid=NA waiting
  java.lang.Thread.State: WAITING
	  at jdk.internal.misc.Unsafe.park(Unsafe.java:-1)
	  at java.util.concurrent.locks.LockSupport.park(LockSupport.java:211)
	  at java.util.concurrent.CompletableFuture$Signaller.block(CompletableFuture.java:1864)
	  at java.util.concurrent.ForkJoinPool.unmanagedBlock(ForkJoinPool.java:3465)
	  at java.util.concurrent.ForkJoinPool.managedBlock(ForkJoinPool.java:3436)
	  at java.util.concurrent.CompletableFuture.waitingGet(CompletableFuture.java:1898)
	  at java.util.concurrent.CompletableFuture.join(CompletableFuture.java:2117)
	  at graphql.GraphQL.execute(GraphQL.java:365)
	  at com.expediagroup.graphql.server.execution.GraphQLRequestHandler.executeSubscription(GraphQLRequestHandler.kt:166)
	  at com.expediagroup.graphql.server.execution.subscription.GraphQLWebSocketServer$handleSubscription$2$3$1$subscriptionJob$1.invokeSuspend(GraphQLWebSocketServer.kt:138)
	  at kotlin.coroutines.jvm.internal.BaseContinuationImpl.resumeWith(ContinuationImpl.kt:33)
	  at kotlinx.coroutines.DispatchedTask.run(DispatchedTask.kt:108)
	  at io.netty.util.concurrent.AbstractEventExecutor.runTask(AbstractEventExecutor.java:174)
	  at io.netty.util.concurrent.AbstractEventExecutor.safeExecute(AbstractEventExecutor.java:167)
	  at io.netty.util.concurrent.SingleThreadEventExecutor.runAllTasks(SingleThreadEventExecutor.java:470)
	  at io.netty.channel.nio.NioEventLoop.run(NioEventLoop.java:569)
	  at io.netty.util.concurrent.SingleThreadEventExecutor$4.run(SingleThreadEventExecutor.java:997)
	  at io.netty.util.internal.ThreadExecutorMap$2.run(ThreadExecutorMap.java:74)
	  at io.ktor.server.netty.EventLoopGroupProxy$Companion.create$lambda$1$lambda$0(NettyApplicationEngine.kt:296)
	  at io.ktor.server.netty.EventLoopGroupProxy$Companion$$Lambda$233/0x00000070012e73a8.run(Unknown Source:-1)
	  at io.netty.util.concurrent.FastThreadLocalRunnable.run(FastThreadLocalRunnable.java:30)
	  at java.lang.Thread.run(Thread.java:840)

"eventLoopGroupProxy-1-1@4709" daemon prio=5 tid=0x27 nid=NA runnable
  java.lang.Thread.State: RUNNABLE
	  at sun.nio.ch.KQueue.poll(KQueue.java:-1)
	  at sun.nio.ch.KQueueSelectorImpl.doSelect(KQueueSelectorImpl.java:122)
	  at sun.nio.ch.SelectorImpl.lockAndDoSelect(SelectorImpl.java:129)
	  - locked <0x1829> (a sun.nio.ch.KQueueSelectorImpl)
	  - locked <0x182a> (a io.netty.channel.nio.SelectedSelectionKeySet)
	  at sun.nio.ch.SelectorImpl.select(SelectorImpl.java:146)
	  at io.netty.channel.nio.SelectedSelectionKeySetSelector.select(SelectedSelectionKeySetSelector.java:68)
	  at io.netty.channel.nio.NioEventLoop.select(NioEventLoop.java:879)
	  at io.netty.channel.nio.NioEventLoop.run(NioEventLoop.java:526)
	  at io.netty.util.concurrent.SingleThreadEventExecutor$4.run(SingleThreadEventExecutor.java:997)
	  at io.netty.util.internal.ThreadExecutorMap$2.run(ThreadExecutorMap.java:74)
	  at io.ktor.server.netty.EventLoopGroupProxy$Companion.create$lambda$1$lambda$0(NettyApplicationEngine.kt:296)
	  at io.ktor.server.netty.EventLoopGroupProxy$Companion$$Lambda$233/0x00000070012e73a8.run(Unknown Source:-1)
	  at io.netty.util.concurrent.FastThreadLocalRunnable.run(FastThreadLocalRunnable.java:30)
	  at java.lang.Thread.run(Thread.java:840)

"eventLoopGroupProxy-3-1@4779" daemon prio=5 tid=0x28 nid=NA runnable
  java.lang.Thread.State: RUNNABLE
	  at sun.nio.ch.KQueue.poll(KQueue.java:-1)
	  at sun.nio.ch.KQueueSelectorImpl.doSelect(KQueueSelectorImpl.java:122)
	  at sun.nio.ch.SelectorImpl.lockAndDoSelect(SelectorImpl.java:129)
	  - locked <0x182b> (a sun.nio.ch.KQueueSelectorImpl)
	  - locked <0x182c> (a io.netty.channel.nio.SelectedSelectionKeySet)
	  at sun.nio.ch.SelectorImpl.select(SelectorImpl.java:146)
	  at io.netty.channel.nio.SelectedSelectionKeySetSelector.select(SelectedSelectionKeySetSelector.java:68)
	  at io.netty.channel.nio.NioEventLoop.select(NioEventLoop.java:879)
	  at io.netty.channel.nio.NioEventLoop.run(NioEventLoop.java:526)
	  at io.netty.util.concurrent.SingleThreadEventExecutor$4.run(SingleThreadEventExecutor.java:997)
	  at io.netty.util.internal.ThreadExecutorMap$2.run(ThreadExecutorMap.java:74)
	  at io.ktor.server.netty.EventLoopGroupProxy$Companion.create$lambda$1$lambda$0(NettyApplicationEngine.kt:296)
	  at io.ktor.server.netty.EventLoopGroupProxy$Companion$$Lambda$233/0x00000070012e73a8.run(Unknown Source:-1)
	  at io.netty.util.concurrent.FastThreadLocalRunnable.run(FastThreadLocalRunnable.java:30)
	  at java.lang.Thread.run(Thread.java:840)

"eventLoopGroupProxy-4-1@4891" daemon prio=5 tid=0x29 nid=NA runnable
  java.lang.Thread.State: RUNNABLE
	  at sun.nio.ch.KQueue.poll(KQueue.java:-1)
	  at sun.nio.ch.KQueueSelectorImpl.doSelect(KQueueSelectorImpl.java:122)
	  at sun.nio.ch.SelectorImpl.lockAndDoSelect(SelectorImpl.java:129)
	  - locked <0x182d> (a sun.nio.ch.KQueueSelectorImpl)
	  - locked <0x182e> (a io.netty.channel.nio.SelectedSelectionKeySet)
	  at sun.nio.ch.SelectorImpl.select(SelectorImpl.java:146)
	  at io.netty.channel.nio.SelectedSelectionKeySetSelector.select(SelectedSelectionKeySetSelector.java:68)
	  at io.netty.channel.nio.NioEventLoop.select(NioEventLoop.java:879)
	  at io.netty.channel.nio.NioEventLoop.run(NioEventLoop.java:526)
	  at io.netty.util.concurrent.SingleThreadEventExecutor$4.run(SingleThreadEventExecutor.java:997)
	  at io.netty.util.internal.ThreadExecutorMap$2.run(ThreadExecutorMap.java:74)
	  at io.ktor.server.netty.EventLoopGroupProxy$Companion.create$lambda$1$lambda$0(NettyApplicationEngine.kt:296)
	  at io.ktor.server.netty.EventLoopGroupProxy$Companion$$Lambda$233/0x00000070012e73a8.run(Unknown Source:-1)
	  at io.netty.util.concurrent.FastThreadLocalRunnable.run(FastThreadLocalRunnable.java:30)
	  at java.lang.Thread.run(Thread.java:840)

"main@1" prio=5 tid=0x1 nid=NA waiting
  java.lang.Thread.State: WAITING
	  at java.lang.Object.wait(Object.java:-1)
	  at java.lang.Object.wait(Object.java:338)
	  at io.netty.util.concurrent.DefaultPromise.await(DefaultPromise.java:254)
	  at io.netty.channel.DefaultChannelPromise.await(DefaultChannelPromise.java:131)
	  at io.netty.channel.DefaultChannelPromise.await(DefaultChannelPromise.java:30)
	  at io.netty.util.concurrent.DefaultPromise.sync(DefaultPromise.java:405)
	  at io.netty.channel.DefaultChannelPromise.sync(DefaultChannelPromise.java:119)
	  at io.netty.channel.DefaultChannelPromise.sync(DefaultChannelPromise.java:30)
	  at io.ktor.server.netty.NettyApplicationEngine.start(NettyApplicationEngine.kt:236)
	  at io.ktor.server.netty.EngineMain.main(EngineMain.kt:23)

"Coroutines Debugger Cleaner@1016" daemon prio=5 tid=0x10 nid=NA waiting
  java.lang.Thread.State: WAITING
	  at java.lang.Object.wait(Object.java:-1)
	  at java.lang.ref.ReferenceQueue.remove(ReferenceQueue.java:155)
	  at java.lang.ref.ReferenceQueue.remove(ReferenceQueue.java:176)
	  at kotlinx.coroutines.debug.internal.ConcurrentWeakMap.runWeakRefQueueCleaningLoopUntilInterrupted(ConcurrentWeakMap.kt:73)
	  at kotlinx.coroutines.debug.internal.DebugProbesImpl$startWeakRefCleanerThread$1.invoke(DebugProbesImpl.kt:96)
	  at kotlinx.coroutines.debug.internal.DebugProbesImpl$startWeakRefCleanerThread$1.invoke(DebugProbesImpl.kt:95)
	  at kotlin.concurrent.ThreadsKt$thread$thread$1.run(Thread.kt:30)

"DefaultDispatcher-worker-1@1757" daemon prio=5 tid=0x12 nid=NA waiting
  java.lang.Thread.State: WAITING
	  at jdk.internal.misc.Unsafe.park(Unsafe.java:-1)
	  at java.util.concurrent.locks.LockSupport.parkNanos(LockSupport.java:376)
	  at kotlinx.coroutines.scheduling.CoroutineScheduler$Worker.park(CoroutineScheduler.kt:838)
	  at kotlinx.coroutines.scheduling.CoroutineScheduler$Worker.tryPark(CoroutineScheduler.kt:783)
	  at kotlinx.coroutines.scheduling.CoroutineScheduler$Worker.runWorker(CoroutineScheduler.kt:731)
	  at kotlinx.coroutines.scheduling.CoroutineScheduler$Worker.run(CoroutineScheduler.kt:684)

"DefaultDispatcher-worker-2@1758" daemon prio=5 tid=0x13 nid=NA waiting
  java.lang.Thread.State: WAITING
	  at jdk.internal.misc.Unsafe.park(Unsafe.java:-1)
	  at java.util.concurrent.locks.LockSupport.parkNanos(LockSupport.java:376)
	  at kotlinx.coroutines.scheduling.CoroutineScheduler$Worker.park(CoroutineScheduler.kt:838)
	  at kotlinx.coroutines.scheduling.CoroutineScheduler$Worker.tryPark(CoroutineScheduler.kt:783)
	  at kotlinx.coroutines.scheduling.CoroutineScheduler$Worker.runWorker(CoroutineScheduler.kt:731)
	  at kotlinx.coroutines.scheduling.CoroutineScheduler$Worker.run(CoroutineScheduler.kt:684)

"Common-Cleaner@6180" daemon prio=8 tid=0xc nid=NA waiting
  java.lang.Thread.State: WAITING
	  at java.lang.Object.wait(Object.java:-1)
	  at java.lang.ref.ReferenceQueue.remove(ReferenceQueue.java:155)
	  at jdk.internal.ref.CleanerImpl.run(CleanerImpl.java:140)
	  at java.lang.Thread.run(Thread.java:840)
	  at jdk.internal.misc.InnocuousThread.run(InnocuousThread.java:162)

"kotlinx.coroutines.DefaultExecutor@5347" daemon prio=5 tid=0x2b nid=NA waiting
  java.lang.Thread.State: WAITING
	  at jdk.internal.misc.Unsafe.park(Unsafe.java:-1)
	  at java.util.concurrent.locks.LockSupport.parkNanos(LockSupport.java:252)
	  at kotlinx.coroutines.DefaultExecutor.run(DefaultExecutor.kt:122)
	  at java.lang.Thread.run(Thread.java:840)

"Reference Handler@6177" daemon prio=10 tid=0x2 nid=NA runnable
  java.lang.Thread.State: RUNNABLE
	  at java.lang.ref.Reference.waitForReferencePendingList(Reference.java:-1)
	  at java.lang.ref.Reference.processPendingReferences(Reference.java:253)
	  at java.lang.ref.Reference$ReferenceHandler.run(Reference.java:215)

"Finalizer@6178" daemon prio=8 tid=0x3 nid=NA waiting
  java.lang.Thread.State: WAITING
	  at java.lang.Object.wait(Object.java:-1)
	  at java.lang.ref.ReferenceQueue.remove(ReferenceQueue.java:155)
	  at java.lang.ref.ReferenceQueue.remove(ReferenceQueue.java:176)
	  at java.lang.ref.Finalizer$FinalizerThread.run(Finalizer.java:172)

"Signal Dispatcher@6179" daemon prio=9 tid=0x4 nid=NA runnable
  java.lang.Thread.State: RUNNABLE

"Notification Thread@1030" daemon prio=9 tid=0x11 nid=NA runnable
  java.lang.Thread.State: RUNNABLE

```