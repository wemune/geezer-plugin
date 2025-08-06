# Geezer Plugin

A private Minecraft server plugin with a variety of features for players and administrators.

## Features

### Commands
- **/playtime [player]**: Check your total playtime on the server.
- **/stats [player]**: Check your gameplay statistics.
- **/uptime**: Shows the server's uptime.
- **/ping [player]**: Check your or another player's ping.
- **/seen <player>**: Check when a player was last online.
- **/coords [player]**: Broadcast your coordinates or send them to a specific player.
- **/msg <player> <message>**: Send a private message to a player.
- **/reply <message>**: Reply to the last person who messaged you.
- **/time**: Shows the current in-game time and time until the next sunrise/sunset.
- **/help**: Displays a list of all Geezer plugin commands.
- **/backup**: Manually starts a server backup.

### Management Systems
- **AFK Manager**: Automatically marks players as "away from keyboard" after a period of inactivity.
- **Backup Manager**: Performs automatic, scheduled backups of the server world.
- **Player List**: Manages the display of the tab player list.
- **Restart Manager**: Handles scheduled server restarts with warnings.

### Listeners
- **Anvil Colors**: Allows players with permission to use color codes on items in anvils.
- **Sign Colors**: Allows players with permission to use color codes on signs.
- **Command Logger**: Logs command usage by players for administrative review.
- **Player Activity**: Logs player joins, quits, and deaths.

### Loot Logging (In Development)
- Shift-right-click containers (Chests, Barrels, Shulker Boxes, etc.) to open a UI that displays a detailed history of all items taken or added, including who made the change and when.