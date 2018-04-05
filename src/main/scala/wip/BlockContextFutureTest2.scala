import org.joda.time.DateTime

import scala.concurrent.Future

class BlockContextFutureTest2 extends App {

  import scala.concurrent.ExecutionContext.Implicits.global


  def printAndGetTime(name: String) = {
    scala.concurrent.blocking {
      println(s"start $name")
      val now = DateTime.now

      Thread.sleep((1000))
      val res = s"$name : $now"
      println(res)
      res
    }
  }

  def printAndGetTimeWithoutBlockContext(name: String) = {

    println(s"start without block context $name")
    val now = DateTime.now

    Thread.sleep((1000))
    val res = s"without block context  result : $name : $now"
    println(res)
    res

  }

  val maxAdditionThreadsForBlocking= 140
  val nbLogicalCores = 8


  val names = (1 to (maxAdditionThreadsForBlocking + nbLogicalCores)).toList.map(_.toString)

  //if the batch size equals  maxAdditionThreadsForBlocking + nbLogicalCores -1 the operation in the nonblocking context can use nbLogicalCores Threads
  //  val names = (1 to (maxAdditionThreadsForBlocking + nbLogicalCores -1)).toList.map(_.toString)

  def withBlockContext = {
    val lf = names.map(n => Future {
      printAndGetTime(n)
    })

    val fl = Future.sequence(lf)
    fl
  }.flatMap(_ => Future.successful(println("Done")))


  def withoutBlockContext = {
    val lf = names.map(n => Future {
      printAndGetTimeWithoutBlockContext(n)
    })

    val fl = Future.sequence(lf)
    fl
  }.flatMap(_ => Future.successful(println("Done")))


  for {
//    a <- withoutBlockContext
    b <- withBlockContext
    c <- withBlockContext
    d <- withBlockContext
    e <- withoutBlockContext
  } yield {
    println("Totaly DONE")
  }


}
