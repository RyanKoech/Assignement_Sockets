class Process {
    var pid = 0
    var isCoOrdinatorFlag = false
    var isDownflag = false

    constructor() {}
    constructor(pid: Int) {
        this.pid = pid
        isDownflag = false
        isCoOrdinatorFlag = false
    }
}