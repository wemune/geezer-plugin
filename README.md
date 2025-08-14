# Geezer Plugin

A private Minecraft server plugin with a variety of features for Geezer World.

## Features

### Core Systems
- **Container Logging**: Shift-right-click any container (Chests, Barrels, Shulker Boxes, etc.) to view a detailed log of all items added or removed, including who made the change and when. Features an interactive GUI with pagination, item details, enchantments, and lore display. Shulker Box contents are preserved even when the box is broken and picked up.
- **Player Statistics Tracking**: Comprehensive tracking of player activities including individual ore mining counts (Diamond, Ancient Debris, Gold, Iron, Emerald, Lapis, Redstone, Coal), mob kills, deaths, and total playtime with persistent database storage.
- **AFK Manager**: Automatically marks players as "away from keyboard" after 5 minutes of inactivity. Broadcasts status changes to all players and tracks movement to detect activity.
- **Ender Pearl Management**: Players can toggle their ability to pick up ender pearls using `/pearls`. This is a session-only setting that resets when the server restarts, particularly useful for enderman XP farms where pearl collection can be undesirable.
- **Backup Manager**: Performs automatic, scheduled backups of the server world with configurable file inclusion/exclusion and automatic cleanup of old backups. Multiple daily backup times can be configured. A manual backup can be triggered with `/backup`.
- **Restart Manager**: Handles scheduled server restarts with broadcast warnings and countdown messages. Can be toggled on or off with `/togglerestart`. Manual restarts are also available via `/restart` command.
- **Player List Management**: Dynamic header and footer management for the tab player list with real-time placeholder replacement and customizable formatting.
- **Daylight Cycle Announcer**: Broadcasts server-wide messages when the sun begins to rise and set, with configurable message content and timing.
- **Database Migration System**: Automatic database schema updates and migrations ensure compatibility across plugin versions without data loss.

### Commands

#### Player Commands
- **/playtime [player]**: Check your or another player's total playtime. Alias: `/pt`.
- **/stats [player]**: Check your or another player's gameplay statistics including ore mining counts, mob kills, deaths, and playtime.
- **/uptime**: Shows the server's current uptime.
- **/ping [player]**: Check your or another player's ping.
- **/seen <player>**: Check when a player was last online.
- **/coords [player]**: Broadcast your coordinates or send them privately to a player.
- **/msg <player> <message>**: Send a private message to a player. Aliases: `/tell`, `/w`, `/m`.
- **/reply <message>**: Reply to the last person who messaged you. Alias: `/r`.
- **/time**: Shows the current in-game time and time until the next sunrise/sunset.
- **/help**: Displays a list of all plugin commands.
- **/colors**: Shows the available color codes for use on signs and anvils.
- **/pearls**: Toggle your ability to pick up ender pearls (session-only, resets on restart).

#### Administrative Commands
- **/backup**: Manually starts a server backup.
- **/togglerestart**: Toggles the automatic restart schedule on or off.
- **/broadcast <message>**: Broadcasts a message to the entire server. Alias: `/bc`.
- **/restart**: Manually restart the server (requires permission).

### Gameplay & Events
- **Color Support**: Players can use color codes on signs and when renaming items in anvils.
- **Advanced Message System**:
    - Custom Message of the Day (MOTD) on player join with placeholder support.
    - A unique MOTD for a player's very first join.
    - Customizable server-wide join, quit, and death messages.
    - Queued announcement system with configurable delays and sound effects.
- **Enhanced Death System**: 
    - Sends private messages to players with exact coordinates (X, Y, Z) and world name where they died.
    - Different death messages for the deceased player vs other players.
    - Sound effects played on death events.
- **Intelligent Ore Announcements**: 
    - Server-wide announcements when players find diamond or ancient debris veins.
    - Advanced vein detection system that scans connected ore blocks to announce entire vein sizes.
    - Prevents duplicate announcements for the same ore vein.
    - Configurable announcement messages and thresholds.
- **World Change Announcements**: Broadcasts a message when a player travels between dimensions.
- **Player Activity Logging**: Comprehensive logging of player joins, quits, deaths, and command usage for administrative review with timestamp and details.

## Configuration

The `config.yml` file allows you to customize all of the features listed above. Key configuration options include:

### Core Settings
- **Timezone**: Configure server timezone for all time-sensitive features (affects restart schedules, backup times, etc.)
- **AFK Timeout**: Set the inactivity period before players are marked as AFK (default: 5 minutes)

### Backup Configuration
- **Backup Schedules**: Configure multiple daily backup times
- **File Inclusion/Exclusion**: Specify which files and directories to include or exclude from backups
- **Automatic Cleanup**: Set retention periods for automatic cleanup of old backups

### Message Customization
- **MOTD Messages**: Customize welcome messages for first-time and returning players
- **Join/Quit Messages**: Configure server-wide join and quit message formats
- **Death Messages**: Customize death announcement formats
- **Ore Announcements**: Configure diamond and ancient debris discovery messages
- **Color Support**: Enable/disable color code usage on signs and anvils

### Player List Settings
- **Header/Footer**: Customize tab list header and footer with placeholder support
- **Update Intervals**: Configure how frequently the player list updates

### Announcement System
- **Message Queue**: Configure announcement delays and timing
- **Sound Effects**: Enable/disable sound effects for various events
- **Broadcast Intervals**: Set timing for recurring announcements

All message formats support color codes and placeholders for dynamic content such as player names, coordinates, timestamps, and statistics.

## Permissions

Below is a list of all available permission nodes.

### Admin Permissions
- `geezer.backup`: Grants access to the `/backup` command.
- `geezer.togglerestart`: Grants access to the `/togglerestart` command.
- `geezer.broadcast`: Grants access to the `/broadcast` command.
- `geezer.restart`: Grants access to the `/restart` command for manual server restarts.
- `geezer.admin`: A parent node that grants all admin permissions (`geezer.backup`, `geezer.togglerestart`, `geezer.broadcast`, `geezer.restart`).

## Database

The plugin uses a SQLite database with HikariCP connection pooling for optimal performance. The database file (`geezer.db`) is created automatically in the plugin's data folder.

### Stored Data
- **Player Statistics**: Individual ore mining counts, mob kills, deaths, total playtime, and last seen timestamps
- **Container Logs**: Detailed logs of all container interactions including item details, enchantments, player actions, and timestamps
- **Player Activity**: Login/logout times, session durations, and activity tracking for AFK management

### Features
- **Automatic Schema Migration**: Database structure updates automatically when the plugin is updated
- **Connection Pooling**: Uses HikariCP for efficient database connection management
- **Data Persistence**: All player progress and statistics are preserved across server restarts
- **Backup Integration**: Database is included in automatic server backups

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
