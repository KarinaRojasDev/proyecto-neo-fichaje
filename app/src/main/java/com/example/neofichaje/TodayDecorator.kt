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
        view.addSpan(DotSpan(8f, Color.parseColor("#FF69B4"))) // puntito rosa
        view.addSpan(ForegroundColorSpan(Color.BLACK))         // texto negro
    }
}