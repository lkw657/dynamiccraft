package com.lkw657.dynamiccraft;
public enum GateState {
    stopped(0), opening(1), closing(2);

    int value;

    GateState(int value) {
        this.value = value;
    }
}