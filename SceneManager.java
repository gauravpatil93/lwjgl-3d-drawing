import static org.lwjgl.glfw.GLFW.*;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;
import static org.lwjgl.opengl.GL20.glUniform4fv;
import static org.lwjgl.opengl.GL20.glUniform3fv;
import static org.lwjgl.opengl.GL20.glUniform1f;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;

/**
 * SceneManager - create Scenes, manage scene displays, setup view and projection
 *  			matrices, handle interaction.
 *  
 * @author rdb
 * @modified Gaurav 02/23/17
 * 02/07/17
 */
public class SceneManager 
{
    //------------------- class variables ---------------------------
    //----- I've chosen to make the shaderProgram identifier a static "public"
    //      variable. This could become a problem for more complex programs 
    //      that use multiple shader programs.
    static int shaderProgram = -1;   // 
    //------------------- instance variables ---------------------------
    private long window;      // GLFW window id


    // storage for shapes created
    ArrayList<Shape3D> objects = new ArrayList<Shape3D>();

    // Variables related to scenes
    // Array list for storing different
    ArrayList<Scene> scenes = new ArrayList<Scene>();
    // A variable for scene number
    private int  current_scene = 0;
    String sceneTitle;


    // We need to reference callback instances.
    private GLFWKeyCallback   keyCallback;

    //-------------- View and Scene transformation parameters
    private Matrix4f projMatrix = new Matrix4f();
    private Matrix4f projXsceneMatrix = new Matrix4f();

    private Matrix4f viewMatrix = new Matrix4f();
    private Matrix4f sceneMatrix = new Matrix4f();
    // initial scene parameters will be used to build sceneMatrix
    private float radiansX = 0;
    private float radiansY = 0;
    private float radiansZ = 0;
    private float deltaRotate = 0.1f;
    private float sceneScale = 0.5f;

    private boolean usePerspective = false;

    // buffers
    private FloatBuffer projXsceneBuf;
    private FloatBuffer specularReflectionBufferOne = null;
    private FloatBuffer lightBufferOne = null;
    private FloatBuffer sceneLightColorBuffer = null;

    // Object of LightVariation
    LightVariationOne lightTypeOne = new LightVariationOne();
    LightVariationTwo lightTypeTwo = new LightVariationTwo();

    protected float [] lightPosition;
    protected int light_switch = 1;
    protected int scene_number;
    protected float ka;
    protected float kd;
    protected float ks;
    protected float global_switch = 1.0f;
    protected float [] sceneLightColor;

    //------------------- Constructor ------------------------------
    /**
     * Constructor manages everything.
     * 
     * @param windowId long    window identifier
     * @param shaderProgram int  identifier for the shader program
     */
    public SceneManager( long windowId, int shader )
    {
        window = windowId;	      // save it.
        shaderProgram = shader;   // save it

        projMatrix.scale( sceneScale ); //  initial matrix is uniform scale by 1/2
        projXsceneBuf = MemoryUtil.memAllocFloat( 16 );


        lightPosition = lightTypeOne.getLightPosition();


        setupKeyHandler();
        makeScene();

        this.objects = scenes.get(0).getShapesList();
        this.ka = scenes.get(0).getLightFactor();
        this.kd = scenes.get(0).getShapesList().get(0).getLightFactorKd();
        this.ks = scenes.get(0).getShapesList().get(0).getLightFactorKs();
        this.sceneLightColor = scenes.get(0).getSceneLight();
        glfwSetWindowTitle( window, scenes.get(0).getTitle());

        setupView();
        setupProjection();
        updateScene();
        renderLoop();        
    }
    //----------------------- finalize -------------------------------
    /**
     * finalize - needs to free the non-java-GC memory allocated
     *   by lwjgl MemoryUtil class
     */
    public void finalize()
    {
        MemoryUtil.memFree( projXsceneBuf ); 
    } 

    //------------------ makeScene --------------------------
    /**
     * Create the objects that make up the scene.
     */
    private void makeScene()
    {

        // --------------------------------------------------------------------------
        // Scene One
        // --------------------------------------------------------------------------
        Scene scene_one = new Scene();
        scene_one.setTitle( "Scene One - Demonstrates setRotate(a,x,y,z) and setRotate(matrix)");
        scene_one.setSceneNumber(0);
        scene_one.setLightFactor(0.50f);
        float [] scene_light_color_scene_1 = {1.0f,1.0f,1.0f};
        scene_one.setSceneLightColor(scene_light_color_scene_1);

        Box box_scene_1 = new Box();
        box_scene_1.setLocation( 0,  0,  0 );
        box_scene_1.setSize( 0.3f,  0.3f,  0.3f );
        box_scene_1.setRotate(170, 1, 0, 1);
        box_scene_1.setLightFactors(0.10f, 0.0f);
        scene_one.addShape( box_scene_1 );

        Matrix3f pure_rotation_matrix_one = new Matrix3f();
        pure_rotation_matrix_one.m00 = 0.36f;
        pure_rotation_matrix_one.m01 = 0.48f;
        pure_rotation_matrix_one.m02 = -0.8f;
        pure_rotation_matrix_one.m10 = -0.8f;
        pure_rotation_matrix_one.m11 = 0.60f;
        pure_rotation_matrix_one.m12 = 0;
        pure_rotation_matrix_one.m20 = 0.48f;
        pure_rotation_matrix_one.m21 = 0.64f;
        pure_rotation_matrix_one.m22 = 0.60f;

        Box box_scene_1_2 = new Box();
        box_scene_1_2.setLocation( 1.3f,  1.3f,  1.3f );
        box_scene_1_2.setSize( 0.1f,  0.1f,  0.1f );
        box_scene_1_2.setRotate(pure_rotation_matrix_one);
        box_scene_1_2.setLightFactors(0.10f, 0.0f);
        scene_one.addShape( box_scene_1_2 );

        Pyramid pym_scene_1_3 = new Pyramid();
        pym_scene_1_3.setLocation(-2.0f, 2.0f, 2.0f);
        pym_scene_1_3.setSize(0.1f, 0.1f, 0.1f);
        pym_scene_1_3.setRotate(pure_rotation_matrix_one);
        pym_scene_1_3.setLightFactors(0.10f, 0.0f);
        scene_one.addShape(pym_scene_1_3);

        scenes.add( scene_one ); // Add scene one to the ScenesList


        // --------------------------------------------------------------------------
        // Scene Two
        // --------------------------------------------------------------------------
        Scene scene_two = new Scene();
        scene_two.setTitle( "Scene Two - Demonstrates non-default constructors and facecoloring options" );
        scene_two.setSceneNumber(1);
        scene_two.setLightFactor(0.20f);
        float [] scene_light_color_scene_2 = {1.0f,1.0f,1.0f};
        scene_two.setSceneLightColor(scene_light_color_scene_2);

        Box box_scene_2 = new Box(3);
        box_scene_2.setLocation( -0.5f,  -0.5f,  -0.5f );
        box_scene_2.setSize( 0.1f,  0.1f,  0.1f );
        box_scene_2.setRotate(120, 1, 0, 1);
        box_scene_2.setLightFactors(0.10f, 0.0f);
        scene_two.addShape( box_scene_2 );

        Box box_scene_2_2 = new Box(1);
        box_scene_2_2.setLocation( 1.5f,  -1.5f,  -1.5f );
        box_scene_2_2.setSize( 0.2f,  0.2f,  0.2f );
        box_scene_2_2.setRotate(140, 1, 0, 1);
        box_scene_2_2.setLightFactors(0.10f, 0.0f);
        scene_two.addShape( box_scene_2_2 );

        Pyramid pym_scene_2= new Pyramid(3);
        pym_scene_2.setLocation( -0.5f, 0.5f, 0 );
        pym_scene_2.setSize(  0.5f, 0.25f,  0.25f );
        pym_scene_2.setLightFactors(0.10f, 0.0f);
        scene_two.addShape( pym_scene_2 );

        scenes.add( scene_two );


        // --------------------------------------------------------------------------
        // Scene Three
        // --------------------------------------------------------------------------
        Scene scene_three = new Scene();
        scene_three.setTitle( "SceneThree-Demonstrates the effect of Ka, Kd, Ks (Try doing < > to see difference)" );
        scene_three.setSceneNumber(2);
        scene_three.setLightFactor(0.70f);
        float [] scene_light_color_scene_3 = {1.0f,1.0f,1.0f};
        scene_three.setSceneLightColor(scene_light_color_scene_3);

        Box box_scene_3 = new Box(1);
        box_scene_3.setLocation( 1.5f,  -1.5f,  -1.5f );
        box_scene_3.setSize( 0.2f,  0.2f,  0.2f );
        box_scene_3.setRotate(140, 1, 0, 1);
        box_scene_3.setLightFactors(0.20f, 0.0f);
        scene_three.addShape( box_scene_3 );

        Pyramid pym_scene_3= new Pyramid(3);
        pym_scene_3.setLocation( -0.5f, 0.5f, 0 );
        pym_scene_3.setSize(  0.5f, 0.25f,  0.25f );
        pym_scene_3.setLightFactors(0.20f, 0.0f);
        scene_three.addShape( pym_scene_3 );

        scenes.add( scene_three );


        // --------------------------------------------------------------------------
        // Scene Four
        // --------------------------------------------------------------------------
        Scene scene_four = new Scene();
        scene_four.setTitle( "Scene Four - Demonstrates the object in Yellow light" );
        scene_four.setSceneNumber(3);
        scene_four.setLightFactor(0.85f);
        float [] scene_light_color_scene_4 = {1.0f, 1.0f, 0};
        scene_four.setSceneLightColor(scene_light_color_scene_4);

        Pyramid pym_scene_4 = new Pyramid();
        pym_scene_4.setLocation( -0.5f, 0.5f, 0 );
        pym_scene_4.setSize(  0.5f, 0.25f,  0.25f );
        pym_scene_4.setLightFactors(0.20f, 0.0f);
        scene_four.addShape( pym_scene_4 );

        scenes.add( scene_four );

        // --------------------------------------------------------------------------
        // Scene Five
        // --------------------------------------------------------------------------
        Scene scene_five = new Scene();
        scene_five.setTitle( "Scene Five - Direction of green light test (Not all vertices get equal light) " );
        scene_five.setSceneNumber(4);
        scene_five.setLightFactor(0.99f);
        float [] scene_light_color_scene_5 = {0.0f, 1.0f, 0.0f};
        scene_five.setSceneLightColor(scene_light_color_scene_5);

        Box box_scene_5 = new Box(true);
        box_scene_5.setLocation( 0.0f,  0.0f,  -0.0f );
        box_scene_5.setSize( 0.3f,  0.3f,  0.3f );
        box_scene_5.setRotate(140, 1, 0, 1);
        box_scene_5.setLightFactors(0.0f, 0.0f);
        scene_five.addShape( box_scene_5 );

        scenes.add( scene_five );


    }
    //-------------------------- renderLoop ----------------------------
    /**
     * Loop until user closes the window or kills the program.
     */
    private void renderLoop() 
    {
        GL11.glClearColor( 0.0f, 0.0f, 0.0f, 0.0f );
        glEnable(GL_DEPTH_TEST);
        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.
        while ( glfwWindowShouldClose( window ) == false )
        {
            // clear the framebuffer
            glClear( GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT ); 

            // redraw the frame
            redraw();

            glfwSwapBuffers( window ); // swap the color buffers

            // Wait for window events. The key callback above will only be
            // invoked during this call.
            // lwjgl demos use glfwPollEvents(), which uses nearly 2X
            //    the cpu time for simple demos as glfwWaitEvents.
            glfwWaitEvents();
        }
    }

    //--------------------- setupView ----------------------
    /**
     * Set up default View specification using lookAt
     */
    private void setupView()
    {
        Vector3f eye    = new Vector3f( 0, 0, 1 );
        Vector3f center = new Vector3f( 0, 0, 0 );
        Vector3f up     = new Vector3f( 0, 1, 0 );        
        viewMatrix.lookAt( eye, center, up );        
    }
    //--------------------- setupProjection ----------------------
    /**
     * Define the projection parameters; this is needed because defaults
     *     are very unfriendly, particularly the near/far clipping parameters.
     */
    private void setupProjection()
    {
        //--------  ortho parameters
        float   left  = -1;    float   right = 1;
        float   bottom = -1;   float   top    = 1;
        float   nearZ  = 0.1f; float   farZ   = 10;
        //-------- perspective parameters
        float	fovy = 90;     float   aspect = 1;

        projMatrix.identity();
        if ( usePerspective )
            projMatrix.perspective( fovy, aspect, nearZ, farZ );
        else
            projMatrix.ortho( left, right, bottom, top, nearZ, farZ );                   	
    }        

    //------------------ updateTransforms --------------------------
    /**
     * We have a constant viewing and projection specification.
     *   Can define it once and send the spec to the shader.
     */
    void updateTransforms()
    {   
        //----- compute the composite 
        projXsceneMatrix.set( projMatrix );
        projXsceneMatrix.mul( viewMatrix );
        projXsceneMatrix.mul( sceneMatrix );  // multiply by scene

        // get stores this matrix into buffer parameter and returns buffer
        projXsceneBuf = projXsceneMatrix.get( projXsceneBuf );

        float[] specularArray = lightTypeOne.getSpecularReflection();
        specularReflectionBufferOne = BufferUtils.createFloatBuffer( specularArray.length );
        specularReflectionBufferOne.put( specularArray ).flip();

        int _specular = glGetUniformLocation( shaderProgram, "specular" );
        glUniform4fv( _specular , specularReflectionBufferOne );

        float[] lightPositionArray = lightTypeOne.getLightPosition();
        lightBufferOne = BufferUtils.createFloatBuffer( lightPositionArray.length );
        lightBufferOne.put( lightPositionArray ).flip();

        int _lightposition = glGetUniformLocation( shaderProgram, "lightposititon" );
        glUniform4fv( _lightposition, lightBufferOne );

        float shineFactor = lightTypeOne.getShineFactor();
        int _shininess = glGetUniformLocation( shaderProgram, "shininess" );
        glUniform1f(_shininess, shineFactor);


        sceneLightColorBuffer = BufferUtils.createFloatBuffer( this.sceneLightColor.length );
        sceneLightColorBuffer.put( this.sceneLightColor ).flip();
        int _lightcolor = glGetUniformLocation( shaderProgram, "lightcolor");
        glUniform3fv(_lightcolor, sceneLightColorBuffer);


        float lightfactka = this.ka;
        int _lightfactorka = glGetUniformLocation ( shaderProgram, "ka");
        glUniform1f(_lightfactorka, lightfactka);

        float lightfactkd =  this.kd;
        int _lightfactorkd = glGetUniformLocation( shaderProgram, "kd");
        glUniform1f(_lightfactorkd, lightfactkd);

        //--- now push the composite into a uniform var in vertex shader
        //  this id does not need to be global since we never change 
        //  projection or viewing specs in this program.
        int unif_pXv = glGetUniformLocation( shaderProgram, "projXview" );

        glUniformMatrix4fv( unif_pXv, false, projXsceneBuf );
    }
    //------------------ updateScene --------------------------
    /**
     * We have a constant viewing and projection specification.
     *   Can define it once and send the spec to the shader.
     */
    void updateScene()
    {   
        sceneMatrix.identity();
        sceneMatrix.rotateX( radiansX )
        .rotateY( radiansY )
        .rotateZ( radiansZ );
        updateTransforms();
    }
    //------------------------ redraw() ----------------------------
    void redraw()
    {
        GL11.glClear( GL11.GL_COLOR_BUFFER_BIT );
        for ( Shape3D obj: objects )
            obj.redraw();

        glFlush();
    }
    //--------------------- setupKeyHandler ----------------------
    /**
     * void setupKeyHandler
     */
    private void setupKeyHandler()
    {
        // Setup a key callback. It is called every time a key is pressed, 
        //      repeated or released.
        glfwSetKeyCallback( window, 
                keyCallback = new GLFWKeyCallback()
        {
            @Override
            public void invoke( long keyWindow, int key, 
                    int scancode, int action, int mods )
            {
                keyHandler( keyWindow, key, scancode, action, mods );
            }
        });
    }
    //--------------------- keyHandler ---------------------------
    /**
     * Make this a full-fledged method called from the invoke method of
     *    the anonymous class created in setupKeyHandler.
     * @param long window window Id
     * @param int  key    key code
     * @param int  code   "scancode" is low-level non-standard internal code
     * @param int  action GLFW_PRESS or GLFW_RELEASE
     * @param int  mods   bits in int encode modifier keys pressed
     *                    GLFW_MOD_ALT | GLFW_MOD_SHIFT | GLFW_MOD_CONTROL
     *                    | GLFW_MOD_SUPER (cmd on mac)
     */
    private void keyHandler( long window, int key, int code, int action, int mods )
    {
        switch ( key )
        {
        //------------ Perspective/Parallel projection toggle  -----------------
        case GLFW_KEY_P:
            if ( action == GLFW_RELEASE ) // use release so user can change mind
                usePerspective = !usePerspective;
            setupProjection();
            updateTransforms();
            break; 
            //------------ Polygon Line draw mode ---------------------------
        case GLFW_KEY_L:
            if ( action == GLFW_RELEASE ) // use release so user can change mind
                glPolygonMode( GL_FRONT_AND_BACK, GL_LINE ); 
            break; 
            //------------ Polygon fill draw mode ---------------------------
        case GLFW_KEY_F:
            if ( action == GLFW_RELEASE ) // use release so user can change mind
                glPolygonMode( GL_FRONT_AND_BACK, GL_FILL );
            break; 
            //------------ Exit program -------------------
            // Either q or Esc keys quit
        case GLFW_KEY_Q:
        case GLFW_KEY_ESCAPE:
            // this is another exit key
            if ( action == GLFW_RELEASE ) // use release so user can change mind
                glfwSetWindowShouldClose( window, true );
            break;
            //----------- any other keys must be rotation keys or invalid
        default: rotationKeyHandler( key, action, mods );
        }
    }

    //--------------------- rotationKeyHandler ---------------------------
    /**
     * Handle key events that specify rotations.
     * 
     * @param int  key    key code
     * @param int  action GLFW_PRESS or GLFW_RELEASE
     * @param int  mods   bits in int encode modifier keys pressed
     *                    GLFW_MOD_ALT | GLFW_MOD_SHIFT | GLFW_MOD_CONTROL
     *                    | GLFW_MOD_SUPER (cmd on mac)
     */
    private void rotationKeyHandler( int key, int action, int mods )
    {
        switch ( key )
        {
        //------------ Rotations about X axis -------------------
        // Use x, X and UP DOWN keys
        case GLFW_KEY_X:             
            if ( action == GLFW.GLFW_REPEAT || action == GLFW.GLFW_PRESS )
            {
                if (( GLFW_MOD_SHIFT & mods ) == 0 ) // it's lower case
                    radiansX += deltaRotate;
                else 
                    radiansX -= deltaRotate;
                updateScene();
            }
            break;
        case GLFW_KEY_UP:             
            if ( action == GLFW.GLFW_REPEAT || action == GLFW.GLFW_PRESS )
            {
                radiansX += deltaRotate;
                updateScene();
            }
            break;
        case GLFW_KEY_DOWN:     
            if ( action == GLFW.GLFW_REPEAT || action == GLFW.GLFW_PRESS )
            {
                radiansX -= deltaRotate;
                updateScene();
            }
            break;
            //------------ Rotations about Y axis -------------------
            // Use y, Y and RIGHT, LEFT keys
        case GLFW_KEY_Y:             
            if ( action == GLFW.GLFW_REPEAT || action == GLFW.GLFW_PRESS )
            {
                if (( GLFW_MOD_SHIFT & mods ) == 0 ) // it's lower case
                    radiansY += deltaRotate;
                else 
                    radiansY -= deltaRotate;
                updateScene();
            }
            break;
        case GLFW_KEY_RIGHT:             
            if ( action == GLFW.GLFW_REPEAT || action == GLFW.GLFW_PRESS )
            {
                radiansY += deltaRotate;
                updateScene();
            }
            break;
        case GLFW_KEY_LEFT:             
            if ( action == GLFW.GLFW_REPEAT || action == GLFW.GLFW_PRESS )
            {
                radiansY -= deltaRotate;
                updateScene();
            }
            break;
            //------------ Rotations about Z axis -------------------
            // Only have z and Z keys
        case GLFW_KEY_Z:             
            if ( action == GLFW.GLFW_REPEAT || action == GLFW.GLFW_PRESS )
            {
                if (( GLFW_MOD_SHIFT & mods ) == 0 ) // it's lower case
                    radiansZ += deltaRotate;
                else 
                    radiansZ -= deltaRotate;
                updateScene();
            }
            break;
            //----------- any other keys must be for implemented methods
        default: customKeyHandler( key, action, mods );
        }
    }


    private void customKeyHandler( int key, int action, int mods )
    {
        switch ( key )
        {
        //------------ Changing Scenes -------------------
        // <> keys ',' and '.'
        case GLFW_KEY_COMMA:
            if ( action == GLFW_RELEASE ) {// use release so user can change mind
                updateScene();
                if ( current_scene > 0 ) {
                    --current_scene;
                    this.sceneTitle = scenes.get( current_scene ).getTitle();
                    this.ka = scenes.get( current_scene ).getLightFactor();
                    this.objects = scenes.get( current_scene ).getShapesList();
                    this.kd = objects.get(0).getLightFactorKd();
                    this.ks = objects.get(0).getLightFactorKs();
                    this.sceneLightColor = scenes.get( current_scene ).getSceneLight();
                    for (int i = 0; i < this.sceneLightColor.length; i++) {
                        this.sceneLightColor[i] = this.sceneLightColor[i] * this.global_switch;
                    }
                    glfwSetWindowTitle( window, sceneTitle );
                    glEnable(GL_DEPTH_TEST);
                    renderLoop();
                }
                else {
                    current_scene = 4;
                    this.sceneTitle = scenes.get( current_scene ).getTitle();
                    this.ka = scenes.get( current_scene ).getLightFactor();
                    this.objects = scenes.get( current_scene ).getShapesList();
                    this.kd = this.objects.get(0).getLightFactorKd();
                    this.ks = this.objects.get(0).getLightFactorKs();
                    this.sceneLightColor = scenes.get( current_scene ).getSceneLight();
                    for (int i = 0; i < this.sceneLightColor.length; i++) {
                        this.sceneLightColor[i] = this.sceneLightColor[i] * this.global_switch;
                    }
                    glfwSetWindowTitle( window, sceneTitle );
                    glEnable(GL_DEPTH_TEST);
                    renderLoop();
                }
            }
            break;
        case GLFW_KEY_PERIOD:
            if ( action == GLFW_RELEASE ) {
                updateScene();
                if ( current_scene < 4 ) {
                    current_scene++;
                    this.sceneTitle = scenes.get( current_scene ).getTitle();
                    this.ka = scenes.get( current_scene ).getLightFactor();
                    this.objects = scenes.get( current_scene ).getShapesList();
                    this.kd = objects.get(0).getLightFactorKd();
                    this.ks = objects.get(0).getLightFactorKs();
                    this.sceneLightColor = scenes.get( current_scene ).getSceneLight();
                    for (int i = 0; i < this.sceneLightColor.length; i++) {
                        this.sceneLightColor[i] = this.sceneLightColor[i] * this.global_switch;
                    }
                    glfwSetWindowTitle( window, sceneTitle );
                    glEnable(GL_DEPTH_TEST);
                    renderLoop();
                }
                else {
                    current_scene = 0;
                    //current_scene++;
                    this.sceneTitle = scenes.get( current_scene ).getTitle();
                    this.scene_number = scenes.get( current_scene ).getSceneNumber();
                    this.ka = scenes.get( scene_number ).getLightFactor();
                    glfwSetWindowTitle( window, sceneTitle );
                    this.objects = scenes.get( current_scene ).getShapesList();
                    this.kd = objects.get(0).getLightFactorKd();
                    this.ks = objects.get(0).getLightFactorKs();
                    this.sceneLightColor = scenes.get( current_scene ).getSceneLight();
                    for (int i = 0; i < this.sceneLightColor.length; i++) {
                        this.sceneLightColor[i] = this.sceneLightColor[i] * this.global_switch;
                    }
                    glfwSetWindowTitle( window, sceneTitle );
                    glEnable(GL_DEPTH_TEST);
                    renderLoop();
                }
            }
            break;
        case GLFW_KEY_0:
            System.out.println(this.light_switch);
            if ( action == GLFW_RELEASE ) {
                if (light_switch == 1) {
                    for ( int i = 0; i < this.sceneLightColor.length; i++) {
                        this.sceneLightColor[i] = 0.0f ;                     
                    }
                    this.light_switch = 0;
                }
                else {
                    for ( int i = 0; i < this.sceneLightColor.length; i++) {
                        this.sceneLightColor[i] = 1.0f;
                    }
                    this.light_switch = 1;
                }
            }
            updateScene();
            break;
        case GLFW_KEY_1:
            if ( action == GLFW_RELEASE ) {
                if (light_switch == 1) {
                    for ( int i = 0; i < this.sceneLightColor.length; i++) {
                        this.sceneLightColor[i] = 0.0f * this.global_switch;                     
                    }
                    this.light_switch = 0;
                }
                else {
                    for ( int i = 0; i < this.sceneLightColor.length; i++) {
                        this.sceneLightColor[i] = 1.0f * this.global_switch;
                    }
                    this.light_switch = 1;
                }
            }
            updateScene();
            break;
        case GLFW_KEY_2:
            if ( action == GLFW_RELEASE ) {
                if ( global_switch == 1) {
                    this.global_switch = 0.0f;
                    for (int i = 0; i < this.sceneLightColor.length; i++) {
                        this.sceneLightColor[i] = this.sceneLightColor[i] * this.global_switch;
                    }
                }
                else {
                    this.global_switch = 1.0f;
                    for (int i = 0; i < this.sceneLightColor.length; i++) {
                        this.sceneLightColor[i] = 1.0f;
                    }
                }

            }
            updateScene();
            break;
        }
    }
}
