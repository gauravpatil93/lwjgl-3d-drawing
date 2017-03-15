/**
 * Shape3D.java - an abstract class representing an OpenGL graphical object
 *
 * 10/16/13 rdb derived from Shape3D.cpp
 * 09/28/15 rdb Revised for lwjgl              
 # 12/28/16 rdb Revised to better match lwjgl3.1, esp. use of MemoryUtil
 *              Revised to match interface to other Shape3D demo classes
 *              Replaced java.math with joml
 * 02/05/16 rdb Refactored to do as much here for children as possible.
 */
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glGetAttribLocation;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniform1f;
import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.opengl.GL20.glUniform4fv;
import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import org.joml.AxisAngle4d;
import org.joml.AxisAngle4f;
import java.lang.Math;
import java.nio.*;

import org.lwjgl.system.MemoryUtil;
import org.joml.*;

abstract public class Shape3D
{
    //------------------ class variables ------------------------------
    static final int MAX_COLORS = 6;   // arbitrary number change it if need more

    private boolean     buffersNeeded = true;

    //---------------------- instance variables ------------------------------
    //------ vertex data and variables
    private   FloatBuffer coordBuffer = null;
    private   int         nCoords = 0;
    protected int         coordSize = 0;  // 3 for xyz, 4 for xyzw

    private   FloatBuffer normalBuffer = null;
    private   int         nNormals = 0;
    protected int         normalSize = 0;  // 3 for xyz, 4 for xyzw

    private   FloatBuffer colorBuffer = null;
    private   int         nColors = 0;
    private   int         colorSize = 0;

    //------------------ GLSL-related instance variables ----------------------------
    // these uniform variable specs need not be used by every object or
    //    every shader program; but it makes sense for the class of 
    //    objects to share their uniform variables.
    //
    protected static int unif_model = -1;  // uniform id for model matrix
    protected static int unif_color = -1;  // uniform id for color value
    private int         shaderPgm    = -1;
    private FloatBuffer posBuffer    = null;
    private int         vaoId        = -1;
    private int         coordVBO      = -1;
    private int         vattrib_vPos  = -1;
    private int         normalVBO      = -1;    // buffer for normals
    private int         vattrib_normal  = -1;
    // Color VBO and vattrib_vCol
    private int         colorVBO        = -1;
    private int         vattrib_vCol  = -1;

    private int         nTriangles = -1;
    private int         nVertices = -1;   // #vertices in buffer (3*nTriangles)

    //------------------ object instance variables ----------------------------
    //----- transformation variables
    protected float xLoc, yLoc, zLoc;        // location (origin) of object
    protected float xSize, ySize, zSize;     ///get size of the object
    protected float angle, dxRot, dyRot, dzRot; // rotation angle and axis

    protected Matrix3f rotation_matrix = new Matrix3f(); 

    protected Color[]       colors = new Color[ MAX_COLORS ];    
    protected FloatBuffer[] colorBufs = new FloatBuffer[ MAX_COLORS ];

    //------------------ object Light Factors ----------------------------

    public float kd, ks;

    //------------------ Constructors ----------------------------------
    /**
     * Create a new object3D at position 0,0,0 of size 1,1,1
     */
    public Shape3D()
    {
        for ( int i = 0; i < colors.length; i ++  ) // fill arrays with null
        {
            colors[ i ] = null;
            colorBufs[ i ] = null;
        }
        setColor( 1, 0, 0 );
        setLocation( 0, 0, 0 );
        setSize( 1, 1, 1 );

        // ------------- Setup GLSL interface variables -------------
        shaderPgm  = SceneManager.shaderProgram; 

        // uniform variables needed by Shapes
        unif_model  = glGetUniformLocation( shaderPgm, "uModel" );
        unif_color  = glGetUniformLocation( shaderPgm, "uColor" );
    }
    //------------------- finalize() ------------------------------------
    /**
     * This method gets called whenever an its object is "garbage-collected";
     * It's similar to C++ destructor, except with Java we don't have to 
     * explicitly destruct stuff that Java knows about. 
     * 
     * However, Java does NOT know about the memory we allocated via 
     * the MemoryUtil class, so we should free up that memory.
     * 
     * In reality, it doesn't matter for this demo program because we 
     * never stop using any Shape3D objects we create until the program
     * terminates -- then it doesn't matter. Even so, it's important
     * to understand the implications of using MemoryUtil, so we will
     * clean up.
     */
    public void finalize()
    {
        MemoryUtil.memFree( coordBuffer ); 
        MemoryUtil.memFree( normalBuffer ); 
        MemoryUtil.memFree( colorBuffer );
        for ( FloatBuffer buf: colorBufs )
        {
            if ( buf != null )
                MemoryUtil.memFree( buf ); 
        }
    }    
    //------------------ public methods -------------------------------
    //------------------------- redraw ----------------------------
    /**
     * Update the specifications for the shape. The parent class does
     *   transformation setup and the uniform variable settings expected
     *   of all children. The child redraw() must call this first.
     */
    protected void redraw()
    {
        // Simple modeling: only size and location. 
        //   we can write down the desired matrix.
        // This specification and the FloatBuffer we create does NOT have
        //   to be done on every re-draw; it only needs to be done when
        //   the location or size changes: setLocation or setSize.
        // The model matrix and modelBuf should be instance variables.
        //
        Matrix4f model = new Matrix4f();
        model = model.scale(xSize, ySize, zSize); // Scale
        model = model.rotate(angle, dxRot, dyRot, dzRot); // Rotate
        model = model.translate(xLoc, yLoc, zLoc); // Translate

        if ( buffersNeeded )
        {
            loadBuffers();
            buffersNeeded = false;
        }
        // MemoryUtil is an lwjgl utility class
        FloatBuffer modelBuf = MemoryUtil.memAllocFloat( 16 );
        model.get(modelBuf);
        //modelBuf.put( model ).flip();

        glUniformMatrix4fv( unif_model, false, modelBuf );
        MemoryUtil.memFree( modelBuf );

        glUniform4fv( unif_color, colors[ 0 ].get4f() ); // update color uniform

        // identify which VAO specification needs to be drawn.
        glBindVertexArray( vaoId );

        // last parameter is # vertexes defining the triangles
        //      verts.length is the number of floats in the verts array, which
        //      is 4 * the number of triangles
        glDrawArrays( GL_TRIANGLES, 0, nVertices );

        // unbind the vao, we are done with it for now.
        glBindVertexArray( 0 );
    }

    //---------------------- loadBuffers() --------------------------
    /**
     *  Download the data buffers related to vertex coordinates.
     *  This version has separate buffers for all attributes.
     */
    protected void loadBuffers() 
    {
        UtilsLWJGL.glError( "--->loadBuffers" );  // clean out any old errors
        // Create a vertex array object if needed
        if ( this.vaoId == -1 )
            this.vaoId = glGenVertexArrays();
        glBindVertexArray( vaoId );  // binding => this VAO is "current" one

        // build the non-color buffers
        loadCoordBuffer();
        loadNormalsBuffer();
        loadColorsBuffer();

        glBindVertexArray( 0 ); // unbind the VAO
        UtilsLWJGL.glError( "<--loadBuffers" );
    }
    //---------------------- loadCoordBuffer() --------------------------
    /**
     * Build VBO for Coordinate data and download to GPU.
     *  Assume the appropriate VAO is currently bound!
     */
    protected void loadCoordBuffer() 
    {
        UtilsLWJGL.glError( "--->loadCoordBuffer" );  // clean out any old errors
        // Could make vertexBuffer static and only allocate first time.

        // ---- set up to transfer points to gpu
        // 2. Create vertex buffer and vertex attribute pointer for positions
        // 2a. Create a vertex buffer for the position data  
        if ( this.coordVBO == -1 )
            this.coordVBO = glGenBuffers();
        // make it the current buffer
        glBindBuffer( GL_ARRAY_BUFFER, this.coordVBO ); 
        // fill it with the position data from the posBuffer

        glBufferData( GL_ARRAY_BUFFER, coordBuffer, GL_STATIC_DRAW );

        // 2b. Create vertex attribute for vertex position
        //    define a shader variable, "vPosition"
        //    Note: could use predefined locations glBindAttrLocation
        if ( vattrib_vPos == -1 )
            vattrib_vPos = glGetAttribLocation( shaderPgm, "vPosition" );
        glEnableVertexAttribArray( vattrib_vPos ); 

        // describe how vPosition data can be found in the current buffer
        glVertexAttribPointer( vattrib_vPos, coordSize, GL_FLOAT, false, 0, 0L );

        UtilsLWJGL.glError( "<---loadCoordBuffer" );  // report error since enter

    }    
    //---------------------- loadNormalsBuffer() --------------------------
    /**
     * If normals used for this object, build VBOs for normal data;     
     *  Assume the appropriate VAO is currently bound.
     */
    protected void loadNormalsBuffer() 
    {
        if ( nNormals == 0 ) 
            return;
        UtilsLWJGL.glError( "--->loadNormalsBuffer" );  // clean out any old errors
        // Should make normal coord buffer static for each subclass and only 
        //     allocate 1 for all instances

        // Create a vertex buffer for the normal data  
        if ( this.normalVBO == -1 )
            this.normalVBO = glGenBuffers();
        // make it the current buffer
        glBindBuffer( GL_ARRAY_BUFFER, this.normalVBO ); 
        // fill it with the position data from the posBuffer
        glBufferData( GL_ARRAY_BUFFER, normalBuffer, GL_STATIC_DRAW );
        UtilsLWJGL.glError( "glBufferData" );  

        // 2b. Create texture coord attribute for texture coordinates 
        //    define a shader variable, "vTexCoord"
        //    Note: could use predefined locations glBindAttrLocation
        if ( vattrib_normal == -1 ) 
            vattrib_normal = glGetAttribLocation( shaderPgm, "vNormal" );
        UtilsLWJGL.glError( "glGetAttribLocation" );  
        glEnableVertexAttribArray( vattrib_normal ); 
        UtilsLWJGL.glError( "EnableVertexAttribArray" );  

        // describe how vPosition data can be found in the current buffer
        glVertexAttribPointer( vattrib_normal, normalSize, GL_FLOAT, false, 0, 0L ); 

        UtilsLWJGL.glError( "<---loadNormalsBuffer" );  // report error since enter
    }
    //---------------------- loadColorsBuffer() --------------------------

    protected void loadColorsBuffer() 
    {
        UtilsLWJGL.glError( "--->loadColorsBuffer" );  // clean out any old errors
        // Could make vertexBuffer static and only allocate first time.

        // ---- set up to transfer points to gpu
        // 2. Create vertex buffer and vertex attribute pointer for positions
        // 2a. Create a vertex buffer for the position data  
        if ( this.colorVBO == -1 )
            this.colorVBO = glGenBuffers();
        // make it the current buffer
        glBindBuffer( GL_ARRAY_BUFFER, this.colorVBO ); 
        // fill it with the position data from the posBuffer

        glBufferData( GL_ARRAY_BUFFER, colorBuffer, GL_STATIC_DRAW );

        // 2b. Create vertex attribute for vertex position
        //    define a shader variable, "vPosition"
        //    Note: could use predefined locations glBindAttrLocation
        if ( vattrib_vCol == -1 )
            vattrib_vCol = glGetAttribLocation( shaderPgm, "vColor" );
        glEnableVertexAttribArray( vattrib_vCol ); 

        // describe how vPosition data can be found in the current buffer
        glVertexAttribPointer( vattrib_vCol, colorSize, GL_FLOAT, false, 0, 0L );

        UtilsLWJGL.glError( "<---loadColorBuffer" );  // report error since enter
    } 


    //++++++++++++++++++++++ public methods ++++++++++++++++++++++++++++++=
    //---------------------- setCoordData ----------------------------------
    /**
     * Specify the vertex coordinate information for this object.
     *   Create and save a FloatBuffer with the information.
     * A null first parameter discards previous vertex coord data
     *
     * @param coords float[]  array of coord positions to
     *                      associate with each vertex.
     *                      If null is specified, deletes previous coords
     * @param floatsPerVertex int  # coord components (2 or 3)
     */
    protected void setCoordData( float [] coords, int floatsPerVertex )
    {
        if ( floatsPerVertex < 2 || floatsPerVertex > 4 ) // only 2-4 are valid
        {
            throw new RuntimeException( "setCoordData: size invalid: "
                    + floatsPerVertex + ". Only 2, 3 or 4 are valid values" );
        }
        // Could make buffer static and only allocate first time.
        if ( coords == null )  // Unused feature for (temporarily) emptying a shape
        {
            MemoryUtil.memFree( coordBuffer );
            coordBuffer = null;
            coordSize = 0;
            nCoords = 0;
            nVertices = 0;
            nTriangles = 0;
        }
        else
        {
            coordBuffer = MemoryUtil.memRealloc( coordBuffer, coords.length );
            coordSize = floatsPerVertex;
            nVertices = coords.length / coordSize;
            nTriangles = nVertices / 3;
            nCoords = coords.length / floatsPerVertex;
            coordBuffer.put( coords ).flip();
        }
    }    
    //---------------------- setNormalData ----------------------------------
    /**
     * Specify the vertex normal coord information for this object.
     *   Create and save a FloatBuffer with the information.
     * A null first parameter discards previous normal data
     *
     * @param normals float[]  array of coord positions to
     *                      associate with each vertex.
     *                      If null is specified, deletes previous coords
     */
    protected void setNormalData( float [] normals, int floatsPerVertex )
    {
        // Could make buffer static and only allocate first time.
        if ( normals == null )
        {
            MemoryUtil.memFree( normalBuffer );
            normalBuffer = null;
            normalSize = 0;
            nNormals = 0;
        }
        else
        {
            normalBuffer = MemoryUtil.memRealloc( normalBuffer, normals.length );
            normalSize = floatsPerVertex;
            nNormals = normals.length / floatsPerVertex;
            normalBuffer.put( normals ).flip();
        }
    }

    //---------------------- setColorData --------------------------------------
    protected void setColorData( float [] colors, int floatsPerVertex )
    {
        if ( colors == null )  // Unused feature for (temporarily) emptying a shape
        {
            MemoryUtil.memFree( colorBuffer );
            colorBuffer = null;
            colorSize = 0;
            nColors = 0;
        }
        else
        {
            colorBuffer = MemoryUtil.memRealloc( colorBuffer, colors.length );
            colorSize = floatsPerVertex;
            nColors = colors.length / floatsPerVertex;
            colorBuffer.put( colors ).flip();
        }
    }  


    //----------------------- get/setLocation --------------------------------
    /**
     * set location to the x,y,z position defined by the args
	 @param x float   x coordinate
	 @param y float   y coord
	 @param z float   z coord
     */
    public void setLocation( float x, float y, float z )
    {
        xLoc = x;
        yLoc = y;
        zLoc = z;
    }
    /**
     * return the value of the x origin of the shape
     * @return float 
     */
    public float getX()
    {
        return xLoc;
    }
    /**
     * return the value of the y origin of the shape
     * @return float 
     */
    public float getY()
    {
        return yLoc;
    }
    /**
     * return the value of the z origin of the shape
     * @return float 
     */
    public float getZ()
    {
        return zLoc;
    }
    /**
     * return the location as a Point3f object
     * @return Vector3f  location as a vector3 
     */
    public Vector3f getLocation()                // return location as a Point
    {
        return new Vector3f( xLoc, yLoc, zLoc );
    }
    //----------------------- get/setColor methods ---------------------------
    /**
     * return the base color of the object
     * @return Color
     */
    public Color getColor()                // return color 0
    {
        return colors[ 0 ];
    }    
    /**
     * return any color of the object
     * @param i   index of color to retrieve
     * @return Color
     */
    public Color getColor( int i )        // return color i
    {
        if ( i >= 0 && i < MAX_COLORS )
            return colors[ i ];
        else
            return null;   // should throw exception
    }

    /**
     * set the "nominal" color of the object to the specified color; this
     *   does not require that ALL components of the object must be the same
     *   color. Typically, the largest component will take on this color,
     *   but the decision is made by the child class.
     * @param c Color  the color
     */
    public void setColor( Color c )
    {
        setColor( 0, c );
    }

    /**
     * set the nominal color (index 0) to the specified color with floats
     * @param r float  red component
     * @param g float  green component
     * @param b float  blue component
     */
    public void setColor( float r, float g, float b )
    {
        setColor( 0, r, g, b, 1 );
    }

    /**
     * set the nominal color (index 0) to the specified color with floats
     * @param r float  red component
     * @param g float  green component
     * @param b float  blue component
     * @param a float  alpha component
     */
    public void setColor( float r, float g, float b, float a )
    {
        setColor( 0, r, g, b, a );
    }

    /**
     * set the index color entry to the specified color with floats
     * @param i int   which color index
     * @param r float  red component
     * @param g float  green component
     * @param b float  blue component
     */
    public boolean setColor( int i, float r, float g, float b )
    {
        return setColor( i, r, g, b, 1 );
    }
    /**
     * set the i-th color entry to the specified color with Color
     * @param i int   which color index
     * @param r float  red component
     * @param g float  green component
     * @param b float  blue component
     * @param a float  alpha component
     */
    public boolean setColor( int i, float r, float g, float b, float a )
    {
        if ( i < 0 || i > MAX_COLORS ) // should throw an exception!
        {
            System.err.println( "*** ERROR *** Shape3D.setColor: bad index: " 
                    + i + "\n" );
            return false;
        }
        if ( colors[ i ] == null )
            colors[ i ] = new Color( r, g, b, a );  // put color at index
        else
            colors[ i ].setColor( r, g, b, a );

        // make buffer!
        colorBufs[ i ] = MemoryUtil.memAllocFloat( 4 );
        colorBufs[ i ].put( r )
        .put( g )
        .put( b )
        .put( a ).flip(); 
        return true;
    }
    /**
     * set the i-th color entry to the specified color with Color
     * @param i int   which color index
     * @param c Color  the color
     */
    public boolean setColor( int i, Color c )
    {
        return setColor( i, c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha() );
    }
    //------------------ setSize ----------------------------------------
    /**
     * set the size of the shape to be scaled by xs, ys, zs
     *    That is, the shape has an internal fixed size, the shape parameters
     *    scale that internal size.
     * @param xs float	x Scale factor
     * @param ys float	y Scale factor
     * @param zs float	z Scale factor
     * 
     */
    public void setSize( float xs, float ys, float zs )
    {
        xSize = xs;
        ySize = ys;
        zSize = zs;
    }

    /**
     * set the rotation parameters: angle, and axis specification
     * @param a float	angle of rotation
     * @param dx float	x axis direction
     * @param dy float	y axis direction
     * @param dz float	z axis direction
     */
    public void setRotate( float a, float dx, float dy, float dz )
    {
        angle = a;
        dxRot = dx;
        dyRot = dy;
        dzRot = dz;
    }

    /**
     * This set rotation method accepts a pure rotation matrix if 
     * @param rotation_matrix
     */
    public void setRotate(Matrix3f rotation_matrix) {
        if (rotation_matrix.determinant() > 0.99 && rotation_matrix.determinant() <= 1.0) {
            AxisAngle4f calculate = new AxisAngle4f();
            calculate.set(rotation_matrix);
            angle = calculate.angle;
            dxRot = calculate.x;
            dyRot = calculate.y;
            dzRot = calculate.z;
            //System.out.println(angle + " " + dxRot + " " + dyRot + " " + dzRot);
        }
        // If the matrix is not a pure rotation matrix then assign some arbitrary axis
        else {
            angle = 0f;
            dxRot = 1f;
            dyRot = 0f;
            dzRot = 0f;
        }
    }

    /**
     * This sets the light factors kd and ks
     * @param kd
     * @param ks
     */ 
    public void setLightFactors( float kd, float ks) {
        this.kd = kd;
        this.ks = ks; 
    }
    
    /**
     * Function returns kd factor
     * @return kd
     */
    public float getLightFactorKd() {
        return this.kd;
    }
    
    /**
     * Function returns ks factor
     * @return ks
     */
    public float getLightFactorKs() {
        return this.ks;
    }
}