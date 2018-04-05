import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util
import scala.util.Success
import scala.concurrent.duration._


Future.successful(1).map((i: Int) => i.toString)
Future.successful(1).flatMap(s => Future.successful(s.toString))


//sequentiellement
for {
  a <- Future.successful("a")
  b <- Future.successful("b")
  c <- Future.successful("c")
} yield a + b + c


val fa = Future.successful("a")
val fb = Future.successful("b")
val fc = Future.successful("c")

Await.result(fa.andThen {
  case Success(s) =>
    println("effect b")
    "c"
}.andThen {
  case Success(s) =>
    println("effect c")
    throw new Exception("arrggh... don't care")
    "c"
}, 2 seconds)



//en parallèle
for {
  a <- fa
  b <- fb
  c <- fc
} yield a + b + c



//aussi en  parallèle
for {
  _ <- Future.unit
  fa = Future.successful("a")
  fb = Future.successful("b")
  fc = Future.successful("c")
  a <- fa
  b <- fb
  c <- fc
} yield a + b + c



