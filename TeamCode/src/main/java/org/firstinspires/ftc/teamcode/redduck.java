package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.hardware.rev.Rev2mDistanceSensor;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;

import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.hardware.rev.Rev2mDistanceSensor;


@Autonomous
public class redduck extends LinearOpMode {

    private DistanceSensor left;
    private DistanceSensor back;

    BNO055IMU imu;
    BNO055IMU.Parameters parameters;
    Orientation lastAngles = new Orientation();
    double globalAngle;

    public void imuinit() {
        imu = hardwareMap.get(BNO055IMU.class, "imu");
        parameters = new BNO055IMU.Parameters();
        parameters.mode = BNO055IMU.SensorMode.IMU;
        parameters.angleUnit = BNO055IMU.AngleUnit.DEGREES;
        parameters.accelUnit = BNO055IMU.AccelUnit.METERS_PERSEC_PERSEC;
        parameters.loggingEnabled = false;
        imu.initialize(parameters);

        telemetry.addData("Gyro Mode", "calibrating...");

        telemetry.update();


        while (!isStopRequested() && !imu.isGyroCalibrated()) {
            sleep(50);
            idle();
        }

        telemetry.addData("Gyro Mode", "ready");

        telemetry.addData("imu calib status", imu.getCalibrationStatus().toString());

        telemetry.update();

    }

    private void resetAngle() {
        lastAngles = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);

        globalAngle = 0;
    }

    private double getAngle() {

        Orientation angles = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);

        double deltaAngle = angles.firstAngle - lastAngles.firstAngle;

        if (deltaAngle < -180)
            deltaAngle += 360;
        else if (deltaAngle > 180)
            deltaAngle -= 360;

        globalAngle += deltaAngle;

        lastAngles = angles;

        return globalAngle;
    }

    public void runOpMode() throws InterruptedException {
        DcMotor tl = hardwareMap.get(DcMotor.class, "tl");
        DcMotor tr = hardwareMap.get(DcMotor.class, "tr");
        DcMotor bl = hardwareMap.get(DcMotor.class, "bl");
        DcMotor br = hardwareMap.get(DcMotor.class, "br");
        motorblock block = new motorblock(tl, tr, bl, br);
        left = hardwareMap.get(DistanceSensor.class, "left");
        back = hardwareMap.get(DistanceSensor.class, "back");

        Rev2mDistanceSensor sensorTimeOfFlight1 = (Rev2mDistanceSensor) left;
        Rev2mDistanceSensor sensorTimeOfFlight2 = (Rev2mDistanceSensor) back;


        imuinit();
        waitForStart();
        while (opModeIsActive()) {

            double power = 0.2;
            double differenceStraight = (back.getDistance(DistanceUnit.INCH) - 7);
            double straight = differenceStraight*0.3;
            double differenceSide = (left.getDistance(DistanceUnit.INCH) - 1);
            double side = differenceSide*0.3;
            double tlStraight = (straight*(-(power + (0.01 * getAngle()))));
            double trStraight = (straight*((power - (0.01 * getAngle()))));
            double blStraight = (straight*(-(power + (0.01 * getAngle()))));
            double brStraight = (straight*((power - (0.01 * getAngle()))));
            double tlSide = (side*((power - (0.01 * getAngle()))));
            double trSide = (side*((power - (0.01 * getAngle()))));
            double blSide = (side*(-(power + (0.01 * getAngle()))));
            double brSide = (side*(-(power + (0.01 * getAngle()))));


            tl.setPower(tlStraight + tlSide);
            tr.setPower(trStraight + trSide);
            bl.setPower(blStraight + blSide);
            br.setPower(brStraight + brSide);

            if (-0.5 < differenceStraight && differenceStraight < 0.5
                    && -0.5 < differenceSide && differenceSide < 0.5) {
                block.leftturn(0.075*getAngle());
            }
            telemetry.addData("angle", getAngle());
            telemetry.addData("difStraight", differenceStraight);
            telemetry.addData("difSide", differenceSide);
            telemetry.update();

        }
    }
}