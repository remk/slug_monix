import java.util.concurrent.{Executor, Executors}

import scala.concurrent.{ExecutionContext, Future}


val io = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())



abstract class ComponentWithControl {
  protected implicit val ec: ExecutionContext
  def asyncAction: Future[String]
}


abstract class ComponentControlGivenToInstantiator(protected
                                                   implicit val ec: ExecutionContext) {
  def asyncAction: Future[String]
}


abstract class ComponentControlGivenToUser() {
  def asyncAction(implicit ec: ExecutionContext): Future[String]
}