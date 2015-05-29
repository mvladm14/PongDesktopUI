package PongGame_UI.interfaces;

import java.util.Collection;

import PongGame_UI.models.GyroscopeCoordinates;
import PongGame_UI.models.Player;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;


public interface PongSvcApi {

	public static final String PONG_PLAYERS = "/player";
	public static final String ID_PARAMETER = "id";
	public static final String PONG_PLAYER_PATH = PONG_PLAYERS + "/{id}/";

	/**
	 * This endpoint in the API returns the players that have been added to the
	 * server. The Player objects should be returned as JSON.
	 * 
	 * To manually test this endpoint, run your server and open this URL in a
	 * browser: http://localhost:8080/myriads/player
	 * 
	 * @return
	 */
	@GET(PONG_PLAYERS)
	public Collection<Player> getPlayersList();

	@POST(PONG_PLAYERS)
	public Player addPlayer(@Body Player p);

	/**
	 * This endpoint in the API returns the gyroscope coordinates that have been
	 * added to the server. The GryoscopeCoordinates objects should be returned
	 * as JSON.
	 * 
	 * To manually test this endpoint, run your server and open this URL in a
	 * browser: http://localhost:8080/myriads/player/{playerId}
	 * 
	 * @return
	 */
	@GET(PONG_PLAYER_PATH)
	public GyroscopeCoordinates getGyroscopeCoordinates(
			@Path(ID_PARAMETER) long id);

	/**
	 * This endpoint allows clients to add GryoscopeCoordinates objects by
	 * sending POST requests that have an application/json body containing the
	 * GryoscopeCoordinates object information.
	 * 
	 * @return
	 */
	@POST(PONG_PLAYER_PATH)
	public GyroscopeCoordinates setGyroscopeCoordinates(
			@Path(ID_PARAMETER) long id, @Body GyroscopeCoordinates gv);

}
