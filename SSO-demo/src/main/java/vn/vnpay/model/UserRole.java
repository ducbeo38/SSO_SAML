package vn.vnpay.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author ducpa
 * Created: 22/08/2023
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserRole {

    private String username;
    private String role;
}
