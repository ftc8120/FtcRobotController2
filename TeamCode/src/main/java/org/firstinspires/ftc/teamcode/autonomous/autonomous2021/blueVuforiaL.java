package org.firstinspires.ftc.teamcode.autonomous.autonomous2021;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.tfod.Recognition;
import org.firstinspires.ftc.robotcore.external.tfod.TFObjectDetector;
import org.firstinspires.ftc.teamcode.RobotBrian;

import java.util.List;

import static org.firstinspires.ftc.robotcore.external.BlocksOpModeCompanion.hardwareMap;

@Autonomous(name = "redVuforiaR")
public class redVuforiaR extends OpMode {

    ElapsedTime timer;
    private RobotBrian robot;
    private double speed;
    private int state;

    private final double half_block=3;//actually figure it out

    private static final String TFOD_MODEL_ASSET = "UltimateGoal.tflite";
    private static final String LABEL_FIRST_ELEMENT = "Quad"; //4rings
    private static final String LABEL_SECOND_ELEMENT = "Single";//1ring

    private static final String VUFORIA_KEY =
            "AYef6RP/////AAABmQhqgETT3Uq8mNFqAbjPOD990o1n/Osn3oBdTsKI0NXgPuXS612xYfN5Q65srnoMx2eKX" +
                    "e32WnMf6M2BSJSgoPfTZmkmujVujpE/hUrmy5p4L7CALtVoM+TDkfshpKd+LGJT834pEOYUqcUj+v" +
                    "ySs3OZQNepaSflmiShfHRNVbrgjrEs1Erlg7zZzc6EQo+yvh0fFtUiQUPLCCcZEPyfnU4k0o8phhbR" +
                    "+Ca9B6dtoeNaYITGHvMmOkBLsyAnR/RQ4Xv8KpvSaSfk0PDyzCG7UsN49k055xOxkFI0iKYp7NMCD" +
                    "F+cezE80dkcnpZCzg1RpGuSpCKGuUbSkJp+q5qudl2qZfWnQntaNI0vlNKD2x1C";

    /**
     * {@link #vuforia} is the variable we will use to store our instance of the Vuforia
     * localization engine.
     */
    private VuforiaLocalizer vuforia;

    /**
     * {@link #tfod} is the variable we will use to store our instance of the TensorFlow Object
     * Detection engine.
     */
    private TFObjectDetector tfod;

    /**
     * Initialize the Vuforia localization engine.
     */
    private void initVuforia() {
        /*
         * Configure Vuforia by creating a Parameter object, and passing it to the Vuforia engine.
         */
        VuforiaLocalizer.Parameters parameters = new VuforiaLocalizer.Parameters();

        parameters.vuforiaLicenseKey = VUFORIA_KEY;
        parameters.cameraDirection = VuforiaLocalizer.CameraDirection.BACK;

        //  Instantiate the Vuforia engine
        vuforia = ClassFactory.getInstance().createVuforia(parameters);

        // Loading trackables is not necessary for the TensorFlow Object Detection engine.
    }

    /**
     * Initialize the TensorFlow Object Detection engine.
     */
    private void initTfod() {
        int tfodMonitorViewId = hardwareMap.appContext.getResources().getIdentifier(
                "tfodMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        TFObjectDetector.Parameters tfodParameters = new TFObjectDetector.Parameters(tfodMonitorViewId);
        tfodParameters.minResultConfidence = 0.8f;
        tfod = ClassFactory.getInstance().createTFObjectDetector(tfodParameters, vuforia);
        tfod.loadModelFromAsset(TFOD_MODEL_ASSET, LABEL_FIRST_ELEMENT, LABEL_SECOND_ELEMENT);
    }

    @Override
    public void init() {
        initVuforia();
        initTfod();
        if (tfod != null) {
            tfod.activate();
            tfod.setZoom(2.5, 16.0/9.0);
            //can change mag later to find seomthing better- if want to test
            //dont change ratio
        }

        timer = new ElapsedTime();
        robot = new RobotBrian();
        robot.init(hardwareMap);
        speed = .5;
        state = 0;
    }

    public void next() {
        state++;
        timer.reset();
        robot.stop();
    }

    @Override
    public void loop() {
        switch(state)
        {

            case 0:
                if(tfod != null){
                    List<Recognition> updatedRecognitions = tfod.getUpdatedRecognitions();
                    if(updatedRecognitions != null){
                        if(updatedRecognitions.size() > 0){
                            //we found rings
                            Recognition stack = updatedRecognitions.get(0);
                            String label = stack.getLabel();
                            if(label.equals(LABEL_FIRST_ELEMENT)) {
                                //4rings(square C) on field
                                state = 102; //
                                next();
                            }
                            else if(label.equals(LABEL_SECOND_ELEMENT)){
                                    //1 ring (square B)
                                state = 202;
                                next();
                                

                            }
                        }else {
                        //we found no rings (square A) on field
                            next();
                    }

                    }
                }
                break;

            case 1: // square A
                robot.right(speed);
                if(timer.seconds()>1)
                    next();
                break;

            case 2:

                robot.forward(speed);
                if(timer.seconds()>2.75)
                    next();
                break;

            case 3:
                // drop the thing in box using servo
                robot.stop();
                if(timer.seconds()>3)
                    next();
                break;



            case 103://square c
                robot.right(speed);
                if(timer.seconds()>1)
                    next();
                break;

            case 104:
                robot.forward(speed);
                if(timer.seconds()>5)
                    next();
                break;

            case 105:
                //park on white tape
                robot.backward(speed);
                if(timer.seconds()>1.8)
                    next();
                break;



            case 203://square b
                robot.forward(speed);
                if(timer.seconds()>3.5)
                next();
                break;

            case 204:
                // drop the thing in box using servo
                robot.backward(speed);
                if(timer.seconds()>.8)
                    next();
                break;
        }
    }
}
