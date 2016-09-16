package com.google.engedu.puzzle8;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Shader;

import java.util.ArrayList;
import java.lang.Math;


public class PuzzleBoard {

    private static final int NUM_TILES = 3;
    private static final int[][] NEIGHBOUR_COORDS = {
            { -1, 0 },
            { 1, 0 },
            { 0, -1 },
            { 0, 1 }
    };
    private ArrayList<PuzzleTile> tiles = new ArrayList<>();
    private int steps = 0;
    private PuzzleBoard previousBoard;

    PuzzleBoard(Bitmap bitmap, int parentWidth) {
        bitmap = bitmap.createScaledBitmap(bitmap, parentWidth, parentWidth, false);
        int tileWidthAndHeight = bitmap.getWidth() / NUM_TILES;
        for (int i = 0; i < NUM_TILES; i++) {
            for (int j = 0; j < NUM_TILES; j++) {
                int yStart = i * tileWidthAndHeight;
                int xStart = j * tileWidthAndHeight;
                Bitmap b = bitmap.createBitmap(bitmap, xStart, yStart, tileWidthAndHeight, tileWidthAndHeight);
                if (i == NUM_TILES - 1 && j == NUM_TILES -1) {
                    tiles.add(null);
                } else {
                    tiles.add(new PuzzleTile(b, (i * NUM_TILES) + j));
                }
            }
        }
    }

    PuzzleBoard(PuzzleBoard otherBoard) {
        steps = otherBoard.steps + 1;
        tiles = (ArrayList<PuzzleTile>) otherBoard.tiles.clone();
    }

    public PuzzleBoard getPreviousBoard() { return previousBoard; }
    public void setPreviousBoard(PuzzleBoard p) { previousBoard = p; }

    public void reset() {
        previousBoard = null;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;
        return tiles.equals(((PuzzleBoard) o).tiles);
    }

    public void draw(Canvas canvas) {
        if (tiles == null) {
            return;
        }
        for (int i = 0; i < NUM_TILES * NUM_TILES; i++) {
            PuzzleTile tile = tiles.get(i);
            if (tile != null) {
                tile.draw(canvas, i % NUM_TILES, i / NUM_TILES);
            }
        }
    }

    public boolean click(float x, float y) {
        for (int i = 0; i < NUM_TILES * NUM_TILES; i++) {
            PuzzleTile tile = tiles.get(i);
            if (tile != null) {
                if (tile.isClicked(x, y, i % NUM_TILES, i / NUM_TILES)) {
                    return tryMoving(i % NUM_TILES, i / NUM_TILES);
                }
            }
        }
        return false;
    }

    private boolean tryMoving(int tileX, int tileY) {
        for (int[] delta : NEIGHBOUR_COORDS) {
            int nullX = tileX + delta[0];
            int nullY = tileY + delta[1];
            if (nullX >= 0 && nullX < NUM_TILES && nullY >= 0 && nullY < NUM_TILES &&
                    tiles.get(XYtoIndex(nullX, nullY)) == null) {
                swapTiles(XYtoIndex(nullX, nullY), XYtoIndex(tileX, tileY));
                return true;
            }

        }
        return false;
    }

    public boolean resolved() {
        for (int i = 0; i < NUM_TILES * NUM_TILES - 1; i++) {
            PuzzleTile tile = tiles.get(i);
            if (tile == null || tile.getNumber() != i)
                return false;
        }
        return true;
    }

    private int XYtoIndex(int x, int y) {
        return x + y * NUM_TILES;
    }

    protected void swapTiles(int i, int j) {
        PuzzleTile temp = tiles.get(i);
        tiles.set(i, tiles.get(j));
        tiles.set(j, temp);
    }

    public ArrayList<PuzzleBoard> neighbours() {
        ArrayList<PuzzleBoard> neighbours = new ArrayList<>();

        int indexOfEmptyTile;
        indexOfEmptyTile = tiles.indexOf(null);

        int emptyX = indexOfEmptyTile % NUM_TILES;
        int emptyY = indexOfEmptyTile / NUM_TILES;
        int tileX, tileY;
        for (int[] delta : NEIGHBOUR_COORDS) {
            tileX = emptyX + delta[0];
            tileY = emptyY + delta[1];
            // If move is within bounds:
            if ( (tileX >= 0 && tileX < NUM_TILES) && (tileY >= 0 && tileY < NUM_TILES) ) {
                PuzzleBoard p = new PuzzleBoard(this);
                p.tryMoving(tileX, tileY);
                neighbours.add(p);
            }
        }

        return neighbours;
    }

    public int priority() {
        int tileX, tileY, desiredX, desiredY;
        int manhattanDistance = 0;
        for (int i = 0; i < tiles.size(); i++) {
            if (tiles.get(i) != null) {
                tileX = i % NUM_TILES;
                tileY = i / NUM_TILES;
                desiredX = tiles.get(i).getNumber() % NUM_TILES;
                desiredY = tiles.get(i).getNumber() / NUM_TILES;
                manhattanDistance += Math.abs(tileX - desiredX) + Math.abs(tileY - desiredY);
            }
        }
        return manhattanDistance + steps;
    }

    public ArrayList<PuzzleBoard> allPreviousBoards() {
        ArrayList<PuzzleBoard> previousBoards = new ArrayList<>();
        PuzzleBoard b = previousBoard;
        previousBoards.add(this);
        while (b != null) {
            previousBoards.add(b);
            b = b.previousBoard;
        }
        return previousBoards;
    }

    public boolean sameStateAs(PuzzleBoard otherBoard) {
        for (int i = 0; i < tiles.size(); i++) {
            if (tiles.get(i) != null && otherBoard.tiles.get(i) != null) {
                if (tiles.get(i).getNumber() != otherBoard.tiles.get(i).getNumber()) {
                    return false;
                }
            }
        }
        return true;
    }
}
