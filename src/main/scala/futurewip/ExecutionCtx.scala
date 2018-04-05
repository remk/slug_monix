package futurewip

import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.{Executors, ThreadFactory}

//import monix.execution.atomic.AtomicLong

import scala.concurrent.ExecutionContext


object ExecutionCtx {

  lazy val global =  ExecutionContext.fromExecutor(null)


  lazy val io = {
    //        val javaService = Executors.newScheduledThreadPool(256)
    //        val javaService = Executors.newSingleThreadScheduledExecutor()
    //unbounded execution context for io
    val javaService = Executors.newCachedThreadPool(
      new ThreadFactory {
        private val counter =new AtomicLong(0L)

        def newThread(r: Runnable) = {
          val th = new Thread(r)
          val name = "io-thread-" +
            counter.getAndIncrement().toString
          //          println(s"newThread :  $name")
          th.setName(name)
          th.setDaemon(true)
          th
        }
      })
    ExecutionContext.fromExecutorService(javaService)
  }




}
