# Geezer Plugin

A private Minecraft server plugin with a variety of features for Geezer World.

## Features

### Core Systems
- **Container Logging**: Shift-right-click any container (Chests, Barrels, Shulker Boxes, etc.) to view a detailed log of all items added or removed, including who made the change and when. Shulker Box contents are preserved even when the box is broken and picked up.
- **AFK Manager**: Automatically marks players as "away from keyboard" after a period of inactivity.
- **Backup Manager**: Performs automatic, scheduled backups of the server world. A manual backup can be triggered with `/backup`.
- **Restart Manager**: Handles scheduled server restarts with broadcast warnings. Can be toggled on or off with `/togglerestart`.
- **Player List**: Manages the header and footer of the tab player list, with support for placeholders.
- **Daylight Cycle Announcer**: Broadcasts a server-wide message when the sun begins to rise and set.

### Commands
- **/playtime [player]**: Check your or another player's total playtime.
- **/stats [player]**: Check your or another player's gameplay statistics.
- **/uptime**: Shows the server's current uptime.
- **/ping [player]**: Check your or another player's ping.
- **/seen <player>**: Check when a player was last online.
- **/coords [player]**: Broadcast your coordinates or send them privately to a player.
- **/msg <player> <message>**: Send a private message to a player.
- **/reply <message>**: Reply to the last person who messaged you.
- **/time**: Shows the current in-game time and time until the next sunrise/sunset.
- **/help**: Displays a list of all plugin commands.
- **/colors**: Shows the available color codes for use on signs and anvils.
- **/backup**: Manually starts a server backup.
- **/togglerestart**: Toggles the automatic restart schedule on or off.

### Gameplay & Events
- **Color Support**: Players can use color codes on signs and when renaming items in anvils.
- **Customizable Messages**:
    - Custom Message of the Day (MOTD) on player join.
    - A unique MOTD for a player's very first join.
    - Customizable server-wide join, quit, and death messages.
- **Death Coordinates**: Sends a private message to players with the coordinates of where they died.
- **Rare Ore Announcements**: A server-wide announcement when a player finds a vein of diamonds or ancient debris.
- **World Change Announcements**: Broadcasts a message when a player travels between dimensions.
- **Player Activity Logging**: Logs player joins, quits, deaths, and command usage for administrative review.

## Configuration

The `config.yml` file allows you to customize all of the features listed above, including AFK timeouts, backup schedules, message formats, and much more.

## Permissions

Below is a list of all available permission nodes.

### Admin Permissions
- `geezer.backup`: Grants access to the `/backup` command.
- `geezer.togglerestart`: Grants access to the `/togglerestart` command.
- `geezer.admin`: A parent node that grants all admin permissions (`geezer.backup`, `geezer.togglerestart`).

## Database

The plugin uses a SQLite database to store player stats, playtime, and container logs. The database file (`geezer.db`) is created automatically in the plugin's data folder.

## Building from Source

1.  Clone the repository: `git clone https://github.com/wemune/geezer-plugin.git`
2.  Navigate to the project directory: `cd geezer-plugin`
3.  Build the plugin using the Gradle wrapper. This will create a single, runnable JAR file with all dependencies included.
    -   On Windows (Command Prompt): `gradlew.bat shadowJar`
    -   On Windows (PowerShell): `.\gradlew.bat shadowJar`
    -   On Linux/macOS: `./gradlew shadowJar`
4.  The compiled JAR file (`Geezer.jar`) will be located in the `build/libs` directory.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

---
**Note**: This plugin is designed for a private server and may include features or configurations specific to that environment.
