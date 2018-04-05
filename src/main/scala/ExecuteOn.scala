import monix.eval.Task
import org.joda.time.DateTime


class ExecuteOn extends App {


  def action(name: String) = {
    val now = DateTime.now
    Thread.sleep(1000)
    val res = s"$name run on ${Thread.currentThread().getName}"
    println(res)
    res
  }

  val task = Task {
    action("example")
  }
  val forkedTask = task.executeOn(MyScheduler.io)


  val onFinish = Task.eval(
    println(s"Ends on thread: ${Thread.currentThread.getName}"))


  val result = task // executes on global
    .flatMap(_ => forkedTask) // executes on io
    .flatMap(_ => Task.eval{action("eval")})   // stay on io
    .flatMap(_ => Task.eval{action("eval")})   // stay on io
//    .flatMap(_ => Task{action("apply (executeAsync)")})   // switch back to global
    .asyncBoundary // switch back to global
    .doOnFinish(_ => onFinish) // executes on global
    .runAsync(MyScheduler.global)





}
