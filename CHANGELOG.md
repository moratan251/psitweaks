## Changelog

#### v1.0.0 (plan)

> Work in progress. Numbers and behavior may change before release.

##### Added
- Added attack spell pieces
  + Trick: Chain Lightning: fires lightning from the focal point that strikes the nearest enemy and chains to nearby enemies
  + Trick: Mine Genesis: sends out a ground shockwave that launches creatures upward
  + Trick: Burst: vaporizes liquid inside the target so it bursts from within
  + Trick: Air Bullet: fires a bullet of compressed air
  + Trick: Dancing Blades: deploys swords from your Blade Pouch to slash nearby enemies
  + Trick: Pseudo Meteor: calls pseudo meteors down around the target position
  + Trick: Micro Black Hole: creates a tiny black hole that pulls creatures in and swings them around
- Added the Blade Pouch: holds up to 8 swords and works in any Curios slot. Trick: Dancing Blades applies each sword's damage, enchantments, and on-hit effects without consuming durability
- Added Zone Magic (Resident SpellGrams active within an area; unlocked by research)
  + Added 11 zone tricks: Soak Zone, Heat Zone, Freeze Zone, Turbulence Zone, Gravity Zone, Interference Zone, Silence Zone, Charged Zone, Acceleration Zone, Deceleration Zone (applies the new Torpor effect), and Contamination Zone (with Mekanism)
  + Controlled by a region matrix, Size, Time, and (for zones that take it) Power; shared resources such as heat, cooling, FE, and wind scale with Cost
  + Added overlap rules: your own matching zones combine resource totals and apply the strongest effect, your own Heat and Freeze Zones cancel each other out, and competing casters' zones defer to the highest Phenomenon Interference
  + Zones periodically alter blocks at random and show integrity and usage when aiming at the core (floor magic circle resolution and particle limits can be adjusted in the client config)
  + Added the Zone Feeder (feeds stored FE into a Charged Zone to cover demand beyond its own capacity) and Zone Receiver (charges FE items and equipment in your inventory while inside a Charged Zone)
- Added Trick: Decontamination (with Mekanism; unlocked by research): places a Resident SpellGram core that weakens radiation sources within 32 blocks based on Cost
- Added spell pieces and a debug item for SpellGram objects
  + Selector: Own Resident SpellGrams, Selector: SpellGram Integrity, Selector: SpellGram Integrity List
  + Trick: Recall SpellGram, Trick: Extend SpellGram, Trick: Circle Follow, Trick: Psi Bullet, Trick: Gram Demolition
  + SpellGram Integrity Debugger (creative only)
- Added utility spell pieces
  + Trick: Ender Chest, Trick: Crafting Table, Trick: Harvest, Trick: Harvest Sequence, Trick: Mass Harvest, Trick: Shear, Trick: Breed, and Operator: Connected Blocks (harvested and sheared drops go straight into the caster's inventory)
- Added list and matrix operators
  + Operator: List Keep Name Matches, List Remove Name Matches, Matrix Set Y Axis, Matrix Cuboid From Corners, List Keep By Distance, Distance-Ranked Element, List Keep In Value Range, List Keep By Value, and Value-Ranked Element
- Added the Autonomous Caster (with Mekanism)
  + Converts Psionic Echo Gas into Psi to automatically cast spells from up to 12 Spell Bullets, with 36 item slots and a fixed internal CAD (Loopcast Spell Bullets are not supported)
  + Includes a pinnable settings window, a sight visualization tab, and an operation log for spell errors and warnings; retains internal Psi, settings, and CAD memory when picked up
  + Added 10 dedicated upgrades (up to 3 per machine, 1 of each kind; Interference Range extends spell range)
  + Restricted to the owner and their team; also added Selector: Autonomous Caster Position
- Added advancements
  + Added 18 advancements for Psitweaks materials and CAD assemblies to Psi's advancement tab
  + Added Japanese translations for Psi's own advancements

##### Changed
- Unified attack spell hit rules
  + Attack spells, beams, spell circles, and projectiles no longer hit the caster, pets, or same-team allies (regardless of friendly fire), nor players when PvP is disabled; Blaze Ball, Dry Meteor, and Aqua Cutter projectiles pass through allies
  + Attack spells (including Trick: Guillotine) now work on bosses such as the Wither and Ender Dragon, as well as riding or sleeping creatures
- Reorganized SpellGram objects
  + Unified Resident SpellGrams (Fire Circle, Ice Circle, and zones), the Meteor Line night, and Micro Black Hole as SpellGram objects
  + Added integrity determined by Phenomenon Interference when placed (Resident SpellGrams also decay over time; aiming at the core shows integrity and the object disappears when depleted)
  + Immune to ordinary attacks and Psi movement tricks (except your own circles), not saved with chunks, and removed when the caster logs out or changes dimension
  + Limited Resident SpellGrams to 32 per player or Autonomous Caster, removing the oldest when exceeded (configurable)
- Remade and adjusted existing spell pieces
  + Trick: Barrier: remade as a 1-minute damage-absorbing shield scaled by Phenomenon Interference (removed the Time input; existing spells still load). Added a configurable HUD indicator and Selector: Barrier Amount
  + Trick: Parade: remade from chance-based dodging into a decoy spell that draws nearby mobs, blocks direct targeting by other players' spells, and redirects Operator: Entity Position; now unlocked by research
  + Trick: Meteor Line: rebuilt to cover a 16–32 block radius in a sphere of night and rain streaks of light even under roofs (Potency 1428 → 1400, Cost 12000 → 12500)
  + Trick: Cocytus: removed the target input (existing spells still load) and rebuilt to blow a blizzard forward that permanently freezes caught mobs in ice without dealing damage (sparing players, pets, allies, and bosses; Cost 5000 → 10000)
  + Trick: Fire Circle / Ice Circle: added a Time input (1–120 s), scaled damage with Cost, applied armor reduction, prevented multi-hits from identical circles, and allowed opposing Interference Zones to suppress them. Switched to dedicated damage types (blocked by Barrier, credited to the caster); Ice Circle no longer builds up freezing indefinitely and now turns water into Frosted Ice
  + Trick: Aqua Cutter: updated with crescent water blade visuals and hit splashes; wets targets for 10 s (preventing fire), or extinguishes burning targets for 1.25x damage instead
  + Trick: Active Air Mine: updated with spherical shockwave visuals and a dedicated damage type
  + Reworked NBT spell pieces: adopted vanilla NBT path syntax (invalid paths return not found instead of an error), added String/Number modes and Fallback to Selector: NBT Value, added NBT Path input to Selector: NBT / NBT Keys, and added Selector: NBT Exists, Selector: NBT Number List, and 4 NBT list-filtering operators
  + Other spell adjustments: lowered Trick: Conjure Pulsar / Conjure Pulsar Light complexity to 1, updated Trick: Time Accelerate re-casts to adopt the new multiplier and duration, quadrupled Trick: FE Charge output (80 FE/Psi), added a single-number mode to Operator: Matrix Cuboid Region, supported `#` tag matching in patterns, and fixed mismatched tooltip stats and costs
- Updated attributes and spell warnings
  + Renamed the attribute "Spell Damage Factor" to "Phenomenon Interference" (ID unchanged)
  + Spell warnings now display with their grid position like Psi errors and respect error suppression
- Updated machines, recipes, and visuals
  + Psi-Link Generator: quadrupled generation (100 FE/Psi) and capacity, and enabled automatic FE output from the back by default (up to 25600 FE/t)
  + Program Research Table: aligned power consumption with JEI/EMI displays, greatly increased energy capacity, displayed non-stackable research ingredients one per slot in JEI/EMI, and rebalanced research recipes overall
  + Models and audio: remade models for the Program Research Table, Sculk Eroder, Psi-Link Generator, and Heavy Psimetal CAD Assembly α/β CADs; updated the Program Research Table's working sound
  + Magician villager and Magician's Head: gave Magician villagers (including zombie villagers) a white-coat researcher look, and replaced Magician's Brain with the placeable, wearable, note-block-compatible Magician's Head (existing items convert automatically)

##### Removed
- Removed Trick: Set SpellGram Follow Target (replaced for circles by Trick: Circle Follow)
- Removed Trick: Radiation Injection

##### QoL
- Added a Spell Programmer button to name spells after their trick, along with snippet support
- Added piece-list filter tabs (bookmarks, recent, categories), display/search settings, and keyboard navigation
- Fixed multi-selection Ctrl+C / X / V acting on the spell grid while the piece list is open
- Fixed mouse-wheel scrolling failing when the piece list overlaps the cost display, and Enter placing a piece from page 1 while viewing a later page

##### Fixed
- Fixed server crashes and freezes from constant-only spells (chained Format String replacements, wildcard matching, value overflows, unbounded list growth) and added a per-cast pattern comparison limit
- Fixed item duplication and loss issues (Portable CAD Assembler on disconnect/death, Gravstringer fan-shooting Tunnelers, Ideaspace Storage crafting grid leftovers on disconnect/death, and clicking on the Ideaspace Storage side button column or crafting panel)
- Fixed missing range and protection checks on block/entity spells (Trick: Block Interact, Block Freeze, Block Melt, Material Mutation, Ideaspace Storage - Deposit / Withdraw in FE mode, and Selector: Stored Entity)
- Fixed read-only entity spell pieces failing with an error when targets include bosses or riding creatures
- (1.21.1) Fixed Trick: Radiation Filter having no effect
- Fixed spell and loot table load errors involving radiation tricks when Mekanism is not installed
- Fixed the Material Mutation recipe table becoming empty after pressing F3+T in singleplayer
- Fixed Trick: Mass Block Break drops disappearing in creative mode
- Fixed Operator: Equal to compare Blocks and Vectors by block position
- Fixed Operator: Matrix Cuboid Region being off by one block for whole-number sizes depending on fractional position
- Fixed vectors losing precision when converted to strings
- Fixed Ideaspace Storage slot counts overflowing and the JEI `+` button failing while the crafting grid is closed
- Fixed Tunneler arrows piling up without despawning
- Fixed Trick: Show Region outlines being invisible with pure-colored CADs such as white or red
- Fixed attack spell aiming and Macro: Face Axial Offset / Face Axial Rotation not reaching the extended range of the Interference Range Extender
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
