import java.util.concurrent.ThreadFactory

import monix.execution.ExecutionModel.AlwaysAsyncExecution
import monix.execution.Scheduler
import monix.execution.atomic.AtomicLong

object MyScheduler {
 lazy val global : Scheduler= monix.execution.Scheduler.Implicits.global

  import java.util.concurrent.Executors

  // ...


  lazy val io : Scheduler = {
    //        val javaService = Executors.newScheduledThreadPool(256)
    //        val javaService = Executors.newSingleThreadScheduledExecutor()
    //unbounded execution context for io
    val javaService = Executors.newCachedThreadPool(
      new ThreadFactory {
        private val counter = AtomicLong(0L)

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
    Scheduler(javaService, AlwaysAsyncExecution)
  }
}
