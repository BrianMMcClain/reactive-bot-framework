reactive-bot-framework
===

This is a basic bot framework using [Project Reactor](https://projectreactor.io/), a reactive framework for Spring. This was built as a way to learn reactive programming on top of Spring, while implementing something useful.

Currently, the bot acts as a Twitch chat bot, expecting the following environment variables to be defined:

- **TWITCH_NICK**: Your Twitch username
- **TWITCH_OAUTH**: The Twitch IRC authentication requires that you use an OAuth token rather than your password. You can generate an OAuth token [here](https://twitchapps.com/tmi/)
- **TWITCH_CHANNEL**: The channel that the bot will connect to