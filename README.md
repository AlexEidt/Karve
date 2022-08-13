# Karve

<p align="center">
    <img src="Documentation/karve.png" width=55% />
</p>

**Karve** is a simple [Seam Carver](https://en.wikipedia.org/wiki/Seam_carving). Seam Carving is basically content aware image resizing. The algorithm preserves the objects in the image while less important background space is removed by removing "seams" from the image.

## Features

* Vertical and Horizontal Seam Carving
* "Animate" the Seam Carving Process at varying speeds
* Fast carving for moderately sized images
* Drag and Drop Images into the application
* Removed Seams can be added back onto the image
* Object removal and preservation via highlighting
* Togglable Seam Highlighting
* Togglable GUI updating for faster carving
* Backward and Forward Energy Seam Carving
* Snapshot current image
* Record Seam Carving animation by saving carved image snapshots
* Uses all CPU Cores for faster carving
* No dependencies!

To start, simply drag and drop the image into the application.

<br />

<img src="Documentation/demo.gif" width=100% alt="Karve Seam Carver Demo" />

<br />

If Karve is carving out areas of the image that you want to keep, you can also mark the image with low and high priority regions.

You can also click on the image itself to mark areas of lower importance. Any areas you left click on will be marked in red and seams will be removed there first.

<img src="Documentation/demo-low-priority.gif" width=100% alt="Karve Seam Carver Demo Low Priority" />

<br />

Similarly, any areas you right click on will be marked in green and the algorithm will avoid those areas of the image.

<img src="Documentation/demo-high-priority.gif" width=100% alt="Karve Seam Carver Demo" />

<br />

# Button Descriptions

## Checkboxes

Button | Description | Key
--- | --- | ---
**Show Seams** | If selected, shows the seams being added/removed from the image. | `S`
**Horizontal** | If selected, carves out horizontal seams instead of vertical ones. | `H`
**Recording** | If selected, saves each carved frame as an image in the `Snapshots` directory. | `R`
**Update** | If selected, updates the image on the UI. De-select this option for more efficient carving. | `U`

## Buttons/Icons

Button | Description | Key
--- | --- | ---
<img src="src/Icons/speedometer.gif" width=70px alt="Speedometer Icon" /> | Indicates carving speed. | 
<img src="src/Icons/startstop.gif" width=70px alt="Start and Stop Icons" /> | Start/Stop removing/adding seams from the image. The speed of removal is determined by the slider. | `SPACE`
<img src="src/Icons/add.png" width=70px alt="Add Icon" /> | Add back the most recently removed seam. | `RIGHT ARROW`
<img src="src/Icons/remove.png" width=70px alt="Remove Icon" /> | Remove the next seam. | `LEFT ARROW`
<img src="src/Icons/snapshot.png" width=70px alt="Snapshot Icon" /> | Take a snapshot of the current image. | `C`

# Acknowledgements

* The image used in the demo is from Ralph McQuarrie who did most concept art for the Star Wars original trilogy.
* The Seam Carving Algorithm closely follows that one detailed in MIT's [18.S191: Introduction to computational thinking for real-world problems](https://computationalthinking.mit.edu/Spring21/).