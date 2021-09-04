# Karve

<p align="center">
    <img src="Documentation/karve.png" width=55% />
</p>

**Karve** is a simple [Seam Carver](https://en.wikipedia.org/wiki/Seam_carving). Seam Carving is basically content aware image resizing. The algorithm preserves the objects in the image while less important background space is removed by removing "seams" from the image.

With Karve you can carve out vertical and horizontal seams, and save your images as well!

<br />

<p align="center">

<img src="Documentation/demo1.gif" width=100% alt="Karve Seam Carver Demo" />
<br /><br /><br />
<img src="Documentation/demo2.gif" width=100% alt="Karve Seam Carver Demo" />

</p>

<br /><br />

# Button Descriptions

Button | Description
--- | ---
<img src="Icons/startstop.gif" width=20% alt="Start and Stop Icons" /> | Start/Stop removing/adding seams from the image. The speed of removal is determined by the slider.
<img src="Icons/add.png" width=20% alt="Add Icon" /> | Add back the most recently removed seam.
<img src="Icons/remove.png" width=20% alt="Remove Icon" /> | Remove the next seam.
<img src="Icons/snapshot.png" width=20% alt="Snapshot Icon" /> | Take a snapshot of the current image.

# Acknowledgements

* The image used in the demo is from Ralph McQuarrie who did most concept art for the Star Wars original trilogy.
* The Seam Carving Algorithm closely follows that one detailed in MIT's [18.S191: Introduction to computational thinking for real-world problems](https://computationalthinking.mit.edu/Spring21/).