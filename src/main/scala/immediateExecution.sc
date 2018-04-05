import monix.eval.Task
import monix.execution.CancelableFuture

val task = Task.eval("Hello world!")

import monix.execution.Scheduler.Implicits.global

val tryingNow = task.coeval

tryingNow.value match {
  case Left(future) =>

    // No luck, this Task really wants async execution
    future.foreach(r => println(s"Is async: $r"))
  case Right(result) =>
    println(s"Immediate execution done: $result")
}

CancelableFuture