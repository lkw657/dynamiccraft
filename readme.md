# Dynamic Craft

Minecraft bukkit plugin to reimplement some craftbook mechanisms with animations isntead of block de/spawning.
Currently have gates

## Building
Copy cache/patched_1.16.1.jar from an installed paper server into libs/
run `gradle build`

## Usage
Use the resource pack

1. Create a rectangle of iron bars
2. Place a sign nearby with {gate} on the second ine
3. right click sign to open/close
4. **destroy the sign before destroying the gate**

## Permiossions
- dynamiccraft.gate
    : create, destroy gates
- dynamiccraft.gate.use 
    : open, close gates

## TODO
- Config
- Speed adjustment
    - Need to properly handle speeds that don't evenly fit into 1
    - Speeds > 0.5?
- How to handle weirdly shaped gates or gates too close to each other?
- What happens if chunk unloads while gate is moving?
- Check all chunks the gate overlaps with have loaded before moving
- Have barrier partially open while gate is moving
- Fix iron bar models when gate is destroyed 
- Invisible armour stands don't seem to be destroyable - make a tool to turn them visable again
- Check error handling
- I think the gate blocks overlap slightly - should check the model size
- Compile for bukkit, spigot instead of just paper