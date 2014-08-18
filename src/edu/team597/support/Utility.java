/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.team597.support;

/**
 *
 * @author Team597
 */
public class Utility {

    public static int Bound(int input, int min, int max) {
        int returnVal = input;
        if (returnVal < min) {
            returnVal = min;
        }
        if (returnVal > max) {
            returnVal = max;
        }

        return returnVal;
    }

    public static double Bound(double input, double min, double max) {
        double returnVal = input;
        if (returnVal < min) {
            returnVal = min;
        }
        if (returnVal > max) {
            returnVal = max;
        }

        return returnVal;
    }

    public static double DeadZone(double input, double threshold) {
        if (Abs(input) < threshold) {
            return 0;
        } else {
            return input;
        }

    }

    public static double Abs(double input) {
        if (input < 0) {
            return -input;
        } else {
            return input;
        }
    }
}
