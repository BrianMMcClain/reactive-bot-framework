package com.github.brianmmcclain.reactivebotframework;

import java.util.HashMap;
import java.util.Map;

import com.github.brianmmcclain.reactivebotframework.commands.*;

import reactor.core.scheduler.Schedulers;

public class TwitchBot {

    private IRCConnection connection;
    private boolean isAuthenticated = false;
    private String channel;

    private Map<String, BotCommand> commandRegistry;

    /**
     * Connect using the default hostname and port
     */
    public TwitchBot() {
        this.connection = new IRCConnection("irc.chat.twitch.tv", 6667);
        this.connection.connect();

        this.commandRegistry = new HashMap<String, BotCommand>();

        // Enable metrics for schedulers
        Schedulers.enableMetrics();
    }

    /**
     * Connect using a custom hostname and port
     * 
     * @param host Hostname of IRC server to connect to
     * @param port Port of IRC server to connect to
     */
    public TwitchBot(String host, int port) {
        this.connection = new IRCConnection(host, port);
        this.connection.connect();
    }

    /**
     * Registers a new command for the bot to respond to
     * 
     * @param command Message prefix to scan for (ie. !command)
     * @param botCommand Instance of a BotCommand object that handles the logic of the command
     */
    public void registerCommand(String command, BotCommand botCommand) {
        this.commandRegistry.put(command, botCommand);
    }

    /**
     * @return True of the underlying IRC connection is active. Otherwise, returns false.
     */
    public boolean isConnected() {
        return this.connection.isConnected();
    }

    /**
     * 
     * @return True if the underlying IRC connection is properly authenticated with the server. Otherwise, returns false
     */
    public boolean isAuthenticated() {
        return this.isAuthenticated;
    }

    /**
     * Authorize against the configured IRC server
     * 
     * @param oauth OAuth token for the account to authoerize with
     * @param nick Username to authenticate with
     */
    public void authorize(String oauth, String nick) {
        this.connection.send("PASS " + oauth);
        this.connection.send("NICK " + nick);

        // Subscribe to the Flux stream and process the messages as they come in
        this.connection.getInputStream().metrics().subscribeOn(Schedulers.parallel()).subscribe(message -> {
            processMessage(message);
        });
    }

    /**
     * 
     * @param channel IRC/Twitch channel to join
     */
    public void joinChannel(String channel) {
        this.connection.send("JOIN #" + channel);
        this.channel = channel;
    }

    /**
     * Send a message to the channel the bot is currently joined to
     * 
     * @param message Message to send
     */
    public void sendMessage(String message) {
        //System.out.println("SENDING: \"" + message + "\"");
        this.connection.send("PRIVMSG #" + this.channel + " :" + message);
    }

    /**
     * Waits until either the underlying IRC connection is authenticated or the timeout is reached
     * 
     * @param waitSeconds How long to wait before giving up, in seconds
     */
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

    /**
     * Basic message processing. Automatically handles Twitch API specific messages,
     * then attempts to see if any message is invoking a registered command
     * 
     * @param message The message to process
     */
    private void processMessage(String message) {
        if (message.contains("Welcome, GLHF!")) {
            this.isAuthenticated = true;

        } else if (message.startsWith("PING")) {
            System.out.print("Responding to PING: . . . ");
            this.connection.send(message.replace("PING", "PONG"));
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

    /**
     * Invoked with a command message has been detected, look it up
     * in the registered commands to ensure it's valid, and then
     * execute it
     * 
     * @param tMessage The TwitchMessage obeject containing the command
     */
    private void processCommand(TwitchMessage tMessage) {
        String command = tMessage.getMessage().split(" ")[0].replace("!", "");
        String data = tMessage.getMessage().replace("!" + command, "").trim();

        if (this.commandRegistry.keySet().contains(command)) {
            BotCommand botCommand = this.commandRegistry.get(command);
            String retMessage = botCommand.execute(data, tMessage);
            this.sendMessage(retMessage);
        }

        // TODO: Command not found
    }
}