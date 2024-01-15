package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

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
                returnVal = KingMoves(board, myPosition);
                break;
            case QUEEN:
                returnVal = QueenMoves(board, myPosition);
                break;
            case BISHOP:
                returnVal = BishopMoves(board, myPosition);
                break;
            case KNIGHT:
                returnVal = KnightMoves(board, myPosition);
                break;
            case ROOK:
                returnVal = RookMoves(board, myPosition);
                break;
            case PAWN:
                returnVal = PawnMoves(board, myPosition, color);
                break;
            default:
                System.out.println("Invalid piece, cannot determine possible moves: " + type);
                System.exit(1);
        }
        return returnVal;
    }

    public Collection<ChessMove> PawnMoves(ChessBoard board, ChessPosition position, ChessGame.TeamColor color){
        // may move 2 squares on it's first move
        // may move 1 square otherwise
        // capture diagonally
        Collection<ChessMove> returnVal = new ArrayList<>();

        if (color == ChessGame.TeamColor.WHITE){
            if (position.getRow() == 1){
                //allow move 2 ahead
                if (!board.occupied(3, position.getColumn())){
                    returnVal.add(new ChessMove(position, new ChessPosition(3, position.getColumn()), PieceType.PAWN));
                }
            }
            if (position.getRow() == 6){
                //here we will handle promotions
                if (!board.occupied(7, position.getColumn())){
                    returnVal.add(new ChessMove(position, new ChessPosition(7, position.getColumn()), PieceType.KING));
                    returnVal.add(new ChessMove(position, new ChessPosition(7, position.getColumn()), PieceType.QUEEN));
                    returnVal.add(new ChessMove(position, new ChessPosition(7, position.getColumn()), PieceType.BISHOP));
                    returnVal.add(new ChessMove(position, new ChessPosition(7, position.getColumn()), PieceType.BISHOP));
                    returnVal.add(new ChessMove(position, new ChessPosition(7, position.getColumn()), PieceType.KNIGHT));
                    returnVal.add(new ChessMove(position, new ChessPosition(7, position.getColumn()), PieceType.ROOK));
                }
            }
            //add in default move of 1 space
            if (!board.occupied(position.getRow() + 1, position.getColumn())){
                returnVal.add(new ChessMove(position, new ChessPosition(position.getRow() + 1, position.getColumn()), PieceType.PAWN));
            }
        }else{
            if (position.getRow() == 6){
                //allow move 2 ahead
                if (!board.occupied(4, position.getColumn())){
                    returnVal.add(new ChessMove(position, new ChessPosition(4, position.getColumn()), PieceType.PAWN));
                }
            }
            if (position.getRow() == 1){
                //handle promotions
                if (!board.occupied(1, position.getColumn())){
                    returnVal.add(new ChessMove(position, new ChessPosition(0, position.getColumn()), PieceType.KING));
                    returnVal.add(new ChessMove(position, new ChessPosition(0, position.getColumn()), PieceType.QUEEN));
                    returnVal.add(new ChessMove(position, new ChessPosition(0, position.getColumn()), PieceType.BISHOP));
                    returnVal.add(new ChessMove(position, new ChessPosition(0, position.getColumn()), PieceType.BISHOP));
                    returnVal.add(new ChessMove(position, new ChessPosition(0, position.getColumn()), PieceType.KNIGHT));
                    returnVal.add(new ChessMove(position, new ChessPosition(0, position.getColumn()), PieceType.ROOK));
                }
            }
            //add in default move of 1 space
            if (!board.occupied(position.getRow() - 1, position.getColumn())){
                returnVal.add(new ChessMove(position, new ChessPosition(position.getRow() - 1, position.getColumn()), PieceType.PAWN));
            }
        }
        return returnVal;
    }

    public Collection<ChessMove> RookMoves(ChessBoard board, ChessPosition position){

    }

    public Collection<ChessMove> KnightMoves(ChessBoard board, ChessPosition position){

    }

    public Collection<ChessMove> BishopMoves(ChessBoard board, ChessPosition position){

    }

    public Collection<ChessMove> QueenMoves(ChessBoard board, ChessPosition position){

    }

    public Collection<ChessMove> KingMoves(ChessBoard board, ChessPosition position){

    }
}
