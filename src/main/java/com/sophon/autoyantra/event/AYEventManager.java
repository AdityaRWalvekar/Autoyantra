package com.sophon.autoyantra.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AYEventManager {
    
    // The one-and-only instance (Singleton)
    private static AYEventManager instance;
    
    // The Filing Cabinet: Event Name -> List of things to do
    private Map<String, List<Runnable>> listeners = new HashMap<>();

    // Private constructor so no one can make a second manager
    private AYEventManager() {}

    // Get the one-and-only instance
    public static AYEventManager getInstance() {
        if (instance == null) {
            instance = new AYEventManager();
        }
        return instance;
    }

    // Sign up to listen to an event
    public void addListener(String eventType, Runnable listener) {
        listeners.computeIfAbsent(eventType, k -> new ArrayList<>()).add(listener);
    }

    // Shout the event to everyone listening
    public void fireEvent(String eventType) {
        List<Runnable> eventListeners = listeners.get(eventType);
        if (eventListeners != null) {
            for (Runnable listener : eventListeners) {
                listener.run();
            }
        }
    }
}