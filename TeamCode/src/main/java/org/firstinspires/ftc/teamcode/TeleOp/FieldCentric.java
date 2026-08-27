package org.firstinspires.ftc.teamcode.TeleOp;

import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.IMU;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
@Disabled
@com.qualcomm.robotcore.eventloop.opmode.TeleOp(name = "FieldCentricTele")
public class FieldCentric extends LinearOpMode {


    DcMotor kP, dP, kG, dG; //Važiuoklė
    IMU imu;
    DcMotor pem; //Paėmimas
    DcMotorEx sm; //Šaudyklė
    DcMotor pak; //Pasikėlimo ant lyno variklis
    DcMotor pad; //Padavimas
    boolean prev = false;
    boolean MotorOn = false;
    boolean stabdis = false;
    double greitis = 0.5;

    @Override
    public void runOpMode() throws InterruptedException {

        //Creating Drivetrain Motors and Setting their behaviour to "brake"
        kP = hardwareMap.get(DcMotor.class, "kP");
        dP = hardwareMap.get(DcMotor.class,  "dP");
        kG = hardwareMap.get(DcMotor.class,  "kG");
        dG = hardwareMap.get(DcMotor.class,  "dG");

        pem = hardwareMap.get(DcMotor.class, "pem");
        pad = hardwareMap.get(DcMotor.class, "pad");
        sm = hardwareMap.get(DcMotorEx.class, "sm");
        pak = hardwareMap.get(DcMotor.class, "pak");

        kP.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        dP.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        kG.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        dG.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);


        pem.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        pad.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        pak.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        pad.setDirection(DcMotorSimple.Direction.REVERSE);
        sm.setDirection(DcMotorSimple.Direction.REVERSE);

        /// =======================IMU==========================
        imu = hardwareMap.get(IMU.class, "imu");
        IMU.Parameters parameters= new IMU.Parameters(new RevHubOrientationOnRobot(
                RevHubOrientationOnRobot.LogoFacingDirection.DOWN,
                RevHubOrientationOnRobot.UsbFacingDirection.BACKWARD
        ));
        imu.initialize(parameters);
        imu.resetYaw();

        waitForStart();

        while (!isStopRequested()) {
            double rpm = sm.getVelocity() / 28;
            double t = getRuntime();
            double kampas = imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.DEGREES);
            ///==========================Telemetrija=====================
            telemetry.addData("RPM: ", rpm);
            telemetry.addData("Kampas: ", kampas);
            telemetry.update();

            ///================VAŽIUOKLĖ=========================
            double pirmyn = gamepad1.left_stick_y;
            double bausti = -gamepad1.left_stick_x; ///STRAFE
            double posukis = -gamepad1.right_stick_x;

            double cos = Math.cos((Math.PI / 2) + kampas);// (-) buvo
            double sin = Math.sin((Math.PI / 2) + kampas);

            double didBausme = bausti * cos + pirmyn * sin; ///Global strafe
            double didPirmyn = pirmyn * cos - bausti * sin; /// Global forward

            double kp, dp, kg, dg;

            kp = didPirmyn + didBausme + posukis;
            dp = didPirmyn - didBausme - posukis;
            kg = didPirmyn - didBausme + posukis;
            dg = didPirmyn + didBausme - posukis;

            kP.setPower(-kp * greitis);
            kG.setPower(-kg * greitis);
            dP.setPower(dp * greitis);
            dG.setPower(dg * greitis);

            ///===============Greičio kontrolė=======================
            if (gamepad1.right_trigger > 0.5) {
                greitis = 0.45;
            } else {
                greitis = 0.5;
            }
            ///===================Kampo nunulinimas=================
            if (gamepad1.options){
                imu.resetYaw();
            }
            ///===================Paėmimas==========================
            if (gamepad1.left_bumper){

                pem.setPower(0.5);
            } else if (!gamepad1.left_bumper && !gamepad1.right_bumper) {
                pem.setPower(0.0);
            }
            ///====================Padavimas=========================
            if (rpm >= 55) {
                pad.setPower(1);
            }
            else if (rpm < 55){
                pad.setPower(0.0);
            }
            ///======================Šaudyklė=====================

            boolean paspaustas = false;
            paspaustas = gamepad1.right_bumper;

                if (paspaustas && !prev){
                    MotorOn = !MotorOn;
                }
                prev = paspaustas;
                if (MotorOn) {
                    sm.setPower(0.85);
                }
                else{
                    sm.setPower(0.2);
                }



            ///=====================Pasikėlimas=====================
//            boolean paspaustas2 = false;
//            paspaustas2 = gamepad1.dpad_down;
//            if(!paspaustas2 && !stabdis) {
//                if (gamepad1.dpad_up) {
//                    pak.setPower(0.75);
//                }
//                else if (!gamepad1.dpad_up) {
//                    pak.setPower(0.15);
//                }
//            }
//            if (gamepad1.dpad_down){
//                pak.setPower(0.0);
//                stabdis = true;
//            }
//            if (gamepad1.dpad_up) stabdis = false;
            if (gamepad1.dpad_up) pak.setPower(1);
            else if (!gamepad1.dpad_up) pak.setPower(0);

            telemetry.update();
        }

    }
}
