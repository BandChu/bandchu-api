package com.bandchu.api.global.extensions

fun org.joda.time.DateTime.toJavaLocalDateTime(): java.time.LocalDateTime =
    java.time.LocalDateTime.of(
        this.year,
        this.monthOfYear,
        this.dayOfMonth,
        this.hourOfDay,
        this.minuteOfHour,
        this.secondOfMinute,
        this.millisOfSecond * 1_000_000
    )