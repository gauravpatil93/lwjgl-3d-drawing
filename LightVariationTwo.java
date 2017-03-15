public class LightVariationTwo {
    float[] specularReflection = {0.1f, 0.1f, 0.1f, 1.0f};
    float[] lightPosition = {1,0,0,0};
    float shineFactor = 0.0f;

    public float[] getSpecularReflection() {
        return this.specularReflection;
    }

    public float[] getLightPosition() {
        return this.lightPosition;
    }

    public float getShineFactor() {
        return this.shineFactor;
    }

    public void setSpecularReflection( float[] specularReflection ) {
        this.specularReflection = specularReflection;
    }

    public void setLightPosition( float[] lightPosition ) {
        this.lightPosition = lightPosition;
    }

    public void setShineFactor( float shineFactor ) {
        this.shineFactor = shineFactor;
    }
}