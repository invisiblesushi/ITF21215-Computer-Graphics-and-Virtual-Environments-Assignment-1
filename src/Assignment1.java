package daniel_chen_assignment1;


import com.jogamp.newt.event.*;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.*;
import com.jogamp.opengl.math.FloatUtil;
import com.jogamp.opengl.util.Animator;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.TextureIO;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import static com.jogamp.opengl.GL.*;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL.GL_UNSIGNED_SHORT;
import static com.jogamp.opengl.GL2ES2.GL_DEBUG_SEVERITY_HIGH;
import static com.jogamp.opengl.GL2ES2.GL_DEBUG_SEVERITY_MEDIUM;
import static com.jogamp.opengl.GL2ES3.*;
import static com.jogamp.opengl.GL2ES3.GL_UNIFORM_BUFFER;
import static com.jogamp.opengl.GL4.GL_MAP_COHERENT_BIT;
import static com.jogamp.opengl.GL4.GL_MAP_PERSISTENT_BIT;

/*
* WASD to move, QE to move UP or DOWN
* 
* Koden er skrevet i Eclipse med JOGL libary inkludert( gluegen-rt.jar, jogl-all.jar )
* 
* Får ikke implementert sphere ifølge kode eksemple fra nettside, feilmelding:
* Caused by: com.jogamp.opengl.GLException: element vertex_buffer_object must be bound to call this method
* 
* Lys er ikke implementert, fikk det ikke til å fungere på min maskin.
*/
public class Assignment1 implements GLEventListener, KeyListener {

    // OpenGL window reference
    private static GLWindow window;

    // The animator is responsible for continuous operation
    private static Animator animator;

    // The program entry point
    public static void main(String[] args) {
        new Assignment1().setup();
    }
    
    //Camera pos x y z
    private float[]cameraPos = {
    		0f, 0f, -8f
    };

    // Vertex data for cube x y z u w
    //u w definerer hvor texture skal være
    // Kilde http://it.hiof.no/~larsvmag/itf21215_17/examples.html
    private float[] vertexDataCube = {
        // Front
        -1.0f, 1.0f, 1.0f, 0.0f, 1.0f,
        -1.0f, -1.0f, 1.0f, 0.0f, 0.0f,
        1.0f, -1.0f, 1.0f, 1.0f, 0.0f,
        1.0f, 1.0f, 1.0f, 1.0f, 1.0f,
        // Back
        1.0f, 1.0f, -1.0f, 0.0f, 1.0f,
        1.0f, -1.0f, -1.0f, 0.0f, 0.0f,
        -1.0f, -1.0f, -1.0f, 1.0f, 0.0f,
        -1.0f, 1.0f, -1.0f, 1.0f, 1.0f,
        // Left
        -1.0f, 1.0f, -1.0f, 0.0f, 1.0f,
        -1.0f, -1.0f, -1.0f, 0.0f, 0.0f,
        -1.0f, -1.0f, 1.0f, 1.0f, 0.0f,
        -1.0f, 1.0f, 1.0f, 1.0f, 1.0f,
        // Right
        1.0f, 1.0f, 1.0f, 0.0f, 1.0f,
        1.0f, -1.0f, 1.0f, 0.0f, 0.0f,
        1.0f, -1.0f, -1.0f, 1.0f, 0.0f,
        1.0f, 1.0f, -1.0f, 1.0f, 1.0f,
        // Top
        -1.0f, 1.0f, -1.0f, 0.0f, 1.0f,
        -1.0f, 1.0f, 1.0f, 0.0f, 0.0f,
        1.0f, 1.0f, 1.0f, 1.0f, 0.0f,
        1.0f, 1.0f, -1.0f, 1.0f, 1.0f,
        // Bottom
        -1.0f, -1.0f, 1.0f, 0.0f, 1.0f,
        -1.0f, -1.0f, -1.0f, 0.0f, 0.0f,
        1.0f, -1.0f, -1.0f, 1.0f, 0.0f,
        1.0f, -1.0f, 1.0f,  1.0f, 1.0f
    };

    // Triangles, connect 3 points
    private short[] elementDataCube = {
        // Front
        0, 1, 2,		2, 3, 0,
        // Back
        4, 5, 6,	    6, 7, 4,
        // Left
        8, 9, 10,   	10, 11, 8,
        // Right
        12, 13, 14, 	14, 15, 12,
        // Top
        16, 17, 18, 	18, 19, 16,
        // Bottom
        20, 21, 22, 	22, 23, 20
    };
    
    // Vertex data
    // Kilde, laget selv ut i fra eksempel på nett
    private float[] vertexDataPyramid = {
           
         // Bottom
            -1.0f, -1.0f, 1.0f, 0.0f, 1.0f, 	//Bot1	0
            -1.0f, -1.0f, -1.0f, 0.0f, 0.0f,	//Bot2	1
            1.0f, -1.0f, -1.0f, 1.0f, 0.0f,		//Bot3	2
            1.0f, -1.0f, 1.0f,  1.0f, 1.0f,		//Bot4	3
            
    		// Front
    		0.0f, 1.0f, 0.0f, 0.0f, 1.0f,		//Top	4
            -1.0f, -1.0f, 1.0f, 0.0f, 0.0f, 	//Bot1	5
            1.0f, -1.0f, 1.0f, 1.0f, 0.0f, 		//Bot4  6		

            // Right
            0.0f, 1.0f, 0.0f, 0.0f, 1.0f,		//Top	7
            1.0f, -1.0f, 1.0f, 0.0f, 0.0f,		//Bot4	8
            1.0f, -1.0f, -1.0f, 1.0f, 0.0f,		//Bot3	9
            
            // Back
            0.0f, 1.0f, 0.0f, 0.0f, 1.0f,		//Top	10
            1.0f, -1.0f, -1.0f, 0.0f, 0.0f,		//Bot3	11
            -1.0f, -1.0f, -1.0f, 1.0f, 0.0f,	//Bot2	12
            
            // Left
            0.0f, 1.0f, 0.0f, 0.0f, 1.0f,		//Top	13
            -1.0f, -1.0f, -1.0f, 0.0f, 0.0f,	//Bot2	14
            -1.0f, -1.0f, 1.0f, 1.0f, 0.0f, 	//Bot1	15

    };

    // Triangles
    // punkt koblet sammen selv.
    private short[] elementDataPyramid = {
       // Bottom
       0, 1, 2, 2, 0, 3, 
       // Pyramid
       6, 4, 5, 15, 13, 14, 12, 10, 11, 9, 7, 8
    };
    
    
    
    // Interface for creating final static variables for defining the buffers
    private interface Buffer {
        int VERTEX = 0;
        int ELEMENT = 1;
        int GLOBAL_MATRICES = 2;
        int MODEL_MATRIX1 = 3;
        int MODEL_MATRIX2 = 4;
        int MODEL_MATRIX3 = 5;
        int MODEL_MATRIX4 = 6;
        int MODEL_MATRIX5 = 7;
        int MODEL_MATRIX6 = 8;
        int MODEL_MATRIX7 = 9;
        int MODEL_MATRIX8 = 10;
        int MODEL_MATRIX9 = 11;
        int MODEL_MATRIX10 = 12;
        int MODEL_MATRIX11 = 13;
        int MODEL_MATRIX12 = 14;
        int MODEL_MATRIX13 = 15;
        int VERTEX_PYRAMID = 16;
        int ELEMENT_PYRAMID = 17;
        int MATRIX_PYRAMID = 18;
        int MAX = 19;
    }

    // The OpenGL profile
    GLProfile glProfile;

    // The texture filename 
    private final String textureFilename = "src/daniel_chen_assignment1/texture.png";
    private final String textureFilename2 = "src/daniel_chen_assignment1/grass.png";
    private final String textureFilename3 = "src/daniel_chen_assignment1/door.png";
    private final String textureFilename4 = "src/daniel_chen_assignment1/skybox.png";
    private final String textureFilename5 = "src/daniel_chen_assignment1/dark.png";
    private final String textureFilename6 = "src/daniel_chen_assignment1/roof.png";
    private final String textureFilename7 = "src/daniel_chen_assignment1/water.png";
    private final String textureFilename8 = "src/daniel_chen_assignment1/window.png";
    private final String textureFilename9 = "src/daniel_chen_assignment1/wood.png";
    private final String textureFilename10 = "src/daniel_chen_assignment1/bush.png";
    private final String textureFilename11 = "src/daniel_chen_assignment1/tree.png";
    private final String textureFilename12 = "src/daniel_chen_assignment1/cloud.png";

    // Create buffers for the names
    private IntBuffer bufferNames = GLBuffers.newDirectIntBuffer(Buffer.MAX);
    private IntBuffer vertexArrayName = GLBuffers.newDirectIntBuffer(2);
    //Increment ++, antall textures
    private IntBuffer textureNames = GLBuffers.newDirectIntBuffer(13);

    // Create buffers for clear values
    private FloatBuffer clearColor = GLBuffers.newDirectFloatBuffer(new float[] {0, 0, 0, 0});
    private FloatBuffer clearDepth = GLBuffers.newDirectFloatBuffer(new float[] {1});

    // Create references to buffers for holding the matrices
    private ByteBuffer globalMatricesPointer, modelMatrixPointer1, modelMatrixPointer2, modelMatrixPointer3, modelMatrixPointer4, modelMatrixPointer5, 
    					modelMatrixPointer6, modelMatrixPointer7, modelMatrixPointer8, modelMatrixPointer9, modelMatrixPointer10, 
    					modelMatrixPointer11, modelMatrixPointer12, modelMatrixPointer13, modelPyramidMatrixPointer;

    // https://jogamp.org/bugzilla/show_bug.cgi?id=1287
    private boolean bug1287 = true;

    // Program instance reference
    private Program program;

    // Variable for storing the start time of the application
    private long start;

    // Application setup function
    private void setup() {

        // Get a OpenGL 4.x profile (x >= 0)
        glProfile = GLProfile.get(GLProfile.GL4);

        // Get a structure for definining the OpenGL capabilities with default values
        GLCapabilities glCapabilities = new GLCapabilities(glProfile);

        // Create the window with default capabilities
        window = GLWindow.create(glCapabilities);

        // Set the title of the window
        window.setTitle("Assignment 1");

        // Set the size of the window
        window.setSize(1440, 900);

        // Set debug context (must be set before the window is set to visible)
        window.setContextCreationFlags(GLContext.CTX_OPTION_DEBUG);

        // Make the window visible
        window.setVisible(true);

        // Add OpenGL and keyboard event listeners
        window.addGLEventListener(this);
        window.addKeyListener(this);

        // Create and start the animator
        animator = new Animator(window);
        animator.start();

        // Add window event listener
        window.addWindowListener(new WindowAdapter() {
            // Window has been destroyed
            @Override
            public void windowDestroyed(WindowEvent e) {
                // Stop animator and exit
                animator.stop();
                System.exit(1);
            }
        });
    }


    // GLEventListener.init implementation
    @Override
    public void init(GLAutoDrawable drawable) {

        // Get OpenGL 4 reference
        GL4 gl = drawable.getGL().getGL4();

        // Initialize debugging
        initDebug(gl);

        // Initialize buffers
        initBuffers(gl);

        // Initialize vertex array
        initVertexArray(gl);

        // Initialize texture
        initTexture(gl);

        // Set up the program
        program = new Program(gl, "simple_texturing", "simple_texturing", "simple_texturing");

        // Enable Opengl depth buffer testing
        gl.glEnable(GL_DEPTH_TEST);

        // Store the starting time of the application
        start = System.currentTimeMillis();
    }

    // GLEventListener.display implementation
    @Override
    public void display(GLAutoDrawable drawable) {

        // Get OpenGL 4 reference
        GL4 gl = drawable.getGL().getGL4();


        // Copy the view matrix to the server
        {
            // Create identity matrix, Camera pos
            float[] view = FloatUtil.makeTranslation(new float[16], 0, false, cameraPos[0], cameraPos[1],  cameraPos[2]);
            // Copy each of the values to the second of the two global matrices
            for (int i = 0; i < 16; i++)
                globalMatricesPointer.putFloat(16 * 4 + i * 4, view[i]);
        }


        // Clear the color and depth buffers
        gl.glClearBufferfv(GL_COLOR, 0, clearColor);
        gl.glClearBufferfv(GL_DEPTH, 0, clearDepth);

        // Activate the vertex program and vertex array
        gl.glUseProgram(program.name);
        gl.glBindVertexArray(vertexArrayName.get(0));
        gl.glBindTexture(gl.GL_TEXTURE_2D, textureNames.get(0)); //velger texture

        // Bind the global matrices buffer to a specified index within the uniform buffers
        gl.glBindBufferBase(
                GL_UNIFORM_BUFFER,
                Semantic.Uniform.TRANSFORM0,
                bufferNames.get(Buffer.GLOBAL_MATRICES));

        // Copy the model matrix to the server
        {
            // Find a time delta for the time passed since the start of execution
            long now = System.currentTimeMillis();
            float diff = (float) (now - start) / 1_000;


            // Create a scale matrix 
            float[] scale = FloatUtil.makeScale(new float[16], true, 0.5f, 0.5f, 0.5f);
            float[] scalePipe = FloatUtil.makeScale(new float[16], true, 0.2f, 0.7f, 0.2f);
            float[] scaleHouse = FloatUtil.makeScale(new float[16], true, 3f, 1f, 1f);
            float[] scaleRoof = FloatUtil.makeScale(new float[16], true, 3f, 1f, 1f);
            float[] scaleDoor = FloatUtil.makeScale(new float[16], true, 0.5f, 1f, 0.01f);
            float[] scaleGround = FloatUtil.makeScale(new float[16], true, 10f, 0.01f, 10f);
            float[] scaleSkyBox = FloatUtil.makeScale(new float[16], true, 10f, 10f, 10f);
            float[] scaleWindow = FloatUtil.makeScale(new float[16], true, 0.6f, 0.6f, 0.01f);
            float[] scaleSkyTop = FloatUtil.makeScale(new float[16], true, 10f, 0.02f, 10f);
            float[] scaleWater = FloatUtil.makeScale(new float[16], true, 3f, 0.01f, 6f);
            float[] scaleTree = FloatUtil.makeScale(new float[16], true, 0.3f, 1f, 0.3f);
            float[] scaleBush = FloatUtil.makeScale(new float[16], true, 1.5f, 0.8f, 1.5f);
            float[] scaleCloud = FloatUtil.makeScale(new float[16], true, 0.4f, 0.8f, 0.4f);

            // Create a translation matrix, position
            float[] translate1 = FloatUtil.makeTranslation(new float[16], 0, true, -8f, 3f, 1.8f);
            float[] translate2 = FloatUtil.makeTranslation(new float[16], 0, true, 0f, 0f, 0f);
            float[] translate3 = FloatUtil.makeTranslation(new float[16], 0, true, 0f, 2f, 0f);
            float[] translate4 = FloatUtil.makeTranslation(new float[16], 0, true, -3f, 0f, 100f);
            float[] translate5 = FloatUtil.makeTranslation(new float[16], 0, true, 0f, -100f, 0f);
            float[] translate6 = FloatUtil.makeTranslation(new float[16], 0, true, 0f, -0.1f, 0f);
            float[] translate7 = FloatUtil.makeTranslation(new float[16], 0, true, 0f, 0.5f, 100f);
            float[] translate8 = FloatUtil.makeTranslation(new float[16], 0, true, 3f, 0.5f, 100f);
            float[] translate9 = FloatUtil.makeTranslation(new float[16], 0, true, 0f, 440f, 0f);
            float[] translate10 = FloatUtil.makeTranslation(new float[16], 0, true, 2.4f, -99f, -0.8f);
            float[] translate11 = FloatUtil.makeTranslation(new float[16], 0, true, 20f, 0f, 10f);
            float[] translate12 = FloatUtil.makeTranslation(new float[16], 0, true, 4f, 2f, 2f);
            float[] translate13 = FloatUtil.makeTranslation(new float[16], 0, true, -4.05f, 4f, 1f);

            // Create a rotation matrix around the z axis based on the time delta
            float[] rotate = FloatUtil.makeRotationAxis(new float[16], 0, diff, 0f, 1f, 0f, new float[3]);
            float[] rotate180 = FloatUtil.makeRotationAxis(new float[16], 0, (float)3.140, 0f, 0f, 1f, new float[3]);            
            
            
            
            // Combine the three matrices by multiplying them
            //float[] model1 = FloatUtil.multMatrix(FloatUtil.multMatrix(scale, translate1, new float[16]), rotate, new float[16]);
            float[] model1 = FloatUtil.multMatrix(scalePipe, translate1, new float[16]);
            
            float[] model2 = FloatUtil.multMatrix(scaleHouse, translate2, new float[16]);
            
            float[] model3 = FloatUtil.multMatrix(scaleRoof, translate3, new float[16]);
            
            float[] model4 = FloatUtil.multMatrix(scaleDoor, translate4, new float[16]);
            
            float[] model5 = FloatUtil.multMatrix(scaleGround, translate5, new float[16]);
            
            float[] model6 = FloatUtil.multMatrix(scaleSkyBox, translate6, new float[16]);
            
            float[] model7 = FloatUtil.multMatrix(scaleWindow, translate7, new float[16]);
            
            float[] model8 = FloatUtil.multMatrix(scaleWindow, translate8, new float[16]);
            
            float[] model9 = FloatUtil.multMatrix(scaleSkyTop, translate9, new float[16]);
            
            float[] model10 = FloatUtil.multMatrix(scaleWater, translate10, new float[16]);
            
            float[] model11 = FloatUtil.multMatrix(scaleTree, translate11, new float[16]);
            
            float[] model12 = FloatUtil.multMatrix(scaleBush, translate12, new float[16]);
            
            float[] model13 = FloatUtil.multMatrix(FloatUtil.multMatrix(scaleCloud, translate13, new float[16]), FloatUtil.multMatrix(rotate, rotate180, new float[16]));

            // Copy the entire matrix to the server
            modelMatrixPointer1.asFloatBuffer().put(model1);
            modelMatrixPointer2.asFloatBuffer().put(model2);
            modelMatrixPointer3.asFloatBuffer().put(model3);
            
            modelMatrixPointer4.asFloatBuffer().put(model4);
            modelMatrixPointer5.asFloatBuffer().put(model5);
            modelMatrixPointer6.asFloatBuffer().put(model6);
            modelMatrixPointer7.asFloatBuffer().put(model7);
            modelMatrixPointer8.asFloatBuffer().put(model8);
            modelMatrixPointer9.asFloatBuffer().put(model9);
            modelMatrixPointer10.asFloatBuffer().put(model10);
            modelMatrixPointer11.asFloatBuffer().put(model11);
            modelMatrixPointer12.asFloatBuffer().put(model12);
            modelMatrixPointer13.asFloatBuffer().put(model13);
        }

        // Bind the model matrix buffer to a specified index within the uniform buffers
        gl.glBindBufferBase(
                GL_UNIFORM_BUFFER,
                Semantic.Uniform.TRANSFORM1,
                bufferNames.get(Buffer.MODEL_MATRIX1));

        // Draw the triangle
        gl.glDrawElements(
                GL_TRIANGLES,
                elementDataCube.length,
                GL_UNSIGNED_SHORT,
                0);
        gl.glBindTexture(gl.GL_TEXTURE_2D, textureNames.get(8)); //velger texture
        // Bind the model matrix buffer to a specified index within the uniform buffers
        gl.glBindBufferBase(
                GL_UNIFORM_BUFFER,
                Semantic.Uniform.TRANSFORM1,
                bufferNames.get(Buffer.MODEL_MATRIX2));

        // Draw the triangle
        gl.glDrawElements(
                GL_TRIANGLES,
                elementDataCube.length,
                GL_UNSIGNED_SHORT,
                0);
        
      //Pyramid
        gl.glBindVertexArray(vertexArrayName.get(1));
        gl.glBindTexture(gl.GL_TEXTURE_2D, textureNames.get(5)); //velger texture
        // Bind the model matrix buffer to a specified index within the uniform buffers
        gl.glBindBufferBase(
                GL_UNIFORM_BUFFER,
                Semantic.Uniform.TRANSFORM1,
                bufferNames.get(Buffer.MODEL_MATRIX3));

        // Draw the triangle
        gl.glDrawElements(
                GL_TRIANGLES,
                elementDataCube.length,
                GL_UNSIGNED_SHORT,
                0);

        
        gl.glBindVertexArray(vertexArrayName.get(0));
        gl.glBindTexture(gl.GL_TEXTURE_2D, textureNames.get(2)); //velger texture
        gl.glBindBufferBase(
                GL_UNIFORM_BUFFER,
                Semantic.Uniform.TRANSFORM1,
                bufferNames.get(Buffer.MODEL_MATRIX4));

        // Draw the triangle
        gl.glDrawElements(
                GL_TRIANGLES,
                elementDataCube.length,
                GL_UNSIGNED_SHORT,
                0);
        
       
        gl.glBindVertexArray(vertexArrayName.get(0));
        gl.glBindTexture(gl.GL_TEXTURE_2D, textureNames.get(1)); //velger texture
        gl.glBindBufferBase(
                GL_UNIFORM_BUFFER,
                Semantic.Uniform.TRANSFORM1,
                bufferNames.get(Buffer.MODEL_MATRIX5));

        // Draw the triangle
        gl.glDrawElements(
                GL_TRIANGLES,
                elementDataCube.length,
                GL_UNSIGNED_SHORT,
                0);

        gl.glBindVertexArray(vertexArrayName.get(0));
        gl.glBindTexture(gl.GL_TEXTURE_2D, textureNames.get(3)); //velger texture
        gl.glBindBufferBase(
                GL_UNIFORM_BUFFER,
                Semantic.Uniform.TRANSFORM1,
                bufferNames.get(Buffer.MODEL_MATRIX6));

        // Draw the triangle
        gl.glDrawElements(
                GL_TRIANGLES,
                elementDataCube.length,
                GL_UNSIGNED_SHORT,
                0);

        gl.glBindVertexArray(vertexArrayName.get(0));
        gl.glBindTexture(gl.GL_TEXTURE_2D, textureNames.get(7)); //velger texture
        gl.glBindBufferBase(
                GL_UNIFORM_BUFFER,
                Semantic.Uniform.TRANSFORM1,
                bufferNames.get(Buffer.MODEL_MATRIX7));

        // Draw the triangle
        gl.glDrawElements(
                GL_TRIANGLES,
                elementDataCube.length,
                GL_UNSIGNED_SHORT,
                0);

        gl.glBindBufferBase(
                GL_UNIFORM_BUFFER,
                Semantic.Uniform.TRANSFORM1,
                bufferNames.get(Buffer.MODEL_MATRIX8));

        // Draw the triangle
        gl.glDrawElements(
                GL_TRIANGLES,
                elementDataCube.length,
                GL_UNSIGNED_SHORT,
                0);

        gl.glBindVertexArray(vertexArrayName.get(0));
        gl.glBindTexture(gl.GL_TEXTURE_2D, textureNames.get(4)); //velger texture
        gl.glBindBufferBase(
                GL_UNIFORM_BUFFER,
                Semantic.Uniform.TRANSFORM1,
                bufferNames.get(Buffer.MODEL_MATRIX9));

        // Draw the triangle
        gl.glDrawElements(
                GL_TRIANGLES,
                elementDataCube.length,
                GL_UNSIGNED_SHORT,
                0);

        gl.glBindVertexArray(vertexArrayName.get(0));
        gl.glBindTexture(gl.GL_TEXTURE_2D, textureNames.get(6)); //velger texture
        gl.glBindBufferBase(
                GL_UNIFORM_BUFFER,
                Semantic.Uniform.TRANSFORM1,
                bufferNames.get(Buffer.MODEL_MATRIX10));

        // Draw the triangle
        gl.glDrawElements(
                GL_TRIANGLES,
                elementDataCube.length,
                GL_UNSIGNED_SHORT,
                0);
        
        gl.glBindVertexArray(vertexArrayName.get(0));
        gl.glBindTexture(gl.GL_TEXTURE_2D, textureNames.get(11)); //velger texture
        gl.glBindBufferBase(
                GL_UNIFORM_BUFFER,
                Semantic.Uniform.TRANSFORM1,
                bufferNames.get(Buffer.MODEL_MATRIX11));

        // Draw the triangle
        gl.glDrawElements(
                GL_TRIANGLES,
                elementDataCube.length,
                GL_UNSIGNED_SHORT,
                0);
        
        gl.glBindVertexArray(vertexArrayName.get(0));
        gl.glBindTexture(gl.GL_TEXTURE_2D, textureNames.get(10)); //velger texture
        gl.glBindBufferBase(
                GL_UNIFORM_BUFFER,
                Semantic.Uniform.TRANSFORM1,
                bufferNames.get(Buffer.MODEL_MATRIX12));

        // Draw the triangle
        gl.glDrawElements(
                GL_TRIANGLES,
                elementDataCube.length,
                GL_UNSIGNED_SHORT,
                0);
        
        gl.glBindVertexArray(vertexArrayName.get(1));
        gl.glBindTexture(gl.GL_TEXTURE_2D, textureNames.get(12)); //velger texture
        gl.glBindBufferBase(
                GL_UNIFORM_BUFFER,
                Semantic.Uniform.TRANSFORM1,
                bufferNames.get(Buffer.MODEL_MATRIX13));

        // Draw the triangle
        gl.glDrawElements(
                GL_TRIANGLES,
                elementDataCube.length,
                GL_UNSIGNED_SHORT,
                0);

        // Deactivate the program and vertex array
        gl.glUseProgram(0);
        gl.glBindVertexArray(0);
        gl.glBindTexture(gl.GL_TEXTURE_2D, 0);
    }

    // GLEventListener.reshape implementation
    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {

        // Get OpenGL 4 reference
        GL4 gl = drawable.getGL().getGL4();

        // Create an orthogonal projection matrix 
        float[] ortho = FloatUtil.makePerspective(new float[16], 0, false, (float)Math.PI/2f, (float)width/height, 0.1f, 100f);
		
        // Copy the projection matrix to the server
        globalMatricesPointer.asFloatBuffer().put(ortho);

        // Set the OpenGL viewport
        gl.glViewport(x, y, width, height);
    }

    // GLEventListener.dispose implementation
    @Override
    public void dispose(GLAutoDrawable drawable) {

        // Get OpenGL 4 reference
        GL4 gl = drawable.getGL().getGL4();

        // Unmap the transformation matrices
        gl.glUnmapNamedBuffer(bufferNames.get(Buffer.GLOBAL_MATRICES));
        gl.glUnmapNamedBuffer(bufferNames.get(Buffer.MODEL_MATRIX1));
        gl.glUnmapNamedBuffer(bufferNames.get(Buffer.MODEL_MATRIX2));
        gl.glUnmapNamedBuffer(bufferNames.get(Buffer.MODEL_MATRIX3));
        
        gl.glUnmapNamedBuffer(bufferNames.get(Buffer.MODEL_MATRIX4));
        gl.glUnmapNamedBuffer(bufferNames.get(Buffer.MODEL_MATRIX5));
        gl.glUnmapNamedBuffer(bufferNames.get(Buffer.MODEL_MATRIX6));
        gl.glUnmapNamedBuffer(bufferNames.get(Buffer.MODEL_MATRIX7));
        gl.glUnmapNamedBuffer(bufferNames.get(Buffer.MODEL_MATRIX8));
        gl.glUnmapNamedBuffer(bufferNames.get(Buffer.MODEL_MATRIX9));
        gl.glUnmapNamedBuffer(bufferNames.get(Buffer.MODEL_MATRIX10));
        gl.glUnmapNamedBuffer(bufferNames.get(Buffer.MODEL_MATRIX11));
        gl.glUnmapNamedBuffer(bufferNames.get(Buffer.MODEL_MATRIX12));
        gl.glUnmapNamedBuffer(bufferNames.get(Buffer.MODEL_MATRIX13));

        // Delete the program
        gl.glDeleteProgram(program.name);

        // Delete the vertex array
        gl.glDeleteVertexArrays(2, vertexArrayName);

        // Delete the buffers
        gl.glDeleteBuffers(Buffer.MAX, bufferNames);

        gl.glDeleteTextures(8, textureNames);
    }

    // KeyListener.keyPressed implementation
    @Override
    public void keyPressed(KeyEvent e) {
        // Destroy the window if the esape key is pressed
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            new Thread(() -> {
                window.destroy();
            }).start();
        }
        //WASD key control cameraPos
        if (e.getKeyCode() == KeyEvent.VK_W) {
        	new Thread(() -> {
        		cameraPos[2] = (float) (cameraPos[2] + 0.5);
        	}).start();
        };
        if (e.getKeyCode() == KeyEvent.VK_A) {
        	new Thread(() -> {
        		cameraPos[0] = (float) (cameraPos[0] + 0.5);
        	}).start();
        };
        if (e.getKeyCode() == KeyEvent.VK_S) {
        	new Thread(() -> {
        		cameraPos[2] = (float) (cameraPos[2] - 0.5);
        	}).start();
        };
        if (e.getKeyCode() == KeyEvent.VK_D) {
        	new Thread(() -> {
        		cameraPos[0] = (float) (cameraPos[0] - 0.5);
        	}).start();
        };
        
        //Up and down
        if (e.getKeyCode() == KeyEvent.VK_Q) {
        	new Thread(() -> {
        		cameraPos[1] = (float) (cameraPos[1] - 0.5);
        	}).start();
        };
        if (e.getKeyCode() == KeyEvent.VK_E) {
        	new Thread(() -> {
        		cameraPos[1] = (float) (cameraPos[1] + 0.5);
        	}).start();
        };
    }

    // KeyListener.keyPressed implementation
    @Override
    public void keyReleased(KeyEvent e) {
    }

    // Function for initializing OpenGL debugging
    private void initDebug(GL4 gl) {

        // Register a new debug listener
        window.getContext().addGLDebugListener(new GLDebugListener() {
            // Output any messages to standard out
            @Override
            public void messageSent(GLDebugMessage event) {
                System.out.println(event);
            }
        });

        // Ignore all messages
        gl.glDebugMessageControl(
                GL_DONT_CARE,
                GL_DONT_CARE,
                GL_DONT_CARE,
                0,
                null,
                false);

        // Enable messages of high severity
        gl.glDebugMessageControl(
                GL_DONT_CARE,
                GL_DONT_CARE,
                GL_DEBUG_SEVERITY_HIGH,
                0,
                null,
                true);

        // Enable messages of medium severity
        gl.glDebugMessageControl(
                GL_DONT_CARE,
                GL_DONT_CARE,
                GL_DEBUG_SEVERITY_MEDIUM,
                0,
                null,
                true);
    }

    // Function fo initializing OpenGL buffers
    private void initBuffers(GL4 gl) {

        // Create a new float direct buffer for the vertex data 
        FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(vertexDataCube);
        FloatBuffer vertexBuffer2 = GLBuffers.newDirectFloatBuffer(vertexDataPyramid);
        
        // Create a new short direct buffer for the triangle indices
        ShortBuffer elementBuffer = GLBuffers.newDirectShortBuffer(elementDataCube);
        ShortBuffer elementBuffer2 = GLBuffers.newDirectShortBuffer(elementDataPyramid);

        // Create the OpenGL buffers
        gl.glCreateBuffers(Buffer.MAX, bufferNames);

        // If the workaround for bug 1287 isn't needed
        if (!bug1287) {

            // Create and initialize a named buffer storage for the vertex data
            gl.glNamedBufferStorage(bufferNames.get(Buffer.VERTEX), vertexBuffer.capacity() * Float.BYTES, vertexBuffer, GL_STATIC_DRAW);
            gl.glNamedBufferStorage(bufferNames.get(Buffer.VERTEX_PYRAMID), vertexBuffer2.capacity() * Float.BYTES, vertexBuffer2, GL_STATIC_DRAW);

            // Create and initialize a named buffer storage for the triangle indices
            gl.glNamedBufferStorage(bufferNames.get(Buffer.ELEMENT), elementBuffer.capacity() * Short.BYTES, elementBuffer, GL_STATIC_DRAW);
            gl.glNamedBufferStorage(bufferNames.get(Buffer.ELEMENT_PYRAMID), elementBuffer2.capacity() * Short.BYTES, elementBuffer2, GL_STATIC_DRAW);

            // Create and initialize a named buffer storage for the global and model matrices 
            gl.glNamedBufferStorage(bufferNames.get(Buffer.GLOBAL_MATRICES), 16 * 4 * 2, null, GL_MAP_WRITE_BIT);
            gl.glNamedBufferStorage(bufferNames.get(Buffer.MODEL_MATRIX1), 16 * 4, null, GL_MAP_WRITE_BIT);
            gl.glNamedBufferStorage(bufferNames.get(Buffer.MODEL_MATRIX2), 16 * 4, null, GL_MAP_WRITE_BIT);
            gl.glNamedBufferStorage(bufferNames.get(Buffer.MODEL_MATRIX3), 16 * 4, null, GL_MAP_WRITE_BIT);
            gl.glNamedBufferStorage(bufferNames.get(Buffer.MODEL_MATRIX4), 16 * 4, null, GL_MAP_WRITE_BIT);
            gl.glNamedBufferStorage(bufferNames.get(Buffer.MODEL_MATRIX5), 16 * 4, null, GL_MAP_WRITE_BIT);
            gl.glNamedBufferStorage(bufferNames.get(Buffer.MODEL_MATRIX6), 16 * 4, null, GL_MAP_WRITE_BIT);
            gl.glNamedBufferStorage(bufferNames.get(Buffer.MODEL_MATRIX7), 16 * 4, null, GL_MAP_WRITE_BIT);
            gl.glNamedBufferStorage(bufferNames.get(Buffer.MODEL_MATRIX8), 16 * 4, null, GL_MAP_WRITE_BIT);
            gl.glNamedBufferStorage(bufferNames.get(Buffer.MODEL_MATRIX9), 16 * 4, null, GL_MAP_WRITE_BIT);
            gl.glNamedBufferStorage(bufferNames.get(Buffer.MODEL_MATRIX10), 16 * 4, null, GL_MAP_WRITE_BIT);
            gl.glNamedBufferStorage(bufferNames.get(Buffer.MODEL_MATRIX11), 16 * 4, null, GL_MAP_WRITE_BIT);
            gl.glNamedBufferStorage(bufferNames.get(Buffer.MODEL_MATRIX12), 16 * 4, null, GL_MAP_WRITE_BIT);
            gl.glNamedBufferStorage(bufferNames.get(Buffer.MODEL_MATRIX13), 16 * 4, null, GL_MAP_WRITE_BIT);

            gl.glNamedBufferStorage(bufferNames.get(Buffer.MATRIX_PYRAMID), 16 * 4, null, GL_MAP_WRITE_BIT);
            
        } else {

            // Create and initialize a buffer storage for the vertex data
            gl.glBindBuffer(GL_ARRAY_BUFFER, bufferNames.get(Buffer.VERTEX));
            gl.glBufferStorage(GL_ARRAY_BUFFER, vertexBuffer.capacity() * Float.BYTES, vertexBuffer, 0);
            gl.glBindBuffer(GL_ARRAY_BUFFER, 0);
            
            gl.glBindBuffer(GL_ARRAY_BUFFER, bufferNames.get(Buffer.VERTEX_PYRAMID));
            gl.glBufferStorage(GL_ARRAY_BUFFER, vertexBuffer2.capacity() * Float.BYTES, vertexBuffer2, 0);
            gl.glBindBuffer(GL_ARRAY_BUFFER, 0);

            // Create and initialize a buffer storage for the triangle indices 
            gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferNames.get(Buffer.ELEMENT));
            gl.glBufferStorage(GL_ELEMENT_ARRAY_BUFFER, elementBuffer.capacity() * Short.BYTES, elementBuffer, 0);
            gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
            
            gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferNames.get(Buffer.ELEMENT_PYRAMID));
            gl.glBufferStorage(GL_ELEMENT_ARRAY_BUFFER, elementBuffer2.capacity() * Short.BYTES, elementBuffer2, 0);
            gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);


            // Retrieve the uniform buffer offset alignment minimum
            IntBuffer uniformBufferOffset = GLBuffers.newDirectIntBuffer(1);
            gl.glGetIntegerv(GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT, uniformBufferOffset);
            // Set the required bytes for the matrices in accordance to the uniform buffer offset alignment minimum
            int globalBlockSize = Math.max(16 * 4 * 2, uniformBufferOffset.get(0));
            int modelBlockSize = Math.max(16 * 4, uniformBufferOffset.get(0));

            
            // Create and initialize a named storage for the global matrices 
            gl.glBindBuffer(GL_UNIFORM_BUFFER, bufferNames.get(Buffer.GLOBAL_MATRICES));
            gl.glBufferStorage(GL_UNIFORM_BUFFER, globalBlockSize, null, GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT);
            gl.glBindBuffer(GL_UNIFORM_BUFFER, 0);

            // Create and initialize a named storage for the model matrices 
            gl.glBindBuffer(GL_UNIFORM_BUFFER, bufferNames.get(Buffer.MODEL_MATRIX1));
            gl.glBufferStorage(GL_UNIFORM_BUFFER, modelBlockSize, null, GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT);
            gl.glBindBuffer(GL_UNIFORM_BUFFER, 0);
            
            gl.glBindBuffer(GL_UNIFORM_BUFFER, bufferNames.get(Buffer.MODEL_MATRIX2));
            gl.glBufferStorage(GL_UNIFORM_BUFFER, modelBlockSize, null, GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT);
            gl.glBindBuffer(GL_UNIFORM_BUFFER, 0);
            
            gl.glBindBuffer(GL_UNIFORM_BUFFER, bufferNames.get(Buffer.MODEL_MATRIX3));
            gl.glBufferStorage(GL_UNIFORM_BUFFER, modelBlockSize, null, GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT);
            gl.glBindBuffer(GL_UNIFORM_BUFFER, 0);
            
            gl.glBindBuffer(GL_UNIFORM_BUFFER, bufferNames.get(Buffer.MODEL_MATRIX4));
            gl.glBufferStorage(GL_UNIFORM_BUFFER, modelBlockSize, null, GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT);
            gl.glBindBuffer(GL_UNIFORM_BUFFER, 0);
            
            gl.glBindBuffer(GL_UNIFORM_BUFFER, bufferNames.get(Buffer.MODEL_MATRIX5));
            gl.glBufferStorage(GL_UNIFORM_BUFFER, modelBlockSize, null, GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT);
            gl.glBindBuffer(GL_UNIFORM_BUFFER, 0);
            
            gl.glBindBuffer(GL_UNIFORM_BUFFER, bufferNames.get(Buffer.MODEL_MATRIX6));
            gl.glBufferStorage(GL_UNIFORM_BUFFER, modelBlockSize, null, GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT);
            gl.glBindBuffer(GL_UNIFORM_BUFFER, 0);
            
            gl.glBindBuffer(GL_UNIFORM_BUFFER, bufferNames.get(Buffer.MODEL_MATRIX7));
            gl.glBufferStorage(GL_UNIFORM_BUFFER, modelBlockSize, null, GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT);
            gl.glBindBuffer(GL_UNIFORM_BUFFER, 0);
            
            gl.glBindBuffer(GL_UNIFORM_BUFFER, bufferNames.get(Buffer.MODEL_MATRIX8));
            gl.glBufferStorage(GL_UNIFORM_BUFFER, modelBlockSize, null, GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT);
            gl.glBindBuffer(GL_UNIFORM_BUFFER, 0);
            
            gl.glBindBuffer(GL_UNIFORM_BUFFER, bufferNames.get(Buffer.MODEL_MATRIX9));
            gl.glBufferStorage(GL_UNIFORM_BUFFER, modelBlockSize, null, GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT);
            gl.glBindBuffer(GL_UNIFORM_BUFFER, 0);
            
            gl.glBindBuffer(GL_UNIFORM_BUFFER, bufferNames.get(Buffer.MODEL_MATRIX10));
            gl.glBufferStorage(GL_UNIFORM_BUFFER, modelBlockSize, null, GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT);
            gl.glBindBuffer(GL_UNIFORM_BUFFER, 0);
            
            gl.glBindBuffer(GL_UNIFORM_BUFFER, bufferNames.get(Buffer.MODEL_MATRIX11));
            gl.glBufferStorage(GL_UNIFORM_BUFFER, modelBlockSize, null, GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT);
            gl.glBindBuffer(GL_UNIFORM_BUFFER, 0);
            
            gl.glBindBuffer(GL_UNIFORM_BUFFER, bufferNames.get(Buffer.MODEL_MATRIX12));
            gl.glBufferStorage(GL_UNIFORM_BUFFER, modelBlockSize, null, GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT);
            gl.glBindBuffer(GL_UNIFORM_BUFFER, 0);
            
            gl.glBindBuffer(GL_UNIFORM_BUFFER, bufferNames.get(Buffer.MODEL_MATRIX13));
            gl.glBufferStorage(GL_UNIFORM_BUFFER, modelBlockSize, null, GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT);
            gl.glBindBuffer(GL_UNIFORM_BUFFER, 0);
        }

        // map the global matrices buffer into the client space
        globalMatricesPointer = gl.glMapNamedBufferRange(
                bufferNames.get(Buffer.GLOBAL_MATRICES),
                0,
                16 * 4 * 2,
                GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT | GL_MAP_INVALIDATE_BUFFER_BIT); // flags

        // map the model matrix buffer into the client space
        modelMatrixPointer1 = gl.glMapNamedBufferRange(
                bufferNames.get(Buffer.MODEL_MATRIX1),
                0,
                16 * 4,
                GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);
        
        
        modelMatrixPointer2 = gl.glMapNamedBufferRange(
                bufferNames.get(Buffer.MODEL_MATRIX2),
                0,
                16 * 4,
                GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);
        
        modelMatrixPointer3 = gl.glMapNamedBufferRange(
                bufferNames.get(Buffer.MODEL_MATRIX3),
                0,
                16 * 4,
                GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);
        
        modelMatrixPointer4 = gl.glMapNamedBufferRange(
                bufferNames.get(Buffer.MODEL_MATRIX4),
                0,
                16 * 4,
                GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);
        
        modelMatrixPointer5 = gl.glMapNamedBufferRange(
                bufferNames.get(Buffer.MODEL_MATRIX5),
                0,
                16 * 4,
                GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);
        
        modelMatrixPointer6 = gl.glMapNamedBufferRange(
                bufferNames.get(Buffer.MODEL_MATRIX6),
                0,
                16 * 4,
                GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);
        
        modelMatrixPointer7 = gl.glMapNamedBufferRange(
                bufferNames.get(Buffer.MODEL_MATRIX7),
                0,
                16 * 4,
                GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);
        
        modelMatrixPointer8 = gl.glMapNamedBufferRange(
                bufferNames.get(Buffer.MODEL_MATRIX8),
                0,
                16 * 4,
                GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);
        
        modelMatrixPointer9 = gl.glMapNamedBufferRange(
                bufferNames.get(Buffer.MODEL_MATRIX9),
                0,
                16 * 4,
                GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);
        
        modelMatrixPointer10 = gl.glMapNamedBufferRange(
                bufferNames.get(Buffer.MODEL_MATRIX10),
                0,
                16 * 4,
                GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);
        
        modelMatrixPointer11 = gl.glMapNamedBufferRange(
                bufferNames.get(Buffer.MODEL_MATRIX11),
                0,
                16 * 4,
                GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);
        
        modelMatrixPointer12 = gl.glMapNamedBufferRange(
                bufferNames.get(Buffer.MODEL_MATRIX12),
                0,
                16 * 4,
                GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);
        
        modelMatrixPointer13 = gl.glMapNamedBufferRange(
                bufferNames.get(Buffer.MODEL_MATRIX13),
                0,
                16 * 4,
                GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);
        
    }

    // Function for initializing the vertex array
    private void initVertexArray(GL4 gl) {

        // Create a single vertex array object
        gl.glCreateVertexArrays(2, vertexArrayName);

        // Associate the vertex attributes in the vertex array object with the vertex buffer
        gl.glVertexArrayAttribBinding(vertexArrayName.get(0), Semantic.Attr.POSITION, Semantic.Stream.A);
        gl.glVertexArrayAttribBinding(vertexArrayName.get(0), Semantic.Attr.TEXCOORD, Semantic.Stream.A);
        
        gl.glVertexArrayAttribBinding(vertexArrayName.get(1), Semantic.Attr.POSITION, Semantic.Stream.A);
        gl.glVertexArrayAttribBinding(vertexArrayName.get(1), Semantic.Attr.TEXCOORD, Semantic.Stream.A);
    
    
        // Set the format of the vertex attributes in the vertex array object
        gl.glVertexArrayAttribFormat(vertexArrayName.get(0), Semantic.Attr.POSITION, 3, GL_FLOAT, false, 0);
        gl.glVertexArrayAttribFormat(vertexArrayName.get(0), Semantic.Attr.TEXCOORD, 2, GL_FLOAT, false, 3 * 4);

        gl.glVertexArrayAttribFormat(vertexArrayName.get(1), Semantic.Attr.POSITION, 3, GL_FLOAT, false, 0);
        gl.glVertexArrayAttribFormat(vertexArrayName.get(1), Semantic.Attr.TEXCOORD, 2, GL_FLOAT, false, 3 * 4);

        // Enable the vertex attributes in the vertex object
        gl.glEnableVertexArrayAttrib(vertexArrayName.get(0), Semantic.Attr.POSITION);
        gl.glEnableVertexArrayAttrib(vertexArrayName.get(0), Semantic.Attr.TEXCOORD);
        
        gl.glEnableVertexArrayAttrib(vertexArrayName.get(1), Semantic.Attr.POSITION);
        gl.glEnableVertexArrayAttrib(vertexArrayName.get(1), Semantic.Attr.TEXCOORD);


        // Bind the triangle indices in the vertex array object the triangle indices buffer
        gl.glVertexArrayElementBuffer(vertexArrayName.get(0), bufferNames.get(Buffer.ELEMENT));
        gl.glVertexArrayElementBuffer(vertexArrayName.get(1), bufferNames.get(Buffer.ELEMENT_PYRAMID));

        // Bind the vertex array object to the vertex buffer
        gl.glVertexArrayVertexBuffer(vertexArrayName.get(0), Semantic.Stream.A, bufferNames.get(Buffer.VERTEX), 0, (2 + 3) * 4);
        gl.glVertexArrayVertexBuffer(vertexArrayName.get(1), Semantic.Stream.A, bufferNames.get(Buffer.VERTEX_PYRAMID), 0, (2 + 3) * 4);

    }

    private void initTexture(GL4 gl) {
        try {
            // Load texture
        	TextureData textureData = TextureIO.newTextureData(glProfile, new File(textureFilename), false, TextureIO.PNG);
        	TextureData textureData2 = TextureIO.newTextureData(glProfile, new File(textureFilename2), false, TextureIO.PNG);
        	TextureData textureData3 = TextureIO.newTextureData(glProfile, new File(textureFilename3), false, TextureIO.PNG);
        	TextureData textureData4 = TextureIO.newTextureData(glProfile, new File(textureFilename4), false, TextureIO.PNG);
        	TextureData textureData5 = TextureIO.newTextureData(glProfile, new File(textureFilename5), false, TextureIO.PNG);
        	TextureData textureData6 = TextureIO.newTextureData(glProfile, new File(textureFilename6), false, TextureIO.PNG);
        	TextureData textureData7 = TextureIO.newTextureData(glProfile, new File(textureFilename7), false, TextureIO.PNG);
        	TextureData textureData8 = TextureIO.newTextureData(glProfile, new File(textureFilename8), false, TextureIO.PNG);
        	TextureData textureData9 = TextureIO.newTextureData(glProfile, new File(textureFilename9), false, TextureIO.PNG);
        	TextureData textureData10 = TextureIO.newTextureData(glProfile, new File(textureFilename10), false, TextureIO.PNG);
        	TextureData textureData11 = TextureIO.newTextureData(glProfile, new File(textureFilename11), false, TextureIO.PNG);
        	TextureData textureData12 = TextureIO.newTextureData(glProfile, new File(textureFilename12), false, TextureIO.PNG);

            // Generate texture name
        	//number of textures
            gl.glGenTextures(12, textureNames);

            // Bind the texture
            gl.glBindTexture(gl.GL_TEXTURE_2D, textureNames.get(0));
            
            // Specify the format of the texture
            gl.glTexImage2D(gl.GL_TEXTURE_2D, 
                0, 
                textureData.getInternalFormat(), 
                textureData.getWidth(), textureData.getHeight(), 
                textureData.getBorder(),
                textureData.getPixelFormat(), 
                textureData.getPixelType(),
                textureData.getBuffer());
            System.out.println("glTexImage2D " + gl.glGetError());

            // Set the sampler parameters
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

            // Generate mip maps
            gl.glGenerateMipmap(GL_TEXTURE_2D);

            // Deactivate texture
            gl.glBindTexture(GL_TEXTURE_2D, 0);
            
            
            ////////////////////
            // Bind the texture
            gl.glBindTexture(gl.GL_TEXTURE_2D, textureNames.get(1));
            
            // Specify the format of the texture
            gl.glTexImage2D(gl.GL_TEXTURE_2D, 
                0, 
                textureData2.getInternalFormat(), 
                textureData2.getWidth(), textureData2.getHeight(), 
                textureData2.getBorder(),
                textureData2.getPixelFormat(), 
                textureData2.getPixelType(),
                textureData2.getBuffer());
            System.out.println("glTexImage2D " + gl.glGetError());

            // Set the sampler parameters
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

            // Generate mip maps
            gl.glGenerateMipmap(GL_TEXTURE_2D);

            // Deactivate texture
            gl.glBindTexture(GL_TEXTURE_2D, 1);
            //////////////////////
            ////////////////////
            // Bind the texture
            gl.glBindTexture(gl.GL_TEXTURE_2D, textureNames.get(2));
            
            // Specify the format of the texture
            gl.glTexImage2D(gl.GL_TEXTURE_2D, 
                0, 
                textureData3.getInternalFormat(), 
                textureData3.getWidth(), textureData3.getHeight(), 
                textureData3.getBorder(),
                textureData3.getPixelFormat(), 
                textureData3.getPixelType(),
                textureData3.getBuffer());
            System.out.println("glTexImage2D " + gl.glGetError());

            // Set the sampler parameters
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

            // Generate mip maps
            gl.glGenerateMipmap(GL_TEXTURE_2D);

            // Deactivate texture
            gl.glBindTexture(GL_TEXTURE_2D, 2);
            //////////////////////
            ////////////////////
            // Bind the texture
            gl.glBindTexture(gl.GL_TEXTURE_2D, textureNames.get(3));
            
            // Specify the format of the texture
            gl.glTexImage2D(gl.GL_TEXTURE_2D, 
                0, 
                textureData4.getInternalFormat(), 
                textureData4.getWidth(), textureData4.getHeight(), 
                textureData4.getBorder(),
                textureData4.getPixelFormat(), 
                textureData4.getPixelType(),
                textureData4.getBuffer());
            System.out.println("glTexImage2D " + gl.glGetError());

            // Set the sampler parameters
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

            // Generate mip maps
            gl.glGenerateMipmap(GL_TEXTURE_2D);

            // Deactivate texture
            gl.glBindTexture(GL_TEXTURE_2D, 3);
            //////////////////////
            ////////////////////
            // Bind the texture
            gl.glBindTexture(gl.GL_TEXTURE_2D, textureNames.get(4));
            
            // Specify the format of the texture
            gl.glTexImage2D(gl.GL_TEXTURE_2D, 
                0, 
                textureData5.getInternalFormat(), 
                textureData5.getWidth(), textureData5.getHeight(), 
                textureData5.getBorder(),
                textureData5.getPixelFormat(), 
                textureData5.getPixelType(),
                textureData5.getBuffer());
            System.out.println("glTexImage2D " + gl.glGetError());

            // Set the sampler parameters
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

            // Generate mip maps
            gl.glGenerateMipmap(GL_TEXTURE_2D);

            // Deactivate texture
            gl.glBindTexture(GL_TEXTURE_2D, 4);
            //////////////////////
            ////////////////////
            // Bind the texture
            gl.glBindTexture(gl.GL_TEXTURE_2D, textureNames.get(5));
            
            // Specify the format of the texture
            gl.glTexImage2D(gl.GL_TEXTURE_2D, 
                0, 
                textureData6.getInternalFormat(), 
                textureData6.getWidth(), textureData6.getHeight(), 
                textureData6.getBorder(),
                textureData6.getPixelFormat(), 
                textureData6.getPixelType(),
                textureData6.getBuffer());
            System.out.println("glTexImage2D " + gl.glGetError());

            // Set the sampler parameters
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

            // Generate mip maps
            gl.glGenerateMipmap(GL_TEXTURE_2D);

            // Deactivate texture
            gl.glBindTexture(GL_TEXTURE_2D, 5);
            //////////////////////
            ////////////////////
            // Bind the texture
            gl.glBindTexture(gl.GL_TEXTURE_2D, textureNames.get(6));
            
            // Specify the format of the texture
            gl.glTexImage2D(gl.GL_TEXTURE_2D, 
                0, 
                textureData7.getInternalFormat(), 
                textureData7.getWidth(), textureData7.getHeight(), 
                textureData7.getBorder(),
                textureData7.getPixelFormat(), 
                textureData7.getPixelType(),
                textureData7.getBuffer());
            System.out.println("glTexImage2D " + gl.glGetError());

            // Set the sampler parameters
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

            // Generate mip maps
            gl.glGenerateMipmap(GL_TEXTURE_2D);

            // Deactivate texture
            gl.glBindTexture(GL_TEXTURE_2D, 6);
            //////////////////////
            ////////////////////
            // Bind the texture
            gl.glBindTexture(gl.GL_TEXTURE_2D, textureNames.get(7));
            
            // Specify the format of the texture
            gl.glTexImage2D(gl.GL_TEXTURE_2D, 
                0, 
                textureData8.getInternalFormat(), 
                textureData8.getWidth(), textureData8.getHeight(), 
                textureData8.getBorder(),
                textureData8.getPixelFormat(), 
                textureData8.getPixelType(),
                textureData8.getBuffer());
            System.out.println("glTexImage2D " + gl.glGetError());

            // Set the sampler parameters
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

            // Generate mip maps
            gl.glGenerateMipmap(GL_TEXTURE_2D);

            // Deactivate texture
            gl.glBindTexture(GL_TEXTURE_2D, 7);
            //////////////////////
            ////////////////////
            // Bind the texture
            gl.glBindTexture(gl.GL_TEXTURE_2D, textureNames.get(8));
            
            // Specify the format of the texture
            gl.glTexImage2D(gl.GL_TEXTURE_2D, 
                0, 
                textureData9.getInternalFormat(), 
                textureData9.getWidth(), textureData9.getHeight(), 
                textureData9.getBorder(),
                textureData9.getPixelFormat(), 
                textureData9.getPixelType(),
                textureData9.getBuffer());
            System.out.println("glTexImage2D " + gl.glGetError());

            // Set the sampler parameters
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

            // Generate mip maps
            gl.glGenerateMipmap(GL_TEXTURE_2D);

            // Deactivate texture
            gl.glBindTexture(GL_TEXTURE_2D, 8);
            //////////////////////
            ////////////////////
            // Bind the texture
            gl.glBindTexture(gl.GL_TEXTURE_2D, textureNames.get(9));
            
            // Specify the format of the texture
            gl.glTexImage2D(gl.GL_TEXTURE_2D, 
                0, 
                textureData9.getInternalFormat(), 
                textureData9.getWidth(), textureData9.getHeight(), 
                textureData9.getBorder(),
                textureData9.getPixelFormat(), 
                textureData9.getPixelType(),
                textureData9.getBuffer());
            System.out.println("glTexImage2D " + gl.glGetError());

            // Set the sampler parameters
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

            // Generate mip maps
            gl.glGenerateMipmap(GL_TEXTURE_2D);

            // Deactivate texture
            gl.glBindTexture(GL_TEXTURE_2D, 9);
            //////////////////////
            ////////////////////
            // Bind the texture
            gl.glBindTexture(gl.GL_TEXTURE_2D, textureNames.get(10));
            
            // Specify the format of the texture
            gl.glTexImage2D(gl.GL_TEXTURE_2D, 
                0, 
                textureData10.getInternalFormat(), 
                textureData10.getWidth(), textureData10.getHeight(), 
                textureData10.getBorder(),
                textureData10.getPixelFormat(), 
                textureData10.getPixelType(),
                textureData10.getBuffer());
            System.out.println("glTexImage2D " + gl.glGetError());

            // Set the sampler parameters
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

            // Generate mip maps
            gl.glGenerateMipmap(GL_TEXTURE_2D);

            // Deactivate texture
            gl.glBindTexture(GL_TEXTURE_2D, 10);
            //////////////////////
            ////////////////////
            // Bind the texture
            gl.glBindTexture(gl.GL_TEXTURE_2D, textureNames.get(11));
            
            // Specify the format of the texture
            gl.glTexImage2D(gl.GL_TEXTURE_2D, 
                0, 
                textureData11.getInternalFormat(), 
                textureData11.getWidth(), textureData11.getHeight(), 
                textureData11.getBorder(),
                textureData11.getPixelFormat(), 
                textureData11.getPixelType(),
                textureData11.getBuffer());
            System.out.println("glTexImage2D " + gl.glGetError());

            // Set the sampler parameters
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

            // Generate mip maps
            gl.glGenerateMipmap(GL_TEXTURE_2D);

            // Deactivate texture
            gl.glBindTexture(GL_TEXTURE_2D, 11);
            //////////////////////
            ////////////////////
            // Bind the texture
            gl.glBindTexture(gl.GL_TEXTURE_2D, textureNames.get(12));
            
            // Specify the format of the texture
            gl.glTexImage2D(gl.GL_TEXTURE_2D, 
                0, 
                textureData12.getInternalFormat(), 
                textureData12.getWidth(), textureData12.getHeight(), 
                textureData12.getBorder(),
                textureData12.getPixelFormat(), 
                textureData12.getPixelType(),
                textureData12.getBuffer());
            System.out.println("glTexImage2D " + gl.glGetError());

            // Set the sampler parameters
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

            // Generate mip maps
            gl.glGenerateMipmap(GL_TEXTURE_2D);

            // Deactivate texture
            gl.glBindTexture(GL_TEXTURE_2D, 12);
            //////////////////////

        }
        catch (IOException io) {
            io.printStackTrace();
        }
    }
    /*
    private float[] createSphereVertices(float radius, int numH, int numV) {
        // Variables needed for the calculations
        float t1, t2;
        float pi = (float)Math.PI;
        float pi2 = (float)Math.PI*2f;
        float d1 = pi2/numH;
        float d2 = pi/numV;

        // Allocate the data needed to store the necessary positions, normals and texture coordinates
        int numVertices = (numH*(numV-1)+2);
        int numFloats = (3+3+2);
        float[] data = new float[numVertices*numFloats];

        data[0] = 0f; data[1] = radius; data[2] = 0f;
        data[3] = 0f; data[4] = 1f; data[5] = 0f;
        data[6] = 0.5f; data[7] = 1f; 
        for (int j=0; j<numV-1; j++) {
            for (int i=0; i<numH; i++) {
                // Position
                data[(j*numH+i+1)*numFloats] = radius*(float)(Math.sin(i*d1)*Math.sin((j+1)*d2));
                data[(j*numH+i+1)*numFloats+1] = radius*(float)Math.cos((j+1)*d2);
                data[(j*numH+i+1)*numFloats+2] = radius*(float)(Math.cos(i*d1)*Math.sin((j+1)*d2));
                // Normal
                data[(j*numH+i+1)*numFloats+3] = (float)(Math.sin(i*d1)*Math.sin((j+1)*d2));
                data[(j*numH+i+1)*numFloats+4] = (float)Math.cos((j+1)*d2);
                data[(j*numH+i+1)*numFloats+5] = (float)(Math.cos(i*d1)*Math.sin((j+1)*d2));
                // UV
                data[(j*numH+i+1)*numFloats+6] = (float)(Math.asin(data[(j*numH+i+1)*numFloats+3])/Math.PI) + 0.5f;
                data[(j*numH+i+1)*numFloats+7] = (float)(Math.asin(data[(j*numH+i+1)*numFloats+4])/Math.PI) + 0.5f;
            }
        }
        data[(numVertices-1)*numFloats] = 0f; data[(numVertices-1)*numFloats+1] = -radius; data[(numVertices-1)*numFloats+2] = 0f;
        data[(numVertices-1)*numFloats+3] = 0f; data[(numVertices-1)*numFloats+4] = -1f; data[(numVertices-1)*numFloats+5] = 0f;
        data[(numVertices-1)*numFloats+6] = 0.5f; data[(numVertices-1)*numFloats+7] = 0f;

        return data;
    }*/
    
    

    // Private class representing a vertex program
    private class Program {

        // The name of the program
        public int name = 0;

        // Constructor
        public Program(GL4 gl, String root, String vertex, String fragment) {

            // Instantiate a complete vertex shader
            ShaderCode vertShader = ShaderCode.create(gl, GL_VERTEX_SHADER, this.getClass(), root, null, vertex,
                    "vert", null, true);

            // Instantiate a complete fragment shader
            ShaderCode fragShader = ShaderCode.create(gl, GL_FRAGMENT_SHADER, this.getClass(), root, null, fragment,
                    "frag", null, true);

            // Create the shader program
            ShaderProgram shaderProgram = new ShaderProgram();

            // Add the vertex and fragment shader
            shaderProgram.add(vertShader);
            shaderProgram.add(fragShader);

            // Initialize the program
            shaderProgram.init(gl);

            // Store the program name (nonzero if valid)
            name = shaderProgram.program();

            // Compile and link the program
            shaderProgram.link(gl, System.out);
        }
    }

    // Private class to provide an semantic interface between Java and GLSL
	private static class Semantic {

		public interface Attr {
			int POSITION = 0;
			int TEXCOORD = 1;
		}

		public interface Uniform {
			int TRANSFORM0 = 1;
			int TRANSFORM1 = 2;
		}

		public interface Stream {
			int A = 0;
		}
	}
}
