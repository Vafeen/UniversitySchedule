package ru.vafeen.universityschedule.presentation.utils

import org.junit.Assert
import org.junit.Test
import java.time.LocalDate


class GetNumberOrWeek {
    private fun createDate(day: Int, month: Int) = LocalDate.of(2025, month, day)

    @Test
    fun test() {
        Assert.assertEquals(
            createDate(3, 2).getNumberOrWeek(), 6
        )
        Assert.assertEquals(
            createDate(9, 2).getNumberOrWeek(), 6
        )
        Assert.assertEquals(
            createDate(10, 2).getNumberOrWeek(), 7
        )
        Assert.assertEquals(
            createDate(16, 2).getNumberOrWeek(), 7
        )
    }

}
