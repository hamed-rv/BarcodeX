# BarcodeX
This is an Android project for scan barcodes and qrcodes with MLKit. In this project MLKit and CameraX are combined with each other.

## How is it work?
Continuously, Frames come from CameraX and will be passed to MlKit, directly. Intentionaly, We avoid manipulating frames before scan just for better performance. Afterward, The results will be manupulated.

![screen shot 1](https://github.com/rvhamed/BarcodeX/blob/master/screen_shot_1.png?raw=true)





