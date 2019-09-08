package com.makotomiyamoto.gameeffects.antivirusdev;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.bukkit.Location;

/**
 *
 *
 * A class that generates a representation of a rasterized plane circle using
 * only fundamental math operators, data structures, and the Spigot API.
 *
 * @author AntivirusDev
 *
 *         <h1>Legal:<br>
 *         This work is licensed under the Creative Commons Zero License
 *         (http://creativecommons.org/publicdomain/zero/1.0/) Feel free to copy
 *         and store this work in its entirety, distribute it, modify it, and
 *         include it in other copyrighted works. Credit is appreciated but by
 *         no means required.
 *
 *         This work is provided 'as is' with no warranties made or liabilities
 *         assumed, express or implied. The author disclaims all implied
 *         warranties, including any warranty of merchantability and warranty of
 *         fitness for a particular purpose.</h1>
 *
 */
public class CircleGenerator {

    private final static int MAX_SQUARE = 1000; // the maximum radius whose square the generator will cache.

    private static final double SQUARE_ROOT_OF_TWO = 1.4142135624;

    // cache circle location sets by radius and whether enclosed locations are
    // allowed

    private static HashMap<Integer, HashSet<RelativeLocation>> cacheXZEnclosed = new HashMap<>();

    private static HashMap<Integer, HashSet<RelativeLocation>> cacheXYEnclosed = new HashMap<>();

    private static HashMap<Integer, HashSet<RelativeLocation>> cacheZYEnclosed = new HashMap<>();

    private static HashMap<Integer, HashSet<RelativeLocation>> cacheXZIgnoreEnclosed = new HashMap<>();

    private static HashMap<Integer, HashSet<RelativeLocation>> cacheXYIgnoreEnclosed = new HashMap<>();

    private static HashMap<Integer, HashSet<RelativeLocation>> cacheZYIgnoreEnclosed = new HashMap<>();

    private static HashMap<Integer, Integer> squareRootCache = new HashMap<Integer, Integer>();
    static {
        for (int i = 0; i <= MAX_SQUARE; i++) {

            squareRootCache.put(i * i, i);

        }

        int greatestPerfectSquare = 0; // for this particular application, the "square root" of a non-perfect square
        // integer is the
        // square root of the greatest perfect square below that integer
        // i.e. the "square root" of 20 is 4.
        for (int i = 0; i <= MAX_SQUARE * MAX_SQUARE; i++) {

            if (squareRootCache.containsKey(i)) {
                greatestPerfectSquare = squareRootCache.get(i);
            } else {
                squareRootCache.put(i, greatestPerfectSquare);
            }
        }
    }

    /**
     *
     * Helper class to store relative locations, since all computed circles are
     * based at (0,0)
     *
     */
    private static class RelativeLocation {

        int rX, rY, rZ;

        public RelativeLocation(int relX, int relY, int relZ) {
            this.rY = relY;
            this.rZ = relZ;
            this.rX = relX;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof RelativeLocation))
                return false;

            RelativeLocation rl = (RelativeLocation) o;
            return rl.rX == rX && rl.rY == rY && rl.rZ == rZ;

        }

        @Override
        public int hashCode() {

            return rX * rX + rY * rY + rZ * rZ;

        }

    }

    /**
     * Enumeration representing the three axis-aligned planes in the game
     *
     */
    public static enum Plane {
        XZ, XY, ZY
    }

    /**
     * Generates a set of locations that represent a rasterized circle. Uses cached
     * results to improve efficiency
     *
     * @param center         - the centerpoint of the circle
     * @param radius         - the radius of the circle
     * @param plane          - which plane to generate the circle on (Plane enum
     *                       included in CircleGen class)
     * @param ignoreEnclosed - whether to ignore 'enclosed' squares, or those which
     *                       have a neighboring square in both axial directions of
     *                       the circle's plane
     *                       @param allowBurrs - whether the circle should have 'burrs' or single blocks jutting out at the tips of the axes
     * @return a set of locations representing a rasterized circle
     * @throws IllegalArgumentException if centerpoint is null, plane is null, or
     *                                  radius is less than zero
     */
    public static HashSet<Location> generateCircle(Location center, int radius, Plane plane, boolean ignoreEnclosed, boolean allowBurrs) {

        if (center == null || radius < 0 || plane == null)
            throw new IllegalArgumentException("Incorrect parameter(s)!"); // either of these three conditions would
        // make the parameters invalid

        HashSet<Location> set = new HashSet<>();

        HashMap<Integer, HashSet<RelativeLocation>> whichCacheToUse = null; // since there are 6 caches, the correct one
        // for the plane must be stored
        switch (plane) { // switch statement is very fast performance-wise
            case XZ:
                whichCacheToUse = ignoreEnclosed ? cacheXZIgnoreEnclosed : cacheXZEnclosed;
                break;
            case XY:
                whichCacheToUse = ignoreEnclosed ? cacheXYIgnoreEnclosed : cacheXYEnclosed;
                break;
            case ZY:
                whichCacheToUse = ignoreEnclosed ? cacheZYIgnoreEnclosed : cacheZYEnclosed;
                break;
            default:
                break;
        }

        if (whichCacheToUse.containsKey(new Integer(radius))) { // optimized caching return

            HashSet<RelativeLocation> locs = whichCacheToUse.get(radius);
            locs = (HashSet<RelativeLocation>) locs.clone();


            if(!allowBurrs) {

                switch (plane) {
                    case XZ:
                        locs.remove(new RelativeLocation(0,0, radius));
                        locs.add(new RelativeLocation(0, 0, radius-1));

                        locs.remove(new RelativeLocation(-radius,0, 0));
                        locs.add(new RelativeLocation(-radius+1, 0, 0));

                        locs.remove(new RelativeLocation(radius,0, 0));
                        locs.add(new RelativeLocation( radius-1, 0, 0));

                        locs.remove(new RelativeLocation(0,0, -radius));
                        locs.add(new RelativeLocation(0, 0, -radius+1));
                        break;
                    case XY:
                        locs.remove(new RelativeLocation(0,radius, 0));
                        locs.add(new RelativeLocation(0, radius-1, 0));

                        locs.remove(new RelativeLocation(0,-radius, 0));
                        locs.add(new RelativeLocation(0, -radius+1, 0));

                        locs.remove(new RelativeLocation(radius,0, 0));
                        locs.add(new RelativeLocation( radius-1, 0, 0));
                        locs.remove(new RelativeLocation(-radius,0, 0));
                        locs.add(new RelativeLocation( -radius+1, 0, 0));

                        break;
                    case ZY:
                        locs.remove(new RelativeLocation(0,radius, 0));
                        locs.add(new RelativeLocation(0, radius-1, 0));

                        locs.remove(new RelativeLocation(0,-radius, 0));
                        locs.add(new RelativeLocation(0, -radius+1, 0));

                        locs.remove(new RelativeLocation(0,0, radius));
                        locs.add(new RelativeLocation(0 , 0, radius-1));
                        locs.remove(new RelativeLocation(0,0, -radius));
                        locs.add(new RelativeLocation( 0, 0, -radius+1));
                        break;
                    default:
                        break;
                }

            }
            for (RelativeLocation rl : locs)
                set.add(new Location(center.getWorld(), center.getX() + rl.rX, center.getY() + rl.rY,
                        center.getZ() + rl.rZ));
            return set;
        }
        HashSet<RelativeLocation> locs = new HashSet<>();

        HashSet<RelativeLocation> remainingOctants = new HashSet<>(); // create a separate set to hold the filled
        // octants, to avoid repeated filling

        final int unchanging_axis = 0; // the axis that does not have a component in the plane of the circle
        int radiusSquared = radius * radius;

        int stopValue = ((int) (radius / SQUARE_ROOT_OF_TWO)) + 1; // represents the midpoint along the edge of the
        // circle in quadrant 1, or the dividing point
        // between octant 2 and octant 1

        RelativeLocation lastAdded = null; // track the location last added to the set
        switch (plane) {
            case XZ:

                for (int ind = 0; ind < stopValue; ind++) { // iterates from pi/2 to the stop point (90 deg. to 45 deg.)
                    // Derived from the equation a^2 = radius^2 - b^2
                    int prevDep = squareRootCache.get(radiusSquared - (ind * ind)); // get the dependent value corresponding
                    // to
                    // the
                    // 'previous' integer independent
                    // variable
                    int nextDep = squareRootCache.get(radiusSquared - ((ind + 1) * (ind + 1))); // get the dependent value
                    // corresponding to the
                    // 'next'
                    // integer independent value

                    for (int i = 0; i <= (prevDep - nextDep); i++) { // compute the delta between prevValue and nextValue,
                        // then
                        // add all
                        // blocks that fall in that delta

                        RelativeLocation add = new RelativeLocation(ind, unchanging_axis, prevDep - i);
                        locs.add(add);
                        lastAdded = add;
                    }
                }

                if (ignoreEnclosed) {
                    locs.remove(lastAdded); // the last added element often becomes enclosed when the octants are filled, so
                    // remove it
                    Iterator<RelativeLocation> iteratorXZ = locs.iterator();

                    while (iteratorXZ.hasNext()) {

                        RelativeLocation rl = iteratorXZ.next();

                        if (locs.contains(new RelativeLocation(rl.rX + 1, rl.rY, rl.rZ))
                                && locs.contains(new RelativeLocation(rl.rX, rl.rY, rl.rZ + 1))) {

                            iteratorXZ.remove();
                        }

                    }
                }

                for (RelativeLocation rl : locs) {
                    fillOctantsXZ(remainingOctants, rl);
                }
                break;
            case XY:

                for (int ind = 0; ind < stopValue; ind++) {
                    int prevDep = squareRootCache.get(radiusSquared - (ind * ind));
                    int nextDep = squareRootCache.get(radiusSquared - ((ind + 1) * (ind + 1)));

                    for (int i = 0; i <= (prevDep - nextDep); i++) {
                        RelativeLocation add = new RelativeLocation(ind, prevDep - i, unchanging_axis);
                        locs.add(add);
                        lastAdded = add;
                    }
                }

                if (ignoreEnclosed) {
                    locs.remove(lastAdded);
                    Iterator<RelativeLocation> iteratorXY = locs.iterator();

                    while (iteratorXY.hasNext()) {

                        RelativeLocation rl = iteratorXY.next();
                        if (locs.contains(new RelativeLocation(rl.rX + 1, rl.rY, rl.rZ))
                                && locs.contains(new RelativeLocation(rl.rX, rl.rY + 1, rl.rZ))) {

                            iteratorXY.remove();
                        }

                    }
                }

                for (RelativeLocation rl : locs) {

                    fillOctantsXY(remainingOctants, rl);
                }
                break;
            case ZY:

                for (int ind = 0; ind < stopValue; ind++) {
                    int prevDep = squareRootCache.get(radiusSquared - (ind * ind));
                    int nextDep = squareRootCache.get(radiusSquared - ((ind + 1) * (ind + 1)));

                    for (int i = 0; i <= (prevDep - nextDep); i++) {
                        RelativeLocation add = new RelativeLocation(unchanging_axis, prevDep - i, ind);
                        locs.add(add);
                        lastAdded = add;
                    }
                }

                if (ignoreEnclosed) {
                    locs.remove(lastAdded);
                    Iterator<RelativeLocation> iteratorZY = locs.iterator();

                    while (iteratorZY.hasNext()) {

                        RelativeLocation rl = iteratorZY.next();
                        if (locs.contains(new RelativeLocation(rl.rX, rl.rY, rl.rZ + 1))
                                && locs.contains(new RelativeLocation(rl.rX, rl.rY + 1, rl.rZ))) {

                            iteratorZY.remove();
                        }

                    }
                }

                for (RelativeLocation rl : locs) {

                    fillOctantsZY(remainingOctants, rl);
                }
                break;
        }

        locs.addAll(remainingOctants); // finally, add the clones locations in the seven other octants to the set of
        // original locations

        whichCacheToUse.put(new Integer(radius), locs);

        if(!allowBurrs) {
            return generateCircle(center, radius, plane, ignoreEnclosed, allowBurrs);
        }

        for (RelativeLocation rl : locs)
            set.add(new Location(center.getWorld(), center.getBlockX() + rl.rX, center.getBlockY() + rl.rY,
                    center.getBlockZ() + rl.rZ));

        return set;
    }

    /**
     * Helper method to clone the blocks in one octant to the other seven in the XZ
     * plane
     *
     * @param set      - set that will ultimately contain the locations of the seven
     *                 points
     * @param location - location in octant 2 to be cloned
     */
    private static void fillOctantsXZ(Set<RelativeLocation> set, RelativeLocation location) {
        set.add(new RelativeLocation(location.rZ, location.rY, location.rX)); // octant 1
        set.add(new RelativeLocation(-location.rX, location.rY, location.rZ)); // octant 3
        set.add(new RelativeLocation(-location.rZ, location.rY, location.rX)); // octant 4
        set.add(new RelativeLocation(-location.rZ, location.rY, -location.rX)); // octant 5
        set.add(new RelativeLocation(-location.rX, location.rY, -location.rZ)); // octant 6
        set.add(new RelativeLocation(location.rX, location.rY, -location.rZ)); // octant 7
        set.add(new RelativeLocation(location.rZ, location.rY, -location.rX)); // octant 8
    }

    /**
     * Helper method to clone the blocks in one octant to the other seven in the ZY
     * plane
     *
     * @param set      - set that will ultimately contain the locations of the seven
     *                 points
     * @param location - location in octant 2 to be cloned
     */
    private static void fillOctantsZY(Set<RelativeLocation> set, RelativeLocation location) {
        set.add(new RelativeLocation(location.rX, location.rZ, location.rY)); // octant 1
        set.add(new RelativeLocation(location.rX, -location.rY, location.rZ)); // octant 3
        set.add(new RelativeLocation(location.rX, -location.rZ, location.rY)); // octant 4
        set.add(new RelativeLocation(location.rX, -location.rZ, -location.rY)); // octant 5
        set.add(new RelativeLocation(location.rX, -location.rY, -location.rZ)); // octant 6
        set.add(new RelativeLocation(location.rX, location.rY, -location.rZ)); // octant 7
        set.add(new RelativeLocation(location.rX, location.rZ, -location.rY)); // octant 8
    }

    /**
     * Helper method to clone the blocks in one octant to the other seven in the XY
     * plane
     *
     * @param set      - set that will ultimately contain the locations of the seven
     *                 points
     * @param location - location in octant 2 to be cloned
     */
    private static void fillOctantsXY(Set<RelativeLocation> set, RelativeLocation location) {
        set.add(new RelativeLocation(location.rY, location.rX, location.rZ)); // octant 1
        set.add(new RelativeLocation(-location.rX, location.rY, location.rZ)); // octant 3
        set.add(new RelativeLocation(-location.rY, location.rX, location.rZ)); // octant 4
        set.add(new RelativeLocation(-location.rY, -location.rX, location.rZ)); // octant 5
        set.add(new RelativeLocation(-location.rX, -location.rY, location.rZ)); // octant 6
        set.add(new RelativeLocation(location.rX, -location.rY, location.rZ)); // octant 7
        set.add(new RelativeLocation(location.rY, -location.rX, location.rZ)); // octant 8
    }

}