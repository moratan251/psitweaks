## Changelog

#### v1.0.0 (plan)

> Work in progress. Numbers and behavior may change before release.


- Added attack spell pieces
  + Trick: Chain Lightning: discharges lightning from the focal point in the direction; it strikes the nearest enemy and branches to the enemies around it
  + Trick: Mine Genesis: shakes the ground so a shockwave spreads out and throws up the creatures standing on it
  + Trick: Burst: vaporizes the liquid inside the target so it bursts from within
  + Trick: Air Bullet: fires a bullet of compressed air
  + Trick: Dancing Blades: deploys the swords in your Blade Pouch as blades that cut nearby enemies
  + Trick: Pseudo Meteor: calls pseudo meteors down around the position
  + Trick: Micro Black Hole: creates a tiny black hole that pulls creatures in and swings them around


- Unified who attack spells can hit
  + Attack spells, beams, spell circles and projectiles do not hit the caster, the caster's pets or allies on the same team (regardless of the friendly fire setting). Players are not hit when PvP is off
  + Projectiles from Trick: Blaze Ball, Trick: Dry Meteor and Trick: Aqua Cutter pass through the caster's side
  + Attack spells (including Trick: Guillotine) now work on bosses such as the Wither and the Ender Dragon, and on creatures that are riding something or sleeping


- Added the Blade Pouch
  + Holds up to 8 swords and works in any Curios slot
  + Trick: Dancing Blades cuts with the swords in the pouch; the sword's attack damage, enchantments and on-hit effects all apply (the swords do not lose durability)


- Remade Trick: Barrier as a shield
  + Raises a shield that takes damage in the target's place for one minute. Phenomenon Interference applies to the shield amount
  + Removed the time input (existing spells still load; a piece connected to it is no longer used)
  + The target does not catch fire while the shield is up. Poison, hunger, falling and similar damage pass through the shield
  + Casting it again keeps whichever shield has more left
  + Shows the remaining shield and time on the HUD (can be toggled in the client config)
  + Added Selector: Barrier Amount


- Remade Trick: Parade as a decoy spell
  + Instead of dodging damage by chance, it conjures a lookalike of the caster at the position. Nearby mobs target the lookalike
  + While it lasts, spells from other players that target the caster directly fail, and Operator: Entity Position returns the lookalike's position
  + Now unlocked by research


- Rebuilt Trick: Meteor Line
  + Covers the position (radius 16 to 32) in a sphere of night and rains streaks of light from its stars on the creatures inside. They also reach under roofs
  + Potency 1428 → 1400, Cost 12000 → 12500


- Remade Trick: Cocytus
  + It no longer takes a target. It blows a blizzard forward and freezes the minds of all the mobs it catches forever (existing spells still load; a piece connected to the target is no longer used)
  + Players, the caster's pets and allies, and bosses are not frozen. It deals no damage
  + A frozen mob now looks encased in ice. The ice breaks if something else wakes the mob up
  + Cost 5000 → 10000


- Adjusted existing attack spells
  + Trick: Aqua Cutter: now looks like a crescent water blade and splashes on hit. Targets it hits stay wet for 10 seconds (they cannot catch fire while wet). Burning targets take 1.25x damage instead, and the fire is put out
  + Trick: Active Air Mine: new spherical shockwave visuals and its own damage type


- Added Zone Magic: Resident SpellGrams that keep working inside an area
  + Trick: Soak Zone
  + Trick: Heat Zone
  + Trick: Freeze Zone
  + Trick: Turbulence Zone
  + Trick: Gravity Zone
  + Trick: Interference Zone
  + Trick: Silence Zone
  + Trick: Charged Zone
  + Trick: Acceleration Zone: growing things inside (crops, saplings, Budding Amethyst and others in the block tag psitweaks:zone_acceleratable) get extra random ticks and grow (Power + 5) times as fast in all. Your own zones' random block changes speed up by the same factor where they overlap
  + Trick: Deceleration Zone: living beings inside get the new effect Torpor (movement and flying speed −(Power + 4) × 5%, players' attack speed −(Power + 4) × 3%), and mobs and projectiles slow down every tick. It spares your side, bosses and magic projectiles
  + Trick: Contamination Zone (with Mekanism): exposes the living beings inside to radiation up to (Power + 4) × 0.1 mSv, without leaving radiation sources. It spares your side


- Added Trick: Decontamination (with Mekanism): places a Resident SpellGram core (not a zone) that weakens the radiation sources within 32 blocks, nearer and stronger ones first, as much as its Cost allows. It is unlocked by research (Program: Decontamination)


- Common rules of Zone Magic
  + Zone tricks are unlocked by research: Program: Zone Magic unlocks the first eight above, and the Acceleration, Deceleration and Contamination Zones each have a program of their own
  + The area is set by a Matrix (region matrix) and a Size (at most 16); leaving out the Matrix centers it on the caster. A zone lasts for its Time (at most 120 seconds, 30 when left out)
  + Zones that take a Power need one (at least 1). Potency and Cost grow with the area and the Power, and the Cost also grows with the Time. Every zone with a Power but the Gravity Zone works at a fair strength even at Power 1: its effects, totals and Cost follow the Power plus 4, and its Potency follows the Power itself. The Gravity, Acceleration, Deceleration and Contamination Zones take a Power of at most 10
  + What stands in for fuel or energy (heat, cooling, FE and wind) is a total set by the zone's Cost and shared by everything inside. The Heat, Freeze, Turbulence and Charged Zones take a Power above 10, which only adds to that total
  + Where your own zones of the same kind overlap, their totals add up and other effects use only the strongest one. Where your own Heat Zone and Freeze Zone overlap, they cancel each other out
  + Where other casters' zones overlap, only the zones of the caster with the highest Phenomenon Interference work
  + Zones change blocks at random, like random ticks but faster (each block about once every 5 seconds): the Heat Zone melts ice and snow layers and dries Wet Sponges, the Freeze Zone freezes water sources, the Soak Zone moistens farmland and wets Sponges, and the Charged Zone scrapes oxidation off copper
  + Aiming at the core of a zone shows its integrity and how much of its Heat, Cooling, Charge or Wind is in use out of the total; the Acceleration, Deceleration and Contamination Zones show their strength
  + The floor of a zone shows a magic circle in the CAD's color (its resolution can be lowered in the client config)
  + Zone particles are capped for all zones together (Zone Particle Limit in the client config), thin out with distance from the camera, and follow the Particles video setting


- Added the Zone Feeder
  + When it is inside a Charged Zone, the zone uses the Zone Feeder's FE to power machines


- Added the Zone Receiver
  + Kept in your inventory inside a Charged Zone, it lets the zone send FE to your other items and equipment that take FE


- Reorganized SpellGram objects
  + Magic entities with integrity are now SpellGram objects: Resident SpellGrams (Fire Circle, Ice Circle and the zones), the Meteor Line night and the Micro Black Hole
  + They are not destroyed or pushed by ordinary attacks such as swords, arrows and explosions, and are not saved with the chunk
  + They disappear when the caster is gone (logged out or moved to another dimension)
  + Psi's Trick: Add Motion, Mass Add Motion, Blink, Mass Blink and Mass Exodus cannot move them, except your own circles
  + Added integrity. An object disappears when its integrity runs out (the Meteor Line night shatters, and the Micro Black Hole vanishes without crushing anything). Integrity depends on the caster's Phenomenon Interference when the object is placed (the multiplier can be changed in the config), and Resident SpellGrams also lose it over time
  + Aim at the core within 5 blocks to see its name and integrity (can be toggled in the client config)
  + One player or one Autonomous Caster can keep up to 32 Resident SpellGrams, several of the same kind included. Placing more removes the oldest (changeable in the config)
  + Trick: Fire Circle / Ice Circle now deal their own damage types, so Barrier blocks them and kills are credited to the caster
  + Trick: Ice Circle no longer keeps building up freezing


- Reworked Trick: Fire Circle / Ice Circle
  + Added a Time input (1 to 120 seconds, 60 if left out). The Cost and integrity grow in proportion to the Time; the Cost is at least 50
  + Adjusted the damage formula so that the total damage follows the Cost
  + Armor now reduces the damage
  + Several identical circles no longer hit the same target more than once
  + While its center is inside another caster's Trick: Interference Zone that wins against it, a circle does nothing (it stays and its time runs on)
  + Trick: Ice Circle now turns water into Frosted Ice, like the Frost Walker enchantment
  + The integrity cap (how far Trick: Extend SpellGram restores) is now twice the integrity at the longest Time, as for zones


- Added spell pieces for SpellGram objects
  + Selector: Own Resident SpellGrams: lists your own Resident SpellGrams in the dimension, however far away, oldest first
  + Selector: SpellGram Integrity
  + Trick: Recall SpellGram: ends your own Resident SpellGrams, however far away
  + Trick: Extend SpellGram: restores the integrity of your own Resident SpellGram
  + Trick: Circle Follow: makes your own circles follow you or a non-living entity. Without a Follow Target it lets them go, and they stay where they are. A following circle uses up extra integrity as it moves
  + Trick: Psi Bullet: fires a bullet of psions that hits a creature or SpellGram object and wears away its health or integrity
  + Trick: Gram Demolition: sends out a shockwave that wears away the integrity of the SpellGram objects in front of the caster all at once
  + Added the SpellGram Integrity Debugger (creative only), which wears away the integrity of the SpellGram object you look at


- Removed Trick: Set SpellGram Follow Target
  + Trick: Circle Follow was added as a piece that does something similar for circles


- Removed Trick: Radiation Injection


- Renamed the attribute "Spell Damage Factor" to "Phenomenon Interference" (the ID is unchanged)


- Added utility spell pieces
  + Trick: Ender Chest
  + Trick: Crafting Table
  + Trick: Harvest
  + Trick: Harvest Sequence
  + Trick: Mass Harvest
  + Trick: Shear
  + Trick: Breed
  + Operator: Connected Blocks
  + The harvest tricks harvest ripe crops and replant them; the harvest and the shearings go into the caster's inventory


- Added list and matrix operators
  + Operator: List Keep Name Matches / List Remove Name Matches: filter an Entity, Item or Block list by display name (* ? [abc] work as in Operator: List Keep Matches)
  + Operator: Matrix Set Y Axis: turns a region matrix around its center so that its Y axis points along a direction
  + Operator: Matrix Cuboid From Corners: builds an axis-aligned cuboid region matrix from two opposite corners. Block mode includes both corner blocks; Vector mode uses the two points as they are
  + Operator: List Keep By Distance / Distance-Ranked Element: sort an Entity, Vector or Block list by distance from a position, and keep the nearest elements (the farthest with a negative Number) or take the one at an index
  + Operator: List Keep In Value Range / List Keep By Value / Value-Ranked Element: filter or sort a list by a matching list of numbers (Values), such as the one from Selector: NBT Number List


- Reworked the NBT spell pieces
  + NBT paths follow the vanilla NBT path syntax ([n], [-1], [], [{...}]). An empty path is the whole NBT, keys with spaces are quoted automatically, and a wrong path counts as not found instead of an error
  + Selector: NBT Value: added String / Number modes and a Fallback input
  + Selector: NBT / NBT Keys: added an NBT Path input, to list what is inside compound tags and lists
  + Added Selector: NBT Exists and Selector: NBT Number List
  + Added Operator: List Keep NBT Matches / List Remove NBT Matches and List Keep NBT In Range / List Keep NBT Out Of Range for Entity, Item and Block lists
  + One cast reads the NBT of up to 1024 targets (changeable in the config)


- Added the Autonomous Caster (with Mekanism)
  + A machine that turns Psionic Echo Gas into Psi and casts the spells on its Spell Bullets automatically
  + Has 12 Spell Bullet slots, 36 item slots laid out like the caster's inventory, and an internal CAD with fixed stats
  + A settings window controls the bullet selection, cast interval, look direction, trigger (pulse / level), loop count and test casts (can be pinned)
  + A sight visualization tab, and an operation log that records spell errors and warnings with their position
  + 10 dedicated upgrades (Calculation Potency, Calculation Efficiency, Overclock, Multi-Step, Memory, Psyon Capacity, Psyon Conversion Speed, Psyon Conversion Efficiency, Phenomenon Interference, Interference Range). Up to 3 per machine, one of each kind
  + The Interference Range Upgrade extends the range of the spells it casts from 32 to 64 blocks (the amount can be changed in the config)
  + Keeps its internal Psi, settings and internal CAD memory when picked up
  + Only the owner and their team can cast or change settings
  + Trick: Spin Chamber and Trick: Russian Roulette switch its Spell Bullet slot. Loopcast Spell Bullets cannot be used
  + Added Selector: Autonomous Caster Position


- Added advancements
  + Added 18 advancements for Psitweaks materials and CAD assemblies to Psi's advancement tab
  + Added Japanese translations of Psi's own advancements


- Changed the look of machines and items
  + Remade the models of the Program Research Table, Sculk Eroder and Psi-Link Generator
  + Changed the working sound of the Program Research Table
  + Remade the 3D models of CADs made from Heavy Psimetal CAD Assembly α / β


- Changed the look of the Magician villager to a researcher in a white coat and an academic cap (zombie villagers too)


- Changed the Magician's Brain into the Magician's Head, which shows the Magician villager's head. Like other mob heads, it can be placed on the floor or a wall and worn, and plays a villager sound on top of a note block (Magician's Brains in existing worlds become Magician's Heads)


- The Psi-Link Generator now makes 4x the energy per Psi (100 FE) and has 4x the capacity. It outputs FE from its back side automatically by default (up to 25600 FE/t)


- Fixed the Program Research Table's power use to match what JEI and EMI show, and changed its capacity to 2,147,483,647 FE (it used only 1/2.5 of the shown amount)
  + Research recipes in JEI and EMI show non-stackable ingredients one per slot


- Adjusted program research recipes overall


- Changed existing spell pieces
  + Trick: Conjure Pulsar / Conjure Pulsar Light now have a complexity of 1, the same as Trick: Conjure Pulsar Sequence
  + Trick: Time Accelerate: casting it again now uses the new cast's multiplier and remaining time
  + Trick: FE Charge: supplies 4x the FE per Psi (80 FE)
  + Operator: Matrix Cuboid Region: added a Number mode that takes one number for all three sizes (the previous behavior is the Vector mode)
  + A pattern starting with # now matches tags, such as #minecraft:logs
  + Fixed tooltip costs and stats that did not match the actual values


- Spell warnings are now shown with their position like Psi's errors, and can be hidden with error suppression


- (QoL) Added a button to the Spell Programmer that names the spell after its trick


- (QoL) Added filter tabs and settings to the Spell Programmer's piece list
  + Tabs for bookmarks, recently used pieces (up to 50), selectors, operators, tricks and others. They work together with the search
  + Settings: list size (S / M / L), keep search text, right-click clears the search, right-click closes the list, sort by English name, show English names
  + Keyboard controls: Tab / Shift + Tab, Up / Down and PageUp / PageDown move the cursor


- (QoL) Added snippets to the Spell Programmer


- Fixed spells built only from constants crashing or freezing the server
  + Chained replacements in Operator: Format String, wildcard matching, overflowing large values and lists growing without limit
  + Added a limit on pattern-matching comparisons per cast


- Fixed items being duplicated or lost
  + Portable CAD Assembler: parts were duplicated or lost on disconnect or death
  + Gravstringer: fan-shooting Tunnelers let you pick up the side arrows too, turning one arrow into three
  + Ideaspace Storage: leftovers in the crafting grid are now dropped at your feet while disconnected or after death
  + Ideaspace Storage screen: clicking on the side button column or the crafting panel threw the held item on the ground


- Fixed spell pieces that act on blocks or others' things skipping range and protection checks
  + Trick: Block Interact reached outside the spell range and unloaded chunks, and ignored protection mods
  + Trick: Block Freeze, Block Melt and Material Mutation, and Trick: Ideaspace Storage - Deposit / Withdraw (FE mode), did not check protected areas
  + Selector: Stored Entity could get targets outside the spell range


- (1.21.1) Fixed Trick: Radiation Filter having no effect


- Fixed an error when loading spells with radiation tricks without Mekanism, and loot table load errors without Mekanism


- Fixed the Material Mutation recipe table becoming empty after pressing F3+T in singleplayer


- Fixed drops from Trick: Mass Block Break disappearing in creative mode


- Fixed Operator: Equal to compare a Block and a Vector by block position


- Fixed Operator: Matrix Cuboid Region being one block short or too long with a whole-number size, depending on the fraction of the position


- Fixed vectors losing digits when turned into strings


- Fixed amounts overflowing their slots on the Ideaspace Storage screen, and the JEI + button while the crafting grid is closed


- (QoL) Fixed multi-selection Ctrl+C / X / V acting on the spell grid while the piece list is open


- Fixed spell pieces that only read entities (Operator: List Keep Matches, Tag List, Selector: Effect List, Main-Hand Item, Carried Items, Entity Slot Item, Trick: Store Entity and others) stopping with an error when the targets included a boss or a creature riding something


- Fixed Tunneler arrows piling up without disappearing


- Fixed the outline of Trick: Show Region being invisible with pure-colored CADs such as white or red


- Fixed attack spell aiming and Macro: Face Axial Offset / Face Axial Rotation not reaching the longer range of the Interference Range Extender


- (QoL) Fixed the mouse wheel not turning pages when the piece list overlaps the cost display, and Enter placing a piece from the first page while a later page is shown


- Fixed missing translations


#### v0.10.10-hotfix

- (1.21.1) Fixed a crash when holding Shift over the item of a Sculk Eroder, Program Research Table, Material Mutator, Psi-Link Generator or Transcendent Energy Cube that has contents


- (1.21.1) Fixed a server crash when such an item burns up in lava, fire, etc.


- (1.21.1) Fixed these machines losing their items, energy, upgrades and other contents when broken


#### v0.10.10

- Updated the Ideaspace Connector
  + Added redstone control
  + Added a target stock setting for automatic output destinations
  + Added an input filter
  + Settings can now be copied and pasted through the clipboard (Ctrl+C / Ctrl+V also work)


- Added the Filter Card
  + Register up to 9 sample items and read them in spells with Selector: Filter Card
  + Works in a Curios slot, in either hand or in the inventory


- Added new spell pieces
  + Trick: Ideaspace Storage - Deposit
  + Trick: Ideaspace Storage - Withdraw
  + Trick: Ideaspace Storage - Slot Deposit
  + Trick: Ideaspace Storage - Slot Withdraw
  + Selector: Ideaspace Storage - Free Capacity
  + Selector: Ideaspace Storage - Type Count
  + Selector: Ideaspace Storage - Last Transfer Amount
  + Selector: Filter Card
  + Operator: Strict Item
  + Operator: Block Item
  + Operator: Dropped Items
  + Operator: Block Resource Amount
  + Operator: Block Free Capacity
  + Operator: Caster Basis Matrix
  + Operator: Rotation Matrix
  + Operator: Translation Matrix
  + Trick: Show Region


- Merged Ideaspace Storage spell pieces into pieces with modes (items, fluids, chemicals, FE)
  + Selector: Ideaspace Storage Energy / Item Amount / Fluid Amount / Chemical Amount -> Selector: Ideaspace Storage - Amount
  + Trick: Ideaspace Storage - FE Absorb -> Trick: Ideaspace Storage - Deposit (FE mode)
  + Trick: Ideaspace Storage - FE Supply -> Trick: Ideaspace Storage - Withdraw (FE mode)
  + Spells that use the old pieces are converted automatically when loaded


- The item filters of the Ideaspace Storage tricks now match according to the connected value
  + String: item ID with wildcards / Item: item type / Item List: any of the items / Operator: Strict Item: also compares NBT


- Changed existing spell pieces
  + Operator: Inside Region / Outside Region: added Entity, Vector and Block List modes (existing spells use Entity)
  + Operator: Matrix Transform Vector: added a mode that transforms every vector of a Vector List or Block List
  + Selector: Main-Hand Item / Carried Items now also work on dropped items
  + Item filter inputs, such as those of Trick: Pull Item and Trick: Send Item, now also accept an Item List (matches any of the items)
  + Vector List inputs, such as that of Trick: Mass Block Break, now also accept a Block List
  + Renamed Operator: List Search / List Search Exclude to Operator: List Keep Matches / List Remove Matches


- Shortened and clarified the descriptions in the guide book
  + Pages with several recipes now show the name of the recipe currently displayed


- Fixed incorrect unlock settings for Ideaspace Storage spell pieces


- Fixed multi-selection paste (QoL) allowing spell pieces that have not been researched


- Fixed spell unlock settings changed by datapacks not being applied on multiplayer clients


- Fixed datapacks with invalid spell unlock settings (spell_unlocks) removing some unlock restrictions
  + If any setting is invalid, loading is aborted and the previous settings are kept


- Fixed the /psitweaks spellunlock command
  + Now uses the latest unlock settings after a datapack reload
  + all grant / all revoke now report change counts consistent with all status
  + Players unlocked with the legacy format can now have their unlocks revoked


#### v0.10.9

- Added the Portable Spell Programmer


- Added new spell pieces
  + Trick: Ideaspace Connector
  + Selector: Ideaspace Storage Energy
  + Selector: Ideaspace Storage - Item Amount
  + Selector: Ideaspace Storage - Fluid Amount
  + Selector: Ideaspace Storage - Chemical Amount


- Changed the Tunneler to also deal damage to the Wither when its health is at or below 50%


- Empty buckets can now be stored one at a time by right-clicking in Ideaspace Storage


#### v0.10.8-hotfix

- (1.21.1) Compatible with Psi 1.21.1-110


#### v0.10.8

- Ported spell pieces from 1.12.2 Random PSIdeas
  + Trick: Conjure Pulsar
  + Trick: Conjure Pulsar Sequence
  + Trick: Conjure Pulsar Light


- (1.21.1) Changed Trick: Antipersonnel Explode so that experience orbs and dropped items are not destroyed


- Added the new Ideaspace Storage system (WIP)
  + A virtual storage feature with a UI similar to AE2 and Mekanism QIO


- Fixed an item duplication bug when the count input of Trick: Send Item was omitted

#### v0.10.7-hotfix

- (1.20.1) Fixed a crash caused by a mixin conflict with NuclearCraft: Neoteric


#### v0.10.7

- Added CAD batteries
  + Quantum CAD Battery
  + Reactive CAD Battery


- Added spell pieces
  + Trick: Send Item
  + Trick: Pull Item


- (1.21.1) Changed the `material_mutator` recipe directory to avoid conflicts with Potions Master (#3)


- Added a JEI description for the Graviton Factor


- Moved client-only features to Psitweaks: QoL
  + As a result, spell-piece bookmarks are reset once
  + Psitweaks: QoL is included in Psi: Tweaks and Additions, so users do not need to install it separately.

#### v0.10.6

- Made the attack damage of the Gravstringer and Tunneler configurable


- Added spell pieces
  + Macro: Face Axial Offset
  + Macro: Face Axial Rotation


- Fixed an issue where the `minecraft:mineable/pickaxe` tag was broken when Mekanism was not installed (#2)


- Fixed an issue where the mining tag for the Transcendent Energy Cube was not set


- Changed the mining tier of the Psycheonic Metal Crux to Diamond


- Changed the crafting recipes for the Ultraclocked CAD Core and Absorptive CAD Core

#### v0.10.5

- Added Russian translation (#1)


- Ported spell pieces from 1.12.2 Random PSIdeas
  + Macro: Caster Raycast
  + Macro: Strong Caster Raycast
  + Macro: Weak Caster Raycast
  + Macro: Axis Caster Raycast
  + Macro: Strong Axis Caster Raycast
  + Macro: Weak Axis Caster Raycast

- Added spell pieces
  + Macro: Caster Axial Offset
  + Macro: Caster Axial Offset (3D)
  + Macro: Caster Axial Rotation
  + Macro: Caster Axial Rotation (3D)
  + Operator: Alive


- Added CAD cores
  + Absorptive CAD Core
  + Ultraclocked CAD Core

- Added material items (including items that currently have no use)
  + Flash Coil
  + Flash Chip
  + Hardened Capsule
  + Nano Reactor
  + Unfathomable Component
  + Quantized Plate
  + Gravitized Plate
  + String?
  + Superstring
  + Photon Glitter
  + Wave Fragment
  + Quantum Factor
  + Graviton Factor
  + Quantized Arrowhead

- Added weapons
  + Gravstringer
    * A significantly enhanced version of the Psimetal Bow.
  + Tunneler
    * An arrow exclusive to the Gravstringer. It can pierce blocks and mobs, and ignores armor and invulnerability frames.

- Fixed an issue where the output quantity of the Chaotic Psimetal crafting recipe was too low when Mekanism was not installed


- Fixed an issue where casting a Projectile Spell Bullet with the Psimetal Bow did not cause it to fire correctly


- (1.21.1) Fixed an issue where the Auto Caster played a sound every time it cast a spell



#### v0.10.4

- (1.21.1) Changed the behavior of Trick: Flight
  + It now continuously consumes Psi during creative flight in exchange for a lower activation cost


- Adjusted the Program: Flight recipe


- Added spell pieces
  + Selector: Effect List
  + Trick: Dry Meteor


- Added the Carbon Poisoning status effect
  + It is applied by attacks from Trick: Dry Meteor


- Adjusted Trick: Aqua Cutter
  + Improved its appearance
  + Changed its damage type from magic to physical (`minecraft:thrown`)


- Added a spell-piece bookmarking feature


- Adjusted Psionic Utilities integration
  + Fixed an issue where moving Psionic Utilities pieces with Ctrl + drag was unavailable because its controls conflicted with Psitweaks' functionality
  + Prevented Psionic Utilities' drag-based connector creation from being triggered over piece-selection windows and similar screens
  + Made drag-based connector creation and piece movement in Psionic Utilities undoable with Ctrl + Z


#### v0.10.3

- Added spell pieces
  + Safety
  + Trick: Flexible Jump
  + Trick: Flexible Switch


- Adjusted the power consumption of the Program Research Table
  + Changed it to use power consumption per tick instead of total power consumption as the basis


- Made the research recipes for Trick: Flexible Die, Trick: Cocytus, and Trick: Meteor Line more difficult


- Added material items
  + Psimetal Nugget
  + Ivory Psimetal Nugget
  + Ebony Psimetal Nugget
  + Chaotic Psimetal Nugget
  + Flashmetal Nugget
  + Heavy Psimetal Nugget
  + Antinite Nugget
  + Psycheonic Metal Block
  + Hypostasis Gem Block


- Added integrations with Mystical Agriculture, Mystical Agradditions, and Productive Bees
  + Enabled crafting of Psi and PsiTweaks materials


- Improved Psi refund handling for Trick: Flexible Die


#### v0.10.2

- (1.20.1) Fixed an issue where capacity changes for gas-burning generators were not being applied correctly


- (1.21.1) Fixed an issue where ΨE-O fuel was not functioning correctly


#### v0.10.1

- Fixed an issue where connectors could be pulled by dragging while a Psitweaks text input window or similar screen was open when using Psionic Utilities


- Added the ability to select multiple spell pieces with Shift + drag or Ctrl + click in the Spell Programmer screen, then copy, cut, and paste them


- Changed the mod so that it can launch even when Mekanism and Mekanism Generators are not installed
  + Without Mekanism, material recipes are simplified, and machine blocks that depend on Mekanism are not added.
  + This change is primarily intended for modpack developers; installing Mekanism is strongly recommended for normal play.


- Adjusted Material Mutation processing recipes


- Made the Third-Eye Device crafting recipe more difficult


- Added the Interference Range Extender
  + A lower-tier device than the Third-Eye Device. Extends spell range and raycast range up to 64 blocks.


- Added config options to control whether Psi consumption when taking damage and the Psi regeneration cooldown after consuming Psi are disabled


- Implemented an in-game configuration screen


- Hardened Psi-Link Generator setting synchronization so only the valid, currently open owner menu can update the machine


- (1.20.1) Changed the specifications of the Program Research Table to the Mekanism base (same design as 1.21.1)


- (1.20.1) Made the Transcendent Spell Bullet crafting recipe more difficult (same as 1.21.1)


#### v0.10.0

- Added value types available in spell programming
  + Item
  + Block
    * Contains Vector information and can be auto-boxed to and unboxed from Vector
  + String
  + Matrix


- Defined Contextual Value
  * A collective term for complex in-game values such as Entity, Item, and Block.


- Defined Plain Value
  * A collective term for simple values such as String, Number, and Vector.


- Added List support for types other than Entity


- CAD memory can now store Plain Values
  * In addition to Vectors that Psi itself can read and write, String, Number, and Matrix values can now be stored.


- Added spell pieces
  * Constant: String

  * Operator: Block Position
  * Operator: Block State
  * Operator: Block State Entries
  * Operator: Block State Value
  * Operator: Equal
  * Operator: Partial Match
  * Operator: From String List
  * Operator: From String
  * Operator: To String List
  * Operator: To String
  * Operator: String Concatenation
  * Operator: String Trim
  * Operator: String Slice
  * Operator: String Length
  * Operator: String Replace
  * Operator: Format String
  * Operator: Starts With
  * Operator: Ends With
  * Operator: String List Join
  * Operator: String Split
  * Operator: Registry ID
  * Operator: Registry ID List
  * Operator: Greater Than
  * Operator: Greater Than or Equal
  * Operator: Item Count
  * Operator: Item Total Count
  * Operator: Add to List (PT)
  * Operator: Remove from List (PT)
  * Operator: List Concatenation (PT)
  * Operator: List Exclusion (PT)
  * Operator: List Intersection (PT)
  * Operator: List Search
  * Operator: List Search Exclude
  * Operator: List Insert
  * Operator: Remove Indices
  * Operator: List Size
  * Operator: Random Element
  * Operator: Player Name
  * Operator: Tag List
  * Operator: Block State
  * Operator: Block State Entries
  * Operator: Block State Value
  * Operator: Number List to Vector
  * Operator: Vector to Number List
  * Operator: Matrix Transform Vector
  * Operator: Matrix Linear Part
  * Operator: Matrix Replace Element
  * Operator: Matrix Delete Row
  * Operator: Matrix Delete Column
  * Operator: Matrix Cuboid Region
  * Operator: Region Vector List
  * Operator: Block Position List
  * Operator: Matrix Add
  * Operator: Matrix Subtract
  * Operator: Matrix Multiply
  * Operator: Matrix Scalar Multiply
  * Operator: Matrix Transpose
  * Operator: Matrix Determinant
  * Operator: Matrix Inverse
  * Operator: Matrix Extract Row
  * Operator: Matrix Extract Column
  * Operator: Matrix Element
  * Operator: Matrix Row Count
  * Operator: Matrix Column Count
  * Operator: Matrix Multiply Vector
  * Operator: Column Matrix From List
  * Operator: Matrix Flatten
  * Operator: Identity Matrix
  * Operator: Zero Matrix
  * Operator: Diagonal Matrix
  * Operator: Matrix Replace Column
  * Operator: Matrix Replace Row
  * Operator: Outside Region
  * Operator: Inside Region

  * Selector: Block
  * Selector: Display Name
  * Selector: Display Name List
  * Selector: Held Item
  * Selector: Held Items
  * Selector: Internal Items
  * Selector: Internal Slot Item
  * Selector: Selected Slot Item
  * Selector: Entity Slot Item
  * Selector: Indexed Element (PT)
  * Selector: NBT
  * Selector: NBT Keys
  * Selector: NBT Value
  * Selector: Online Players
  * Selector: Stored Value
  * Selector: Block List

  * Trick: Store Value
  * Trick: Switch
  * Trick: Jump
  * Trick: Mass Block Break
  * Jump Anchor


- Fixed bugs in the block breaking process for Trick: Block Break (Fortune) and Trick: Block Break (Silk Touch)

#### v0.9.5

- Added spell pieces
  * Trick: Meteor Line
  * Trick: Block Melt
  * Trick: Block Freeze
  * Operator: Tangent
  * Operator: Arc Tangent
  * Operator: Hyperbolic Sine
  * Operator: Hyperbolic Cosine
  * Operator: Hyperbolic Tangent


- Fixed Molecular Divider's hit detection not behaving as intended


- Made the Program: Cocytus recipe harder


- Changed the Material Mutator so Gas Upgrades can no longer be installed



- (1.20.1) Changed Magician villager trades to match 1.21.1


- (1.20.1) Fixed unintended Psi refund amounts when combining High-efficiency Computation with Trick: Flexible Die


- (1.21.1) Fixed the Material Mutator's Psionic Echo consumption amount


- (1.21.1) Fixed Program Research Station research progress resetting unexpectedly


- (1.21.1) Fixed the Sculk Eroder crafting recipe


- (1.21.1) Added Create Aeronautics (Sable) compat features
    * Simulated Contraptions are no longer treated as out of range when using spells that refer to block positions
    * Added Trick: Physical Propulsion




#### v0.9.4

- Added Psi-Link Generator
  * An early-game generator that produces power while consuming the owner's remaining Psi
  * Generates 25 FE/Psi


- Added Transcendent Universal Cable and Transcendent Energy Cube
  * 128x the performance of the Ultimate tier


- Added PsiTweaks documentation to Encyclopaedia Psionica



#### v0.9.3

- Added attributes: Spell Damage Multiplier, Psi Regeneration Rate, and Psi Capacity


- Added Spell Damage Multiplier, Psi Regeneration Rate, and Psi Capacity bonuses to the M.O.V.A.L Suit and Psi's psimetal exosuit armor


- Added Spell Magazine
  * Stores up to 12 spell bullets, and can swap its stored spell bullet with the one in a CAD by right-clicking while holding a CAD
  * Inspired by the item of the same name from 1.12.2 Random PSIdeas (by kamefrede123)


- Added Sorcery Booster
  * Increases Spell Damage Multiplier by 30% when equipped in the Magic Calculation Area


- Added Inline Caster
  * Has a single internal socket slot and can store a Spell Bullet via the CAD Assembler and similar methods
  * Casts the currently selected spell bullet when right-clicked while holding a CAD
  * Treated as casting through the CAD being carried
  * Inspired by the item of the same name from 1.12.2 Random PSIdeas (by kamefrede123)


- Added Secondary Caster and Parallel Caster
  * 5-slot and 11-slot variants of the Inline Caster

#### v0.9.2

- Fixed Recipe Error

#### v0.9.1

- Antinite and Psycheonic Metal can now be melted in Tinkers' Construct


- Added Trick: Fire Circle
  * Places a fire spell object that deals fire damage to entities inside it


- Added Selector: Nearby SpellGram Object


- Added Trick: Set SpellGram Follow Target
  * Sets a spellgram object to follow a specified entity


- Added Trick: Ice Circle
  * Places an ice spell object that deals freezing damage to entities inside it


- Added Trick: Cure Radiation


- On multiplayer servers, Psitweaks offensive spells no longer hit players when pvp=false in server.properties


- Changed Trick: Radiation Injection to scale exposure amount exponentially based on power


#### v0.9.0

- Added Trick: Material Mutation
  * Breaks specific blocks and transmutes them into different items


- Added Trick: Radiation Filter
  * Protects the target from radiation exposure


- Added Material Mutator
  * A machine that performs Trick: Material Mutation processing using power and Psionic Echo Gas


- Added Polonium Block and Plutonium Block


- Added intermediate materials
  * Americium Pellet
  * Neptunium Pellet
  * Hypostasis Gem
  * Enriched Hypostasis Gem
  * Hypostasis Alloy
  * Hypostasis Control Circuit
  * Psycheonic Metal Nugget
  * Psycheonic Metal Ingot
  * Jade
  * Magatama
  * Spellmachinery Casing


- Added Philosopher's Stone
  * Enables item-transmutation-style crafting inspired by ProjectE (similar concept, different implementation)


- Added Psycheonic Metal CAD Assembly


#### v0.8.3

- Added Antinite Ore
  * Rarely generates outside the End main island; currently has no use


- Added Chaotic Psimetal Block, Flashmetal Block, and Heavy Psimetal Block


- Spell pieces can now be searched in English during spell programming regardless of the current language setting


#### v0.8.2

- Program items can now be duplicated by crafting them with a Blank Program


- Added Trick: Flexible Die
  * In addition to Psi's Trick: Die behavior, refunds Psi cost for unexecuted spell pieces
  * Known minor issue: when used in spells that run every tick, Psi display can become desynced


- Added Auto Caster: custom tick
  * Cast interval can be configured in ticks


- Increased Auto Caster socket count to 7

#### v0.8.1

- Added TiC compat molten psimetals, tools, modifiers
  * some modifiers are inspired by PlusTiC


- Backported Mekanism QIO recipes from 1.21.1


#### v0.8.0 

- Added Program Research Station
    * Unlock new spell pieces by spending items, power, and time
    * Existing powerful spells from this mod now also require unlocking
    * Whether research is required can be changed in the config


- Added Trick: Blaze Ball
    * Launches a fireball forward to attack.


- Added Trick: Radiation Injection
    * Exposes the target to radiation


- Added Trick: Guillotine
    * Deals damage and drops the target's head on kill


- Added Trick: Active Air Mine
    * Creates a spherical shockwave at the specified coordinates to attack


- Added tags to items


- Reduced cast cost of Molecular Divider


- Added "Spellcaster" villager job


- Added Portable CAD Assembler


- Fixed an issue when using PsiEX:TiCCAD with CADDisassembler
