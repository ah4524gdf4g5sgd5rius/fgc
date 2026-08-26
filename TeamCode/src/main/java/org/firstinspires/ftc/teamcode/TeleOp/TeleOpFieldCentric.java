package org.firstinspires.ftc.teamcode.TeleOp;

import com.arcrobotics.ftclib.drivebase.MecanumDrive;
import com.arcrobotics.ftclib.hardware.motors.Motor;
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

@com.qualcomm.robotcore.eventloop.opmode.TeleOp(name = "FieldCentricTele")
public class TeleOpFieldCentric extends LinearOpMode {


    Motor kP, dP, kG, dG; //Važiuoklė
    IMU imu;
    DcMotor pem; //Paėmimas
    DcMotorEx sm; //Šaudyklė
    DcMotor pak; //Pasikėlimo ant lyno variklis
    DcMotor pad; //Padavimas
    boolean paspaustas = false;
    boolean pultelis = false;

    public double drive_speed = 1;

    @Override
    public void runOpMode() throws InterruptedException {

        //Creating Drivetrain Motors and Setting their behaviour to "brake"
        kP = new Motor(hardwareMap, "kP");
        dP = new Motor(hardwareMap, "dP");
        kG = new Motor(hardwareMap, "kG");
        dG = new Motor(hardwareMap, "dG");

        pem = hardwareMap.get(DcMotor.class, "pem");
        pad = hardwareMap.get(DcMotor.class, "pad");
        sm = hardwareMap.get(DcMotorEx.class, "sm");

        kP.setZeroPowerBehavior(Motor.ZeroPowerBehavior.BRAKE);
        dP.setZeroPowerBehavior(Motor.ZeroPowerBehavior.BRAKE);
        kG.setZeroPowerBehavior(Motor.ZeroPowerBehavior.BRAKE);
        dG.setZeroPowerBehavior(Motor.ZeroPowerBehavior.BRAKE);

        pem.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        pad.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        pad.setDirection(DcMotorSimple.Direction.REVERSE);
        sm.setDirection(DcMotorSimple.Direction.REVERSE);

        sm.setMode(DcMotor.RunMode.RUN_USING_ENCODER);



        MecanumDrive drive = new MecanumDrive(kP, dP, kG, dG);
        /// !!!!!!!!!!!!!!!!!!SUDERINTI!!!!!!!!!!!!!!!!!!!!!!
        imu = hardwareMap.get(IMU.class, "imu");
        IMU.Parameters parameters= new IMU.Parameters(new RevHubOrientationOnRobot(
                RevHubOrientationOnRobot.LogoFacingDirection.DOWN,
                RevHubOrientationOnRobot.UsbFacingDirection.BACKWARD
        ));
        imu.initialize(parameters);
        imu.resetYaw();

        //Creating timer variables
        ElapsedTime timer = new ElapsedTime();


        waitForStart();

        while (!isStopRequested()) {

            double rpm = sm.getVelocity() / 28;
            double t = getRuntime();
            double kampas = imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.DEGREES);

            telemetry.addData("RPM:", rpm);
            telemetry.update();
            ///================VAŽIUOKLĖ=========================
            drive.driveFieldCentric(
                    gamepad1.left_stick_x * drive_speed,
                    -gamepad1.left_stick_y * drive_speed,
                    gamepad1.right_stick_x * drive_speed,
                    kampas

            );
            ///===============Greičio kontrolė=======================
            if (gamepad1.right_trigger > 0.5) {
                drive_speed = 0.45;
            } else {
                drive_speed = 0.75;
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
            ///====================Šaudyklė=========================
//            if (gamepad1.right_trigger > 0.15) {
//                sm.setPower(1);
//            }
//            else if (gamepad1.right_trigger < 0.15){
//                sm.setPower(0.0);
//            }
            //=======================Jungiklio testas==============
            if (gamepad1.right_bumper && !paspaustas){
                pultelis = !pultelis;
                paspaustas = true;
            }
            if (!gamepad1.right_bumper){
                paspaustas = false;
            }
            if (pultelis) {
                sm.setPower(1);
            } else if (!pultelis){
                sm.setPower(0.0);
            }

            ///======================Padavimas=====================
            if (gamepad1.right_bumper && t < 0.5){
                pad.setPower(0.8);
            }
            else if (!gamepad1.right_bumper || t > 0.5){
                pad.setPower(0.0);
                sleep(8000);
                resetRuntime();
            }

            ///=====================Pasikėlimas=====================
            if (gamepad1.dpad_up) {
                pak.setPower(0.75);
            } else if (!gamepad1.dpad_up){
                pak.setPower(0.15);
            }
            if (gamepad1.dpad_down){
                pak.setPower(0.0);
            }

        }

    }
}
