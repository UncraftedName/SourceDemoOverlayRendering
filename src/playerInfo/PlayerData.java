package playerInfo;

import java.util.HashMap;

public class PlayerData {

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private final static HashMap<String, String> clientNameToDisplayName;

    static {
        clientNameToDisplayName = new HashMap<>() {{
            put("BANANA BOYE", "bill_play3");
        }};
    }

    public String getDisplayName(String clientName) {
        return clientNameToDisplayName.getOrDefault(clientName, clientName);
    }
}
