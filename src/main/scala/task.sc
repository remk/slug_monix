import monix.eval.Task

val t1 = Task.now("a")

val t2 = Task.eval("b")
val t3 = Task {"c"}
