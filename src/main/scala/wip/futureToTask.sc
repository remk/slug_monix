

import monix.eval.Task

import scala.concurrent.{ExecutionContext, Future}





def asyncExternalApi : Future[Int] = Future.successful(3)



//no execution context required yet !
def doThing = Task.deferFutureAction { implicit ec : ExecutionContext =>
  asyncExternalApi.map { _ + 1 }.flatMap { x =>
    Future.successful({
      println(x)
      x
    })
  }
}











