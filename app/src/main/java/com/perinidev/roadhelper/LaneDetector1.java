package com.perinidev.roadhelper;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LaneDetector1 {
    private static final int HOUGH_THRESHOLD = 40;
    private static final int HOUGH_MIN_LINE_LENGTH = 40;
    private static final int HOUGH_MAX_LINE_GAP = 70 ;

    boolean ROI = false;

    double SLOPE_THRESHOLD = 0.5;
    Scalar leftColor = new Scalar(255, 0, 0);
    Scalar rightColor = new Scalar(0, 255, 0);

    //correction factor
    double factor = 0.0035;


    double distToLeftLane = 0.0;
    double distTorightLane = 0.0;
    double distToLaneCenter = 0.0;
    double betweenLanex = 0.0;
    String isOut = "";

    double minSlope = 0.5;
    double maxSlope = 3;

    double leftLaneX = 0.0;
    double rightLaneX = 0.0;


    public Mat detectLanes(Mat input,int imageHeight,int imageWidth) {

//        int imageHeight = input.height();
//        int imageWidth = input.width();


        Mat result = new Mat();

        Imgproc.cvtColor(input, result, Imgproc.COLOR_RGBA2GRAY); //result is the original

        // Apply Gaussian blur to reduce noise
        Imgproc.GaussianBlur(result, result, new Size(5, 5), 0);

        // Cany Edge detection
        Imgproc.Canny(result, result, 150, 200);

        // Apply morphological operations to improve edge detection
        Mat kerneltest = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
        Imgproc.dilate(result, result, kerneltest);
        //Imgproc.erode(result, result, kerneltest);


        Mat mask = Mat.zeros(result.size(), result.type());
        MatOfPoint roiPoints = new MatOfPoint(new Point(
                0, imageHeight), // botton left
                new Point(imageWidth, imageHeight), //botton right
                new Point(imageWidth*6/10, imageHeight/2), //upper right  1280 //2/3
                new Point(imageWidth/2.5, imageHeight/2)); //upper left    640 ///3


        Imgproc.fillConvexPoly(mask, roiPoints, new Scalar(255, 255, 255), Imgproc.LINE_AA);

        Mat roiEdges = new Mat();
        Core.bitwise_and(mask,result,roiEdges); //last parameter roidEdges

        Core.bitwise_and(result, roiEdges, result);


        //Apply adaptive thresholding to improve edge detection
        //Imgproc.adaptiveThreshold(result, result, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 11, 2);

        // Apply morphological operations to improve edge detection
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
        //Imgproc.dilate(result, result, kernel);
        Imgproc.erode(result, result, kernel);


//         //eliminate the horizontal info
//        Mat kernel1 = Mat.zeros(1, 3, CvType.CV_32F);
//        kernel1.put(0,0,-1);
//        kernel1.put(0, 2, 1);
//        Imgproc.filter2D(result, result, -1, kernel1);

        Mat result_cany = new Mat(); //test
        result_cany = roiEdges.clone(); //testx'


        // Apply a Hough transform to detect the lines in the image
        Mat lines = new Mat();
        Imgproc.HoughLinesP(result, lines, 1, Math.PI / 180, HOUGH_THRESHOLD, HOUGH_MIN_LINE_LENGTH, HOUGH_MAX_LINE_GAP);


        //fit lanes

        // Initialize lists to hold the slopes and intercepts of the detected lines
        List<Double> rightSlope = new ArrayList<>();
        List<Double> leftSlope = new ArrayList<>();
        List<Double> rightIntercept = new ArrayList<>();
        List<Double> leftIntercept = new ArrayList<>();

        boolean isSlope = false;

        // Loop through each line detected by the Hough transform
        for (int i = 0; i < lines.rows(); i++) {
            // Extract the line endpoints
            double[] line = lines.get(i, 0);
            double x1 = line[0], y1 = line[1], x2 = line[2], y2 = line[3];
            // Calculate the slope and intercept of the line
            double slope = (y2 - y1) / (x2 - x1);
            double intercept = y1 - slope * x1;

            // If the slope is positive and greater than a threshold, it is the right lane
            // If the slope is negative and less than a threshold, it is the left lane


            if (slope > SLOPE_THRESHOLD) {
                rightSlope.add(slope);
                rightIntercept.add(intercept);
            } else if (slope < -SLOPE_THRESHOLD) {
                leftSlope.add(slope);
                leftIntercept.add(intercept);
            }
        }


        // Calculate the average slope and intercept for the left and right lanes
        double leftavgSlope = leftSlope.parallelStream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double leftavgIntercept = leftIntercept.parallelStream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double rightavgSlope = rightSlope.parallelStream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double rightavgIntercept = rightIntercept.parallelStream().mapToDouble(Double::doubleValue).average().orElse(0.0);

        // Determine if left and right lanes are detected
        boolean leftLaneDetected = (leftavgSlope != 0 && leftavgIntercept != 0);
        boolean rightLaneDetected = (rightavgSlope != 0 && rightavgIntercept != 0);

        // Here we plot the lines and the shape of the lane using the average slope and intercepts

        int left_line_x1 = (int) ((0.65 * input.rows() - leftavgIntercept) / leftavgSlope);
        int left_line_x2 = (int) ((input.rows() - leftavgIntercept) / leftavgSlope);
        int right_line_x1 = (int) ((0.65 * input.rows() - rightavgIntercept) / rightavgSlope);
        int right_line_x2 = (int) ((input.rows() - rightavgIntercept) / rightavgSlope);
        Point[] pts = new Point[]{
                new Point(left_line_x1, 0.65 * input.rows()),
                new Point(left_line_x2, input.rows()),
                new Point(right_line_x2, input.rows()),
                new Point(right_line_x1, 0.65 * input.rows())
        };


        if (Math.abs(rightavgSlope) >= minSlope && Math.abs(rightavgSlope) <= maxSlope){

            Imgproc.line(input, pts[3], pts[2], rightColor, 10);

            // Calculate the distance from the center line to the right lane and draw it on the input image
            rightLaneX = (right_line_x1 + right_line_x2) / 2.0;
            distTorightLane = Math.abs(imageWidth / 2 - rightLaneX) * factor;
        }


        if (Math.abs(leftavgSlope) >= minSlope && Math.abs(leftavgSlope) <= maxSlope) {

            //show lanes
            Imgproc.line(input, pts[0], pts[1], leftColor, 10);

            // Calculate the distance from the center line to the left lane and draw it on the input image
            leftLaneX = (left_line_x1 + left_line_x2) / 2.0;
            distToLeftLane = Math.abs(imageWidth / 2 - leftLaneX) * factor;

        }

        if ((leftLaneDetected && rightLaneDetected ) ) {

            //show polly
            MatOfPoint points = new MatOfPoint(pts);
            Scalar color = new Scalar(0, 0, 255, 128); // blue color in BGR format with alpha value 128 (50% transparency)
            Mat overlay = new Mat(input.size(), input.type());
            input.copyTo(overlay);
            Imgproc.fillPoly(overlay, Arrays.asList(points), color);
            double alpha = 0.25;
            Core.addWeighted(overlay, alpha, input, 1 - alpha, 0, input);

            // calculate the distance from right to left lanes //betweenLanex
            betweenLanex = (rightLaneX - leftLaneX) * factor;



        }


        if (ROI){
            // Draw the ROI on the input image
            List<MatOfPoint> roiContours = new ArrayList<>();
            roiContours.add(roiPoints);
            Imgproc.polylines(input, roiContours, true, new Scalar(0, 255, 0), 3);
            // Draw the center line on the input image
            Point pt1 = new Point(imageWidth/2, 0);
            Point pt2 = new Point(imageWidth/2, imageHeight);
            Imgproc.line(input, pt1, pt2, new Scalar(0, 255, 0), 3);
        }


        return input;
        //return result_cp;


    }

    private double averageX(List<Point> points) {

        double sumx = 0;
        for (Point point : points) {
            sumx += point.x;
        }
        return sumx / points.size();

    }
}