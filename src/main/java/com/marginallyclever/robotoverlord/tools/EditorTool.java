package com.marginallyclever.robotoverlord.tools;

import com.jogamp.opengl.GL2;
import com.marginallyclever.robotoverlord.Viewport;
import com.marginallyclever.robotoverlord.components.CameraComponent;

import javax.vecmath.Point3d;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

/**
 * Interface for tools that can be used to visually edit the scene.
 * @since 2.3.0
 * @author Dan Royer
 */
public interface EditorTool {

    /**
     * This method is called when the tool is activated. It receives the SelectedItems object containing the selected
     * entities and their initial world poses.
     *
     * @param selectedItems The selected items to be manipulated by the tool.
     */
    void activate(SelectedItems selectedItems);

    /**
     * This method is called when the tool is deactivated. It allows the tool to perform any necessary cleanup
     * actions before another tool takes over.
     */
    void deactivate();

    /**
     * Handles mouse input events for the tool.
     *
     * @param event The MouseEvent object representing the input event.
     */
    void handleMouseEvent(MouseEvent event);

    /**
     * Handles keyboard input events for the tool.
     *
     * @param event The KeyEvent object representing the input event.
     */
    void handleKeyEvent(KeyEvent event);

    /**
     * Updates the tool's internal state, if necessary.
     *
     * @param deltaTime Time elapsed since the last update.
     */
    void update(double deltaTime);

    /**
     * Renders any tool-specific visuals to the 3D scene.
     */
    void render(GL2 gl2);

    void setViewport(Viewport viewport);

    /**
     * Returns true if the tool is active (was clicked correctly and could be dragged)
     * @return true if the tool is active (was clicked correctly and could be dragged)
     */
    boolean isInUse();

    /**
     * Force cancel the tool.  useful if two tools are activated at once.
     */
    void cancelUse();

    /**
     * Returns the point on the tool clicked by the user.  This is used to determine which tool is closer to the user.
     * @return the point on the tool clicked by the user.
     */
    Point3d getStartPoint();

    void mouseMoved(MouseEvent event);
    void mousePressed(MouseEvent event);
    void mouseDragged(MouseEvent event);
    void mouseReleased(MouseEvent event);
}