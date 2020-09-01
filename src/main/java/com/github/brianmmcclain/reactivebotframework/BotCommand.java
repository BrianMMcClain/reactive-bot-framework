package com.github.brianmmcclain.reactivebotframework;

public  abstract class BotCommand{

    protected String command;
    protected String data;
    protected TwitchMessage tMessage;

    public BotCommand(String command, String data, TwitchMessage tMessage) {
        this.command = command;
        this.data = data;
        this.tMessage = tMessage;
    }

    public String execute() {
        return "Method Not Implimented";
    }
}
