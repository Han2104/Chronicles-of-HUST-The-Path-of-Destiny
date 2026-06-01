package com.hust.game.maps.b1;

import java.awt.Rectangle;

public class CollisionUtils {
    public static boolean checkAABB(Rectangle a, Rectangle b) {
        return a.intersects(b);
    }
}
