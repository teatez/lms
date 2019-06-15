package com.teatez.lms.example

import org.junit.Assert.assertEquals
import org.junit.Test

class SugmaTest {
    @Test fun testSugma() {
        val b = "balls"
        val s = SugmaBallsMuthaFucker(b)
        assertEquals(MyBalls().sugma(s), b)
    }
}
