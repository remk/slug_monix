import monix.eval.Task
import org.joda.time.DateTime


class BlockContextTaskTest extends App {

  import monix.execution.Scheduler.Implicits.global


  def printAndGetTime(name: String) = {
    Task {
      scala.concurrent.blocking {
        println(s"start $name")
        val now = DateTime.now

        Thread.sleep((1000))
        val res = s"$name : $now"
        println(res)
        res
      }
    }
  }

  def printAndGetTimeWithoutBlockContext(name: String) = {
    Task {
      println(s"start without block context $name")
      val now = DateTime.now

      Thread.sleep((1000))
      val res = s"without block context  result : $name : $now"
      println(res)
      res
    }
  }


  val names = (1 to 256).toList.map(_.toString)


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
    b <- withBlockContext
    c <- withoutBlockContext
  } yield {
    println("Totaly DONE")
  }).runAsync


}
