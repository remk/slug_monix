

import monix.eval.Task
import org.joda.time.DateTime

import scala.concurrent.{Await, Future}
import scala.util.Random
import scala.concurrent.duration._
import monix.execution.Scheduler.Implicits.global

def printAndGetTime(name : String) = {
  val now = DateTime.now
  Thread.sleep(Random.nextInt(200))
  val res = s"$name : $now"
  println(res)
  res
}

val names = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toList.map(_.toString)


//names.map(printAndGetTime(_))


val tasks = names.map(n => Task(printAndGetTime(n)))


//val gathered = Task.gather(tasks)
//val sequenced = Task.sequence(tasks)

val batched = tasks.grouped(4).toList.map(b => Task.gather(b))
val sequenceOfBatch = Task.sequence(batched).map(_.flatten)


//Await.result(sequenced.runAsync, 10 seconds)
//Await.result(gathered.runAsync, 10 seconds)
val resByBatch  = Await.result(sequenceOfBatch.runAsync, 10 seconds)


val f1 = Future { "1"}
val f2 = Future { "2"}
val f3 = Future { "3"}

f1.zip(f2).zip(f3)


Await.result(f1.filter(_ == "1"), 1 second)
Await.result(f1.filter(_ == "2").recoverWith{case e : Throwable => f3}, 1 second)