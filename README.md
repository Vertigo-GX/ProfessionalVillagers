# Professional Villagers
A small server-side/singleplayer Minecraft mod for Fabric that makes villagers slightly easier to work with.

(Note: Has only been tested in singeplayer so far!)

### Features:
* Enchantments level with the librarian instead of having a set level.
* Right-clicking on a villager with an emerald block resets its trade offers. Only works if the villager doesn't have any experience.
* Right-clicking on a wandering trader with an emerald block dismisses it after 5 seconds. Doesn't dismiss traders spawned by spawn eggs.
* Right-clicking on a villager with a poisonous potato resets its trade offers, level and experience. The villager must have the Weakness effect, same as when curing a zombie villager with a golden apple.
* Right-clicking on a librarian with an enchanted book teaches it a random enchantment from that book. Can be done once per master-level librarian. The book is consumed in the process.
* Modifies the trade offers of certain professions:
    * Farmers always have the pumpkin trade.
    * Fishermen always have the raw cod trade.
    * Toolsmiths no longer sell axes, but always have the diamond hoe and diamond shovel trades.
* All features are optional.

### Configuration:
Either edit the config file (`professional-villagers.ini`) found in the Fabric config folder, or use [Mod Menu](https://modrinth.com/mod/modmenu) to edit the options in game.

### Recommendations:
* [Mod Menu](https://modrinth.com/mod/modmenu)