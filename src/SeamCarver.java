/*
 * SeamCarver
 * Alex Eidt
 */

public interface SeamCarver {
    int getWidth();
    int getHeight();
    int[] getImage();
    void setUpdate(boolean update);
    void setEnergy(int x, int y, int val);
    int add(int count, boolean highlight, int color);
    boolean add(boolean highlight, int color);
    int remove(int count, boolean highlight, int color);
    boolean remove(boolean highlight, int color);
    void updateImage(boolean highlight, int color);
}
