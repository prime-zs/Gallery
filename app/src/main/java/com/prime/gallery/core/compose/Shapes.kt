package com.prime.gallery.core.compose

import androidx.compose.foundation.shape.GenericShape

val FolderShape =
    GenericShape { size, _ ->
        val x = size.width
        val y = size.height
        val r = 0.1f * x
        val b = 0.4f * x

        moveTo(r, 0f)
        lineTo(b, 0f)
        lineTo(b + r, r)
        lineTo(x - r, r)
        quadraticBezierTo(x, r, x, 2 * r)
        lineTo(x, y - r)
        quadraticBezierTo(x, y, x - r, y)
        lineTo(r, y)
        quadraticBezierTo(0f, y, 0f, y - r)
        lineTo(0f, r)
        quadraticBezierTo(0f, 0f, r, 0f)
        close()
    }
