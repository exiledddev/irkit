# IRKit

A kit plugin for Paper 1.21.11 (it also runs on Purpur). Build kits in an in-game editor, hand them to anyone with vanilla selectors, and let players pick their own from a menu. It also includes the `/randarmor`, `/randitem` and `/powersuit` commands from the old IsMP plugin, now with full selector support.

Every command that targets players uses the real vanilla selector argument. Names, UUIDs and every selector filter work: `@a[team=red]`, `@a[distance=..10,gamemode=adventure]`, `@r[limit=3]`, `@p[tag=cast]`, and so on. `/execute as ... run` works too. That makes it work alongside [TeamSplit](https://github.com/exiledddev/teamsplit):

```
/kit give knight @a[team=red]
/kit give archer @a[team=blue]
/powersuit 3 @a[team=green]
/teamrun red kit give knight {player}
```

## Install

1. Download the jar: open this repo's **Actions** tab, click **Build**, open the latest run, and download **IRKit** under **Artifacts**. It's a zip; the jar is inside.
2. Put it in your server's `plugins/` folder and restart.

## Making a kit

1. Run `/kit create knight`. This opens the kit editor.
2. Arrange the kit exactly like your own inventory:
   - Rows 1–3 are the inventory and row 4 is the hotbar.
   - The bottom row has slots for the **helmet, chestplate, leggings, boots and offhand**. Anything goes there, even a carved pumpkin or a banner on the head.
   - The last bottom slot is the kit's **menu icon**. It's optional and isn't given to players.
   - **Load my inventory** copies what you're wearing and carrying straight into the editor.
3. Click **Save**.

You can also run plain `/kit create`, or click **New kit** in `/kits`. Then IRKit asks for the name in an anvil when you save: type it and click the paper on the right. Closing the anvil takes you back to the editor with your items still there.

Closing the editor with Esc or **Cancel** discards your changes. Items you place in the editor come out of your own inventory, so build kits in creative mode.

## Giving kits

`/kit give <kit> <targets> [--add|--replace]`

- **replace** (the default) clears the player's inventory, armor and offhand, then puts every item in the exact slot you gave it in the kit. Hotbar layouts match the kit.
- **add** keeps what they have. Kit items go into their kit slot if it's free, or anywhere else if it isn't. Their old armor and offhand move into their inventory, and anything that doesn't fit drops at their feet.

In both modes armor and offhand items are equipped straight away. You can change the default mode in `config.yml`.

## Players taking kits themselves

- `/kits` opens a menu of every kit:
  - Left-click takes a kit.
  - Right-click previews it.
  - Staff with `irkit.manage` can shift-click to edit a kit, or click **New kit**.
- `/kit claim <kit>` and `/kit preview <kit>` do the same from chat.
- Turn on `per-kit-permissions` in the config to limit which kits each player sees and can take, with `irkit.kit.<name>` (or `irkit.kit.*` for all of them).

## Commands

| Command | What it does | Permission |
|---|---|---|
| `/kits` | Kit menu: take, preview, edit | `irkit.kits` |
| `/kit claim <kit>` | Take a kit for yourself | `irkit.kits` |
| `/kit preview <kit>` | Look at a kit without taking it | `irkit.kits` |
| `/kit list` | List kits | any IRKit permission |
| `/kit give <kit> <targets> [--add\|--replace]` | Give a kit to players | `irkit.give` |
| `/kit create [name]` | Build a new kit in the editor | `irkit.manage` |
| `/kit edit <kit>` | Change a kit in the editor | `irkit.manage` |
| `/kit copy <kit> <new name>` | Duplicate a kit (handy for knight_red / knight_blue) | `irkit.manage` |
| `/kit rename <kit> <new name>` | Rename a kit | `irkit.manage` |
| `/kit delete <kit>` | Delete a kit (asks you to confirm) | `irkit.manage` |
| `/randarmor <targets>` | Random armor with random Protection, Mending and Unbreaking | `irkit.give` |
| `/randitem <targets>` | Random sword, pickaxe and axe, plus golden apples, wind charges and steak | `irkit.give` |
| `/powersuit <1-5> <targets>` | Tiered diamond-to-netherite gear, tools, shield, potions and supplies | `irkit.give` |
| `/irkit reload` | Reload `config.yml` and the kit files | `irkit.manage` |

All of these permissions default to op. `irkit.kit.<name>` only matters when `per-kit-permissions` is on.

`/randarmor`, `/randitem` and `/powersuit` hand out exactly the same gear as the old IsMP plugin. The differences:

- They now take a `<targets>` argument, where `/randarmor` and `/randitem` used to always hit everyone. Use `@a` for the old behavior.
- They work from the console and command blocks.

## Configuration

`plugins/IRKit/config.yml`:

```yaml
kits:
  default-give-mode: replace   # /kit give without --add/--replace: replace or add
  claim-give-mode: replace     # when players take a kit themselves
  per-kit-permissions: false   # require irkit.kit.<name> to see and take a kit in /kits

gear:
  drop-replaced-gear: true     # /randarmor and /powersuit drop the armor they replace (false deletes it)
```

Kits are saved to `plugins/IRKit/kits/<name>.yml`, one file per kit. Items are stored in Paper's own format, which upgrades them automatically when Minecraft updates. To back up or move kits between servers, copy those files.

## Building

You need JDK 21. Alternatively, let GitHub build it: every push runs the **Build** workflow, which uploads the jar as an artifact.

```
./gradlew build          # jar ends up in build/libs/
./gradlew runServer      # starts a local Paper 1.21.11 test server with the plugin installed
```
