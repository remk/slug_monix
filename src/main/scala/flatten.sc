import scala.concurrent.Future

val ff = Future.successful(Future.successful("nested future"))


// since scala 2.12, no execution context required
ff.flatten

//before, no flatten and  an execution context is required
import scala.concurrent.ExecutionContext.Implicits.global
ff.flatMap(f => f)



