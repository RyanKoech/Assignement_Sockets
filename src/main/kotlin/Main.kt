import Election.initialElection
import java.util.*

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val `in` = Scanner(System.`in`)
        println("Hey, please enter the amount of processes to start:")
        val processes = `in`.nextInt()
        val t = Array<MyThread>(processes){ i ->
            MyThread(Process(i + 1), processes)
        }
//        for (i in 0 until processes) t[i] = MyThread(Process(i + 1), processes)
        initialElection(t)
        for (i in 0 until processes) Thread(t[i]).start()
    }
}