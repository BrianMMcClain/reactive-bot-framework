package com.github.brianmmcclain.reactivebotframework.commands;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.github.brianmmcclain.reactivebotframework.TwitchMessage;

public class Gamble extends BotCommand {

    private final int STARTING_CURRENCY = 500;

    private Map<String, Integer> currency;
    private Random rand;

    public Gamble() {
        super();
        this.currency = new HashMap<String, Integer>();
        this.rand = new Random();
    }

    @Override
    public String execute(String data, TwitchMessage tMessage) {
        int c = this.getCurrency(tMessage.getSentBy());
        
        try {
            int gambleAmount = Integer.parseInt(data);

            if (gambleAmount > c) {
                return String.format("@%s Sorry! You can't gamble more than you currently have. Current Balance: %d", tMessage.getSentBy(), c);
            } else if (gambleAmount <= 0) {
                return String.format("@%s Sorry! You must wager 1 or more. Current Balance: %d", tMessage.getSentBy(), c);
            }

            if (this.rand.nextBoolean()) {
                // Win
                this.currency.put(tMessage.getSentBy(), c + gambleAmount);
                return String.format("@%s You won! Current Balance: %d", tMessage.getSentBy(), c + gambleAmount);
            } else {
                // Lose
                this.currency.put(tMessage.getSentBy(), c - gambleAmount);
                return String.format("@%s Sorry, you lost. Current Balance: %d", tMessage.getSentBy(), c - gambleAmount);
            }

        } catch (NumberFormatException e) {
            return String.format("@%s Sorry! %s isn't a valid number!", tMessage.getSentBy(), data);
        }

    }

    private int getCurrency(String name) {
        
        if (!this.currency.containsKey(name)) {
            this.currency.put(name, STARTING_CURRENCY);
        }

        return this.currency.get(name);
    }
    
}
