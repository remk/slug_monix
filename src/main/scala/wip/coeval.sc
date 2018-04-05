import monix.eval.Coeval


val coeval = Coeval {
  println("Effect!")
  "Hello!"
}

// Coeval has lazy behavior, so nothing
// happens until being evaluated:
coeval.value
//=> Effect!
// res1: String = Hello!




// And we can handle errors explicitly:
import scala.util.{Success, Failure}

coeval.runTry match {
  case Success(value) =>
    println(value)
  case Failure(ex) =>
    System.err.println(ex)
}

