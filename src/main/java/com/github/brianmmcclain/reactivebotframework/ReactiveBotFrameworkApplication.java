package com.github.brianmmcclain.reactivebotframework;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ReactiveBotFrameworkApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReactiveBotFrameworkApplication.class, args);

		String oauth = System.getenv("TWITCH_OAUTH");
		String nick = System.getenv("TWITCH_NICK");
		String channel = System.getenv("TWITCH_CHANNEL");

		TwitchBot bot = new TwitchBot();
		bot.authorize(oauth, nick);
		bot.joinChannel(channel);
		bot.sendMessage("Hello!!");
	}
}
