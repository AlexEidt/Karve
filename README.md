# Karve

<p align="center">
    <img src="Documentation/karve.png" width=55% />
</p>

**Karve** is a simple [Seam Carver](https://en.wikipedia.org/wiki/Seam_carving). Seam Carving is basically content aware image resizing. The algorithm preserves the objects in the image while less important background space is removed by removing "seams" from the image.

With Karve you can carve out vertical and horizontal seams, and save your images as well!

You can also click on the image itself to mark areas of lower importance. Any areas you click on will be marked and seams will be removed there first.

<br />

<p align="center">

<img src="Documentation/demo.gif" width=100% alt="Karve Seam Carver Demo" />

</p>

<br /><br />

# Files

File | Description
--- | ---
`Console.java` | Runs the Seam Carver from the console. For those interested in carving a certain number of seams from an image without the user interface.
`Main.java` | Runs the Graphical User Interface (GUI) window that allows the user to interface with the Karver.
`SeamCarver.java` | Standalone Seam Carver class.
`Utils.java` |  Contains a collection of useful (and unrelated) functions for the Seam Carver.

# Button Descriptions

Button | Description
--- | ---
<img src="Icons/startstop.gif" width=100px alt="Start and Stop Icons" /> | Start/Stop removing/adding seams from the image. The speed of removal is determined by the slider.
<img src="Icons/add.png" width=100px alt="Add Icon" /> | Add back the most recently removed seam.
<img src="Icons/remove.png" width=100px alt="Remove Icon" /> | Remove the next seam.
<img src="Icons/snapshot.png" width=100px alt="Snapshot Icon" /> | Take a snapshot of the current image.

# Acknowledgements

* The image used in the demo is from Ralph McQuarrie who did most concept art for the Star Wars original trilogy.
* The Seam Carving Algorithm closely follows that one detailed in MIT's [18.S191: Introduction to computational thinking for real-world problems](https://computationalthinking.mit.edu/Spring21/).