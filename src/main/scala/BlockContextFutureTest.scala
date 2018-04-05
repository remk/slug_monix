import scala.concurrent.impl.SkExecutionContextImpl


class BlockContextFutureTest extends App {
  import java.time.Instant
  import scala.concurrent.{ Future, blocking }
  import scala.concurrent.ExecutionContext.Implicits.global
// implicit val global =  ProposedExecutionContextImpl.fromExecutor(null)
//implicit val global =  SkExecutionContextImpl.fromExecutor(null)
  def printAndGetTime(prefix: String, name: String): String = {
    println(s"start $prefix $name")
    val now = Instant.now
    Thread.sleep(1000)
    val res = s"done $prefix $name : $now"
    println(res)
    res
  }

  val maxAdditionThreadsForBlocking = 256
  val nbLogicalCores = Runtime.getRuntime.availableProcessors
  val names = (1 to (nbLogicalCores + maxAdditionThreadsForBlocking)).map(_.toString).toVector

  for {
    b <- Future.sequence(names.map(n => Future { blocking(printAndGetTime("with BlockContext", n)) })).map(_ => println("Done"))
    c <- Future.sequence(names.map(n => Future { printAndGetTime("without BlockContext", n) })).map(_ => println("Done"))
  } yield println("Totally DONE")
}