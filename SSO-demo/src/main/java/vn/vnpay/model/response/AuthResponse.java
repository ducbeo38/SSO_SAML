package vn.vnpay.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author ducpa
 * Created: 21/08/2023
 */

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse<T> {

    private int code;
    private String message;
    private T data;
}