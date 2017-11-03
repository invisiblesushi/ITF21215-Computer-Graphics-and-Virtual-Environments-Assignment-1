package lwjgldemo;

import org.joml.camera.OrthoCameraControl;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Random;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.system.MemoryUtil.*;

/**
 * Showcases simple ortho rendering with shaders and a camera with mouse controls.
 * 
 * @author Kai Burjack
 */
public class OrthoDemo {
    private static GLFWErrorCallback errorCallback;
    private static GLFWKeyCallback keyCallback;
    private static GLFWFramebufferSizeCallback fbCallback;
    private static GLFWWindowSizeCallback wsCallback;
    private static GLFWMouseButtonCallback mbCallback;
    private static GLFWCursorPosCallback cpCallback;
    private static GLFWScrollCallback sCallback;
    private static int fbWidth = 300;
    private static int fbHeight = 300;
    private static int windowWidth = 300;
    private static int windowHeight = 300;
    private static OrthoCameraControl cam = new OrthoCameraControl(300);

    public static void main(String[] args) {
        glfwSetErrorCallback(errorCallback = GLFWErrorCallback.createPrint(System.err));
        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");
        glfwWindowHint(GLFW_SAMPLES, 4);
        long window = glfwCreateWindow(windowWidth, windowHeight, "Hello Orthographic Projection!", NULL, NULL);
        if (window == NULL)
            throw new RuntimeException("Failed to create the GLFW window");
        glfwSetKeyCallback(window, keyCallback = new GLFWKeyCallback() {
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                    glfwSetWindowShouldClose(window, true);
            }
        });
        glfwSetFramebufferSizeCallback(window, fbCallback = new GLFWFramebufferSizeCallback() {
            public void invoke(long window, int w, int h) {
                if (w > 0 && h > 0) {
                    fbWidth = w;
                    fbHeight = h;
                }
            }
        });
        glfwSetWindowSizeCallback(window, wsCallback = new GLFWWindowSizeCallback() {
            public void invoke(long window, int width, int height) {
                windowWidth = width;
                windowHeight = height;
                cam.setSize(width, height);
            }
        });
        glfwSetMouseButtonCallback(window, mbCallback = new GLFWMouseButtonCallback() {
            public void invoke(long window, int button, int action, int mods) {
                if (action == GLFW_PRESS) {
                    cam.onMouseDown(button);
                } else {
                    cam.onMouseUp(button);
                }
            }
        });
        glfwSetCursorPosCallback(window, cpCallback = new GLFWCursorPosCallback() {
            public void invoke(long window, double xpos, double ypos) {
                cam.onMouseMove((int) xpos, windowHeight - (int) ypos);
            }
        });
        glfwSetScrollCallback(window, sCallback = new GLFWScrollCallback() {
            public void invoke(long window, double xoffset, double yoffset) {
                if (yoffset > 0)
                    cam.zoom((float) 1.1f);
                else
                    cam.zoom((float) 1.0f / 1.1f);
            }
        });
        /* Fix for HiDPI displays */
        IntBuffer framebufferSize = BufferUtils.createIntBuffer(2);
        nglfwGetFramebufferSize(window, memAddress(framebufferSize), memAddress(framebufferSize) + 4);
        fbWidth = framebufferSize.get(0);
        fbHeight = framebufferSize.get(1);
        cam.setSize(windowWidth, windowHeight);

        /* Make context current and init OpenGL context and objects */
        glfwMakeContextCurrent(window);
        GL.createCapabilities();
        glClearColor(0.97f, 0.97f, 0.97f, 1.0f);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        int program = glCreateProgram();
        int vs = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vs, "uniform mat4 projMatrix;" + "void main(void) {" + "  gl_Position = projMatrix * gl_Vertex;" + "}");
        glCompileShader(vs);
        glAttachShader(program, vs);
        int fs = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fs, "void main(void) {" + "  gl_FragColor = vec4(0.2, 0.2, 0.2, 0.6);" + "}");
        glCompileShader(fs);
        glAttachShader(program, fs);
        glLinkProgram(program);
        glUseProgram(program);
        int matLocation = glGetUniformLocation(program, "projMatrix");

        /* Game loop */
        FloatBuffer fb = BufferUtils.createFloatBuffer(16);
        Random rnd = new Random();
        while (!glfwWindowShouldClose(window)) {
            glViewport(0, 0, fbWidth, fbHeight);
            glClear(GL_COLOR_BUFFER_BIT);

            /* Upload matrix to shader program */
            glUniformMatrix4fv(matLocation, false, cam.viewproj().get(fb));

            /* Draw a few simple quads */
            glBegin(GL_QUADS);
            rnd.setSeed(0L);
            for (int i = 0; i < 50; i++) {
                float x = (rnd.nextFloat() * 2.0f - 1.0f) * 1000.0f;
                float y = (rnd.nextFloat() * 2.0f - 1.0f) * 1000.0f;
                float s = (rnd.nextFloat() + 0.2f) * 100.0f;
                glVertex2f(x, y);
                glVertex2f(x + s, y);
                glVertex2f(x + s, y + s);
                glVertex2f(x, y + s);
            }
            glEnd();

            glfwSwapBuffers(window);
            glfwPollEvents();
        }
        glfwDestroyWindow(window);
        glfwTerminate();
        errorCallback.free();
        keyCallback.free();
        fbCallback.free();
        wsCallback.free();
        mbCallback.free();
        cpCallback.free();
        sCallback.free();
    }
}