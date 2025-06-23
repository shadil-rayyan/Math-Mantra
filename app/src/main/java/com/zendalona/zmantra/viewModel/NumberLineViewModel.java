package com.zendalona.zmantra.viewModel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class NumberLineViewModel extends ViewModel {

    private static final String TAG = "NumberLineViewModel";

    private final MutableLiveData<Integer> _lineStart = new MutableLiveData<>(-5);
    private final MutableLiveData<Integer> _lineEnd = new MutableLiveData<>(5);
    private final MutableLiveData<Integer> _currentPosition = new MutableLiveData<>(0);

    public LiveData<Integer> lineStart = _lineStart;
    public LiveData<Integer> lineEnd = _lineEnd;
    public LiveData<Integer> currentPosition = _currentPosition;

    public void reset() {
        Log.d(TAG, "reset called. Setting start=-5, end=5, position=0");
        _lineStart.setValue(-5);
        _lineEnd.setValue(5);
        _currentPosition.setValue(0);
    }

    public void moveRight() {
        Integer currentPositionValue = _currentPosition.getValue();
        Integer lineEndValue = _lineEnd.getValue();

        Log.d(TAG, "moveRight called. Current position: " + currentPositionValue + ", line end: " + lineEndValue);

        if (currentPositionValue != null && lineEndValue != null) {
            if (currentPositionValue < lineEndValue) {
                _currentPosition.setValue(currentPositionValue + 1);
                Log.d(TAG, "Moved right to " + (currentPositionValue + 1));
            } else {
                Log.d(TAG, "At line end, shifting range right");
                shiftRight();
            }
        }
    }

    public void moveLeft() {
        Integer currentPositionValue = _currentPosition.getValue();
        Integer lineStartValue = _lineStart.getValue();

        Log.d(TAG, "moveLeft called. Current position: " + currentPositionValue + ", line start: " + lineStartValue);

        if (currentPositionValue != null && lineStartValue != null) {
            if (currentPositionValue > lineStartValue) {
                _currentPosition.setValue(currentPositionValue - 1);
                Log.d(TAG, "Moved left to " + (currentPositionValue - 1));
            } else {
                Log.d(TAG, "At line start, shifting range left");
                shiftLeft();
            }
        }
    }

    private void shiftRight() {
        Integer lineEndValue = _lineEnd.getValue();
        if (lineEndValue != null) {
            int newStart = lineEndValue + 1;
            int newEnd = newStart + 10;
            Log.d(TAG, "shiftRight: newStart=" + newStart + ", newEnd=" + newEnd);
            _lineStart.setValue(newStart);
            _lineEnd.setValue(newEnd);
            _currentPosition.setValue(newStart);
        }
    }

    private void shiftLeft() {
        Integer lineStartValue = _lineStart.getValue();
        if (lineStartValue != null) {
            int newEnd = lineStartValue - 1;
            int newStart = newEnd - 10;
            Log.d(TAG, "shiftLeft: newStart=" + newStart + ", newEnd=" + newEnd);
            _lineStart.setValue(newStart);
            _lineEnd.setValue(newEnd);
            _currentPosition.setValue(newEnd);
        }
    }
}
