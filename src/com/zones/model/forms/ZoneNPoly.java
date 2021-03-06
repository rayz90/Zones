package com.zones.model.forms;

import java.util.List;

import com.zones.model.ZoneForm;
import com.zones.persistence.Vertice;

/**
 * A not so primitive npoly zone
 * 
 * 
 * @author durgus, Meaglin
 */
public class ZoneNPoly extends ZoneForm {
    private int[] _x;
    private int[] _y;
    private int   _z1;
    private int   _z2;
    private long   _size;

    public ZoneNPoly(int[] x, int[] y, int z1, int z2) {
        _x = x;
        _y = y;
        _z1 = z1;
        _z2 = z2;
        if (_z1 > _z2) // switch them if alignment is wrong
        {
            _z1 = z2;
            _z2 = z1;
        }
        calculateSize();
    }

    public ZoneNPoly(List<Vertice> vertices, int minz, int maxz) {
        _x = new int[vertices.size()];
        _y = new int[vertices.size()];
        for (Vertice v : vertices) {
            _x[v.getVertexorder()] = v.getX();
            _y[v.getVertexorder()] = v.getY();
        }
        _z1 = minz;
        _z2 = maxz;
        if (_z1 > _z2) // switch them if alignment is wrong
        {
            _z1 = maxz;
            _z2 = minz;
        }
        calculateSize();
    }

    @Override
    public boolean isInsideZone(int x, int y) {
        boolean inside = false;
        for (int i = 0, j = _x.length - 1; i < _x.length; j = i++) {
            if(_y[i] == _y[j] && _y[i] == y && x <= max(_x[i], _x[j]) && x >= min(_x[i], _x[j])) return true;
            if(_x[i] == _x[j] && _x[i] == x && y <= max(_y[i], _y[j]) && y >= min(_y[i], _y[j])) return true;

            if ((((_y[i] <= y) && (y < _y[j])) || ((_y[j] <= y) && (y < _y[i]))) && (x < (_x[j] - _x[i]) * (y - _y[i]) / (_y[j] - _y[i]) + _x[i])) {
                inside = !inside;
            }
        }
        return inside;
    }

    private static final int min(int a, int b) {
        return a > b ? b : a;
    }
    
    private static final int max(int a, int b) {
        return a > b ? a : b;
    }
    
    @Override
    public boolean intersectsRectangle(int ax1, int ax2, int ay1, int ay2) {
        int tX, tY, uX, uY;

        // First check if a point of the polygon lies inside the rectangle
        if (_x[0] > ax1 && _x[0] < ax2 && _y[0] > ay1 && _y[0] < ay2)
            return true;

        // Or a point of the rectangle inside the polygon
        if (isInsideZone(ax1, ay1, (_z2 - 1)))
            return true;

        // If the first point wasn't inside the rectangle it might still have
        // any line crossing any side
        // of the rectangle

        // Check every possible line of the polygon for a collision with any of
        // the rectangles side
        for (int i = 0; i < _y.length; i++) {
            tX = _x[i];
            tY = _y[i];
            uX = _x[(i + 1) % _x.length];
            uY = _y[(i + 1) % _x.length];

            // Check if this line intersects any of the four sites of the
            // rectangle
            if (lineSegmentsIntersect(tX, tY, uX, uY, ax1, ay1, ax1, ay2))
                return true;
            if (lineSegmentsIntersect(tX, tY, uX, uY, ax1, ay1, ax2, ay1))
                return true;
            if (lineSegmentsIntersect(tX, tY, uX, uY, ax2, ay2, ax1, ay2))
                return true;
            if (lineSegmentsIntersect(tX, tY, uX, uY, ax2, ay2, ax2, ay1))
                return true;
        }

        return false;
    }

    @Override
    public double getDistanceToZone(int x, int y) {
        double test, shortestDist = Math.pow(_x[0] - x, 2) + Math.pow(_y[0] - y, 2);

        for (int i = 1; i < _y.length; i++) {
            test = Math.pow(_x[i] - x, 2) + Math.pow(_y[i] - y, 2);
            if (test < shortestDist)
                shortestDist = test;
        }

        return Math.sqrt(shortestDist);
    }

    @Override
    public int getLowZ() {
        return _z1;
    }

    @Override
    public int getHighZ() {
        return _z2;
    }

    @Override
    public long getSize() {
        return _size;
    }

    /*
     * 
     * see Greens theorem http://en.wikipedia.org/wiki/Green%27s_theorem
     * http://stackoverflow.com/questions/451426/how-do-i-calculate-the-surface-area-of-a-2d-polygon
     * 
     */
    private void calculateSize() {
        long size = 0;
        for (int i = 0, j = _x.length - 1; i < _x.length; j = i++) {
            int x0 = _x[j];
            int y0 = _y[j];
            int x1 = _x[i];
            int y1 = _y[i];
            size += x0 * y1 - x1 * y0;
        }
        _size = Math.round(Math.abs(size) * 0.5) * ((long)(_z2 - _z1 + 1));
    }

    @Override
    public int getLowX() {
        int rt = 0;
        for (int x : _x)
            if (rt == 0 || x < rt)
                rt = x;
        return rt;
    }

    @Override
    public int getHighX() {
        int rt = 0;
        for (int x : _x)
            if (rt == 0 || x > rt)
                rt = x;
        return rt;
    }

    @Override
    public int getLowY() {
        int rt = 0;
        for (int y : _y)
            if (rt == 0 || y < rt)
                rt = y;
        return rt;
    }

    @Override
    public int getHighY() {
        int rt = 0;
        for (int y : _y)
            if (rt == 0 || y > rt)
                rt = y;
        return rt;
    }

    public int[] getX() {
        return _x;
    }

    public int[] getY() {
        return _y;
    }

    @Override
    public int[][] getPoints() {
        return new int[][] { _x , _y };
    }

    @Override
    public int getPointsSize() {
        return _x.length;
    }


}
