public class Point {
    float x;
    float y;

    public Point(){
        super();
    }

    public Point(float x, float y){
        if(x <0 || x > 10 || y<0 || y>10) {
            throw new Error("the coordinate is 0-10.");
        }
        this.x = x;
        this.y = y;
    }

    public String toString(){
        return "(" + (this.x) + "," + (this.y) + ")";
    }
}
