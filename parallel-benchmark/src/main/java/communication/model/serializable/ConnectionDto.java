package communication.model.serializable;

import communication.model.NeighbourConnectionDto;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ConnectionDto implements CustomSerializable<NeighbourConnectionDto>{

	private String address;
	private int port;
	private String id;

	@Override
	public NeighbourConnectionDto toRealObject() {
		return NeighbourConnectionDto.builder()
			.address(address)
			.port(port)
			.id(id)
			.build();
	}
}
