package org.joml.camera;

import org.joml.Math;
import org.joml.Matrix3x2f;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

/**
 * Allows to control an orthographic camera with the mouse via panning and zooming.
 * <p>
 * Call the following methods:
 * <ul>
 * <li>{@link #setSize(int, int)} when the window/control size changes
 * <li>{@link #onMouseDown(int)} when further calls to {@link #onMouseMove(int, int)} should pan/rotate the view
 * <li>{@link #onMouseMove(int, int)} everytime the mouse moves
 * <li>{@link #onMouseUp(int)} when panning/rotating should stop
 * <li>{@link #zoom(float)} to zoom in/out
 * <li>{@link #viewproj()} to obtain the current view-projection matrix
 * <li>{@link #center(float, float)} to center the view onto the given coordinate
 * </ul>
 * 
 * @author Kai Burjack
 */
public class OrthoCameraControl {

    public static int MOUSE_LEFT = 0;
    public static int MOUSE_RIGHT = 1;
    public static int MOUSE_CENTER = 2;

    private Matrix3x2f view = new Matrix3x2f();
    private Matrix4f viewproj = new Matrix4f();
    private Matrix4f invviewproj = new Matrix4f();
    private int[] vp = new int[4];
    private float mouseX, mouseY;
    private float mouseDownX, mouseDownY;
    private boolean[] mouseDown = new boolean[3];
    private Vector2f v = new Vector2f();
    private Vector3f v3 = new Vector3f();

    private float minRotateWinDistance2 = 100.0f * 100.0f;

    /**
     * @param extents
     *            the initial extents in world coordinates
     */
    public OrthoCameraControl(float extents) {
        view.view(-extents, extents, -extents, extents);
        update();
    }

    /**
     * @param minRotateWinDistance
     *            the minimum distance in window coordinates/pixels between
     *            the mouse down position and the current mouse position so
     *            that rotation is allowed
     */
    public void setMinRotateWinDistance(float minRotateWinDistance) {
        this.minRotateWinDistance2 = minRotateWinDistance * minRotateWinDistance;
    }

    /**
     * @param width
     *            the width of the control/window in window coordinates/pixels
     * @param height
     *            the height of the control/window in window coordinates/pixels
     */
    public void setSize(int width, int height) {
        vp[0] = 0;
        vp[1] = 0;
        vp[2] = width;
        vp[3] = height;
        update();
    }

    public Matrix4f viewproj() {
        return viewproj;
    }
    public Matrix4f invviewproj() {
        return invviewproj;
    }

    private void update() {
        float aspect = (float) vp[2] / vp[3];
        viewproj.setOrtho2D(-aspect, +aspect, -1, +1)
                .mul(view)
                .invertAffine(invviewproj);
    }

    /**
     * @param x
     *            the x coordiante of the point to center on in world coordinates
     * @param y
     *            the y coordiante of the point to center on in world coordinates
     */
    public void center(float x, float y) {
        view.setTranslation(0, 0).translate(-x, -y);
        update();
    }

    public void onMouseDown(int button) {
        mouseDownX = mouseX;
        mouseDownY = mouseY;
        mouseDown[button] = true;
        if (button == MOUSE_CENTER) {
            /* Reset rotation with mouse position as center */
            view.positiveX(v);
            float ang = (float) Math.atan2(v.y, v.x);
            Vector2f ndc = ndc(mouseDownX, mouseDownY);
            view.translateLocal(-ndc.x, -ndc.y)
                .rotateLocal(ang)
                .translateLocal(ndc.x, ndc.y);
            update();
        }
    }

    public void onMouseUp(int button) {
        mouseDown[button] = false;
    }

    /**
     * @param winX
     *            the x coordinate in window coordinates/pixels
     * @param winY
     *            the y coordinate in window coordinates/pixels
     */
    public void onMouseMove(int winX, int winY) {
        Vector2f ndc;
        if (mouseDown[MOUSE_LEFT]) {
            /* Move */
            ndc = ndc(winX, winY);
            float x0 = ndc.x, y0 = ndc.y;
            ndc = ndc(mouseX, mouseY);
            float x1 = ndc.x, y1 = ndc.y;
            view.translateLocal(x0 - x1, y0 - y1);
            update();
        } else if (mouseDown[MOUSE_RIGHT]) {
            /* Check if rotation is possible */
            float dx = winX - mouseDownX;
            float dy = winY - mouseDownY;
            if (dx * dx + dy * dy > minRotateWinDistance2) {
                /* Rotate */
                float dx0 = winX - mouseDownX, dy0 = winY - mouseDownY;
                float dx1 = mouseX - mouseDownX, dy1 = mouseY - mouseDownY;
                float ang = (float) Math.atan2(dx1 * dy0 - dy1 * dx0, dx1 * dx0 + dy1 * dy0);
                ndc = ndc(mouseDownX, mouseDownY);
                view.translateLocal(-ndc.x, -ndc.y)
                    .rotateLocal(ang)
                    .translateLocal(ndc.x, ndc.y);
                update();
            }
        }
        mouseX = winX;
        mouseY = winY;
    }

    private Vector2f ndc(float winX, float winY) {
        float aspect = (float) vp[2] / vp[3];
        float x = (winX / vp[2] * 2.0f - 1.0f) * aspect;
        float y = winY / vp[3] * 2.0f - 1.0f;
        return v.set(x, y);
    }

    /**
     * @param scale
     *            the scale factor. &lt; 1.0 to zoom out; &gt; 1.0 to zoom in
     */
    public void zoom(float scale) {
        Vector2f ndc = ndc(mouseX, mouseY);
        view.translateLocal(-ndc.x, -ndc.y)
            .scaleLocal(scale, scale)
            .translateLocal(ndc.x, ndc.y);
        update();
    }

    /**
     * @param dest
     *            contains the view rectangle as {x: minX, y: minY, z: maxX, w: maxY}
     * @return dest
     */
    public Vector4f viewRect(Vector4f dest) {
        float minX = Float.POSITIVE_INFINITY, minY = Float.POSITIVE_INFINITY;
        float maxX = Float.NEGATIVE_INFINITY, maxY = Float.NEGATIVE_INFINITY;
        for (int i = 0; i < 4; i++) {
            float x = ((i & 1) << 1) - 1.0f;
            float y = (((i >>> 1) & 1) << 1) - 1.0f;
            invviewproj.transformPosition(v3.set(x, y, 0));
            minX = minX < v3.x ? minX : v3.x;
            minY = minY < v3.y ? minY : v3.y;
            maxX = maxX > v3.x ? maxX : v3.x;
            maxY = maxY > v3.y ? maxY : v3.y;
        }
        return dest.set(minX, minY, maxX, maxY);
    }

    /**
     * @param cornerDest
     *            the corner at NDC (-1, -1) of the view
     * @param xDest
     *            the direction and length (in world coordinates) of the view along the NDC x axis
     * @param yDest
     *            the direction and length (in world coordinates) of the view along the NDC y axis
     */
    public void viewSpan(Vector2f cornerDest, Vector2f xDest, Vector2f yDest) {
        invviewproj.transformPosition(v3.set(-1, -1, 0));
        cornerDest.set(v3.x, v3.y);
        xDest.set(2*invviewproj.m00(), 2*invviewproj.m10());
        yDest.set(2*invviewproj.m01(), 2*invviewproj.m11());
    }

}
