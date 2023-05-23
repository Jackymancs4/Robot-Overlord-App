package com.marginallyclever.robotoverlord.tools;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.Ray;
import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.robotoverlord.RayHit;
import com.marginallyclever.robotoverlord.clipboard.Clipboard;
import com.marginallyclever.robotoverlord.components.CameraComponent;
import com.marginallyclever.robotoverlord.components.PoseComponent;
import com.marginallyclever.robotoverlord.components.ShapeComponent;
import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.entity.EntityManager;
import com.marginallyclever.robotoverlord.renderpanel.OpenGLRenderPanel;
import com.marginallyclever.robotoverlord.swinginterface.UndoSystem;
import com.marginallyclever.robotoverlord.swinginterface.edits.SelectEdit;
import com.marginallyclever.robotoverlord.systems.render.Viewport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.*;

public class SelectionTool implements EditorTool {
    private static final Logger logger = LoggerFactory.getLogger(SelectionTool.class);
    public static final String PICK_POINT_NAME = "pick point";
    private final EntityManager entityManager;
    private final Viewport viewport;
    private boolean isActive=false;

    public SelectionTool(EntityManager entityManager,Viewport viewport) {
        super();
        this.entityManager = entityManager;
        this.viewport = viewport;
    }

    /**
     * This method is called when the tool is activated. It receives the SelectedItems object containing the selected
     * entities and their initial world poses.
     *
     * @param list The selected items to be manipulated by the tool.
     */
    @Override
    public void activate(List<Entity> list) {
        isActive=true;
    }

    /**
     * This method is called when the tool is deactivated. It allows the tool to perform any necessary cleanup
     * actions before another tool takes over.
     */
    @Override
    public void deactivate() {
        isActive=false;
    }

    /**
     * Handles mouse input events for the tool.
     *
     * @param event The MouseEvent object representing the input event.
     */
    @Override
    public void handleMouseEvent(MouseEvent event) {
        if(!isActive) return;

        // if they dragged the cursor around before releasing the mouse button, don't pick.
        if (event.getClickCount() == 2) {
            pickItemUnderCursor();
        }
    }

    /**
     * Handles keyboard input events for the tool.
     *
     * @param event The KeyEvent object representing the input event.
     */
    @Override
    public void handleKeyEvent(KeyEvent event) {

    }

    /**
     * Updates the tool's internal state, if necessary.
     *
     * @param deltaTime Time elapsed since the last update.
     */
    @Override
    public void update(double deltaTime) {

    }

    /**
     * Renders any tool-specific visuals to the 3D scene.
     *
     * @param gl2 The OpenGL context.
     */
    @Override
    public void render(GL2 gl2) {

    }

    @Override
    public void setViewport(Viewport viewport) {

    }

    /**
     * Returns true if the tool is active (was clicked correctly and could be dragged)
     *
     * @return true if the tool is active (was clicked correctly and could be dragged)
     */
    @Override
    public boolean isInUse() {
        return false;
    }

    /**
     * Force cancel the tool.  useful if two tools are activated at once.
     */
    @Override
    public void cancelUse() {

    }

    /**
     * Returns the point on the tool clicked by the user.  This is used to determine which tool is closer to the user.
     *
     * @return the point on the tool clicked by the user.
     */
    @Override
    public Point3d getStartPoint() {
        return null;
    }

    @Override
    public void mouseMoved(MouseEvent event) {

    }

    @Override
    public void mousePressed(MouseEvent event) {

    }

    @Override
    public void mouseDragged(MouseEvent event) {

    }

    @Override
    public void mouseReleased(MouseEvent event) {

    }

    private void pickItemUnderCursor() {
        Entity found = findEntityUnderCursor();
        logger.debug((found==null)?"found=null":"found=" + found.getName());

        // TODO shift + select to grow selection
        // TODO ctrl + select to toggle item from selection.

        List<Entity> list = new ArrayList<>();
        if(found!=null) list.add(found);

        UndoSystem.addEvent(this,new SelectEdit(Clipboard.getSelectedEntities(),list));
    }

    /**
     * Use ray tracing to find the Entity at the cursor position closest to the camera.
     * @return the name of the item under the cursor, or -1 if nothing was picked.
     */
    private Entity findEntityUnderCursor() {
        CameraComponent camera = viewport.getCamera();
        if(camera==null) return null;

        Ray ray = viewport.getRayThroughCursor();

        // traverse the scene Entities and find the ShapeComponent that collides with the ray.
        List<RayHit> rayHits = findRayIntersections(ray);
        if(rayHits.size()==0) return null;

        rayHits.sort(Comparator.comparingDouble(o -> o.distance));

        // set the pick point
        Entity pickPoint = entityManager.getRoot().findChildNamed("pick point");
        if(pickPoint==null) createPickPoint();

        Vector3d from = ray.getPoint(rayHits.get(0).distance);
        Vector3d to = new Vector3d(from);
        to.add(rayHits.get(0).normal);
        Matrix4d m = MatrixHelper.createIdentityMatrix4();
        //Matrix4d m = new Matrix4d();
        //Matrix3d lookAt = MatrixHelper.lookAt(from,to);
        //m.set(lookAt);
        m.setTranslation(from);
        pickPoint.getComponent(PoseComponent.class).setWorld(m);


        return rayHits.get(0).target.getEntity();
    }

    /**
     * test ray intersection with all entities in the scene.
     * @param ray the ray to test.
     */
    private List<RayHit> findRayIntersections(Ray ray) {
        List<RayHit> rayHits = new ArrayList<>();

        Queue<Entity> toTest = new LinkedList<>(entityManager.getEntities());
        while(!toTest.isEmpty()) {
            Entity entity = toTest.remove();
            toTest.addAll(entity.getChildren());

            ShapeComponent shape = entity.getComponent(ShapeComponent.class);
            if(shape==null) continue;
            RayHit hit = shape.intersect(ray);
            if(hit!=null) rayHits.add(hit);
        }
        return rayHits;
    }

    private void createPickPoint() {
        Entity pickPoint = entityManager.getRoot().findChildNamed(PICK_POINT_NAME);
        if(pickPoint == null) {
            pickPoint = new Entity(PICK_POINT_NAME);
            entityManager.addEntityToParent(pickPoint,entityManager.getRoot());
        }
    }
}