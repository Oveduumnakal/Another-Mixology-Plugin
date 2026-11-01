# Changelog

Another Mixology Plugin releases are numbered `X.Y` (milestone `Release X.Y`, tag `R-X.Y`). Entries below 1.0 are
the history it inherits: `V<upstream>-extX.Y.Z` from Mastering Mixology Extended, the rest from Mastering Mixology.

## 1.0
First release as Another Mixology Plugin, built from Mastering Mixology Extended V1.9.1-ext1.1.0. Changes from that
version:
* **New name and settings**: the plugin has its own settings group, so settings from Mastering Mixology or
  Mastering Mixology Extended do not carry over. It can be installed alongside them, but turn one off so their
  highlights do not overlap.
* **Recommendation strategy** setting (top of the plugin settings). Choose how orders are coloured brew or
  skip: *Expected value* (new, default), *Bottleneck*, *Adaptive* (the previous policy) or *Greedy* (brew all 3).
  Expected value counts time, not just potions: each delivery costs 0.7 potions for the walk to the conveyor belt,
  and a Retort potion takes longer than an Alembic or Agitator one (timings measured over 40 order sets). It weights
  each colour by the cube of its relative deficit, so the colours finish together with little wasted resin. In
  simulation it reaches the Reagent Pouch about 15 minutes sooner than counting potions alone (5.8 h vs 6.1 h).
* **Cap Concentrate clicks** now blocks only on the last tick before the Retort finishes. It used to block once a
  click would finish the potion, which cut off clicks that still saved a tick.
* **Retort quick-action colour**: while Concentrate clicks still help, the Retort is drawn in the quick-action colour,
  like the Agitator and Alembic; it returns to the station colour on the last tick.
* **Reward tracking**: the potion packs, Aldarium and the Chugging Barrel use a single quantity setting, where 0 means
  not tracked, instead of a tick box plus a quantity.
* **Colors** section (was Accessibility), above Highlights. It holds *Use Letters* plus the paste, skip, station,
  quick-action and digweed colours.
  * Recipes are coloured blocks by default; *Use Letters* (off by default) shows M/A/L letters instead. It replaces
    *Dyslexic/Colorblind Mode*.
  * The paste colours apply everywhere, with new defaults: Mox `#3984FF`, Aga `#6BFF76`, Lye `#FF0000`; skip `#4A4A4A`.
  * Order names in the game's order list are coloured by paste, like the orders overlay.
  * *Colorblind mode* (Off, Protanopia, Deuteranopia, Tritanopia): preset paste colours chosen by simulating each
    colour blindness.
* **Recommendations are always on** once a reward is tracked; pick *Greedy* to skip nothing. The Recommended-Potion
  Highlight section and the recommend colours are removed.
* **Highlights** section now holds the highlight toggles, without the "Highlight" prefix: *Levers*, *Next lever
  only*, *Mixing vessel*, *Conveyor belt*, *Stations*, *Quick-action events* and *DigWeed*.
* **Resin bars always show** in the goal overlay while a reward is tracked; the *Show Resin Bars* setting is
  removed.
* **Hide orders panel** option (on by default): replaces the game's potion orders list with a plugin overlay.
* **New defaults**: *Inventory Potion Tags* Colored, *Order sorting* Shortest Path, *Display resin amount*,
  *Next lever only* and *Wait for full order* on.
* **Mixing guidance** (new, all toggles in the Highlights and Misclick protection sections). Walks you through the
  mixing half of the loop: levers, then vessel, then stations, then conveyor.
  * *Next lever only* — highlights just the lever for the next paste instead of all three.
  * *Lever click protection* — only the next lever can be pulled. If the vessel holds pastes that match no order,
    every lever stays pullable so you cannot get stuck. A fast double click cannot let a second paste through.
  * *Potion protection* — blocks mixing at the vessel when its three pastes do not make a potion for an open order.
  * *Mixing vessel and conveyor belt highlights* — green when they are the next thing to click.
  * *Wait for full order* — no station highlight until every order you are making has been mixed.
  * Skipped orders draw their recipe letters or blocks in the skip colour.
* **Digweed target** (Highlights, on by default): while you hold a digweed, it and the potion to add it to are
  outlined in the inventory and a digweed icon flashes on that order; once added, the icon stays and a small digweed
  marks the potion. Expected value counts the digweed (and potions it already improved) as
  doubled resin, batch bonus included, so an order worth brewing only with the digweed gets recommended.
* **Rewards panel**: a row per tracked reward with its estimated time to finish (`08h 53m 09s`, or `Ready`) and
  progress, an *All Rewards* row and the resin bars, split by faint lines. The estimate follows your measured pace
  (after 10 order sets) and the chosen strategy; in simulation it lands within 2 % of the average. New main
  settings: *Reward Progress* (Percent, Number or None in the middle of each bar) and *Reward Order* (bought top to
  bottom by default, or each reward on its own). Rewards are listed in the order you tick them, and
  right-clicking the panel moves one up or down. Bar numbers switch between light and dark text by the colour behind them.
* **Fixed**: an error logged at startup when the plugin starts before login.
* **Fixed**: logging out and back in lost the digweed marks on the order and the potion, and counted a delivery.
* **Fixed**: the rewards panel stayed hidden after turning the plugin on, until the next delivery.
* **Fixed**: lever click protection locked every lever when the vessel already held a mix for no planned order (for example after a relog, or one only a skipped order takes), leaving no way to correct it. Every lever is now free and the vessel is not highlighted for such a mix.
* **Fixed**: the numbers in the resin bars sat near the top of the bar; they are now vertically centred.
* **Fixed**: with two orders of the same potion, the station highlight could point at a skipped order's station,
  so finishing the potion left the recommended order open. It now points at the recommended order first.
* **Setting tooltips** put each sentence on its own line.

## V1.9.1-ext1.1.0
* **Misclick protection** (new config section, all toggles off by
  default). Contributed by @Mitchole.
  * *Lock idle stations* — per-station toggles for Alembic, Agitator,
    and Retort. Consumes the operate click when the station is empty
    and no unfulfilled order needs it with the potion you're currently
    holding. Examine and other options are untouched.
  * *Cap Concentrate clicks* — stops Retort clicks on the last tick,
    when passive fill completes the potion anyway, so spare clicks can't
    load the next potion.

## V1.9.1-ext1.0
First Extended-fork release; baseline upstream v1.9.1.

**Reward tracking**
* Multi-select goal — tick any subset of the ten rewards; overlay shows
  per-resin progress bars and a single notification when the combined
  cost is met.
* Quantity fields for every repeatable reward (packs, aldarium,
  Chugging Barrel).

**Meta-strategy**
* Recommended-Potion Highlight — colours each order green/red per an
  adaptive state-machine policy (Monte-Carlo tuned; see above). Off by
  default; needs at least one reward ticked.

**Accessibility** (new config section)
* Dyslexic / Colorblind Mode — swaps M/A/L letters for colour blocks in
  the order list and inventory tags.
* Configurable recommend/skip text colours (default gold / charcoal).
* Configurable per-resin colours (defaults `#9D4EDD` / `#00FFB3` /
  `#FF6D00`) applied to blocks, inventory tags, lever highlights, and
  progress-bar fills; live re-paint on change.

## V1.9.1
* Fixed the `Fix Alembic quick-action sound effect` feature to work with the latest RuneLite version

## V1.9.0
* Added an option to fix the quick-action sound delay on the alembic. This option is enabled by default.
* Internal housekeeping to make the plugin more bulletproof against game updates.

## V1.8.1
* Fix an issue where the plugin would break the mixology overlay when boosting for better potions 

## V1.8.0
* New goal tracker feature, use it to track your progress towards the different minigame rewards
* Lowered the potion inventory overlay to make it easier to distinguish finished potions in the inventory

## V1.7.0
* Fixes stations other than the first in a batch being highlighted even with the config toggled off
* Fixes the vanilla UI enhancement for players that are boosting their herblore levels
* Adds colored inventory tags
* Improves how the `by station` potion order works to prioritize concentrate last to avoid missclicks
* Adds a `shortest past` potion order option

## V1.6.0
* Adds support for potion order reordering in the interface. For now, the only available options are: Vanilla (default) & By Station type
* Customizable notifications for digweed spawns

## V1.5.0
* Removal of the potion highlighting strategies, these were conceived when you could only deliver one potion at a time, now it is almost always better to fulfill all orders before delivering.
* Improved quick-action highlighting behavior.
* An option to display the amount of resin you have has been added, it is set to disabled by default.

## V1.4.1
* Fix repeated potion orders not being marked as fulfilled
* Fix `ready!` indicator when using the `NONE` strategy
* Replaced the `ready!` indicator with `done!`

## V1.4.0
* Separated quick-action from station highlighting
* Station highlighting is now based on potions in the inventory instead of what you're currently mixing, this should greatly help those who mix multiple potions at the same time
* Added a `ready!` indicator whenever you finish refining a potion that fulfills an order
* Added inventory potion identification similar to the Item Identification plugin, this can be disabled in the plugin settings 

## V1.3.0
* Added lever highlighting
* Fixed potion experience values

## V1.2.0
* Fixed a bug where using another station rather than the highlighted would not dismiss it even after delivering the potions
* Fixed a bug where disabling station highlights would still keep them highlighted until fulfilling an order
* Added a strategy to favor the retort station
* Added the option of having no strategy at all, thus not highlighting any potion order
* Changed when stations are highlighted, now the plugin tries to highlight them as soon as you mix a potion order correctly

## V1.1.0
* Added different configurable potion order strategy highlighting including: Favor experience, Favor alembic station, Favor mox/aga/lye
* Added station highlighting when you pick up a potion
* Added station quick-action highlighting
* Added digweed notifications & highlights