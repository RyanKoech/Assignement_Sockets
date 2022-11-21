import Election.electionDetector
import Election.isElectionFlag
import Election.isPingFlag
import java.io.IOException
import java.io.PrintWriter
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.*

class MyThread(var process: Process, private val total_processes: Int) : Runnable {
	var sock: Array<ServerSocket?>
	var r: Random

	init {
		r = Random()
		sock = arrayOfNulls(total_processes)
		messageFlag = BooleanArray(total_processes)
		for (i in 0 until total_processes) messageFlag[i] = false
	}

	@Synchronized
	private fun recovery() {
		while (isElectionFlag) {
			// wait;
		}
		System.out.println(("P" + process.pid) + ": I'm back!")
		try {
			Election.pingLock.lock()
			isPingFlag = false
			val outgoing = Socket(InetAddress.getLocalHost(), 12345)
			val scan = Scanner(outgoing.getInputStream())
			val out = PrintWriter(outgoing.getOutputStream(), true)
			System.out.println(("P" + process.pid) + ": Who is the coordinator?")
			out.println("Who is the coordinator?")
			out.flush()
			val pid = scan.nextLine()
			if (process.pid > pid.toInt()) {
				out.println("Resign")
				out.flush()
				System.out.println(("P" + process.pid) + ": Resign -> P" + pid)
				val resignStatus = scan.nextLine()
				if (resignStatus == "Successfully Resigned") {
					process.isCoOrdinatorFlag=(true)
					sock[process.pid - 1] = ServerSocket(10000 + process.pid)
					System.out.println(
						("P" + process.pid
								) + ": P" + pid + ", get out of here, I'm the coordinator!"
					)
				}
			} else {
				out.println("Don't Resign")
				out.flush()
			}
			Election.pingLock.unlock()
			return
		} catch (ex: IOException) {
			println(ex.message)
		}
	}

	@Synchronized
	private fun pingCoOrdinator() {
		try {
			Election.pingLock.lock()
			if (isPingFlag) {
				System.out.println(("P" + process.pid) + ": Coordinator, are you there?")
				val outgoing = Socket(InetAddress.getLocalHost(), 12345)
				outgoing.close()
			}
		} catch (ex: Exception) {
			isPingFlag = false
			isElectionFlag = true
			electionDetector = process
			System.out.println(("P" + process.pid) + ": Coordinator is down, I'm  initiating election..")
		} finally {
			Election.pingLock.unlock()
		}
	}

	private fun executeJob() {
		val temp = r.nextInt(20)
		for (i in 0..temp) {
			try {
				Thread.sleep(((temp + 1) * 100).toLong())
			} catch (e: InterruptedException) {
				System.out.println("Error Executing Thread:" + process.pid)
				println(e.message)
			}
		}
	}

	@Synchronized
	private fun sendMessage(): Boolean {
		var response = false
		try {
			Election.electionLock.lock()
			if ((isElectionFlag && !isMessageFlag(process.pid - 1)
						&& (process.pid >= electionDetector!!.pid))
			) {
				for (i in process.pid + 1..total_processes) {
					try {
						val electionMessage = Socket(InetAddress.getLocalHost(), 10000 + i)
						println("P$i: Here")
						electionMessage.close()
						response = true
					} catch (ex: IOException) {
						System.out.println(
							(("P" + process.pid) + ": P" + i
									+ " didn't respond")
						)
					} catch (ex: Exception) {
						println(ex.message)
					}
				}
				setMessageFlag(true, process.pid - 1)
				Election.electionLock.unlock()
				return response
			} else {
				throw Exception()
			}
		} catch (ex1: Exception) {
			Election.electionLock.unlock()
			return true
		}
	}

	@Synchronized
	private fun serve() {
		try {
			var done = false
			var incoming: Socket? = null
			val s = ServerSocket(12345)
			isPingFlag = true
			val temp = r.nextInt(5) + 5
			for (counter in 0 until temp) {
				incoming = s.accept()
				if (isPingFlag) System.out.println(("P" + process.pid) + ": Yes")
				val scan = Scanner(incoming.getInputStream())
				val out = PrintWriter(incoming.getOutputStream(), true)
				while (scan.hasNextLine() && !done) {
					val line = scan.nextLine()
					if ((line == "Who is the coordinator?")) {
						System.out.println(("P" + process.pid) + ": Me")
						out.println(process.pid)
						out.flush()
					} else if ((line == "Resign")) {
						process.isCoOrdinatorFlag=(false)
						out.println("Successfully Resigned")
						out.flush()
						incoming.close()
						s.close()
						System.out.println(("P" + process.pid) + ": Successfully Resigned")
						return
					} else if ((line == "Don't Resign")) {
						done = true
					}
				}
			}
			process.isCoOrdinatorFlag=(false)
			process.isDownflag=(true)
			try {
				incoming!!.close()
				s.close()
				sock[process.pid - 1]!!.close()
				Thread.sleep(15000)
				recovery()
			} catch (e: Exception) {
				println(e.message)
			}
		} catch (ex: IOException) {
			println(ex.message)
		}
	}

	override fun run() {
		try {
			sock[process.pid - 1] = ServerSocket(10000 + process.pid)
		} catch (ex: IOException) {
			println(ex.message)
		}
		while (true) {
			if (process.isCoOrdinatorFlag) {
				serve()
			} else {
				while (true) {
					executeJob()
					pingCoOrdinator()
					if (isElectionFlag) {
						if (!sendMessage()) {
							isElectionFlag = false
							System.out.println("New coordinator: P" + process.pid)
							process.isCoOrdinatorFlag=(true)
							for (i in 0 until total_processes) {
								setMessageFlag(false, i)
							}
							break
						}
					}
				}
			}
		}
	}

	companion object {
		private var messageFlag: BooleanArray = booleanArrayOf()
		fun isMessageFlag(index: Int): Boolean {
			return messageFlag[index]
		}

		fun setMessageFlag(messageFlag: Boolean, index: Int) {
			Companion.messageFlag[index] = messageFlag
		}
	}
}