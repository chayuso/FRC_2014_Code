/*----------------------------------------------------------------------------*/
/* Copyright (c) FIRST 2008. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/
package edu.wpi.first.wpilibj.templates;

import edu.team597.BallManipulator;
import edu.team597.Drive;
import edu.team597.Shooting;
import edu.team597.support.CheesyVisionServer;
import edu.team597.support.XboxController;
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Relay;
import edu.wpi.first.wpilibj.Timer;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 */
public class RobotTemplate extends IterativeRobot {

    CheesyVisionServer server = CheesyVisionServer.getInstance();
    public final int listenPort = 1180;

    Joystick joystickControl1;
    Joystick joystickControl2;
    XboxController shooterControl;
    Compressor airCompressor;
    Drive drive;
    Shooting shootingFunction;
    BallManipulator ballControl;
    long lastPrint = 0;
    int autonomousState = 1;
    int timerState = 1;
    Timer timerAutonomous;
    Relay relayLEDLightChannel1;
    Relay relayLEDLightChannel2;
    DigitalInput diBallLimit;

    public RobotTemplate() {
        joystickControl1 = new Joystick(1);
        joystickControl2 = new Joystick(2);
        shooterControl = new XboxController(3);
        airCompressor = new Compressor(1, 1);
        diBallLimit = new DigitalInput(14);
        relayLEDLightChannel1 = new Relay(7);
        relayLEDLightChannel2 = new Relay(8);
        drive = new Drive(joystickControl1, joystickControl2, relayLEDLightChannel1, relayLEDLightChannel2, diBallLimit);
        shootingFunction = new Shooting(shooterControl);
        ballControl = new BallManipulator(shooterControl, relayLEDLightChannel1, relayLEDLightChannel2, diBallLimit);
        timerAutonomous = new Timer();

    }

    /**
     * This function is run when the robot is first started up and should be
     * used for any initialization code.
     */
    public void robotInit() {
        server.setPort(listenPort);
        server.start();

        airCompressor.start();
        drive.Initialize();
        ballControl.Initialize();
        shootingFunction.Initialize();
        //Used to carry out autonomous in a timely order
        timerState = 1;
        //Ensures that each autonomous function is performed once and in order
        autonomousState = 1;
    }

    public void autonomousInit() {
        autonomousState = 1;
        server.reset();
        server.startSamplingCounts();
    }

    /**
     * This function is called periodically during autonomous
     */
    public void autonomousPeriodic() {

        if (System.currentTimeMillis() > lastPrint) {
            //Prints out the Left and Right values for Cheesy Vision
            System.out.println("Current left: " + server.getLeftStatus() + ", current right: " + server.getRightStatus());
            System.out.println("Left count: " + server.getLeftCount() + ", right count: " + server.getRightCount() + ", total: " + server.getTotalCount() + "\n");
            lastPrint = lastPrint + 1000;
        }
        //Restarts the autonomous timer once and disables drive teleop
        if (timerState == 1) {
            timerAutonomous.reset();
            timerAutonomous.start();
            drive.SetOperatorState(false);
            timerState = 2;
        }

        /*Closes mouth, shifts to speed (if not already), and runs both drive
         motors backwards at 50% for 2.7 seconds then stops and opens mouth*/
        if (autonomousState == 1) {
            if (timerAutonomous.get() <= 2.7) {
                drive.SetShifterState(false);
                drive.SetMotorSpeedValueRight(-.48);
                drive.SetMotorSpeedValueLeft(-.50);
                ballControl.SetMouthClosed();
            } else if (timerAutonomous.get() > 2.7) {
                drive.SetMotorSpeedValueRight(-.28);
                drive.SetMotorSpeedValueLeft(-.30);
                ballControl.SetMouthOpen();
                autonomousState = 2;
            }
        }

        //Calls and performs the shooting functions
        /*If both left and right Chessy Vision values are covered, there will 
         be no second delay that will be used to determine the hot goal*/
        if (autonomousState == 2) {
            if (server.getLeftStatus() == false && server.getRightStatus() == false) {
                if (timerAutonomous.get() >= 4.5) {
                    shootingFunction.ShootTrigger();
                    drive.SetMotorSpeedValueRight(0);
                    drive.SetMotorSpeedValueLeft(0);
                    autonomousState = 3;
                }
            } else {
                if (timerAutonomous.get() >= 5.5) {
                    shootingFunction.ShootTrigger();
                    drive.SetMotorSpeedValueRight(0);
                    drive.SetMotorSpeedValueLeft(0);
                    autonomousState = 4;
                }
            }

        }

        /*Enables the drive controls while closing the mouth and reseting the 
         encoder
         *Two if statements are used here in order to compinsate for the wait
         *time for the previous funtion with a delay of 2 seconds after shooting
         */
        if (autonomousState == 3) {
            if (timerAutonomous.get() >= 6.5) {
                drive.SetOperatorState(true);
                ballControl.SetMouthClosed();
                ballControl.EncoderReset();

                autonomousState = 5;
            }
        }

            if (autonomousState == 4) {
                if (timerAutonomous.get() >= 7.5) {
                    drive.SetOperatorState(true);
                    ballControl.SetMouthClosed();
                    ballControl.EncoderReset();

                    autonomousState = 5;
                }
            }
        

        //Calls all of the available teleop functions so that they can be used
        drive.Periodic();
        ballControl.Periodic();
        shootingFunction.Periodic();
    }

    /**
     * This function is called periodically during operator control
     */
    public void teleopPeriodic() {
        if (System.currentTimeMillis() > lastPrint) {
            System.out.println("BallLimit: === : " + diBallLimit.get());
            System.out.println("Current left: " + server.getLeftStatus() + ", current right: " + server.getRightStatus());
            System.out.println("Left count: " + server.getLeftCount() + ", right count: " + server.getRightCount() + ", total: " + server.getTotalCount() + "\n");
            lastPrint = lastPrint + 1000;
        }

        /*Resets the autonomous & timer states so that they can be triggered 
         again*/
        autonomousState = 1;
        timerState = 1;
        drive.SetOperatorState(true);

        //Enables all teleop functions
        drive.Periodic();
        ballControl.Periodic();
        shootingFunction.Periodic();

    }

    /**
     * This function is called periodically during test mode
     */
    public void testPeriodic() {

    }

    // this method is called once and only once when the robot first initializes and calls StartCompetition
    public void disabledInit() {
        server.stopSamplingCounts();
    }

    // this method is called periodically while in disabled mode.
    public void disabledPeriodic() {

    }

}
