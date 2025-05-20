package com.example.neofichaje

import android.graphics.Color
import android.text.style.ForegroundColorSpan
import com.prolificinteractive.materialcalendarview.*
import com.prolificinteractive.materialcalendarview.spans.DotSpan

class TodayDecorator : DayViewDecorator {

    private val today = CalendarDay.today()

    override fun shouldDecorate(day: CalendarDay): Boolean {
        return day == today
    }

    override fun decorate(view: DayViewFacade) {
        view.addSpan(DotSpan(8f, Color.parseColor("#7F5AF0")))
        view.addSpan(ForegroundColorSpan(Color.BLACK))
    }
}