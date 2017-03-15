/**
 * Implementation of the Pyramid.
 * @author gaurav
 *
 */
public class Pyramid extends Shape3D {
    //--------- instance variables -----------------
    final private int   coordSize = 4;
    final private int   colorSize = 4;
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
        {
                // The triangle in the front  
                0.0f, 1.0f, 0.0f, 1f,    -1.0f, 0.0f, 1.0f, 1f,  1.0f, 0.0f, 1.0f, 1f, 

                // The triangle on the right
                0.0f, 1.0f, 0.0f, 1f,     1.0f, 0.0f, 1.0f, 1f,  1.0f, 0.0f,-1.0f, 1f,

                // The triangle on the back
                0.0f, 1.0f, 0.0f, 1f,    1.0f, 0.0f,-1.0f, 1f,   -1.0f, 0.0f, -1.0f, 1f,   

                // The triangle on the left
                0.0f, 1.0f, 0.0f, 1f,   -1.0f, 0.0f, -1.0f, 1f,   -1.0f, 0.0f, 1.0f, 1f, 

                // Triangle part of the base rectangle
                -1.0f, 0.0f, -1.0f, 1f,  1.0f, 0.0f,-1.0f, 1f,     -1.0f, 0.0f, 1.0f, 1f,

                // Triangle part of the base rectangle
                1.0f, 0.0f,-1.0f, 1f,     -1.0f, 0.0f, 1.0f, 1f,   1.0f, 0.0f, 1.0f, 1f, 
        };

    private float[] colors = {
            // right face: red
            1f, 0f, 0f, 1f,    1f, 0f, 0f, 1f,   1f, 0f, 0f, 1f,   

            // top face: green
            0f, 1f, 0f, 1f,     0f, 1f, 0f, 1f,  0f, 1f, 0f, 1f,   

            // back face: magenta
            1f, 0f, 1f, 1f,    1f, 0f, 1f, 1f,   1f, 0f, 1f, 1f,

            // left face: cyan
            0f, 1f, 1f, 1f,    0f, 1f, 1f, 1f,   0f, 1f, 1f, 1f,  

            // bottom face: yellow
            1f, 1f, 0f, 1f,   1f, 1f, 0f, 1f,   1f, 1f, 0f, 1f,   

            // front face: blue
            0f, 0f, 1f, 1f,    0f, 0f, 1f, 1f,   0f, 0f, 1f, 1f,  
    };

    private float[] colors0 = { 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 0f, 0f, 1f, 1f, 0f, 0f, 1f, 1f, 0f, 0f, 1f, 1f, 1f, 0f, 0f, 1f, 1f, 0f, 0f, 1f, 1f, 0f, 0f, 1f, }; 
    private float[] colors1 = { 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 0f, 0f, 1f, 1f, 0f, 0f, 1f, 1f, 0f, 0f, 1f, 1f, 1f, 0f, 0f, 1f, 1f, 0f, 0f, 1f, 1f, 0f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, }; 
    private float[] colors2 = { 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 0f, 0f, 1f, 1f, 0f, 0f, 1f, 1f, 0f, 0f, 1f, 1f, 1f, 0f, 0f, 1f, 1f, 0f, 0f, 1f, 1f, 0f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, }; 
    private float[] colors3 = { 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 0f, 0f, 1f, 1f, 0f, 0f, 1f, 1f, 0f, 0f, 1f, 1f, 1f, 0f, 0f, 1f, 1f, 0f, 0f, 1f, 1f, 0f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, }; 
    private float[] colors4 = { 0f, 0f, 1f, 1f, 0f, 0f, 1f, 1f, 0f, 0f, 1f, 1f, 1f, 0f, 0f, 1f, 1f, 0f, 0f, 1f, 1f, 0f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, }; 
    private float[] colors5 = { 1f, 0f, 0f, 1f, 1f, 0f, 0f, 1f, 1f, 0f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 0f, 0f, 1f, 1f, 0f, 0f, 1f, 1f, 0f, 0f, 1f, 1f, };


    /**
     * Calculates the Surface Normals
     */
    public void calcNormals() {
        for ( int i = 0; i < verts.length; i+=12) {
            for ( int j = 0; j<11; j+=4) {
                normalx[x++] = verts[i+j];
                normaly[y++] = verts[i+j+1];
                normalz[z++] = verts[i+j+2];
            }
        }
    }

    /**
     * Calculates the Vertex Normals
     */
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
    public Pyramid()
    {
        setCoordData( verts, 4 );
        setNormalData( verts, 4 ); // Normals are same as vertices!
        setColorData( colors, 4);
    }
    public Pyramid(boolean useVertexNormals)
    {
        calcNormals();
        vertexNormals();
        setCoordData( verts, 4 );
        if (useVertexNormals) {
            calcNormals();
            vertexNormals();
            setNormalData( vertexnormals, 4 );
        }
        else {
            setNormalData( verts, 4 );
        }
        setColorData( colors, 4 );
    }
    public Pyramid(int i) {
        setCoordData( verts, 4 );
        setNormalData( verts, 4 ); // Normals are same as vertices!
        switch( i ) {
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
            setColorData( colors5, 4 );
            break;
        default:
            setColorData( colors, 4);
        }
    }
}