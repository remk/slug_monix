import monix.eval.Task
import org.joda.time.DateTime


class BatchingTask extends App {


  def printAndGetTime(name: String) = {
    val now = DateTime.now
    Thread.sleep(2000)
    val res = s"$name : $now"
    println(res)
    res
  }

  val names = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toList.map(_.toString)

  val tasks = names.map(n => Task(printAndGetTime(n)))

  val batched = tasks.grouped(8).toVector.map(b => Task.gather(b))
  val sequenceOfBatch = Task.sequence(batched).map(_.flatten)

  import monix.execution.Scheduler.Implicits.global
  sequenceOfBatch.runAsync.foreach(r => println(r))


}
