package nl.stijlaartit.variants.generated;

import nl.stijlaartit.variants.generated.models.Bike;
import nl.stijlaartit.variants.generated.models.Car;
import nl.stijlaartit.variants.generated.models.Vehicle;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;

class VehicleUnionTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void deserializesVehicleToCarUsingTypeDiscriminator() {
        Vehicle vehicle = objectMapper.readValue("{\"type\":\"car\",\"brand\":\"Audi\"}", Vehicle.class);

        assertThat(vehicle).isInstanceOf(Car.class);
        assertThat(((Car) vehicle).brand()).isEqualTo("Audi");
    }

    @Test
    void deserializesVehicleToBikeUsingTypeDiscriminator() {
        Vehicle vehicle = objectMapper.readValue("{\"type\":\"bike\",\"model\":\"Gravel\"}", Vehicle.class);

        assertThat(vehicle).isInstanceOf(Bike.class);
        assertThat(((Bike) vehicle).model()).isEqualTo("Gravel");
    }
}
