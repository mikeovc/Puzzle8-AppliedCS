package com.google.engedu.puzzle8;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Random;

public class PuzzleBoardView extends View {
    public static final int NUM_SHUFFLE_STEPS = 40;
    static final Comparator<PuzzleBoard> COMPARATOR = new Comparator<PuzzleBoard>() {
        @Override
        public int compare(PuzzleBoard puzzleBoard, PuzzleBoard t1) {
            if (puzzleBoard.priority() < t1.priority()) {
                return -1;
            }
            else if (puzzleBoard.priority() > t1.priority()) {
                return 1;
            } else {
                return 0;
            }
        }
    };

    private Activity activity;
    private PuzzleBoard puzzleBoard;
    private ArrayList<PuzzleBoard> animation;
    private Random random = new Random();

    public PuzzleBoardView(Context context) {
        super(context);
        activity = (Activity) context;
        animation = null;
    }

    public void initialize(Bitmap imageBitmap) {
        int width = getWidth();
        puzzleBoard = new PuzzleBoard(imageBitmap, width);
        refreshScreen();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (puzzleBoard != null) {
            if (animation != null && animation.size() > 0) {
                puzzleBoard = animation.remove(0);
                puzzleBoard.draw(canvas);
                if (animation.size() == 0) {
                    animation = null;
                    puzzleBoard.reset();
                    Toast toast = Toast.makeText(activity, "Solved! ", Toast.LENGTH_LONG);
                    toast.show();
                } else {
                    this.postInvalidateDelayed(500);
                }
            } else {
                puzzleBoard.draw(canvas);
            }
        }
    }

    public void shuffle() {
        if (animation == null && puzzleBoard != null) {
            for (int i = 0; i < NUM_SHUFFLE_STEPS; i++) {
                ArrayList<PuzzleBoard> boards = puzzleBoard.neighbours();
                int randomIndex = random.nextInt(boards.size());
                puzzleBoard = boards.get(randomIndex);
            }
            refreshScreen();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (animation == null && puzzleBoard != null) {
            switch(event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (puzzleBoard.click(event.getX(), event.getY())) {
                        invalidate();
                        if (puzzleBoard.resolved()) {
                            Toast toast = Toast.makeText(activity, "Congratulations!", Toast.LENGTH_LONG);
                            toast.show();
                        }
                        return true;
                    }
            }
        }
        return super.onTouchEvent(event);
    }

    public void solve() {
        PriorityQueue<PuzzleBoard> boards = new PriorityQueue<>(1, COMPARATOR);
        boards.add(puzzleBoard);

        while (boards.size() != 0) {
            PuzzleBoard retrievedBoard = boards.poll();
            if (!retrievedBoard.resolved()) {
                addNeighbours(boards, retrievedBoard);
            } else {
                boards.clear();
                ArrayList<PuzzleBoard> solvePath = retrievedBoard.allPreviousBoards();
                Collections.reverse(solvePath);
                retrievedBoard.clearHistory();
                animation = solvePath;
                invalidate();
            }
        }
    }

    private void addNeighbours(PriorityQueue<PuzzleBoard> heap, PuzzleBoard currentBoard) {
        for (PuzzleBoard neighbour : currentBoard.neighbours()) {
            if (currentBoard.getPreviousBoard() == null ||
                    !neighbour.sameStateAs(currentBoard.getPreviousBoard())) {
                heap.add(neighbour);
            }
        }
    }

    private void refreshScreen() {
        puzzleBoard.reset();
        invalidate();
    }
}
