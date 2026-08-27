package org.firstinspires.ftc.teamcode.TeleOp;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

@Disabled
@com.qualcomm.robotcore.eventloop.opmode.TeleOp(name = "RoboCentricTele")
public class RoboCentric extends LinearOpMode {


    DcMotor kP, dP, kG, dG; //Važiuoklė
    DcMotor pem; //Paėmimas
    DcMotorEx sm; //Šaudyklė
    DcMotor pak; //Pasikėlimo ant lyno variklis
    DcMotor pad; //Padavimas
    boolean prev = false;
    boolean MotorOn = false;
    boolean stabdis = false;
    double greitis;


    public double drive_speed = 1;

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

        waitForStart();

        while (!isStopRequested()) {
            double rpm = sm.getVelocity() / 28;
            double t = getRuntime();

            ///==========================Telemetrija=====================
            telemetry.addData("RPM: ", rpm);

            telemetry.update();


            ///================VAŽIUOKLĖ=========================
            double pirmyn = gamepad1.left_stick_y;
            double bausti = -gamepad1.left_stick_x; ///STRAFE
            double posukis = -gamepad1.right_stick_x;

            double kp, dp, kg, dg;

            kp = pirmyn + bausti + posukis;
            dp = pirmyn - bausti - posukis;
            kg = pirmyn - bausti + posukis;
            dg = pirmyn + bausti - posukis;

            kP.setPower(-kp * greitis);
            kG.setPower(-kg * greitis);
            dP.setPower(dp * greitis);
            dG.setPower(dg * greitis);

            ///===============Greičio kontrolė=======================
            if (gamepad1.left_trigger > 0.5) {
                greitis = 0.45;
            } else {
                greitis = 0.6;
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
