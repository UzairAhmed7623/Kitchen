package com.inkhornsolutions.kitchen.modelclasses;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Chat {
    private List<Map<String, Object>> messages = new ArrayList<>();

    public Chat() {
    }

    public Chat(List<Map<String, Object>> messages) {
        this.messages = messages;
    }

    public List<Map<String, Object>> getMessages() {
        return messages;
    }

    public void setMessages(List<Map<String, Object>> messages) {
        this.messages = messages;
    }
}
