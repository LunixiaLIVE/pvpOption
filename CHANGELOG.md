# pvpOption — Changelog

Opt-in PvP flagging — only flagged players deal/take PvP damage.
Works on client and server.

Format based on [Keep a Changelog](https://keepachangelog.com/); versioning per [SemVer](https://semver.org/).

## [1.5.3] — 2026-07-02

Multi-loader release for **Minecraft 1.21.1 – 1.21.10** (a single jar covering that range).

### Added
- **Fabric + NeoForge** support from a single **universal** jar (per-loader `-fabric` / `-neoforge` jars are also produced).
- **Minecraft 1.21.1 through 1.21.10** compatibility in one jar.

### Changed
- **No Architectury API required** — pvpOption is fully standalone. Events are wired natively (Fabric API `ALLOW_DAMAGE` on Fabric, `LivingIncomingDamageEvent` on the NeoForge event bus).
- **Back-ported for 1.21.1–1.21.10:** the PvP master-switch mixin targets `MinecraftServer.isPvpAllowed` here — Minecraft moved that method to `ServerLevel` in 1.21.11 (which is on the `multi_1.21.11` branch). The world identifier uses `ResourceLocation` (renamed to `Identifier` in 1.21.11).

### Notes
- **Floor is 1.21.1:** Minecraft 1.21.0's Fabric API ships an older data-attachment API (`AttachmentRegistry.create`) incompatible with the legacy-migration code, so 1.21.0 is not included.
- **Ceiling is 1.21.10:** 1.21.11 relocated `isPvpAllowed` and renamed `ResourceLocation` — a hard break — so it lives on `multi_1.21.11`.

### Dependencies
- **Fabric jar:** Minecraft 1.21.1–1.21.10, Fabric Loader >= 0.19.2, Fabric API *(Fabric only)*
- **NeoForge jar:** Minecraft 1.21.1–1.21.10, NeoForge 21.1.x–21.10.x  *(no Fabric API, no Architectury)*
