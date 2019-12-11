package uchan.weather.login;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by tpals on 2017-09-07.
 */

public class UpdateRequest extends StringRequest {

    final static private String URL = "http://uchanism24.cafe24.com/Update.php";
    private Map<String, String> parameters;

    public UpdateRequest(String userPassword, String userName, int userAge, String userID, Response.Listener<String> listener) {
        super(Method.POST, URL, listener, null);
        parameters = new HashMap<>();
        parameters.put("userPassword", userPassword);
        parameters.put("userName", userName);
        parameters.put("userAge", userAge + "");
        parameters.put("userID", userID);
    }

    @Override
    public Map<String, String> getParams() {
        return parameters;
    }
}
