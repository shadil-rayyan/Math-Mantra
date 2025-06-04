package com.zendalona.mathsmantra.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class NumberLineViewModel : ViewModel() {
    private val _lineStart = MutableLiveData<Int?>(-5)
    private val _lineEnd = MutableLiveData<Int?>(5)
    private val _currentPosition = MutableLiveData<Int?>(0)

    var lineStart: LiveData<Int?> = _lineStart
    var lineEnd: LiveData<Int?> = _lineEnd
    var currentPosition: LiveData<Int?> = _currentPosition

    fun reset() {
        _lineStart.setValue(-5)
        _lineEnd.setValue(5)
        _currentPosition.setValue(0)
    }

    fun moveRight() {
        val currentPositionValue = _currentPosition.getValue()
        val lineEndValue = _lineEnd.getValue()
        if (currentPositionValue != null && lineEndValue != null) {
            if (currentPositionValue < lineEndValue) {
                _currentPosition.setValue(currentPositionValue + 1)
            } else {
                // Only shift line if needed, but do NOT reset position to newStart blindly.
                // Instead, move currentPosition by 1 if possible.
                shiftRight()
            }
        }
    }


    fun moveLeft() {
        val currentPositionValue = _currentPosition.getValue()
        val lineStartValue = _lineStart.getValue()
        if (currentPositionValue != null && lineStartValue != null) {
            if (currentPositionValue > lineStartValue) {
                _currentPosition.setValue(currentPositionValue - 1)
            } else {
                shiftLeft()
            }
        }
    }

    private fun shiftRight() {
        val lineEndValue = _lineEnd.getValue()
        val currentPositionValue = _currentPosition.getValue()
        if (lineEndValue != null && currentPositionValue != null) {
            val newStart = lineEndValue + 1
            val newEnd = newStart + 10
            _lineStart.setValue(newStart)
            _lineEnd.setValue(newEnd)
            _currentPosition.setValue(currentPositionValue + 1)  // keep continuity
        }
    }


    private fun shiftLeft() {
        val lineStartValue = _lineStart.getValue()
        if (lineStartValue != null) {
            val newEnd = lineStartValue - 1
            val newStart = newEnd - 10
            _lineStart.setValue(newStart)
            _lineEnd.setValue(newEnd)
            _currentPosition.setValue(newEnd)
        }
    }
}
