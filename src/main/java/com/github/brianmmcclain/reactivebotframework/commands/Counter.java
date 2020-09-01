package com.github.brianmmcclain.reactivebotframework.commands;

import com.github.brianmmcclain.reactivebotframework.TwitchMessage;

public class Counter extends BotCommand {
    
    private int counter;

    public Counter() {
        super();
        this.counter = 0;
    }

    @Override
    public String execute(String data, TwitchMessage tMessage) {
        this.counter++;
        return "The counter is currently at " + this.counter;
    }
}
