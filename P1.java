/**
 * BoxDemo.java - demonstrates very simple 3D rendering with GLSL.  *        It has a basic unit cube defined in 
 *        It has a only basic unit cube defined in Box and makes only 1 scene.
 *        
 * Keyboard interaction: 
 * 		p  - toggles between pre-defined perspective and orthographic views
 * 		l  - invokes polygon line drawing mode
 *      f  - invokes polygon fill drawing mode
 *      xX - rotation of scene about x-axis
 *      	 up key same as x (+x rotation)
 *      	 down key same as X (-x rotation)
 *      yY - rotation of scene about y-axis
 *      	 left key same as y (+y rotation)
 *      	 right key same as Y (-y rotation)
 *      zZ - rotation of scene about z-axis
 *      q,esc - quit program
 *
 * @author rdb
 * 02/07/17 version 1.0 derived from revised Basic3D
 *               
 * This program originally made use of code from demos found at lwjgl.org 
 * accessed as lwjgl3-demo-master and downloaded in late August 2015. 
 * It also uses a modified complete class from that package, UtilsLWJGL.
 * 
 * The shader was originally from Bailey and Cunningham, Graphics Shaders, 2e.
 */
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.joml.*;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.nio.*;
import java.io.*;
import java.util.ArrayList;
import org.lwjgl.system.MemoryUtil;

public class P1
{
    //---------------------- class variables -------------------------

    //---------------------- instance variables ----------------------
    // window size parameters
    int windowW = 600;
    int windowH = 640;

    // We need to strongly reference callback instances.
    private GLFWErrorCallback errorCallback;
    //--------------- Constructor ------------------------------------------
    /**
     * Constructor creates initial scene.
     *
     */
    public P1()
    {
        int shaderProgram = -1; // GLSL shader program id
        long window = -1;  // GLFW window id
        // Setup error callback to print to System.err.
        errorCallback = GLFWErrorCallback.createPrint( System.err ).set();

        window = UtilsLWJGL.openWindow( "BoxDemo", windowW, windowH );
        //glfwSetWindowTitle( window, "BOXES");
        // The next line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the ContextCapabilities instance and makes the OpenGL
        // bindings available for use.        
        GL.createCapabilities();

        try 
        {
            shaderProgram = UtilsLWJGL.makeShaderProgram( 
                    "P1.vsh", "P1.fsh" );
            glUseProgram( shaderProgram );  
        } 
        catch ( IOException iox )  
        {
            System.err.println( "Shader construction failed." );
            System.exit( -1 );
        }
        SceneManager manager = new SceneManager( window, shaderProgram );
        // Release window and window callbacks
        glfwFreeCallbacks( window );
        glfwDestroyWindow( window );
        glfwSetErrorCallback( null ).free(); // free old errorCallback
        glfwTerminate();
    }
    //------------------------- main ----------------------------------
    /**
     * main constructs the object
     */
    public static void main( String args[] )
    {
        P1 demo = new P1();
    }
}
