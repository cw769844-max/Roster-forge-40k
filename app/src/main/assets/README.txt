Drop the official BSData wh40k-10e release ZIP here.

Filename: wh40k-10e.zip
Source:   https://github.com/BSData/wh40k-10e/releases (download the
          latest *.zip asset, e.g. wh40k-10e-v15.0.0.zip, and rename it
          to exactly "wh40k-10e.zip").

On first launch the app will:
  1. Open this file from assets.
  2. Run BsXmlParser.parseRelease over its .gst + .cat entries.
  3. Insert the parsed factions, units, detachments, enhancements and
     stratagems into Room.

If the file is missing or parsing fails, the app silently falls back to
the hand-crafted SampleCatalogueSeed (Space Marines + Necrons only).

Licence: BSData wh40k-10e is published under CC-BY. Bundling it here
preserves attribution; the README in the BSData repository explains the
attribution requirements.
