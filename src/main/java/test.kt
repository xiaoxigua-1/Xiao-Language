fun a() {
    val a = 10
    fun b() {

    }
    fun c() {
        val b = ::b
        b()
    }

}