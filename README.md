# DMOTE Beam

3D models for an accessory to the
[DMOTE keyboard](http://viktor.eikman.se/article/the-dmote/): Plastic parts
holding a simple “back beam”, typically just an unthreaded hollow metal rod
about half an inch wide. Such a beam, sold separately by your local hardware
store, stabilizes the split keyboard and carries the wire that connects its two
halves electronically.

DMOTE Beam is just an example project. You may not need a back beam of any
kind. Feel free to accessorize however you like, if at all.

## Usage

Use `lein run` with command-line arguments to generate files of OpenSCAD code
under `output/scad` and, optionally, STL files for slicing and 3D printing.

## Showcase

Your renders go into the `output/stl` folder. Some samples are included with
the project, though they are not produced from the latest source code.

### `dmote-beam-clip`

The clip is used with the `rear-housing` feature on the DMOTE. On one side it
holds the beam, and on the other side, it’s got a hole for a threaded fastener
that closes the clip around the beam and simultaneously connects it to the
keyboard case through a nut you can put inside the rear housing. This way, you
can run a connecting wire all the way through a beam, which is appropriate if
you’ve got sockets for the wire on the far sides of the case.

[![Image of DMOTE v0.2.0](http://viktor.eikman.se/image/dmote-2-top-down-view/display)](http://viktor.eikman.se/article/the-dmote/)

You would typically print four copies of the clip, possibly in pairs with
different lengths, for each keyboard you build.

The bundled sample was made for a 15 mm square-profile beam with M6 socket-cap
fasteners. It was used for the master build of DMOTE version 0.2.0.

### `dmote-beam-backplate-anchor` and `-funicular`

The anchor and funicular models are not intended for use with a rear housing.
Instead, they’re for a back plate, an alternative feature of the DMOTE.
Another point of contrast with clips is that anchors and funiculars work best
when your wire sockets face each other. Each anchor connects directly to a back
plate on one half of the keyboard while a pair of funiculars hang between the
halves, carrying a wire or a secondary beam that contains a wire.

[![Image of DMOTE v0.1.0](http://viktor.eikman.se/image/dmote-1-glamour-shot/display)](http://viktor.eikman.se/article/the-dmote/)

An anchor does not tighten around the beam the way a clip does. The idea
is to run a threaded rod all the way through and put a lock nut on the
outsides. In this configuration, the main beam does not enter the anchors, nor
even the funiculars; it’s cut into three parts and merely maintains
horizontal spacing while the rod inside provides more stability.

The bundled samples were made for an 8 mm round-profile beam with an M6
threaded rod inside and M6 countersunk screws going into the case. They were
used for the master build of DMOTE version 0.1.0.

## License

Copyright © 2018–2019 Viktor Eikman

This software is distributed under the [Eclipse Public License](LICENSE-EPL)
(EPL) v2.0 or any later version thereof. This software may also be made
available under the [GNU General Public License](LICENSE-GPL) (GPL), v3.0 or
any later version thereof, as a secondary license hereby granted under the
terms of the EPL.
