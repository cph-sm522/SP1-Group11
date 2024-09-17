package dat.entities;

import jakarta.persistence.Entity;
import lombok.*;

@Entity
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Actor extends Person {
}
