package chess;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChessGame chessGame = (ChessGame) o;
        return Objects.deepEquals(board, chessGame.board) && currentTurn == chessGame.currentTurn;
    }

    @Override
    public int hashCode() {
        return Objects.hash(board, currentTurn);
    }

    private ChessBoard board;
    private TeamColor currentTurn;

    public ChessGame() {
        this.board = new ChessBoard();
        this.currentTurn = TeamColor.WHITE;
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return currentTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        currentTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> possibleMoves(ChessPosition startPosition) {
        var focusedPiece = board.getPiece(startPosition);
        Collection<ChessMove> returnVal = new HashSet<>();

        if (focusedPiece == null) {
            return null;
        }
        Collection<ChessMove> possibleMoves = focusedPiece.pieceMoves(board, startPosition);
        return possibleMoves;
    }

    public Collection<ChessMove> validMoves(ChessPosition startPosition){
        var focusedPiece = board.getPiece(startPosition);
        Collection<ChessMove> returnVal = new HashSet<>();

        if(focusedPiece == null){
            return null;
        }

        Collection<ChessMove> possibleMoves = focusedPiece.pieceMoves(board, startPosition);
        //the moves that put us in checkmate we should not do
        ChessGame testGame = new ChessGame();
        for(ChessMove move : possibleMoves){
            testGame.board = new ChessBoard(boardDeepCopy(this.board.getBoard()));

            testGame.board.setBoard(move.getEndPosition().getRow(),move.getEndPosition().getColumn(), this.board.getPiece(move.getStartPosition()));
            testGame.board.setBoard(move.getStartPosition().getRow(), move.getStartPosition().getColumn(), null);

            if(!testGame.checkmatePostMove(focusedPiece.getTeamColor())){
                returnVal.add(move);
            }
        }
        return returnVal;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessGame.TeamColor pieceInQuestionColor = board.getPiece(move.getStartPosition()).getTeamColor();
        if(getTeamTurn() != pieceInQuestionColor){
            throw new InvalidMoveException("Invalid move, it must be your turn" + move.toString());
        }

        Collection<ChessMove> possibleMoves = possibleMoves(move.getStartPosition());

        //are we currently in check?
        boolean check = isInCheck(getTeamTurn());
        //would our move get us out of check? If we are in checkmate, it's game over.

        if(move.containedWithin(possibleMoves)){
            //make move

            //see if this would put us in checkmate then don't do it
            ChessGame testGame = new ChessGame();
            testGame.board = new ChessBoard(boardDeepCopy(this.board.getBoard()));
            testGame.makeMockMove(move);

            if(check && testGame.isInCheckmate(getTeamTurn())){
                throw new InvalidMoveException("Invalid move, cannot move" + move.toString());
            }

            //do the actual move
            this.board.setBoard(move.getEndPosition().getRow(),move.getEndPosition().getColumn(), this.board.getPiece(move.getStartPosition()));
            this.board.setBoard(move.getStartPosition().getRow(), move.getStartPosition().getColumn(), null);

            if(move.getPromotionPiece() != null){
                this.board.setBoard(move.getEndPosition().getRow(),move.getEndPosition().getColumn(), new ChessPiece(currentTurn, move.getPromotionPiece()));
            }

            //set the team
            if(pieceInQuestionColor == TeamColor.WHITE){
                setTeamTurn(TeamColor.BLACK);
            }else{
                setTeamTurn(TeamColor.WHITE);
            }
        }else{
            throw new InvalidMoveException("Invalid move, cannot move" + move.toString());
        }
    }

    public void makeMockMove(ChessMove move) throws InvalidMoveException {
        Collection<ChessMove> possibleMoves = possibleMoves(move.getStartPosition());

        if(move.containedWithin(possibleMoves)){
            this.board.setBoard(move.getEndPosition().getRow(),move.getEndPosition().getColumn(), this.board.getPiece(move.getStartPosition()));
            this.board.setBoard(move.getStartPosition().getRow(), move.getStartPosition().getColumn(), null);
        }else{
            throw new InvalidMoveException("Invalid move, cannot move" + move.toString());
        }
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        // Is the king being threatened directly?
        Collection<ChessMove> combined = new HashSet<>();
        ChessPosition kingPosition = null;

        for(int i = 1; i <= 8; i++){
            for(int j = 1; j <= 8; j++){
                ChessPosition position = new ChessPosition(i, j);
                if (board.occupiedByOppositeColor(position, teamColor)){
                    combined.addAll(possibleMoves(position));
                }
                if(board.getPiece(position) == null){
                    continue;
                }
                if(board.getPiece(position).getPieceType() == ChessPiece.PieceType.KING && board.getPiece(position).getTeamColor() == teamColor){
                    kingPosition = position;
                }
            }
        }

        for (ChessMove move : combined){
            if(move.getEndPosition().equals(kingPosition)){
                return true;
            }
        }
        return false;
        //get position of teamColor's king and see if it is in the combined set

    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean checkmatePostMove(TeamColor teamColor){
        Collection<ChessMove> validEnemyMoves = new HashSet<>();
        ChessPosition kingPosition = null;

        for(int i = 1; i <= 8; i++){
            for(int j = 1; j <= 8; j++){
                ChessPosition position = new ChessPosition(i, j);
                if (board.occupiedByOppositeColor(position, teamColor)){
                    validEnemyMoves.addAll(possibleMoves(position));
                }
                if(board.getPiece(position) != null){
                    if(board.getPiece(position).getPieceType() == ChessPiece.PieceType.KING && board.getPiece(position).getTeamColor() == teamColor){
                        kingPosition = position;
                    }
                }
            }
        }
        for(ChessMove enemyMove : validEnemyMoves) {
            if (enemyMove.getEndPosition().equals(kingPosition)) {
                return true;
            }
        }

        return false;
    }

    public boolean isInCheckmate(TeamColor teamColor) {
        // of all possible moves, is there a possibility for the enemy to take the king?
        // get all the possibilities for our team to move
        Collection<ChessMove> validSelfMoves = new HashSet<>();
        ChessGame.TeamColor enemyColor = null;
        ChessPosition kingPosition = null;

        if(teamColor == TeamColor.WHITE){
            enemyColor = TeamColor.BLACK;
        }else{
            enemyColor = TeamColor.WHITE;
        }

        for(int i = 1; i <= 8; i++){
            for(int j = 1; j <= 8; j++){
                ChessPosition position = new ChessPosition(i, j);
                if (board.occupiedByOppositeColor(position, enemyColor)){
                    validSelfMoves.addAll(possibleMoves(position));
                }
                if(board.getPiece(position) == null){
                    continue;
                }
                if(board.getPiece(position).getPieceType() == ChessPiece.PieceType.KING && board.getPiece(position).getTeamColor() == teamColor){
                    kingPosition = position;
                }
            }
        }

        // make a hypothetical situation where each of these moves are made then see if the enemy can take our king
        ChessGame testGame = new ChessGame();
        Collection<ChessMove> validEnemyMoves = new HashSet<>();

        //apply each of our moves to the board and see if the enemy has a way of taking our king
        for(ChessMove move : validSelfMoves){
            testGame.board = new ChessBoard(boardDeepCopy(this.board.getBoard()));
            //execute the move then see if the enemy has a way of taking our king
            try{
                testGame.makeMockMove(move);
            } catch (InvalidMoveException e) {
                continue;
            }
            //now go through all the moves the enemy can make
            for(int i = 1; i <= 8; i++){
                for(int j = 1; j <= 8; j++){
                    ChessPosition position = new ChessPosition(i, j);
                    if (board.occupiedByOppositeColor(position, teamColor)){
                        validEnemyMoves.addAll(possibleMoves(position));
                    }
                }
            }
            boolean enemyCanTake = false;
            //now can any of these take the king?
            for(ChessMove enemyMove : validEnemyMoves){
                if(enemyMove.getEndPosition().equals(kingPosition)){
                    enemyCanTake = true;
                    break;
                }
            }
            if(!enemyCanTake){
                return false;
            }
        }
        return true;
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        //combine all possible moves, if count is 0, fail
        ChessGame.TeamColor enemyColor = null;
        Collection<ChessMove> combined = new HashSet<>();
        Collection<ChessMove> returnSet = new HashSet<>();

        if(teamColor == TeamColor.WHITE){
            enemyColor = TeamColor.BLACK;
        }else{
            enemyColor = TeamColor.WHITE;
        }

        for(int i = 1; i <= 8; i++){
            for(int j = 1; j <= 8; j++){
                ChessPosition position = new ChessPosition(i, j);
                if (board.occupiedByOppositeColor(position, enemyColor)){
                    combined.addAll(possibleMoves(position));
                }
            }
        }
        //do any of these put me in checkmate?
        ChessGame testGame = new ChessGame();
        for(ChessMove move : combined){
            testGame.board = new ChessBoard(boardDeepCopy(this.board.getBoard()));
            try{
                testGame.makeMockMove(move);
            } catch (InvalidMoveException e) {
                continue;
            }
            if(!testGame.isInCheckmate(teamColor)){
                returnSet.add(move);
            }
        }
        return returnSet.isEmpty();
    }

    public static ChessPiece[][] boardDeepCopy(ChessPiece[][] original) {
        ChessPiece[][] copy = new ChessPiece[original.length][];
        for (int i = 0; i < original.length; i++) {
            copy[i] = new ChessPiece[original[i].length];
            System.arraycopy(original[i], 0, copy[i], 0, original[i].length);
        }
        return copy;
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }
}
