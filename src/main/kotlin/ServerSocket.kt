import java.net.ServerSocket
import kotlin.concurrent.thread

fun main() {

    val server = ServerSocket(12345)

    val client = server.accept()

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