/**
 * Box.java - a class implementation representing a Box object
 *           in OpenGL
 * Oct 16, 2013
 * rdb - derived from Box.cpp
 * 
 * 10/28/14 rdb - revised to explicitly draw faces
 *              - drawPrimitives -> drawObject( GL2 )
 *              - uses glsl
 * 11/10/14 rdb - existing rebuilds glsl buffers on every redraw.
 *                should and canonly do it once.
 */

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.nio.*;

import org.lwjgl.system.MemoryUtil;

public class Box extends Shape3D
{
    //--------- instance variables -----------------
    final private int   coordSize = 3;
    private float vertexnormals[]= new float[144];
    private float normalx[] = new float[36];
    private float normaly[] = new float[36];
    private float normalz[]= new float[36];
    private int x=0;
    private int y=0;
    private int z=0;
    private int w=0;

    // vertex coordinates
    private float[] verts  = 
        {   // 3-element vertex coordinates; 
                // 3 letter codes are cube corners [lr][bt][nf] left/right bottom/top near/far
                // right face 2 triangles:  rbn, rbf, rtf and rbn, rtf, rtn
                0.5f, -0.5f, 0.5f,    0.5f, -0.5f, -0.5f,  0.5f, 0.5f, -0.5f,  
                0.5f, -0.5f, 0.5f,    0.5f, 0.5f, -0.5f,   0.5f, 0.5f, 0.5f,
                // top face: ltn, rtn, rtf  and  ltn, rtf, ltf
                -0.5f, 0.5f, 0.5f,     0.5f, 0.5f, 0.5f,   0.5f, 0.5f, -0.5f,   
                -0.5f, 0.5f, 0.5f,     0.5f, 0.5f, -0.5f, -0.5f, 0.5f, -0.5f,
                // back face: rbf, lbf, ltf and rbf, ltf, rtf
                0.5f, -0.5f, -0.5f,  -0.5f, -0.5f, -0.5f,  -0.5f, 0.5f, -0.5f,   
                0.5f, -0.5f, -0.5f,  -0.5f, 0.5f, -0.5f,    0.5f, 0.5f, -0.5f,
                // left face: lbf, lbn, ltn and lbf, ltn, ltf -- corrected
                -0.5f, -0.5f, -0.5f,   -0.5f, -0.5f, 0.5f,  -0.5f, 0.5f,  0.5f, 
                -0.5f, -0.5f, -0.5f,   -0.5f, 0.5f,  0.5f,  -0.5f, 0.5f, -0.5f, 
                // bottom face:  lbf, rbf, rbn  and lbf, rbn, lbn
                -0.5f, -0.5f, -0.5f,   0.5f, -0.5f, -0.5f,   0.5f, -0.5f,  0.5f,
                -0.5f, -0.5f, -0.5f,   0.5f, -0.5f,  0.5f,  -0.5f, -0.5f,  0.5f,
                // front face 2 triangles:  lbn, rbn, rtn  and lbn, rtn, ltn
                -0.5f, -0.5f, 0.5f,    0.5f, -0.5f, 0.5f,   0.5f, 0.5f, 0.5f, 
                -0.5f, -0.5f, 0.5f,    0.5f, 0.5f, 0.5f,   -0.5f, 0.5f, 0.5f,   
        };

    private float colors [] = 
        {
                // front face: blue
                0f, 0f, 1f, 1f,    0f, 0f, 1f, 1f,   0f, 0f, 1f, 1f,  
                0f, 0f, 1f, 1f,    0f, 0f, 1f, 1f,   0f, 0f, 1f, 1f, 
                // right face: red
                1f, 0f, 0f, 1f,    1f, 0f, 0f, 1f,   1f, 0f, 0f, 1f,   
                1f, 0f, 0f, 1f,    1f, 0f, 0f, 1f,   1f, 0f, 0f, 1f,   
                // top face: green
                0f, 1f, 0f, 1f,     0f, 1f, 0f, 1f,  0f, 1f, 0f, 1f,   
                0f, 1f, 0f, 1f,     0f, 1f, 0f, 1f,  0f, 1f, 0f, 1f, 
                // back face: magenta
                1f, 0f, 1f, 1f,    1f, 0f, 1f, 1f,   1f, 0f, 1f, 1f,
                1f, 0f, 1f, 1f,    1f, 0f, 1f, 1f,   1f, 0f, 1f, 1f,
                // left face: cyan
                0f, 1f, 1f, 1f,    0f, 1f, 1f, 1f,   0f, 1f, 1f, 1f,  
                0f, 1f, 1f, 1f,    0f, 1f, 1f, 1f,   0f, 1f, 1f, 1f,  
                // bottom face: yellow
                1f, 1f, 0f, 1f,   1f, 1f, 0f, 1f,   1f, 1f, 0f, 1f,   
                1f, 1f, 0f, 1f,   1f, 1f, 0f, 1f,   1f, 1f, 0f, 1f, 
        };


    private float colors0 [] = { 1f, 0f, 0f, 1f, 1f, 0f, 0f, 1f, 1f, 0f, 0f, 1f, 1f, 0f, 0f, 1f, 1f, 0f, 0f, 1f, 1f, 0f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 0f, 0f, 1f, 1f, 0f, 0f, 1f, 1f, 0f, 0f, 1f, 1f, 0f, 0f, 1f, 1f, 0f, 0f, 1f, 1f, 0f, 0f, 1f, 1f, };
    private float colors1 [] = { 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 0f, 0f, 1f, 1f, 0f, 0f, 1f, 1f, 0f, 0f, 1f, 1f, 0f, 0f, 1f, 1f, 0f, 0f, 1f, 1f, 0f, 0f, 1f, 1f, 1f, 0f, 0f, 1f, 1f, 0f, 0f, 1f, 1f, 0f, 0f, 1f, 1f, 0f, 0f, 1f, 1f, 0f, 0f, 1f, 1f, 0f, 0f, 1f, };
    private float colors2 [] = { 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 0f, 0f, 1f, 1f, 0f, 0f, 1f, 1f, 0f, 0f, 1f, 1f, 0f, 0f, 1f, 1f, 0f, 0f, 1f, 1f, 0f, 0f, 1f, 1f, 1f, 0f, 0f, 1f, 1f, 0f, 0f, 1f, 1f, 0f, 0f, 1f, 1f, 0f, 0f, 1f, 1f, 0f, 0f, 1f, 1f, 0f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, };
    private float colors3 [] = { 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 0f, 0f, 1f, 1f, 0f, 0f, 1f, 1f, 0f, 0f, 1f, 1f, 0f, 0f, 1f, 1f, 0f, 0f, 1f, 1f, 0f, 0f, 1f, 1f, 1f, 0f, 0f, 1f, 1f, 0f, 0f, 1f, 1f, 0f, 0f, 1f, 1f, 0f, 0f, 1f, 1f, 0f, 0f, 1f, 1f, 0f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, };
    private float colors4 [] = { 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 0f, 0f, 1f, 1f, 0f, 0f, 1f, 1f, 0f, 0f, 1f, 1f, 0f, 0f, 1f, 1f, 0f, 0f, 1f, 1f, 0f, 0f, 1f, 1f, 1f, 0f, 0f, 1f, 1f, 0f, 0f, 1f, 1f, 0f, 0f, 1f, 1f, 0f, 0f, 1f, 1f, 0f, 0f, 1f, 1f, 0f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, };
    private float colors5 [] = { 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 0f, 0f, 1f, 1f, 0f, 0f, 1f, 1f, 0f, 0f, 1f, 1f, 0f, 0f, 1f, 1f, 0f, 0f, 1f, 1f, 0f, 0f, 1f, 1f, 1f, 0f, 0f, 1f, 1f, 0f, 0f, 1f, 1f, 0f, 0f, 1f, 1f, 0f, 0f, 1f, 1f, 0f, 0f, 1f, 1f, 0f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, };


    public void calcNormals() {
        for ( int i = 0; i < verts.length; i+=12) {
            for ( int j = 0; j<11; j+=4) {
                normalx[x++] = verts[i+j];
                normaly[y++] = verts[i+j+1];
                normalz[z++] = verts[i+j+2];
            }
        }
    }

    public void vertexNormals() {
        int counter = 0;
        for (int i = 0; i < normalz.length; i+=3) {
            float x1=normalx[i]-normalx[i+1];
            float x2=normalx[i+2]-normalx[i+1];
            float y1=normaly[i]-normaly[i+1];
            float y2=normaly[i+2]-normaly[i+1];
            float z1=normalz[i]-normalz[i+1];
            float z2=normalz[i+2]-normalz[i+1];
            float x3= (y2*z1)-(z2*y1);
            float y3= (z2*x1)-(x2*z1);
            float z3= (x2*y1)-(y2*x1);

            while (counter <3) {
                vertexnormals[w]=x3;
                w++;
                vertexnormals[w]=y3;
                w++;
                vertexnormals[w]=z3;
                w++;
                vertexnormals[w]=0;
                w++;
                counter++;
            }
            counter = 0;
        }
    }
    //------------- constructor -----------------------
    /**
     * Construct the data for this box object.
     */
    public Box()
    {
        calcNormals();
        vertexNormals();
        setCoordData( verts, 3 );
        setNormalData( verts, 3 ); // Normals are same as vertices!
        setColorData( colors, 4 );
    }

    public Box(boolean useVertexNormals) {
        setCoordData( verts, 3 );
        if (useVertexNormals) {
            calcNormals();
            vertexNormals();
            setNormalData( vertexnormals, 3 ); 
        }
        else {
            setNormalData( verts, 3);
        }
        // Normals are same as vertices!
        setColorData( colors, 4 );
    }

    public Box(int i) {
        setCoordData( verts, 3 );
        setNormalData( verts, 3 ); // Normals are same as vertices!
        switch(i){
        case 0:
            setColorData( colors0, 4 );
            break;
        case 1:
            setColorData( colors1, 4 );
            break;
        case 2:
            setColorData( colors2, 4 );
            break;
        case 3:
            setColorData( colors3, 4 );
            break;
        case 4:
            setColorData( colors4, 4 );
            break;
        case 5:
            setColorData( colors5, 4);
            break;
        default:
            setColorData( colors, 4);
            break;
        }
    }
}
