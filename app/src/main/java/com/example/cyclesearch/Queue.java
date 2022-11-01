package com.example.cyclesearch;

import java.util.LinkedList;

public class Queue {

    private final java.util.Queue<Attribute> queue;
    private final int queueLength = 11;
    private int counter;

    public Queue() {
        this.queue = new LinkedList<>();
        this.counter = 0;

        for(int i = 0; i < queueLength; i++){
            queue.add(null);
        }
    }

    public void addToQueue(Attribute activity){
        queue.remove();
        queue.add(activity);
        this.counter++;
    }

    public boolean isReady() {
        return this.counter >= this.queueLength;
    }

    public Attribute tallyQueue(){
        int[] tallyArray = {0, 0, 0, 0};
        java.util.Queue<Attribute> tallyQueue = new LinkedList<>(queue);
        this.counter = 0;

        for(int i = 0; i < queueLength; i++){
            Attribute type = tallyQueue.poll();
            if (type == null) continue;

            switch(type) {
                case WALKING:
                    tallyArray[0]++;
                    break;
                case STANDING:
                    tallyArray[1]++;
                    break;
                case SITTING:
                    tallyArray[2]++;
                    break;
                case BIKING:
                    tallyArray[3]++;
                    break;
                default:
                    break;
            }
        }

        int indexMax = 0;
        for(int i = 0; i < 4; i++){
            indexMax = tallyArray[i] > tallyArray[indexMax] ? i : indexMax;
        }

        switch(indexMax){
            case 0:
                return Attribute.WALKING;
            case 1:
                return Attribute.STANDING;
            case 2:
                return Attribute.SITTING;
            case 3:
                return Attribute.BIKING;
            default:
                return null;
        }
    }
}