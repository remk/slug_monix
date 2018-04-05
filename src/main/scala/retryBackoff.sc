import monix.eval.Task

import scala.concurrent.duration.FiniteDuration


//example retry with backoff from monix documentation
def retryBackoff[A](source: Task[A],
                    maxRetries: Int, firstDelay: FiniteDuration): Task[A] = {

  source.onErrorRecoverWith {
    case ex: Exception =>
      if (maxRetries > 0)
      // Recursive call, it's OK as Monix is stack-safe
        retryBackoff(source, maxRetries-1, firstDelay*2)
          .delayExecution(firstDelay)
      else
        Task.raiseError(ex)
  }
}