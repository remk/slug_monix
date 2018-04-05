import monix.eval.Task

val t1 = Task.eval(1)
val t2 = Task.eval(2)
val t3 = Task.eval(3)
val t4 = Task.eval(4)
val t5 = Task.eval(5)
val t6 = Task.eval(6)
val t7 = Task.eval(7)

Task.zip3(t1, t2, t3).map {
  case (one, two, three) => one + two + three
}

Task.parMap3(t1, t2, t3) {
  (one, two, three) => one + two + three
}


Task.gather(List(t1, t2, t3)).map {
  _.sum
}

import cats.syntax.all._

(t1,t2,t3,t4,t5,t6,t7).parMapN{
  (r1, r2, r3, r4, r5, r6, r7) =>
    r1 + r2 + r3 + r4 + r5 + r6 + r7
}

