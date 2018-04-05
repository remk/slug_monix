
import monix.eval.Task
import monix.eval.Task.Error

import scala.concurrent.duration._
import scala.concurrent.TimeoutException
import monix.execution.Scheduler.Implicits.global

class TimeoutTest  extends App {


  val source = Task{
    scala.concurrent.blocking {
      println("foo")

      Thread.sleep(2000)

      println("bar")
      "Hello!"
    }
  }

  val timedOut = source.timeoutTo(
    1.seconds,
    Task({
      println("error")
      throw new TimeoutException
    })
  )

  timedOut.runAsync.recover{case e : Throwable => println("ARRRG")}
}
