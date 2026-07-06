<div align="center">

# ⚔️ pvpOption

### Opt-in PvP flagging — only flagged players deal/take PvP damage.

![](https://img.shields.io/badge/Fabric-DBA463?style=for-the-badge&logoColor=white)&nbsp;![](https://img.shields.io/badge/NeoForge-F16436?style=for-the-badge&logoColor=white)&nbsp;![](https://img.shields.io/badge/Paper-2A9DF4?style=for-the-badge&logoColor=white)&nbsp;

[![](https://img.shields.io/badge/Download_on-Modrinth-00AF5C?style=for-the-badge&logo=modrinth&logoColor=white)](https://modrinth.com/project/pvpoption)&nbsp;[![](https://img.shields.io/badge/Download_on-CurseForge-F16436?style=for-the-badge&logo=curseforge&logoColor=white)](https://www.curseforge.com/minecraft/mc-mods/pvpoption-mod)

![](https://img.shields.io/badge/Minecraft-26.x_%7C_1.21.x-62B47A?style=flat-square) ![](https://img.shields.io/badge/Side-Single_Player_%26_Server-8E44AD?style=flat-square) ![](https://img.shields.io/badge/Fabric_API-required_on_Fabric-4A90D9?style=flat-square) ![](https://img.shields.io/badge/License-MIT-blue?style=flat-square)

</div>

---

## ✨ What it does

On most servers PvP is a single switch — it's on for everyone or off for everyone. pvpOption replaces that
with a **per-player, opt-in flag**: you take and deal player-versus-player damage **only** when you have
chosen to, and **only against other players who have chosen to as well**. Everyone else stays fully
protected.

- **Opt in with one command.** `/pvp` (or `/pvpoption`) flips your flag on. A short **warmup** counts down
  first — a grace period so you can't instantly ambush someone — then your flag goes live.
- **Both sides must agree.** An attack only lands when **attacker and defender are both flagged**. Swing at
  an unflagged player and the hit is cancelled with a message telling you why — no accidental griefing, no
  "I didn't mean to hit them."
- **A combat cooldown keeps fights honest.** Every time a valid PvP hit connects, both players get a
  **combat timer**. You can't disable your flag and run to safety mid-fight — you're locked into PvP until
  the timer expires.
- **Optional auto-unflag.** Admins can have the mod quietly drop a player's flag after a stretch of
  inactivity, so people who forget to `/pvp off` don't wander around flagged forever.
- **A live action-bar HUD** shows your current state at a glance — warmup countdown, **⚔ PvP Active**, and
  the remaining combat cooldown.
- **Broadcasts (optional).** When someone enters or leaves PvP mode the server can announce it, so everyone
  knows who's fair game.
- **Admin controls for everything.** Toggle the whole system, tune warmup / cooldown / auto-unflag live,
  force a player's flag on or off, and **lock** troublemakers out of PvP entirely.
- **expRepair integration.** If [expRepair](https://github.com/LunixiaLIVE/expRepair) is installed, the mod
  can **suppress XP repair while a player is flagged** — no topping off your gear in the middle of a duel.
- **Server-side.** A **vanilla client** can join a modded server and it just works — the flag, the gating,
  and the action-bar HUD are all driven server-side. Runs in single-player too.

Player state (your flag and any admin lock) is stored per-UUID and **persists across restarts** — no new
blocks or items, no client mod required.

## 🔧 How it works

### Flag states

Every player is always in exactly one of these states:

| State | What it means |
|:--|:--|
| 🟢 **Disabled** *(default)* | Fully protected. You can't be hit by, or hit, other players. |
| 🟡 **Warming up** | You ran `/pvp on` and the warmup is counting down. Not yet vulnerable — you can still `/pvp off` to cancel. |
| 🔴 **Enabled** | Your flag is live. You deal and take damage from other **flagged** players. |
| 🔴 **In combat** | Flagged *and* inside the combat cooldown from a recent hit. You **cannot** unflag until it expires. |
| ⛔ **Restricted** | An admin has **locked** you out of PvP. You can't flag on until you're unlocked. |

### When damage is allowed

Player-versus-player damage is gated at the moment of the hit. A blow only connects when **all** of these
are true:

1. The PvP system is **enabled** server-wide.
2. The **attacker** is flagged for PvP.
3. The **defender** is flagged for PvP.

If any check fails, the damage is **cancelled** and the attacker is told why ("You are not flagged…" or
"That player is not flagged…"). When a hit *is* allowed, both players' combat cooldowns are refreshed and
their activity timers reset. Only genuine player-vs-player hits are considered — mobs, the environment, and
self-damage are never affected.

### System-wide switch

The admin `enable` / `disable` toggle is the master switch. When it's off, no PvP damage passes at all,
pending warmups are cancelled, and cooldowns are cleared. The mod also keeps `pvp=` in **`server.properties`**
in sync with this setting and forces the world's PvP-allowed check to match it, so vanilla systems agree with
the mod.

### expRepair integration

If [expRepair](https://github.com/LunixiaLIVE/expRepair) (1.7+) is present, pvpOption registers a repair-
suppression hook with it. While `disableRepairInPvP` is on, any player **currently flagged for PvP** has
their XP repair suppressed — so you can't refill durability in the middle of a fight. The hook is wired up
only when expRepair is actually installed; without it the `disableRepair` admin command simply reports that
expRepair isn't present.

## ⌨️ Commands

The root command is **`/pvpoption`**, with **`/pvp`** as a shorter alias. Running it bare **toggles** your
own flag.

### Player commands

| Command | What it does |
|:--|:--|
| `/pvp` | Toggle your own PvP flag on or off. |
| `/pvp on` | Opt in — starts the warmup, then flags you. |
| `/pvp off` | Opt out — cancels a pending warmup, or unflags you (blocked while in combat cooldown). |
| `/pvp status` | Show your own flag state, plus any remaining combat cooldown. |
| `/pvp list` | List every player currently flagged for PvP (and who's in combat). |

### Admin commands

All admin subcommands live under `/pvp admin …` and require **operator / game-master permission**
(`COMMANDS_GAMEMASTER`, i.e. permission level 2+).

| Command | What it does |
|:--|:--|
| `/pvp admin` | Show the current admin settings (system state, warmup, cooldown, auto-unflag, broadcast, repair). |
| `/pvp admin enable` \| `disable` | Turn the whole PvP system on or off server-wide. |
| `/pvp admin warmup [seconds]` | Show or set the opt-in warmup, in seconds (`0` = instant). |
| `/pvp admin cooldown [seconds]` | Show or set the combat cooldown, in seconds (`0` = disabled). |
| `/pvp admin autoUnflag [minutes]` | Show or set the idle auto-unflag time, in minutes (`0` = disabled). |
| `/pvp admin broadcast on` \| `off` | Toggle the server-wide "entered/left PvP mode" announcements. |
| `/pvp admin disableRepair on` \| `off` | Toggle expRepair repair suppression for flagged players. |
| `/pvp admin reload [silent]` | Reload the config from disk (`silent` announces only to you). |
| `/pvp admin set <player> on` \| `off` | Force a player's flag on or off. |
| `/pvp admin status <player>` | Inspect another player's flag, cooldown, and lock state. |
| `/pvp admin lock <player>` | Unflag a player and **restrict** them from opting into PvP. |
| `/pvp admin unlock <player>` | Lift a restriction and restore a player's PvP access. |

## 💡 Use cases

- **Mixed communities.** Let builders, farmers, and explorers coexist with duelists on the same world —
  nobody gets ganked who didn't sign up for it.
- **Consensual duels.** Two players both `/pvp on`, fight it out, and the combat cooldown stops the loser
  from flag-dancing to escape.
- **Event & arena servers.** Flip the master switch, tune the warmup and cooldown for your format, and use
  `admin set` to flag participants at the start of a round.
- **Moderation.** `admin lock` a player who abuses PvP so they physically can't flag on, without touching
  anyone else.
- **Hardcore duels with real stakes.** Pair with expRepair and `disableRepair` so gear damage during a
  fight actually matters.

## ⚙️ Configuration

The config lives at **`config/pvpoption.json`** and is created with defaults on first launch. Every value is
also settable live through the `/pvp admin …` commands, and **`/pvp admin reload`** re-reads the file without
a restart. Per-player flags and locks are stored separately in **`config/pvpoption/playerdata.json`**.

```jsonc
{
  "pvpEnabled": true,          // master switch — is the PvP system active at all? (also syncs server.properties `pvp=`)
  "disableRepairInPvP": false, // suppress expRepair XP repair while a player is flagged (needs expRepair 1.7+)
  "cooldownSeconds": 30,       // combat cooldown after a valid hit; you can't unflag until it expires (0 = disabled)
  "warmupSeconds": 5,          // grace-period countdown before `/pvp on` actually flags you (0 = instant)
  "broadcastToggle": true,     // announce to everyone when a player enters or leaves PvP mode
  "autoUnflagMinutes": 0       // auto-drop a flag after this many idle minutes (0 = never)
}
```

> [!TIP]
> Changing a setting through `/pvp admin` writes it straight back to `pvpoption.json`, so in-game tweaks
> survive restarts. If you edit the file by hand, run `/pvp admin reload` to apply it live.

## 📦 Versions &amp; downloads

> [!NOTE]
> This repo uses a **branch-per-version** layout. This `main` branch is **documentation only** — the code for each Minecraft version lives on its own branch, each with an independent history and its own `CHANGELOG.md`.

| Branch | Minecraft | Loaders | Dependencies | Log |
|:------:|:---------:|:-------:|:------------:|:---:|
| [`multi_26.2`](https://github.com/LunixiaLIVE/pvpOption/tree/multi_26.2) | 26.2.x | Fabric · NeoForge | Fabric API *(Fabric only)* | [📄](https://github.com/LunixiaLIVE/pvpOption/blob/multi_26.2/CHANGELOG.md) |
| [`multi_26.1`](https://github.com/LunixiaLIVE/pvpOption/tree/multi_26.1) | 26.1, 26.1.1, 26.1.2 | Fabric · NeoForge | Fabric API *(Fabric only)* | [📄](https://github.com/LunixiaLIVE/pvpOption/blob/multi_26.1/CHANGELOG.md) |
| [`multi_1.21.11`](https://github.com/LunixiaLIVE/pvpOption/tree/multi_1.21.11) | 1.21.11 | Fabric · NeoForge | Fabric API *(Fabric only)* | [📄](https://github.com/LunixiaLIVE/pvpOption/blob/multi_1.21.11/CHANGELOG.md) |
| [`multi_1.21.1`](https://github.com/LunixiaLIVE/pvpOption/tree/multi_1.21.1) | 1.21.1–1.21.10 | Fabric · NeoForge | Fabric API *(Fabric only)* | [📄](https://github.com/LunixiaLIVE/pvpOption/blob/multi_1.21.1/CHANGELOG.md) |
| [`plugin_1.21.11`](https://github.com/LunixiaLIVE/pvpOption/tree/plugin_1.21.11) | 1.21.11 | Paper | Paper (no extra deps) | — |

> [!TIP]
> Every `multi_*` branch builds **one jar that runs on both Fabric and NeoForge**. On 26.x that's a shared universal jar (Minecraft is unobfuscated there); on 1.21.x it's a jar-in-jar bundle (`-multi.jar`) with the Fabric and NeoForge builds nested inside, each loader picking its own. Per-loader `-fabric` / `-neoforge` jars are produced too (`build/staging/`). Fully self-contained — **no extra library mods to install**.

<details>
<summary>🛠️ <b>Building from source</b></summary>

Each code branch is a self-contained Gradle project. Grab the branch for your Minecraft version:

```bash
git clone -b multi_26.2 https://github.com/LunixiaLIVE/pvpOption.git
cd pvpOption
./gradlew build
```

The universal jar lands in `build/libs/` — drop it into your `mods/` folder on either loader.
</details>

## 📄 License

Released under the **MIT License**.

<div align="center"><sub>⛏️ Part of <a href="https://github.com/LunixiaLIVE/Lunixia-Minecraft-QOL-Mods">Lunixia's Minecraft QOL Mods</a>.</sub></div>
