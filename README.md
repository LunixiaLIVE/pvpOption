# pvpOption

Opt-in PvP flagging — only flagged players deal/take PvP damage.
**Client & server.**

## Features

- Opt-in PvP flag with warmup and combat cooldown
- Optional auto-unflag on inactivity
- Integrates with expRepair (repair suppression)

## Versions & downloads

This repository uses a **branch-per-version** layout: this `main` branch is documentation only — the code for each Minecraft version lives on its own branch, each with its own history and `CHANGELOG.md`.

| Branch | Minecraft | Loaders | Dependencies | Notes |
|--------|-----------|---------|--------------|-------|
| [`multi_26.2`](https://github.com/LunixiaLIVE/pvpOption/tree/multi_26.2) | 26.2.x | Fabric · NeoForge | Fabric API *(Fabric only)* | [changelog](https://github.com/LunixiaLIVE/pvpOption/blob/multi_26.2/CHANGELOG.md) |
| [`multi_26.1`](https://github.com/LunixiaLIVE/pvpOption/tree/multi_26.1) | 26.1, 26.1.1, 26.1.2 | Fabric · NeoForge | Fabric API *(Fabric only)* | [changelog](https://github.com/LunixiaLIVE/pvpOption/blob/multi_26.1/CHANGELOG.md) |
| [`multi_1.21.11`](https://github.com/LunixiaLIVE/pvpOption/tree/multi_1.21.11) | 1.21.11 | Fabric · NeoForge | Architectury API, Fabric API | — |
| [`plugin_1.21.11`](https://github.com/LunixiaLIVE/pvpOption/tree/plugin_1.21.11) | 1.21.11 | Paper | Paper (no extra deps) | — |

The `multi_*` branches each build a single **universal** jar that runs on **both** Fabric and NeoForge (per-loader `-fabric` / `-neoforge` jars are also produced). The 26.x builds are fully standalone — **no Architectury API at runtime**.

## License

MIT
