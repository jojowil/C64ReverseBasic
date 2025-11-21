# C64ReverseBasic

This is an attempt at a BASIC "decompiler". It's not a very complex one as it's
intended to reproduce a BASIC programs suitable to run in the online [JS-based
C64 emulator](https://programmingby.design/c64-emu/) or in [VICE](https://vice-emu.sourceforge.io/).

Running programs is a simple as dragging the .prg file onto the emulator and typing RUN
as needed (VICE tends to autorun). This program endeavors to take the running program
and provide the original source code text of the program.

## Concept

This is renewal of the joy I had using Commodore computers in the past.

## Implementation

This is done in Java and C, so far, to demonstrate languages to my students.

Everything in this design is Java 21 and C23 compatible.

## The Language

This project is based on the [Commodore 64 V2.0 BASIC](https://www.c64-wiki.com/wiki/BASIC).
There are [tons of examples, forums and further documentation](https://www.lemon64.com/) all over the Internet.
