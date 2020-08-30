package com.example.devlife

import com.example.devlife.model.GifPost
import java.util.function.BinaryOperator

class Storage(val url: String) {
    private var history = arrayListOf<GifPost>()
    private var currentIndex: Int = 0

    fun getCategory(): String = url

    fun add(gifPost: GifPost) = history.add(gifPost)

    fun current(): GifPost = history[currentIndex]

    fun next(): GifPost {
        currentIndex = Math.min(lastIndex(), currentIndex + 1)
        return history[currentIndex]
    }

    fun prev(): GifPost {
        currentIndex = Math.max(0, currentIndex - 1)
        return history[currentIndex]
    }

    fun size(): Int = history.size

    fun empty(): Boolean {
        return history.size == 0
    }

    fun isAtStart(): Boolean {
        return currentIndex == 0
    }

    fun isAtEnd(): Boolean {
        return currentIndex == lastIndex()
    }

    private fun lastIndex(): Int = history.size - 1
}