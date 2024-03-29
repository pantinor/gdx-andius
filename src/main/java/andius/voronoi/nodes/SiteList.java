package andius.voronoi.nodes;

import java.util.ArrayList;
import java.util.List;

public final class SiteList implements IDisposable {

    private ArrayList<Site> _sites;
    private int _currentIndex;
    private boolean _sorted;

    public SiteList() {
        _sites = new ArrayList();
        _sorted = false;
    }

    @Override
    public void dispose() {
        if (_sites != null) {
            for (Site site : _sites) {
                site.dispose();
            }
            _sites.clear();
            _sites = null;
        }
    }

    public int push(Site site) {
        _sorted = false;
        _sites.add(site);
        return _sites.size();
    }

    public int get_length() {
        return _sites.size();
    }

    public Site next() {
        if (_sorted == false) {
            throw new Error("SiteList::next():  sites have not been sorted");
        }
        if (_currentIndex < _sites.size()) {
            return _sites.get(_currentIndex++);
        } else {
            return null;
        }
    }

    public Rectangle getSitesBounds() {
        if (_sorted == false) {
            Site.sortSites(_sites);
            _currentIndex = 0;
            _sorted = true;
        }
        float xmin, xmax, ymin, ymax;
        if (_sites.isEmpty()) {
            return new Rectangle(0, 0, 0, 0);
        }
        xmin = Float.MAX_VALUE;
        xmax = Float.MIN_VALUE;
        for (Site site : _sites) {
            if (site.get_x() < xmin) {
                xmin = site.get_x();
            }
            if (site.get_x() > xmax) {
                xmax = site.get_x();
            }
        }
        // here's where we assume that the sites have been sorted on y:
        ymin = _sites.get(0).get_y();
        ymax = _sites.get(_sites.size() - 1).get_y();

        return new Rectangle(xmin, ymin, xmax - xmin, ymax - ymin);
    }

    public ArrayList<Point> siteCoords() {
        ArrayList<Point> coords = new ArrayList();
        for (Site site : _sites) {
            coords.add(site.get_coord());
        }
        return coords;
    }

    /**
     *
     * @return the largest circle centered at each site that fits in its region;
     * if the region is infinite, return a circle of radius 0.
     *
     */
    public ArrayList<Circle> circles() {
        ArrayList<Circle> circles = new ArrayList();
        for (Site site : _sites) {
            float radius = 0;
            Edge nearestEdge = site.nearestEdge();

            if (!nearestEdge.isPartOfConvexHull()) {
                radius = nearestEdge.sitesDistance() * 0.5f;
            }
            circles.add(new Circle(site.get_x(), site.get_y(), radius));
        }
        return circles;
    }

    public List<List<Point>> regions(Rectangle plotBounds) {
        ArrayList<List<Point>> regions = new ArrayList();
        for (Site site : _sites) {
            List<Point> r = site.region(plotBounds);
            if (r != null) {
                regions.add(r);
            }
        }
        return regions;
    }

}
