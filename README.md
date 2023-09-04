# LaneDetector App


![image](https://github.com/engperini/opencv_lanedetector_app/assets/117356668/9f4f26c2-0791-4876-806f-2fa2f5886550)

https://play.google.com/store/apps/details?id=com.perinidev.roadhelper&hl=pt_BR&gl=US

![image](https://github.com/engperini/opencv_lanedetector_app/assets/117356668/6935454f-3f1c-4c48-8344-201bc96545d4)



## Overview

The LaneDetector app is designed to assist drivers in staying within their lanes while driving on the highway. It utilizes computer vision technology with the OpenCV library on the Android platform to detect road lanes in real-time through the device's camera.

**Key Features:**

- Real-time lane detection using your device's camera.
- Lane distance display for safer driving.
- Sound alerts to warn you when you're too close to lane boundaries.
- Region of Interest (ROI) display option.
- Lane assist mode to help you stay within your lane.

## Table of Contents

- [Getting Started](#getting-started)
- [Usage](#usage)
- [Dependencies](#dependencies)
- [Java Classes](#java-classes)
  - [VideoClass](#videoclass)
  - [LaneDetector1](#lanedetector1)
- [Contribution](#contribution)
- [Author](#author)
- [License](#license)

## Getting Started

To get started with the LaneDetector app, follow these steps:

1. **Clone the Repository:**


2. **Open in Android Studio:**

Open the project in Android Studio and configure it for your Android device.

3. **Run the App:**

Run the app on your Android device.

## Usage

The LaneDetector app is simple to use:

- Press the "Start" button to begin lane detection using your device's camera.
- Adjust the lane distance display scale as needed using the provided input field.
- Enable sound alerts using the "Sound On" button to receive warnings when you're too close to lane boundaries.
- Toggle the "Show ROI" checkbox to display the Region of Interest (ROI) on the camera feed.
- Activate lane assist mode using the "Lane Assist" checkbox to receive assistance in staying within your lane.

## Dependencies

The LaneDetector app relies on the following dependencies:

- [OpenCV Library](https://opencv.org/): OpenCV is used for image processing tasks, including lane detection. Ensure that OpenCV is correctly set up in your project.

## Java Classes

### VideoClass

- **Description:** This class represents the main activity of the LaneDetector app. It handles the camera feed, user interface interactions, and lane detection using the `LaneDetector1` class.

- **Key Methods:**
- `onCreate(Bundle savedInstanceState)`: Initializes the app's UI and sets up OpenCV.
- `detectLane(Mat input, int imageHeight, int imageWidth)`: Detects lanes in the camera feed and displays them on the screen.
- `blinkImageView(Context context, ImageView imageView, int numRepetitions, int delayMillis)`: Animates the lane departure alerts.

### LaneDetector1

- **Description:** This class contains the core logic for lane detection using computer vision techniques. It processes the camera feed, detects lanes, and calculates lane distances.

- **Key Methods:**
- `detectLanes(Mat input, int imageHeight, int imageWidth)`: Performs lane detection on the input image and returns the result.
- `averageX(List<Point> points)`: Calculates the average x-coordinate of a list of points.

## License

This project is licensed under the [MIT License](LICENSE).



