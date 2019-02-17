package com.youknow.everydayphoto.common

import org.junit.Test
import java.util.*

class CommonTest {
    @Test
    fun toLongTimeTest() {
        val time = "2019-02-09T17:30:04Z"
        val longTime = time.toLongTime()
        println(Date(longTime).timeStamp())
    }
}