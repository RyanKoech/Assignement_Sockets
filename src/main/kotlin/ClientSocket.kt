import java.net.Socket
import kotlin.concurrent.thread

fun main() {

    // Slower Clock
    val TIMER_RATE : Int = 1200

    var time : Int = 0
    var period : Int = TIMER_RATE

    thread {
        while (true) {
            println(time)
            Thread.sleep(period.toLong())
            time++
        }
    }

    val client = Socket("127.0.0.1", 12345)

    val clientOutputSteam = client.getOutputStream()
    val clientInputSteam = client.getInputStream()

    thread {
        while(true){
            val nextByte = clientInputSteam.read()

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
                clientOutputSteam.write(byteArrayOf(time.toByte()))
            }

            Thread.sleep(ONE_SECOND.toLong())
        }
    }

}