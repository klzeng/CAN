import java.io.Serializable;


/*
 * zone: the coordinate space belongs to a node:
 * - start_point: left-bottom corner point
 * - end_point: right-top corner point
 *
 */
public class Zone{
    Point start_point;  // bottom left point
    Point end_point;    // top right point

    // constructor
    public Zone(){
        this.start_point = new Point();
        this.end_point = new Point();
    }

    public Zone(Point start, Point end){
        this.start_point = start;
        this.end_point = end;
    }

    public float getWidth(){
        return end_point.x - start_point.x;
    }

    public float getHeight(){
        return end_point.y - start_point.y;
    }

    public float getMidX(){
        return this.start_point.x + this.getWidth()/2;
    }

    public float getMidY(){
        return this.start_point.y + this.getHeight()/2;
    }

    public boolean inZone(Point p){
        return (p.x<=end_point.x && p.y<=end_point.y && p.x>=start_point.x && p.y>=start_point.y);
    }

    public float[] getCoordinateArray(){
        return new float[]{this.start_point.x, this.start_point.y, this.end_point.x, this.end_point.y};
    }

    public String toString(){
        return this.start_point +","+  this.end_point;
    }
}
