/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2003-2013, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

package scala.concurrent.impl

import java.util.concurrent.{Callable, Executor, ExecutorService, ForkJoinPool, ForkJoinTask, ForkJoinWorkerThread, ThreadFactory, TimeUnit}
import java.util.concurrent.atomic.AtomicInteger
import java.util.Collection


import scala.concurrent.{BlockContext, CanAwait, ExecutionContext, ExecutionContextExecutor, ExecutionContextExecutorService}
import scala.annotation.tailrec


class SkExecutionContextImpl (val executor: Executor, val reporter: Throwable => Unit) extends ExecutionContextExecutor {
  require(executor ne null, "Executor must not be null")
  override def execute(runnable: Runnable) = executor execute runnable
  override def reportFailure(t: Throwable) = reporter(t)
}

object SkExecutionContextImpl {

  // Implement BlockContext on FJP threads
  final class DefaultThreadFactory(
                                    daemonic: Boolean,
                                    maxThreads: Int,
                                    prefix: String,
                                    uncaught: Thread.UncaughtExceptionHandler) extends ForkJoinPool.ForkJoinWorkerThreadFactory {

    println("DefaultThreadFactory")

    require(prefix ne null, "DefaultThreadFactory.prefix must be non null")
    require(maxThreads > 0, "DefaultThreadFactory.maxThreads must be greater than 0")
    val maxThreadsForCompar = maxThreads
    private final val currentNumberOfThreads = new AtomicInteger(0)

    @tailrec private final def reserveThread(): Boolean = {
      val n = currentNumberOfThreads.get()
      println(s"reserveThread with current nbThreads : $n")
      n match
      {
        case `maxThreadsForCompar` =>
          println("reserveThread max ")
          false
        case Int.`MaxValue` => false
        case other =>
          println("reserveThread other ")
          currentNumberOfThreads.compareAndSet(other, other + 1) ||  {

            println("recursive call to reserveThread")
            reserveThread() }
      }
    }

    @tailrec private final def deregisterThread(): Boolean = {
      val n = currentNumberOfThreads.get()
      println(s"deregisterThread with current nbThreads : $n")
      n match
      {
        case 0 => false
        case other => currentNumberOfThreads.compareAndSet(other, other - 1) || deregisterThread()
      }
    }

    def wire[T <: Thread](thread: T): T = {
      thread.setDaemon(daemonic)
      thread.setUncaughtExceptionHandler(uncaught)
      thread.setName(prefix + "-" + thread.getId())
      thread
    }



    def newThread(fjp: ForkJoinPool): ForkJoinWorkerThread ={
      println(s"newThread fjp. active thread : ${fjp.getActiveThreadCount} poolSize : ${fjp.getPoolSize} " +
        s"  running thread : ${fjp.getRunningThreadCount} steal task ${fjp.getStealCount}")


      if (reserveThread()) {
        println("newThread fjp reserve thread ok")
        wire(new ForkJoinWorkerThread(fjp) with BlockContext {

          // We have to decrement the current thread count when the thread exits

          final override def onStart(): Unit = {
            super.onStart()
            println(s"on start ${this.getName}")
          }
          final override def onTermination(exception: Throwable): Unit = {
            println("on termination")
            deregisterThread()}
          final override def blockOn[T](thunk: =>T)(implicit permission: CanAwait): T = {
            var result = null.asInstanceOf[T]
            ForkJoinPool.managedBlock(new ForkJoinPool.ManagedBlocker {
              @volatile var isdone = false
              override def block(): Boolean = {
                result = try {
                  println("block")
                  // When we block, switch out the BlockContext temporarily so that nested blocking does not created N new Threads
                  BlockContext.withBlockContext(BlockContext.defaultBlockContext) {
                    println("do block")
                    thunk }
                } finally {
                  isdone = true
                }

                true
              }
              override def isReleasable = isdone
            })
            result
          }
        })
      } else {
        //        println("newThread fjp reserve thread KO")
        null
      }
    }
  }

  def createDefaultExecutorService(reporter: Throwable => Unit): ExecutorService = {
    def getInt(name: String, default: String) = (try System.getProperty(name, default) catch {
      case e: SecurityException => default
    }) match {
      case s if s.charAt(0) == 'x' => (Runtime.getRuntime.availableProcessors * s.substring(1).toDouble).ceil.toInt
      case other => other.toInt
    }

    def range(floor: Int, desired: Int, ceiling: Int) = scala.math.min(scala.math.max(floor, desired), ceiling)
    val numThreads = getInt("scala.concurrent.context.numThreads", "x1")

    // The thread factory must provide additional threads to support managed blocking.
    val maxExtraThreads = getInt("scala.concurrent.context.maxExtraThreads", "256")

    // The hard limit on the number of active threads that the thread factory will produce
    // scala/bug#8955 Deadlocks can happen if maxNoOfThreads is too low, although we're currently not sure
    //         about what the exact threshold is. numThreads + 256 is conservatively high.
    //    val maxNoOfThreads = getInt("scala.concurrent.context.maxThreads", "x1")
    val maxNoOfThreads = numThreads

    val desiredParallelism = range(
      getInt("scala.concurrent.context.minThreads", "1"),
      numThreads,
      maxNoOfThreads)


    val uncaughtExceptionHandler: Thread.UncaughtExceptionHandler = new Thread.UncaughtExceptionHandler {
      override def uncaughtException(thread: Thread, cause: Throwable): Unit = reporter(cause)
    }

    val threadFactory = new SkExecutionContextImpl.DefaultThreadFactory(daemonic = true,
      maxThreads = maxNoOfThreads + ( maxExtraThreads * 2  +1 ),
      prefix = "scala-execution-context-global",
      uncaught = uncaughtExceptionHandler)

    new ForkJoinPool(desiredParallelism, threadFactory, uncaughtExceptionHandler, true)
  }

  def fromExecutor(e: Executor, reporter: Throwable => Unit = ExecutionContext.defaultReporter): SkExecutionContextImpl =
    new SkExecutionContextImpl(Option(e).getOrElse(createDefaultExecutorService(reporter)), reporter)

  def fromExecutorService(es: ExecutorService, reporter: Throwable => Unit = ExecutionContext.defaultReporter):
  SkExecutionContextImpl with ExecutionContextExecutorService = {
    new SkExecutionContextImpl(Option(es).getOrElse(createDefaultExecutorService(reporter)), reporter)
      with ExecutionContextExecutorService {
      final def asExecutorService: ExecutorService = executor.asInstanceOf[ExecutorService]
      override def execute(command: Runnable) = executor.execute(command)
      override def shutdown() { asExecutorService.shutdown() }
      override def shutdownNow() = asExecutorService.shutdownNow()
      override def isShutdown = asExecutorService.isShutdown
      override def isTerminated = asExecutorService.isTerminated
      override def awaitTermination(l: Long, timeUnit: TimeUnit) = asExecutorService.awaitTermination(l, timeUnit)
      override def submit[T](callable: Callable[T]) = asExecutorService.submit(callable)
      override def submit[T](runnable: Runnable, t: T) = asExecutorService.submit(runnable, t)
      override def submit(runnable: Runnable) = asExecutorService.submit(runnable)
      override def invokeAll[T](callables: Collection[_ <: Callable[T]]) = asExecutorService.invokeAll(callables)
      override def invokeAll[T](callables: Collection[_ <: Callable[T]], l: Long, timeUnit: TimeUnit) = asExecutorService.invokeAll(callables, l, timeUnit)
      override def invokeAny[T](callables: Collection[_ <: Callable[T]]) = asExecutorService.invokeAny(callables)
      override def invokeAny[T](callables: Collection[_ <: Callable[T]], l: Long, timeUnit: TimeUnit) = asExecutorService.invokeAny(callables, l, timeUnit)
    }
  }
}
