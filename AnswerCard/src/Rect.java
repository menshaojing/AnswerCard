/**
 * @ClassName Rect
 * @Description TODO
 * @Author menshaojing
 * @Date 2021/6/28 13:52
 * @Version 1.0
 */
public class Rect {
    public int x,y,w,h;

    public static void main(String[] args) {
       Rect rect=new Rect(25,77,161,130);
        System.out.println( rect.intersect(new Rect(129,112,24,56)));
    }
    public Rect() {
    }

    public Rect(int x, int y, int w, int h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }
    public Rect(double x, double y, double w, double h) {
        this.x = (int) x;
        this.y = (int) y;
        this.w = (int) w;
        this.h = (int) h;
    }

    public void set(double x, double y, double w, double h) {
        this.x = (int) x;
        this.y = (int) y;
        this.w = (int) w;
        this.h = (int) h;
    }

    static int is_rect_intersect(int x01, int x02, int y01, int y02,
                                 int x11, int x12, int y11, int y12)
    {
        int zx = Math.abs(x01 + x02 -x11 - x12);
        int x  = Math.abs(x01 - x02) + Math.abs(x11 - x12);
        int zy = Math.abs(y01 + y02 - y11 - y12);
        int y  = Math.abs(y01 - y02) + Math.abs(y11 - y12);
        if(zx <= x && zy <= y)
            return 1;
        else
            return 0;
    }

    public boolean intersect(Rect target) {
        return is_rect_intersect(this, target);
    }

    public static boolean is_rect_intersect(Rect a,Rect b)
    {
        int x01=a.x;
        int x02=a.x+a.w;
        int y01=a.y;
        int y02=a.y+a.h;
        int x11=b.x;
        int x12=b.x+b.w;
        int y11=b.y;
        int y12=b.y+b.h;
        int zx = Math.abs(x01 + x02 -x11 - x12);
        int x  = Math.abs(x01 - x02) + Math.abs(x11 - x12);
        int zy = Math.abs(y01 + y02 - y11 - y12);
        int y  = Math.abs(y01 - y02) + Math.abs(y11 - y12);
        if(zx <= x && zy <= y)
        {   return true;}
        else{
            return false;}
    }
}
