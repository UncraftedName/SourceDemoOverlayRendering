package playerInfo;

import java.util.HashMap;
import java.util.Objects;

// will keep this just in case, but I don't think i'll use this
@Deprecated
public enum PlayerIcon {

    // if the path is not present or null, it's set to a default path

    Default,
    Imanex,
    UncraftedName,
    Jonah,
    LamboLord24,
    BiiWix,
    Blizik,
    diabl075,
    Fnzzy,
    riwantone,
    Skizzie,
    moose_hockey,
    michael94,
    InlandEmpireCuber,
    batjon,
    emorymaker,
    Hashi_inu,
    kojlax,
    GaryFTW,
    Bradley0130,
    SodaCan,
    Green_Snake,
    toad1750,
    NuSa,
    Pr0tal_Player,
    maltemller,
    Xeiz,
    TechnicalGordon,
    TheSwagatron,
    HellSt0ne,
    Avetixz,
    captaintrailmix,
    Dacciox,
    Tendersteel,
    eljesT,
    adamantite,
    Mitsunee,
    zetadezorro,
    sunsetbear,
    Imita,
    KnockKnock,
    Marcus_MPC;


    private static final String defaultPath = "";
    public final String path;

    PlayerIcon(final String path) {
        this.path = Objects.requireNonNullElse(path, defaultPath);
    }

    PlayerIcon() {
        this(null);
    }

    // maps the name in the demo to the player name; use getOrDefault() to get items from this map
    public static HashMap<String, PlayerIcon> demoNameMapper;

    static {
        demoNameMapper = new HashMap<>();
        demoNameMapper.put("UncraftedName", UncraftedName);
        demoNameMapper.put("Jonah", Jonah);

    }
}
