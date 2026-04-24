# pvpOption

A Minecraft mod/plugin that adds opt-in PvP flagging. Players must explicitly enable PvP before they can deal or receive damage from other players. Unflagged players are fully protected.

---

## Branches

| Branch | Platform | MC Version |
|---|---|---|
| `multi_1.21.11` | Fabric + NeoForge (Architectury) | 1.21.11 |
| `plugin_1.21.11` | Paper, Purpur, Pufferfish | 1.21.11 |

---

## Features

- Opt-in PvP flagging — players choose when to engage
- Configurable warmup period before PvP activates
- Combat cooldown — prevents toggling off mid-fight
- Action bar HUD showing PvP and combat status
- Auto-unflag after configurable idle time
- Broadcast toggle announcements to the server
- Admin commands to force-set, lock, and unlock player flags
- Config hot-reload without restarting the server

> **Note:** The mod syncs the `pvp=` value in `server.properties` to match its own `pvpEnabled` setting on startup. The mod's config is the source of truth — editing `server.properties` directly has no effect.

---

## Commands

All commands use `/pvpoption` (alias `/pvp`).

| Command | Description |
|---|---|
| `/pvp` | Toggle your PvP flag |
| `/pvp on` | Enable your PvP flag |
| `/pvp off` | Disable your PvP flag |
| `/pvp status` | Show your current PvP status |
| `/pvp list` | List all flagged players |

### Admin (`pvpoption.admin` permission)

| Command | Description |
|---|---|
| `/pvp admin` | Show current config values |
| `/pvp admin enable\|disable` | Enable or disable PvP system-wide |
| `/pvp admin warmup <seconds>` | Set warmup duration (0 to disable) |
| `/pvp admin cooldown <seconds>` | Set combat cooldown duration (0 to disable) |
| `/pvp admin autounflag <minutes>` | Set idle auto-unflag time (0 to disable) |
| `/pvp admin broadcast on\|off` | Toggle flag change broadcasts |
| `/pvp admin set <player> on\|off` | Force-set a player's flag |
| `/pvp admin status <player>` | View a player's PvP status |
| `/pvp admin lock <player>` | Unflag and block a player from enabling PvP |
| `/pvp admin unlock <player>` | Restore a player's ability to use PvP |
| `/pvp admin reload [silent]` | Reload config from disk |

---

## Configuration

Config is written on first launch. All values can also be changed live via admin commands.

**Multi-loader** — `config/pvpoption.json`
**Paper plugin** — `plugins/pvpOption/config.json`

```json
{
  "pvpEnabled": true,
  "cooldownSeconds": 30,
  "warmupSeconds": 5,
  "broadcastToggle": true,
  "autoUnflagMinutes": 0
}
```

| Field | Default | Description |
|---|---|---|
| `pvpEnabled` | `true` | Master switch for the PvP system |
| `cooldownSeconds` | `30` | Seconds after combat before PvP can be disabled |
| `warmupSeconds` | `5` | Seconds before PvP activates after opting in |
| `broadcastToggle` | `true` | Announce flag changes to all players |
| `autoUnflagMinutes` | `0` | Minutes of idle time before auto-disabling PvP (0 = off) |

---

## Requirements

**Multi-loader:** Architectury API  
**Paper:** Paper 1.21.11+
