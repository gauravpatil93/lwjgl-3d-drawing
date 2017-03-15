import java.util.ArrayList;
/**
 * The Scene class wraps the shapes, light, color of the light, scene number and the Ka value
 * @author gaurav
 *
 */
public class Scene {
    ArrayList<Shape3D> shapes = new ArrayList<Shape3D>();
    int sceneNumber;
    float ka;
    float [] sceneLightColor;

    public String title;

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void addShape(Shape3D shape) {
        this.shapes.add(shape);
    }

    public ArrayList<Shape3D> getShapesList() {
        return this.shapes;
    }

    public int getSceneNumber() {
        return this.sceneNumber;
    }

    public void setSceneNumber(int sceneNumber) {
        this.sceneNumber = sceneNumber;
    }

    public void setLightFactor(float ka) {
        this.ka = ka;
    }

    public float getLightFactor() {
        return this.ka;
    }

    public void setSceneLightColor(float[] sceneLightColor) {
        this.sceneLightColor = sceneLightColor;
    }

    public float[] getSceneLight() {
        return this.sceneLightColor;
    }
}
