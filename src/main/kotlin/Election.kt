
import java.util.concurrent.locks.ReentrantLock


object Election {
    var pingLock = ReentrantLock()
    var electionLock = ReentrantLock()
    var isElectionFlag = false
    var isPingFlag = true
    var electionDetector: Process? = null

    fun initialElection(t: Array<MyThread>) {
        var temp : Process = Process(-1)
        for (i in t.indices) if (temp.pid < t[i].process.pid) temp = t[i].process
        t[(temp.pid - 1).toInt()].process.isCoOrdinatorFlag = true
    }
}