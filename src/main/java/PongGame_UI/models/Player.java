package PongGame_UI.models;

import java.util.Objects;

import com.fluentinterface.ReflectionBuilder;
import com.fluentinterface.builder.Builder;

public class Player {

	private long id;
	private String username;
	private GyroscopeCoordinates gyroscopeCoordinates;

	public static PlayerBuilder create() {
		return ReflectionBuilder.implementationFor(PlayerBuilder.class)
				.create();
	}

	public interface PlayerBuilder extends Builder<Player> {
		public PlayerBuilder withUsername(String username);

		public PlayerBuilder withGyroscopeCoordinates(
				GyroscopeCoordinates gyroscopeCoordinates);
		
		public PlayerBuilder withId(long id);
	}

	public GyroscopeCoordinates getGyroscopeCoordinates() {
		return gyroscopeCoordinates;
	}

	public void setGyroscopeCoordinates(
			GyroscopeCoordinates gyroscopeCoordinates) {
		this.gyroscopeCoordinates = gyroscopeCoordinates;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	@Override
	public int hashCode() {
		return Objects.hash(getUsername(), getGyroscopeCoordinates());
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof GyroscopeCoordinates)
				&& Objects.equals(getUsername(), ((Player) obj).getUsername())
				&& Objects.equals(getGyroscopeCoordinates(),
						((Player) obj).getGyroscopeCoordinates());
	}
}
