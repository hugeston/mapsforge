/*
 * Copyright 2010, 2011, 2012, 2013 mapsforge.org
 * Copyright 2024-2025 Sublimis
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.mapsforge.core.model;

import java.io.Serializable;

/**
 * A Rectangle represents an immutable set of four double coordinates.
 */
public class Rectangle implements Serializable {
    private static final long serialVersionUID = 1L;

    public final double bottom;
    public final double left;
    public final double right;
    public final double top;

    public Rectangle(double left, double top, double right, double bottom) {
        check(left, top, right, bottom);
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }

    /**
     * Constructs minimum Rectangle from the four points given so that all points are contained
     * in it (envelope of points).
     *
     * @param a point in arbitrary order.
     * @param b ..
     * @param c ..
     * @param d ..
     */
    public Rectangle(Point a, Point b, Point c, Point d) {
        this.left = minimum(a.x, b.x, c.x, d.x);
        this.right = maximum(a.x, b.x, c.x, d.x);
        this.bottom = maximum(a.y, b.y, c.y, d.y);
        this.top = minimum(a.y, b.y, c.y, d.y);
        check(this.left, this.top, this.right, this.bottom);
    }

    /**
     * @return true if this Rectangle contains the given point, false otherwise.
     */
    public boolean contains(Point point) {
        return this.left <= point.x && this.right >= point.x && this.top <= point.y && this.bottom >= point.y;
    }

    /**
     * Enlarges the Rectangle sides individually
     *
     * @param left   left enlargement
     * @param top    top enlargement
     * @param right  right enlargement
     * @param bottom bottom enlargement
     * @return
     */
    public Rectangle enlarge(double left, double top, double right, double bottom) {
        return new Rectangle(this.left - left, this.top - top, this.right + right, this.bottom + bottom);
    }

    public Rectangle envelope(double padding) {
        return new Rectangle(this.left - padding, this.top - padding, this.right + padding, this.bottom + padding);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof Rectangle)) {
            return false;
        }
        Rectangle other = (Rectangle) obj;
        if (Double.doubleToLongBits(this.left) != Double.doubleToLongBits(other.left)) {
            return false;
        } else if (Double.doubleToLongBits(this.top) != Double.doubleToLongBits(other.top)) {
            return false;
        } else if (Double.doubleToLongBits(this.right) != Double.doubleToLongBits(other.right)) {
            return false;
        } else if (Double.doubleToLongBits(this.bottom) != Double.doubleToLongBits(other.bottom)) {
            return false;
        }
        return true;
    }

    /**
     * @return a new Point at the horizontal and vertical center of this Rectangle.
     */
    public Point getCenter() {
        return new Point(getCenterX(), getCenterY());
    }

    /**
     * @return the horizontal center of this Rectangle.
     */
    public double getCenterX() {
        return (this.left + this.right) / 2;
    }

    /**
     * @return the vertical center of this Rectangle.
     */
    public double getCenterY() {
        return (this.top + this.bottom) / 2;
    }

    public double getHeight() {
        return this.bottom - this.top;
    }

    public double getWidth() {
        return this.right - this.left;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(this.left);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(this.top);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(this.right);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(this.bottom);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    /**
     * @return true if this Rectangle intersects with the given Rectangle, false otherwise.
     */
    public boolean intersects(Rectangle rectangle) {
        if (rectangle == null) {
            return false;
        }

        if (this == rectangle) {
            return true;
        }

        return this.left <= rectangle.right && rectangle.left <= this.right && this.top <= rectangle.bottom
                && rectangle.top <= this.bottom;
    }

    /**
     * @return true if this Rectangle contains the given Rectangle, false otherwise.
     */
    public boolean contains(Rectangle rectangle) {
        if (rectangle == null) {
            return false;
        }

        if (this == rectangle) {
            return true;
        }

        return this.left <= rectangle.left && this.right >= rectangle.right && this.top <= rectangle.top
                && this.bottom >= rectangle.bottom;
    }

    public boolean intersectsCircle(double pointX, double pointY, double radius) {
        double halfWidth = getWidth() / 2;
        double halfHeight = getHeight() / 2;

        double centerDistanceX = Math.abs(pointX - getCenterX());
        double centerDistanceY = Math.abs(pointY - getCenterY());

        // is the circle is far enough away from the rectangle?
        if (centerDistanceX > halfWidth + radius) {
            return false;
        } else if (centerDistanceY > halfHeight + radius) {
            return false;
        }

        // is the circle close enough to the rectangle?
        if (centerDistanceX <= halfWidth) {
            return true;
        } else if (centerDistanceY <= halfHeight) {
            return true;
        }

        double cornerDistanceX = centerDistanceX - halfWidth;
        double cornerDistanceY = centerDistanceY - halfHeight;
        return cornerDistanceX * cornerDistanceX + cornerDistanceY * cornerDistanceY <= radius * radius;
    }

    /**
     * Rotates the rectangle with {@link Rotation}. This method can be chained to apply multiple
     * rotations to one rectangle.
     *
     * @param rotation the rotation
     * @return a new rotated rectangle.
     */
    public Rectangle rotate(Rotation rotation) {
        return rotation.rotate(this);
    }

    public Rectangle shift(Point origin) {
        if (origin.x == 0 && origin.y == 0) {
            return this;
        }
        return new Rectangle(this.left + origin.x, this.top + origin.y, this.right + origin.x, this.bottom + origin.y);
    }

    public Rectangle clampClipCoordinates(double horizontal, double vertical) {
        Rectangle output = this;

        if (output.getWidth() > horizontal || output.getHeight() > vertical) {
            final double newLeft = Math.abs(output.left) > horizontal ? Math.signum(output.left) * horizontal : output.left;
            final double newTop = Math.abs(output.top) > vertical ? Math.signum(output.top) * vertical : output.top;
            final double newRight = Math.abs(output.right) > horizontal ? Math.signum(output.right) * horizontal : output.right;
            final double newBottom = Math.abs(output.bottom) > vertical ? Math.signum(output.bottom) * vertical : output.bottom;
            output = new Rectangle(newLeft, newTop, newRight, newBottom);
        }

        return output;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("left=");
        stringBuilder.append(this.left);
        stringBuilder.append(", top=");
        stringBuilder.append(this.top);
        stringBuilder.append(", right=");
        stringBuilder.append(this.right);
        stringBuilder.append(", bottom=");
        stringBuilder.append(this.bottom);
        return stringBuilder.toString();
    }

    private static double minimum(double a, double b, double c, double d) {
        if (b < a) {
            a = b;
        }
        if (c < a) {
            a = c;
        }
        if (d < a) {
            a = d;
        }
        return a;
    }

    private static double maximum(double a, double b, double c, double d) {
        if (b > a) {
            a = b;
        }
        if (c > a) {
            a = c;
        }
        if (d > a) {
            a = d;
        }
        return a;
    }

    private static void check(double left, double top, double right, double bottom) {
        if (left > right) {
            throw new IllegalArgumentException("left: " + left + ", right: " + right);
        } else if (top > bottom) {
            throw new IllegalArgumentException("top: " + top + ", bottom: " + bottom);
        }
    }
}
