package communication.model;

import lombok.Builder;

@Builder
public class NeighbourConnectionDto {
	private String id;
	private String address;
	private int port;
}
