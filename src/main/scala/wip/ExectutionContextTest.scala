import org.joda.time.DateTime

import scala.concurrent.Future
import scala.util.Random

class ExectutionContextTest extends App {

  import scala.concurrent.ExecutionContext.Implicits.global


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



  names.map(n => Future{printAndGetTime(n)})



}
