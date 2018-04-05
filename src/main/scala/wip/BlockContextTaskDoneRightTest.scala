
import java.util.concurrent.ThreadFactory

import monix.eval.Task
import monix.execution.ExecutionModel.AlwaysAsyncExecution
import monix.execution.Scheduler
import monix.execution.atomic.AtomicLong
import org.joda.time.DateTime


class BlockContextTaskDoneRightTest extends App {

  val cpuBound = monix.execution.Scheduler.Implicits.global

  import java.util.concurrent.Executors

  // ...


  lazy val io = {
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


  def printAndGetTime(name: String) = {
    Task {
      //do not need blocking context since we run it in an unbounded thread pool
      //      scala.concurrent.blocking {
      println(s"start $name")
      val now = DateTime.now

      Thread.sleep((1000))
      val res = s"$name : $now"
      println(res)
      res
      //      }
    }
  }

  def printAndGetTimeWithoutBlockContext(name: String) = {
    Task {
//      println(s"start without block context $name")
      val now = DateTime.now

      Thread.sleep((1000))
      val res = s"without block context  result : $name : $now"
      println(res)
      res
    }
  }


  val names = (1 to 5000).toList.map(_.toString)


  def withBlockContext = {
    val lf = names.map(n =>
      printAndGetTime(n)
    )
    val fl = Task.gather(lf)
    fl
  }.flatMap(_ => Task.pure(println("Done")))


  def withoutBlockContext = {
    val lf = names.map(n =>
      printAndGetTimeWithoutBlockContext(n)
    )


    val fl = Task.gather(lf)
    fl
  }.flatMap(_ => Task.pure(println("Done")))


  (for {
    //    a <- withoutBlockContext
    b <- withBlockContext.executeOn(io)
//    c <- withoutBlockContext.executeOn(cpuBound)
    d <- withBlockContext.executeOn(io)
  } yield {
    println("Totaly DONE")
  }).runAsync(cpuBound)


}
