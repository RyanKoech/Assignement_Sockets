import java.net.ServerSocket
import kotlin.concurrent.thread

const val ONE_SECOND : Int = 1000

fun main() {

    // Faster clock
    val TIMER_RATE : Int = 800

    var time : Int = 0
    var period : Int = TIMER_RATE

    thread {
        while (true) {
            println(time)
            Thread.sleep(period.toLong())
            time++
        }
    }


    val server = ServerSocket(12345)

    val client = server.accept()

    val serverOutputSteam = client.getOutputStream()
    val serverInputSteam = client.getInputStream()

    thread {
        while(true){
            val nextByte = serverInputSteam.read()

            thread {

                val theirTime : Int = nextByte
                val ourTime : Int = time

                if(ourTime < theirTime) { // Time is behind the sender

                    val correctionPeriod = ((theirTime - ourTime) * ONE_SECOND).toLong()

                    // Speed up the logical clock
                    period = 500
                    Thread.sleep(correctionPeriod)
                    // Reset the logical clock
                    period = TIMER_RATE

                } else if (ourTime > theirTime) { // Time is ahead of the sender

                    val correctionPeriod = ((ourTime - theirTime) * ONE_SECOND).toLong()

                    // Slow the logical clock
                    period = 2000
                    Thread.sleep(correctionPeriod)
                    // Reset the logical clock
                    period = TIMER_RATE
                }
            }
        }
    }

    thread {
        while (true){
            val number = Math.random()

            //Send the time with a probability of 0.1 every second
            if(number < 0.1) {
                serverOutputSteam.write(byteArrayOf(time.toByte()))
            }

            Thread.sleep(ONE_SECOND.toLong())
        }
    }

}