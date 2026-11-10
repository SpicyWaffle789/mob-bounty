# MobBounty

A Paper plugin that pays players in-game currency for killing mobs. Rewards are configurable per mob type, with separate amounts for bosses and the Ender Dragon.

## Features

- Pays players through [Vault](https://www.spigotmc.org/resources/vault.34315/), so it works with any Vault-compatible economy plugin (EssentialsX, CMI, etc.)
- Per-mob reward amounts (e.g. `ENDERMAN: 1.0`)
- Separate rewards for bosses (Wither, Warden, Elder Guardian) and the Ender Dragon
- Customisable reward message with colour codes
- `/mobbounty reload` to apply config changes without a restart

## Requirements

- Paper (or a compatible fork) 1.20.x
- Java 17+
- [Vault](https://www.spigotmc.org/resources/vault.34315/)
- An economy plugin that supports Vault

## Installation

1. Download the jar from the [Actions](../../actions) artifacts, or [build it yourself](#building).
2. Put the jar in your server's `plugins/` folder.
3. Make sure Vault and an economy plugin are installed.
4. Start the server. The config is created at `plugins/MobBounty/config.yml`.

## Configuration

```yaml
rewards:
  # Paid for any mob not listed below
  default: 0.50
  # Paid for Wither, Warden and Elder Guardian
  boss: 50.00
  ender-dragon: 10000.00

  # Per-mob overrides. These take priority over everything above.
  # Use the Bukkit EntityType name (UPPERCASE, underscores).
  # Set a mob to 0 to give no reward for it.
  mobs:
  #  ENDERMAN: 1.0
  #  ZOMBIE: 0.75
  #  CREEPER: 1.25

messages:
  kill-reward: "&a+$%amount% for killing a %mob%!"
  reloaded: "&aMobBounty config reloaded."
  no-permission: "&cYou don't have permission to do that."
```

### Reward priority

For each kill, the first match wins:

1. An entry under `rewards.mobs`
2. `rewards.ender-dragon` (Ender Dragon only)
3. `rewards.boss` (Wither, Warden, Elder Guardian)
4. `rewards.default`

Mob names are the Bukkit [`EntityType`](https://jd.papermc.io/paper/1.20/org/bukkit/entity/EntityType.html) names, such as `WITHER_SKELETON` or `IRON_GOLEM`. Unknown names are skipped with a warning in the console.

### Message placeholders

| Placeholder | Value |
| ----------- | ----- |
| `%amount%`  | The reward, formatted to 2 decimal places |
| `%mob%`     | The mob's name, e.g. `Ender Dragon` |

Colour codes use `&` (e.g. `&a` for green).

> **Updating from an older version?** Bukkit doesn't overwrite an existing `config.yml`. Delete it to regenerate, or copy the new options in by hand.

## Commands and permissions

| Command | Permission | Default | Description |
| ------- | ---------- | ------- | ----------- |
| `/mobbounty reload` | `mobbounty.reload` | op | Reloads `config.yml` |

## Notes

- Only kills by players are rewarded. Players and armor stands never pay out.
- Mob spawner farms and similar setups pay the full reward. Lower the `default` amount or set specific mobs to `0` if this affects your economy.

## Building

Requires JDK 17.

```bash
./gradlew build
```

The jar is written to `build/libs/`. If the Gradle wrapper isn't in your checkout, install Gradle 8.5 and run `gradle build` instead.

## License

[GPL-3.0](LICENSE)
