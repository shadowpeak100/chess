package chess;

import java.util.*;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChessPiece that = (ChessPiece) o;
        return color == that.color && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(color, type);
    }

    private ChessGame.TeamColor color;
    private ChessPiece.PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.color = pieceColor;
        this.type = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return this.color;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return this.type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> returnVal = null;

        switch (type){
            case KING:
                returnVal = kingMoves(board, myPosition);
                break;
            case QUEEN:
                returnVal = queenMoves(board, myPosition);
                break;
            case BISHOP:
                returnVal = bishopMoves(board, myPosition);
                break;
            case KNIGHT:
                returnVal = knightMoves(board, myPosition);
                break;
            case ROOK:
                returnVal = rookMoves(board, myPosition);
                break;
            case PAWN:
                returnVal = pawnMoves(board, myPosition, color);
                break;
            default:
                System.out.println("Invalid piece, cannot determine possible moves: " + type);
                System.exit(1);
        }
        return returnVal;
    }

    public void handleWhitePawnMove(ChessBoard board, ChessPosition currentPosition, ChessPosition proposedPosition, Collection<ChessMove> returnVal, ChessGame.TeamColor color){
        //if it is out of bounds, return
        if(proposedPosition.getColumn() < 1 || proposedPosition.getColumn() > 8 || proposedPosition.getRow() < 1 || proposedPosition.getRow() > 8){
            return;
        }

        //is it a valid 1 space move
        if(!board.occupied(proposedPosition) && Math.abs(proposedPosition.getRow() - currentPosition.getRow()) == 1
        && currentPosition.getColumn() - proposedPosition.getColumn() == 0){
            // is it an edge or not?
            if(!handlePromotionWhite(board, currentPosition, proposedPosition, returnVal)){
                returnVal.add(new ChessMove(currentPosition, proposedPosition, null));
            }
        }

        //is it a valid 2 space move
        if (currentPosition.getRow() == 2 && proposedPosition.getRow() == 4){
            if (!board.occupied(proposedPosition) && !board.occupied(new ChessPosition(proposedPosition.getRow() + 1, proposedPosition.getColumn() + 1))){
                returnVal.add(new ChessMove(currentPosition, proposedPosition, null));
            }
        }

        //is it a valid capture
        if(Math.abs(currentPosition.getColumn() - proposedPosition.getColumn()) == 1 && Math.abs(currentPosition.getRow() - proposedPosition.getRow()) == 1){
            if (board.occupiedByOppositeColor(proposedPosition, color)){
                //is it a promotion or not?
                if(!handlePromotionWhite(board, currentPosition, proposedPosition, returnVal)){
                    returnVal.add(new ChessMove(currentPosition, proposedPosition, null));
                }
            }
        }
    }

    public void handleBlackPawnMove(ChessBoard board, ChessPosition currentPosition, ChessPosition proposedPosition, Collection<ChessMove> returnVal, ChessGame.TeamColor color){
        //if it is out of bounds, return
        if(proposedPosition.getColumn() <= 0 || proposedPosition.getColumn() > 8 || proposedPosition.getRow() <= 0 || proposedPosition.getRow() > 8){
            return;
        }

        //is it a valid 1 space move
        if(!board.occupied(proposedPosition) && Math.abs(proposedPosition.getRow() - currentPosition.getRow()) == 1
        && currentPosition.getColumn() - proposedPosition.getColumn() == 0){
            // is it an edge or not?
            if(!handlePromotionBlack(board, currentPosition, proposedPosition, returnVal)){
                returnVal.add(new ChessMove(currentPosition, proposedPosition, null));

            }
        }

        //is it a valid 2 space move
        if (currentPosition.getRow() == 7 && proposedPosition.getRow() == 5){
            if (!board.occupied(proposedPosition) && !board.occupied(new ChessPosition(proposedPosition.getRow() + 1, proposedPosition.getColumn()))){
                returnVal.add(new ChessMove(currentPosition, proposedPosition, null));
            }
        }

        //is it a valid capture
        if(Math.abs(currentPosition.getColumn() - proposedPosition.getColumn()) == 1 && Math.abs(currentPosition.getRow() - proposedPosition.getRow()) == 1){
            if (board.occupiedByOppositeColor(proposedPosition, color)){
                //is it a promotion or not?
                if(!handlePromotionBlack(board, currentPosition, proposedPosition, returnVal)){
                    returnVal.add(new ChessMove(currentPosition, proposedPosition, null));
                }
            }
        }
    }

    public boolean handlePromotionWhite(ChessBoard board, ChessPosition currentPosition, ChessPosition proposedPosition, Collection<ChessMove> returnVal){
        if (proposedPosition.getRow() == 8) {
            if (!board.occupied(8, proposedPosition.getColumn())) {
                return pawnAddPromotions(currentPosition, proposedPosition, returnVal);
            }
            if (board.occupiedByOppositeColor(8, proposedPosition.getColumn(), ChessGame.TeamColor.WHITE)) {
                return pawnAddPromotions(currentPosition, proposedPosition, returnVal);
            }
        }
        return false;
    }

    private boolean pawnAddPromotions(ChessPosition currentPosition, ChessPosition proposedPosition, Collection<ChessMove> returnVal) {
        returnVal.add(new ChessMove(currentPosition, new ChessPosition(8, proposedPosition.getColumn()), PieceType.QUEEN));
        returnVal.add(new ChessMove(currentPosition, new ChessPosition(8, proposedPosition.getColumn()), PieceType.BISHOP));
        returnVal.add(new ChessMove(currentPosition, new ChessPosition(8, proposedPosition.getColumn()), PieceType.KNIGHT));
        returnVal.add(new ChessMove(currentPosition, new ChessPosition(8, proposedPosition.getColumn()), PieceType.ROOK));
        return true;
    }

    public boolean handlePromotionBlack(ChessBoard board, ChessPosition currentPosition, ChessPosition proposedPosition, Collection<ChessMove> returnVal){
        if (proposedPosition.getRow() == 1){
            if (!board.occupied(1, proposedPosition.getColumn() + 1)){
                returnVal.add(new ChessMove(currentPosition, new ChessPosition(1, proposedPosition.getColumn()), PieceType.QUEEN));
                returnVal.add(new ChessMove(currentPosition, new ChessPosition(1, proposedPosition.getColumn()), PieceType.BISHOP));
                returnVal.add(new ChessMove(currentPosition, new ChessPosition(1, proposedPosition.getColumn()), PieceType.KNIGHT));
                returnVal.add(new ChessMove(currentPosition, new ChessPosition(1, proposedPosition.getColumn()), PieceType.ROOK));
                return true;
            }
        }
        return false;
    }



    public Collection<ChessMove> pawnMoves(ChessBoard board, ChessPosition position, ChessGame.TeamColor color){
        // may move 2 squares on it's first move
        // may move 1 square otherwise
        // capture diagonally
        Collection<ChessMove> returnVal = new HashSet<>();
        ChessPosition proposedPosition = null;

        if (color == ChessGame.TeamColor.WHITE){
            //propose 2 spaces ahead
            proposedPosition = new ChessPosition(position.getRow() + 2, position.getColumn());
            handleWhitePawnMove(board, position, proposedPosition, returnVal, color);

            //propose 1 space ahead
            proposedPosition = new ChessPosition(position.getRow() + 1, position.getColumn());
            handleWhitePawnMove(board, position, proposedPosition, returnVal, color);

            //propose the 2 two captures
            proposedPosition = new ChessPosition(position.getRow() + 1, position.getColumn() + 1);
            handleWhitePawnMove(board, position, proposedPosition, returnVal, color);
            proposedPosition = new ChessPosition(position.getRow() + 1, position.getColumn() - 1);
            handleWhitePawnMove(board, position, proposedPosition, returnVal, color);
        }else{
            //propose 2 spaces ahead
            proposedPosition = new ChessPosition(position.getRow() - 2, position.getColumn());
            handleBlackPawnMove(board, position, proposedPosition, returnVal, color);

            //propose 1 space ahead
            proposedPosition = new ChessPosition(position.getRow() - 1, position.getColumn());
            handleBlackPawnMove(board, position, proposedPosition, returnVal, color);

            //propose the 2 two captures
            proposedPosition = new ChessPosition(position.getRow() - 1, position.getColumn() + 1);
            handleBlackPawnMove(board, position, proposedPosition, returnVal, color);
            proposedPosition = new ChessPosition(position.getRow() - 1, position.getColumn() - 1);
            handleBlackPawnMove(board, position, proposedPosition, returnVal, color);
        }
        return returnVal;
    }

    public Collection<ChessMove> rookMoves(ChessBoard board, ChessPosition position){

        Collection<ChessMove> returnVal = new HashSet<>();
        // get current position
        int row = position.getRow();
        int col = position.getColumn();

        // all moves in a positive row path
        for(int i = 1; i < 8; i++){
            if(row + i <= 8){
                ChessPosition proposedPosition = new ChessPosition(row + i, col);

                if(board.validMove(proposedPosition) && !board.occupied(proposedPosition)){
                    returnVal.add(new ChessMove(position, proposedPosition, null));
                }
                if (board.occupiedByOppositeColor(proposedPosition, color)){
                    returnVal.add(new ChessMove(position, proposedPosition, null));
                    break;
                }
                if (board.occupied(proposedPosition)){
                    break;
                }
            }
        }

        // all moves in a negative row path
        for(int i = 1; i < 8; i++){
            if(row - i >= 1){
                ChessPosition proposedPosition = new ChessPosition(row - i, col);

                if(board.validMove(proposedPosition) && !board.occupied(proposedPosition)){
                    returnVal.add(new ChessMove(position, proposedPosition, null));
                }
                if (board.occupiedByOppositeColor(proposedPosition, color)){
                    returnVal.add(new ChessMove(position, proposedPosition, null));
                    break;
                }
                if (board.occupied(proposedPosition)){
                    break;
                }
            }
        }

        // all moves in a positive column path
        for(int i = 1; i < 8; i++){
            if(col + i <= 8){
                ChessPosition proposedPosition = new ChessPosition(row, col + i);

                if(board.validMove(proposedPosition) && !board.occupied(proposedPosition)){
                    returnVal.add(new ChessMove(position, proposedPosition, null));
                }
                if (board.occupiedByOppositeColor(proposedPosition, color)){
                    returnVal.add(new ChessMove(position, proposedPosition, null));
                    break;
                }
                if (board.occupied(proposedPosition)){
                    break;
                }
            }
        }

        // all moves in a negative column path
        for(int i = 1; i < 8; i++){
            if(col - i >= 1){
                ChessPosition proposedPosition = new ChessPosition(row, col - i);

                if(board.validMove(proposedPosition) && !board.occupied(proposedPosition)){
                    returnVal.add(new ChessMove(position, proposedPosition, null));
                }
                if (board.occupiedByOppositeColor(proposedPosition, color)){
                    returnVal.add(new ChessMove(position, proposedPosition, null));
                    break;
                }
                if (board.occupied(proposedPosition)){
                    break;
                }
            }
        }
        return returnVal;
    }

    private Collection<ChessMove> knightMovesHelper(ChessPosition proposedPosition, ChessPosition position, ChessBoard board, Collection<ChessMove> returnVal){
        if(board.validMove(proposedPosition) && !board.occupied(proposedPosition)){
            returnVal.add(new ChessMove(position, proposedPosition, null));
        }
        if(board.validMove(proposedPosition) && board.occupiedByOppositeColor(proposedPosition, color)){
            returnVal.add(new ChessMove(position, proposedPosition, null));
        }
        return returnVal;
    }

    public Collection<ChessMove> knightMoves(ChessBoard board, ChessPosition position){
        // 8 possible moves
        Collection<ChessMove> returnVal = new HashSet<>();
        int row = position.getRow();
        int col = position.getColumn();
        int proposedRow;
        int proposedCol;
        ChessPosition proposedPosition = null;


        //moves in a clockwise fashion
        //quadrant 1
        proposedCol = col + 1;
        proposedRow = row + 2;
        proposedPosition = new ChessPosition(proposedRow, proposedCol);
        knightMovesHelper(proposedPosition, position, board, returnVal);

        proposedCol = col + 2;
        proposedRow = row + 1;
        proposedPosition = new ChessPosition(proposedRow, proposedCol);
        knightMovesHelper(proposedPosition, position, board, returnVal);

        //quadrant 2
        proposedCol = col + 2;
        proposedRow = row - 1;
        proposedPosition = new ChessPosition(proposedRow, proposedCol);
        knightMovesHelper(proposedPosition, position, board, returnVal);

        proposedCol = col + 1;
        proposedRow = row - 2;
        proposedPosition = new ChessPosition(proposedRow, proposedCol);
        knightMovesHelper(proposedPosition, position, board, returnVal);

        //quadrant 3
        proposedCol = col - 1;
        proposedRow = row - 2;
        proposedPosition = new ChessPosition(proposedRow, proposedCol);
        knightMovesHelper(proposedPosition, position, board, returnVal);

        proposedCol = col - 2;
        proposedRow = row - 1;
        proposedPosition = new ChessPosition(proposedRow, proposedCol);
        knightMovesHelper(proposedPosition, position, board, returnVal);

        //quadrant 4
        proposedCol = col - 2;
        proposedRow = row + 1;
        proposedPosition = new ChessPosition(proposedRow, proposedCol);
        knightMovesHelper(proposedPosition, position, board, returnVal);

        proposedCol = col - 1;
        proposedRow = row + 2;
        proposedPosition = new ChessPosition(proposedRow, proposedCol);
        knightMovesHelper(proposedPosition, position, board, returnVal);

        return returnVal;
    }

    public Collection<ChessMove> bishopMoves(ChessBoard board, ChessPosition position){
        // all moves in quadrant 1 direction
        Collection<ChessMove> returnVal = new HashSet<>();

        for(int i = 1; i <= 7; i++){
            ChessPosition proposedPosition = new ChessPosition(position.getRow() + i, position.getColumn() + i);
            if (bishopMoveHelper(board, position, returnVal, proposedPosition)) break;
        }

        // all moves in quadrant 2 direction
        for(int i = 1; i <= 7; i++){
            ChessPosition proposedPosition = new ChessPosition(position.getRow() + i, position.getColumn() - i);
            if (bishopMoveHelper(board, position, returnVal, proposedPosition)) break;
        }

        // all moves in quadrant 3 direction
        for(int i = 1; i <= 7; i++){
            ChessPosition proposedPosition = new ChessPosition(position.getRow() - i, position.getColumn() - i);
            if (bishopMoveHelper(board, position, returnVal, proposedPosition)) break;
        }

        // all moves in quadrant 4 direction
        for(int i = 1; i <= 7; i++){
            ChessPosition proposedPosition = new ChessPosition(position.getRow() - i, position.getColumn() + i);
            if (bishopMoveHelper(board, position, returnVal, proposedPosition)) break;
        }

        return returnVal;
    }

    private boolean bishopMoveHelper(ChessBoard board, ChessPosition position, Collection<ChessMove> returnVal, ChessPosition proposedPosition) {
        if(board.validMove(proposedPosition) && !board.occupied(proposedPosition)){
            returnVal.add(new ChessMove(position, proposedPosition, null));
        }
        if(board.validMove(proposedPosition) && board.occupiedByOppositeColor(proposedPosition, color)){
            returnVal.add(new ChessMove(position, proposedPosition, null));
            return true;
        }
        if(!board.validMove(proposedPosition) || board.occupied(proposedPosition)){
            return true;
        }
        return false;
    }

    public Collection<ChessMove> queenMoves(ChessBoard board, ChessPosition position){
        // could combine getting the moves for a bishop and rook - but must change all types to queen
        Collection<ChessMove> rookMoves = rookMoves(board, position);
        Collection<ChessMove> bishopMoves = bishopMoves(board, position);
        Collection<ChessMove> combined = new HashSet<>();

        combined.addAll(rookMoves);
        combined.addAll(bishopMoves);

        return combined;
    }

    public Collection<ChessMove> kingMoves(ChessBoard board, ChessPosition position){
        // dumb and lazy. But important. Move in any direction 1 space
        Collection<ChessMove> returnVal = new HashSet<>();
        int row = position.getRow();
        int col = position.getColumn();
        int proposedRow;
        int proposedCol;

        proposedCol = col;
        proposedRow = row + 1;
        kingMoveHelper(board, position, returnVal, row, proposedCol, proposedRow);

        proposedCol = col + 1;
        proposedRow = row + 1;
        kingMoveHelper(board, position, returnVal, row, proposedCol, proposedRow);

        proposedCol = col + 1;
        proposedRow = row;
        kingMoveHelper(board, position, returnVal, row, proposedCol, proposedRow);

        proposedCol = col + 1;
        proposedRow = row - 1;
        kingMoveHelper(board, position, returnVal, row, proposedCol, proposedRow);

        proposedCol = col;
        proposedRow = row - 1;
        kingMoveHelper(board, position, returnVal, row, proposedCol, proposedRow);

        proposedCol = col - 1;
        proposedRow = row - 1;
        kingMoveHelper(board, position, returnVal, row, proposedCol, proposedRow);

        proposedCol = col - 1;
        proposedRow = row;
        kingMoveHelper(board, position, returnVal, row, proposedCol, proposedRow);

        proposedCol = col - 1;
        proposedRow = row + 1;
        kingMoveHelper(board, position, returnVal, row, proposedCol, proposedRow);

        return returnVal;
    }

    private void kingMoveHelper(ChessBoard board, ChessPosition position, Collection<ChessMove> returnVal, int row, int proposedCol, int proposedRow) {
        ChessPosition proposedPosition;
        proposedPosition = new ChessPosition(proposedRow, proposedCol);
        if(board.validMove(proposedPosition) && !board.occupied(proposedPosition)){
            returnVal.add(new ChessMove(position, proposedPosition, null));
        }
        if(board.validMove(proposedPosition) && board.occupiedByOppositeColor(proposedPosition, color)){
            returnVal.add(new ChessMove(position, proposedPosition, null));
        }
    }
}
