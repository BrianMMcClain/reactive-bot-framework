package com.github.brianmmcclain.reactivebotframework;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import reactor.core.scheduler.Schedulers;

public class TwitchBot {

    private IRCConnection connection;
    private boolean isAuthenticated = false;
    private String channel;

    private Map<String, Class> commandRegistry;

    public TwitchBot() {
        this.connection = new IRCConnection("irc.chat.twitch.tv", 6667);
        this.connection.connect();

        this.commandRegistry = new HashMap<String, Class>();
    }

    public TwitchBot(String host, int port) {
        this.connection = new IRCConnection(host, port);
        this.connection.connect();
    }

    public void registerCommand(String command, Class commandClass) {
        this.commandRegistry.put(command, commandClass);
    }

    public boolean isConnected() {
        return this.connection.isConnected();
    }

    public boolean isAuthenticated() {
        return this.isAuthenticated;
    }

    public void authorize(String oauth, String nick) {
        this.connection.send("PASS " + oauth);
        this.connection.send("NICK " + nick);
        this.connection.getInputStream().subscribeOn(Schedulers.parallel()).subscribe(message -> {
            processMessage(message);
        });
    }

    public void joinChannel(String channel) {
        this.connection.send("JOIN #" + channel);
        this.channel = channel;
    }

    public void sendMessage(String message) {
        System.out.println("SENDING: \"" + message + "\"");
        this.connection.send("PRIVMSG #" + this.channel + " :" + message);
    }

    // TODO: This sucks, there's probably a better way to do this
    public void waitForAuthentication(int waitSeconds) {
        int count = 0;
        while (count < waitSeconds) {
            if (this.isAuthenticated) {
                return;
            } else {
                count++;
                try {
                    Thread.sleep(1000);
                } catch (IllegalArgumentException e) {

                } catch (InterruptedException e) {

                }

            }
        }
    }

    private void processMessage(String message) {
        if (message.contains("Welcome, GLHF!")) {
            this.isAuthenticated = true;

        } else if (message.startsWith("PING")) {
            System.out.print("Responding to PING: . . . ");
            sendMessage(message.replace("PING", "PONG"));
            System.out.println("done!");
        } else {
            TwitchMessage tMessage = new TwitchMessage(message);
            if (tMessage.getMessage().startsWith("!")) {
                processCommand(tMessage);
            } else {
                // Just a normal chat message
                //System.out.println(tMessage.getSentBy() + ": " + tMessage.getMessage());
            }
        }
    }

    private void processCommand(TwitchMessage tMessage) {
        String command = tMessage.getMessage().split(" ")[0].replace("!", "");
        String data = tMessage.getMessage().replace("!" + command, "").trim();

        if (this.commandRegistry.keySet().contains(command)) {
            try {
                Constructor con = this.commandRegistry.get(command).getConstructors()[0];
                BotCommand botCommand = (BotCommand) con.newInstance(command, data, tMessage);
                String retMessage = botCommand.execute();
                this.sendMessage(retMessage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}