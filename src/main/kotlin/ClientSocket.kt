import java.net.Socket
import kotlin.concurrent.thread

fun main() {

    val client = Socket("127.0.0.1", 12345)

    val clientOutputSteam = client.getOutputStream()
    val clientInputSteam = client.getInputStream()

    thread {
        while(true){
            val nextByte = clientInputSteam.read()
            print(nextByte.toChar())
        }
    }

    thread {
        while (true){
            val input = readln()
            clientOutputSteam.write(input.toByteArray())
        }
    }

}