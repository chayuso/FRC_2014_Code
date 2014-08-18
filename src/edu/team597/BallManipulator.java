/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.team597;

import edu.team597.support.ToggleButton;
import edu.team597.support.Utility;
import edu.team597.support.XboxController;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.Relay;
//import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.Timer;
//import edu.wpi.first.wpilibj.Victor;

/**
 *
 * @author Team597
 */
public class BallManipulator {

    public static class ManipulatorStateMachine {

        public static final int ENCODER_RESET = 0;
        public static final int OPERATOR_CTRL = 1;
        public static final int SET_LEVEL = 2;

        public static final int ENCODER_INTAKE_LEVEL = 0;
        public static final int ENCODER_SHOOTING_LEVEL = 89;
        public static final int ENCODER_BLOCKING_LEVEL = 94;
    }

    Talon motorIntakeRoller;
    Talon motorArm;
    long lastPrint = 0;
    XboxController xBoxController;
    DoubleSolenoid solenoidMouth;
    Encoder encoderArm;
    DigitalInput diArmLowerLimit;
    DigitalInput diArmUpperLimit;
    DigitalInput diBallSettle1;
    //DigitalInput diBallSettle2;
    Relay manipulatorLEDLightChannel1;
    Relay manipulatorLEDLightChannel2;
    Timer autoTimer;

    final double MOTOR_INTAKE_SPEED = .98;
    final double MOTOR_ARM_SPEED = 0.35;
    final double ARM_MOVEMENT_SAFETY_THRESHOLD = -0.8;

    ToggleButton buttonToggleCloseMouth;

    int manipulatorState = ManipulatorStateMachine.OPERATOR_CTRL;
    int armAngleTarget = ManipulatorStateMachine.ENCODER_INTAKE_LEVEL;

    int autonomousState = 1;

    public BallManipulator(XboxController xBoxController1, Relay relayLEDLightChannel1, Relay relayLEDLightChannel2, DigitalInput diBallLimit) {
        xBoxController = xBoxController1;
        manipulatorLEDLightChannel1 = relayLEDLightChannel1;
        manipulatorLEDLightChannel2 = relayLEDLightChannel2;
        motorIntakeRoller = new Talon(5);
        motorArm = new Talon(6);
        diArmLowerLimit = new DigitalInput(10);
        diArmUpperLimit = new DigitalInput(9);
        solenoidMouth = new DoubleSolenoid(5, 6);
        encoderArm = new Encoder(2, 3);
        diBallSettle1 = diBallLimit;
        //diBallSettle2 = new DigitalInput(7);
        buttonToggleCloseMouth = new ToggleButton(true);
        autoTimer = new Timer();

    }

    public void Initialize() {
        encoderArm.start();
        encoderArm.setReverseDirection(true);
        autoTimer.reset();
        autoTimer.start();
    }

    public void Periodic() {
        autonomousState = 1;

        if (System.currentTimeMillis() > lastPrint) {
            System.out.println(System.currentTimeMillis());
            System.out.println("==========================");
            System.out.println("!@#$%^&*()_+!@#$%^&*()_+!@#$%^&*()_+!@#$%^&*()_");
            System.out.println("BallSettle1" + diBallSettle1.get());
            //System.out.println("BallSettle2" + diBallSettle2.get());
            System.out.println("MLight1" + manipulatorLEDLightChannel1.get());
            System.out.println("MLight2" + manipulatorLEDLightChannel2.get());
            System.out.println("!@#$%^&*()_+!@#$%^&*()_+!@#$%^&*()_+!@#$%^&*()_");
            //System.out.println("Arm:" + motorArm.get());
            //System.out.println("==========================");
            System.out.println("Chayuso");
            System.out.println("==========================");
            System.out.println("Encoder:" + encoderArm.get());
            System.out.println("LowerLimitSwitch:" + diArmLowerLimit.get());
            System.out.println("UpperLimitSwitch:" + diArmUpperLimit.get());
            System.out.println("===========================");
            System.out.println("ArmSpeed:" + motorArm.get());
            System.out.println("BallSet1:" + diBallSettle1.get());
            //System.out.println("BallSet2:" + diBallSettle2.get());
            System.out.println("Manipulator state: " + manipulatorState);
            lastPrint = lastPrint + 1000;
        }

        //Arm motor Speed Value
        double motorArmSpeedSetpoint = 0;
        //Arm Set Level State Machine
        switch (manipulatorState) {
            case ManipulatorStateMachine.OPERATOR_CTRL:
                //Left Trigger and Left Xbox Joystick to manipulate arm
                if (xBoxController.getZ() < ARM_MOVEMENT_SAFETY_THRESHOLD) {
                    motorArmSpeedSetpoint = xBoxController.getLeftY();
                } else {
                    motorArmSpeedSetpoint = 0;
                }

                //Intake Level
               /* if (xBoxController.getLeftStickPressed() && xBoxController.getAButton()) {
                 armAngleTarget = ManipulatorStateMachine.ENCODER_INTAKE_LEVEL;
                 manipulatorState = ManipulatorStateMachine.SET_LEVEL;
                 }
                 */
                //Encoder Reset
                if (xBoxController.getLeftStickPressed() && xBoxController.getBButton()) {
                    manipulatorState = ManipulatorStateMachine.ENCODER_RESET;
                }
                //Blocking Level
                if (xBoxController.getLeftStickPressed() && xBoxController.getYButton()) {
                    armAngleTarget = ManipulatorStateMachine.ENCODER_BLOCKING_LEVEL;
                    manipulatorState = ManipulatorStateMachine.SET_LEVEL;
                }
                //Shooting Level
                if (xBoxController.getLeftStickPressed() && xBoxController.getXButton()) {
                    armAngleTarget = ManipulatorStateMachine.ENCODER_SHOOTING_LEVEL;
                    manipulatorState = ManipulatorStateMachine.SET_LEVEL;
                }
                break;

            case ManipulatorStateMachine.ENCODER_RESET:
                motorArmSpeedSetpoint = MOTOR_ARM_SPEED;
                if (diArmLowerLimit.get() == false) {
                    motorArmSpeedSetpoint = 0;
                    encoderArm.reset();
                    manipulatorState = ManipulatorStateMachine.OPERATOR_CTRL;
                }
                break;

            case ManipulatorStateMachine.SET_LEVEL:
                if (Math.abs(encoderArm.get() - armAngleTarget) < 2) {
                    motorArmSpeedSetpoint = 0;
                    manipulatorState = ManipulatorStateMachine.OPERATOR_CTRL;
                } else if (encoderArm.get() < armAngleTarget) {
                    motorArmSpeedSetpoint = -MOTOR_ARM_SPEED;
                } else if (encoderArm.get() > armAngleTarget) {
                    motorArmSpeedSetpoint = MOTOR_ARM_SPEED;
                }
                break;

        }

        // if we have reached the lower limit
        if (diArmLowerLimit.get() == false) {
            motorArmSpeedSetpoint = Utility.Bound(motorArmSpeedSetpoint, -1, 0);
            encoderArm.reset();
        } // if we have reached the upper limit
        if (diArmUpperLimit.get() == false) {
            motorArmSpeedSetpoint = Utility.Bound(motorArmSpeedSetpoint, 0, 1);
        }
        motorArm.set(motorArmSpeedSetpoint);

        if (diBallSettle1.get() == false/* && diBallSettle2.get() == false*/) {
            manipulatorLEDLightChannel1.set(Relay.Value.kOn);
            manipulatorLEDLightChannel2.set(Relay.Value.kOn);
        }

        // intake/outtake
        if (xBoxController.getAButton() && xBoxController.getLeftStickPressed() == false) {
            motorIntakeRoller.set(MOTOR_INTAKE_SPEED);
        } else if (xBoxController.getXButton() && xBoxController.getLeftStickPressed() == false) {
            motorIntakeRoller.set(-(MOTOR_INTAKE_SPEED));
        } else {
            motorIntakeRoller.set(0);
        }

        // mouth open/close
        buttonToggleCloseMouth.setCurrentState(xBoxController.getLeftBumper());
        if (buttonToggleCloseMouth.getCurrentState() == false) {
            solenoidMouth.set(DoubleSolenoid.Value.kForward);
        } else if (buttonToggleCloseMouth.getCurrentState() == true) {
            solenoidMouth.set(DoubleSolenoid.Value.kReverse);
        }

    }

    public void SetMouthOpen() {
        buttonToggleCloseMouth.setInternalToggleState(false);
    }

    public void SetMouthClosed() {
        buttonToggleCloseMouth.setInternalToggleState(true);
    }

    public void EncoderReset() {
        manipulatorState = ManipulatorStateMachine.ENCODER_RESET;
    }
}
