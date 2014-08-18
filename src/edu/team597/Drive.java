/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.team597;

import edu.team597.support.Utility;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Relay;
import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.Timer;

/**
 *
 * @author Team597
 */
public class Drive {

    public static class DriveStateMachine {

        //State Machine values
        public static final int OPERATOR_CTRL = 0;
        public static final int SET_CONTROL = 1;
        public static final int INVERT_CTRL = 2;
    }

    Talon motorRight1;
    Talon motorRight2;
    Talon motorLeft1;
    Talon motorLeft2;
    DoubleSolenoid solenoidShifter;
    Joystick joystickLeft;
    Joystick joystickRight;
    Timer timerAutonomous;
    Relay driveLEDLightChannel1;
    Relay driveLEDLightChannel2;
    DigitalInput diBallSettle;

    float lastPrint = 0;

    int driveState = DriveStateMachine.OPERATOR_CTRL;
    double motorSpeedSetValueLeft = 0;
    double motorSpeedSetValueRight = 0;
    boolean invertedMotorSpeed = false;
    boolean shifterOnSetValue = false;

    double MOTOR_LEFT_DRIVE_SPEED = .5;
    double MOTOR_RIGHT_DRIVE_SPEED = .5;
    //double DEADZONE_JOYSTICK_THRESHOLD = 0.1;
    int autonomousShifterState = 1;

    public Drive(Joystick j1, Joystick j2, Relay relayLEDLightChannel1, Relay relayLEDLightChannel2, DigitalInput diBallLimit) {
        joystickLeft = j1;
        joystickRight = j2;
        solenoidShifter = new DoubleSolenoid(3, 4);
        motorRight1 = new Talon(1);
        motorRight2 = new Talon(2);
        motorLeft1 = new Talon(3);
        motorLeft2 = new Talon(4);
        timerAutonomous = new Timer();
        driveLEDLightChannel1 = relayLEDLightChannel1;
        driveLEDLightChannel2 = relayLEDLightChannel2;
        diBallSettle = diBallLimit;

    }

    public void Initialize() {
        autonomousShifterState = 1;
        timerAutonomous.reset();
        timerAutonomous.start();

    }

    public void Periodic() {
        autonomousShifterState = 1;

        if (System.currentTimeMillis() > lastPrint) {

            System.out.println("!@#$%^&*()_+!@#$%^&*()_+!@#$%^&*()_+!@#$%^&*()_");
            System.out.println("SolenoidShifter:   " + shifterOnSetValue);
            System.out.println("!@#$%^&*()_+!@#$%^&*()_+!@#$%^&*()_+!@#$%^&*()_123456890-123456890");
            lastPrint = lastPrint + 1000;
        }

        //Main Drive State Machine
        switch (driveState) {
            case DriveStateMachine.OPERATOR_CTRL:

                if (invertedMotorSpeed) {
                    motorRight1.set((-joystickLeft.getY()));
                    motorRight2.set((-joystickLeft.getY()));
                    motorLeft1.set((joystickRight.getY()));
                    motorLeft2.set((joystickRight.getY()));
                } else {
                    motorRight1.set((joystickRight.getY()));
                    motorRight2.set((joystickRight.getY()));
                    motorLeft1.set((-joystickLeft.getY()));
                    motorLeft2.set((-joystickLeft.getY()));
                }

                //Torque Drive
                if (joystickLeft.getRawButton(1)) {
                    shifterOnSetValue = true;
                    solenoidShifter.set(DoubleSolenoid.Value.kReverse);
                }
                //Speed Drive
                if (joystickRight.getRawButton(1)) {
                    shifterOnSetValue = false;
                    solenoidShifter.set(DoubleSolenoid.Value.kForward);
                }
                
                
                if (shifterOnSetValue) {
                    solenoidShifter.set(DoubleSolenoid.Value.kReverse);
                    driveLEDLightChannel1.set(Relay.Value.kOff);
                    driveLEDLightChannel2.set(Relay.Value.kOn);

                } else {
                    solenoidShifter.set(DoubleSolenoid.Value.kForward);
                    driveLEDLightChannel1.set(Relay.Value.kOn);
                    driveLEDLightChannel2.set(Relay.Value.kOff);
                    

                }
                
                if (diBallSettle.get() == true) {
                    if (shifterOnSetValue) {
                        driveLEDLightChannel1.set(Relay.Value.kOff);
                        driveLEDLightChannel2.set(Relay.Value.kOn);
                    }
                    else {
                        driveLEDLightChannel1.set(Relay.Value.kOn);
                        driveLEDLightChannel2.set(Relay.Value.kOff);
                    }
                }
//if (driveLEDLightChannel1.get() != Relay.Value.kForward && driveLEDLightChannel2.get() != Relay.Value.kForward) {

                //}
                //Invert Drive
                if (joystickRight.getRawButton(10)) {
                    invertedMotorSpeed = true;
                }
                //Regular Drive
                if (joystickRight.getRawButton(11)) {
                    invertedMotorSpeed = false;
                }

                break;
            case DriveStateMachine.SET_CONTROL:
                //Regular Robot Drive Set Value
                motorRight1.set(-motorSpeedSetValueRight);
                motorRight2.set(-motorSpeedSetValueRight);
                motorLeft1.set(motorSpeedSetValueLeft);
                motorLeft2.set(motorSpeedSetValueLeft);

                //Torque Drive Set Value if true, else Speed
                if (shifterOnSetValue) {
                    solenoidShifter.set(DoubleSolenoid.Value.kReverse);

                } else {
                    solenoidShifter.set(DoubleSolenoid.Value.kForward);

                }
                /* if (shifterOnSetValue) {
                
                 } else {
                
                 }*/
                break;
        }

    }

    //Enables Operator State if true
    public void SetOperatorState(boolean operatorState) {
        if (operatorState) {
            driveState = DriveStateMachine.OPERATOR_CTRL;
        } else {
            driveState = DriveStateMachine.SET_CONTROL;
            motorSpeedSetValueRight = 0;
            motorSpeedSetValueLeft = 0;
            shifterOnSetValue = false;
        }
    }

    //Left motors Set Value
    public void SetMotorSpeedValueRight(double inputMotorSpeed) {
        motorSpeedSetValueRight = Utility.Bound(inputMotorSpeed, -1, 1);
    }

    //Right motors Set Value
    public void SetMotorSpeedValueLeft(double inputMotorSpeed) {
        motorSpeedSetValueLeft = Utility.Bound(inputMotorSpeed, -1, 1);
    }

    //Shifting Set Value
    public void SetShifterState(boolean inputShifterOn) {
        shifterOnSetValue = inputShifterOn;
        if (shifterOnSetValue) {
                    solenoidShifter.set(DoubleSolenoid.Value.kReverse);
                    driveLEDLightChannel1.set(Relay.Value.kOff);
                    driveLEDLightChannel2.set(Relay.Value.kOn);

                } else {
                    solenoidShifter.set(DoubleSolenoid.Value.kForward);
                    driveLEDLightChannel1.set(Relay.Value.kOn);
                    driveLEDLightChannel2.set(Relay.Value.kOff);
                    

                }
    }

}
