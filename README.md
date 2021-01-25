[ ![Download](https://api.bintray.com/packages/rvhamed/BarcodeX/barcodex/images/download.svg) ](https://bintray.com/rvhamed/BarcodeX/barcodex/_latestVersion)
# BarcodeX 

Scan barcodes and qrcodes with MLKit. In this project MLKit and CameraX are combined with each other.

## How Does it work?
Continuously, Frames come from CameraX and will be passed to MlKit, directly. Intentionaly, We avoid manipulating frames before scan just for better performance. Afterward, The results will be manupulated. Check **Sample** project.

## Implementation

*Just:* Add the dependency

	dependencies {
	        implementation 'com.github.rvhamed:barcodex:Tag'
	}
  

 ## ScreenShot
  
 <img src="https://github.com/rvhamed/BarcodeX/blob/master/screen_shot_1.png?raw=true" width="400" />
  






