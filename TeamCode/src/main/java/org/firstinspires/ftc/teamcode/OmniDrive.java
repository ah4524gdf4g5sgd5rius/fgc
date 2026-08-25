package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.util.ElapsedTime;


@TeleOp(name="Omni Drive", group="Linear OpMode")
public class OmniDrive extends LinearOpMode {

    private static final double MAX_POWER = 1;
    private static final double RIGHT_DRIVE_SCALE = 0.95;

    private static final double SHOOTER_TICKS_PER_REV = 28.0;
    private static final double SHOOTER_TARGET_RPM = 4000.0;

    private static final double SHOOTER_P = 30.0;
    private static final double SHOOTER_I = 0.0;
    private static final double SHOOTER_D = 0.5;
    private static final double SHOOTER_F = 14.0;

    private static final double SHOOTER_READY_FRACTION = 0.65;
    private static final double SHOOTER_BOOST_FRACTION = 1.20;
    private static final double SHOOTER_BOOST_EXIT_FRACTION = 0.98;
    private boolean shooterBoosting = false;

    private ElapsedTime runtime = new ElapsedTime();
    private DcMotor frontLeftDrive = null;
    private DcMotor frontRightDrive = null;
    private DcMotor backLeftDrive = null;
    private DcMotor backRightDrive = null;
    private DcMotor intakeMotor = null;
    private DcMotorEx shooterMain = null;
    private DcMotorEx shooterOther = null;
    private static final double TRIGGER_THRESHOLD = 0.5;

    @Override
    public void runOpMode() {

        frontLeftDrive = hardwareMap.get(DcMotor.class, "FrontLeft");
        frontRightDrive = hardwareMap.get(DcMotor.class, "FrontRight");
        backLeftDrive = hardwareMap.get(DcMotor.class, "BackLeft");
        backRightDrive = hardwareMap.get(DcMotor.class, "BackRight");
        intakeMotor = hardwareMap.get(DcMotor.class, "intake");
        shooterMain = hardwareMap.get(DcMotorEx.class, "shooterMain");
        shooterOther = hardwareMap.get(DcMotorEx.class, "shooterOther");

        shooterMain.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        shooterMain.setVelocityPIDFCoefficients(SHOOTER_P, SHOOTER_I, SHOOTER_D, SHOOTER_F);


        frontLeftDrive.setDirection(DcMotor.Direction.FORWARD);
        backLeftDrive.setDirection(DcMotor.Direction.FORWARD);
        frontRightDrive.setDirection(DcMotor.Direction.REVERSE);
        backRightDrive.setDirection(DcMotor.Direction.REVERSE);


        frontLeftDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        frontRightDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backLeftDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backRightDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        telemetry.addData("Status", "Initialized");
        telemetry.update();

        waitForStart();
        runtime.reset();

        while (opModeIsActive()) {
            double max;

            double axial   = -gamepad1.left_stick_y;
            double lateral =  gamepad1.left_stick_x;
            double yaw     =  gamepad1.right_stick_x;

            double frontLeftPower  = axial + lateral + yaw;
            double frontRightPower = axial - lateral - yaw;
            double backLeftPower   = axial - lateral + yaw;
            double backRightPower  = axial + lateral - yaw;

            max = Math.max(Math.abs(frontLeftPower), Math.abs(frontRightPower));
            max = Math.max(max, Math.abs(backLeftPower));
            max = Math.max(max, Math.abs(backRightPower));

            if (max > 1.0) {
                frontLeftPower  /= max;
                frontRightPower /= max;
                backLeftPower   /= max;
                backRightPower  /= max;
            }

            // Cap overall drive power at 60%.
            frontLeftPower  *= MAX_POWER;
            frontRightPower *= MAX_POWER;
            backLeftPower   *= MAX_POWER;
            backRightPower  *= MAX_POWER;

            frontRightPower *= RIGHT_DRIVE_SCALE;
            backRightPower  *= RIGHT_DRIVE_SCALE;

            frontLeftDrive.setPower(frontLeftPower);
            frontRightDrive.setPower(frontRightPower);
            backLeftDrive.setPower(backLeftPower);
            backRightDrive.setPower(backRightPower);

            if (gamepad1.right_bumper) {
                intakeMotor.setPower(1.0);
            } else if (gamepad1.left_bumper) {
                intakeMotor.setPower(-1.0);
            } else {
                intakeMotor.setPower(0.0);
            }


            boolean currentR2 = gamepad1.right_trigger > TRIGGER_THRESHOLD;
            double shooterRpm = (shooterMain.getVelocity() / SHOOTER_TICKS_PER_REV) * 60.0;
            if (currentR2) {
                if (shooterRpm < SHOOTER_TARGET_RPM * SHOOTER_BOOST_EXIT_FRACTION) {
                    shooterBoosting = true;
                } else {
                    shooterBoosting = false;
                }
                double commandRpm = shooterBoosting ? SHOOTER_TARGET_RPM * SHOOTER_BOOST_FRACTION : SHOOTER_TARGET_RPM;
                shooterMain.setVelocity(commandRpm * SHOOTER_TICKS_PER_REV / 60.0);
            } else {
                shooterBoosting = false;
                shooterMain.setVelocity(0);
            }
            boolean shooterReady = currentR2 && shooterRpm >= SHOOTER_TARGET_RPM * SHOOTER_READY_FRACTION;
            if (shooterReady) {
                shooterOther.setPower(1.0);
            } else {
                shooterOther.setPower(0.0);
            }

            telemetry.addData("Status", "Run Time: " + runtime.toString());
            telemetry.addData("Front left/Right", "%4.2f, %4.2f", frontLeftPower, frontRightPower);
            telemetry.addData("Back  left/Right", "%4.2f, %4.2f", backLeftPower, backRightPower);
            telemetry.addData("Shooter RPM", "%.0f / %.0f", shooterRpm, SHOOTER_TARGET_RPM);
            telemetry.addData("Shooter Ready", shooterReady);
            telemetry.update();
        }
    }
}
