package com.github.brianmmcclain.reactivebotframework.commands;

import java.util.Random;

import com.github.brianmmcclain.reactivebotframework.TwitchMessage;

public class Dice extends BotCommand {
    
    private Random rand;

    public Dice() {
        super();

        this.rand = new Random(); 
    }

    @Override
    public String execute(String data, TwitchMessage tMessage) {
        return "@" + tMessage.getSentBy() + " You rolled: " + (rand.nextInt(6) + 1);
    }
}
