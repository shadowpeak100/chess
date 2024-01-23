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
        Collection<ChessMove> postFormat = new HashSet<>();
        Collection<ChessMove> returnVal = null;

        // the program uses 0-7 numbering while users use 1-8, convert to computer ready index
        ChessPosition updatedPosition = new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn() - 1);

        switch (type){
            case KING:
                returnVal = KingMoves(board, updatedPosition);
                break;
            case QUEEN:
                returnVal = QueenMoves(board, updatedPosition);
                break;
            case BISHOP:
                returnVal = BishopMoves(board, updatedPosition);
                break;
            case KNIGHT:
                returnVal = KnightMoves(board, updatedPosition);
                break;
            case ROOK:
                returnVal = RookMoves(board, updatedPosition);
                break;
            case PAWN:
                returnVal = PawnMoves(board, updatedPosition, color);
                break;
            default:
                System.out.println("Invalid piece, cannot determine possible moves: " + type);
                System.exit(1);
        }
        // convert back to 1-8 numbering
        for (ChessMove cM : returnVal) {
            postFormat.add(new ChessMove(myPosition, new ChessPosition(cM.getEndPosition().getRow() + 1, cM.getEndPosition().getColumn() + 1), cM.getPromotionPiece()));
        }
        return postFormat;
    }

    public void handleWhitePawnMove(ChessBoard board, ChessPosition currentPosition, ChessPosition proposedPosition, Collection<ChessMove> returnVal, ChessGame.TeamColor color){
        //is it a valid 1 space move
        if(!board.occupied(proposedPosition) && Math.abs(proposedPosition.getRow() - currentPosition.getRow()) == 1){
            // is it an edge or not?
            if(!handlePromotionWhite(board, proposedPosition, returnVal)){
                returnVal.add(new ChessMove(currentPosition, proposedPosition, null));

            }
        }

        //is it a valid 2 space move
        if (currentPosition.getRow() == 1 && proposedPosition.getRow() == 3){
            if (!board.occupied(proposedPosition)){
                returnVal.add(new ChessMove(currentPosition, proposedPosition, null));
            }
        }

        //is it a valid capture
        if(Math.abs(currentPosition.getColumn() - proposedPosition.getColumn()) == 1 && Math.abs(currentPosition.getRow() - proposedPosition.getRow()) == 1){
            if (board.occupiedEnemy(proposedPosition, color)){
                //is it a promotion or not?
                if(!handlePromotionWhite(board, proposedPosition, returnVal)){
                    returnVal.add(new ChessMove(currentPosition, proposedPosition, null));
                }
            }
        }
    }

    public boolean handlePromotionWhite(ChessBoard board, ChessPosition position, Collection<ChessMove> returnVal){
        if (position.getRow() == 6) {
            if (!board.occupied(7, position.getColumn())) {
                returnVal.add(new ChessMove(position, new ChessPosition(7, position.getColumn()), PieceType.QUEEN));
                returnVal.add(new ChessMove(position, new ChessPosition(7, position.getColumn()), PieceType.BISHOP));
                returnVal.add(new ChessMove(position, new ChessPosition(7, position.getColumn()), PieceType.KNIGHT));
                returnVal.add(new ChessMove(position, new ChessPosition(7, position.getColumn()), PieceType.ROOK));
                return true;
            }
        }
        return false;
    }

    public boolean handlePromotionBlack(ChessBoard board, ChessPosition position, Collection<ChessMove> returnVal){
        if (position.getRow() == 1){
            if (!board.occupied(0, position.getColumn())){
                returnVal.add(new ChessMove(position, new ChessPosition(0, position.getColumn()), PieceType.QUEEN));
                returnVal.add(new ChessMove(position, new ChessPosition(0, position.getColumn()), PieceType.BISHOP));
                returnVal.add(new ChessMove(position, new ChessPosition(0, position.getColumn()), PieceType.KNIGHT));
                returnVal.add(new ChessMove(position, new ChessPosition(0, position.getColumn()), PieceType.ROOK));
                return true;
            }
        }
        return false;
    }



    public Collection<ChessMove> PawnMoves(ChessBoard board, ChessPosition position, ChessGame.TeamColor color){
        // may move 2 squares on it's first move
        // may move 1 square otherwise
        // capture diagonally
        Collection<ChessMove> returnVal = new HashSet<>();

        if (color == ChessGame.TeamColor.WHITE){
            if (position.getRow() == 1){
                //allow move 2 ahead
                ChessPosition proposed = new ChessPosition(3, position.getColumn());
                if (!board.occupied(proposed)){
                    returnVal.add(new ChessMove(position, proposed, null));
                }
            }
            boolean promoted = handlePromotionWhite(board, position, returnVal);
            //add in default move of 1 space
            if (!board.occupied(position.getRow() + 1, position.getColumn()) && !promoted){
                returnVal.add(new ChessMove(position, new ChessPosition(position.getRow() + 1, position.getColumn()), null));
            }
            //add in moves for taking enemy piece
            if (board.occupiedEnemy(position.getRow() + 1, position.getColumn() + 1, color) && board.validMove(position.getRow() + 1, position.getColumn() + 1)){
                if (!handlePromotionWhite(board, position, returnVal)){
                    returnVal.add(new ChessMove(position, new ChessPosition(position.getRow() + 1, position.getColumn() + 1), null));
                }
            }
            if (board.occupiedEnemy(position.getRow() + 1, position.getColumn() - 1, color) && board.validMove(position.getRow() + 1, position.getColumn() - 1)){
                if (!handlePromotionWhite(board, position, returnVal)){
                    returnVal.add(new ChessMove(position, new ChessPosition(position.getRow() + 1, position.getColumn() - 1), null));
                }
            }
        }else{
            // black pieces
            if (position.getRow() == 6){
                //allow move 2 ahead
                if (!board.occupied(4, position.getColumn())){
                    returnVal.add(new ChessMove(position, new ChessPosition(4, position.getColumn()), null));
                }
            }
            boolean promoted = handlePromotionBlack(board, position, returnVal);
            //add in default move of 1 space
            if (!board.occupied(position.getRow() - 1, position.getColumn()) && !promoted){
                returnVal.add(new ChessMove(position, new ChessPosition(position.getRow() - 1, position.getColumn()), null));
            }
            //add in moves for taking enemy piece
            if (board.occupiedEnemy(position.getRow() - 1, position.getColumn() - 1, color) && board.validMove(position.getRow() - 1, position.getColumn() - 1)){
                if (!handlePromotionBlack(board, position, returnVal)) {
                    returnVal.add(new ChessMove(position, new ChessPosition(position.getRow() - 1, position.getColumn() - 1), null));
                }
            }
            if (board.occupiedEnemy(position.getRow() - 1, position.getColumn() + 1, color) && board.validMove(position.getRow() - 1, position.getColumn() + 1)){
                if (!handlePromotionBlack(board, position, returnVal)) {
                    returnVal.add(new ChessMove(position, new ChessPosition(position.getRow() - 1, position.getColumn() + 1), null));
                }
            }
        }
        return returnVal;
    }

    public Collection<ChessMove> RookMoves(ChessBoard board, ChessPosition position){

        Collection<ChessMove> returnVal = new HashSet<>();
        // get current position
        int row = position.getRow();
        int col = position.getColumn();

        // all moves in a positive row path
        for(int i = 1; i < 8; i++){
            if(row + i <= 7){
                ChessPosition proposedPosition = new ChessPosition(row + i, col);
                if(!board.occupied(proposedPosition)){
                    returnVal.add(new ChessMove(position, proposedPosition, null));
                }
            }
        }

        // all moves in a negative row path
        for(int i = 1; i < 8; i++){
            if(row - i >= 0){
                ChessPosition proposedPosition = new ChessPosition(row - i, col);
                if(!board.occupied(proposedPosition)){
                    returnVal.add(new ChessMove(position, proposedPosition, null));
                }
            }
        }

        // all moves in a positive column path
        for(int i = 1; i < 8; i++){
            if(col + i <= 7){
                ChessPosition proposedPosition = new ChessPosition(row, col + i);
                if(!board.occupied(proposedPosition)){
                    returnVal.add(new ChessMove(position, proposedPosition, null));
                }
            }
        }

        // all moves in a negative column path
        for(int i = 1; i < 8; i++){
            if(col - i >= 0){
                ChessPosition proposedPosition = new ChessPosition(row, col - i);
                if(!board.occupied(proposedPosition)){
                    returnVal.add(new ChessMove(position, new ChessPosition(row, col - i), null));
                }
            }
        }
        return returnVal;
    }

    public Collection<ChessMove> KnightMoves(ChessBoard board, ChessPosition position){
        // 8 possible moves
        Collection<ChessMove> returnVal = new HashSet<>();
        int row = position.getRow();
        int col = position.getColumn();
        int proposedRow;
        int proposedCol;

        //moves in a clockwise fashion
        //quadrant 1
        proposedCol = col + 1;
        proposedRow = row + 2;
        if(board.validMove(proposedRow, proposedCol) && !board.occupied(proposedRow, proposedCol)){
            returnVal.add(new ChessMove(position, new ChessPosition(proposedRow, proposedCol), null));
        }

        proposedCol = col + 2;
        proposedRow = row + 1;
        if(board.validMove(proposedRow, proposedCol) && !board.occupied(proposedRow, proposedCol)){
            returnVal.add(new ChessMove(position, new ChessPosition(proposedRow, proposedCol), null));
        }

        //quadrant 2
        proposedCol = col + 2;
        proposedRow = row - 1;
        if(board.validMove(proposedRow, proposedCol) && !board.occupied(proposedRow, proposedCol)){
            returnVal.add(new ChessMove(position, new ChessPosition(proposedRow, proposedCol), null));
        }

        proposedCol = col + 1;
        proposedRow = row - 2;
        if(board.validMove(proposedRow, proposedCol) && !board.occupied(proposedRow, proposedCol)){
            returnVal.add(new ChessMove(position, new ChessPosition(proposedRow, proposedCol), null));
        }

        //quadrant 3
        proposedCol = col - 1;
        proposedRow = row - 2;
        if(board.validMove(proposedRow, proposedCol) && !board.occupied(proposedRow, proposedCol)){
            returnVal.add(new ChessMove(position, new ChessPosition(proposedRow, proposedCol), null));
        }

        proposedCol = col - 2;
        proposedRow = row - 1;
        if(board.validMove(proposedRow, proposedCol) && !board.occupied(proposedRow, proposedCol)){
            returnVal.add(new ChessMove(position, new ChessPosition(proposedRow, proposedCol), null));
        }

        //quadrant 4
        proposedCol = col - 2;
        proposedRow = row + 1;
        if(board.validMove(proposedRow, proposedCol) && !board.occupied(proposedRow, proposedCol)){
            returnVal.add(new ChessMove(position, new ChessPosition(proposedRow, proposedCol), null));
        }

        proposedCol = col - 1;
        proposedRow = row + 2;
        if(board.validMove(proposedRow, proposedCol) && !board.occupied(proposedRow, proposedCol)){
            returnVal.add(new ChessMove(position, new ChessPosition(proposedRow, proposedCol), null));
        }

        return returnVal;
    }

    public Collection<ChessMove> BishopMoves(ChessBoard board, ChessPosition position){
        // all moves in quadrant 1 direction
        Collection<ChessMove> returnVal = new HashSet<>();

        for(int i = 1; i <= 7; i++){
            ChessPosition proposedPosition = new ChessPosition(position.getRow() + i, position.getColumn() + i);
            if(board.validMove(proposedPosition) && !board.occupied(proposedPosition)){
                returnVal.add(new ChessMove(position, proposedPosition, null));
            }
        }

        // all moves in quadrant 2 direction
        for(int i = 1; i <= 7; i++){
            ChessPosition proposedPosition = new ChessPosition(position.getRow() + i, position.getColumn() - i);
            if(board.validMove(proposedPosition) && !board.occupied(proposedPosition)){
                returnVal.add(new ChessMove(position, proposedPosition, null));
            }
        }

        // all moves in quadrant 3 direction
        for(int i = 1; i <= 7; i++){
            ChessPosition proposedPosition = new ChessPosition(position.getRow() - i, position.getColumn() - i);
            if(board.validMove(proposedPosition) && !board.occupied(proposedPosition)){
                returnVal.add(new ChessMove(position, proposedPosition, null));
            }
        }

        // all moves in quadrant 4 direction
        for(int i = 1; i <= 7; i++){
            ChessPosition proposedPosition = new ChessPosition(position.getRow() - i, position.getColumn() + i);
            if(board.validMove(proposedPosition) && !board.occupied(proposedPosition)){
                returnVal.add(new ChessMove(position, proposedPosition, null));
            }
        }

        return returnVal;
    }

    public Collection<ChessMove> QueenMoves(ChessBoard board, ChessPosition position){
        // could combine getting the moves for a bishop and rook - but must change all types to queen
        Collection<ChessMove> knightMoves = KnightMoves(board, position);
        Collection<ChessMove> bishopMoves = BishopMoves(board, position);
        Collection<ChessMove> combined = new HashSet<>();

        combined.addAll(knightMoves);
        combined.addAll(bishopMoves);

        return combined;
    }

    public Collection<ChessMove> KingMoves(ChessBoard board, ChessPosition position){
        // dumb and lazy. But important. Move in any direction 1 space
        Collection<ChessMove> returnVal = new HashSet<>();
        int row = position.getRow();
        int col = position.getColumn();
        int proposedRow;
        int proposedCol;

        proposedCol = col;
        proposedRow = row + 1;
        if(board.validMove(proposedRow, proposedCol) && !board.occupied(proposedRow, proposedCol)){
            returnVal.add(new ChessMove(position, new ChessPosition(proposedRow, proposedCol), null));
        }

        proposedCol = col + 1;
        proposedRow = row + 1;
        if(board.validMove(proposedRow, proposedCol) && !board.occupied(proposedRow, proposedCol)){
            returnVal.add(new ChessMove(position, new ChessPosition(proposedRow, proposedCol), null));
        }

        proposedCol = col + 1;
        proposedRow = row;
        if(board.validMove(proposedRow, proposedCol) && !board.occupied(proposedRow, proposedCol)){
            returnVal.add(new ChessMove(position, new ChessPosition(proposedRow, proposedCol), null));
        }

        proposedCol = col + 1;
        proposedRow = row - 1;
        if(board.validMove(proposedRow, proposedCol) && !board.occupied(proposedRow, proposedCol)){
            returnVal.add(new ChessMove(position, new ChessPosition(proposedRow, proposedCol), null));
        }

        proposedCol = col;
        proposedRow = row - 1;
        if(board.validMove(proposedRow, proposedCol) && !board.occupied(proposedRow, proposedCol)){
            returnVal.add(new ChessMove(position, new ChessPosition(proposedRow, proposedCol), null));
        }

        proposedCol = col - 1;
        proposedRow = row - 1;
        if(board.validMove(proposedRow, proposedCol) && !board.occupied(proposedRow, proposedCol)){
            returnVal.add(new ChessMove(position, new ChessPosition(proposedRow, proposedCol), null));
        }

        proposedCol = col - 1;
        proposedRow = row;
        if(board.validMove(proposedRow, proposedCol) && !board.occupied(proposedRow, proposedCol)){
            returnVal.add(new ChessMove(position, new ChessPosition(proposedRow, proposedCol), null));
        }

        proposedCol = col - 1;
        proposedRow = row + 1;
        if(board.validMove(proposedRow, proposedCol) && !board.occupied(proposedRow, proposedCol)){
            returnVal.add(new ChessMove(position, new ChessPosition(proposedRow, proposedCol), null));
        }

        return returnVal;
    }
}
