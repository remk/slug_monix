import monix.eval.Task
import org.joda.time.DateTime

import scala.concurrent.Future
import scala.util.Random

class ExectutionContextTest2 extends App {

  import monix.execution.Scheduler.Implicits.global


  def printAndGetTime(name : String) = {
    scala.concurrent.blocking {
      println(s"start $name")
      val now = DateTime.now

      Thread.sleep((5000))
      val res = s"$name : $now"
      println(res)
      res
    }
  }


  println("test")
  val names = (1 to 5000 ).toList.map(_.toString)



 val tasks =  names.map(n => Task{printAndGetTime(n)})


  Task.sequence(tasks).runAsync


}
