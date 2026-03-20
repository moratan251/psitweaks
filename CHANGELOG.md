## Changelog

#### v0.9.3

- Added attributes: Spell Damage Multiplier, Psi Regeneration Rate, and Psi Capacity


- Added Spell Damage Multiplier, Psi Regeneration Rate, and Psi Capacity bonuses to the M.O.V.A.L Suit and Psi's psimetal exosuit armor


- Added Spell Magazine
  * Stores up to 12 spell bullets, and can swap its stored spell bullet with the one in a CAD by right-clicking while holding a CAD
  * Inspired by the item of the same name from 1.12.2 Random PSIdeas (by kamefrede123)


- Added Sorcery Booster
  * Increases Spell Damage Multiplier by 30% when equipped in the socket circle


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
