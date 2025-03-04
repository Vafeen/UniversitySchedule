package ru.vafeen.universityschedule.domain.utils

import androidx.compose.ui.graphics.Color
import ru.vafeen.universityschedule.domain.models.Settings

/**
 * Получает основной цвет для текущей темы приложения.
 *
 * Эта функция возвращает основной цвет в зависимости от того,
 * используется ли темная тема или светлая.
 *
 * @param isDark Указывает, активна ли темная тема.
 *               Если true, возвращается цвет для темной темы;
 *               если false, возвращается цвет для светлой темы.
 * @return Основной цвет [Color] для текущей темы или null, если цвета не заданы.
 */
fun Settings.getMainColorForThisThemeOrDefault(isDark: Boolean, defaultColor: Color): Color =
    (if (isDark) darkThemeColor else lightThemeColor) ?: defaultColor
