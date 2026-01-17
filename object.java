public class object {
    vector position;
    float radius;
    vector velocity;
    boolean boundary;

    public object(vector position, float radius, vector velocity, boolean boundary){
        this.position = position;
        this.radius = radius;
        this.velocity = velocity;
        this.boundary = false;
    }

    public void updateGravity(vector gravity, double drawInt){
        this.velocity = this.velocity.addVector(gravity).multiply((float)(drawInt));
    }

    public vector updateDiffusion(float diffusionRate, object leftVect, object rightVect, object upVect, object downVect){
        //vector initialVect = this.velocity.subVector((((leftVect.addVector(rightVect)).addVector(upVect)).addVector(downVect)).subVector(this.velocity.multiply(4f))).multiply(diffusionRate);
        //this.velocity = initialVect.addVector((((leftVect.addVector(rightVect)).addVector(upVect)).addVector(downVect)).divide(diffusionRate*4 + 1)).multiply(diffusionRate);
        vector divergence = rightVect.velocity.subVector(leftVect.velocity).addVector(upVect.velocity.subVector(downVect.velocity));
        
        if(leftVect.boundary == true || downVect.boundary == true){
            this.velocity = this.velocity.subVector(divergence.divide(3f));
        }
        else if(rightVect.boundary == true || upVect.boundary == true){
            this.velocity = this.velocity.addVector(divergence.divide(3f));
        }
        else{
            this.velocity = this.velocity.addVector(divergence.divide(4f));
        }

        return this.velocity;
    }
}
