[![](https://jitpack.io/v/rvhamed/BarcodeX.svg)](https://jitpack.io/#rvhamed/BarcodeX)


# BarcodeX 

Scan barcodes and qrcodes with MLKit. In this project MLKit and CameraX are combined with each other.

## How Does it work?
Continuously, Frames come from CameraX and will be passed to MlKit, directly. Intentionaly, We avoid manipulating frames before scan just for better performance. Afterward, The results will be manupulated. Check **BarcodeXActivity**.

## Implementation

*Step 1:* Add it in your root build.gradle at the end of repositories:

 	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}

*Step 2:* Add the dependency

	dependencies {
	        implementation 'com.github.rvhamed:BarcodeX:Tag'
	}
  
 
 ## ScreenShot
  
 <img src="https://github.com/rvhamed/BarcodeX/blob/master/screen_shot_1.png?raw=true" width="400" />
  






