

import monix.eval.Task
import monix.execution.Scheduler.Implicits.global
import org.joda.time.DateTime

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.Random

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




