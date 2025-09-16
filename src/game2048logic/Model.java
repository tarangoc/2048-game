package game2048logic;

import game2048rendering.Board;
import game2048rendering.Side;
import game2048rendering.Tile;

import java.util.Formatter;


/** The state of a game of 2048.
 *  @author P. N. Hilfinger + Josh Hug
 */
public class Model {
    /** Current contents of the board. */
    private final Board board;
    /** Current score. */
    private int score;

    /* Coordinate System: column x, row y of the board (where x = 0,
     * y = 0 is the lower-left corner of the board) will correspond
     * to board.tile(x, y).  Be careful!
     */

    /** Largest piece value. */
    public static final int MAX_PIECE = 2048;

    /** A new 2048 game on a board of size SIZE with no pieces
     *  and score 0. */
    public Model(int size) {
        board = new Board(size);
        score = 0;
    }

    /** A new 2048 game where RAWVALUES contain the values of the tiles
     * (0 if null). VALUES is indexed by (x, y) with (0, 0) corresponding
     * to the bottom-left corner. Used for testing purposes. */
    public Model(int[][] rawValues, int score) {
        board = new Board(rawValues);
        this.score = score;
    }

    /** Return the current Tile at (x, y), where 0 <= x < size(),
     *  0 <= y < size(). Returns null if there is no tile there.
     *  Used for testing. */
    public Tile tile(int x, int y) {
        return board.tile(x, y);
    }

    /** Return the number of squares on one side of the board. */
    public int size() {
        return board.size();
    }

    /** Return the current score. */
    public int score() {
        return score;
    }


    /** Clear the board to empty and reset the score. */
    public void clear() {
        score = 0;
        board.clear();
    }


    /** Add TILE to the board. There must be no Tile currently at the
     *  same position. */
    public void addTile(Tile tile) {
        board.addTile(tile);
    }

    /** Return true iff the game is over (there are no moves, or
     *  there is a tile with value 2048 on the board). */
    public boolean gameOver() {
        return maxTileExists() || !atLeastOneMoveExists();
    }

    /** Returns this Model's board. */
    public Board getBoard() {
        return board;
    }
    /** Returns true if at least one space on the board is empty.
     *  Empty spaces are stored as null.
     * */
    public boolean emptySpaceExists() {
        for (int x = 0; x <= board.size() - 1; x++) { // x = row and y = column
            for (int y = 0; y <= board.size() - 1; y++) { // make a new tile
                Tile first = board.tile(x, y);
                if (null == first) { //from spec, if (t == null)
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns true if any tile is equal to the maximum valid value.
     * Maximum valid value is given by MAX_PIECE. Note that
     * given a Tile object t, we get its value with t.value().
     */
    public boolean maxTileExists() {
        // iterate through each tile
        for (int x = 0; x <= board.size() - 1; x++) {
            for (int y = 0; y <= board.size() - 1; y++) {
                Tile regularTiles = board.tile(x, y);
                //edge case???
                if (regularTiles != null) {
                    if (regularTiles.value() == MAX_PIECE) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Returns true if there are any valid moves on the board.
     * There are two ways that there can be valid moves:
     * 1. There is at least one empty space on the board.
     * 2. There are two adjacent tiles with the same value.
     */
    public boolean atLeastOneMoveExists() {
        int x = 0;
        int y = 0;
        board.tile(x, y);
        int boardSize = board.size();
        if (emptySpaceExists()) {
            return true;
        }
        for (x = 0; x < boardSize; x++) {
            for (y = 0; y < boardSize; y++) {
                Tile currTile = board.tile(x, y);

                // look for current tile
                if (currTile == null) {
                    continue;
                }

                // right tile
                if (x + 1 < boardSize) {
                    Tile rightTile = board.tile(x + 1, y);
                    if (rightTile != null && currTile.value() == rightTile.value()) {
                        return true;
                    }
                }

                // down tile
                if (y + 1 < boardSize) {
                    Tile downTile = board.tile(x, y + 1);
                    if (downTile != null && currTile.value() == downTile.value()) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Moves the tile at position (x, y) as far up as possible.
     * <p>
     * Rules for Tilt:
     * 1. If two Tiles are adjacent in the direction of motion (ignoring empty space)
     *    and have the same value, they are merged into one Tile of twice the original
     *    value and that new value is added to the score instance variable
     * 2. A tile that is the result of a merge will not merge again on that
     *    tilt. So each move, every tile will only ever be part of at most one
     *    merge (perhaps zero).
     * 3. When three adjacent tiles in the direction of motion have the same
     *    value, then the leading two tiles in the direction of motion merge,
     *    and the trailing tile does not.
     */
    public void moveTileUpAsFarAsPossible(int x, int y) {
        Tile currTile = board.tile(x, y);
        if (currTile != null) {

            int targetY = y;
            int secondY = targetY + 1;

            while (secondY < board.size() && board.tile(x, secondY) == null) {
                targetY = secondY;
                secondY++;
            }

            if (secondY < board.size()) {
                Tile next = board.tile(x, secondY);

                if (next != null) {
                    if (next.value() == currTile.value() && !next.wasMerged()) {
                        board.move(x, secondY, currTile);
                        score += next.value() * 2;
                        return;
                    }
                }
            }

            if (targetY != y) {
                board.move(x, targetY, currTile);
            }
        }
    }

    /** Handles the movements of the tilt in column x of board B
     * by moving every tile in the column as far up as possible.
     * The viewing perspective has already been set,
     * so we are tilting the tiles in this column up.
     * */
    public void tiltColumn(int x) {
        for (int y = board.size() - 2; y >= 0; y--) {

            moveTileUpAsFarAsPossible(x, y);
        }
    }

    public void tilt(Side side) {
        board.setViewingPerspective(side);

        for (int x = 0; x < board.size(); x++) {
            tiltColumn(x);
        }
        board.setViewingPerspective(Side.NORTH);
    }

    /** Tilts every column of the board toward SIDE.
     */
    public void tiltWrapper(Side side) {
        board.resetMerged();
        tilt(side);
    }


    @Override
    public String toString() {
        Formatter out = new Formatter();
        out.format("%n[%n");
        for (int y = size() - 1; y >= 0; y -= 1) {
            for (int x = 0; x < size(); x += 1) {
                if (tile(x, y) == null) {
                    out.format("|    ");
                } else {
                    out.format("|%4d", tile(x, y).value());
                }
            }
            out.format("|%n");
        }
        String over = gameOver() ? "over" : "not over";
        out.format("] %d (game is %s) %n", score(), over);
        return out.toString();
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof Model m) && this.toString().equals(m.toString());
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }
}
