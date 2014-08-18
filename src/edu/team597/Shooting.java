/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.team597;

import edu.team597.support.Utility;
import edu.team597.support.XboxController;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.Timer;

/**
 *
 * @author Team597
 */
public class Shooting {

    public static class ShooterStateMachine {

        public static final int READY = 0;
        public static final int SHOOTING = 1;
        public static final int RELOADING1 = 2;
        public static final int RELOADING2 = 3;
        public static final int RELOADING3 = 4;

    }

    XboxController shootController;
    DoubleSolenoid solenoidGearedBox;
    Talon motorWinch;

    DigitalInput diWinchRetractLimit;
    long lastPrint = 0;

    int shooterState = ShooterStateMachine.READY;
    int autonomousState = 1;

    final double MAX_WINCH_MOTOR_SPEED = .97;
    final double MID_WINCH_MOTOR_SPEED = 0.82;
    final double RELOAD_TIMEOUT_SECONDS = 6.0;
    final double WAIT_BEFORE_RELOAD_SECONDS = 2;

    Timer shootingTimer;
    Timer reloadTimeoutTimer;
    Timer autonomousTimer;

    public Shooting(XboxController xBoxController1) {
        shootController = xBoxController1;
        solenoidGearedBox = new DoubleSolenoid(1, 2);
        motorWinch = new Talon(7);
        diWinchRetractLimit = new DigitalInput(5);

        shootingTimer = new Timer();
        reloadTimeoutTimer = new Timer();
        autonomousTimer = new Timer();

    }

    public void Initialize() {
        autonomousState = 1;
        autonomousTimer.reset();
        autonomousTimer.start();
    }

    public void Autonomous() {

    }

    public void Periodic() {
        autonomousState = 1;

        if (System.currentTimeMillis() > lastPrint) {
            System.out.println("WinchLimitSwitch:" + diWinchRetractLimit.get());
            System.out.println("==========================");
            lastPrint = lastPrint + 2000;
        }

        // Main Shooter State Machine
        switch (shooterState) {
            case ShooterStateMachine.READY:
                // if the safety limit switch is triggered..
                if (diWinchRetractLimit.get() == false) {
                    //..dont allow the winch motor to run
                    motorWinch.set(0);
                } else {
                    // bound the yaxis speed between 0 and MAX_WINCH_MOTOR_SPEED
                    motorWinch.set(Utility.Bound(shootController.getRightY(), 0, MAX_WINCH_MOTOR_SPEED));
                }

                if (shootController.getStart()) {
                    solenoidGearedBox.set(DoubleSolenoid.Value.kReverse);
                } else {
                    // set the winch release to "Engaged"
                    solenoidGearedBox.set(DoubleSolenoid.Value.kForward);
                }

                // if the trigger is pressed..
                if (shootController.getRightBumper()) {
                    // start a timer to delay the next state
                    shootingTimer.reset();
                    shootingTimer.start();
                    // go to next state
                    shooterState = ShooterStateMachine.SHOOTING;
                }
                break;
            case ShooterStateMachine.SHOOTING:
                solenoidGearedBox.set(DoubleSolenoid.Value.kReverse);

                motorWinch.set(0);

                if (shootingTimer.get() > WAIT_BEFORE_RELOAD_SECONDS) {
                    // start a timer as a timeout for the next state
                    shootingTimer.reset();
                    shootingTimer.start();
                    reloadTimeoutTimer.reset();
                    reloadTimeoutTimer.start();
                    // go to next state
                    shooterState = ShooterStateMachine.RELOADING1;
                }
                break;
            case ShooterStateMachine.RELOADING1:
                solenoidGearedBox.set(DoubleSolenoid.Value.kForward);
                // set the motor to a really snow value for a little bit
                // so the dog gear catches.
                motorWinch.set(0.1);
                if (shootingTimer.get() > 0.5) {
                    // start a timer as a timeout for the next state
                    shootingTimer.reset();
                    shootingTimer.start();
                    // go to next state
                    shooterState = ShooterStateMachine.RELOADING2;
                }
                break;
            case ShooterStateMachine.RELOADING2:
                solenoidGearedBox.set(DoubleSolenoid.Value.kForward);
                motorWinch.set(MAX_WINCH_MOTOR_SPEED);

                // only pull at max speed for 2.3 seconds
                if (shootingTimer.get() > 2.3) {
                    shooterState = ShooterStateMachine.RELOADING3;
                    shootingTimer.reset();
                    shootingTimer.start();
                }
                // if the limit switch is pressed,  or if we hit the timeout...
                if (!diWinchRetractLimit.get() || (reloadTimeoutTimer.get() > RELOAD_TIMEOUT_SECONDS)) {
                    motorWinch.set(0);
                    shooterState = ShooterStateMachine.READY;
                    reloadTimeoutTimer.stop();
                }
                break;
            case ShooterStateMachine.RELOADING3:
                solenoidGearedBox.set(DoubleSolenoid.Value.kForward);
                motorWinch.set(MID_WINCH_MOTOR_SPEED);

                // if the limit switch is pressed,  or if we hit the timeout...
                if (!diWinchRetractLimit.get() || (shootingTimer.get() > RELOAD_TIMEOUT_SECONDS)) {
                    motorWinch.set(0);
                    shooterState = ShooterStateMachine.READY;
                    shootingTimer.stop();
                    reloadTimeoutTimer.stop();
                }
                break;
        }

    }

    public void ShootTrigger() {
        if (shooterState == ShooterStateMachine.READY) {
            // start a timer to delay the next state
            shootingTimer.reset();
            shootingTimer.start();
            // go to next state
            shooterState = ShooterStateMachine.SHOOTING;
        }

    }
}
