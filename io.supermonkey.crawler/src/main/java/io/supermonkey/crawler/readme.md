- disable window animations
- install dependencies for genymotion and create snapshot called "link"
- install android maven project to build toolkit


todos
- identity of view should include hierarchy, otherwise two activities with different extras are identified as the same
- allow more than one click on a button (e.g. so that sportsearch activity of komoot is passed)
- mix random and trace walk, so that it doesn't get stuck and still is efficient
- show current, now previous view in UI
- actitvate gps and compass in geny
- validate selectors in inspector (before returning hierarchy), set isClickable() of element accordingly
- mark goto-home edges in different color
- show traces in UI (click on note -> show traversed elements in screenshot or so)
- gradle build
- ui should show already traversed elements for each node of the graph (screenshot with colored boxes)


http://developer.android.com/tools/help/uiautomator/UiDevice.html#waitForIdle(long)

bugs
- endless loop, trace seems to be invalid