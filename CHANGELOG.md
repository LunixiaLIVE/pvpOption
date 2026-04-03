# Changelog

## v1.5.2
- Fixed mod environment to `*` — now loads correctly in singleplayer and on dedicated servers
- Lowered Java bytecode target to 21 (minimum JVM: Java 21+)

## v1.5
- Added expRepair integration hook — expRepair checks pvpOption's PvP flag before triggering passive/manual repair during combat

## v1.4
- Added admin commands to force-set or clear any player's PvP flag

## v1.3
- PvP flag now persists across server restarts (saved to config)

## v1.2
- Added `/pvp status` to check your own or another player's flag
- Added operator broadcast when a player toggles PvP

## v1.1
- Updated to Minecraft 1.21.11

## Initial Release
- Opt-in PvP flagging system — only players flagged for PvP can deal or take damage from each other
- `/pvp` to toggle your own flag on/off
- Unflagged players are completely immune to player damage
