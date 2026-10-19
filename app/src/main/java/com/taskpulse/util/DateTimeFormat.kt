package com.taskpulse.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Formatea los instantes (epoch en milisegundos) a texto para la interfaz.
// Se centraliza aquí para que toda la app muestre fechas igual.
object DateTimeFormat {

    // "14:30" para la hora de la lista de tareas.
    fun time(epochMillis: Long): String =
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(epochMillis))

    // "12 mar" para el día, junto a la hora en el detalle.
    fun day(epochMillis: Long): String =
        SimpleDateFormat("d MMM", Locale.getDefault()).format(Date(epochMillis))

    // "lunes, 12 de marzo · 14:30" para el detalle.
    fun full(epochMillis: Long): String =
        SimpleDateFormat("EEEE d 'de' MMMM · HH:mm", Locale.getDefault())
            .format(Date(epochMillis))

    // "lun 12 mar" para la rueda del selector de fecha.
    fun weekdayDay(epochMillis: Long): String =
        SimpleDateFormat("EEE d MMM", Locale.getDefault()).format(Date(epochMillis))
}
