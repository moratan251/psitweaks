## Changelog

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
