package com.hh.wifip2p;

public enum MessageOptions {
    MESSAGE_READ(0),
    MESSAGE_CLIP_STRING(1),

    MESSAGE_CLIP_SET_STRING(2);
    private int value;

    private MessageOptions(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
