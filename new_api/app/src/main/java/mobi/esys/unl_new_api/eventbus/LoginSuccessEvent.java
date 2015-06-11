package mobi.esys.unl_new_api.eventbus;


public class LoginSuccessEvent {
    private transient final String token;

    public LoginSuccessEvent(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }
}
