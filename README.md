# SNBT Escape Test

## This is for demo purposes only, do not use this in a real world

This 25w03a fabric mod is a simple proof of concept that explores the possibility of allowing certain escape sequences in SNBT. This would be especially useful now that text components are read as SNBT instead of JSON within a string.

Valid escapes include:
+ `\n` newline
+ `\t` tab
+ `\r` carriage return
+ `\uXXXX` unicode

See the following bugs for more info:
+ https://bugs.mojang.com/browse/MC-279229 "SNBT text components prevent \n and \t from working"
+ https://bugs.mojang.com/browse/MC-279250 "SNBT text components prevent unicode escapes from working"

Notes:
+ This has only been tested with `/tellraw` and the `item_name` component at this point
+ Currently, fetching nbt with `/data get` will use the real characters and not their escape sequences like `\n`
