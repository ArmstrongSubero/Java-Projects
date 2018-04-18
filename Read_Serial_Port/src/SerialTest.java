import com.fazecast.jSerialComm.SerialPort;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import java.util.Scanner;


public class SerialTest extends Application  {

    public static SerialPort myComPort = null;   // Serial port
    String deviceConnected;                      // Device connected

    double roll;                                 // Variable for roll, pitch, yaw
    double pitch;
    double yaw;

    double w, x, y, z;                           // Variables for quaternion


    // add a primitive cylinder
    final Cylinder cylinder = new Cylinder(50, 100);  // cyliner construction, specify radis, height


    @Override
    public void start(Stage primaryStage) throws Exception {

        Group sceneRoot = new Group();
        Scene scene = new Scene(sceneRoot, 500, 500);
        scene.setFill(Color.WHITE);

        // create 3d camera node
        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setNearClip(0.1);
        camera.setFarClip(100000.0);
        camera.setTranslateZ(-1000);
        scene.setCamera(camera);

        // set cylinder properties
        final PhongMaterial blueMaterial = new PhongMaterial();   // paint for 3d interpolation
        blueMaterial.setDiffuseColor(Color.DEEPSKYBLUE);
        blueMaterial.setSpecularColor(Color.BLUE);
        cylinder.setMaterial(blueMaterial);

        sceneRoot.getChildren().add(cylinder);   // add shape to scene

        primaryStage.setTitle("3DScene");
        primaryStage.setScene(scene);
        primaryStage.show();


        // Start background thread
        new Thread() {
            @Override
            public void run()
            {
                readimu();
            }

            ;
        }.start();

        }

        private void readimu() {

            // get serial ports
            int len = SerialPort.getCommPorts().length;
            SerialPort serialPorts[] = new SerialPort[len];
            serialPorts = SerialPort.getCommPorts();

            // take first serial port
            String portName = serialPorts[0].getDescriptivePortName();
            System.out.println(serialPorts[0].getSystemPortName() + ": " + portName + ": " + 0);
            myComPort = serialPorts[0];

            // connect to opened serial port
            myComPort.setComPortParameters(9600, 8, 1, SerialPort.NO_PARITY);
            myComPort.openPort();

            // set timeout as scanner timeout
            myComPort.setComPortTimeouts(SerialPort.TIMEOUT_SCANNER, 100, 0);

            // double to store quaternion values
            Double[] ar = new Double[4];
            Scanner scanner = new Scanner(myComPort.getInputStream());

            //
            while (scanner.hasNextLine()) {
                try {

                    String line = scanner.nextLine();
                    String[] tokens = line.split(",");

                    // extract quaternion from csv
                    ar[0] = Double.parseDouble(tokens[0]);
                    ar[1] = Double.parseDouble(tokens[1]);
                    ar[2] = Double.parseDouble(tokens[2]);
                    ar[3] = Double.parseDouble(tokens[3]);

                    w = ar[0];
                    x = ar[1];
                    y = ar[2];
                    z = ar[3];


                    // convert Eular angles
                    roll  =   Math.atan2(2 * y * w - 2 * x * z, 1 - 2 * y * y - 2 * z * z);
                    pitch =   Math.atan2(2 * x * w - 2 * y * z, 1 - 2 * x * x - 2 * z * z);
                    yaw   =   Math.asin(2 * x * y + 2 * z * w);

                    // convert to degrees
                    roll  *= 57.295779513;
                    pitch *= 57.295779513;
                    yaw   *= 57.295779513;

                    // rotate cylinder according to roll, pitch yaw values
                    cylinder.setRotationAxis(Rotate.X_AXIS);
                    cylinder.setRotate(roll);

                    System.out.println(ar[0] + " " + ar[1] + " " + ar[2] + " " + ar[3]);
                }

                catch (Exception e)
                {
                    System.out.println("An error occurred");
                }
            }
        }



    public static void main(String[] args)
    {
        // Launch application
        Application.launch(args);
    }
}


