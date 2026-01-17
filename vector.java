public class vector {
    
    public float x;
    public float y;

    public vector(float x, float y){
        this.x = x;
        this.y = y;
    }

    public float getX(){
        return this.x;
    }

    public float getY(){
        return this.y;
    }

    public void setX(float x){
        this.x = x;
    }

    public void setY(float y){
        this.y = y;
    }


    public double getDist(vector other){
        double dist = Math.sqrt((this.x - other.getX())*(this.x - other.getX()) + (this.y - other.getY())*(this.y - other.getY()));
        return dist;
    }

    public float getAngle(vector other){
        float angle = (float) Math.atan((Math.abs(this.y - other.getY()))/(Math.abs(this.x - other.getX())));
        return angle;
    }

    public vector displacement(vector other){
        float xdisp = other.x - this.x;
        float ydisp = other.y - this.y;

        return new vector(xdisp, ydisp);
    }

    public vector multiply(float factor){
        return new vector(this.x * factor, this.y * factor);
    }

    public vector divide(float factor){
        return new vector(this.x/factor, this.y/factor);
    }

    public vector addVector(vector other){
        return new vector(this.x + other.x, this.y + other.y);
    }

    public vector subVector(vector other){
        return new vector(this.x - other.x, this.y - other.y);
    }

    public float magnitude(){
        return (float)(Math.sqrt(this.x*this.x + this.y*this.y));
    }

    public vector normalization(){
        float xnorm = (float) (this.getX()/this.magnitude());
        float ynorm = (float) (this.getY()/this.magnitude());
        return new vector(xnorm, ynorm);
    }

    public vector copy() {
        return new vector(this.x, this.y);
    }

}